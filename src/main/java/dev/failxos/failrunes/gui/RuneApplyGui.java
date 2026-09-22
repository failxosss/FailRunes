package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.core.Chances;
import dev.failxos.failrunes.engine.ApplicationService;
import dev.failxos.failrunes.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Requirement #11-#13: drag a Rune onto an item, apply Lucky Gems, watch Success/Fail/Critical live-update.
 * Slots: 11 = target item, 13 = rune, 15 = gems (stack), 22 = apply button, 24 = live chance display.
 */
public final class RuneApplyGui extends Gui {
    private static final int TARGET_SLOT = 11, RUNE_SLOT = 13, GEM_SLOT = 15, APPLY_SLOT = 31, CHANCE_SLOT = 22;

    public RuneApplyGui(FailRunesPlugin plugin, Player viewer) {
        super(Text.mm("<dark_purple>✦ Apply Rune"));
        set(TARGET_SLOT, GuiItems.named(Material.LIME_STAINED_GLASS_PANE, "<green>Place Equipment"), null);
        set(RUNE_SLOT, GuiItems.named(Material.YELLOW_STAINED_GLASS_PANE, "<yellow>Place Rune"), null);
        set(GEM_SLOT, GuiItems.named(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "<aqua>Place Lucky Gems (optional)"), null);
        handlers.remove(TARGET_SLOT); handlers.remove(RUNE_SLOT); handlers.remove(GEM_SLOT);
        refreshChance(plugin, null, null, 0);
        set(APPLY_SLOT, GuiItems.named(Material.ANVIL, "<white>Apply"), (p, e) -> {
            ItemStack target = inventory.getItem(TARGET_SLOT), runeItem = inventory.getItem(RUNE_SLOT), gemItem = inventory.getItem(GEM_SLOT);
            if (target == null || target.getType().isAir() || runeItem == null || !"RUNE".equals(plugin.itemFactory().itemType(runeItem))) {
                p.sendMessage(plugin.lang().get("message.apply-missing-items")); return;
            }
            var pdc = runeItem.getItemMeta().getPersistentDataContainer();
            String id = pdc.get(plugin.keys().runeId, org.bukkit.persistence.PersistentDataType.STRING);
            int level = pdc.getOrDefault(plugin.keys().runeLevel, org.bukkit.persistence.PersistentDataType.INTEGER, 1);
            var rune = plugin.runes().get(id);
            if (rune == null) return;
            ApplicationService.Outcome outcome = plugin.applications().validate(p, target, rune, level);
            if (outcome != ApplicationService.Outcome.OK) { p.sendMessage(plugin.lang().get("message.apply-invalid." + outcome.name().toLowerCase())); return; }
            int gems = gemItem == null ? 0 : Math.min(gemItem.getAmount(), plugin.settings().gemsMaxApply());
            var result = plugin.applications().apply(p, target, rune, level, gems);
            if (result == null) return;
            inventory.setItem(RUNE_SLOT, dec(runeItem));
            if (gems > 0) inventory.setItem(GEM_SLOT, dec(gemItem, gems));
            refreshChance(plugin, target, rune, level);
        });
        GuiItems.fillBorder(inventory);
        set(BACK_SLOT, GuiItems.back(), (p, e) -> { if (parent != null) parent.open(p); });
        set(CLOSE_SLOT, GuiItems.close(), (p, e) -> p.closeInventory());
    }
    private ItemStack dec(ItemStack it) { return dec(it, 1); }
    private ItemStack dec(ItemStack it, int by) {
        if (it == null) return null;
        it.setAmount(it.getAmount() - by);
        return it.getAmount() <= 0 ? null : it;
    }
    /** Recomputes and redraws the live Success/Fail/Critical Fail display (requirement #13). */
    public void refreshChance(FailRunesPlugin plugin, ItemStack target, dev.failxos.failrunes.api.Rune rune, int level) {
        if (rune == null) { set(CHANCE_SLOT, GuiItems.named(Material.PAPER, "<gray>Place items to preview chances"), null); return; }
        ItemStack gemItem = inventory.getItem(GEM_SLOT);
        int gems = gemItem == null ? 0 : Math.min(gemItem.getAmount(), plugin.settings().gemsMaxApply());
        double baseS = Math.max(5, 80), baseC = Math.min(50, 5);
        Chances c = Chances.compute(baseS, baseC, gems, plugin.settings().gemFormula());
        set(CHANCE_SLOT, GuiItems.named(Material.PAPER, "<white>Chances",
                "<green>Success: " + Text.pct(c.success()), "<yellow>Fail: " + Text.pct(c.fail()), "<red>Critical Fail: " + Text.pct(c.critical())), null);
    }
    public static int targetSlot() { return TARGET_SLOT; }
    public static int runeSlot() { return RUNE_SLOT; }
    public static int gemSlot() { return GEM_SLOT; }
}
