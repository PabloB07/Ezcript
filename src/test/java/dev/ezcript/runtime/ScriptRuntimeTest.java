package dev.ezcript.runtime;

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ScriptRuntimeTest {
    @Test
    void capturesAndInvokesRubyBlocksWithJavaCommandArguments() {
        ScriptingContainer ruby = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
        CallbackBridge bridge = new CallbackBridge();
        try {
            ruby.put("$bridge", bridge);
            ruby.runScriptlet("""
                    def register_command(&callback)
                      $bridge.register(callback)
                    end
                    register_command { |sender, args| "#{sender}: #{args.to_a.join(' ')}" }
                    """);

            assertNotNull(bridge.callback);
            Object result = ruby.callMethod(bridge.callback, "call",
                    new Object[]{"console", new String[]{"one", "two"}}, Object.class);
            assertEquals("console: one two", result);
        } finally {
            ruby.terminate();
        }
    }

    @Test
    void importsAndUsesJavaClassesWithRubyJavaImport() {
        ScriptingContainer ruby = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
        try {
            Object result = ruby.runScriptlet("""
                    java_import 'java.util.UUID'
                    UUID.randomUUID.toString.length
                    """);

            assertEquals("36", result.toString());
        } finally {
            ruby.terminate();
        }
    }

    @Test
    void convertsRubyArraysForJavaListParameters() {
        ScriptingContainer ruby = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
        CollectionBridge bridge = new CollectionBridge();
        try {
            ruby.put("$bridge", bridge);
            Object result = ruby.runScriptlet("$bridge.join(['one', 'two'])");

            assertEquals("one,two", result.toString());
            ruby.runScriptlet("$bridge.update_board_lines('board', ['first', '', 'third'])");
            assertArrayEquals(new String[]{"first", "", "third"}, bridge.lines);
        } finally {
            ruby.terminate();
        }
    }

    @Test
    void definesUpdateLinesInTheScriptPreludeAndConvertsItsRubyArray() {
        ScriptingContainer ruby = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
        CollectionBridge bridge = new CollectionBridge();
        try {
            ruby.put("$ez", bridge);
            ruby.runScriptlet(ScriptRuntime.rubyPrelude());
            Object callback = ruby.runScriptlet(
                    "proc { |board| update_lines(board, ['first', '', 'third']) }");

            ruby.callMethod(callback, "call", new Object[]{"board"}, Object.class);

            assertArrayEquals(new String[]{"first", "", "third"}, bridge.lines);
        } finally {
            ruby.terminate();
        }
    }

    public static final class CallbackBridge {
        private Object callback;

        public void register(Object callback) {
            this.callback = callback;
        }
    }

    public static final class CollectionBridge {
        private String[] lines;

        public String join(List<?> values) {
            return String.join(",", values.stream().map(String::valueOf).toList());
        }

        public void update_board_lines(Object board, List<?> values) {
            lines = FastBoardSupport.toLines(values);
        }
    }
}
