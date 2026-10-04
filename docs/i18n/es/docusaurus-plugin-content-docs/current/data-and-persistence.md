---
title: Valores y persistencia
---

Skript ofrece variables integradas y expresiones tipadas. Ezcript usa valores y objetos Ruby; actualmente no incluye `{variables}` de Skript, variables de listas, persistencia automática ni expresiones de tipos propias.

## Valores y colecciones Ruby

```ruby
participantes = []
participantes << player.getUniqueId
participantes.delete(player.getUniqueId)

puntuaciones = {
  player.getUniqueId.toString => 10
}
puntuaciones[player.getUniqueId.toString] += 5
```

Los hashes y arrays pertenecen al runtime Ruby actual. No se escriben automáticamente a disco, no se comparten entre scripts ni se restauran después de reiniciar.

## Ámbito y recarga

Las variables locales, las variables de instancia (`@ronda`) y las constantes siguen las reglas Ruby dentro de su runtime. Cada archivo se carga en su propio `ScriptRuntime`; no dependas de una variable global Ruby definida en otro `.ez`.

`/ezcript reload` cierra los runtimes y crea otros nuevos. Los valores en memoria y callbacks registrados se pierden. Guarda explícitamente los datos que deban persistir en un archivo o base de datos.

## Persistencia YAML explícita vía Bukkit

Puedes usar la configuración Bukkit mediante interoperabilidad Java. Esto no es una abstracción propia de Ezcript:

```ruby
java_import 'org.bukkit.configuration.file.YamlConfiguration'
java_import 'java.io.File'

archivo_datos = File.new("plugins/Ezcript/datos-jugador.yml")
datos = YamlConfiguration.loadConfiguration(archivo_datos)

def guardar_monedas(datos, archivo_datos, jugador, cantidad)
  datos.set("coins.#{jugador.getUniqueId}", cantidad)
  datos.save(archivo_datos)
end
```

En producción, crea los directorios necesarios, gestiona errores de I/O y evita guardar un archivo completo en eventos frecuentes. Para conjuntos grandes, considera una base de datos. Ezcript todavía no incluye gestor de bases de datos, migraciones ni helpers de persistencia asíncrona.

## Datos de eventos

Los disparadores Skript proporcionan valores como `player`, `event-block` o `attacker`. En Ezcript, el bloque recibe `event`; lee sus datos mediante métodos Bukkit/Paper y asígnalos a variables Ruby si quieres nombres más breves.
