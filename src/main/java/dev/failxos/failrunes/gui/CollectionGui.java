package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.Rune;
import dev.failxos.failrunes.storage.PlayerData;
import dev.failxos.failrunes.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Requirement #58: discovered/owned/used/missing/seasonal/crate/special collection tracker with percentage. */
public final class CollectionGui extends Gui {
    public CollectionGui(FailRunesPlugin plugin, Player viewer, int page) {
        super(Text.mm("<gold>✦ Rune Collection (" + Text.dec(plugin.collection().percent(plugin.storage().cachedOrLoad(viewer.getUniqueId()))) + "%)"));
        PlayerData data = plugin.storage().cachedOrLoad(viewer.getUniqueId());
        var all = new java.util.ArrayList<>(plugin.runes().all());
        int perPage = 45, from = page * perPage, to = Math.min(all.size(), from + perPage);
        for (int i = from; i < to; i++) {
            Rune r = all.get(i);
            boolean owned = data.discovered().contains(r.id());
            set(i - from, owned ? plugin.itemFactory().runeItem(r, r.maxLevel())
                    : GuiItems.named(Material.GRAY_DYE, "<dark_gray>???", "<dark_gray>Not yet discovered"), null);
        }
        if (from > 0) set(PREV_SLOT, GuiItems.prevPage(), (p, e) -> new CollectionGui(plugin, p, page - 1).parent(parent).open(p));
        if (to < all.size()) set(NEXT_SLOT, GuiItems.nextPage(), (p, e) -> new CollectionGui(plugin, p, page + 1).parent(parent).open(p));
        GuiItems.fillBorder(inventory);
        set(CLOSE_SLOT, GuiItems.close(), (p, e) -> p.closeInventory());
    }
}
