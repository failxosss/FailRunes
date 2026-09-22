package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.DamageHistory;
import dev.failxos.failrunes.storage.PlayerData;
import dev.failxos.failrunes.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Requirement #24: recent damage events (source, attacker, victim, rune, weapon, damage, time, crit). */
public final class DamageHistoryGui extends Gui {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    public DamageHistoryGui(FailRunesPlugin plugin, Player viewer) {
        super(Text.mm("<dark_red>✦ Damage History"));
        PlayerData data = plugin.storage().cachedOrLoad(viewer.getUniqueId());
        int slot = 0;
        for (DamageHistory.Entry e : data.damageHistory().entries()) {
            if (slot >= 45) break;
            set(slot++, GuiItems.named(e.incoming() ? Material.RED_DYE : Material.ORANGE_DYE,
                    (e.incoming() ? "<red>Taken: " : "<gold>Dealt: ") + Text.dec(e.damage()),
                    "From: " + safe(e.attacker()), "To: " + safe(e.victim()),
                    e.rune() != null ? "Rune: " + e.rune() : "", "Weapon: " + safe(e.weapon()),
                    e.critical() ? "<yellow>Critical Hit" : "", FMT.format(Instant.ofEpochMilli(e.time()))), null);
        }
        if (data.damageHistory().isEmpty()) set(22, GuiItems.named(Material.BARRIER, "<gray>No recent damage events"), null);
        GuiItems.fillBorder(inventory);
        set(CLOSE_SLOT, GuiItems.close(), (p, e) -> p.closeInventory());
    }
    private String safe(String s) { return s == null ? "-" : s; }
}
