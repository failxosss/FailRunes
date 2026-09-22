package dev.failxos.failrunes.integration;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.Plugin;

/** Optional: lets XP/gem/wand prices be configured in real economy currency instead of XP where desired. */
public final class VaultHook {
    private Economy econ;
    public VaultHook(Plugin host) {
        if (!host.getServer().getPluginManager().isPluginEnabled("Vault")) return;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) econ = rsp.getProvider();
    }
    public boolean enabled() { return econ != null; }
    public boolean has(OfflinePlayer p, double amount) { return econ != null && econ.has(p, amount); }
    public boolean withdraw(OfflinePlayer p, double amount) { return econ != null && econ.withdrawPlayer(p, amount).transactionSuccess(); }
    public void deposit(OfflinePlayer p, double amount) { if (econ != null) econ.depositPlayer(p, amount); }
}
