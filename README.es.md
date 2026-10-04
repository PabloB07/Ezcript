# Ezcript

**Ezcript** es un plugin de Paper para crear funciones de servidor, minijuegos y modalidades con scripts Ruby guardados como archivos `.ez`. Incluye JRuby y expone helpers Ruby para comandos, eventos Bukkit, scoreboards FastBoard, menús SpiGUI y colores legacy/RGB/MiniMessage.

> **Estado:** desarrollo inicial. Ya están implementados el runtime, la DSL básica de comandos y eventos, diagnósticos de sintaxis Ruby, comprobaciones de lint y un servidor Language Server Protocol (LSP) por stdio. La API y la compatibilidad con versiones de Paper pueden cambiar.

## Requisitos

- Paper o Purpur 26.2 (Paper API build 112) o 26.x compatible
- Java 25 o posterior para ejecutar el servidor y el plugin
- JDK 25 y Gradle 9 para compilar
- Node.js 20.19+ (o 22.12+) para compilar la documentación

El proyecto compila contra Paper API `26.2.build.112-stable` y genera bytecode Java 25. El índice del editor se genera desde esta misma API e incluye sus tipos públicos de Bukkit, Paper, Adventure, FastBoard y SpiGUI. Paper 26.3 aún es una versión previa en el repositorio configurado y no es el objetivo de compilación verificado.

## Compilar e instalar

```powershell
.\gradlew.bat clean build
```

Copia `build/libs/ezcript.jar` al directorio `plugins` del servidor e inicia Paper una vez. Los scripts van en `plugins/Ezcript/scripts/`; puedes copiar [`examples/welcome.ez`](examples/welcome.ez) como punto de partida. Recárgalos con `/ezcript reload` y revisa sintaxis y estilo con `/ezcript lint`.

El jar también incluye un linter de línea de comandos y un servidor LSP:

```powershell
java -jar build/libs/ezcript.jar lint plugins/Ezcript/scripts/example.ez
java -jar build/libs/ezcript.jar lsp
```

Configura el cliente Ruby de tu editor para iniciar el segundo comando con archivos `.ez`. El servidor se comunica por entrada/salida estándar usando LSP JSON-RPC.

Para Visual Studio Code, compila o instala [Ezcript para VS Code](vscode-ezcript/README.md) desde `vscode-ezcript/ezcript.vsix`. Incluye resaltado colorido para Ruby/Ezcript, tres temas opcionales, autocompletado y hover de Paper/Bukkit, e integración con el LSP.

## Ejemplo

```ruby
command "hola" do |sender, args|
  sender.sendMessage("¡Hola desde Ezcript!")
end

on :join do |event|
  event.getPlayer.sendMessage("¡Te damos la bienvenida!")
end
```

Consulta la [documentación](docs/README.md) sobre la DSL, los eventos, las APIs de interfaz, los colores, lint y la configuración del editor. La guía también está en inglés en [README.md](README.md).

El ejemplo [`examples/lobby-ui.ez`](examples/lobby-ui.ez) muestra FastBoard, menús SpiGUI y colores RGB/MiniMessage.

## Licencia

Ezcript se distribuye bajo la licencia MIT; consulta [LICENSE](LICENSE).
