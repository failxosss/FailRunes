package dev.failxos.failrunes.effects;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.EffectRegistry;
import dev.failxos.failrunes.api.RuneContext;
import dev.failxos.failrunes.api.RuneTarget;
import dev.failxos.failrunes.util.Text;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Registers every built-in effect type (requirement #51). New effects can be added at runtime with
 * {@code EffectRegistry.register("NAME", (ctx, spec) -> ...)} by this plugin or any other, without engine changes.
 */
public final class Effects {
    private Effects() {}

    public static void registerAll(FailRunesPlugin plugin) {
        EffectRegistry.register("DAMAGE", (ctx, s) -> {
            double amt = s.num("amount", 4, ctx.level) * ctx.damageMultiplier;
            for (LivingEntity t : targets(ctx, s)) damage(plugin, ctx, t, amt);
        });
        EffectRegistry.register("AREA_DAMAGE", (ctx, s) -> {
            double amt = s.num("amount", 4, ctx.level);
            double radius = s.num("radius", 3, ctx.level);
            for (Entity e : origin(ctx).getWorld().getNearbyEntities(origin(ctx), radius, radius, radius)) {
                if (e instanceof LivingEntity le && le != ctx.player) damage(plugin, ctx, le, amt);
            }
            particles(plugin, ctx, s, origin(ctx));
        });
        EffectRegistry.register("HEAL", (ctx, s) -> {
            double amt = s.num("amount", 2, ctx.level);
            LivingEntity t = ctx.player;
            t.setHealth(Math.min(maxHealth(t), t.getHealth() + amt));
        });
        EffectRegistry.register("LIGHTNING", (ctx, s) -> {
            for (LivingEntity t : targets(ctx, s)) {
                if (s.bool("damage-only")) t.getWorld().strikeLightningEffect(t.getLocation());
                else t.getWorld().strikeLightning(t.getLocation());
            }
        });
        EffectRegistry.register("FIRE", (ctx, s) -> {
            int ticks = (int) s.num("ticks", 60, ctx.level);
            for (LivingEntity t : targets(ctx, s)) t.setFireTicks(Math.max(t.getFireTicks(), ticks));
        });
        registerPotion("POISON", PotionEffectType.POISON);
        registerPotion("WITHER", PotionEffectType.WITHER);
        registerPotion("SLOW", PotionEffectType.SLOWNESS);
        registerPotion("SPEED", PotionEffectType.SPEED);
        registerPotion("STRENGTH", PotionEffectType.STRENGTH);
        registerPotion("REGENERATION", PotionEffectType.REGENERATION);
        registerPotion("ABSORPTION", PotionEffectType.ABSORPTION);

        EffectRegistry.register("TELEPORT", (ctx, s) -> {
            String mode = s.str("mode", "behind-target");
            LivingEntity target = ctx.victim != null ? ctx.victim : ctx.player;
            Location dest = switch (mode) {
                case "to-target" -> target.getLocation();
                case "random-nearby" -> randomNearby(ctx.player.getLocation(), s.num("radius", 8, ctx.level));
                default -> target.getLocation().subtract(target.getLocation().getDirection().normalize().multiply(1.5));
            };
            ctx.player.teleportAsync(dest);
        });
        EffectRegistry.register("PULL", (ctx, s) -> pushPull(ctx, s, -1));
        EffectRegistry.register("PUSH", (ctx, s) -> pushPull(ctx, s, 1));
        EffectRegistry.register("EXPLOSION", (ctx, s) -> {
            Location l = origin(ctx);
            l.getWorld().createExplosion(l, (float) s.num("power", 2, ctx.level), s.bool("fire"), s.bool("break-blocks"), ctx.player);
        });
        EffectRegistry.register("PROJECTILE", (ctx, s) -> shoot(ctx, s, 1));
        EffectRegistry.register("MULTI_PROJECTILE", (ctx, s) -> shoot(ctx, s, (int) s.num("count", 5, ctx.level)));

        EffectRegistry.register("BLOCK_BREAK", (ctx, s) -> {
            if (ctx.block == null) return;
            if (plugin.settings().worldguardEnabled() && !plugin.worldGuard().canBuild(ctx.player, ctx.block.getLocation())) return;
            ctx.block.breakNaturally(ctx.player.getInventory().getItemInMainHand());
        });
        EffectRegistry.register("BLOCK_PLACE", (ctx, s) -> {
            if (ctx.block == null) return;
            Material m = Material.matchMaterial(s.str("material", "STONE"));
            if (m == null) return;
            if (plugin.settings().worldguardEnabled() && !plugin.worldGuard().canBuild(ctx.player, ctx.block.getLocation())) return;
            ctx.block.setType(m);
        });
        EffectRegistry.register("ITEM_DROP", (ctx, s) -> {
            Material m = Material.matchMaterial(s.str("material", "EMERALD"));
            if (m == null) return;
            ItemStack it = new ItemStack(m, (int) s.num("amount", 1, ctx.level));
            if (ctx.pending != null) ctx.pending.rewards.add(it); else origin(ctx).getWorld().dropItemNaturally(origin(ctx), it);
        });
        EffectRegistry.register("MONEY", (ctx, s) -> {
            if (plugin.vault().enabled()) plugin.vault().deposit(ctx.player, s.num("amount", 10, ctx.level));
        });
        EffectRegistry.register("XP", (ctx, s) -> dev.failxos.failrunes.util.XpUtil.give(ctx.player, Math.round(s.num("amount", 10, ctx.level))));
        EffectRegistry.register("LOOT", (ctx, s) -> {
            var pool = plugin.pools().get(s.str("pool", ""));
            if (pool == null || pool.table().isEmpty()) return;
            var entry = pool.table().roll(new java.util.Random());
            if (entry == null) return;
            var rune = plugin.runes().get(entry.runeId());
            if (rune == null) return;
            int lvl = entry.minLevel() + new java.util.Random().nextInt(Math.max(1, entry.maxLevel() - entry.minLevel() + 1));
            ItemStack it = plugin.itemFactory().unidentified(pool.id, lvl, lvl);
            if (ctx.pending != null) ctx.pending.rewards.add(it); else origin(ctx).getWorld().dropItemNaturally(origin(ctx), it);
        });
        EffectRegistry.register("PARTICLE", (ctx, s) -> particles(plugin, ctx, s, origin(ctx)));
        EffectRegistry.register("SOUND", (ctx, s) -> {
            try { ctx.player.playSound(ctx.player.getLocation(), Sound.valueOf(s.str("sound", "ENTITY_EXPERIENCE_ORB_PICKUP")),
                    (float) s.num("volume", 1, ctx.level), (float) s.num("pitch", 1, ctx.level)); } catch (IllegalArgumentException ignored) {}
        });
        EffectRegistry.register("MESSAGE", (ctx, s) -> ctx.player.sendMessage(Text.mm(s.str("text", ""))));
        EffectRegistry.register("TITLE", (ctx, s) -> ctx.player.showTitle(net.kyori.adventure.title.Title.title(
                Text.mm(s.str("title", "")), Text.mm(s.str("subtitle", "")))));
        EffectRegistry.register("ACTIONBAR", (ctx, s) -> ctx.player.sendActionBar(Text.mm(s.str("text", ""))));
        EffectRegistry.register("DETECT", (ctx, s) -> { /* handled directly by DetectorListener */ });
    }

    private static void registerPotion(String name, PotionEffectType type) {
        EffectRegistry.register(name, (ctx, s) -> {
            int ticks = (int) s.num("ticks", 100, ctx.level);
            int amp = (int) s.num("amplifier", 0, ctx.level);
            for (LivingEntity t : targets(ctx, s)) t.addPotionEffect(new PotionEffect(type, ticks, amp));
        });
    }
    private static void shoot(RuneContext ctx, dev.failxos.failrunes.api.EffectSpec s, int count) {
        String proj = s.str("projectile", "ARROW");
        Vector base = ctx.player.getLocation().getDirection();
        double spread = s.num("spread-degrees", 12, ctx.level);
        for (int i = 0; i < count; i++) {
            Vector dir = count == 1 ? base : spreadVector(base, spread, i, count);
            Projectile p = switch (proj) {
                case "FIREBALL" -> ctx.player.launchProjectile(org.bukkit.entity.SmallFireball.class);
                case "SNOWBALL" -> ctx.player.launchProjectile(Snowball.class);
                default -> ctx.player.launchProjectile(Arrow.class);
            };
            p.setVelocity(dir.normalize().multiply(s.num("speed", 2, ctx.level)));
        }
    }
    private static Vector spreadVector(Vector base, double degrees, int i, int count) {
        double angle = Math.toRadians(-degrees / 2 + degrees * i / Math.max(1, count - 1));
        double cos = Math.cos(angle), sin = Math.sin(angle);
        return new Vector(base.getX() * cos - base.getZ() * sin, base.getY(), base.getX() * sin + base.getZ() * cos);
    }
    private static void pushPull(RuneContext ctx, dev.failxos.failrunes.api.EffectSpec s, int sign) {
        double strength = s.num("strength", 1, ctx.level) * sign;
        for (LivingEntity t : targets(ctx, s)) {
            Vector dir = t.getLocation().toVector().subtract(ctx.player.getLocation().toVector());
            if (dir.lengthSquared() < 1e-4) continue;
            t.setVelocity(t.getVelocity().add(dir.normalize().multiply(strength)));
        }
    }
    private static void damage(FailRunesPlugin plugin, RuneContext ctx, LivingEntity t, double amount) {
        var ev = new dev.failxos.failrunes.api.events.RuneDamageEvent(ctx.player, ctx.rune, ctx.level, t, amount);
        plugin.getServer().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return;
        t.damage(ev.getDamage(), ctx.player);
    }
    private static void particles(FailRunesPlugin plugin, RuneContext ctx, dev.failxos.failrunes.api.EffectSpec s, Location at) {
        try {
            Particle p = Particle.valueOf(s.str("particle", ctx.rune != null ? ctx.rune.particle() : "CRIT"));
            at.getWorld().spawnParticle(p, at, (int) s.num("count", 15, ctx.level), s.num("spread", 0.4, ctx.level),
                    s.num("spread", 0.4, ctx.level), s.num("spread", 0.4, ctx.level));
        } catch (IllegalArgumentException ignored) {}
    }
    private static Location origin(RuneContext ctx) { return ctx.block != null ? ctx.block.getLocation().add(0.5, 0.5, 0.5) : ctx.player.getLocation(); }
    private static Location randomNearby(Location base, double radius) {
        double a = Math.random() * Math.PI * 2;
        return base.clone().add(Math.cos(a) * radius, 0, Math.sin(a) * radius);
    }
    private static double maxHealth(LivingEntity e) {
        var attr = e.getAttribute(Attribute.MAX_HEALTH);
        return attr == null ? 20 : attr.getValue();
    }

    /** Resolves {@link RuneTarget} against the current context into concrete LivingEntities. */
    static List<LivingEntity> targets(RuneContext ctx, dev.failxos.failrunes.api.EffectSpec s) {
        RuneTarget t;
        try { t = RuneTarget.valueOf(s.str("target", "TARGET")); } catch (IllegalArgumentException e) { t = RuneTarget.TARGET; }
        List<LivingEntity> out = new ArrayList<>();
        switch (t) {
            case SELF -> out.add(ctx.player);
            case TARGET, PLAYER, MOB -> { if (ctx.victim != null) out.add(ctx.victim); }
            case RANDOM_TARGET -> nearby(ctx, 8).stream().findAny().ifPresent(out::add);
            case ALL_MOBS, NEARBY_MOBS -> out.addAll(nearby(ctx, 8));
            case NEARBY_PLAYERS -> ctx.player.getWorld().getNearbyEntities(ctx.player.getLocation(), 8, 8, 8).stream()
                    .filter(e -> e instanceof Player).map(e -> (LivingEntity) e).forEach(out::add);
            case AREA, BLOCK -> out.addAll(nearby(ctx, s.num("radius", 4, ctx.level)));
        }
        return out;
    }
    private static List<LivingEntity> nearby(RuneContext ctx, double radius) {
        return ctx.player.getWorld().getNearbyEntities(ctx.player.getLocation(), radius, radius, radius).stream()
                .filter(e -> e instanceof LivingEntity && e != ctx.player).map(e -> (LivingEntity) e).collect(Collectors.toList());
    }
}
