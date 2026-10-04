---
title: Solución de problemas
---

## El script no se carga

Busca en el log del servidor el nombre del `.ez` y el número de línea Ruby. Comprueba:

- ¿Es Ruby válido? Ejecuta `/ezcript lint` para sintaxis y estilo básico.
- ¿Cada bloque `do` tiene su `end`?
- ¿El nombre de `java_import` coincide con una clase Java?
- ¿Está instalado el plugin/API importado y el servidor puede acceder a sus clases?
- ¿Un método Java devolvió `nil` antes de encadenar otro método?

`include` mezcla módulos Ruby; no importa una clase Java escrita como texto. Usa `java_import 'java.util.UUID'` o el namespace JRuby `Java::...`.

## `undefined method 'update_lines'`

`update_lines(board, lines)` viene del prelude del runtime Ezcript incluido en el jar actualizado. Instala `build/libs/ezcript.jar` recompilado y detén/arranca el servidor por completo; `/ezcript reload` recarga archivos `.ez`, pero no reemplaza el código Java que ya está cargado en el plugin activo. Después usa:

```ruby
update_lines(board, [color("&aPrimera línea"), "", color("&#55ccffLínea RGB")])
```

Este helper convierte el array Ruby al `String[]` Java que requiere FastBoard. No pases el array Ruby directamente a `updateLines(String...)` de FastBoard.

## El comando no funciona

La DSL actual solo registra etiquetas de comandos de una palabra. Los argumentos se separan por espacios; no se agrupan los que tienen comillas. Busca errores al cargar y luego ejecuta `/ezcript reload`.

## El evento no se registra

Actualmente solo se aceptan los aliases de `on` que aparecen en [Comandos y eventos](./commands-and-events). Importar una clase de evento no la registra automáticamente.

## El servidor se ralentiza en un script

Los callbacks síncronos se ejecutan en el hilo principal. Evita bucles grandes, I/O de red/disco bloqueante y cálculos costosos. Respeta las reglas del scheduler antes de pasar trabajo a otro hilo.

## Los datos desaparecen al recargar

La memoria Ruby no es persistente. La recarga crea runtimes nuevos; guarda los datos explícitamente en YAML o una base de datos.

## VS Code no ofrece autocompletado Paper

Recompila el jar después de cambiar la dependencia Paper API, configura `ezcript.serverJar` para `build/libs/ezcript.jar` actualizado y ejecuta **Ezcript: Restart Language Server**. El índice incluido describe símbolos de compilación; no detecta funciones disponibles durante la ejecución.
