---
title: Troubleshooting
---

## Script did not load

Read the server log for the `.ez` file and Ruby line number. Check:

- Is the file valid Ruby? Run `/ezcript lint` for syntax and basic style diagnostics.
- Are all `do` blocks closed with `end`?
- Did a `java_import` name exactly match a Java class?
- Is the imported plugin/API installed and visible to the server?
- Did a Java method return `nil` before another method was called on it?

Ruby's `include` mixes a module into a Ruby module/class; it does not import a Java class by string. Use `java_import 'java.util.UUID'` or JRuby's `Java::...` namespace.

## `undefined method 'update_lines'`

`update_lines(board, lines)` is provided by the Ezcript runtime prelude in the current jar. Install the rebuilt `build/libs/ezcript.jar` and fully stop and restart the server; `/ezcript reload` reloads `.ez` scripts but cannot replace Java code already loaded in the running plugin. Then use:

```ruby
update_lines(board, [color("&aFirst line"), "", color("&#55ccffRGB line")])
```

This helper converts the Ruby array to the Java `String[]` required by FastBoard. Do not pass the Ruby array directly to FastBoard's `updateLines(String...)`.

## Command is not recognized

Only a registered one-word command label is handled by the current command DSL. Script command arguments are split on spaces and quoted grouping is not implemented. Check the server log for a failed script load, then run `/ezcript reload`.

## Event is not registered

Only the aliases in [Commands and events](./commands-and-events) are accepted by `on` today. Importing a Bukkit event class does not register it automatically.

## Server lags during a script

Synchronous command/event callbacks run on the main server thread. Avoid large loops, blocking network/disk calls, and expensive computations. Follow Paper's scheduler thread rules before moving any work off-thread.

## Data disappeared after reload

Ruby memory is not persistent storage. Reload creates new runtimes. Save data explicitly to YAML or a database.

## VS Code shows no Paper completions

Rebuild the Ezcript jar after changing the Paper API dependency, configure `ezcript.serverJar` to the new `build/libs/ezcript.jar`, and run **Ezcript: Restart Language Server**. The bundled index describes compile-time API symbols, not runtime feature detection.
