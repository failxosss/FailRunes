package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.PoolType;
import dev.failxos.failrunes.api.RuneLootTable;
import dev.failxos.failrunes.api.RunePool;
import dev.failxos.failrunes.api.RuneRarity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/** Loads pools.yml. Each pool independently controls visibility, purchasability, source and drop method (#2, #37-43). */
public final class PoolManager {
    private final FailRunesPlugin plugin;
    private final Map<String, RunePool> pools = new LinkedHashMap<>();

    public PoolManager(FailRunesPlugin plugin) { this.plugin = plugin; }

    public void load() {
        pools.clear();
        File f = new File(plugin.getDataFolder(), "pools.yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        ConfigurationSection root = cfg.getConfigurationSection("pools");
        for (RuneRarity r : RuneRarity.values())
            pools.put(r.name().toLowerCase(), new RunePool(r.name().toLowerCase(), PoolType.valueOf(r.name()),
                    cap(r.name()), true, true, "TOME", "purchase", 0, r));
        if (root != null) for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            long endsAt = parseDate(s.getString("ends-at", null));
            RunePool p = new RunePool(id, PoolType.parse(s.getString("type"), PoolType.SPECIAL), s.getString("display", id),
                    s.getBoolean("visible", true), s.getBoolean("purchasable", false), s.getString("drop-method", "NONE"),
                    s.getString("source", "config"), endsAt, RuneRarity.parse(s.getString("tier"), RuneRarity.COMMON));
            pools.put(id, p);
        }
    }
    public void buildLoot() {
        for (RunePool p : pools.values()) {
            RuneLootTable table = new RuneLootTable();
            for (var r : plugin.runes().byPool(p.id)) table.add(r.id(), r.weight(), 1, r.maxLevel());
            p.table(table);
        }
    }
    private String cap(String s) { return s.substring(0, 1) + s.substring(1).toLowerCase(); }
    private long parseDate(String s) {
        if (s == null) return 0;
        try { return new SimpleDateFormat("yyyy-MM-dd").parse(s).getTime(); } catch (Exception e) { return 0; }
    }
    public RunePool get(String id) { return id == null ? null : pools.get(id.toLowerCase(Locale.ROOT)); }
    public Collection<RunePool> all() { return pools.values(); }
    public List<RunePool> visible() { return pools.values().stream().filter(p -> p.visible).toList(); }
}
