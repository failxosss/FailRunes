package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.util.Text;
import dev.failxos.failrunes.util.XpUtil;
import org.bukkit.entity.Player;

/** Requirement #13: purchase Lucky Gems with XP. */
public final class BuyGemsGui extends Gui {
    public BuyGemsGui(FailRunesPlugin plugin, Player viewer) {
        super(Text.mm("<green>✦ Lucky Gems"));
        var gm = plugin.settings().gamemode(plugin.settings().defaultGamemode());
        int[] amounts = {1, 8, 16, 32, 64};
        int[] slots = {20, 21, 22, 23, 24};
        for (int i = 0; i < amounts.length; i++) {
            int amt = amounts[i];
            long cost = gm.gemCost() * amt;
            set(slots[i], plugin.itemFactory().luckyGem(amt), (p, e) -> {
                if (!XpUtil.take(p, cost)) { p.sendMessage(plugin.lang().get("message.not-enough-xp")); return; }
                p.getInventory().addItem(plugin.itemFactory().luckyGem(amt));
                p.sendMessage(plugin.lang().get("message.gems-purchased", "amount", amt));
            });
        }
        GuiItems.fillBorder(inventory);
        set(BACK_SLOT, GuiItems.back(), (p, e) -> { if (parent != null) parent.open(p); });
        set(CLOSE_SLOT, GuiItems.close(), (p, e) -> p.closeInventory());
    }
}
