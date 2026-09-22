package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.items.AppliedRunes;
import dev.failxos.failrunes.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Requirement #23: /runeinfo - all runes currently installed on the held item. */
public final class RuneInfoItemGui extends Gui {
    public RuneInfoItemGui(FailRunesPlugin plugin, Player viewer, ItemStack item) {
        super(Text.mm("<white>✦ " + (item == null ? "No Item" : item.getType().name())));
        if (item == null || item.getType().isAir()) { set(22, GuiItems.named(Material.BARRIER, "<gray>Hold an item first"), null); return; }
        AppliedRunes ar = plugin.appliedRunes();
        int slot = 10;
        for (var e : ar.read(item).entrySet()) {
            var rune = plugin.runes().get(e.getKey());
            if (rune == null || slot >= 44) continue;
            set(slot++, plugin.itemFactory().runeItem(rune, e.getValue()), (p, ev) -> new RuneInfoGui(plugin, rune).parent(this).open(p));
        }
        if (ar.read(item).isEmpty()) set(22, GuiItems.named(Material.BARRIER, "<gray>No Runes on this item"), null);
        GuiItems.fillBorder(inventory);
        set(CLOSE_SLOT, GuiItems.close(), (p, e) -> p.closeInventory());
    }
}
