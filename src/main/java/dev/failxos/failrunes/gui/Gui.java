package dev.failxos.failrunes.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/** Base of every FailRunes GUI: a 54-slot inventory with clickable slots, back/close, and paging built in. */
public abstract class Gui {
    public interface ClickHandler { void onClick(Player player, InventoryClickEvent event); }

    protected final Inventory inventory;
    protected final Map<Integer, ClickHandler> handlers = new HashMap<>();
    protected Gui parent;
    public static final int SIZE = 54;
    public static final int BACK_SLOT = 45, CLOSE_SLOT = 49, PREV_SLOT = 48, NEXT_SLOT = 50, SEARCH_SLOT = 47;

    protected Gui(Component title) {
        this.inventory = org.bukkit.Bukkit.createInventory(new GuiHolder(this), SIZE, title);
    }
    public Inventory inventory() { return inventory; }
    public Gui parent(Gui p) { this.parent = p; return this; }
    public Gui parentGui() { return parent; }

    protected void set(int slot, ItemStack item, ClickHandler handler) {
        inventory.setItem(slot, item);
        if (handler != null) handlers.put(slot, handler); else handlers.remove(slot);
    }
    public void handle(Player player, InventoryClickEvent event) {
        ClickHandler h = handlers.get(event.getSlot());
        if (h != null) h.onClick(player, event);
    }
    public void open(Player player) { player.openInventory(inventory); }

    public static final class GuiHolder implements org.bukkit.inventory.InventoryHolder {
        private final Gui gui;
        public GuiHolder(Gui gui) { this.gui = gui; }
        public Gui gui() { return gui; }
        @Override public Inventory getInventory() { return gui.inventory(); }
    }
}
