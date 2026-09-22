package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.*;
import dev.failxos.failrunes.api.events.*;
import dev.failxos.failrunes.core.Chances;
import dev.failxos.failrunes.items.AppliedRunes;
import dev.failxos.failrunes.storage.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Random;

/** Implements requirements #11-#13: drag-apply validation, success/fail/critical-fail, Lucky Gem math. */
public final class ApplicationService {
    public enum Outcome { OK, WRONG_EQUIPMENT, LEVEL_TOO_HIGH, CONFLICT, DUPLICATE_MAX, ZONE_BLOCKED, PROTECTED_ITEM }

    private final FailRunesPlugin plugin;
    private final Random random = new Random();

    public ApplicationService(FailRunesPlugin plugin) { this.plugin = plugin; }

    public Outcome validate(Player player, ItemStack target, Rune rune, int level) {
        String type = dev.failxos.failrunes.engine.EquipmentTypes.of(target);
        if (!dev.failxos.failrunes.engine.EquipmentTypes.matches(rune.equipment(), type)) return Outcome.WRONG_EQUIPMENT;
        boolean overmax = plugin.settings().overmaxEnabled();
        if (level > rune.cap(overmax)) return Outcome.LEVEL_TOO_HIGH;
        AppliedRunes ar = plugin.appliedRunes();
        Map<String, Integer> existing = ar.read(target);
        if (existing.containsKey(rune.id())) return Outcome.DUPLICATE_MAX;
        for (String conflict : rune.conflicts()) if (existing.containsKey(conflict)) return Outcome.CONFLICT;
        Zone zone = plugin.zones().zoneAt(player.getLocation());
        if (!zone.applyAllowed) return Outcome.ZONE_BLOCKED;
        return Outcome.OK;
    }

    /** @return the resulting item (may be null if the item was destroyed on critical fail). */
    public RuneResult apply(Player player, ItemStack target, Rune rune, int level, int gems) {
        PlayerData data = plugin.storage().cachedOrLoad(player.getUniqueId());
        Chances base = new Chances(baseSuccess(rune, level), baseFail(rune, level), baseCritical(rune, level));
        Chances chances = Chances.compute(base.success(), base.critical(), gems, plugin.settings().gemFormula());

        RuneApplyEvent apply = new RuneApplyEvent(player, rune, level);
        plugin.getServer().getPluginManager().callEvent(apply);
        if (apply.isCancelled()) return null;

        RuneResult result = chances.roll(random.nextDouble() * 100);
        AppliedRunes ar = plugin.appliedRunes();

        switch (result) {
            case SUCCESS -> {
                ar.put(target, rune.id(), level);
                data.discover(rune.id());
                data.stats(data.stats().withSuccess());
                plugin.getServer().getPluginManager().callEvent(new RuneSuccessEvent(player, rune, level));
                feedback(player, data, rune, "message.apply-success", Sound.ENTITY_PLAYER_LEVELUP, Particle.HAPPY_VILLAGER);
            }
            case FAIL -> {
                data.stats(data.stats().withFail());
                plugin.getServer().getPluginManager().callEvent(new RuneFailEvent(player, rune, level));
                feedback(player, data, rune, "message.apply-fail", Sound.ENTITY_VILLAGER_NO, Particle.SMOKE);
            }
            case CRITICAL_FAIL -> {
                boolean destroy = rune.criticalFailDestroy() != null ? rune.criticalFailDestroy() : true;
                data.stats(data.stats().withCritical(destroy));
                plugin.getServer().getPluginManager().callEvent(new RuneCriticalFailEvent(player, rune, level));
                feedback(player, data, rune, "message.apply-critical", Sound.ENTITY_GENERIC_EXPLODE, Particle.LAVA);
                if (destroy) { target.setAmount(0); return result; }
            }
        }
        if (gems > 0) { data.stats(data.stats().gems(gems)); }
        plugin.storage().saveAsync(data);
        return result;
    }
    private double baseSuccess(Rune rune, int level) {
        double over = Math.max(0, level - rune.maxLevel());
        return Math.max(5, 80 - over * 7);
    }
    private double baseFail(Rune rune, int level) { return 100 - baseSuccess(rune, level) - baseCritical(rune, level); }
    private double baseCritical(Rune rune, int level) {
        double over = Math.max(0, level - rune.maxLevel());
        return Math.min(50, 5 + over * 5);
    }
    private void feedback(Player p, PlayerData data, Rune rune, String key, Sound sound, Particle particle) {
        if (plugin.prefs().enabled(data, rune.id(), RunePreference.SOUNDS)) p.playSound(p.getLocation(), sound, 1f, 1f);
        if (plugin.prefs().enabled(data, rune.id(), RunePreference.PARTICLES)) p.getWorld().spawnParticle(particle, p.getLocation().add(0, 1, 0), 20, 0.3, 0.3, 0.3);
        if (plugin.prefs().enabled(data, rune.id(), RunePreference.MESSAGES)) {
            Component name = dev.failxos.failrunes.util.Text.mm(dev.failxos.failrunes.util.Text.escape(rune.name()));
            p.sendMessage(plugin.lang().getComponent(key, name));
        }
    }
}
