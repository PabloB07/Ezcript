---
title: Paper, Bukkit, and Adventure API
---

JRuby's Java integration makes the Paper API the broadest way to interact with Minecraft in Ezcript. The plugin does not wrap every Bukkit operation in a custom DSL: import a Java type, call its methods, and follow the API's requirements.

## Imports

```ruby
java_import 'org.bukkit.Bukkit'
java_import 'org.bukkit.Material'
java_import 'org.bukkit.GameMode'
java_import 'org.bukkit.entity.Player'
java_import 'org.bukkit.inventory.ItemStack'
```

Use `java_import` for a Java class. Ruby's `include` is for modules and is **not** a Java class-import statement.

```ruby
if sender.is_a?(Player)
  item = ItemStack.new(Material::DIAMOND, 1)
  sender.getInventory.addItem(item)
end
```

JRuby can also access Java classes through its `Java::...` namespace. `java_import` is usually easier to read:

```ruby
random = Java::JavaUtil::Random.new
```

## API areas available through imports

The compile-time Paper API includes Bukkit server, player, entity, world, block, inventory, event, scheduler, command, permission, scoreboard, boss bar, and configuration APIs. Adventure provides component-based chat, titles, sounds, boss bars, and audience interfaces. Availability of a method still depends on the server API/version and whether the calling event or object permits that operation.

`ezcript.jar` contains an editor index generated from the Paper API configured in Gradle. VS Code uses it for Paper/Bukkit/Adventure class, field, method, import, and inherited-method completions. That editor index is not a runtime compatibility shim; compile-time completion does not guarantee that a method exists on every server release.

## FastBoard scoreboards

Ezcript bundles FastBoard. Call `fastboard(player)` (or its alias `scoreboard(player)`) to get the shared board for a player. Update it with FastBoard's Java API:

```ruby
java_import 'org.bukkit.Bukkit'
java_import 'org.bukkit.entity.Player'

on :join do |event|
  player = event.getPlayer
  board = fastboard(player)
  board.updateTitle(color("&6&lEzcript"))
  update_lines(board, [
    color("&7Welcome,"),
    color("&f#{player.getName}"),
    "",
    color("&aPlayers: &f#{Bukkit.getOnlinePlayers.size}")
  ])
end
```

FastBoard's `updateLines(String...)` Java varargs method cannot safely receive a Ruby array directly: JRuby may pass it as `Object[]`, causing a `ClassCastException`. Use Ezcript's `update_lines(board, lines_array)` helper, which converts Ruby lines to a Java `String[]`. Other FastBoard methods such as `updateTitle` work normally.

All scripts share one Ezcript-managed board per player, so separate scripts do not replace each other's FastBoard instance. A later call to `fastboard(player)` returns that shared board; coordinate title/line updates when multiple scripts use it. Ezcript deletes boards when the player quits, when scripts reload, and when the plugin disables. The helper expects calls on the server thread. FastBoard supports `&` legacy codes through `color`, as well as RGB such as `&#55ccff`.

## SpiGUI inventories

Ezcript bundles SpiGUI and initializes it for the plugin. Create a paginated menu with `gui(title, rows, tag)`, make a button from an `ItemStack` using `create_button(item) { |click| ... }`, add it with SpiGUI's `addButton`, then use `open_gui(player, menu)`:

```ruby
java_import 'org.bukkit.entity.Player'

command "menu" do |sender, args|
  next unless sender.is_a?(Player)

  menu = gui(color("&6Server menu"), 3, "server-menu")
  icon = create_item("DIAMOND", 1, "&bInformation", ["&7Click for server details"])
  menu.addButton(create_button(icon) do |click|
    click.getWhoClicked.sendMessage(mini("<green>Welcome to the server!</green>"))
  end)
  open_gui(sender, menu)
end
```

`create_item(material, amount, name, lore)` resolves a Bukkit material name and translates `&`/RGB colors in the display name and lore. SpiGUI automatically cancels normal inventory interactions for menu buttons and supports pagination. Menu click callbacks run on the server thread. `/ezcript reload` closes open SpiGUI menus before terminating their script runtimes, so old callbacks cannot invoke unloaded Ruby code.

## Text colors

- `color("&eYellow &lbold")` returns a legacy-colored string; `&#RRGGBB` and `&#aabbcc` RGB forms are supported.
- `mini("<gradient:#55ccff:#aa55ff>RGB text</gradient>")` returns an Adventure `Component` parsed by MiniMessage. Send it through Paper's Adventure-aware `sendMessage` APIs.
- For any additional Adventure API, import its class normally with `java_import`.

## Scheduler and thread safety

Most Bukkit world, entity, and inventory operations must execute on the server's main thread. Event callbacks run synchronously by default. Use the Bukkit scheduler for delayed or repeating server-thread work. Only use asynchronous tasks for work that does not access non-thread-safe Bukkit state, and return to the main thread before modifying it.

See [timers and schedulers](./tasks-and-timers) for an example.

## Important API rules

- Check the event's cancellation support before calling `setCancelled`.
- Check for `nil`/`null` Java results before chaining methods.
- Respect Bukkit's main-thread rules.
- Avoid blocking I/O or long loops in synchronous events.
- Treat imported Java APIs as privileged server code; scripts are not sandboxed.

## API version

The Paper API index is generated from the dependency configured in `build.gradle` (Paper API `26.2.build.112-stable`) and includes the bundled FastBoard 2.2.2 and SpiGUI 1.4.1 public APIs. SpiGUI's published documentation lists testing through Paper 1.20; successful compilation against Paper 26.2 does not guarantee runtime compatibility with every Paper/Purpur release. Test the plugin and menu behavior on each target server before deployment. Rebuild the jar when changing compile-time API versions. See [Paper 26 API examples](./paper-26-examples).
