# Ezcript (WIP)

**Ezcript** is a Paper plugin for building server features, minigames, and game modes with Ruby scripts saved as `.ez` files. It embeds JRuby and exposes Ruby helpers for commands, Bukkit events, FastBoard scoreboards, SpiGUI menus, and legacy/RGB/MiniMessage colors.

> **Status:** early development. The runtime, basic event/command DSL, Ruby syntax diagnostics, lint checks, and a stdio Language Server Protocol (LSP) server are implemented. APIs and Paper-version compatibility may change.

## Requirements

- Paper or Purpur 26.2 (Paper API build 112) or compatible 26.x
- Java 25 or newer to run the server and plugin
- JDK 25 and Gradle 9 to build
- Node.js 20.19+ (or 22.12+) to build the documentation

The project compiles against Paper API `26.2.build.112-stable` and emits Java 25 bytecode. The bundled editor index is generated from this same API, including its public Bukkit, Paper, Adventure, FastBoard, and SpiGUI types. Paper 26.3 is still pre-release in the configured repository and is not the verified build target.

## Build and install

```powershell
.\gradlew.bat clean build
```

Copy `build/libs/ezcript.jar` to the server's `plugins` directory and start Paper once. Scripts belong in `plugins/Ezcript/scripts/`; copy [`examples/welcome.ez`](examples/welcome.ez) there as a starting point. Reload them with `/ezcript reload`; check their syntax and style with `/ezcript lint`.

The jar also provides a command-line linter and LSP server:

```powershell
java -jar build/libs/ezcript.jar lint plugins/Ezcript/scripts/example.ez
java -jar build/libs/ezcript.jar lsp
```

Configure your editor's Ruby language client to launch the second command for `.ez` files. The server communicates over standard input/output using LSP JSON-RPC.

For Visual Studio Code, build or install [Ezcript for VS Code](vscode-ezcript/README.md) from `vscode-ezcript/ezcript.vsix`. It provides colorful Ruby/Ezcript syntax highlighting, three optional editor themes, Paper/Bukkit API completion and hover, and the bundled LSP integration.

## Example

```ruby
command "hello" do |sender, args|
  sender.sendMessage("Hello from Ezcript!")
end

on :join do |event|
  event.getPlayer.sendMessage("Welcome to the server!")
end
```

See [the documentation](docs/README.md) for the DSL, supported events, UI APIs, colors, linting, and editor setup. The same guide is available in Spanish in [README.es.md](README.es.md).

The [`examples/lobby-ui.ez`](examples/lobby-ui.ez) sample demonstrates FastBoard, SpiGUI menus, and RGB/MiniMessage colors.

## License

Ezcript is distributed under the MIT License; see [LICENSE](LICENSE).
