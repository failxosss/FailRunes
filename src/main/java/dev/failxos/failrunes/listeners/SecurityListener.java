package dev.failxos.failrunes.listeners;

import dev.failxos.failrunes.FailRunesPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;

/**
 * Requirement #68: FailRunes items are never trusted at face value - vanilla anvils, crafting tables and
 * grindstones can never combine, duplicate or launder rune data since it lives in a signed PDC field vanilla
 * recipes do not copy; this listener additionally blocks the specific known duplication vectors.
 */
public final class SecurityListener implements Listener {
    private final FailRunesPlugin plugin;
    public SecurityListener(FailRunesPlugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvil(PrepareAnvilEvent e) {
        var result = e.getResult();
        if (result == null) return;
        // never let an anvil rename/repair operation duplicate a FailRunes item's applied-runes tag onto a second item
        var left = e.getInventory().getItem(0);
        if (left != null && plugin.itemFactory().itemType(left) != null && !plugin.appliedRunes().read(left).isEmpty()
                && e.getInventory().getItem(1) != null && plugin.itemFactory().itemType(e.getInventory().getItem(1)) != null) {
            e.setResult(null);
        }
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onGrindstone(PrepareGrindstoneEvent e) {
        var item = e.getInventory().getItem(0);
        if (item != null && plugin.itemFactory().itemType(item) != null) e.setResult(null); // never allow enchant-stripping FailRunes items
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onCraft(PrepareItemCraftEvent e) {
        for (var it : e.getInventory().getMatrix())
            if (it != null && plugin.itemFactory().itemType(it) != null) { e.getInventory().setResult(null); return; }
    }
}
