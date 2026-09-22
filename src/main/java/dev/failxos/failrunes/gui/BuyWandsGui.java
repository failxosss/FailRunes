package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.util.Text;
import dev.failxos.failrunes.util.XpUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

/** Requirement #15: purchase Cleansing Wands with XP. */
public final class BuyWandsGui extends Gui {
    public BuyWandsGui(FailRunesPlugin plugin, Player viewer) {
        super(Text.mm("<white>✦ Cleansing Wands"));
        var gm = plugin.settings().gamemode(plugin.settings().defaultGamemode());
        set(22, plugin.itemFactory().tool("WAND", "Cleansing Wand", Material.STICK, 1, List.of("item.wand.lore")),
                (p, e) -> {
                    if (!XpUtil.take(p, gm.wandCost())) { p.sendMessage(plugin.lang().get("message.not-enough-xp")); return; }
                    p.getInventory().addItem(plugin.itemFactory().tool("WAND", "Cleansing Wand", Material.STICK, 1, List.of("item.wand.lore")));
                    p.sendMessage(plugin.lang().get("message.wand-purchased"));
                });
        GuiItems.fillBorder(inventory);
        set(BACK_SLOT, GuiItems.back(), (p, e) -> { if (parent != null) parent.open(p); });
        set(CLOSE_SLOT, GuiItems.close(), (p, e) -> p.closeInventory());
    }
}
