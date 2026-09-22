package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Generic yes/no confirmation used by Cleansing Wands, Incinerator and destructive admin actions. */
public final class ConfirmationGui extends Gui {
    public ConfirmationGui(FailRunesPlugin plugin, String question, Runnable onConfirm) {
        super(Text.mm("<red>✦ Confirm"));
        set(21, GuiItems.named(Material.LIME_WOOL, "<green>Confirm", question), (p, e) -> { onConfirm.run(); p.closeInventory(); });
        set(23, GuiItems.named(Material.RED_WOOL, "<red>Cancel"), (p, e) -> { if (parent != null) parent.open(p); else p.closeInventory(); });
        GuiItems.fillBorder(inventory);
    }
}
