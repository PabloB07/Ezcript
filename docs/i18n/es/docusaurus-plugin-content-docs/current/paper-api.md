---
title: API de Paper, Bukkit y Adventure
---

La integración Java de JRuby permite acceder directamente a la API Paper desde Ezcript. El plugin no envuelve cada operación Bukkit en una DSL: importa una clase Java, llama a sus métodos y respeta los requisitos de esa API.

## Imports

```ruby
java_import 'org.bukkit.Bukkit'
java_import 'org.bukkit.Material'
java_import 'org.bukkit.GameMode'
java_import 'org.bukkit.entity.Player'
java_import 'org.bukkit.inventory.ItemStack'
```

Usa `java_import` para importar clases Java. `include` en Ruby mezcla módulos en otra clase o módulo; **no** importa una clase Java por nombre.

```ruby
if sender.is_a?(Player)
  item = ItemStack.new(Material::DIAMOND, 1)
  sender.getInventory.addItem(item)
end
```

JRuby también permite acceder a clases Java con el namespace `Java::...`, aunque `java_import` suele ser más claro:

```ruby
random = Java::JavaUtil::Random.new
```

## Áreas disponibles mediante imports

La API Paper de compilación incluye servidor Bukkit, jugadores, entidades, mundos, bloques, inventarios, eventos, scheduler, comandos, permisos, scoreboards, boss bars y configuración. Adventure añade componentes de chat, títulos, sonidos, boss bars e interfaces de audiencia. Cada método depende de la versión del servidor y de las reglas del objeto/evento.

`ezcript.jar` incluye un índice del editor generado desde la Paper API configurada en Gradle. VS Code lo usa para completar clases, campos, métodos, imports y métodos heredados de Paper/Bukkit/Adventure. El índice no adapta APIs en tiempo de ejecución: un método que aparece en el editor no tiene por qué existir en todas las versiones del servidor.

## Scoreboards FastBoard

Ezcript incluye FastBoard. Usa `fastboard(player)` (o su alias `scoreboard(player)`) para obtener el scoreboard compartido del jugador. Actualízalo con la API Java de FastBoard:

```ruby
java_import 'org.bukkit.Bukkit'
java_import 'org.bukkit.entity.Player'

on :join do |event|
  player = event.getPlayer
  board = fastboard(player)
  board.updateTitle(color("&6&lEzcript"))
  update_lines(board, [
    color("&7Bienvenido,"),
    color("&f#{player.getName}"),
    "",
    color("&aJugadores: &f#{Bukkit.getOnlinePlayers.size}")
  ])
end
```

El método Java varargs `updateLines(String...)` de FastBoard no puede recibir un array Ruby de forma segura: JRuby puede pasarlo como `Object[]` y provocar `ClassCastException`. Usa el helper Ezcript `update_lines(board, array_de_lineas)`, que convierte las líneas Ruby a un `String[]` Java. Otros métodos FastBoard, como `updateTitle`, funcionan normalmente.

Todos los scripts comparten un tablero Ezcript por jugador, así que scripts distintos no reemplazan sus instancias FastBoard entre sí. Una llamada posterior a `fastboard(player)` devuelve el tablero compartido; coordina las actualizaciones de título/líneas si varios scripts lo usan. Ezcript elimina los tableros cuando el jugador sale, al recargar scripts y al desactivar el plugin. El helper espera llamadas en el hilo del servidor. FastBoard admite códigos legacy `&` mediante `color` y RGB como `&#55ccff`.

## Inventarios SpiGUI

Ezcript incluye SpiGUI y lo inicializa para el plugin. Crea un menú paginado con `gui(título, filas, tag)`, crea un botón desde un `ItemStack` con `create_button(item) { |click| ... }`, agrégalo con `addButton` de SpiGUI y ábrelo con `open_gui(player, menu)`:

```ruby
java_import 'org.bukkit.entity.Player'

command "menu" do |sender, args|
  next unless sender.is_a?(Player)

  menu = gui(color("&6Menú del servidor"), 3, "server-menu")
  icon = create_item("DIAMOND", 1, "&bInformación", ["&7Haz clic para ver información"])
  menu.addButton(create_button(icon) do |click|
    click.getWhoClicked.sendMessage(mini("<green>¡Bienvenido al servidor!</green>"))
  end)
  open_gui(sender, menu)
end
```

`create_item(material, cantidad, nombre, lore)` resuelve un nombre de material Bukkit y traduce colores `&`/RGB del nombre visible y el lore. SpiGUI cancela automáticamente las interacciones normales de los botones del menú y admite paginación. Los callbacks se ejecutan en el hilo del servidor. `/ezcript reload` cierra los menús SpiGUI abiertos antes de terminar sus runtimes para que callbacks antiguos no ejecuten código Ruby descargado.

## Colores de texto

- `color("&eAmarillo &lnegrita")` devuelve un texto con colores legacy; admite RGB en formato `&#RRGGBB` o `&#aabbcc`.
- `mini("<gradient:#55ccff:#aa55ff>Texto RGB</gradient>")` devuelve un `Component` Adventure analizado por MiniMessage. Envíalo mediante las APIs de `sendMessage` de Paper compatibles con Adventure.
- Para usar otras funciones Adventure, importa la clase correspondiente con `java_import`.

## Scheduler y concurrencia

La mayoría de las operaciones sobre mundo, entidades e inventario debe ejecutarse en el hilo principal. Los callbacks de eventos son síncronos por defecto. Para trabajo demorado o periódico usa el scheduler Bukkit. Las tareas asíncronas no deben acceder a estado Bukkit que no sea thread-safe; vuelve al hilo principal antes de modificarlo.

Consulta [Tareas y temporizadores](./tasks-and-timers).

## Reglas importantes

- Verifica que el evento permita cancelación antes de llamar a `setCancelled`.
- Comprueba resultados Java `nil`/`null` antes de encadenar métodos.
- Respeta las reglas de hilo principal de Bukkit.
- Evita I/O bloqueante o bucles lentos en callbacks síncronos.
- Las APIs Java importadas tienen acceso privilegiado al servidor; los scripts no están aislados.

## Versión

El índice se genera desde la dependencia Paper API de `build.gradle` (`26.2.build.112-stable`) e incluye las APIs públicas incluidas de FastBoard 2.2.2 y SpiGUI 1.4.1. La documentación publicada de SpiGUI indica pruebas hasta Paper 1.20; compilar contra Paper 26.2 no garantiza compatibilidad en ejecución con cada versión Paper/Purpur. Prueba el plugin y los menús en cada servidor objetivo antes de desplegarlos. Recompila el jar al cambiar las versiones de las APIs. Consulta los [ejemplos de API Paper 26](./paper-26-examples).
