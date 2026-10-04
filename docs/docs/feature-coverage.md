---
title: Skript feature coverage
---

This matrix separates capabilities provided by Ruby/JRuby or Paper from features that Ezcript itself implements. “Use Paper API” means a script can call a documented Java API with JRuby; it does not mean Ezcript provides Skript-like aliases, a dedicated DSL, or extra safety checks.

## Available today

| Feature family | Status | How to use it |
| --- | --- | --- |
| `.ez` Ruby scripts | Implemented | JRuby executes each script on load |
| Script command callbacks | Basic implementation | `command "name" do |sender, args| ... end` |
| Built-in event callbacks | Limited implementation | Seven event types and aliases in [Commands and events](./commands-and-events) |
| Scoreboards and inventory GUIs | FastBoard and SpiGUI helpers | `fastboard(player)`, `gui(...)`, `create_button(...)`; [API guide](./paper-api) |
| Legacy, RGB, and MiniMessage text | Basic helpers | `color("&a...")`, `color("&#55ccff...")`, `mini("<green>...</green>")` |
| Ruby conditions, methods, collections, classes, and blocks | Ruby/JRuby | Use ordinary Ruby syntax |
| Paper/Bukkit/Adventure APIs | Java interop | `java_import 'fully.qualified.Class'` |
| Syntax/style lint | Basic implementation | Ruby parser, trailing spaces, and 120-character line checks |
| VS Code syntax/theme/LSP | Implemented, early | Grammar, three themes, bundled Paper API index, basic hover/completions |
| Script reload | Basic implementation | `/ezcript reload` tears down and reloads `.ez` files |

The UI helper example is available in the repository at `examples/lobby-ui.ez`.

## Skript feature families not built in yet

This is a family-level checklist, not a promise that every individual upstream expression has a one-to-one Ruby replacement. Skript's exact catalogue changes over time; use the [official reference](https://docs.skriptlang.org/) for its current built-ins.

| Skript feature family / examples | Ezcript status | Ruby-shaped direction |
| --- | --- | --- |
| Event catalogue: chat, command, movement, respawn, inventory, world, entity, block, and server lifecycle triggers | Only seven event types are registered; see [commands and events](./commands-and-events) | Explicit event registrations with typed event objects, priorities, cancellation, and documented thread behavior |
| Event values, event-specific expressions, trigger conditions, and event priority/cancellation controls | Not implemented by the DSL | Typed event context and explicit registration options |
| Commands: usage, argument parsing/types, permissions, aliases, executor/console behavior, and tab completion | Basic named callback only; arguments split on whitespace | Command definitions with validated options and typed arguments |
| Expressions and conversions: player/entity properties, locations, blocks, items, inventories, dates, timespans, numbers, and text | No Skript expression aliases or conversion system | Ruby values plus typed Paper objects and opt-in helpers |
| Conditions: permission checks, comparisons, region/range checks, and event predicates | No Skript condition aliases | Ruby predicates and Paper/plugin APIs |
| Player/entity effects: message, teleport, health, damage, potion effects, spawn, remove, and velocity | No Skript effect aliases | Direct Paper API calls or documented helpers |
| World/block effects: set/change blocks, drops, explosions, weather, time, and world properties | No Skript effect aliases | Direct Paper API calls with validation and lifecycle-safe helpers |
| Items/inventories: give/take items, slots, equipment, inventory contents, drops, and GUI menus | `create_item`, `create_button`, `gui`, `open_gui` cover basic SpiGUI menus; no Skript effect aliases | Direct Paper/SpiGUI APIs; remaining convenience effects need dedicated helpers |
| Text and presentation: chat formatting, clickable text, titles, action bars, sounds, particles, and effects | Legacy/RGB formatting and MiniMessage conversion exist; no Skript effect aliases | Adventure components and Paper APIs |
| Scoreboards, teams, boss bars, metadata, permissions, economy, and plugin-specific integrations | FastBoard helper; remaining families have no wrappers | Call the relevant Paper or installed-plugin API directly; optional adapters need separate support |
| Variables: local/global/list variables, persistence, scopes, and variable database | Ruby variables are script-local runtime state; no persistent Ezcript store | Explicit storage API with scope, serialization, and persistence rules |
| Control-flow sections such as loops, conditional sections, functions, and reusable triggers | Ruby control flow/methods work inside a script; no Skript section syntax or cross-file Ezcript registry | Ruby methods/modules and explicit cross-file loading with lifecycle rules |
| `wait`, delayed/repeating triggers, task handles, and cancellation | No Ezcript scheduler DSL; see [tasks and timers](./tasks-and-timers) | Scheduler wrappers with cancellation handles and reload-safe cleanup |
| Options, aliases, script metadata, load/unload sections, and configuration | Not implemented | Validated Ruby configuration and explicit script lifecycle hooks |
| Files, databases, HTTP, and other integrations commonly supplied by Skript add-ons | No built-in integration layer | Use Java libraries/plugin APIs directly or add maintained, documented adapters |
| Add-on syntax, parser hooks, and Skript add-on compatibility | Not compatible | Integrate with an add-on's Java API; its Skript syntax does not transfer automatically |
| Language server: symbols, definitions, references, rename, semantic diagnostics, and type checking | Partial; Paper completions/hover are available | Expand Ruby-aware analysis and Paper type inference |

This project is in early development. This page is a coverage/roadmap document, not a claim that the listed planned features are available. Ruby can express an algorithm, but it does not by itself implement server event hooks, persistence, permission checks, command registration, or task cleanup.

## Parity work order

To approach the core Skript use cases safely, development should prioritize: (1) comprehensive event mapping and event context, (2) robust command definitions/arguments/permissions, (3) reload-safe scheduler/task handles, (4) persistent variable/storage APIs, (5) reusable cross-file functions/modules, and (6) editor diagnostics/type inference. Every feature needs tests against the actual Paper server/API versions it claims to support.

For the upstream feature catalogue and current Skript syntax, consult the [Skript docs](https://docs.skriptlang.org/).
