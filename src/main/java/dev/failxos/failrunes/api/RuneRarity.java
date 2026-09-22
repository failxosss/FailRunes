package dev.failxos.failrunes.api;

/** Core purchasable tiers. Special pools (tomb, dojo, ...) are described by {@link PoolType}. */
public enum RuneRarity {
    COMMON, RARE, LEGENDARY, MYTHICAL;

    public static RuneRarity parse(String s, RuneRarity def) {
        if (s == null) return def;
        try { return valueOf(s.trim().toUpperCase(java.util.Locale.ROOT)); } catch (IllegalArgumentException e) { return def; }
    }
    public int tier() { return ordinal() + 1; }
}
