---
title: Commands and events
---

## Script commands

Register a one-word script command with a Ruby block:

```ruby
command "announce" do |sender, args|
  message = args.to_a.join(" ")
  sender.sendMessage("[Server] #{message}")
end
```

The callback receives:

| Parameter | Value |
| --- | --- |
| `sender` | Bukkit `CommandSender` (a player or the console) |
| `args` | Java `String[]`; convert with `args.to_a` to use Ruby array methods |

**Current limitation:** arguments are split on whitespace. Quoted arguments are not grouped, and command aliases, permissions, descriptions, tab completion, and an argument parser are not yet part of the Ezcript command DSL. A registered script command takes precedence when its label matches.

Use `sender.is_a?(Player)` when a command must be player-only. `Player` needs `java_import 'org.bukkit.entity.Player'` first.

## Event handlers

Register handlers using a symbol after `on`. The block receives the original Bukkit/Paper event object:

```ruby
java_import 'org.bukkit.event.player.PlayerJoinEvent'

on :join do |event|
  player = event.getPlayer
  player.sendMessage("Welcome, #{player.getName}!")
end
```

These aliases are currently registered by Ezcript:

| Ezcript symbol | Event class |
| --- | --- |
| `:join`, `:player_join` | `PlayerJoinEvent` |
| `:quit`, `:player_quit` | `PlayerQuitEvent` |
| `:death`, `:player_death` | `PlayerDeathEvent` |
| `:break`, `:block_break` | `BlockBreakEvent` |
| `:place`, `:block_place` | `BlockPlaceEvent` |
| `:interact`, `:player_interact` | `PlayerInteractEvent` |
| `:damage`, `:entity_damage` | `EntityDamageByEntityEvent` |

The callback is a real Bukkit event, so event-specific methods remain available:

```ruby
on :break do |event|
  if event.getBlock.getType == Material::DIAMOND_ORE
    event.setDropItems(false)
    event.getPlayer.sendMessage("Ore drops are handled by the minigame.")
  end
end
```

Import any Java class used in an example, such as `org.bukkit.Material`. Event handlers execute synchronously on the server thread at normal event priority. A handler can call `event.setCancelled(true)` only when that event is cancellable.

## Event timing and context

Unlike Skript's named event values, Ezcript passes the concrete event object as `event`. Read the data through that event's Bukkit/Paper methods. For example, use `event.getPlayer` for a join event and `event.getEntity`/`event.getDamager` for an entity-damage event.

The supported `on` aliases are currently fixed in the plugin source. Being able to import a Paper event class does not automatically make `on :that_event` register it.
