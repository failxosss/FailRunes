package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.Rune;
import dev.failxos.failrunes.api.events.RuneIncinerateEvent;
import dev.failxos.failrunes.storage.PlayerData;
import dev.failxos.failrunes.util.XpUtil;
import org.bukkit.entity.Player;

/** Requirement #14: destroys unwanted runes and converts them into XP, value configurable by rune/rarity/level/percent. */
public final class IncineratorService {
    private final FailRunesPlugin plugin;
    public IncineratorService(FailRunesPlugin plugin) { this.plugin = plugin; }

    public long value(Rune rune, int level) {
        if (rune.incinerateValue() != null) return Math.round(rune.incinerateValue() * level);
        long tomeCost = plugin.settings().gamemode(plugin.settings().defaultGamemode()).cost(rune.rarity().name().toLowerCase());
        double percent = plugin.settings().gamemode(plugin.settings().defaultGamemode()).incinerateReturnPercent();
        return Math.round(tomeCost * (percent / 100.0) * level / Math.max(1, rune.maxLevel()));
    }
    public boolean incinerate(Player player, Rune rune, int level) {
        RuneIncinerateEvent ev = new RuneIncinerateEvent(player, rune, level);
        plugin.getServer().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return false;
        long xp = value(rune, level);
        XpUtil.give(player, xp);
        PlayerData data = plugin.storage().cachedOrLoad(player.getUniqueId());
        data.stats(data.stats().incinerated(1, xp));
        plugin.storage().saveAsync(data);
        return true;
    }
}
