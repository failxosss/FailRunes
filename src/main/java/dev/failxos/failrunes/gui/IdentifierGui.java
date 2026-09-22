package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.items.ItemFactory;
import dev.failxos.failrunes.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/** Requirement #9: place an unidentified rune, consume the identification cost, reveal name/level/type/chances. */
public final class IdentifierGui extends Gui {
    private static final int INPUT_SLOT = 22;

    public IdentifierGui(FailRunesPlugin plugin, Player viewer) {
        super(Text.mm("<dark_purple>✦ Rune Identifier"));
        set(INPUT_SLOT, GuiItems.named(Material.PRISMARINE_SHARD, "<yellow>Place an Unidentified Rune"), null);
        handlers.remove(INPUT_SLOT); // allow raw placement; handled in InventoryClickListener via GuiHolder check
        set(24, GuiItems.named(Material.ENDER_EYE, "<white>Identify"), (p, e) -> {
            var item = inventory.getItem(INPUT_SLOT);
            String type = plugin.itemFactory().itemType(item);
            if (item == null || !"UNIDENTIFIED".equals(type)) { p.sendMessage(plugin.lang().get("message.identifier-empty")); return; }
            var meta = item.getItemMeta();
            var pdc = meta.getPersistentDataContainer();
            String pool = pdc.get(plugin.keys().unidPool, org.bukkit.persistence.PersistentDataType.STRING);
            int min = pdc.getOrDefault(plugin.keys().unidMinLevel, org.bukkit.persistence.PersistentDataType.INTEGER, 1);
            int max = pdc.getOrDefault(plugin.keys().unidMaxLevel, org.bukkit.persistence.PersistentDataType.INTEGER, 1);
            var gm = plugin.settings().gamemode(plugin.settings().defaultGamemode());
            if (!dev.failxos.failrunes.util.XpUtil.take(p, gm.identifyCost())) { p.sendMessage(plugin.lang().get("message.not-enough-xp")); return; }
            var revealed = plugin.identify().identify(p, item, pool, min, max);
            if (revealed == null) { p.sendMessage(plugin.lang().get("message.identify-failed")); return; }
            inventory.setItem(INPUT_SLOT, null);
            p.getInventory().addItem(revealed);
            p.sendMessage(plugin.lang().get("message.identify-success"));
        });
        GuiItems.fillBorder(inventory);
        set(BACK_SLOT, GuiItems.back(), (p, e) -> { if (parent != null) parent.open(p); });
        set(CLOSE_SLOT, GuiItems.close(), (p, e) -> p.closeInventory());
    }
    public static int inputSlot() { return INPUT_SLOT; }
}
