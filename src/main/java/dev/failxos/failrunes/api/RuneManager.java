package dev.failxos.failrunes.api;

import java.util.*;
import java.util.stream.Collectors;

/** Rune registry. Other plugins call {@code RuneManager.register(rune)}. */
public final class RuneManager {
    private static RuneManager instance;
    private final Map<String, Rune> runes = new LinkedHashMap<>();
    private final Map<String, List<Rune>> byPool = new HashMap<>();

    public RuneManager() { instance = this; }
    public static RuneManager instance() { return instance; }

    /** Register (or replace) a rune. IDs are case-insensitive and stable. */
    public static void register(Rune rune) { instance.add(rune); }
    public static void unregister(String id) { instance.remove(id); }

    public synchronized void add(Rune r) {
        Rune old = runes.put(r.id(), r);
        if (old != null) byPool.getOrDefault(old.pool(), new ArrayList<>()).remove(old);
        byPool.computeIfAbsent(r.pool(), k -> new ArrayList<>()).add(r);
    }
    public synchronized void remove(String id) {
        Rune old = runes.remove(id.toLowerCase(Locale.ROOT));
        if (old != null) byPool.getOrDefault(old.pool(), new ArrayList<>()).remove(old);
    }
    /** Drops everything that was loaded from YAML; runes registered through the API survive a reload. */
    public synchronized void clearConfigured() {
        new ArrayList<>(runes.values()).stream().filter(r -> r.source().equals("config")).forEach(r -> remove(r.id()));
    }
    public Rune get(String id) { return id == null ? null : runes.get(id.toLowerCase(Locale.ROOT)); }
    public synchronized Collection<Rune> all() { return new ArrayList<>(runes.values()); }
    public int size() { return runes.size(); }
    public synchronized List<Rune> byPool(String pool) { return new ArrayList<>(byPool.getOrDefault(pool, List.of())); }

    /** Search by name, id, category, rarity, equipment, effect type or level ("lvl:5"). */
    public List<Rune> search(String query) {
        String q = query.toLowerCase(Locale.ROOT).trim();
        if (q.isEmpty()) return new ArrayList<>(all());
        return all().stream().filter(r -> matches(r, q)).collect(Collectors.toList());
    }
    private boolean matches(Rune r, String q) {
        if (q.startsWith("lvl:") || q.startsWith("level:")) {
            try { return r.maxLevel() >= Integer.parseInt(q.substring(q.indexOf(':') + 1).trim()); } catch (NumberFormatException e) { return false; }
        }
        return r.id().contains(q) || r.name().toLowerCase(Locale.ROOT).contains(q)
                || r.category().name().toLowerCase(Locale.ROOT).contains(q)
                || r.rarity().name().toLowerCase(Locale.ROOT).contains(q)
                || r.pool().contains(q)
                || r.equipment().stream().anyMatch(e -> e.toLowerCase(Locale.ROOT).contains(q))
                || r.effects().stream().anyMatch(e -> e.type().toLowerCase(Locale.ROOT).contains(q));
    }
}
