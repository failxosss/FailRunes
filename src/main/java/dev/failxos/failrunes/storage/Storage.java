package dev.failxos.failrunes.storage;

import java.util.UUID;

public interface Storage {
    void init();
    void close();
    PlayerData load(UUID uuid);
    void save(PlayerData data);
    /** Runs off the main thread; the callback is scheduled back onto it. */
    void loadAsync(UUID uuid, java.util.function.Consumer<PlayerData> callback);
    void saveAsync(PlayerData data);
}
