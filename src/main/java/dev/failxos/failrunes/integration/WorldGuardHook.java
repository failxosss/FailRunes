package dev.failxos.failrunes.integration;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** Optional: block-changing rune effects respect WorldGuard's BUILD flag / region protection when present. */
public final class WorldGuardHook {
    private final boolean enabled;
    public WorldGuardHook(Plugin host) {
        enabled = host.getServer().getPluginManager().isPluginEnabled("WorldGuard");
    }
    public boolean enabled() { return enabled; }

    public boolean canBuild(Player player, Location loc) {
        if (!enabled) return true;
        try {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            LocalPlayer local = WorldGuardPlugin.inst().wrapPlayer(player);
            return query.testBuild(BukkitAdapter.adapt(loc), local);
        } catch (Throwable t) { return true; }
    }
}
