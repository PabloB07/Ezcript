package dev.ezcript.lsp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ezcript.ScriptAnalyzer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class LanguageServer {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");
    private static final Pattern RECEIVER_EXPRESSION = Pattern.compile(
            "(?:[A-Za-z_$][A-Za-z0-9_$]*|Java(?:::[A-Za-z_$][A-Za-z0-9_$]*)+)"
                    + "(?:\\s*\\([^()]*\\))?(?:\\s*(?:\\.|::)\\s*[A-Za-z_$][A-Za-z0-9_$]*"
                    + "(?:\\s*\\([^()]*\\))?)*");
    private static final Pattern JAVA_IMPORT = Pattern.compile(
            "\\bjava_import\\s+(?:\\[([^\\]]*)]|['\"]([^'\"]+)['\"]|([A-Za-z_$][\\w.$:]*))");
    private static final Pattern IMPORTED_STRING = Pattern.compile("['\"]([^'\"]+)['\"]");
    private static final Pattern EVENT_HANDLER = Pattern.compile(
            "\\bon\\s+:(\\w+)\\s+do\\s*\\|\\s*([A-Za-z_]\\w*)");
    private static final Pattern COMMAND_HANDLER = Pattern.compile(
            "\\bcommand\\s+['\"][^'\"]+['\"]\\s+do\\s*\\|\\s*([A-Za-z_]\\w*)");
    private static final Pattern ASSIGNMENT = Pattern.compile(
            "^\\s*([A-Za-z_]\\w*)\\s*=\\s*(.+?)\\s*(?:#.*)?$");
    private static final List<String> KEYWORDS = List.of(
            "command", "on", "do", "end", "if", "elsif", "else", "unless", "case",
            "when", "while", "until", "each", "times", "true", "false", "nil", "self",
            "java_import", "include");
    private static final Map<String, String> EVENT_TYPES = Map.ofEntries(
            Map.entry("join", "org.bukkit.event.player.PlayerJoinEvent"),
            Map.entry("player_join", "org.bukkit.event.player.PlayerJoinEvent"),
            Map.entry("quit", "org.bukkit.event.player.PlayerQuitEvent"),
            Map.entry("player_quit", "org.bukkit.event.player.PlayerQuitEvent"),
            Map.entry("death", "org.bukkit.event.entity.PlayerDeathEvent"),
            Map.entry("player_death", "org.bukkit.event.entity.PlayerDeathEvent"),
            Map.entry("break", "org.bukkit.event.block.BlockBreakEvent"),
            Map.entry("block_break", "org.bukkit.event.block.BlockBreakEvent"),
            Map.entry("place", "org.bukkit.event.block.BlockPlaceEvent"),
            Map.entry("block_place", "org.bukkit.event.block.BlockPlaceEvent"),
            Map.entry("interact", "org.bukkit.event.player.PlayerInteractEvent"),
            Map.entry("player_interact", "org.bukkit.event.player.PlayerInteractEvent"),
            Map.entry("damage", "org.bukkit.event.entity.EntityDamageByEntityEvent"),
            Map.entry("entity_damage", "org.bukkit.event.entity.EntityDamageByEntityEvent"));

    private final Map<String, String> documents = new HashMap<>();
    private final PaperApiIndex paperApi;

    LanguageServer() throws IOException {
        paperApi = PaperApiIndex.load();
    }

    void run() throws IOException {
        BufferedInputStream input = new BufferedInputStream(System.in);
        while (true) {
            byte[] body = readMessage(input);
            if (body == null) return;
            JsonNode request = JSON.readTree(body);
            String method = request.path("method").asText("");
            JsonNode id = request.get("id");
            JsonNode params = request.path("params");
            switch (method) {
                case "initialize" -> respond(id, Map.of("capabilities", Map.of(
                        "textDocumentSync", 1,
                        "completionProvider", Map.of("triggerCharacters", List.of(":", ".")),
                        "hoverProvider", true)));
                case "initialized" -> { }
                case "shutdown" -> respond(id, null);
                case "exit" -> { return; }
                case "textDocument/didOpen" -> {
                    JsonNode doc = params.path("textDocument");
                    String uri = doc.path("uri").asText();
                    documents.put(uri, doc.path("text").asText(""));
                    publishDiagnostics(uri, documents.get(uri));
                }
                case "textDocument/didChange" -> {
                    String uri = params.path("textDocument").path("uri").asText();
                    JsonNode changes = params.path("contentChanges");
                    if (!changes.isEmpty() && documents.containsKey(uri)) {
                        String text = changes.get(changes.size() - 1).path("text").asText("");
                        documents.put(uri, text);
                        publishDiagnostics(uri, text);
                    }
                }
                case "textDocument/didClose" -> {
                    String uri = params.path("textDocument").path("uri").asText();
                    documents.remove(uri);
                    notifyClient("textDocument/publishDiagnostics", Map.of("uri", uri, "diagnostics", List.of()));
                }
                case "textDocument/completion" -> respond(id, completion(params));
                case "textDocument/hover" -> respond(id, hover(params));
                default -> {
                    if (id != null && !id.isNull()) respond(id, null);
                }
            }
        }
    }

    private Object completion(JsonNode params) {
        String uri = params.path("textDocument").path("uri").asText();
        String source = documents.get(uri);
        if (source == null) return List.of();

        int lineNumber = Math.max(0, params.path("position").path("line").asInt());
        int character = Math.max(0, params.path("position").path("character").asInt());
        String[] lines = source.split("\\R", -1);
        if (lineNumber >= lines.length) return List.of();
        String linePrefix = prefixAt(lines[lineNumber], character);
        List<Map<String, Object>> items = new ArrayList<>();

        Matcher importMatcher = Pattern.compile("\\bjava_import\\s+['\"]([^'\"]*)$")
                .matcher(linePrefix);
        if (importMatcher.find()) {
            int quote = Math.max(linePrefix.lastIndexOf('\''), linePrefix.lastIndexOf('"'));
            addImportCompletions(items, importMatcher.group(1), lineNumber, quote + 1, character);
            return Map.of("isIncomplete", items.size() >= 200, "items", items);
        }

        int dot = linePrefix.lastIndexOf('.');
        if (dot >= 0) {
            String receiver = receiverBefore(linePrefix, dot);
            String memberPrefix = linePrefix.substring(dot + 1).stripLeading();
            Matcher member = IDENTIFIER.matcher(memberPrefix);
            if (member.find() && member.start() == 0) memberPrefix = member.group();
            else memberPrefix = "";
            if (!receiver.isBlank()) {
                String type = resolveExpressionType(source, lineNumber, receiver, new HashMap<>(), imports(source));
                if (type != null) {
                    addMemberCompletions(items, type, memberPrefix, isClassReceiver(receiver, imports(source)));
                    return Map.of("isIncomplete", items.size() >= 250, "items", items);
                }
            }
        }

        String prefix = trailingIdentifier(linePrefix);
        for (String keyword : KEYWORDS) {
            if (keyword.startsWith(prefix)) {
                items.add(Map.of("label", keyword, "kind", 14, "detail", "Ruby / Ezcript"));
            }
        }
        if (!prefix.isEmpty()) addClassCompletions(items, prefix);
        return Map.of("isIncomplete", items.size() >= 200, "items", items);
    }

    private Object hover(JsonNode params) {
        String uri = params.path("textDocument").path("uri").asText();
        String source = documents.get(uri);
        if (source == null) return null;

        int lineNumber = Math.max(0, params.path("position").path("line").asInt());
        int character = Math.max(0, params.path("position").path("character").asInt());
        String[] lines = source.split("\\R", -1);
        if (lineNumber >= lines.length) return null;
        String line = lines[lineNumber];
        String word = wordAt(line, character);
        if (word.isBlank()) return null;

        if (word.equals("command") || word.equals("on") || word.equals("java_import")) {
            String description = switch (word) {
                case "command" -> "`command \"name\" do |sender, args| ... end` registers a script command. `sender` is a Bukkit `CommandSender`.";
                case "on" -> "`on :join do |event| ... end` registers a supported Paper/Bukkit event. The event parameter is typed from the event name.";
                default -> "`java_import 'org.bukkit.Bukkit'` imports a Paper API class into the Ruby script. JRuby Java interop then exposes its methods.";
            };
            return hoverContents(description);
        }

        int wordStart = line.indexOf(word, Math.min(character, Math.max(0, line.length() - 1)));
        if (wordStart < 0) wordStart = line.lastIndexOf(word, Math.min(character, line.length()));
        String beforeWord = line.substring(0, Math.max(wordStart, 0));
        String type = null;
        Matcher memberAccess = Pattern.compile("([A-Za-z_$][\\w.$:]*)\\s*\\.\\s*$").matcher(beforeWord);
        if (memberAccess.find()) {
            type = resolveExpressionType(source, lineNumber, memberAccess.group(1),
                    new HashMap<>(), imports(source));
        }
        if (type != null) {
            for (PaperApiIndex.ApiMethod method : paperApi.methods(type)) {
                if (method.name().equals(word)) {
                    return hoverContents("`" + method.returnType() + " " + type + "." + signature(method)
                            + (method.deprecated() ? "`\n\nDeprecated Paper API." : "`"));
                }
            }
            for (PaperApiIndex.ApiField field : paperApi.fields(type)) {
                if (field.name().equals(word)) {
                    return hoverContents("`" + field.type() + " " + type + "." + field.name()
                            + (field.deprecated() ? "`\n\nDeprecated Paper API." : "`"));
                }
            }
        }

        PaperApiIndex.ApiClass apiClass = resolveClass(word, imports(source));
        if (apiClass != null) {
            String typeDescription = apiClass.interfaceType() ? "interface" : apiClass.enumType() ? "enum" : "class";
            String inherited = apiClass.superName() == null ? "" : "\n\nExtends `" + apiClass.superName() + "`.";
            return hoverContents("Paper API " + typeDescription + ": `" + apiClass.name() + "`" + inherited);
        }
        return null;
    }

    private void addMemberCompletions(
            List<Map<String, Object>> items,
            String type,
            String prefix,
            boolean classReceiver) {
        for (PaperApiIndex.ApiMethod method : paperApi.methods(type)) {
            if (!method.name().startsWith(prefix)) continue;
            if (method.staticMethod() != classReceiver) continue;
            String label = method.name();
            String detail = method.returnType() + " " + signature(method);
            if (method.staticMethod()) detail = "static " + detail;
            if (method.deprecated()) detail += " (deprecated)";
            items.add(Map.of(
                    "label", label,
                    "kind", 2,
                    "detail", detail,
                    "filterText", method.name()));
        }

        for (PaperApiIndex.ApiField field : paperApi.fields(type)) {
            if (!field.name().startsWith(prefix)) continue;
            if (field.staticField() != classReceiver) continue;
            items.add(Map.of(
                    "label", field.name(),
                    "kind", field.staticField() ? 21 : 5,
                    "detail", field.type() + (field.deprecated() ? " (deprecated)" : "")));
        }

        if (classReceiver) {
            items.add(Map.of("label", "new", "kind", 2, "detail", "Create a new " + type + " instance"));
        }
        for (PaperApiIndex.ApiMethod method : paperApi.methods(type)) {
            if (classReceiver || method.staticMethod() || !method.parameters().isEmpty()) continue;
            String property = beanProperty(method.name());
            if (property == null || !property.startsWith(prefix) || property.equals(method.name())) continue;
            items.add(Map.of(
                    "label", property,
                    "kind", 10,
                    "detail", "Ruby Java property: " + method.returnType() + " (" + method.name() + ")"));
        }
        items.sort((left, right) -> left.get("label").toString().compareToIgnoreCase(right.get("label").toString()));
        if (items.size() > 250) items.subList(250, items.size()).clear();
    }

    private void addClassCompletions(List<Map<String, Object>> items, String prefix) {
        for (PaperApiIndex.ApiClass apiClass : paperApi.classes()) {
            String candidate = apiClass.simpleName();
            if (!candidate.regionMatches(true, 0, prefix, 0, prefix.length())) continue;
            items.add(Map.of(
                    "label", candidate,
                    "kind", apiClass.enumType() ? 13 : 7,
                    "detail", apiClass.name(),
                    "filterText", candidate));
            if (items.size() >= 200) return;
        }
    }

    private void addImportCompletions(
            List<Map<String, Object>> items,
            String prefix,
            int line,
            int startCharacter,
            int endCharacter) {
        for (PaperApiIndex.ApiClass apiClass : paperApi.classes()) {
            String candidate = apiClass.name();
            if (!candidate.regionMatches(true, 0, prefix, 0, prefix.length())) continue;
            items.add(Map.of(
                    "label", candidate,
                    "kind", apiClass.enumType() ? 13 : 7,
                    "detail", candidate,
                    "textEdit", Map.of(
                            "range", Map.of(
                                    "start", Map.of("line", line, "character", startCharacter),
                                    "end", Map.of("line", line, "character", endCharacter)),
                            "newText", candidate)));
            if (items.size() >= 200) return;
        }
    }

    private String resolveExpressionType(
            String source,
            int lineNumber,
            String expression,
            Map<String, String> variables,
            Map<String, String> importedTypes) {
        return resolveExpressionType(source, lineNumber, expression, variables, importedTypes,
                new java.util.HashSet<>());
    }

    private String resolveExpressionType(
            String source,
            int lineNumber,
            String expression,
            Map<String, String> variables,
            Map<String, String> importedTypes,
            java.util.Set<String> resolvingVariables) {
        String normalized = expression.strip().replace("::", ".");
        normalized = normalized.replaceAll("\\s*\\([^()]*\\)", "");
        if (normalized.isBlank()) return null;
        String[] segments = normalized.split("\\.");
        String root = segments[0];
        String type = variables.get(root);
        if (type == null) type = importedTypes.get(root);
        if (type == null && root.equals("Java") && segments.length > 1) {
            type = resolveJavaNamespace(normalized);
            return type;
        }
        if (type == null) {
            for (PaperApiIndex.ApiClass candidate : paperApi.findSimpleName(root)) {
                if (candidate.name().startsWith("org.bukkit.")
                        || candidate.name().startsWith("io.papermc.paper.")
                        || candidate.name().startsWith("com.destroystokyo.paper.")
                        || candidate.name().startsWith("net.kyori.adventure.")) {
                    type = candidate.name();
                    break;
                }
            }
        }
        if (type == null) {
            type = inferBlockParameter(source, lineNumber, root);
            if (type != null) variables.put(root, type);
        }
        if (type == null) {
            if (!resolvingVariables.add(root)) return null;
            String[] lines = source.split("\\R", -1);
            for (int index = 0; index <= lineNumber && index < lines.length; index++) {
                Matcher assignment = ASSIGNMENT.matcher(lines[index]);
                if (assignment.matches() && !assignment.group(2).strip().equals(root)) {
                    String assignedType = resolveExpressionType(
                            String.join("\n", lines), index, assignment.group(2),
                            variables, importedTypes, resolvingVariables);
                    if (assignedType != null) variables.put(assignment.group(1), assignedType);
                }
            }
            resolvingVariables.remove(root);
            type = variables.get(root);
        }
        if (type == null) return null;
        for (int index = 1; index < segments.length; index++) {
            String methodName = segments[index];
            String returnType = methodReturnType(type, methodName);
            if (returnType == null || returnType.equals("void")) return null;
            type = apiTypeFromReturn(returnType);
            if (type == null) return null;
        }
        return type;
    }

    private String inferBlockParameter(String source, int lineNumber, String parameter) {
        String[] lines = source.split("\\R", -1);
        String priorLines = String.join("\n", java.util.Arrays.copyOfRange(lines, 0, Math.min(lineNumber + 1, lines.length)));
        Matcher eventMatcher = EVENT_HANDLER.matcher(priorLines);
        String inferredEventType = null;
        String eventParameter = null;
        while (eventMatcher.find()) {
            eventParameter = eventMatcher.group(2);
            inferredEventType = EVENT_TYPES.get(eventMatcher.group(1));
        }
        if (parameter.equals(eventParameter)) return inferredEventType;

        Matcher commandMatcher = COMMAND_HANDLER.matcher(priorLines);
        String senderParameter = null;
        while (commandMatcher.find()) senderParameter = commandMatcher.group(1);
        if (parameter.equals(senderParameter)) return "org.bukkit.command.CommandSender";
        return null;
    }

    private String methodReturnType(String receiverType, String methodName) {
        String propertyMethod = propertyMethod(methodName);
        for (PaperApiIndex.ApiMethod method : paperApi.methods(receiverType)) {
            if ((method.name().equals(methodName) || method.name().equals(propertyMethod))
                    && method.parameters().isEmpty()) {
                return method.returnType();
            }
        }
        return null;
    }

    private String apiTypeFromReturn(String returnType) {
        String typeName = returnType.replace('$', '.');
        String erasedType = typeName.replaceFirst("<.*$", "").replaceFirst("\\[\\]$", "").strip();
        return paperApi.findClass(erasedType) == null ? null : erasedType;
    }

    private String resolveJavaNamespace(String expression) {
        String[] parts = expression.split("\\.");
        if (parts.length < 2) return null;
        String className = parts[parts.length - 1];
        String packageName = String.join(".", java.util.Arrays.copyOfRange(parts, 1, parts.length - 1));
        String dottedPackage = packageName.replaceAll("([a-z])([A-Z])", "$1.$2").toLowerCase();
        String candidate = dottedPackage.isEmpty() ? className : dottedPackage + "." + className;
        PaperApiIndex.ApiClass exact = paperApi.findClass(candidate);
        if (exact != null) return exact.name();
        List<PaperApiIndex.ApiClass> candidates = paperApi.findSimpleName(className);
        return candidates.size() == 1 ? candidates.getFirst().name() : null;
    }

    private Map<String, String> imports(String source) {
        Map<String, String> result = new HashMap<>();
        Matcher matcher = JAVA_IMPORT.matcher(source);
        while (matcher.find()) {
            String importBody = matcher.group(1) != null ? matcher.group(1)
                    : matcher.group(2) != null ? matcher.group(2)
                    : matcher.group(3);
            Matcher className = IMPORTED_STRING.matcher(importBody);
            if (matcher.group(1) == null && matcher.group(2) != null) {
                result.put(simpleName(importBody), importBody);
            } else if (matcher.group(1) == null && matcher.group(3) != null) {
                result.put(simpleName(importBody), importBody.replace("::", "."));
            } else {
                while (className.find()) {
                    String name = className.group(1).replace("::", ".");
                    result.put(simpleName(name), name);
                }
            }
        }
        return result;
    }

    private PaperApiIndex.ApiClass resolveClass(String name, Map<String, String> importedTypes) {
        String imported = importedTypes.get(name);
        if (imported != null) return paperApi.findClass(imported);
        List<PaperApiIndex.ApiClass> matches = paperApi.findSimpleName(name);
        return matches.size() == 1 ? matches.getFirst() : null;
    }

    private boolean isClassReceiver(String receiver, Map<String, String> importedTypes) {
        String normalized = receiver.strip();
        if (normalized.startsWith("Java::")) return true;
        if (normalized.contains(".") || normalized.contains("(")) return false;
        if (importedTypes.containsKey(normalized)) return true;
        return resolveClass(normalized, importedTypes) != null;
    }

    private static String receiverBefore(String linePrefix, int dot) {
        String left = linePrefix.substring(0, dot).stripTrailing();
        Matcher matcher = RECEIVER_EXPRESSION.matcher(left);
        String receiver = null;
        while (matcher.find()) {
            if (matcher.end() == left.length()) receiver = matcher.group();
        }
        return receiver == null ? "" : receiver;
    }

    private static String signature(PaperApiIndex.ApiMethod method) {
        return method.name() + "(" + String.join(", ", method.parameters()) + ")";
    }

    private static String simpleName(String qualifiedName) {
        int separator = Math.max(qualifiedName.lastIndexOf('.'), qualifiedName.lastIndexOf(':'));
        return qualifiedName.substring(separator + 1);
    }

    private static String propertyMethod(String name) {
        return name.length() == 1 ? "get" + name.toUpperCase()
                : "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static String beanProperty(String method) {
        if (method.startsWith("get") && method.length() > 3) {
            return Character.toLowerCase(method.charAt(3)) + method.substring(4);
        }
        if (method.startsWith("is") && method.length() > 2) {
            return Character.toLowerCase(method.charAt(2)) + method.substring(3);
        }
        return null;
    }

    private static String trailingIdentifier(String value) {
        Matcher matcher = IDENTIFIER.matcher(value);
        String result = "";
        while (matcher.find()) result = matcher.group();
        return result;
    }

    private static String prefixAt(String value, int character) {
        return value.substring(0, Math.min(character, value.length()));
    }

    private static String wordAt(String line, int character) {
        int offset = Math.min(character, line.length());
        int start = offset;
        int end = offset;
        while (start > 0 && (Character.isLetterOrDigit(line.charAt(start - 1))
                || line.charAt(start - 1) == '_' || line.charAt(start - 1) == '$')) start--;
        while (end < line.length() && (Character.isLetterOrDigit(line.charAt(end))
                || line.charAt(end) == '_' || line.charAt(end) == '$')) end++;
        return line.substring(start, end);
    }

    private static Object hoverContents(String markdown) {
        return Map.of("contents", Map.of("kind", "markdown", "value", markdown));
    }

    private void publishDiagnostics(String uri, String source) throws IOException {
        List<Map<String, Object>> diagnostics = new ArrayList<>();
        for (var diagnostic : ScriptAnalyzer.analyze(source)) {
            int line = Math.max(0, diagnostic.line() - 1);
            diagnostics.add(Map.of(
                    "range", Map.of("start", Map.of("line", line, "character", 0),
                            "end", Map.of("line", line, "character", 1)),
                    "severity", diagnostic.severity(),
                    "source", "ezcript",
                    "message", diagnostic.message()));
        }
        notifyClient("textDocument/publishDiagnostics", Map.of("uri", uri, "diagnostics", diagnostics));
    }

    private static byte[] readMessage(BufferedInputStream input) throws IOException {
        StringBuilder headers = new StringBuilder();
        int previous = -1;
        int current;
        while ((current = input.read()) != -1) {
            headers.append((char) current);
            if (previous == '\r' && current == '\n' && headers.toString().endsWith("\r\n\r\n")) break;
            previous = current;
        }
        if (headers.length() == 0) return null;
        int contentLength = -1;
        for (String line : headers.toString().split("\r\n")) {
            if (line.regionMatches(true, 0, "Content-Length:", 0, 15)) {
                contentLength = Integer.parseInt(line.substring(15).trim());
            }
        }
        if (contentLength < 0) throw new IOException("LSP message is missing Content-Length");
        byte[] body = input.readNBytes(contentLength);
        if (body.length != contentLength) throw new IOException("Truncated LSP message");
        return body;
    }

    private static synchronized void respond(JsonNode id, Object result) throws IOException {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("result", result);
        send(response);
    }

    private static synchronized void notifyClient(String method, Object params) throws IOException {
        send(Map.of("jsonrpc", "2.0", "method", method, "params", params));
    }

    private static void send(Object value) throws IOException {
        byte[] body = JSON.writeValueAsBytes(value);
        System.out.write(("Content-Length: " + body.length + "\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
        System.out.write(body);
        System.out.flush();
    }
}
