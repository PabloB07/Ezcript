---
title: Ruby syntax for Ezcript
---

An `.ez` file is Ruby source code interpreted by JRuby. Use ordinary Ruby expressions and blocks; Ezcript adds only a small server DSL on top.

## Values and strings

```ruby
server_name = "Bedwars"
max_players = 16
ranked = true
current_map = nil

message = "Welcome to #{server_name}!"
```

Ruby values include strings, numbers, booleans, `nil`, arrays, hashes, ranges, symbols, and objects. Double-quoted strings interpolate `#{expression}`; single-quoted strings do not.

```ruby
maps = ["Ruins", "Harbor", "Citadel"]
settings = {starting_coins: 100, ranked: true}
mode = :ranked
```

## Blocks and callbacks

Blocks are Ruby's callback syntax. Ezcript passes values to the parameters between `|` characters:

```ruby
command "coins" do |sender, args|
  sender.sendMessage("Arguments: #{args.to_a.join(' ')}")
end

on :join do |event|
  event.getPlayer.sendMessage("Welcome!")
end
```

Unlike Skript's indentation-based sections, Ruby blocks have explicit `do` and `end` delimiters. Indentation improves readability, but does not determine the block's scope.

## Branches

```ruby
if player.hasPermission("arena.admin")
  player.sendMessage("You can manage this arena.")
elsif player.isOp
  player.sendMessage("You are an operator.")
else
  player.sendMessage("You are a player.")
end
```

Ruby also supports `unless`, `case`/`when`, and modifier forms such as `log("joined") if enabled`. Use the explicit multi-line form for event handlers.

## Iteration

```ruby
Bukkit.getOnlinePlayers.each do |player|
  player.sendMessage("The round starts soon.")
end

3.times do |index|
  Bukkit.getLogger.info("Countdown step #{index}")
end
```

Arrays and collections use Ruby methods such as `each`, `map`, `select`, and `find`. A Java collection may need `to_a` before using Ruby array-specific methods.

## Methods and classes

Ruby helpers reduce duplication within a script:

```ruby
def announce(message)
  Bukkit.broadcastMessage("[Arena] #{message}")
end

command "start" do |sender, _args|
  announce("#{sender.getName} started the round.")
end
```

For Ruby semantics and the JRuby version shipped with the plugin, see the [Ruby documentation](https://docs.ruby-lang.org/en/) and [JRuby documentation](https://www.jruby.org/documentation).
