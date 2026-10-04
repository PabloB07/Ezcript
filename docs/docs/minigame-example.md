---
title: Minigame example
---

This small example demonstrates what the current command/event DSL can do without implying that Ezcript already contains Skript's full gameplay helper library. It stores participants in Ruby memory, so they are lost when the script is reloaded or the server stops.

```ruby
java_import 'org.bukkit.Bukkit'
java_import 'org.bukkit.entity.Player'

$arena_players = {}

command "arena" do |sender, args|
  action = args.to_a.first

  case action
  when "join"
    unless sender.is_a?(Player)
      sender.sendMessage("Only players can join the arena.")
      next
    end

    $arena_players[sender.getUniqueId] = sender
    sender.sendMessage("You joined the arena.")
    Bukkit.broadcastMessage("#{sender.getName} joined the arena.")
  when "leave"
    unless sender.is_a?(Player)
      sender.sendMessage("Only players can leave the arena.")
      next
    end

    $arena_players.delete(sender.getUniqueId)
    sender.sendMessage("You left the arena.")
  when "list"
    names = $arena_players.values.map(&:getName)
    sender.sendMessage("Arena players: #{names.empty? ? 'none' : names.join(', ')}")
  else
    sender.sendMessage("Usage: /arena <join|leave|list>")
  end
end

on :quit do |event|
  $arena_players.delete(event.getPlayer.getUniqueId)
end
```

Try `/arena join`, `/arena list`, and `/arena leave`. The player-leave cleanup also runs when a participant disconnects.

## Known limits of this example

- The participant set is in memory only; it is not a persistent Skript variable.
- The command DSL splits arguments on whitespace and has no built-in usage, permission, alias, or tab-completion metadata.
- This is a participant registry, not a complete minigame engine. It does not implement queues, teams, countdown tasks, arena configuration, inventory restoration, rewards, or match persistence.
- Do not add timers using `Thread.sleep`; use a lifecycle-safe Bukkit scheduler integration.

The pattern uses JRuby blocks, ordinary Ruby hashes and arrays, and Paper API calls. See the [feature matrix](./feature-coverage) before assuming another Skript feature is already built in.
