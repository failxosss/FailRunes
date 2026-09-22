package dev.failxos.failrunes.integration;

import org.bukkit.plugin.Plugin;

/** Optional integration point: bonus-drop events from MCMMO are routed into the common drop pipeline when installed. */
public final class McmmoHook {
    private final boolean enabled;
    public McmmoHook(Plugin host) { enabled = host.getServer().getPluginManager().isPluginEnabled("mcMMO"); }
    public boolean enabled() { return enabled; }
}
