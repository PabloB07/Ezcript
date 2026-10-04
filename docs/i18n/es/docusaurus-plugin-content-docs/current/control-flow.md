---
title: Condiciones, bucles y control de flujo
---

El control de flujo Ruby reemplaza secciones en inglés como `if`, `loop` y `stop trigger` de Skript. Los ejemplos enseñan sintaxis Ruby; no agregan efectos propios a Ezcript.

## Condiciones y comparaciones

```ruby
if player.getHealth <= 5
  player.sendMessage("¡Busca refugio!")
end

if player.hasPermission("arena.vip") && ronda.active?
  arena.join(player)
end
```

Ruby usa `==`, `!=`, `<`, `<=`, `>` y `>=` para comparar; `&&`, `||` y `!` son operadores booleanos. `and` y `or` tienen diferente precedencia; en condiciones compuestas prefiere `&&` y `||`.

```ruby
case player.getGameMode
when GameMode::CREATIVE
  player.sendMessage("Creativo")
when GameMode::SURVIVAL
  player.sendMessage("Supervivencia")
else
  player.sendMessage("Otro modo")
end
```

## Iterar jugadores, entidades y colecciones

```ruby
Bukkit.getOnlinePlayers.each do |player|
  next unless player.getWorld == arena.world
  player.sendMessage("Estás en la arena.")
end
```

`next` salta a la siguiente iteración y `break` sale del bucle más cercano. Los métodos de colecciones Ruby incluyen `each`, `map`, `select` y `any?`.

## Rangos y repetición

```ruby
(1..5).each do |segundos|
  Bukkit.getLogger.info("Cuenta regresiva: #{segundos}")
end

5.times do |indice|
  Bukkit.getLogger.info("Índice: #{indice}")
end
```

## Salir de una sección o método

`return` termina el método actual; `break` sale de un bucle. Ezcript no tiene la palabra clave `stop trigger` de Skript. Para helpers propios, organiza la salida con condicionales o retornos tempranos:

```ruby
def iniciar_ronda(jugador)
  return unless jugador.hasPermission("arena.play")

  jugador.sendMessage("Iniciando la ronda.")
end
```

## Errores

Ruby permite `begin`/`rescue`/`ensure`, pero capturar una excepción no hace seguros los accesos Bukkit desde un hilo incorrecto ni revierte acciones parciales. Los errores inesperados en callbacks se registran en el log del plugin.
