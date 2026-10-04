---
title: Functions and code reuse
---

Use ordinary Ruby methods to share logic in a script:

```ruby
def announce_round(message)
  Bukkit.broadcastMessage("[Arena] #{message}")
end

def give_starting_kit(player)
  player.getInventory.addItem(ItemStack.new(Material::STONE_SWORD))
end
```

Ruby methods can use defaults, keyword arguments, classes, modules, and mixins according to Ruby/JRuby rules. Java interoperability lets a script use Bukkit classes without reimplementing their behavior.

## Script boundaries

Each `.ez` file is evaluated in its own `ScriptRuntime`. A method or global variable defined in one file should not be treated as a shared API for other script files. Each file must import the Java types it uses.

Reload stops active runtimes, discards registered handlers, and loads the files again. Build shared helpers into a Java plugin/library or keep the related code together until Ezcript provides an explicit cross-script module system.

## Add-ons and plugin integrations

Skript add-ons register new syntax and types into Skript. Ezcript does not load Skript add-ons or translate their syntax. In Ruby scripts, use a plugin's documented Java API with `java_import` when the plugin is installed and its classes are accessible:

```ruby
java_import 'org.bukkit.Bukkit'

other_plugin = Bukkit.getPluginManager.getPlugin("ExamplePlugin")
if other_plugin && other_plugin.isEnabled
  # Call that plugin's documented public API here.
end
```

The exact class and methods depend on the third-party plugin. Check that plugin's API and dependency/load-order guidance. Ezcript does not currently provide a universal plugin-hook registry.
