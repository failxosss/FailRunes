package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.FailRunesPlugin;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** Simple one-shot "type your next chat line into the GUI" helper used by search boxes. */
public final class ChatInputService {
    private final Map<UUID, Consumer<String>> waiting = new ConcurrentHashMap<>();
    private final FailRunesPlugin plugin;
    public ChatInputService(FailRunesPlugin plugin) { this.plugin = plugin; }

    public void awaitLine(Player p, Consumer<String> onInput) { waiting.put(p.getUniqueId(), onInput); }
    public boolean consume(Player p, String line) {
        Consumer<String> c = waiting.remove(p.getUniqueId());
        if (c == null) return false;
        plugin.getServer().getGlobalRegionScheduler().run(plugin, t -> c.accept(line));
        return true;
    }
}
