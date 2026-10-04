---
title: Comandos y eventos
---

## Comandos de script

Registra un comando de una palabra con un bloque Ruby:

```ruby
command "anunciar" do |sender, args|
  mensaje = args.to_a.join(" ")
  sender.sendMessage("[Servidor] #{mensaje}")
end
```

El callback recibe:

| Parámetro | Valor |
| --- | --- |
| `sender` | Bukkit `CommandSender`, ya sea jugador o consola |
| `args` | Java `String[]`; usa `args.to_a` para métodos de array Ruby |

**Limitación actual:** los argumentos se separan por espacios. No se agrupan argumentos entre comillas, y la DSL todavía no incluye aliases, permisos, descripciones, autocompletado ni parser de argumentos. Un comando de script tiene prioridad si coincide la etiqueta.

Usa `sender.is_a?(Player)` para restringirlo a jugadores; antes importa `org.bukkit.entity.Player`.

## Manejadores de eventos

Registra eventos con un símbolo después de `on`. El bloque recibe el objeto real del evento Bukkit/Paper:

```ruby
java_import 'org.bukkit.event.player.PlayerJoinEvent'

on :join do |event|
  jugador = event.getPlayer
  jugador.sendMessage("¡Bienvenido, #{jugador.getName}!")
end
```

Aliases registrados actualmente:

| Símbolo Ezcript | Clase del evento |
| --- | --- |
| `:join`, `:player_join` | `PlayerJoinEvent` |
| `:quit`, `:player_quit` | `PlayerQuitEvent` |
| `:death`, `:player_death` | `PlayerDeathEvent` |
| `:break`, `:block_break` | `BlockBreakEvent` |
| `:place`, `:block_place` | `BlockPlaceEvent` |
| `:interact`, `:player_interact` | `PlayerInteractEvent` |
| `:damage`, `:entity_damage` | `EntityDamageByEntityEvent` |

El callback es el evento Bukkit original, por lo que puedes usar sus métodos específicos:

```ruby
on :break do |event|
  if event.getBlock.getType == Material::DIAMOND_ORE
    event.setDropItems(false)
    event.getPlayer.sendMessage("El minijuego gestiona la recompensa.")
  end
end
```

Importa las clases Java de los ejemplos, como `org.bukkit.Material`. Los eventos se procesan síncronamente con prioridad normal en el hilo del servidor. Solo llama a `event.setCancelled(true)` si el evento permite cancelación.

## Contexto del evento

A diferencia de los valores con nombre de Skript, Ezcript entrega `event` explícitamente. Obtén los datos mediante los métodos Bukkit/Paper de la clase correspondiente.

Los aliases de `on` están limitados a los registrados en el código actual del plugin. Poder importar una clase de evento Paper no registra automáticamente `on :ese_evento`.
