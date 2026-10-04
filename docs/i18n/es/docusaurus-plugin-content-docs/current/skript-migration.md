---
title: Migración desde Skript
---

Ezcript comparte el objetivo de facilitar scripts de servidor, pero ejecuta Ruby en vez de analizar la sintaxis inglesa de Skript. Migrar significa reescribir el código en Ruby y reemplazar expresiones/efectos exclusivos de Skript por llamadas Java o métodos Ruby.

## Correspondencias

| Concepto Skript | Enfoque Ruby/Ezcript | Soporte actual |
| --- | --- | --- |
| `on join:` | `on :join do |event| ... end` | Disponible |
| `command /hello:` | `command "hello" do |sender, args| ... end` | Básico |
| `if player's health is below 5:` | `if player.getHealth < 5` | Ruby + API Paper |
| `loop all players:` | `Bukkit.getOnlinePlayers.each do |player| ... end` | Ruby + API Paper |
| `send "Hi" to player` | `player.sendMessage("Hi")` | API Paper |
| `cancel event` | `event.setCancelled(true)` | Solo en eventos cancelables |
| `{coins::%uuid of player%}` | Hash Ruby indexado por UUID | Solo memoria |
| `wait 5 seconds` | Tarea del scheduler Bukkit | Sin wrapper de Ezcript |
| Declaración de función | `def name(...) ... end` | Métodos Ruby en el mismo archivo |
| `stop trigger` | Reestructurar con ramas/métodos Ruby | Sin efecto equivalente |
| Sintaxis de add-ons Skript | Usar la API Java del plugin externo | Sin compatibilidad de add-ons |

## Conversión de ejemplo

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

El valor implícito `player` se convierte en un valor explícito que se obtiene del evento.

## Lista para portar scripts

1. Identifica eventos, comandos, variables Skript y sintaxis de add-ons.
2. Traduce eventos/comandos solo si aparecen en la [tabla de DSL disponible](./commands-and-events).
3. Reemplaza expresiones y efectos por Ruby y métodos documentados de Paper.
4. Elige almacenamiento explícito para valores que sobrevivan a recargas o reinicios.
5. Revisa hilos y scheduler para cualquier delay o lógica periódica.
6. Prueba en staging: `/ezcript lint` comprueba sintaxis/estilo, no compatibilidad de API ni comportamiento del juego.

Consulta la [matriz de cobertura](./feature-coverage) antes de planear una migración que dependa de funciones Skript específicas.
