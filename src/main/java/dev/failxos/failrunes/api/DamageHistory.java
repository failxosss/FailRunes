package dev.failxos.failrunes.api;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Bounded, time-limited ring of recent damage events for one player (memory only). */
public final class DamageHistory {
    public record Entry(long time, String source, String attacker, String victim, String rune, String weapon,
                        double damage, boolean critical, boolean incoming) {}
    private final ArrayDeque<Entry> q = new ArrayDeque<>();

    public synchronized void add(Entry e, int max, long retentionMs) {
        q.addFirst(e);
        prune(max, retentionMs);
    }
    public synchronized void prune(int max, long retentionMs) {
        long cutoff = System.currentTimeMillis() - retentionMs;
        while (q.size() > max || (!q.isEmpty() && q.peekLast().time() < cutoff)) q.pollLast();
    }
    public synchronized List<Entry> entries() { return new ArrayList<>(q); }
    public synchronized boolean isEmpty() { return q.isEmpty(); }
    public synchronized long lastTime() { return q.isEmpty() ? 0 : q.peekFirst().time(); }
}
