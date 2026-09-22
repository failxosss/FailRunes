package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.Rune;
import dev.failxos.failrunes.util.Text;
import org.bukkit.Material;

/** Requirement #7: shows possible rewards, chances, rune levels/rarity for loot-generating runes. */
public final class LootTableGui extends Gui {
    public LootTableGui(FailRunesPlugin plugin, Rune rune) {
        super(Text.mm("<dark_purple>✦ Loot Table"));
        var pool = plugin.pools().get(rune.pool());
        var entries = pool.table().table().entries();
        int slot = 0;
        for (var e : entries) {
            if (slot >= 45) break;
            var r = plugin.runes().get(e.value().runeId());
            if (r == null) continue;
            double chance = pool.table().table().chance(e);
            set(slot++, GuiItems.named(dev.failxos.failrunes.items.ItemFactory.fallbackMaterial(r.rarity()),
                    "<white>" + Text.escape(r.name()), "Chance: " + Text.pct(chance), "Levels: " + e.value().minLevel() + "-" + e.value().maxLevel()), null);
        }
        GuiItems.fillBorder(inventory);
        set(BACK_SLOT, GuiItems.back(), (p, e) -> { if (parent != null) parent.open(p); });
        set(CLOSE_SLOT, GuiItems.close(), (p, e) -> p.closeInventory());
    }
}
