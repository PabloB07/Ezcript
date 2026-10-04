package dev.ezcript.lsp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class PaperApiIndex {
    private final Map<String, ApiClass> classes = new HashMap<>();
    private final Map<String, List<ApiClass>> simpleNames = new HashMap<>();
    private final List<ApiClass> sortedClasses;

    private PaperApiIndex(List<ApiClass> entries) {
        for (ApiClass type : entries) {
            classes.put(type.name(), type);
            simpleNames.computeIfAbsent(type.simpleName(), ignored -> new ArrayList<>()).add(type);
        }
        sortedClasses = entries.stream().sorted((left, right) -> left.name().compareTo(right.name())).toList();
    }

    static PaperApiIndex load() throws IOException {
        try (InputStream input = PaperApiIndex.class.getResourceAsStream("/paper-api-index.json")) {
            if (input == null) return new PaperApiIndex(List.of());
            return fromJson(new ObjectMapper().readTree(input));
        }
    }

    static PaperApiIndex fromJson(JsonNode root) {
        List<ApiClass> entries = new ArrayList<>();
        for (JsonNode item : root.path("classes")) {
            List<ApiMethod> methods = new ArrayList<>();
            for (JsonNode method : item.path("methods")) {
                List<String> parameters = new ArrayList<>();
                method.path("parameters").forEach(parameter -> parameters.add(parameter.asText()));
                methods.add(new ApiMethod(
                        method.path("name").asText(),
                        method.path("returnType").asText("void"),
                        parameters,
                        method.path("staticMethod").asBoolean(),
                        method.path("varArgs").asBoolean(),
                        method.path("deprecated").asBoolean()));
            }
            List<ApiField> fields = new ArrayList<>();
            for (JsonNode field : item.path("fields")) {
                fields.add(new ApiField(
                        field.path("name").asText(),
                        field.path("type").asText("java.lang.Object"),
                        field.path("staticField").asBoolean(),
                        field.path("deprecated").asBoolean()));
            }
            List<String> interfaces = new ArrayList<>();
            item.path("interfaces").forEach(value -> interfaces.add(value.asText()));
            entries.add(new ApiClass(
                    item.path("name").asText(),
                    item.path("simpleName").asText(),
                    item.path("superName").asText(null),
                    interfaces,
                    item.path("interfaceType").asBoolean(),
                    item.path("enumType").asBoolean(),
                    methods,
                    fields));
        }
        return new PaperApiIndex(entries);
    }

    ApiClass findClass(String name) {
        ApiClass exact = classes.get(name);
        if (exact != null) return exact;
        List<ApiClass> candidates = simpleNames.get(name);
        return candidates == null || candidates.size() != 1 ? null : candidates.getFirst();
    }

    List<ApiClass> findSimpleName(String name) {
        return simpleNames.getOrDefault(name, List.of());
    }

    List<ApiClass> classes() {
        return sortedClasses;
    }

    List<ApiMethod> methods(String className) {
        Set<String> seen = new LinkedHashSet<>();
        List<ApiMethod> results = new ArrayList<>();
        collectMethods(className, seen, results, new LinkedHashSet<>());
        return results;
    }

    List<ApiField> fields(String className) {
        Set<String> seen = new LinkedHashSet<>();
        List<ApiField> results = new ArrayList<>();
        collectFields(className, seen, results, new LinkedHashSet<>());
        return results;
    }

    private void collectMethods(String className, Set<String> seen, List<ApiMethod> results, Set<String> visited) {
        if (className == null || !visited.add(className)) return;
        ApiClass type = findClass(className);
        if (type == null) return;
        for (ApiMethod method : type.methods()) {
            String signature = method.name() + "(" + String.join(",", method.parameters()) + ")";
            if (seen.add(signature)) results.add(method);
        }
        collectMethods(type.superName(), seen, results, visited);
        for (String implementedInterface : type.interfaces()) {
            collectMethods(implementedInterface, seen, results, visited);
        }
    }

    private void collectFields(String className, Set<String> seen, List<ApiField> results, Set<String> visited) {
        if (className == null || !visited.add(className)) return;
        ApiClass type = findClass(className);
        if (type == null) return;
        for (ApiField field : type.fields()) {
            if (seen.add(field.name())) results.add(field);
        }
        collectFields(type.superName(), seen, results, visited);
        for (String implementedInterface : type.interfaces()) {
            collectFields(implementedInterface, seen, results, visited);
        }
    }

    record ApiClass(
            String name,
            String simpleName,
            String superName,
            List<String> interfaces,
            boolean interfaceType,
            boolean enumType,
            List<ApiMethod> methods,
            List<ApiField> fields) { }

    record ApiMethod(
            String name,
            String returnType,
            List<String> parameters,
            boolean staticMethod,
            boolean varArgs,
            boolean deprecated) { }

    record ApiField(String name, String type, boolean staticField, boolean deprecated) { }
}
