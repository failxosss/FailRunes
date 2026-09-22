package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.Rune;
import dev.failxos.failrunes.api.events.RuneCooldownEvent;
import dev.failxos.failrunes.storage.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cooldowns are tracked per player+rune independently of whether the item is still held/worn (requirement #26),
 * and popups keep ticking via a repeating task until they expire.
 */
public final class CooldownService {
    private final FailRunesPlugin plugin;
    private final Map<UUID, Map<String, PopupTask>> popups = new ConcurrentHashMap<>();

    public CooldownService(FailRunesPlugin plugin) { this.plugin = plugin; }

    public boolean onCooldown(PlayerData data, Rune rune) { return data.cooldownRemaining(rune.id()) > 0.001; }

    public void start(Player player, PlayerData data, Rune rune, int level) {
        double seconds = rune.level(level).cooldown();
        RuneCooldownEvent ev = new RuneCooldownEvent(player, rune, level, seconds);
        plugin.getServer().getPluginManager().callEvent(ev);
        seconds = ev.getSeconds();
        if (seconds <= 0) return;
        data.cooldown(rune.id(), System.currentTimeMillis() + Math.round(seconds * 1000));
        if (plugin.prefs().enabled(data, rune.id(), dev.failxos.failrunes.api.RunePreference.COOLDOWN_POPUP)) startPopup(player, data, rune);
    }
    private void startPopup(Player player, PlayerData data, Rune rune) {
        popups.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .compute(rune.id(), (id, old) -> {
                    if (old != null) old.cancel();
                    PopupTask task = new PopupTask(plugin, player.getUniqueId(), rune, data);
                    task.schedule();
                    return task;
                });
    }
    public void clear(UUID uuid) {
        Map<String, PopupTask> m = popups.remove(uuid);
        if (m != null) m.values().forEach(PopupTask::cancel);
    }

    private static final class PopupTask {
        private final FailRunesPlugin plugin;
        private final UUID uuid;
        private final Rune rune;
        private final PlayerData data;
        private io.papermc.paper.threadedregions.scheduler.ScheduledTask handle;

        PopupTask(FailRunesPlugin plugin, UUID uuid, Rune rune, PlayerData data) {
            this.plugin = plugin; this.uuid = uuid; this.rune = rune; this.data = data;
        }
        void schedule() {
            handle = plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, t -> tick(), 0, 500, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
        void tick() {
            double remaining = data.cooldownRemaining(rune.id());
            Player p = plugin.getServer().getPlayer(uuid);
            if (remaining <= 0 || p == null) { cancel(); return; }
            Component msg = dev.failxos.failrunes.util.Text.mm("<gray>" + dev.failxos.failrunes.util.Text.escape(rune.name())
                    + " <dark_gray>| <yellow>" + dev.failxos.failrunes.util.Text.dec(remaining) + "s");
            plugin.getServer().getGlobalRegionScheduler().run(plugin, t -> p.sendActionBar(msg));
        }
        void cancel() { if (handle != null) handle.cancel(); }
    }
}
