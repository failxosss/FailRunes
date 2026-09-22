package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.storage.PlayerData;

public final class CollectionService {
    private final FailRunesPlugin plugin;
    public CollectionService(FailRunesPlugin plugin) { this.plugin = plugin; }

    public double percent(PlayerData data) {
        int total = plugin.runes().size();
        if (total == 0) return 0;
        return data.discovered().size() * 100.0 / total;
    }
}
