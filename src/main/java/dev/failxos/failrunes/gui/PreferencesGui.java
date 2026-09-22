package dev.failxos.failrunes.gui;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.RunePreference;
import dev.failxos.failrunes.storage.PlayerData;
import dev.failxos.failrunes.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Requirement #21: per-rune sounds/particles/messages/cooldown popups/action bar/titles/chat/visual toggles. */
public final class PreferencesGui extends Gui {
    public PreferencesGui(FailRunesPlugin plugin, Player viewer) {
        super(Text.mm("<white>✦ Rune Preferences"));
        PlayerData data = plugin.storage().cachedOrLoad(viewer.getUniqueId());
        int[] bits = {RunePreference.SOUNDS, RunePreference.PARTICLES, RunePreference.MESSAGES, RunePreference.COOLDOWN_POPUP,
                RunePreference.ACTION_BAR, RunePreference.TITLES, RunePreference.CHAT, RunePreference.VISUAL};
        String[] labels = {"Sounds", "Particles", "Messages", "Cooldown Popups", "Action Bar", "Titles", "Chat Messages", "Visual Effects"};
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19};
        for (int i = 0; i < bits.length; i++) {
            int bit = bits[i]; String label = labels[i];
            boolean on = RunePreference.has(data.globalPref(), bit);
            set(slots[i], toggleIcon(label, on), (p, e) -> {
                plugin.prefs().toggleGlobal(data, bit);
                inventory.setItem(slots[i], toggleIcon(label, RunePreference.has(data.globalPref(), bit)));
            });
        }
        GuiItems.fillBorder(inventory);
        set(BACK_SLOT, GuiItems.back(), (p, e) -> { if (parent != null) parent.open(p); });
        set(CLOSE_SLOT, GuiItems.close(), (p, e) -> p.closeInventory());
    }
    private org.bukkit.inventory.ItemStack toggleIcon(String label, boolean on) {
        return GuiItems.named(on ? Material.LIME_DYE : Material.GRAY_DYE, "<white>" + label, on ? "<green>ON" : "<red>OFF", "Click to toggle");
    }
}
