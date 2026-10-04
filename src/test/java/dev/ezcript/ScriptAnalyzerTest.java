package dev.ezcript;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptAnalyzerTest {
    @Test
    void acceptsRubySyntax() {
        assertTrue(ScriptAnalyzer.analyze("""
                command "hello" do |sender, args|
                  sender.sendMessage("Hello")
                end
                """).isEmpty());
    }

    @Test
    void reportsTheRubyParserLine() {
        var diagnostics = ScriptAnalyzer.analyze("""
                value = 1
                command "broken" do
                """);

        assertEquals(1, diagnostics.size());
        assertEquals(2, diagnostics.getFirst().line());
        assertTrue(diagnostics.getFirst().message().contains("Ruby syntax"));
    }

    @Test
    void reportsStyleIssues() {
        var diagnostics = ScriptAnalyzer.analyze("value = 1  \n" + "x".repeat(121));

        assertEquals(2, diagnostics.size());
        assertEquals("Trailing whitespace", diagnostics.get(0).message());
        assertEquals("Line exceeds 120 characters", diagnostics.get(1).message());
    }

    @Test
    void acceptsTheBundledLobbyUiExample() throws IOException {
        String source = Files.readString(Path.of("examples", "lobby-ui.ez"));

        assertTrue(ScriptAnalyzer.analyze(source).isEmpty());
    }

    @Test
    void acceptsThePaper26ApiExample() throws IOException {
        String source = Files.readString(Path.of("examples", "paper-26-api.ez"));

        assertTrue(ScriptAnalyzer.analyze(source).isEmpty());
    }
}
