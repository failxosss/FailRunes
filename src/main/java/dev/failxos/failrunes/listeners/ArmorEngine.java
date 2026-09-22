package dev.failxos.failrunes.listeners;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.RuneTrigger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Fires triggers for runes on worn armor (not just held items) - armor pieces proc independently. */
public final class ArmorEngine {
    private final FailRunesPlugin plugin;
    public ArmorEngine(FailRunesPlugin plugin) { this.plugin = plugin; }

    public void fireArmor(RuneTrigger trigger, Player p, java.util.function.Consumer<dev.failxos.failrunes.api.RuneContext> prepare) {
        for (ItemStack piece : new ItemStack[]{p.getInventory().getHelmet(), p.getInventory().getChestplate(),
                p.getInventory().getLeggings(), p.getInventory().getBoots()}) {
            if (piece != null) plugin.engine().fire(trigger, p, piece, prepare);
        }
    }
}
