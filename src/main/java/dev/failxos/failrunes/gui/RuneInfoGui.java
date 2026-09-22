package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.Rune;
import dev.failxos.failrunes.api.RuneLevel;
import dev.failxos.failrunes.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Full rune details incl. scaling progression across levels (requirement #18, #22, #57). */
public final class RuneInfoGui extends Gui {
    public RuneInfoGui(FailRunesPlugin plugin, Rune rune) {
        super(Text.mm("<dark_purple>✦ " + Text.escape(rune.name())));
        set(13, plugin.itemFactory().runeItem(rune, rune.maxLevel()), null);
        int slot = 19;
        for (int lvl = 1; lvl <= rune.maxLevel() && slot < 26; lvl++, slot++) {
            RuneLevel rl = rune.level(lvl);
            set(slot, GuiItems.named(Material.PAPER, "<white>Level " + dev.failxos.failrunes.util.Roman.of(lvl),
                    rl.chance() > 0 ? "Chance: " + Text.pct(rl.chance()) : "",
                    rl.cooldown() > 0 ? "Cooldown: " + Text.dec(rl.cooldown()) + "s" : ""), null);
        }
        if (!plugin.pools().get(rune.pool()).table().isEmpty() && rune.effects().stream().anyMatch(e -> e.type().equals("LOOT")))
            set(31, GuiItems.named(Material.CHEST, "<gold>View Loot Table"), (p, e) -> new LootTableGui(plugin, rune).parent(this).open(p));
        GuiItems.fillBorder(inventory);
        set(BACK_SLOT, GuiItems.back(), (p, e) -> { if (parent != null) parent.open(p); });
        set(CLOSE_SLOT, GuiItems.close(), (p, e) -> p.closeInventory());
    }
}
