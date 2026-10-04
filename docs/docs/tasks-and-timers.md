---
title: Delays, repeating tasks, and scheduling
---

Skript includes delay and periodic triggers. Ezcript does not currently define a Ruby `wait` effect or periodic-trigger DSL. Use Bukkit's scheduler explicitly through Java interop.

## Schedule a synchronous delayed task

Conceptually, a delayed task needs all three of these values:

```text
plugin instance + Java Runnable callback + delay in ticks
```

Ezcript does not currently expose its JavaPlugin instance to scripts or provide a tested Ruby-to-`Runnable` scheduler helper. Therefore there is no ready-to-run scheduler snippet to copy yet. Obtain a valid plugin instance and construct the Java `Runnable` using the bundled JRuby version's supported SAM conversion before calling Bukkit's `runTaskLater(plugin, runnable, ticks)`. Do not invent an `EzcriptPlugin.instance` helper; it does not exist in the current API.

Ticks run at 20 ticks per second under ideal server conditions; lag makes wall-clock timing slower. Scheduler APIs require a live plugin instance. Never create a repeating task without a cancellation/lifecycle plan.

## Thread rules

Use synchronous scheduler tasks for Bukkit world/player/inventory changes. Asynchronous tasks are for independent work such as network or disk I/O; do not read or modify ordinary Bukkit state from the asynchronous task.

## Planned Ezcript conveniences

An Ezcript-level scheduler DSL would need explicit plugin ownership, task cancellation handles, reload cleanup, and documented threading semantics. Until that API exists, call the server scheduler directly and manage task lifetimes carefully.
