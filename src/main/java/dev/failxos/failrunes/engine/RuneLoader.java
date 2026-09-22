package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

/** Parses runes.yml into {@link Rune} objects and registers them with source "config" so a reload can replace them cleanly. */
public final class RuneLoader {
    private final FailRunesPlugin plugin;
    public RuneLoader(FailRunesPlugin plugin) { this.plugin = plugin; }

    public int load() {
        RuneManager.instance().clearConfigured();
        int count = 0;
        File dir = new File(plugin.getDataFolder(), "runes");
        if (!dir.exists()) dir.mkdirs();
        File[] files = dir.listFiles((d, n) -> n.endsWith(".yml"));
        List<File> list = new ArrayList<>();
        File single = new File(plugin.getDataFolder(), "runes.yml");
        if (single.exists()) list.add(single);
        if (files != null) list.addAll(Arrays.asList(files));
        for (File f : list) count += loadFile(f);
        return count;
    }
    private int loadFile(File f) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        ConfigurationSection root = cfg.getConfigurationSection("runes");
        if (root == null) return 0;
        int count = 0;
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            try { RuneManager.register(parse(id, s)); count++; }
            catch (Exception e) { plugin.getLogger().warning("[FailRunes] Failed to parse rune '" + id + "' in " + f.getName() + ": " + e.getMessage()); }
        }
        return count;
    }
    private Rune parse(String id, ConfigurationSection s) {
        Rune.Builder b = Rune.builder(id)
                .name(s.getString("name", id))
                .description(s.getString("description", ""))
                .rarity(RuneRarity.parse(s.getString("rarity"), RuneRarity.COMMON))
                .category(RuneCategory.parse(s.getString("category"), RuneCategory.SPECIAL))
                .pool(s.getString("pool", s.getString("rarity", "common").toLowerCase()))
                .maxLevel(s.getInt("max-level", 5))
                .overmaxLevel(s.getInt("overmax-level", 0))
                .weight(s.getDouble("weight", 10))
                .trigger(RuneTrigger.parse(s.getString("trigger"), RuneTrigger.ON_HIT))
                .chance(Scaling.parse(s.get("chance"), Scaling.of(10)))
                .cooldown(Scaling.parse(s.get("cooldown"), Scaling.of(0)))
                .conflicts(s.getStringList("conflicts"))
                .blockTags(s.getStringList("block-tags"))
                .disabledZones(s.getStringList("disabled-zones"))
                .sound(s.getString("sound", "entity.experience_orb.pickup"))
                .particle(s.getString("particle", "CRIT"))
                .icon(s.getString("icon", ""))
                .symbol(s.getString("symbol", "✦"))
                .messageDefault(s.getBoolean("message-default", true))
                .original(s.getBoolean("original", false))
                .source("config");
        List<?> equip = s.getList("equipment");
        if (equip != null) for (Object o : equip) b.equipment(String.valueOf(o).toUpperCase(Locale.ROOT));
        if (s.contains("critical-fail.destroy-item")) b.criticalFailDestroy(s.getBoolean("critical-fail.destroy-item"));
        if (s.contains("incinerate-value")) b.incinerateValue(s.getDouble("incinerate-value"));
        for (String cond : s.getStringList("conditions")) {
            b.conditionString(cond);
            b.condition(ConditionParser.parse(cond, (key, raw) -> plugin.getLogger().warning("[FailRunes] Unknown condition '" + key + "' on rune " + id)));
        }
        List<Map<?, ?>> effects = s.getMapList("effects");
        for (Map<?, ?> m : effects) b.effect(EffectSpec.parse(m));
        return b.build();
    }
}
