package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.RunePreference;
import dev.failxos.failrunes.storage.PlayerData;

public final class PreferenceService {
    private final FailRunesPlugin plugin;
    public PreferenceService(FailRunesPlugin plugin) { this.plugin = plugin; }

    public boolean enabled(PlayerData data, String runeId, int bit) {
        return RunePreference.has(data.pref(runeId), bit);
    }
    public boolean globalSoundEnabled(PlayerData data, int bit) { return RunePreference.has(data.globalPref(), bit); }
    public void toggle(PlayerData data, String runeId, int bit) {
        int flags = data.pref(runeId);
        data.pref(runeId, flags ^ bit);
    }
    public void toggleGlobal(PlayerData data, int bit) { data.globalPref(data.globalPref() ^ bit); }
}
