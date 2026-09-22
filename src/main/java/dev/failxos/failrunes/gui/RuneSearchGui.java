package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.Rune;
import dev.failxos.failrunes.util.Text;

import java.util.List;

/** Requirement #57: search runes by name/category/rarity/equipment/effect/level. */
public final class RuneSearchGui extends Gui {
    public RuneSearchGui(FailRunesPlugin plugin, String query) {
        super(Text.mm("<dark_purple>✦ Search: " + Text.escape(query.isEmpty() ? "(all)" : query)));
        List<Rune> results = plugin.runes().search(query);
        int slot = 0;
        for (Rune r : results) {
            if (slot >= 45) break;
            set(slot++, plugin.itemFactory().runeItem(r, r.maxLevel()), (p, e) -> new RuneInfoGui(plugin, r).parent(this).open(p));
        }
        set(SEARCH_SLOT, GuiItems.search(), (p, e) -> {
            p.closeInventory();
            p.sendMessage(plugin.lang().get("message.search-prompt"));
            plugin.chatInput().awaitLine(p, line -> new RuneSearchGui(plugin, line).open(p));
        });
        GuiItems.fillBorder(inventory);
        set(BACK_SLOT, GuiItems.back(), (p, e) -> { if (parent != null) parent.open(p); });
        set(CLOSE_SLOT, GuiItems.close(), (p, e) -> p.closeInventory());
    }
}
