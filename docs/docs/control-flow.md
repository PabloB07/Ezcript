---
title: Conditions, loops, and control flow
---

Ruby's own control flow replaces Skript's `if`, `loop`, `stop trigger`, and related English-like sections. These examples describe Ruby syntax; they do not add a new Ezcript-specific effect.

## Conditions and comparisons

```ruby
if player.getHealth <= 5
  player.sendMessage("Find cover!")
end

if player.hasPermission("arena.vip") && round.active?
  arena.join(player)
end
```

Ruby uses `==`, `!=`, `<`, `<=`, `>`, and `>=` for comparisons; `&&`, `||`, and `!` are boolean operators. Ruby's `and` and `or` have different precedence, so prefer `&&` and `||` in compound conditions.

```ruby
case player.getGameMode
when GameMode::CREATIVE
  player.sendMessage("Creative mode")
when GameMode::SURVIVAL
  player.sendMessage("Survival mode")
else
  player.sendMessage("Another game mode")
end
```

## Iterating players, entities, and collections

```ruby
Bukkit.getOnlinePlayers.each do |player|
  next unless player.getWorld == arena.world
  player.sendMessage("You are in the arena.")
end
```

`next` skips to the next iteration. `break` leaves the nearest loop. Ruby collection methods such as `each`, `map`, `select`, and `any?` are useful when the collection implements the appropriate interface.

## Ranges and repetition

```ruby
(1..5).each do |seconds|
  Bukkit.getLogger.info("Countdown: #{seconds}")
end

5.times do |index|
  Bukkit.getLogger.info("Index: #{index}")
end
```

## Stop a block or exit a method

Ruby's `return` exits the current method; `break` exits a loop. There is no Skript `stop trigger` keyword in Ezcript. Prefer early returns in your own helper methods:

```ruby
def start_round(player)
  return unless player.hasPermission("arena.play")

  player.sendMessage("Starting the round.")
end
```

At top level or inside a registered callback, design the block's branch structure with `if`/`else` rather than assuming Skript section-control effects exist.

## Errors

Ruby supports `begin`/`rescue`/`ensure`, but catching an exception does not make Bukkit calls safe from the wrong thread or make a partially executed game action reversible. Unexpected callback errors are logged by the plugin.
