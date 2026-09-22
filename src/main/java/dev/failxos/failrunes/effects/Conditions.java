package dev.failxos.failrunes.effects;

import dev.failxos.failrunes.api.ConditionParser;
import dev.failxos.failrunes.api.RuneContext;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Locale;

/** Registers every condition key from requirement #49 (health, world, zone, biome, weather, time, gear, stance...). */
public final class Conditions {
    private Conditions() {}

    public static void registerAll() {
        reg("health-below", (arg, x) -> ctx -> pct(ctx.player) < d(arg));
        reg("health-above", (arg, x) -> ctx -> pct(ctx.player) > d(arg));
        reg("health-hearts-below", (arg, x) -> ctx -> ctx.player.getHealth() < d(arg));
        reg("world", (arg, x) -> ctx -> ctx.player.getWorld().getName().equalsIgnoreCase(arg));
        reg("zone", (arg, x) -> ctx -> ctx.zone != null && ctx.zone.id.equalsIgnoreCase(arg));
        reg("biome", (arg, x) -> ctx -> ctx.player.getLocation().getBlock().getBiome().getKey().getKey().equalsIgnoreCase(arg));
        reg("weather-thunder", (arg, x) -> ctx -> ctx.player.getWorld().isThundering());
        reg("weather-rain", (arg, x) -> ctx -> ctx.player.getWorld().hasStorm());
        reg("weather-clear", (arg, x) -> ctx -> !ctx.player.getWorld().hasStorm());
        reg("time-day", (arg, x) -> ctx -> ctx.player.getWorld().getTime() < 13000);
        reg("time-night", (arg, x) -> ctx -> ctx.player.getWorld().getTime() >= 13000);
        reg("sneaking", (arg, x) -> ctx -> ctx.player.isSneaking());
        reg("not-sneaking", (arg, x) -> ctx -> !ctx.player.isSneaking());
        reg("sprinting", (arg, x) -> ctx -> ctx.player.isSprinting());
        reg("jumping", (arg, x) -> ctx -> !ctx.player.isOnGround() && ctx.player.getVelocity().getY() > 0.05);
        reg("falling", (arg, x) -> ctx -> !ctx.player.isOnGround() && ctx.player.getVelocity().getY() < -0.05);
        reg("blocking", (arg, x) -> ctx -> ctx.player.isBlocking());
        reg("fishing", (arg, x) -> ctx -> ctx.player.isInsideVehicle() == false && ctx.player.getGameMode() != GameMode.SPECTATOR && ctx.trigger.name().equals("ON_FISH"));
        reg("mining", (arg, x) -> ctx -> ctx.trigger.name().equals("ON_BLOCK_BREAK"));
        reg("combat", (arg, x) -> ctx -> ctx.victim != null || ctx.damage > 0);
        reg("world-type-nether", (arg, x) -> ctx -> ctx.player.getWorld().getEnvironment().name().equals("NETHER"));
        reg("world-type-end", (arg, x) -> ctx -> ctx.player.getWorld().getEnvironment().name().equals("THE_END"));
        reg("weapon-type", (arg, x) -> ctx -> {
            var it = ctx.player.getInventory().getItemInMainHand();
            return dev.failxos.failrunes.engine.EquipmentTypes.of(it) != null
                    && dev.failxos.failrunes.engine.EquipmentTypes.of(it).equalsIgnoreCase(arg);
        });
        reg("armor-type", (arg, x) -> ctx -> java.util.Arrays.stream(new org.bukkit.inventory.ItemStack[]{
                ctx.player.getInventory().getHelmet(), ctx.player.getInventory().getChestplate(),
                ctx.player.getInventory().getLeggings(), ctx.player.getInventory().getBoots()})
                .anyMatch(it -> it != null && dev.failxos.failrunes.engine.EquipmentTypes.of(it) != null
                        && dev.failxos.failrunes.engine.EquipmentTypes.of(it).equalsIgnoreCase(arg)));
        reg("permission", (arg, x) -> ctx -> ctx.player.hasPermission(arg));
        reg("chance", (arg, x) -> ctx -> Math.random() * 100 < d(arg));
        reg("target-type", (arg, x) -> ctx -> ctx.victim != null && ctx.victim.getType().name().equalsIgnoreCase(arg));
    }
    private static double pct(Player p) { return p.getHealth() / Math.max(1e-6, p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()) * 100; }
    private static double d(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return 0; } }
    private static void reg(String key, java.util.function.BiFunction<String, dev.failxos.failrunes.api.RuneCondition, dev.failxos.failrunes.api.RuneCondition> f) {
        ConditionParser.register(key, f);
    }
}
