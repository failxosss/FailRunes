package dev.failxos.failrunes.storage;

import dev.failxos.failrunes.api.DamageHistory;
import dev.failxos.failrunes.api.RunePreference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** In-memory, database-backed profile for one player. */
public final class PlayerData {
    public record Stats(long success, long fail, long critical, long destroyed, long gemsUsed, long incinerated,
                        long cleansed, long detectorFinds, long xpSpent, long xpRecovered, String mostUsedRune, String rarestRune) {
        public static final Stats EMPTY = new Stats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, null, null);
        public Stats withSuccess() { return copy(success + 1, fail, critical, destroyed); }
        public Stats withFail() { return copy(success, fail + 1, critical, destroyed); }
        public Stats withCritical(boolean destroyedItem) { return copy(success, fail, critical + 1, destroyed + (destroyedItem ? 1 : 0)); }
        private Stats copy(long s, long f, long c, long d) { return new Stats(s, f, c, d, gemsUsed, incinerated, cleansed, detectorFinds, xpSpent, xpRecovered, mostUsedRune, rarestRune); }
        public Stats gems(long n) { return new Stats(success, fail, critical, destroyed, gemsUsed + n, incinerated, cleansed, detectorFinds, xpSpent, xpRecovered, mostUsedRune, rarestRune); }
        public Stats incinerated(long n, long xp) { return new Stats(success, fail, critical, destroyed, gemsUsed, incinerated + n, cleansed, detectorFinds, xpSpent, xpRecovered + xp, mostUsedRune, rarestRune); }
        public Stats cleansed(long n) { return new Stats(success, fail, critical, destroyed, gemsUsed, incinerated, cleansed + n, detectorFinds, xpSpent, xpRecovered, mostUsedRune, rarestRune); }
        public Stats detectorFind() { return new Stats(success, fail, critical, destroyed, gemsUsed, incinerated, cleansed, detectorFinds + 1, xpSpent, xpRecovered, mostUsedRune, rarestRune); }
        public Stats spent(long xp) { return new Stats(success, fail, critical, destroyed, gemsUsed, incinerated, cleansed, detectorFinds, xpSpent + xp, xpRecovered, mostUsedRune, rarestRune); }
    }

    public final UUID uuid;
    private final Set<String> discovered = new HashSet<>();
    private int luckyGemsInventoryHint = 0; // display-only; real count lives in the player's inventory
    private int detectorLevel = 0;
    private Stats stats = Stats.EMPTY;
    private final Map<String, Integer> prefsPerRune = new HashMap<>();
    private int prefsGlobal = RunePreference.DEFAULT;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final DamageHistory damageHistory = new DamageHistory();
    private boolean dirty;

    public PlayerData(UUID uuid) { this.uuid = uuid; }

    public Set<String> discovered() { return discovered; }
    public boolean discover(String runeId) { boolean n = discovered.add(runeId); if (n) dirty = true; return n; }
    public int detectorLevel() { return detectorLevel; }
    public void detectorLevel(int lvl) { detectorLevel = lvl; dirty = true; }
    public Stats stats() { return stats; }
    public void stats(Stats s) { stats = s; dirty = true; }
    public int luckyGems() { return luckyGemsInventoryHint; }
    public void luckyGems(int n) { luckyGemsInventoryHint = n; }

    public int pref(String runeId) { return prefsPerRune.getOrDefault(runeId, prefsGlobal & RunePreference.PER_RUNE_MASK); }
    public void pref(String runeId, int flags) { prefsPerRune.put(runeId, flags); dirty = true; }
    public int globalPref() { return prefsGlobal; }
    public void globalPref(int flags) { prefsGlobal = flags; dirty = true; }
    public Map<String, Integer> allPrefs() { return prefsPerRune; }

    public Map<String, Long> cooldowns() { return cooldowns; }
    public void cooldown(String runeId, long expiresAt) { cooldowns.put(runeId, expiresAt); }
    public double cooldownRemaining(String runeId) {
        Long e = cooldowns.get(runeId);
        return e == null ? 0 : Math.max(0, (e - System.currentTimeMillis()) / 1000.0);
    }
    public DamageHistory damageHistory() { return damageHistory; }
    public boolean dirty() { return dirty; }
    public void clearDirty() { dirty = false; }
}
