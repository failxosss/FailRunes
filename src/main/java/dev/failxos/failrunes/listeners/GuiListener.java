package dev.failxos.failrunes.listeners;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.gui.Gui;
import dev.failxos.failrunes.gui.IdentifierGui;
import dev.failxos.failrunes.gui.IncineratorGui;
import dev.failxos.failrunes.gui.RuneApplyGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

/**
 * Requirement #68: routes every click through the owning Gui and blocks the exploit surface (shift-click,
 * drag, double-click, hotbar swap) from moving items in/out of FailRunes GUIs except through defined input slots.
 */
public final class GuiListener implements Listener {
    private final FailRunesPlugin plugin;
    public GuiListener(FailRunesPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof Gui.GuiHolder holder)) return;
        Gui gui = holder.gui();
        boolean clickedTop = e.getClickedInventory() == e.getView().getTopInventory();
        int inputSlot = inputSlotOf(gui);

        if (clickedTop && e.getSlot() == inputSlot) {
            // allow placing/removing exactly one stack in the designated slot; still disallow shift-click siphoning stacks in
            if (e.getClick().isShiftClick() || e.getClick() == ClickType.DOUBLE_CLICK) e.setCancelled(true);
            return;
        }
        if (clickedTop && gui instanceof RuneApplyGui && isApplySlot(e.getSlot())) {
            if (e.getClick().isShiftClick() || e.getClick() == ClickType.DOUBLE_CLICK) e.setCancelled(true);
            return;
        }
        if (clickedTop) {
            e.setCancelled(true);
            if (e.getWhoClicked() instanceof Player p) gui.handle(p, e);
        } else if (e.getClick().isShiftClick() && e.getInventory().getHolder() instanceof Gui.GuiHolder) {
            e.setCancelled(true); // never let shift-click dump items from the player's own inventory into arbitrary slots
        }
    }
    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getInventory().getHolder() instanceof Gui.GuiHolder holder)) return;
        int top = e.getView().getTopInventory().getSize();
        for (int slot : e.getRawSlots()) {
            if (slot < top && slot != inputSlotOf(holder.gui()) && !isApplySlot(slot)) { e.setCancelled(true); return; }
        }
    }
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof Gui.GuiHolder holder)) return;
        // return any items left in designated input slots to the player, never silently delete them (requirement #73)
        if (e.getPlayer() instanceof Player p) {
            for (int slot : new int[]{IdentifierGui.inputSlot(), IncineratorGui.inputSlot(),
                    RuneApplyGui.targetSlot(), RuneApplyGui.runeSlot(), RuneApplyGui.gemSlot()}) {
                var item = e.getInventory().getItem(slot);
                if (item != null && !item.getType().isAir() && !isPlaceholder(item)) {
                    var leftover = p.getInventory().addItem(item);
                    leftover.values().forEach(l -> p.getWorld().dropItemNaturally(p.getLocation(), l));
                }
            }
        }
    }
    private boolean isPlaceholder(org.bukkit.inventory.ItemStack it) {
        return it.getType().name().endsWith("_STAINED_GLASS_PANE") || it.getType() == org.bukkit.Material.PRISMARINE_SHARD
                || it.getType() == org.bukkit.Material.BLAZE_POWDER;
    }
    private int inputSlotOf(Gui gui) {
        if (gui instanceof IdentifierGui) return IdentifierGui.inputSlot();
        if (gui instanceof IncineratorGui) return IncineratorGui.inputSlot();
        return -1;
    }
    private boolean isApplySlot(int slot) { return slot == RuneApplyGui.targetSlot() || slot == RuneApplyGui.runeSlot() || slot == RuneApplyGui.gemSlot(); }
}
