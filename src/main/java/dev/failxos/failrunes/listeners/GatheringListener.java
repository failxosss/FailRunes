package dev.failxos.failrunes.listeners;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.RuneTrigger;
import dev.failxos.failrunes.engine.DetectorService;
import dev.failxos.failrunes.engine.DropContext;
import dev.failxos.failrunes.engine.PendingBreak;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

/**
 * ON_BLOCK_BREAK / ON_BLOCK_PLACE / ON_FISH covering mining, logging, farming, digging and fishing
 * (requirements #31-#35). Every break routes through the common {@link dev.failxos.failrunes.engine.DropPipeline}
 * (#28) and, if MCMMO is present, its bonus drops are folded in too (#29).
 */
public final class GatheringListener implements Listener {
    private final FailRunesPlugin plugin;
    public GatheringListener(FailRunesPlugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        ItemStack tool = p.getInventory().getItemInMainHand();
        PendingBreak pending = new PendingBreak(e.getBlock().getLocation(), e.getBlock().getType(), e.getBlock().getBlockData(), tool.clone());
        plugin.engine().fire(RuneTrigger.ON_BLOCK_BREAK, p, tool, ctx -> { ctx.block = e.getBlock(); ctx.pending = pending; ctx.tags = plugin.blockTags().tagsOf(e.getBlock().getType()); });
        plugin.detector().onGather(p, e.getBlock().getType(), plugin.blockTags().tagsOf(e.getBlock().getType()));
        if (pending.hasWork()) {
            DropContext dc = new DropContext(p, e.getBlock().getLocation());
            dc.rewards.addAll(pending.rewards);
            plugin.dropPipeline().run(dc);
            for (ItemStack it : dc.rewards) e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), it);
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        ItemStack hand = p.getInventory().getItemInMainHand();
        plugin.engine().fire(RuneTrigger.ON_BLOCK_PLACE, p, hand, ctx -> ctx.block = e.getBlockPlaced());
    }
    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH && e.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) return;
        Player p = e.getPlayer();
        ItemStack rod = p.getInventory().getItemInMainHand();
        PendingBreak pending = new PendingBreak(p.getLocation(), org.bukkit.Material.WATER, null, rod.clone());
        plugin.engine().fire(RuneTrigger.ON_FISH, p, rod, ctx -> {
            ctx.pending = pending;
            if (e.getCaught() instanceof org.bukkit.entity.Item it) ctx.caught = it;
        });
        plugin.detector().onFish(p);
        if (pending.hasWork()) {
            DropContext dc = new DropContext(p, p.getLocation());
            dc.rewards.addAll(pending.rewards);
            plugin.dropPipeline().run(dc);
            for (ItemStack it : dc.rewards) p.getWorld().dropItemNaturally(p.getLocation(), it);
        }
    }
}
