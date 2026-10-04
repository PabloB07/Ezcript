package dev.ezcript.lsp;

import dev.ezcript.ScriptAnalyzer;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Main {
    private Main() { }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args[0].equals("lsp")) {
            new LanguageServer().run();
            return;
        }
        if (args[0].equals("lint")) {
            int errors = 0;
            for (int index = 1; index < args.length; index++) {
                Path file = Path.of(args[index]);
                for (var diagnostic : ScriptAnalyzer.analyze(Files.readString(file))) {
                    System.out.printf("%s:%d: %s%n", file, diagnostic.line(), diagnostic.message());
                    errors++;
                }
            }
            if (errors > 0) System.exit(1);
            return;
        }
        System.err.println("Usage: java -jar Ezcript.jar [lsp | lint <file.ez>...]");
        System.exit(2);
    }
}
