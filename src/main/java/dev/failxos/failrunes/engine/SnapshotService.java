package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.RuneSnapshot;
import dev.failxos.failrunes.items.AppliedRunes;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Snapshots the player's active runes/set pieces/zone at the moment an effect activates (requirement #27/#46),
 * so switching equipment afterward can never retroactively change an already-triggered effect.
 */
public final class SnapshotService {
    private final FailRunesPlugin plugin;
    public SnapshotService(FailRunesPlugin plugin) { this.plugin = plugin; }

    public RuneSnapshot snapshot(Player player, ItemStack heldOrWorn) {
        AppliedRunes ar = plugin.appliedRunes();
        Map<String, Integer> runes = new HashMap<>();
        PlayerInventory inv = player.getInventory();
        for (ItemStack it : new ItemStack[]{inv.getItemInMainHand(), inv.getItemInOffHand(),
                inv.getHelmet(), inv.getChestplate(), inv.getLeggings(), inv.getBoots()}) {
            if (it == null) continue;
            for (var e : ar.read(it).entrySet()) runes.merge(e.getKey(), e.getValue(), Math::max);
        }
        Map<String, Integer> setPieces = plugin.sets().equippedCounts(player);
        Zone zone = plugin.zones().zoneAt(player.getLocation());
        String heldName = heldOrWorn == null ? "" : heldOrWorn.getType().name();
        return new RuneSnapshot(player.getUniqueId(), Map.copyOf(runes), Map.copyOf(setPieces), zone.id, heldName,
                player.getHealth(), System.currentTimeMillis());
    }
}
