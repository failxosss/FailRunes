package dev.failxos.failrunes.items;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/** All PersistentDataContainer keys used to identify FailRunes items. Nothing is stored in lore alone. */
public final class Keys {
    public final NamespacedKey runeId, runeLevel, runeSource, runeUuid, sig;
    public final NamespacedKey itemType; // TOME, UNIDENTIFIED, GEM, IDENTIFIER, WAND, INCINERATOR, DETECTOR, HIDDEN_TREASURE
    public final NamespacedKey tomeRarity, tomePool, gemAmount, wandUses, unidPool, unidMinLevel, unidMaxLevel;
    public final NamespacedKey appliedRunes; // serialized "id:level;id:level" on equipment
    public final NamespacedKey cooldownData;

    public Keys(Plugin p) {
        runeId = ns(p, "rune_id");
        runeLevel = ns(p, "rune_level");
        runeSource = ns(p, "rune_source");
        runeUuid = ns(p, "rune_uuid");
        sig = ns(p, "sig");
        itemType = ns(p, "item_type");
        tomeRarity = ns(p, "tome_rarity");
        tomePool = ns(p, "tome_pool");
        gemAmount = ns(p, "gem_amount");
        wandUses = ns(p, "wand_uses");
        unidPool = ns(p, "unid_pool");
        unidMinLevel = ns(p, "unid_min_level");
        unidMaxLevel = ns(p, "unid_max_level");
        appliedRunes = ns(p, "applied_runes");
        cooldownData = ns(p, "cooldown_data");
    }
    private static NamespacedKey ns(Plugin p, String k) { return new NamespacedKey(p, k); }
}
