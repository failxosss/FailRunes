package dev.failxos.failrunes.api;

/** A pool is independent: it decides its own visibility, availability, source and purchasability. */
public final class RunePool {
    public final String id;
    public final PoolType type;
    public final String display;
    public final boolean visible;
    public final boolean purchasable;
    /** TOME, CRATE, BOSS, EVENT, ZONE, SEASONAL, DETECTOR, NONE */
    public final String dropMethod;
    public final String source;
    /** Epoch millis after which the pool stops handing out new runes (0 = never). */
    public final long endsAt;
    public final RuneRarity tier;
    private RuneLootTable table = new RuneLootTable();

    public RunePool(String id, PoolType type, String display, boolean visible, boolean purchasable,
                    String dropMethod, String source, long endsAt, RuneRarity tier) {
        this.id = id; this.type = type; this.display = display; this.visible = visible; this.purchasable = purchasable;
        this.dropMethod = dropMethod; this.source = source; this.endsAt = endsAt; this.tier = tier;
    }
    public RuneLootTable table() { return table; }
    public void table(RuneLootTable t) { table = t; }
    public boolean expired(long now) { return endsAt > 0 && now > endsAt; }
}
