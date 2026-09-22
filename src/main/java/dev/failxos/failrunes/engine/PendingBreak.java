package dev.failxos.failrunes.engine;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Drop work registered by runes at break/kill/fish time and executed when the drops are known. */
public final class PendingBreak {
    public final Location location;
    public final Material type;
    public final BlockData data;
    public final ItemStack tool;
    public final List<DropStage> stages = new ArrayList<>();
    public final List<ItemStack> rewards = new ArrayList<>();

    public PendingBreak(Location location, Material type, BlockData data, ItemStack tool) {
        this.location = location; this.type = type; this.data = data; this.tool = tool;
    }
    public boolean hasWork() { return !stages.isEmpty() || !rewards.isEmpty(); }
}
