package dev.failxos.failrunes.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class WeightedTable<T> {
    public record Entry<T>(T value, double weight) {}
    private final List<Entry<T>> entries = new ArrayList<>();
    private double total;

    public WeightedTable<T> add(T value, double weight) {
        if (weight > 0) { entries.add(new Entry<>(value, weight)); total += weight; }
        return this;
    }
    public T roll(Random r) {
        if (entries.isEmpty()) return null;
        double x = r.nextDouble() * total;
        for (Entry<T> e : entries) { x -= e.weight(); if (x < 0) return e.value(); }
        return entries.get(entries.size() - 1).value();
    }
    public List<Entry<T>> entries() { return Collections.unmodifiableList(entries); }
    public double total() { return total; }
    public boolean isEmpty() { return entries.isEmpty(); }
    /** Chance in percent for one entry. */
    public double chance(Entry<T> e) { return total <= 0 ? 0 : e.weight() / total * 100.0; }
}
