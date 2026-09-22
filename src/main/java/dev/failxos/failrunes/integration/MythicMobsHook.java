package dev.failxos.failrunes.integration;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/** Optional integration: identifies MythicMobs bosses so boss-drop pools can target them by internal name. */
public final class MythicMobsHook {
    private final boolean enabled;
    public MythicMobsHook(Plugin host) { enabled = host.getServer().getPluginManager().isPluginEnabled("MythicMobs"); }
    public boolean enabled() { return enabled; }

    public String mythicTypeOf(Entity e) {
        if (!enabled) return null;
        try {
            var mgr = io.lumine.mythic.bukkit.MythicBukkit.inst().getMobManager();
            var am = mgr.getMythicMobInstance(e);
            return am == null ? null : am.getMobType();
        } catch (Throwable t) { return null; }
    }
}
