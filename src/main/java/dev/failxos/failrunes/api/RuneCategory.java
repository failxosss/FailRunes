package dev.failxos.failrunes.api;

public enum RuneCategory {
    COMBAT, WEAPONS, ARMOR, MINING, FARMING, LOGGING, DIGGING, FISHING, MOVEMENT, UTILITY, ECONOMY, XP, LOOT,
    SPECIAL, SEASONAL, EVENT, BOSS, TOMB, DARKZONE, DOJO, CRATE;

    public static RuneCategory parse(String s, RuneCategory def) {
        if (s == null) return def;
        try { return valueOf(s.trim().toUpperCase(java.util.Locale.ROOT)); } catch (IllegalArgumentException e) { return def; }
    }
}
