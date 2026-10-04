package dev.ezcript.lsp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperApiIndexTest {
    @Test
    void loadsThePaperApiAndInheritedPlayerMethods() throws Exception {
        PaperApiIndex api = PaperApiIndex.load();

        assertNotNull(api.findClass("org.bukkit.Bukkit"));
        assertNotNull(api.findClass("org.bukkit.event.player.PlayerJoinEvent"));
        assertNotNull(api.findClass("fr.mrmicky.fastboard.FastBoard"));
        assertNotNull(api.findClass("com.samjakob.spigui.SpiGUI"));
        assertNotNull(api.findClass("java.util.Collection"));
        assertTrue(api.methods("fr.mrmicky.fastboard.FastBoard").stream()
                .anyMatch(method -> method.name().equals("updateLines")));
        assertTrue(api.methods("com.samjakob.spigui.SpiGUI").stream()
                .anyMatch(method -> method.name().equals("create")));
        assertTrue(api.methods("org.bukkit.entity.Player").stream()
                .anyMatch(method -> method.name().equals("sendMessage")));
        assertTrue(api.methods("org.bukkit.entity.Player").stream()
                .anyMatch(method -> method.name().equals("getWorld")));
        assertTrue(api.methods("org.bukkit.entity.Player").stream()
                .anyMatch(method -> method.name().equals("sendActionBar")));
        assertTrue(api.methods("java.util.Collection").stream()
                .anyMatch(method -> method.name().equals("size")));
    }
}
