package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.Rune;
import dev.failxos.failrunes.api.RunePool;
import dev.failxos.failrunes.api.events.RuneIdentifyEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/** Requirement #9-#10: rolls the hidden rune inside an unidentified item and reveals name/level/type/chances. */
public final class IdentifyService {
    private final FailRunesPlugin plugin;
    private final Random random = new Random();
    public IdentifyService(FailRunesPlugin plugin) { this.plugin = plugin; }

    /** @return the revealed rune item, or null if identification failed/was cancelled. */
    public ItemStack identify(Player player, ItemStack unidentified, String pool, int minLevel, int maxLevel) {
        RunePool p = plugin.pools().get(pool);
        if (p == null || p.table().isEmpty()) return null;
        var entry = p.table().roll(random);
        if (entry == null) return null;
        Rune rune = plugin.runes().get(entry.runeId());
        if (rune == null) return null;
        int level = Math.max(minLevel, Math.min(maxLevel, entry.minLevel() + random.nextInt(Math.max(1, entry.maxLevel() - entry.minLevel() + 1))));
        RuneIdentifyEvent ev = new RuneIdentifyEvent(player, rune, level);
        plugin.getServer().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return null;
        return plugin.itemFactory().runeItem(rune, level);
    }
}
