package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.Rune;
import dev.failxos.failrunes.api.RunePool;
import dev.failxos.failrunes.items.ItemFactory;
import dev.failxos.failrunes.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

/** Browses one pool's runes with pagination; tomes purchasable straight from here if the pool allows it. */
public final class PoolBrowseGui extends Gui {
    public PoolBrowseGui(FailRunesPlugin plugin, RunePool pool, int page) {
        super(Text.mm("<dark_purple>✦ " + Text.escape(pool.display)));
        List<Rune> runes = plugin.runes().byPool(pool.id);
        int perPage = 45;
        int from = page * perPage, to = Math.min(runes.size(), from + perPage);
        for (int i = from; i < to; i++) {
            Rune r = runes.get(i);
            int slot = i - from;
            set(slot, plugin.itemFactory().runeItem(r, r.maxLevel()), (p, e) -> new RuneInfoGui(plugin, r).parent(this).open(p));
        }
        if (pool.purchasable) {
            var gm = plugin.settings().gamemode(plugin.playerGamemode(null));
            long cost = gm.cost(pool.tier.name().toLowerCase());
            set(46, plugin.itemFactory().tome(pool.tier, pool.id, cost), (p, e) -> {
                if (!dev.failxos.failrunes.util.XpUtil.take(p, cost)) { p.sendMessage(plugin.lang().get("message.not-enough-xp")); return; }
                var item = plugin.itemFactory().tome(pool.tier, pool.id, cost);
                p.getInventory().addItem(item);
                p.sendMessage(plugin.lang().get("message.tome-purchased"));
            });
        }
        if (from > 0) set(PREV_SLOT, GuiItems.prevPage(), (p, e) -> new PoolBrowseGui(plugin, pool, page - 1).parent(parent).open(p));
        if (to < runes.size()) set(NEXT_SLOT, GuiItems.nextPage(), (p, e) -> new PoolBrowseGui(plugin, pool, page + 1).parent(parent).open(p));
        GuiItems.fillBorder(inventory);
        set(BACK_SLOT, GuiItems.back(), (p, e) -> { if (parent != null) parent.open(p); });
        set(CLOSE_SLOT, GuiItems.close(), (p, e) -> p.closeInventory());
    }
}
