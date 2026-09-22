package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.PoolType;
import dev.failxos.failrunes.api.RunePool;
import dev.failxos.failrunes.storage.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.Set;

/**
 * Requirement #10: the Rune Detector triggers on mining/logging/farming/digging/fishing actions and can reveal
 * unidentified runes from configurable pools; higher detector levels unlock better tiers.
 */
public final class DetectorService {
    private final FailRunesPlugin plugin;
    private final Random random = new Random();
    public DetectorService(FailRunesPlugin plugin) { this.plugin = plugin; }

    public void onGather(Player p, Material block, Set<String> tags) { attempt(p, "block"); }
    public void onFish(Player p) { attempt(p, "fish"); }

    private void attempt(Player p, String actionKind) {
        PlayerData data = plugin.storage().cachedOrLoad(p.getUniqueId());
        if (data.detectorLevel() <= 0) return;
        double chance = plugin.getConfig().getDouble("detector.activation-chance", 2.0) * (1 + data.detectorLevel() * 0.15);
        if (random.nextDouble() * 100 >= chance) return;
        java.util.List<String> pools = plugin.getConfig().getStringList("detector.level-" + data.detectorLevel() + ".pools");
        if (pools.isEmpty()) pools = plugin.getConfig().getStringList("detector.default-pools");
        if (pools.isEmpty()) return;
        String poolId = pools.get(random.nextInt(pools.size()));
        RunePool pool = plugin.pools().get(poolId);
        if (pool == null || pool.table().isEmpty()) return;
        var entry = pool.table().roll(random);
        if (entry == null) return;
        var item = plugin.itemFactory().unidentified(poolId, entry.minLevel(), entry.maxLevel());
        p.getInventory().addItem(item).values().forEach(left -> p.getWorld().dropItemNaturally(p.getLocation(), left));
        data.stats(data.stats().detectorFind());
        p.sendMessage(plugin.lang().get("message.detector-find"));
    }
}
