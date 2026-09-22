package dev.failxos.failrunes.engine;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Asks every protection plugin (WorldGuard, claims, ...) whether the player may break a block by firing a
 * BlockBreakEvent that has drops disabled. This is plugin-agnostic and respects region flags.
 */
public final class ProtectionHook {
    private boolean checking;

    public boolean isChecking() { return checking; }

    public boolean canBreak(Player p, Block b) {
        if (checking) return false;
        checking = true;
        try {
            BlockBreakEvent e = new BlockBreakEvent(b, p);
            e.setDropItems(false);
            e.setExpToDrop(0);
            Bukkit.getPluginManager().callEvent(e);
            return !e.isCancelled();
        } finally { checking = false; }
    }
}
