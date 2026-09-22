package dev.failxos.failrunes.api;

public enum PoolType {
    COMMON, RARE, LEGENDARY, MYTHICAL, TOMB, DARKZONE, DOJO, CRATE_EXCLUSIVE, EVENT, SEASONAL, BOSS, SPECIAL, ZONE;

    public static PoolType parse(String s, PoolType def) {
        if (s == null) return def;
        try { return valueOf(s.trim().toUpperCase(java.util.Locale.ROOT)); } catch (IllegalArgumentException e) { return def; }
    }
}
