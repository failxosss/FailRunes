package dev.failxos.failrunes.items;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.Rune;
import dev.failxos.failrunes.api.RuneLevel;
import dev.failxos.failrunes.api.RuneRarity;
import dev.failxos.failrunes.integration.ItemsAdderHook;
import dev.failxos.failrunes.util.Roman;
import dev.failxos.failrunes.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Builds every FailRunes item: identified/unidentified runes, tomes, gems, tools. Icons come from ItemsAdder when configured. */
public final class ItemFactory {
    private final FailRunesPlugin plugin;
    private final Keys keys;
    private final AppliedRunes applied;

    public ItemFactory(FailRunesPlugin plugin) {
        this.plugin = plugin;
        this.keys = plugin.keys();
        this.applied = plugin.appliedRunes();
    }

    public static NamedTextColor color(RuneRarity r) {
        return switch (r) {
            case COMMON -> NamedTextColor.GRAY;
            case RARE -> NamedTextColor.AQUA;
            case LEGENDARY -> NamedTextColor.GOLD;
            case MYTHICAL -> NamedTextColor.LIGHT_PURPLE;
        };
    }
    public static Material fallbackMaterial(RuneRarity r) {
        return switch (r) {
            case COMMON -> Material.PAPER;
            case RARE -> Material.BOOK;
            case LEGENDARY -> Material.ENCHANTED_BOOK;
            case MYTHICAL -> Material.NETHER_STAR;
        };
    }

