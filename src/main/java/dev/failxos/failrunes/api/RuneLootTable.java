package dev.failxos.failrunes.api;

import dev.failxos.failrunes.core.WeightedTable;

import java.util.Random;

/** Weighted table of runes with a level range per entry. */
public final class RuneLootTable {
    public record Entry(String runeId, int minLevel, int maxLevel) {}
    private final WeightedTable<Entry> table = new WeightedTable<>();
    public RuneLootTable add(String runeId, double weight, int min, int max) { table.add(new Entry(runeId, min, max), weight); return this; }
    public Entry roll(Random r) { return table.roll(r); }
    public WeightedTable<Entry> table() { return table; }
    public boolean isEmpty() { return table.isEmpty(); }
}
