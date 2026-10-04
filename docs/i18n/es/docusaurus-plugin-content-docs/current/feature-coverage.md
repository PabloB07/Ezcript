---
title: Cobertura de funciones Skript
---

Esta matriz separa lo que aportan Ruby/JRuby o Paper de las funciones implementadas por Ezcript. «Usar la API Paper» significa que un script puede llamar una API Java documentada mediante JRuby; no significa que Ezcript implemente aliases, DSL equivalente a Skript o verificaciones adicionales.

## Disponible hoy

| Área | Estado | Uso |
| --- | --- | --- |
| Scripts Ruby `.ez` | Implementado | JRuby ejecuta cada script al cargar |
| Callbacks de comandos | Implementación básica | `command "nombre" do |sender, args| ... end` |
| Callbacks de eventos | Implementación limitada | Siete eventos y aliases en [Comandos y eventos](./commands-and-events) |
| Scoreboards e inventarios GUI | Helpers FastBoard y SpiGUI | `fastboard(player)`, `gui(...)`, `create_button(...)`; [guía de API](./paper-api) |
| Texto legacy, RGB y MiniMessage | Helpers básicos | `color("&a...")`, `color("&#55ccff...")`, `mini("<green>...</green>")` |
| Condiciones, métodos, colecciones, clases y bloques Ruby | Ruby/JRuby | Sintaxis Ruby normal |
| API Paper/Bukkit/Adventure | Interoperabilidad Java | `java_import 'paquete.Clase'` |
| Lint de sintaxis/estilo | Básico | Parser Ruby, espacios finales y límite de 120 caracteres |
| VS Code: gramática/temas/LSP | Implementación inicial | Gramática, tres temas, índice Paper, hover y autocompletado básicos |
| Recarga de scripts | Básico | `/ezcript reload` cierra y vuelve a cargar archivos `.ez` |

El ejemplo de helpers de interfaz está en el repositorio en `examples/lobby-ui.ez`.

## Áreas de Skript todavía no integradas

Esta lista agrupa familias de funciones; no promete un reemplazo Ruby uno a uno para cada expresión upstream. El catálogo exacto de Skript cambia con el tiempo; consulta la [referencia oficial](https://docs.skriptlang.org/) para ver sus funciones actuales.

| Área de Skript / ejemplos | Estado en Ezcript | Dirección Ruby prevista |
| --- | --- | --- |
| Catálogo de eventos: chat, comandos, movimiento, reaparición, inventario, mundo, entidades, bloques y ciclo de vida del servidor | Solo se registran siete tipos; consulta [comandos y eventos](./commands-and-events) | Registros explícitos con objetos tipados, prioridades, cancelación y reglas de hilo documentadas |
| Valores/expresiones de evento, condiciones de trigger y controles de prioridad/cancelación | No están implementados en la DSL | Contexto de evento tipado y opciones explícitas de registro |
| Comandos: uso, análisis/tipos de argumentos, permisos, aliases, ejecutor/consola y autocompletado | Solo callback con nombre; argumentos separados por espacios | Definiciones de comandos con opciones validadas y argumentos tipados |
| Expresiones/conversiones: propiedades de jugador/entidad, ubicaciones, bloques, ítems, inventarios, fechas, duraciones, números y texto | No hay aliases de expresiones ni sistema de conversiones Skript | Valores Ruby, objetos Paper tipados y helpers optativos |
| Condiciones: permisos, comparaciones, regiones/rangos y predicados de eventos | No hay aliases de condiciones Skript | Predicados Ruby y APIs Paper/plugins |
| Efectos de jugadores/entidades: mensajes, teletransporte, salud, daño, pociones, aparición, eliminación y velocidad | No hay aliases de efectos Skript | Llamadas Paper directas o helpers documentados |
| Efectos de mundos/bloques: cambiar bloques, drops, explosiones, clima, hora y propiedades del mundo | No hay aliases de efectos Skript | Llamadas Paper con validación y helpers seguros para el ciclo de vida |
| Ítems/inventarios: dar/quitar ítems, ranuras, equipamiento, contenidos, drops y menús GUI | `create_item`, `create_button`, `gui`, `open_gui` cubren menús SpiGUI básicos; no hay aliases de efectos Skript | APIs Paper/SpiGUI directas; los efectos restantes requieren helpers específicos |
| Texto y presentación: formato de chat, texto interactivo, títulos, barras de acción, sonidos, partículas y efectos | Existe formato legacy/RGB y conversión MiniMessage; no hay aliases de efectos Skript | Componentes Adventure y APIs Paper |
| Scoreboards, equipos, boss bars, metadata, permisos, economía e integraciones específicas de plugins | Existe helper FastBoard; las otras familias no tienen wrappers | Llamar a la API Paper o del plugin instalado; los adapters necesitan soporte separado |
| Variables: locales/globales/listas, persistencia, ámbitos y base de datos de variables | Las variables Ruby viven en el runtime del script; no hay almacenamiento persistente Ezcript | API explícita con ámbito, serialización y reglas de persistencia |
| Secciones de control como bucles, condiciones, funciones y triggers reutilizables | El control/métodos Ruby funciona dentro del script; no hay sintaxis de secciones Skript ni registro Ezcript entre archivos | Métodos/módulos Ruby y carga entre archivos con ciclo de vida explícito |
| `wait`, triggers demorados/periódicos, handles y cancelación de tareas | No hay DSL de scheduler Ezcript; consulta [tareas y temporizadores](./tasks-and-timers) | Wrappers con handles de cancelación y limpieza segura en recargas |
| Options, aliases, metadata, secciones de carga/descarga y configuración | No implementado | Configuración Ruby validada y hooks explícitos del ciclo de vida |
| Archivos, bases de datos, HTTP y otras integraciones que suelen aportar add-ons de Skript | Sin capa integrada de adapters | Usar bibliotecas Java/APIs de plugins directamente o crear adapters mantenidos y documentados |
| Sintaxis, hooks del parser y compatibilidad con add-ons de Skript | No compatible | Integrarse con la API Java del add-on; su sintaxis Skript no se transfiere automáticamente |
| LSP: símbolos, definiciones, referencias, renombrar, diagnósticos semánticos y tipos | Parcial; existen completado/hover Paper | Ampliar análisis Ruby y la inferencia de tipos Paper |

Ezcript está en desarrollo inicial. Esta página es una matriz y hoja de ruta, no afirma que las funciones futuras estén disponibles. Ruby puede expresar un algoritmo, pero no implementa por sí sola eventos de servidor, persistencia, permisos, registro de comandos ni limpieza de tareas.

## Orden sugerido para llegar a paridad

1. Amplio mapeo de eventos y contexto de evento.
2. Definiciones robustas de comandos, argumentos y permisos.
3. Handles de tareas/scheduler seguros frente a recargas.
4. Variables persistentes y almacenamiento.
5. Funciones reutilizables y módulos entre archivos.
6. Diagnósticos, tipos e inferencia del editor.

Consulta [la documentación oficial de Skript](https://docs.skriptlang.org/) para su catálogo de funciones actual.
