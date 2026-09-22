package dev.failxos.failrunes.items;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.LinkedHashMap;
import java.util.Map;

/** Reads/writes the "id:level;id:level" rune list stored in an item's PDC. Never trusts item lore. */
public final class AppliedRunes {
    private final Keys keys;
    public AppliedRunes(Keys keys) { this.keys = keys; }

    public Map<String, Integer> read(ItemStack item) {
        Map<String, Integer> out = new LinkedHashMap<>();
        if (item == null || !item.hasItemMeta()) return out;
        ItemMeta meta = item.getItemMeta();
        String raw = meta.getPersistentDataContainer().get(keys.appliedRunes, PersistentDataType.STRING);
        if (raw == null || raw.isBlank()) return out;
        for (String part : raw.split(";")) {
            String[] kv = part.split(":");
            if (kv.length == 2) try { out.put(kv[0], Integer.parseInt(kv[1])); } catch (NumberFormatException ignored) {}
        }
        return out;
    }
    public void write(ItemStack item, Map<String, Integer> runes) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : runes.entrySet()) { if (sb.length() > 0) sb.append(';'); sb.append(e.getKey()).append(':').append(e.getValue()); }
        meta.getPersistentDataContainer().set(keys.appliedRunes, PersistentDataType.STRING, sb.toString());
        item.setItemMeta(meta);
    }
    public void put(ItemStack item, String runeId, int level) {
        Map<String, Integer> m = read(item); m.put(runeId, level); write(item, m);
    }
    public void remove(ItemStack item, String runeId) {
        Map<String, Integer> m = read(item); m.remove(runeId); write(item, m);
    }
}
