package dev.ezcript;

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScriptAnalyzer {
    private static final Pattern LINE_NUMBER = Pattern.compile("(?:<script>|[^\\r\\n]*\\.ez):(\\d+):");
    private static final int MAX_LINE_LENGTH = 120;
    private static final ScriptingContainer PARSER = new ScriptingContainer(LocalContextScope.SINGLETHREAD);

    private ScriptAnalyzer() { }

    public static List<Diagnostic> analyze(String source) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        try {
            synchronized (PARSER) {
                PARSER.parse(source);
            }
        } catch (RuntimeException exception) {
            String message = exception.getMessage() == null ? "Invalid Ruby syntax" : exception.getMessage();
            Matcher matcher = LINE_NUMBER.matcher(message);
            int line = matcher.find() ? Integer.parseInt(matcher.group(1)) : 1;
            diagnostics.add(new Diagnostic(Math.max(line, 1), "Ruby syntax: " + message, 1));
        }

        String[] lines = source.split("\\R", -1);
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            if (!line.isEmpty() && Character.isWhitespace(line.charAt(line.length() - 1))) {
                diagnostics.add(new Diagnostic(index + 1, "Trailing whitespace", 2));
            }
            if (line.length() > MAX_LINE_LENGTH) {
                diagnostics.add(new Diagnostic(index + 1, "Line exceeds " + MAX_LINE_LENGTH + " characters", 2));
            }
        }
        return diagnostics;
    }

    public record Diagnostic(int line, String message, int severity) { }
}
