package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.EffectSpec;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;

/** Equipment set bonuses (requirement #44): activate at N pieces, each threshold its own effect list. */
public final class SetBonusManager {
    public record Threshold(int pieces, List<EffectSpec> effects, String message) {}
    public record RuneSet(String id, String display, Set<String> pieceIds, List<Threshold> thresholds) {}

    private final FailRunesPlugin plugin;
    private final Map<String, RuneSet> sets = new LinkedHashMap<>();
    private NamespacedKey setPieceKey;

    public SetBonusManager(FailRunesPlugin plugin) { this.plugin = plugin; }

    public void load() {
        sets.clear();
        setPieceKey = new NamespacedKey(plugin, "set_piece");
        File f = new File(plugin.getDataFolder(), "sets.yml");
        if (!f.exists()) { plugin.saveResource("sets.yml", false); }
        if (!f.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        ConfigurationSection root = cfg.getConfigurationSection("sets");
        if (root == null) return;
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            List<Threshold> th = new ArrayList<>();
            ConfigurationSection bonuses = s.getConfigurationSection("bonuses");
            if (bonuses != null) for (String key : bonuses.getKeys(false)) {
                ConfigurationSection b = bonuses.getConfigurationSection(key);
                List<EffectSpec> effects = new ArrayList<>();
                for (Map<?, ?> m : (List<Map<?, ?>>) b.getMapList("effects")) effects.add(EffectSpec.parse(m));
                th.add(new Threshold(Integer.parseInt(key.replaceAll("\\D", "")), effects, b.getString("message", "")));
            }
            th.sort(Comparator.comparingInt(Threshold::pieces));
            sets.put(id, new RuneSet(id, s.getString("display", id), new HashSet<>(s.getStringList("pieces")), th));
        }
    }
    public String setOf(ItemStack it) {
        if (it == null || !it.hasItemMeta()) return null;
        return it.getItemMeta().getPersistentDataContainer().get(setPieceKey, PersistentDataType.STRING);
    }
    public void markPiece(ItemStack it, String setId) {
        ItemMeta meta = it.getItemMeta();
        meta.getPersistentDataContainer().set(setPieceKey, PersistentDataType.STRING, setId);
        it.setItemMeta(meta);
    }
    /** Counts equipped armor pieces per set id (used for both display and RuneSnapshot). */
    public Map<String, Integer> equippedCounts(Player p) {
        Map<String, Integer> out = new HashMap<>();
        for (ItemStack it : new ItemStack[]{p.getInventory().getHelmet(), p.getInventory().getChestplate(),
                p.getInventory().getLeggings(), p.getInventory().getBoots()}) {
            String id = setOf(it);
            if (id != null) out.merge(id, 1, Integer::sum);
        }
        return out;
    }
    public RuneSet get(String id) { return sets.get(id); }
    public Collection<RuneSet> all() { return sets.values(); }
    public Threshold activeThreshold(RuneSet set, int pieces) {
        Threshold best = null;
        for (Threshold t : set.thresholds()) if (t.pieces() <= pieces) best = t;
        return best;
    }
}
