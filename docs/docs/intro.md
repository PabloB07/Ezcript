---
slug: /
title: Introduction
---

Ezcript runs real Ruby code from `.ez` files inside a Paper or Purpur server. Use it to script small server features, minigames, and game modes without creating a separate Java plugin for each change.

Ezcript is in early development. Script callbacks can call the Bukkit/Paper Java API through JRuby, so only load scripts you trust.

## Quick start

1. Install the Ezcript jar into the server's `plugins` directory and start the server.
2. Create `plugins/Ezcript/scripts/welcome.ez`.
3. Add a command or event handler:

```ruby
java_import 'org.bukkit.Bukkit'

command "hello" do |sender, args|
  sender.sendMessage("Hello! There are #{Bukkit.getOnlinePlayers.size} players online.")
end

on :join do |event|
  event.getPlayer.sendMessage("Welcome!")
end
```

4. Run `/ezcript reload`, then try `/hello`.

Continue with [the Ruby DSL](./language) or [editor support](./editor-support). A complete example is included at `examples/welcome.ez` in the project.
