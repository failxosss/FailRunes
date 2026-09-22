package dev.failxos.failrunes.config;

import java.util.Map;

/** Per-gamemode XP prices (survival, skyblock, factions, prison, custom...). */
public record GamemodeSettings(String id, Map<String, Long> tomeCost, long gemCost, long identifyCost,
                               long wandCost, double incinerateReturnPercent) {
    public long cost(String rarity) { return tomeCost.getOrDefault(rarity.toLowerCase(), 0L); }
}
