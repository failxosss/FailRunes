package dev.failxos.failrunes.integration;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/** Thin wrapper: FailRunes still starts and works with fallback vanilla materials if ItemsAdder is absent. */
public final class ItemsAdderHook {
    private final boolean enabled;

    public ItemsAdderHook() { enabled = Bukkit.getPluginManager().isPluginEnabled("ItemsAdder"); }
    public boolean enabled() { return enabled; }

    /** @param id e.g. "failrunes:common_rune" — namespace:item as configured in items.yml */
    public ItemStack item(String id) {
        if (!enabled || id == null || id.isBlank()) return null;
        try {
            CustomStack cs = CustomStack.getInstance(id);
            return cs == null ? null : cs.getItemStack();
        } catch (Throwable t) { return null; }
    }
    public String idOf(ItemStack item) {
        if (!enabled || item == null) return null;
        try { CustomStack cs = CustomStack.byItemStack(item); return cs == null ? null : cs.getNamespacedID(); }
        catch (Throwable t) { return null; }
    }
}
