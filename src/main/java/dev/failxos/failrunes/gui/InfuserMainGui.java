package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.PoolType;
import dev.failxos.failrunes.api.RunePool;
import dev.failxos.failrunes.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/** Requirement #3/#4: the main Infuser menu with every category, clearly marked purchasable/not/event/crate/zone/seasonal. */
public final class InfuserMainGui extends Gui {
    public InfuserMainGui(FailRunesPlugin plugin, Player viewer) {
        super(Text.mm("<dark_purple>✦ Infuser"));
        int[] slots = {10, 11, 12, 13, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31};
        int i = 0;
        for (RunePool pool : orderedPools(plugin)) {
            if (i >= slots.length) break;
            set(slots[i++], poolIcon(pool), (p, e) -> new PoolBrowseGui(plugin, pool, 0).parent(this).open(p));
        }
        set(37, GuiItems.named(Material.ENDER_EYE, "<white>Rune Identifier"), (p, e) -> new IdentifierGui(plugin, p).parent(this).open(p));
        set(38, GuiItems.named(Material.EMERALD, "<green>Lucky Gems"), (p, e) -> new BuyGemsGui(plugin, p).parent(this).open(p));
        set(39, GuiItems.named(Material.BOOK, "<white>Rune Information"), (p, e) -> new RuneSearchGui(plugin, "").parent(this).open(p));
        set(40, GuiItems.named(Material.BLAZE_POWDER, "<white>Rune Incinerator"), (p, e) -> new IncineratorGui(plugin, p).parent(this).open(p));
        set(41, GuiItems.named(Material.STICK, "<white>Cleansing Wands"), (p, e) -> new BuyWandsGui(plugin, p).parent(this).open(p));
        GuiItems.fillBorder(inventory);
        set(CLOSE_SLOT, GuiItems.close(), (p, e) -> p.closeInventory());
    }
    private java.util.List<RunePool> orderedPools(FailRunesPlugin plugin) {
        java.util.List<RunePool> visible = new java.util.ArrayList<>(plugin.pools().visible());
        visible.sort(java.util.Comparator.comparing(p -> p.type.ordinal()));
        return visible;
    }
    private org.bukkit.inventory.ItemStack poolIcon(RunePool pool) {
        Material m = switch (pool.type) {
            case COMMON -> Material.PAPER; case RARE -> Material.BOOK; case LEGENDARY -> Material.ENCHANTED_BOOK;
            case MYTHICAL -> Material.NETHER_STAR; case TOMB -> Material.CHISELED_SANDSTONE; case DARKZONE -> Material.NETHERITE_SCRAP;
            case DOJO -> Material.BAMBOO; case CRATE_EXCLUSIVE -> Material.CHEST; case EVENT -> Material.FIREWORK_ROCKET;
            case SEASONAL -> Material.SNOWBALL; case BOSS -> Material.DRAGON_HEAD; default -> Material.MAP;
        };
        String badge = !pool.visible ? null : pool.purchasable ? "<green>PURCHASABLE" : switch (pool.type) {
            case EVENT -> "<gold>EVENT ONLY"; case CRATE_EXCLUSIVE -> "<gold>CRATE ONLY"; case ZONE -> "<gold>ZONE ONLY";
            case SEASONAL -> "<aqua>SEASONAL"; default -> "<red>NOT PURCHASABLE";
        };
        return GuiItems.named(m, "<white>" + Text.escape(pool.display), badge == null ? "" : badge);
    }
}
