package dev.failxos.failrunes.api;

import dev.failxos.failrunes.core.Chances;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Everything about one attempt to apply a rune to an item. */
public record RuneApplication(Player player, Rune rune, int level, int gems, Chances chances, ItemStack target) {}
