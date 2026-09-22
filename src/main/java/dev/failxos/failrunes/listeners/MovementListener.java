package dev.failxos.failrunes.listeners;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.RuneTrigger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

/** ON_SPRINT / ON_JUMP / ON_FALL via move-event edge detection (cheap: only reacts on actual state changes). */
public final class MovementListener implements Listener {
    private final FailRunesPlugin plugin;
    public MovementListener(FailRunesPlugin plugin) { this.plugin = plugin; }

    @EventHandler(ignoreCancelled = true)
    public void onSprint(PlayerToggleSprintEvent e) {
        if (e.isSprinting()) plugin.armorEngine().fireArmor(RuneTrigger.ON_SPRINT, e.getPlayer(), ctx -> {});
    }
    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getY() < e.getTo().getY() && e.getPlayer().isOnGround())
            plugin.armorEngine().fireArmor(RuneTrigger.ON_JUMP, e.getPlayer(), ctx -> {});
        if (e.getPlayer().getFallDistance() > 2 && !e.getPlayer().isOnGround())
            plugin.armorEngine().fireArmor(RuneTrigger.ON_FALL, e.getPlayer(), ctx -> {});
    }
}
