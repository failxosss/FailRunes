package dev.failxos.failrunes.engine;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

/** Standardised internal tags (ORE, WOOD, CROP, DIGGABLE, FISH, MOB, PLAYER) used by every rune. */
public final class BlockTags {
    private final Map<String, Set<Material>> map = new HashMap<>();

    public void load(ConfigurationSection cfg) {
        map.clear();
        for (String t : List.of("ORE", "WOOD", "CROP", "DIGGABLE", "FISH")) map.put(t, EnumSet.noneOf(Material.class));
        for (Material m : Material.values()) {
            if (m.isLegacy()) continue;
            String n = m.name();
            if (n.endsWith("_ORE") || n.equals("ANCIENT_DEBRIS") || n.equals("GILDED_BLACKSTONE")) map.get("ORE").add(m);
            if (n.endsWith("_LOG") || n.endsWith("_WOOD") || n.endsWith("_STEM") || n.endsWith("_HYPHAE") || n.equals("BAMBOO_BLOCK")) map.get("WOOD").add(m);
        }
        add("CROP", "WHEAT", "CARROTS", "POTATOES", "BEETROOTS", "SUGAR_CANE", "PUMPKIN", "MELON", "COCOA", "NETHER_WART",
                "TORCHFLOWER_CROP", "PITCHER_CROP", "SWEET_BERRY_BUSH", "CACTUS", "BAMBOO", "KELP_PLANT", "KELP");
        add("DIGGABLE", "DIRT", "GRASS_BLOCK", "COARSE_DIRT", "ROOTED_DIRT", "PODZOL", "MYCELIUM", "SAND", "RED_SAND",
                "GRAVEL", "CLAY", "SOUL_SAND", "SOUL_SOIL", "MUD", "SNOW", "SNOW_BLOCK", "DIRT_PATH", "FARMLAND");
        add("FISH", "COD", "SALMON", "TROPICAL_FISH", "PUFFERFISH");
        if (cfg != null) for (String tag : cfg.getKeys(false)) {
            ConfigurationSection s = cfg.getConfigurationSection(tag);
            if (s == null) continue;
            Set<Material> set = map.computeIfAbsent(tag.toUpperCase(Locale.ROOT), k -> EnumSet.noneOf(Material.class));
            for (String a : s.getStringList("add")) { Material m = Material.matchMaterial(a); if (m != null) set.add(m); }
            for (String r : s.getStringList("remove")) { Material m = Material.matchMaterial(r); if (m != null) set.remove(m); }
        }
    }
    private void add(String tag, String... names) {
        for (String n : names) { Material m = Material.matchMaterial(n); if (m != null) map.get(tag).add(m); }
    }
    public Set<String> tagsOf(Material m) {
        Set<String> out = new HashSet<>(2);
        for (Map.Entry<String, Set<Material>> e : map.entrySet()) if (e.getValue().contains(m)) out.add(e.getKey());
        return out;
    }
    public boolean matches(Material m, Set<String> tags) {
        for (String t : tags) { Set<Material> s = map.get(t); if (s != null && s.contains(m)) return true; }
        return false;
    }
    public static String entityTag(Entity e) {
        if (e instanceof Player) return "PLAYER";
        return e instanceof LivingEntity ? "MOB" : null;
    }
}
