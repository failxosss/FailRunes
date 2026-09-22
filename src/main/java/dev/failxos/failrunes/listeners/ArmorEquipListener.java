package dev.failxos.failrunes.listeners;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.RuneTrigger;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class ArmorEquipListener implements Listener {
    private final FailRunesPlugin plugin;
    public ArmorEquipListener(FailRunesPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onChange(PlayerArmorChangeEvent e) {
        if (e.getNewItem() != null && !e.getNewItem().getType().isAir())
            plugin.engine().fire(RuneTrigger.ON_ARMOR_EQUIP, e.getPlayer(), e.getNewItem(), ctx -> {});
    }
}
