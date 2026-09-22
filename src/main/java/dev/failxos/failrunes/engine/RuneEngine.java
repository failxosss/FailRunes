package dev.failxos.failrunes.engine;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.*;
import dev.failxos.failrunes.api.events.RuneTriggerEvent;
import dev.failxos.failrunes.items.AppliedRunes;
import dev.failxos.failrunes.storage.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Central proc dispatcher: for a given trigger, resolves which equipped runes may fire, checks conditions,
 * cooldowns and zone rules, then executes their effects through the pluggable {@link EffectRegistry}.
 */
public final class RuneEngine {
    private final FailRunesPlugin plugin;
    private final Random random = new Random();

    public RuneEngine(FailRunesPlugin plugin) { this.plugin = plugin; }

    /** Fires every rune on the given item(s) that listens to this trigger. Returns the number that activated. */
    public int fire(RuneTrigger trigger, Player player, ItemStack source, java.util.function.Consumer<RuneContext> prepare) {
        if (source == null) return 0;
        AppliedRunes ar = plugin.appliedRunes();
        Map<String, Integer> runes = ar.read(source);
        if (runes.isEmpty()) return 0;
        PlayerData data = plugin.storage().cachedOrLoad(player.getUniqueId());
        Zone zone = plugin.zones().zoneAt(player.getLocation());
        int fired = 0;
        for (var e : runes.entrySet()) {
            Rune rune = plugin.runes().get(e.getKey());
            if (rune == null || rune.trigger() != trigger) continue;
            if (!zone.allows(rune)) continue;
            if (plugin.cooldowns().onCooldown(data, rune)) continue;
            RuneContext ctx = new RuneContext(player, trigger);
            ctx.rune = rune; ctx.level = e.getValue(); ctx.zone = zone;
            if (prepare != null) prepare.accept(ctx);
            ctx.snapshot = plugin.snapshots().snapshot(player, source);
            if (!passesConditions(rune, ctx)) continue;
            double chance = rune.level(ctx.level).chance() * zoneMultiplier(zone, trigger);
            if (random.nextDouble() * 100 >= chance) continue;

            RuneTriggerEvent ev = new RuneTriggerEvent(player, rune, ctx.level);
            plugin.getServer().getPluginManager().callEvent(ev);
            if (ev.isCancelled()) continue;

            execute(rune, ctx, data);
            fired++;
        }
        return fired;
    }
    private boolean passesConditions(Rune rune, RuneContext ctx) {
        for (RuneCondition c : rune.conditions()) if (!safeTest(c, ctx)) return false;
        return true;
    }
    private boolean safeTest(RuneCondition c, RuneContext ctx) {
        try { return c.test(ctx); } catch (Exception e) { return false; }
    }
    private double zoneMultiplier(Zone zone, RuneTrigger trigger) {
        return trigger == RuneTrigger.ON_FISH ? zone.fishMultiplier : zone.procMultiplier;
    }

    private void execute(Rune rune, RuneContext ctx, PlayerData data) {
        for (EffectSpec spec : rune.effects()) {
            RuneEffect effect = EffectRegistry.get(spec.type());
            if (effect == null) { plugin.getLogger().warning("[FailRunes] Unknown effect type '" + spec.type() + "' on rune " + rune.id()); continue; }
            try { effect.apply(ctx, spec); }
            catch (Exception ex) { plugin.getLogger().warning("[FailRunes] Effect '" + spec.type() + "' on rune " + rune.id() + " threw: " + ex); }
        }
        plugin.cooldowns().start(ctx.player, data, rune, ctx.level);
        announce(rune, ctx, data);
        plugin.debug("trigger " + rune.id() + " lvl" + ctx.level + " for " + ctx.player.getName());
    }
    private void announce(Rune rune, RuneContext ctx, PlayerData data) {
        boolean discovered = data.discover(rune.id());
        if (discovered) {
            var dev = new dev.failxos.failrunes.api.events.RuneDiscoverEvent(ctx.player, rune, ctx.level);
            plugin.getServer().getPluginManager().callEvent(dev);
            if (plugin.prefs().enabled(data, rune.id(), RunePreference.SOUNDS)) playDiscoverySound(ctx.player);
        }
        if (!rune.messageDefault() || !plugin.prefs().enabled(data, rune.id(), RunePreference.MESSAGES)) return;
        RuneLevel rl = rune.level(ctx.level);
        Component name = dev.failxos.failrunes.util.Text.mm("<color:" + hex(rune) + ">" + dev.failxos.failrunes.util.Text.escape(rune.name())
                        + " " + dev.failxos.failrunes.util.Roman.of(ctx.level) + "</color>")
                .hoverEvent(HoverEvent.showText(hoverInfo(rune, ctx.level, rl)));
        Component line = plugin.lang().getComponent("message.rune-activated", name);
        if (plugin.prefs().enabled(data, rune.id(), RunePreference.CHAT)) ctx.player.sendMessage(line);
        if (plugin.prefs().enabled(data, rune.id(), RunePreference.ACTION_BAR)) ctx.player.sendActionBar(line);
    }
    private void playDiscoverySound(Player p) {
        try { p.playSound(p.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f); } catch (Exception ignored) {}
    }
    private Component hoverInfo(Rune rune, int level, RuneLevel rl) {
        Component c = dev.failxos.failrunes.util.Text.mm("<color:" + hex(rune) + ">" + dev.failxos.failrunes.util.Text.escape(rune.name())
                + " " + dev.failxos.failrunes.util.Roman.of(level) + "</color>\n<gray>" + rune.rarity() + "\n");
        if (rl.chance() > 0) c = c.append(dev.failxos.failrunes.util.Text.mm("<gray>Chance: <white>" + dev.failxos.failrunes.util.Text.pct(rl.chance()) + "\n"));
        if (rl.cooldown() > 0) c = c.append(dev.failxos.failrunes.util.Text.mm("<gray>Cooldown: <white>" + dev.failxos.failrunes.util.Text.dec(rl.cooldown()) + "s\n"));
        for (String l : dev.failxos.failrunes.util.Text.wrap(rune.description(), 40)) c = c.append(dev.failxos.failrunes.util.Text.mm("<gray>" + dev.failxos.failrunes.util.Text.escape(l) + "\n"));
        return c;
    }
    private static String hex(Rune r) {
        return switch (r.rarity()) { case COMMON -> "gray"; case RARE -> "aqua"; case LEGENDARY -> "gold"; case MYTHICAL -> "light_purple"; };
    }
}
