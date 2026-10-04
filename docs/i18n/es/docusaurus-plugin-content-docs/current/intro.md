---
slug: /
title: Introducción
---

Ezcript ejecuta código Ruby real desde archivos `.ez` dentro de un servidor Paper o Purpur. Úsalo para crear funciones de servidor, minijuegos y modalidades sin hacer un plugin Java independiente para cada cambio.

Ezcript está en desarrollo inicial. Los callbacks pueden llamar la API Java de Bukkit/Paper mediante JRuby; carga únicamente scripts de confianza.

## Inicio rápido

1. Instala el jar de Ezcript en `plugins` e inicia el servidor.
2. Crea `plugins/Ezcript/scripts/welcome.ez`.
3. Añade un comando o manejador de eventos:

```ruby
java_import 'org.bukkit.Bukkit'

command "hola" do |sender, args|
  sender.sendMessage("¡Hola! Hay #{Bukkit.getOnlinePlayers.size} jugadores conectados.")
end

on :join do |event|
  event.getPlayer.sendMessage("¡Te damos la bienvenida!")
end
```

4. Ejecuta `/ezcript reload` y prueba `/hola`.

Continúa con la [DSL de Ruby](./language) o la [configuración del editor](./editor-support).
