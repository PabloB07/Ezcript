# Ezcript for Visual Studio Code

Ruby-powered editing support for Ezcript `.ez` scripts:

- A colorful Ruby-oriented `.ez` grammar for the Ezcript DSL, Java imports, Paper API types, string interpolation, method calls, symbols, variables, and comments.
- Three original dark themes: **Ezcript Neon**, **Ezcript Aurora**, and **Ezcript Ember**.
- Snippets for server commands and Paper/Bukkit event handlers.
- Diagnostics, hover, and completion powered by the Language Server Protocol server inside the Ezcript plugin jar.
- Paper/Bukkit/Adventure API completions generated from the Paper API used to build the Ezcript jar, including inherited Java methods, referenced Java standard-library types, and Java imports.
- `Ezcript: Lint Current File` and `Ezcript: Restart Language Server` commands.

## Requirements

- Visual Studio Code 1.85 or newer.
- Java 25 or newer available as `java` on `PATH`, or set `ezcript.javaPath`.
- A built Ezcript plugin jar, which also contains the LSP server and command-line linter.

## Setup

1. Install the Ezcript plugin jar on your Paper/Purpur server using the project's installation instructions.
2. Open the Ezcript project in VS Code and install `ezcript.vsix`, or install this extension from a VSIX.
3. Open an `.ez` file. By default, the extension starts the language server from `build/libs/ezcript.jar`.
4. If your plugin jar is elsewhere, set **Ezcript: Server Jar** to its absolute path or a path relative to the first VS Code workspace folder.
5. Set **Ezcript: Java Path** if the Java executable is not named `java` or is not on `PATH`.

The jar must be accessible on the computer running VS Code. The extension starts it as a separate process; it does not connect to the Minecraft server over the network.

## Commands

- **Ezcript: Lint Current File** runs the bundled Ruby parser/style linter on the active `.ez` file and writes the result to the Ezcript output channel.
- **Ezcript: Restart Language Server** restarts diagnostics and editor features after updating the plugin jar or configuration.

Use `command` and `on` to insert the included snippets. Import Paper classes in Ruby with `java_import 'org.bukkit.Bukkit'`; API completions are read from the bundled Paper 26.2 API index and support inherited methods and event/command callback types.

Select **Ezcript Neon**, **Ezcript Aurora**, or **Ezcript Ember** from **Preferences: Color Theme** (`Ctrl+K Ctrl+T` on Windows/Linux, `Cmd+K Cmd+T` on macOS). The themes are original palettes designed for Ezcript's Ruby syntax and are optional; the extension does not change your theme automatically.

The visual direction takes inspiration from syntax-highlighting extensions that offer expressive language grammars and several color themes, such as [Sk-VSC](https://github.com/AyhamAl-Ali/Sk-VSC). Ezcript's Ruby grammar and all three palettes are original.

## Build the VSIX

From this directory, run:

```powershell
npm install
npm run package
```

The generated `ezcript.vsix` can be installed from the VS Code command palette with **Extensions: Install from VSIX...**. The output filename deliberately does not contain a version; VS Code still requires the extension manifest to carry its internal version. The language client is bundled into the extension so the VSIX does not require a separate npm install.
