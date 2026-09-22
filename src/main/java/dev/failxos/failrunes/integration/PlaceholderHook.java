package dev.failxos.failrunes.integration;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.RuneRarity;
import dev.failxos.failrunes.storage.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * %failrunes_runes% %failrunes_rune_count% %failrunes_common|rare|legendary|mythical%
 * %failrunes_lucky_gems% %failrunes_detector_level% %failrunes_successful_applications%
 * %failrunes_failed_applications% %failrunes_critical_failures% %failrunes_collection_percent%
 */
public final class PlaceholderHook extends PlaceholderExpansion {
    private final FailRunesPlugin plugin;
    public PlaceholderHook(FailRunesPlugin plugin) { this.plugin = plugin; }

    @Override public @NotNull String getIdentifier() { return "failrunes"; }
    @Override public @NotNull String getAuthor() { return "Failxos"; }
    @Override public @NotNull String getVersion() { return plugin.getPluginMeta().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override public String onRequest(OfflinePlayer p, @NotNull String params) {
        if (p == null || p.getUniqueId() == null) return "";
        PlayerData d = plugin.storage().cached(p.getUniqueId());
        if (d == null) return "";
        return switch (params.toLowerCase()) {
            case "runes", "rune_count" -> String.valueOf(d.discovered().size());
            case "common" -> String.valueOf(count(d, RuneRarity.COMMON));
            case "rare" -> String.valueOf(count(d, RuneRarity.RARE));
            case "legendary" -> String.valueOf(count(d, RuneRarity.LEGENDARY));
            case "mythical" -> String.valueOf(count(d, RuneRarity.MYTHICAL));
            case "lucky_gems" -> String.valueOf(d.luckyGems());
            case "detector_level" -> String.valueOf(d.detectorLevel());
            case "successful_applications" -> String.valueOf(d.stats().success());
            case "failed_applications" -> String.valueOf(d.stats().fail());
            case "critical_failures" -> String.valueOf(d.stats().critical());
            case "collection_percent" -> String.format("%.1f", plugin.collection().percent(d));
            default -> null;
        };
    }
    private long count(PlayerData d, RuneRarity r) {
        return d.discovered().stream().map(plugin.runes()::get).filter(x -> x != null && x.rarity() == r).count();
    }
}
