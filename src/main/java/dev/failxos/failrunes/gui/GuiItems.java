package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/** Reusable decorative/navigation items shared by every GUI screen. */
public final class GuiItems {
    private GuiItems() {}

    public static ItemStack named(Material mat, String name, String... lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Text.item("<!i>" + name));
        if (lore.length > 0) {
            java.util.List<Component> l = new java.util.ArrayList<>();
            for (String s : lore) l.add(Text.item("<!i><gray>" + s));
            meta.lore(l);
        }
        it.setItemMeta(meta);
        return it;
    }
    public static ItemStack filler() { ItemStack it = named(Material.GRAY_STAINED_GLASS_PANE, " "); return it; }
    public static ItemStack back() { return named(Material.ARROW, "<yellow>← Back"); }
    public static ItemStack close() { return named(Material.BARRIER, "<red>Close"); }
    public static ItemStack prevPage() { return named(Material.SPECTRAL_ARROW, "<yellow>← Previous Page"); }
    public static ItemStack nextPage() { return named(Material.SPECTRAL_ARROW, "<yellow>Next Page →"); }
    public static ItemStack search() { return named(Material.OAK_SIGN, "<white>Search", "Click to search by name."); }
    public static ItemStack badge(boolean purchasable, String extra) {
        String label = extra != null ? extra : (purchasable ? "<green>PURCHASABLE" : "<red>NOT PURCHASABLE");
        return named(purchasable ? Material.LIME_DYE : Material.GRAY_DYE, label);
    }
    public static void fillBorder(org.bukkit.inventory.Inventory inv) {
        ItemStack f = filler();
        for (int i = 0; i < 9; i++) if (inv.getItem(i) == null) inv.setItem(i, f);
        for (int i = 45; i < 54; i++) if (inv.getItem(i) == null) inv.setItem(i, f);
    }
}
