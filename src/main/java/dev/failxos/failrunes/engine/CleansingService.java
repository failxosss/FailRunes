package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.events.RuneCleanseEvent;
import dev.failxos.failrunes.items.AppliedRunes;
import dev.failxos.failrunes.storage.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Requirement #15: Cleansing Wands remove one rune from an item; confirmation is handled by the GUI layer. */
public final class CleansingService {
    private final FailRunesPlugin plugin;
    public CleansingService(FailRunesPlugin plugin) { this.plugin = plugin; }

    public boolean cleanse(Player player, ItemStack item, String runeId) {
        var rune = plugin.runes().get(runeId);
        if (rune == null) return false;
        AppliedRunes ar = plugin.appliedRunes();
        Integer level = ar.read(item).get(runeId);
        if (level == null) return false;
        RuneCleanseEvent ev = new RuneCleanseEvent(player, rune, level);
        plugin.getServer().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return false;
        ar.remove(item, runeId);
        PlayerData data = plugin.storage().cachedOrLoad(player.getUniqueId());
        data.stats(data.stats().cleansed(1));
        plugin.storage().saveAsync(data);
        return true;
    }
}
