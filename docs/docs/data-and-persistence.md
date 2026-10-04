---
title: Values and persistence
---

Skript offers built-in variables and typed expressions. Ezcript uses Ruby values and objects instead; its current runtime does not provide Skript-style `{variables}`, list variables, automatic persistence, or typed expression syntax.

## Ruby values and collections

```ruby
participants = []
participants << player.getUniqueId
participants.delete(player.getUniqueId)

scores = {
  player.getUniqueId.toString => 10
}
scores[player.getUniqueId.toString] += 5
```

Hashes and arrays belong to the current Ruby runtime. They are not automatically written to disk, shared between scripts, or restored after a restart.

## Scope and reload

Local variables belong to their Ruby scope; instance variables (`@round`) and constants have Ruby semantics within their runtime. Each script file is loaded into its own `ScriptRuntime`. Do not rely on a Ruby global from another `.ez` file.

`/ezcript reload` terminates script runtimes and creates new ones. In-memory values and registered callbacks are lost. Store persistent game data explicitly in a file or a database.

## Explicit YAML persistence through Bukkit

The Bukkit configuration API can be used directly through JRuby. This is ordinary Java interop, not an Ezcript storage abstraction:

```ruby
java_import 'org.bukkit.configuration.file.YamlConfiguration'
java_import 'java.io.File'

data_file = File.new("plugins/Ezcript/player-data.yml")
data = YamlConfiguration.loadConfiguration(data_file)

def save_coins(data, data_file, player, amount)
  data.set("coins.#{player.getUniqueId}", amount)
  data.save(data_file)
end
```

In production, create parent directories as needed, handle I/O errors, and avoid saving a whole configuration repeatedly in a high-frequency event. Consider a database for larger datasets. Ezcript does not currently bundle a database manager, data migrations, or asynchronous persistence helpers.

## Event values

Skript exposes named values such as `player`, `event-block`, or `attacker` depending on the trigger. In Ezcript, the block receives `event`; read event data from its Bukkit/Paper methods and assign it to normal Ruby variables when you need a shorter name.
