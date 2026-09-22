package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Requirement #14: destroy an unwanted rune item for XP; input is any FailRunes RUNE item. */
public final class IncineratorGui extends Gui {
    private static final int INPUT_SLOT = 22;

    public IncineratorGui(FailRunesPlugin plugin, Player viewer) {
        super(Text.mm("<gold>✦ Rune Incinerator"));
        set(INPUT_SLOT, GuiItems.named(Material.BLAZE_POWDER, "<yellow>Place a Rune to Incinerate"), null);
        handlers.remove(INPUT_SLOT);
        set(24, GuiItems.named(Material.FIRE_CHARGE, "<white>Incinerate"), (p, e) -> {
            var item = inventory.getItem(INPUT_SLOT);
            if (item == null || !"RUNE".equals(plugin.itemFactory().itemType(item))) { p.sendMessage(plugin.lang().get("message.incinerator-empty")); return; }
            var pdc = item.getItemMeta().getPersistentDataContainer();
            String id = pdc.get(plugin.keys().runeId, org.bukkit.persistence.PersistentDataType.STRING);
            int lvl = pdc.getOrDefault(plugin.keys().runeLevel, org.bukkit.persistence.PersistentDataType.INTEGER, 1);
            var rune = plugin.runes().get(id);
            if (rune == null) return;
            new ConfirmationGui(plugin, "Incinerate " + rune.name() + "?", () -> {
                plugin.incinerator().incinerate(p, rune, lvl);
                inventory.setItem(INPUT_SLOT, null);
            }).parent(this).open(p);
        });
        GuiItems.fillBorder(inventory);
        set(BACK_SLOT, GuiItems.back(), (p, e) -> { if (parent != null) parent.open(p); });
        set(CLOSE_SLOT, GuiItems.close(), (p, e) -> p.closeInventory());
    }
    public static int inputSlot() { return INPUT_SLOT; }
}