    /** A revealed rune item, e.g. "Thunder IV" ready to be applied or displayed in the Infuser. */
    public ItemStack runeItem(Rune rune, int level) {
        ItemsAdderHook ia = plugin.itemsAdder();
        ItemStack it = ia != null && ia.enabled() ? ia.item(rune.icon()) : null;
        if (it == null) it = new ItemStack(fallbackMaterial(rune.rarity()));
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Text.item("<!i><color:" + hex(rune.rarity()) + ">" + Text.escape(rune.name()) + " " + Roman.of(level) + "</color>"));
        List<Component> lore = new ArrayList<>();
        for (String line : Text.wrap(rune.description(), 40)) lore.add(Text.item("<!i><gray>" + Text.escape(line)));
        RuneLevel rl = rune.level(level);
        lore.add(Component.empty());
        lore.add(Text.item("<!i><gray>Rarity: <white>" + rune.rarity()));
        lore.add(Text.item("<!i><gray>Category: <white>" + rune.category()));
        if (rl.chance() > 0) lore.add(Text.item("<!i><gray>Chance: <white>" + Text.pct(rl.chance())));
        if (rl.cooldown() > 0) lore.add(Text.item("<!i><gray>Cooldown: <white>" + Text.dec(rl.cooldown()) + "s"));
        lore.add(Text.item("<!i><gray>Equipment: <white>" + String.join(", ", rune.equipment())));
        meta.lore(lore);
        meta.getPersistentDataContainer().set(keys.itemType, PersistentDataType.STRING, "RUNE");
        meta.getPersistentDataContainer().set(keys.runeId, PersistentDataType.STRING, rune.id());
        meta.getPersistentDataContainer().set(keys.runeLevel, PersistentDataType.INTEGER, level);
        meta.getPersistentDataContainer().set(keys.runeUuid, PersistentDataType.STRING, UUID.randomUUID().toString());
        it.setItemMeta(meta);
        return it;
    }

    /** Hides everything until Rune-Identified: no name, level, type or chances are shown. */
    public ItemStack unidentified(String pool, int minLevel, int maxLevel) {
        ItemsAdderHook ia = plugin.itemsAdder();
        ItemStack it = ia != null && ia.enabled() ? ia.item("failrunes:unidentified_rune") : null;
        if (it == null) it = new ItemStack(Material.PRISMARINE_SHARD);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Text.item("<!i><yellow>✦ " + plugin.lang().get("item.unidentified.name")));
        List<Component> lore = new ArrayList<>();
        for (String l : plugin.lang().list("item.unidentified.lore")) lore.add(Text.item("<!i><gray>" + l));
        meta.lore(lore);
        meta.getPersistentDataContainer().set(keys.itemType, PersistentDataType.STRING, "UNIDENTIFIED");
        meta.getPersistentDataContainer().set(keys.unidPool, PersistentDataType.STRING, pool);
        meta.getPersistentDataContainer().set(keys.unidMinLevel, PersistentDataType.INTEGER, minLevel);
        meta.getPersistentDataContainer().set(keys.unidMaxLevel, PersistentDataType.INTEGER, maxLevel);
        meta.getPersistentDataContainer().set(keys.runeUuid, PersistentDataType.STRING, UUID.randomUUID().toString());
        it.setItemMeta(meta);
        return it;
    }

    public ItemStack tome(RuneRarity rarity, String pool, long cost) {
        ItemsAdderHook ia = plugin.itemsAdder();
        ItemStack it = ia != null && ia.enabled() ? ia.item("failrunes:tome_" + rarity.name().toLowerCase()) : null;
        if (it == null) it = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Text.item("<!i><color:" + hex(rarity) + ">✦ " + rarity.name() + " RUNE TOME"));
        meta.lore(List.of(
                Component.empty(),
                Text.item("<!i><gray>Cost: <white>" + Text.num(cost) + " XP"),
                Component.empty(),
                Text.item("<!i><gray>Contains a random " + rarity.name().toLowerCase() + " rune.")
        ));
        meta.getPersistentDataContainer().set(keys.itemType, PersistentDataType.STRING, "TOME");
        meta.getPersistentDataContainer().set(keys.tomeRarity, PersistentDataType.STRING, rarity.name());
        meta.getPersistentDataContainer().set(keys.tomePool, PersistentDataType.STRING, pool);
        it.setItemMeta(meta);
        return it;
    }

    public ItemStack luckyGem(int amount) {
        ItemsAdderHook ia = plugin.itemsAdder();
        ItemStack it = ia != null && ia.enabled() ? ia.item("failrunes:lucky_gem") : null;
        if (it == null) it = new ItemStack(Material.EMERALD);
        it.setAmount(Math.max(1, Math.min(64, amount)));
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Text.item("<!i><green>✦ Lucky Gem"));
        meta.lore(List.of(Text.item("<!i><gray>Increases Success chance"), Text.item("<!i><gray>when applying a Rune.")));
        meta.getPersistentDataContainer().set(keys.itemType, PersistentDataType.STRING, "GEM");
        it.setItemMeta(meta);
        return it;
    }

    public ItemStack tool(String type, String name, Material fallback, int uses, List<String> loreKeys) {
        ItemsAdderHook ia = plugin.itemsAdder();
        ItemStack it = ia != null && ia.enabled() ? ia.item("failrunes:" + type.toLowerCase()) : null;
        if (it == null) it = new ItemStack(fallback);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Text.item("<!i><white>✦ " + name));
        List<Component> lore = new ArrayList<>();
        for (String k : loreKeys) lore.add(Text.item("<!i><gray>" + plugin.lang().get(k)));
        if (uses > 0) lore.add(Text.item("<!i><dark_gray>Uses: " + uses));
        meta.lore(lore);
        meta.getPersistentDataContainer().set(keys.itemType, PersistentDataType.STRING, type);
        if (uses > 0) meta.getPersistentDataContainer().set(keys.wandUses, PersistentDataType.INTEGER, uses);
        it.setItemMeta(meta);
        return it;
    }

    public String itemType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(keys.itemType, PersistentDataType.STRING);
    }
    private static String hex(RuneRarity r) {
        return switch (r) { case COMMON -> "gray"; case RARE -> "aqua"; case LEGENDARY -> "gold"; case MYTHICAL -> "light_purple"; };
    }
}
