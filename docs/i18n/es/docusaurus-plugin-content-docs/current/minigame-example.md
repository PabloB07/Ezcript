---
title: Ejemplo de minijuego
---

Este ejemplo pequeño muestra qué se puede hacer con la DSL actual de comandos/eventos sin afirmar que Ezcript ya incluye toda la biblioteca de helpers de Skript. Los participantes se almacenan en memoria Ruby y se pierden al recargar el script o apagar el servidor.

```ruby
java_import 'org.bukkit.Bukkit'
java_import 'org.bukkit.entity.Player'

$jugadores_arena = {}

command "arena" do |sender, args|
  accion = args.to_a.first

  case accion
  when "join"
    unless sender.is_a?(Player)
      sender.sendMessage("Solo los jugadores pueden entrar a la arena.")
      next
    end

    $jugadores_arena[sender.getUniqueId] = sender
    sender.sendMessage("Entraste a la arena.")
    Bukkit.broadcastMessage("#{sender.getName} entró a la arena.")
  when "leave"
    unless sender.is_a?(Player)
      sender.sendMessage("Solo los jugadores pueden salir de la arena.")
      next
    end

    $jugadores_arena.delete(sender.getUniqueId)
    sender.sendMessage("Saliste de la arena.")
  when "list"
    nombres = $jugadores_arena.values.map(&:getName)
    sender.sendMessage("Jugadores: #{nombres.empty? ? 'ninguno' : nombres.join(', ')}")
  else
    sender.sendMessage("Uso: /arena <join|leave|list>")
  end
end

on :quit do |event|
  $jugadores_arena.delete(event.getPlayer.getUniqueId)
end
```

Prueba `/arena join`, `/arena list` y `/arena leave`. La limpieza de desconexión también se ejecuta si un participante abandona el servidor.

## Límites del ejemplo

- Los participantes solo están en memoria; no son variables Skript persistentes.
- Los argumentos se separan por espacios; la DSL no ofrece metadata de uso, permisos, aliases ni autocompletado.
- Es un registro de participantes, no un motor completo de minijuegos. No incluye colas, equipos, cuentas regresivas, configuración de arenas, restauración de inventario, recompensas ni persistencia de partidas.
- No añadas temporizadores con `Thread.sleep`; usa una integración del scheduler Bukkit que gestione su ciclo de vida.

El patrón usa bloques JRuby, hashes/arrays Ruby y llamadas Paper. Consulta la [matriz de cobertura](./feature-coverage) antes de asumir que otra función de Skript ya está integrada.
