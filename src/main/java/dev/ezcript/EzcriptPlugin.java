package dev.ezcript;

import com.samjakob.spigui.SpiGUI;
import com.samjakob.spigui.menu.SGMenu;
import dev.ezcript.runtime.ScriptRuntime;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class EzcriptPlugin extends JavaPlugin implements Listener {
    private final Map<String, ScriptRuntime.ScriptCommand> scriptCommands = new HashMap<>();
    private final Map<UUID, FastBoard> scoreboards = new HashMap<>();
    private final List<ScriptRuntime> runtimes = new ArrayList<>();
    private Path scriptsDirectory;
    private SpiGUI spiGUI;

    @Override
    public void onEnable() {
        scriptsDirectory = getDataFolder().toPath().resolve("scripts");
        try {
            Files.createDirectories(scriptsDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create the scripts directory", exception);
        }
        getServer().getPluginManager().registerEvents(this, this);
        reloadScripts();
        getLogger().info("Ezcript is ready. Scripts: " + scriptsDirectory.toAbsolutePath());
    }

    @Override
    public void onDisable() {
        closeScriptMenus();
        closeRuntimes();
        closeScoreboards();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadScripts();
            sender.sendMessage(ChatColor.GREEN + "Ezcript scripts reloaded.");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("lint")) {
            int issues = 0;
            try (var paths = Files.list(scriptsDirectory)) {
                for (Path path : paths.filter(file -> file.toString().endsWith(".ez")).sorted().toList()) {
                    var diagnostics = ScriptAnalyzer.analyze(Files.readString(path));
                    for (var diagnostic : diagnostics) {
                        issues++;
                        sender.sendMessage(ChatColor.YELLOW.toString() + path.getFileName() + ":" +
                                diagnostic.line() + ": " + diagnostic.message());
                    }
                }
            } catch (IOException exception) {
                getLogger().log(Level.SEVERE, "Could not lint scripts", exception);
                sender.sendMessage(ChatColor.RED + "Could not read the scripts directory.");
                return true;
            }
            sender.sendMessage(issues == 0
                    ? ChatColor.GREEN + "No lint issues found."
                    : ChatColor.YELLOW.toString() + issues + " issue(s) found.");
            return true;
        }
        sender.sendMessage(ChatColor.YELLOW + "Usage: /ezcript <reload|lint>");
        return true;
    }

    private void reloadScripts() {
        closeRuntimes();
        closeScoreboards();
        closeScriptMenus();
        HandlerList.unregisterAll((org.bukkit.plugin.Plugin) this);
        getServer().getPluginManager().registerEvents(this, this);
        spiGUI = new SpiGUI(this);
        scriptCommands.clear();

        try (var paths = Files.list(scriptsDirectory)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".ez")).sorted().toList()) {
                ScriptRuntime runtime = new ScriptRuntime(this, path);
                try {
                    runtime.load(Files.readString(path));
                    runtime.activate();
                    runtimes.add(runtime);
                    getLogger().info("Loaded " + path.getFileName());
                } catch (Exception exception) {
                    runtime.close();
                    getLogger().log(Level.SEVERE, "Failed to load script " + path.getFileName(), exception);
                }
            }
        } catch (IOException exception) {
            getLogger().log(Level.SEVERE, "Could not read the scripts directory", exception);
        }
    }

    private void closeRuntimes() {
        for (ScriptRuntime runtime : runtimes) {
            runtime.close();
        }
        runtimes.clear();
    }

    private void closeScoreboards() {
        for (FastBoard board : scoreboards.values()) {
            board.delete();
        }
        scoreboards.clear();
    }

    private void closeScriptMenus() {
        for (Player player : getServer().getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof SGMenu) {
                player.closeInventory();
            }
        }
    }

    public SGMenu createScriptMenu(String title, int rows, String tag) {
        if (spiGUI == null) {
            throw new IllegalStateException("SpiGUI is not initialized");
        }
        return spiGUI.create(title, rows, tag);
    }

    public FastBoard fastboard(Player player) {
        return scoreboards.computeIfAbsent(player.getUniqueId(), ignored -> new FastBoard(player));
    }

    public void registerScriptCommand(String name, ScriptRuntime.ScriptCommand callback) {
        scriptCommands.put(normalizeScriptCommand(name), callback);
    }

    public void validateScriptCommand(String name) {
        normalizeScriptCommand(name);
    }

    private String normalizeScriptCommand(String name) {
        String normalized = name.toLowerCase(Locale.ROOT).replaceFirst("^/", "");
        if (normalized.isBlank() || normalized.contains(" ")) {
            throw new IllegalArgumentException("Script command names must be a single word");
        }
        return normalized;
    }

    public void registerScriptEvent(String name, ScriptRuntime.ScriptEvent callback) {
        Class<? extends Event> eventClass = scriptEventClass(name);
        Listener listener = new Listener() { };
        EventExecutor executor = (ignored, event) -> callback.run(event);
        getServer().getPluginManager().registerEvent(
                eventClass, listener, EventPriority.NORMAL, executor, this, true);
    }

    public void validateScriptEvent(String name) {
        scriptEventClass(name);
    }

    private Class<? extends Event> scriptEventClass(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "join", "player_join" -> org.bukkit.event.player.PlayerJoinEvent.class;
            case "quit", "player_quit" -> org.bukkit.event.player.PlayerQuitEvent.class;
            case "death", "player_death" -> org.bukkit.event.entity.PlayerDeathEvent.class;
            case "break", "block_break" -> org.bukkit.event.block.BlockBreakEvent.class;
            case "place", "block_place" -> org.bukkit.event.block.BlockPlaceEvent.class;
            case "interact", "player_interact" -> org.bukkit.event.player.PlayerInteractEvent.class;
            case "damage", "entity_damage" -> org.bukkit.event.entity.EntityDamageByEntityEvent.class;
            default -> throw new IllegalArgumentException("Unknown Ezcript event: " + name);
        };
    }

    @org.bukkit.event.EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        executeScriptCommand(event.getPlayer(), event.getMessage(), event::setCancelled);
    }

    @org.bukkit.event.EventHandler
    public void onConsoleCommand(ServerCommandEvent event) {
        executeScriptCommand(event.getSender(), event.getCommand(), event::setCancelled);
    }

    @org.bukkit.event.EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        FastBoard board = scoreboards.remove(event.getPlayer().getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    private void executeScriptCommand(CommandSender sender, String input, java.util.function.Consumer<Boolean> cancel) {
        String commandLine = input.startsWith("/") ? input.substring(1) : input;
        String[] parts = commandLine.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isBlank()) return;
        ScriptRuntime.ScriptCommand callback = scriptCommands.get(parts[0].toLowerCase(Locale.ROOT));
        if (callback == null) return;
        cancel.accept(true);
        try {
            callback.run(sender, Arrays.copyOfRange(parts, 1, parts.length));
        } catch (RuntimeException exception) {
            getLogger().log(Level.SEVERE, "Script command failed: " + parts[0], exception);
            sender.sendMessage(ChatColor.RED + "That Ezcript command failed; check the server log.");
        }
    }
}
