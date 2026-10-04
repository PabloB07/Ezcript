---
title: Editor support
---

The bundled LSP server uses standard input/output JSON-RPC and supports Ruby syntax diagnostics, basic style diagnostics, keyword completions, and introductory hover text for `.ez` documents.

## Start the server

Build the jar, then configure an editor's LSP client to launch:

```text
java -jar <path-to-ezcript.jar> lsp
```

Associate `.ez` files with the Ruby language client and use stdio as the transport. The exact client configuration depends on your editor. The server currently reports trailing whitespace, lines over 120 characters, and JRuby parser errors. It also uses the Paper API index bundled in the jar for class, method, field, and Java-import completions, including inherited API methods, relevant Java standard-library types, and known event/command callback types. The index is generated from the Paper API dependency configured in the Gradle build.

## Visual Studio Code

The project includes an extension in the `vscode-ezcript/` directory. Build `ezcript.vsix` with `npm install` followed by `npm run package` from that directory, then use **Extensions: Install from VSIX...** in VS Code. The extension adds colorful Ruby-style `.ez` highlighting, three optional dark color themes (Ezcript Neon, Aurora, and Ember), and snippets; it starts the LSP process from the configured Ezcript plugin jar (`ezcript.serverJar`).

## Command-line lint

```text
java -jar <path-to-ezcript.jar> lint path/to/script.ez
```

The linter exits with status 1 if it finds issues and status 0 otherwise.
