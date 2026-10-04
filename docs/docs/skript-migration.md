---
title: Migrating from Skript
---

Ezcript shares the goal of making server scripting productive, but it executes Ruby rather than parsing Skript's English-like syntax. Migration means rewriting the script in Ruby and replacing Skript-only expressions/effects with Java API calls or Ruby methods.

## Syntax mapping

| Skript concept | Ruby/Ezcript approach | Current Ezcript support |
| --- | --- | --- |
| `on join:` | `on :join do |event| ... end` | Supported |
| `command /hello:` | `command "hello" do |sender, args| ... end` | Supported; basic dispatch only |
| `if player's health is below 5:` | `if player.getHealth < 5` | Ruby control flow; API call required |
| `loop all players:` | `Bukkit.getOnlinePlayers.each do |player| ... end` | Ruby iteration plus Paper API |
| `send "Hi" to player` | `player.sendMessage("Hi")` | Paper API |
| `cancel event` | `event.setCancelled(true)` | Use only on cancellable events |
| `{coins::%uuid of player%}` | Ruby `Hash` keyed by UUID | In-memory only; no built-in persistence |
| `wait 5 seconds` | Bukkit scheduler task | No Ezcript timer helper; Java interop required |
| Skript function declaration | Ruby `def name(...) ... end` | Same-file Ruby methods |
| `stop trigger` | Refactor into Ruby methods/branches | No equivalent Ezcript effect |
| Skript add-on syntax | Import and call the add-on plugin's Java API | No Skript add-on compatibility |

## Example conversion

Skript:

```text
on join:
    send "Welcome!" to player
```

Ezcript:

```ruby
on :join do |event|
  event.getPlayer.sendMessage("Welcome!")
end
```

Skript's implicit `player` value becomes an explicit value read from the event. This makes it clear which event data the handler uses.

## Porting checklist

1. List the trigger events, commands, Skript variables, and add-on syntax used by the original script.
2. Translate each trigger to an Ezcript command/event only if it appears in the [supported DSL table](./commands-and-events).
3. Replace Skript expressions and effects with Ruby code and documented Paper API calls.
4. Choose an explicit persistence format for values that must survive script reloads or server restarts.
5. Review scheduler/thread behavior for any delay or repeating logic.
6. Test on a staging server; `/ezcript lint` checks syntax/style, not API compatibility or game behavior.

See the [feature coverage matrix](./feature-coverage) before planning a port that depends on a Skript-only feature.
