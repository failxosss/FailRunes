package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.FailRunesPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ZoneManager {
    private final FailRunesPlugin plugin;
    private volatile List<Zone> zones = List.of();
    private final Zone fallback;

    public ZoneManager(FailRunesPlugin plugin) {
        this.plugin = plugin;
        this.fallback = new Zone("overworld", new YamlConfiguration());
    }
    public void load(YamlConfiguration cfg) {
        List<Zone> list = new ArrayList<>();
        ConfigurationSection sec = cfg.getConfigurationSection("zones");
        if (sec != null) for (String id : sec.getKeys(false)) {
            ConfigurationSection z = sec.getConfigurationSection(id);
            if (z != null) list.add(new Zone(id, z));
        }
        list.sort(Comparator.comparingInt((Zone z) -> z.priority).reversed());
        zones = list;
    }
    public Zone zoneAt(Location l) {
        for (Zone z : zones) if (z.contains(l)) return z;
        return fallback;
    }
    public Zone get(String id) {
        for (Zone z : zones) if (z.id.equalsIgnoreCase(id)) return z;
        return null;
    }
    public List<Zone> all() { return zones; }
}
