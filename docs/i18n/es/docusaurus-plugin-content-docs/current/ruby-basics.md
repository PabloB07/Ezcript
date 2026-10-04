---
title: Sintaxis Ruby para Ezcript
---

Un archivo `.ez` es código Ruby interpretado por JRuby. Usa expresiones y bloques Ruby normales; Ezcript añade una pequeña DSL de servidor.

## Valores y cadenas

```ruby
nombre_del_servidor = "Bedwars"
jugadores_maximos = 16
clasificatoria = true
mapa_actual = nil

mensaje = "¡Bienvenido a #{nombre_del_servidor}!"
```

Ruby incluye cadenas, números, booleanos, `nil`, arrays, hashes, rangos, símbolos y objetos. Las cadenas con comillas dobles interpolan `#{expresion}`; las de comillas simples no.

```ruby
mapas = ["Ruins", "Harbor", "Citadel"]
ajustes = {monedas_iniciales: 100, clasificatoria: true}
modo = :clasificatoria
```

## Bloques y callbacks

Los bloques son la sintaxis Ruby para callbacks. Ezcript pasa valores a los parámetros entre `|`:

```ruby
command "coins" do |sender, args|
  sender.sendMessage("Argumentos: #{args.to_a.join(' ')}")
end

on :join do |event|
  event.getPlayer.sendMessage("¡Bienvenido!")
end
```

A diferencia de las secciones indentadas de Skript, los bloques Ruby se delimitan con `do` y `end`. La indentación ayuda a leer, pero no define el alcance del bloque.

## Condicionales

```ruby
if player.getHealth <= 5
  player.sendMessage("¡Busca refugio!")
elsif player.isOp
  player.sendMessage("Modo operador")
else
  player.sendMessage("Modo jugador")
end
```

Ruby también tiene `unless`, `case`/`when` y modificadores como `log("join") if habilitado`. Para callbacks usa preferentemente la forma multilínea.

## Iteración

```ruby
Bukkit.getOnlinePlayers.each do |player|
  player.sendMessage("La ronda empieza pronto.")
end

3.times do |indice|
  Bukkit.getLogger.info("Cuenta: #{indice}")
end
```

Arrays y colecciones ofrecen métodos como `each`, `map`, `select` y `find`. Una colección Java puede necesitar `to_a` antes de usar métodos específicos de un array Ruby.

## Métodos y clases

```ruby
def anunciar(mensaje)
  Bukkit.broadcastMessage("[Arena] #{mensaje}")
end

command "start" do |sender, _args|
  anunciar("#{sender.getName} inició la ronda.")
end
```

Ruby admite métodos, argumentos predeterminados y con nombre, clases, módulos y mixins según las reglas de Ruby/JRuby. Consulta la [documentación de Ruby](https://docs.ruby-lang.org/en/) y [JRuby](https://www.jruby.org/documentation).
