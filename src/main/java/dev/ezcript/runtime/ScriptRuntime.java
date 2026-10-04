package dev.ezcript.runtime;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import fr.mrmicky.fastboard.FastBoard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ScriptRuntime implements AutoCloseable {
    private static final String RUBY_PRELUDE = """
            def command(name, &callback)
              raise ArgumentError, "command requires a block" unless callback
              $ez.register_command(name.to_s, callback)
            end

            def on(event_name, &callback)
              raise ArgumentError, "on requires a block" unless callback
              $ez.register_event(event_name.to_s, callback)
            end

            def fastboard(player)
              $ez.fastboard(player)
            end

            def scoreboard(player)
              fastboard(player)
            end

            def update_lines(board, lines)
              $ez.update_board_lines(board, lines.to_a)
            end

            def gui(title, rows, tag = nil)
              $ez.create_menu(title.to_s, Integer(rows), tag&.to_s)
            end

            def create_button(item, &callback)
              raise ArgumentError, "create_button requires a block" unless callback
              $ez.create_button(item, callback)
            end

            def create_item(material, amount = 1, name = nil, lore = [])
              $ez.create_item(material.to_s, Integer(amount), name&.to_s, lore.to_a)
            end

            def open_gui(player, menu)
              $ez.open_menu(player, menu)
            end

            def color(text)
              $ez.colorize(text.to_s)
            end

            def mini(text)
              $ez.mini_message(text.to_s)
            end
            """;

    private final ScriptingContainer ruby = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
    private final EzcriptBridge bridge;
    private final Path file;
    private final List<PendingCommand> pendingCommands = new ArrayList<>();
    private final List<PendingEvent> pendingEvents = new ArrayList<>();

    public ScriptRuntime(dev.ezcript.EzcriptPlugin plugin, Path file) {
        this.file = file;
        this.bridge = new EzcriptBridge(plugin, this);
        ruby.put("$ez", bridge);
    }

    public void load(String source) {
        ruby.runScriptlet(RUBY_PRELUDE);
        ruby.runScriptlet(source);
    }

    static String rubyPrelude() {
        return RUBY_PRELUDE;
    }

    public void activate() {
        dev.ezcript.EzcriptPlugin plugin = bridge.plugin();
        for (PendingCommand command : pendingCommands) {
            plugin.registerScriptCommand(command.name(), (sender, arguments) ->
                    invoke(command.callback(), sender, arguments));
        }
        for (PendingEvent event : pendingEvents) {
            plugin.registerScriptEvent(event.name(), value -> invoke(event.callback(), value));
        }
        pendingCommands.clear();
        pendingEvents.clear();
    }

    void registerCommand(String name, Object callback) {
        bridge.plugin().validateScriptCommand(name);
        pendingCommands.add(new PendingCommand(name, callback));
    }

    void registerEvent(String name, Object callback) {
        bridge.plugin().validateScriptEvent(name);
        pendingEvents.add(new PendingEvent(name, callback));
    }

    private void invoke(Object callback, Object... arguments) {
        synchronized (ruby) {
            try {
                ruby.callMethod(callback, "call", arguments, Object.class);
            } catch (RuntimeException exception) {
                throw new ScriptExecutionException(file, exception);
            }
        }
    }

    FastBoard fastboard(Player player) {
        return bridge.plugin().fastboard(player);
    }

    @Override
    public void close() {
        ruby.terminate();
    }

    public interface ScriptCommand {
        void run(CommandSender sender, String[] arguments);
    }

    public interface ScriptEvent {
        void run(Event event);
    }

    private record PendingCommand(String name, Object callback) { }
    private record PendingEvent(String name, Object callback) { }

    public static final class ScriptExecutionException extends RuntimeException {
        public ScriptExecutionException(Path file, RuntimeException cause) {
            super("Error executing " + file.getFileName() + ": " + cause.getMessage(), cause);
        }
    }

    public static final class EzcriptBridge {
        private final dev.ezcript.EzcriptPlugin plugin;
        private final ScriptRuntime runtime;

        private EzcriptBridge(dev.ezcript.EzcriptPlugin plugin, ScriptRuntime runtime) {
            this.plugin = plugin;
            this.runtime = runtime;
        }

        public dev.ezcript.EzcriptPlugin plugin() {
            return plugin;
        }

        public FastBoard fastboard(Player player) {
            return runtime.fastboard(player);
        }

        public void update_board_lines(FastBoard board, List<?> lines) {
            FastBoardSupport.updateLines(board, lines);
        }

        public SGMenu create_menu(String title, int rows, String tag) {
            return plugin.createScriptMenu(title, rows, tag);
        }

        public SGButton create_button(ItemStack item, Object callback) {
            if (callback == null) {
                throw new IllegalArgumentException("create_button requires a callback");
            }
            return new SGButton(item).withListener(event -> runtime.invoke(callback, event));
        }

        public ItemStack create_item(String materialName, int amount, String name, List<?> lore) {
            Material material = Material.matchMaterial(materialName);
            if (material == null || material.isAir()) {
                throw new IllegalArgumentException("Unknown or unusable item material: " + materialName);
            }
            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();
            if (name != null) {
                meta.setDisplayName(ColorFormatter.colorize(name));
            }
            if (lore != null && !lore.isEmpty()) {
                List<String> formattedLore = lore.stream()
                        .map(line -> ColorFormatter.colorize(String.valueOf(line)))
                        .toList();
                meta.setLore(formattedLore);
            }
            item.setItemMeta(meta);
            return item;
        }

        public void open_menu(Player player, SGMenu menu) {
            player.openInventory(menu.getInventory());
        }

        public String colorize(String text) {
            return ColorFormatter.colorize(text);
        }

        public Component mini_message(String text) {
            return MiniMessage.miniMessage().deserialize(text);
        }

        public void register_command(String name, Object callback) {
            runtime.registerCommand(name, callback);
        }

        public void register_event(String name, Object callback) {
            runtime.registerEvent(name, callback);
        }
    }

}
