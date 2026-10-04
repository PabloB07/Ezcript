---
title: Paper 26 API examples
---

Ezcript runs ordinary Ruby through JRuby; it does not replace Paper's Java API with a second API. Import the Paper types you need, use Ruby control flow, and call the Java methods. The examples here target the Paper API 26.2 build selected by this project.

## Adventure chat, titles, and action bars

`mini` converts MiniMessage markup into an Adventure `Component`. Paper's `Player`/`Audience` methods can receive that component directly:

```ruby
java_import 'org.bukkit.entity.Player'
java_import 'net.kyori.adventure.title.Title'

command "welcome" do |sender, args|
  next unless sender.is_a?(Player)

  sender.sendMessage(mini("<gradient:#55ccff:#aa55ff>Welcome, #{sender.getName}!</gradient>"))
  sender.sendActionBar(mini("<green>Your adventure starts now.</green>"))
  sender.showTitle(Title.title(
    mini("<gold><bold>Ezcript"),
    mini("<gray>Paper 26 server scripting")
  ))
end
```

For simple legacy strings and RGB strings, use `color("&eGold")` and `color("&#55ccffSky")`. MiniMessage has its own tag syntax; `color` does not parse MiniMessage tags.

## Block events and world effects

The `:break` alias provides a `BlockBreakEvent`. Use its Bukkit methods to inspect permissions and the block, cancel the event when appropriate, and invoke a world API:

```ruby
java_import 'org.bukkit.Material'
java_import 'org.bukkit.Particle'

on :break do |event|
  player = event.getPlayer
  block = event.getBlock

  next unless block.getType == Material::DIAMOND_ORE
  next unless player.hasPermission("ezcript.mining.bonus")

  event.setCancelled(true)
  block.setType(Material::AIR)
  block.getWorld.spawnParticle(
    Particle::HAPPY_VILLAGER,
    block.getLocation.add(0.5, 0.5, 0.5),
    12
  )
  player.sendMessage(mini("<green>Bonus ore collected.</green>"))
end
```

This handler runs synchronously on the server thread. Check that an event is cancellable before calling `setCancelled`, and keep expensive work out of the handler.

## Inventory items and player state

Use the `create_item` convenience helper for a colored display name/lore, then use the ordinary Paper inventory API to place it. This avoids passing Ruby arrays into Java varargs methods:

```ruby
java_import 'org.bukkit.Material'
java_import 'org.bukkit.entity.Player'

command "starter" do |sender, args|
  next unless sender.is_a?(Player)
  next unless sender.hasPermission("ezcript.starter")

  item = create_item(
    "IRON_SWORD",
    1,
    "&bStarter sword",
    ["&7A first adventure item", "&#55ccffBound to #{sender.getName}"]
  )
  sender.getInventory.setItem(0, item)
  sender.setLevel([sender.getLevel, 1].max)
  sender.sendMessage(mini("<green>Starter item added.</green>"))
end
```

Paper methods such as `getInventory`, `setItem`, `setLevel`, `hasPermission`, and `sendMessage` are Java methods; Ruby supplies the surrounding conditionals and interpolation.

## Paper API completion in VS Code

The editor index is generated from the same compile dependency as Ezcript itself. The index includes public Paper/Bukkit/Adventure types and methods, their inherited public methods, and the bundled FastBoard/SpiGUI APIs. After changing the Paper API version, rebuild `ezcript.jar`, configure `ezcript.serverJar` to that jar, and restart the language server. Completions describe the compile-time API, not runtime compatibility with a different server build.

## Compatibility

The checked-in build targets Paper API `26.2.build.112-stable` and Java 25. Paper 26.3 is pre-release in the configured repository, so it may change APIs and has not been verified by this build. For plugin-specific details, always consult the Javadocs for the exact Paper API version installed on the server.

The complete snippets are also available in the project root at `examples/paper-26-api.ez` for direct use in the server scripts directory.
