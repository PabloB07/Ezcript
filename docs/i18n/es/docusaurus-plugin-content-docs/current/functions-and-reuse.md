---
title: Funciones y reutilización de código
---

Usa métodos Ruby normales para compartir lógica dentro de un script:

```ruby
def anunciar_ronda(mensaje)
  Bukkit.broadcastMessage("[Arena] #{mensaje}")
end

def entregar_kit(jugador)
  jugador.getInventory.addItem(ItemStack.new(Material::STONE_SWORD))
end
```

Ruby permite argumentos predeterminados y con nombre, clases, módulos y mixins según las reglas de Ruby/JRuby. Con la interoperabilidad Java puedes usar Bukkit sin volver a implementar su lógica.

## Límite entre scripts

Cada archivo `.ez` se evalúa en su propio `ScriptRuntime`. No trates un método o variable global de otro archivo como una API compartida. Cada archivo debe importar las clases Java que use.

La recarga cierra los runtimes activos, descarta callbacks y vuelve a cargar los archivos. Hasta que Ezcript tenga un sistema explícito de módulos entre scripts, coloca el código relacionado junto o impleméntalo en una biblioteca/plugin Java.

## Add-ons e integraciones

Los add-ons Skript registran sintaxis y tipos nuevos en Skript. Ezcript no carga add-ons Skript ni traduce su sintaxis. En Ruby puedes llamar la API Java documentada de otro plugin si está instalado y sus clases son accesibles:

```ruby
java_import 'org.bukkit.Bukkit'

otro_plugin = Bukkit.getPluginManager.getPlugin("ExamplePlugin")
if otro_plugin && otro_plugin.isEnabled
  # Llama aquí a la API pública documentada del plugin.
end
```

Las clases dependen del plugin externo. Revisa su API y las instrucciones de dependencias/orden de carga; Ezcript no tiene todavía un registro universal de hooks de plugins.
