package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.api.Rune;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** A region of the world with its own rune rules (see zones.yml). */
public final class Zone {
    public final String id;
    public final Set<String> worlds = new HashSet<>();
    public final int priority;
    private final double[] min, max;
    public final Set<String> onlyRunes = new HashSet<>(), onlyPools = new HashSet<>(),
            disabledRunes = new HashSet<>(), disabledPools = new HashSet<>();
    public final double procMultiplier, cooldownMultiplier, damageMultiplier, lootMultiplier, fishMultiplier;
    public final boolean applyAllowed;
    public final List<String> detectorPools;

    public Zone(String id, ConfigurationSection s) {
        this.id = id;
        worlds.addAll(s.getStringList("worlds"));
        priority = s.getInt("priority", 0);
        min = s.isList("min") ? doubles(s.getDoubleList("min")) : null;
        max = s.isList("max") ? doubles(s.getDoubleList("max")) : null;
        onlyRunes.addAll(s.getStringList("enabled-runes"));
        onlyPools.addAll(s.getStringList("enabled-pools"));
        disabledRunes.addAll(s.getStringList("disabled-runes"));
        disabledPools.addAll(s.getStringList("disabled-pools"));
        procMultiplier = s.getDouble("proc-multiplier", 1.0);
        cooldownMultiplier = s.getDouble("cooldown-multiplier", 1.0);
        damageMultiplier = s.getDouble("damage-multiplier", 1.0);
        lootMultiplier = s.getDouble("loot-multiplier", 1.0);
        fishMultiplier = s.getDouble("fishing-proc-multiplier", 1.0);
        applyAllowed = s.getBoolean("allow-apply", true);
        detectorPools = s.getStringList("detector-pools");
    }
    private static double[] doubles(List<Double> l) { return new double[]{l.get(0), l.get(1), l.get(2)}; }

    public boolean contains(Location l) {
        if (l.getWorld() == null) return false;
        if (!worlds.isEmpty() && !worlds.contains("*") && !worlds.contains(l.getWorld().getName())) return false;
        if (min == null || max == null) return true;
        double x = l.getX(), y = l.getY(), z = l.getZ();
        return x >= Math.min(min[0], max[0]) && x <= Math.max(min[0], max[0])
                && y >= Math.min(min[1], max[1]) && y <= Math.max(min[1], max[1])
                && z >= Math.min(min[2], max[2]) && z <= Math.max(min[2], max[2]);
    }
    public boolean allows(Rune r) {
        if (disabledRunes.contains(r.id()) || disabledPools.contains(r.pool()) || r.disabledZones().contains(id)) return false;
        if (!onlyRunes.isEmpty() || !onlyPools.isEmpty()) return onlyRunes.contains(r.id()) || onlyPools.contains(r.pool());
        return true;
    }
}
