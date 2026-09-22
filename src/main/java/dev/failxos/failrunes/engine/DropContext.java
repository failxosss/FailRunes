package dev.failxos.failrunes.engine;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Mutable drop state passed through {@link DropPipeline}. {@code items} = natural drops, {@code rewards} = rune-made items. */
public final class DropContext {
    public final Player player;
    public final Location location;
    public final List<ItemStack> items = new ArrayList<>();
    public final List<ItemStack> rewards = new ArrayList<>();
    public boolean toInventory;
    public double lootMultiplier = 1.0;
    public final Random rnd = new Random();

    public DropContext(Player player, Location location) { this.player = player; this.location = location; }
}
