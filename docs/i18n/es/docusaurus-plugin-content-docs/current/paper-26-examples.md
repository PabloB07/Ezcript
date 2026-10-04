---
title: Ejemplos de la API Paper 26
---

Ezcript ejecuta Ruby normal mediante JRuby; no reemplaza la API Java de Paper por una API paralela. Importa los tipos Paper que necesites, usa el control de flujo Ruby e invoca los métodos Java. Estos ejemplos apuntan a Paper API 26.2, la versión seleccionada por este proyecto.

## Chat Adventure, títulos y barras de acción

`mini` convierte el marcado MiniMessage en un `Component` Adventure. Los métodos `Player`/`Audience` de Paper pueden recibir ese componente directamente:

```ruby
java_import 'org.bukkit.entity.Player'
java_import 'net.kyori.adventure.title.Title'

command "bienvenida" do |sender, args|
  next unless sender.is_a?(Player)

  sender.sendMessage(mini("<gradient:#55ccff:#aa55ff>¡Bienvenido, #{sender.getName}!</gradient>"))
  sender.sendActionBar(mini("<green>Tu aventura comienza ahora.</green>"))
  sender.showTitle(Title.title(
    mini("<gold><bold>Ezcript"),
    mini("<gray>Scripts para Paper 26")
  ))
end
```

Para cadenas legacy y RGB sencillas, usa `color("&eDorado")` y `color("&#55ccffCeleste")`. MiniMessage tiene su propia sintaxis de tags; `color` no analiza tags MiniMessage.

## Eventos de bloques y efectos de mundo

El alias `:break` entrega un `BlockBreakEvent`. Usa sus métodos Bukkit para comprobar permisos y el bloque, cancelar el evento cuando corresponda y llamar a la API del mundo:

```ruby
java_import 'org.bukkit.Material'
java_import 'org.bukkit.Particle'

on :break do |event|
  jugador = event.getPlayer
  bloque = event.getBlock

  next unless bloque.getType == Material::DIAMOND_ORE
  next unless jugador.hasPermission("ezcript.mining.bonus")

  event.setCancelled(true)
  bloque.setType(Material::AIR)
  bloque.getWorld.spawnParticle(
    Particle::HAPPY_VILLAGER,
    bloque.getLocation.add(0.5, 0.5, 0.5),
    12
  )
  jugador.sendMessage(mini("<green>Mineral extra obtenido.</green>"))
end
```

Este handler se ejecuta de forma síncrona en el hilo del servidor. Comprueba que el evento permita cancelación antes de llamar a `setCancelled` y evita trabajo costoso en el handler.

## Ítems de inventario y estado del jugador

Usa el helper `create_item` para crear un nombre/lore con colores y luego la API normal de inventarios Paper para colocar el objeto. Así no pasas arrays Ruby a métodos Java varargs:

```ruby
java_import 'org.bukkit.entity.Player'

command "starter" do |sender, args|
  next unless sender.is_a?(Player)
  next unless sender.hasPermission("ezcript.starter")

  item = create_item(
    "IRON_SWORD",
    1,
    "&bEspada inicial",
    ["&7Un primer objeto de aventura", "&#55ccffVinculada a #{sender.getName}"]
  )
  sender.getInventory.setItem(0, item)
  sender.setLevel([sender.getLevel, 1].max)
  sender.sendMessage(mini("<green>Objeto inicial añadido.</green>"))
end
```

Métodos Paper como `getInventory`, `setItem`, `setLevel`, `hasPermission` y `sendMessage` son métodos Java; Ruby aporta los condicionales e interpolación.

## Autocompletado de Paper en VS Code

El índice del editor se genera desde la misma dependencia de compilación de Ezcript. Incluye tipos/métodos públicos de Paper/Bukkit/Adventure, sus métodos públicos heredados y las APIs incluidas de FastBoard/SpiGUI. Tras cambiar la versión de Paper API, recompila `ezcript.jar`, configura `ezcript.serverJar` con ese jar y reinicia el servidor de lenguaje. El autocompletado describe la API de compilación, no garantiza compatibilidad en ejecución con otra build del servidor.

## Compatibilidad

El build actual apunta a Paper API `26.2.build.112-stable` y Java 25. Paper 26.3 es una versión previa en el repositorio configurado y sus APIs podrían cambiar; este build aún no se ha verificado contra ella. Para detalles específicos, consulta siempre los Javadocs de la versión exacta de Paper instalada en el servidor.

Los ejemplos completos también están en la raíz del proyecto en `examples/paper-26-api.ez` para copiarlos directamente al directorio de scripts del servidor.
