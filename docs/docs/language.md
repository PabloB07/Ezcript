---
title: Ruby DSL
---

Ezcript files are ordinary Ruby source code with the `.ez` extension. JRuby executes scripts in the plugin, and the Bukkit/Paper API is available through normal Java interop.

Ezcript's goal is to cover Skript's server-scripting use cases while keeping Ruby syntax and the Java API visible. That does **not** mean that every Skript expression, event alias, convenience effect, or add-on syntax has already been implemented. This reference marks the difference between Ruby/Paper capabilities and Ezcript-specific features.

## Reference guide

- [Ruby basics and block syntax](./ruby-basics)
- [Commands and events](./commands-and-events)
- [Minigame example](./minigame-example)
- [Conditions, loops, and control flow](./control-flow)
- [Values, variables, and persistence](./data-and-persistence)
- [Paper, Bukkit, and Adventure API](./paper-api)
- [Paper 26 API examples](./paper-26-examples)
- [FastBoard, SpiGUI, and text colors](./paper-api#fastboard-scoreboards)
- [Functions, scripts, and reuse](./functions-and-reuse)
- [Timers, delays, and schedulers](./tasks-and-timers)
- [Skript-to-Ezcript migration](./skript-migration)
- [Feature coverage and roadmap](./feature-coverage)
- [Troubleshooting](./troubleshooting)

The upstream [Skript documentation](https://docs.skriptlang.org/) describes Skript's current feature set. Use the [coverage matrix](./feature-coverage) and [migration guide](./skript-migration) to see how those feature families map to Ezcript and what the current runtime actually supports.

## Paper API and Ruby imports

Scripts can use the full public API exposed by the Paper API dependency, not only the Ezcript event DSL. Import Java classes with `java_import` and call their Java methods using Ruby syntax. Ruby's `include` expects a module, so do not use `include 'java.util.Random'` to import a Java class:

```ruby
java_import 'org.bukkit.Bukkit'
java_import 'org.bukkit.GameMode'
java_import 'org.bukkit.entity.Player'

command "survival" do |sender, args|
  if sender.is_a?(Player)
    sender.setGameMode(GameMode::SURVIVAL)
    sender.sendMessage("Welcome to #{Bukkit.getServer.getName}!")
  end
end
```

The editor's API completions are generated from the Paper API used to compile Ezcript. They include public Bukkit, Paper, and Adventure classes, inherited methods, fields, referenced Java standard-library types, and Java-import suggestions. The bundled API index is based on Paper API `26.2.build.112-stable`; when the dependency version changes, rebuild Ezcript and point the editor to the rebuilt jar.

## Current Ezcript DSL

The built-in Ezcript DSL provides `command`, `on`, FastBoard/SpiGUI helpers, and text-color helpers. Ruby syntax, Ruby control flow, ordinary Ruby methods, and Java interop are supplied by JRuby; convenience expressions such as `player's balance`, `send "hello" to player`, Skript list variables, or Skript's `wait` effect are **not** automatically added by that fact.

### Commands

Register a server command using a block. The callback receives the `CommandSender` and a `String[]` of whitespace-separated arguments:

```ruby
command "announce" do |sender, args|
  message = args.to_a.join(" ")
  sender.sendMessage("Announcement: #{message}")
end
```

Command arguments are currently split on whitespace; quoted multi-word arguments are not yet parsed. A script command takes precedence when its label matches.

### Events

Register an event with `on`. Its callback receives the Bukkit event object:

```ruby
on :join do |event|
  player = event.getPlayer
  player.sendMessage("Welcome, #{player.getName}!")
end
```

Supported event names:

| Name | Bukkit event |
| --- | --- |
| `:join`, `:player_join` | `PlayerJoinEvent` |
| `:quit`, `:player_quit` | `PlayerQuitEvent` |
| `:death`, `:player_death` | `PlayerDeathEvent` |
| `:break`, `:block_break` | `BlockBreakEvent` |
| `:place`, `:block_place` | `BlockPlaceEvent` |
| `:interact`, `:player_interact` | `PlayerInteractEvent` |
| `:damage`, `:entity_damage` | `EntityDamageByEntityEvent` |

Callbacks execute synchronously on the server thread. Keep handlers short; use Bukkit's scheduler for expensive work and follow its thread-safety rules.

## Reload and lint

`/ezcript reload` reloads every `.ez` file in the scripts directory. A script that fails to load is logged and does not prevent other scripts from loading. `/ezcript lint` checks Ruby syntax, trailing whitespace, and lines longer than 120 characters.

The runtime currently exposes the Bukkit event and command sender directly. Treat scripts as trusted code: JRuby interop is not a security sandbox.
