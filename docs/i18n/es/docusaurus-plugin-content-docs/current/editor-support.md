---
title: Soporte para editores
---

El servidor LSP incluido usa JSON-RPC por entrada/salida estándar y ofrece diagnósticos de sintaxis Ruby, diagnósticos básicos de estilo, completado de palabras clave y texto introductorio al pasar el cursor por documentos `.ez`.

## Iniciar el servidor

Compila el jar y configura el cliente LSP del editor para iniciar:

```text
java -jar <ruta-al-jar-ezcript.jar> lsp
```

Asocia los archivos `.ez` con el cliente de lenguaje Ruby y usa stdio como transporte. La configuración exacta depende del editor. El servidor informa espacios finales, líneas mayores de 120 caracteres y errores del parser de JRuby. También usa el índice Paper integrado en el jar para completar clases, métodos, campos e imports Java, incluidos métodos heredados, tipos relevantes de la biblioteca estándar de Java y tipos conocidos en callbacks de comandos y eventos. Gradle genera el índice desde la dependencia Paper API configurada para compilar.

## Visual Studio Code

El proyecto incluye una extensión en el directorio `vscode-ezcript/`. Genera `ezcript.vsix` con `npm install` y `npm run package` desde ese directorio, e instálalo en VS Code con **Extensions: Install from VSIX...**. Incluye resaltado colorido Ruby para `.ez`, tres temas oscuros opcionales (Ezcript Neon, Aurora y Ember), snippets y conexión al LSP integrado en el jar de Ezcript. Configura `ezcript.serverJar` si el jar no está en la ruta predeterminada.

## Lint desde la línea de comandos

```text
java -jar <ruta-al-jar-ezcript.jar> lint ruta/al/script.ez
```

El linter termina con código 1 si encuentra problemas y con código 0 si no encuentra ninguno.
