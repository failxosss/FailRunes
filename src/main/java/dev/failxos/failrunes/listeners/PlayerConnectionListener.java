package dev.failxos.failrunes.listeners;

import dev.failxos.failrunes.FailRunesPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerConnectionListener implements Listener {
    private final FailRunesPlugin plugin;
    public PlayerConnectionListener(FailRunesPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) { plugin.storage().loadAsync(e.getPlayer().getUniqueId(), d -> {}); }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.cooldowns().clear(e.getPlayer().getUniqueId());
        plugin.storage().unload(e.getPlayer().getUniqueId());
    }
    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        if (plugin.chatInput().consume(e.getPlayer(), e.getMessage())) e.setCancelled(true);
    }
}
