---
title: DSL de Ruby
---

Los archivos Ezcript son código Ruby normal con extensión `.ez`. JRuby ejecuta los scripts dentro del plugin, y la API Java de Bukkit/Paper está disponible mediante la interoperabilidad normal de Java.

Ezcript busca cubrir los casos de uso de scripting de servidor de Skript conservando la sintaxis Ruby y haciendo visible la API Java. Eso **no** significa que ya estén implementadas todas las expresiones, aliases de eventos, efectos convenientes o sintaxis de add-ons de Skript. Esta referencia diferencia las capacidades de Ruby/Paper de las funciones específicas de Ezcript.

## Guía de referencia

- [Sintaxis Ruby para Ezcript](./ruby-basics)
- [Comandos y eventos](./commands-and-events)
- [Ejemplo de minijuego](./minigame-example)
- [Condiciones, bucles y control de flujo](./control-flow)
- [Valores y persistencia](./data-and-persistence)
- [API de Paper, Bukkit y Adventure](./paper-api)
- [Ejemplos de API Paper 26](./paper-26-examples)
- [FastBoard, SpiGUI y colores de texto](./paper-api#scoreboards-fastboard)
- [Funciones y reutilización](./functions-and-reuse)
- [Tareas y temporizadores](./tasks-and-timers)
- [Migración desde Skript](./skript-migration)
- [Cobertura de funciones y hoja de ruta](./feature-coverage)
- [Solución de problemas](./troubleshooting)

La [documentación oficial de Skript](https://docs.skriptlang.org/) describe el conjunto actual de funciones de Skript. Consulta la [matriz de cobertura](./feature-coverage) y la [guía de migración](./skript-migration) para ver cómo se mapean y qué soporta realmente el runtime actual.

## API de Paper e imports Ruby

Los scripts pueden usar toda la API pública expuesta por la dependencia Paper API, no solo la DSL de eventos de Ezcript. Importa clases Java con `java_import` y llama a sus métodos usando sintaxis Ruby. `include` en Ruby espera un módulo; no uses `include 'java.util.Random'` para importar una clase Java:

```ruby
java_import 'org.bukkit.Bukkit'
java_import 'org.bukkit.GameMode'
java_import 'org.bukkit.entity.Player'

command "survival" do |sender, args|
  if sender.is_a?(Player)
    sender.setGameMode(GameMode::SURVIVAL)
    sender.sendMessage("¡Bienvenido a #{Bukkit.getServer.getName}!")
  end
end
```

El autocompletado del editor se genera desde la Paper API usada para compilar Ezcript. Incluye clases públicas de Bukkit, Paper y Adventure, métodos heredados, campos, tipos referenciados de la biblioteca estándar de Java y sugerencias para imports Java. El índice integrado corresponde a Paper API `26.2.build.112-stable`; al cambiar la versión de la dependencia, recompila Ezcript y usa el jar actualizado en el editor.

## DSL disponible actualmente

La DSL integrada de Ezcript ofrece `command`, `on`, helpers para FastBoard/SpiGUI y helpers de color. Ruby, el control de flujo, los métodos Ruby y la interoperabilidad Java vienen de JRuby; eso **no** añade expresiones como `balance del jugador`, efectos `send "hola" to player`, variables de lista Skript ni un efecto `wait` de Skript.

## Comandos

Registra un comando con un bloque. El callback recibe el `CommandSender` y un `String[]` con argumentos separados por espacios:

```ruby
command "anunciar" do |sender, args|
  mensaje = args.to_a.join(" ")
  sender.sendMessage("Anuncio: #{mensaje}")
end
```

Por ahora los argumentos se separan por espacios; no se analizan argumentos entre comillas. Si coincide una etiqueta, el comando de script tiene prioridad.

## Eventos

Registra un evento con `on`. El callback recibe el objeto del evento Bukkit:

```ruby
on :join do |event|
  jugador = event.getPlayer
  jugador.sendMessage("¡Bienvenido, #{jugador.getName}!")
end
```

Eventos disponibles:

| Nombre | Evento Bukkit |
| --- | --- |
| `:join`, `:player_join` | `PlayerJoinEvent` |
| `:quit`, `:player_quit` | `PlayerQuitEvent` |
| `:death`, `:player_death` | `PlayerDeathEvent` |
| `:break`, `:block_break` | `BlockBreakEvent` |
| `:place`, `:block_place` | `BlockPlaceEvent` |
| `:interact`, `:player_interact` | `PlayerInteractEvent` |
| `:damage`, `:entity_damage` | `EntityDamageByEntityEvent` |

Los callbacks se ejecutan de forma síncrona en el hilo del servidor. Mantén los handlers breves; usa el scheduler de Bukkit para trabajo costoso y respeta sus reglas de concurrencia.

## Recarga y lint

`/ezcript reload` recarga todos los archivos `.ez` del directorio de scripts. Si uno no carga, se registra el error y los demás siguen cargando. `/ezcript lint` comprueba sintaxis Ruby, espacios al final y líneas de más de 120 caracteres.

El runtime expone directamente los eventos y remitentes de comandos de Bukkit. Considera los scripts código de confianza: JRuby no es un entorno aislado de seguridad.
