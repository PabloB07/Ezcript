---
title: Delays, tareas periódicas y scheduler
---

Skript tiene efectos de espera y triggers periódicos. Ezcript todavía no define un `wait` Ruby ni DSL de triggers periódicos. Usa explícitamente el scheduler Bukkit mediante interoperabilidad Java.

## Programar una tarea síncrona demorada

Conceptualmente, una tarea demorada necesita estos tres valores:

```text
instancia del plugin + callback Java Runnable + demora en ticks
```

Ezcript todavía no expone su instancia JavaPlugin a los scripts ni ofrece un helper probado para convertir Ruby en un `Runnable`. Por eso aún no hay un ejemplo de scheduler listo para copiar. Obtén una instancia válida del plugin y construye el `Runnable` Java con la conversión SAM que soporte la versión JRuby incluida antes de llamar `runTaskLater(plugin, runnable, ticks)` de Bukkit. No inventes un helper `EzcriptPlugin.instance`: no existe en la API actual.

En condiciones ideales transcurren 20 ticks por segundo; con lag, el tiempo real es mayor. El scheduler requiere un plugin activo. No crees tareas repetidas sin una estrategia para cancelarlas y limpiarlas al recargar.

## Reglas de hilo

Usa tareas síncronas para modificar mundos, jugadores e inventarios. Reserva tareas asíncronas para trabajo independiente de archivos o red; no leas ni modifiques estado Bukkit ordinario desde ellas.

## Helpers futuros de Ezcript

Una DSL propia de scheduler necesita propiedad explícita del plugin, handles de cancelación, limpieza durante recargas y reglas de concurrencia documentadas. Hasta que exista, usa la API Bukkit directamente con cuidado.
