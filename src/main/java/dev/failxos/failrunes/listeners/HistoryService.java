package dev.failxos.failrunes.listeners;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.DamageHistory;

import java.util.UUID;

public final class HistoryService {
    private final FailRunesPlugin plugin;
    public HistoryService(FailRunesPlugin plugin) { this.plugin = plugin; }

    public void record(UUID uuid, DamageHistory.Entry entry) {
        var data = plugin.storage().cachedOrLoad(uuid);
        data.damageHistory().add(entry, plugin.settings().damageHistoryMax(), plugin.settings().damageHistoryRetentionMs());
    }
}
