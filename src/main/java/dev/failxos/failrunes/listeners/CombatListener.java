package dev.failxos.failrunes.listeners;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.RuneTrigger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.projectiles.ProjectileSource;

/** ON_ATTACK/ON_HIT/ON_KILL/ON_DEATH/ON_DAMAGE/ON_CRITICAL/ON_PROJECTILE_HIT/ON_MOB_KILL/ON_PLAYER_KILL + damage history. */
public final class CombatListener implements Listener {
    private final FailRunesPlugin plugin;
    public CombatListener(FailRunesPlugin plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        Player attacker = resolveAttacker(e.getDamager());
        if (attacker != null && e.getEntity() instanceof LivingEntity victim) {
            boolean crit = attacker.getFallDistance() > 0 && !attacker.isOnGround() && attacker.getVelocity().getY() < 0
                    && e.getDamager() instanceof Player;
            var item = attacker.getInventory().getItemInMainHand();
            plugin.engine().fire(RuneTrigger.ON_ATTACK, attacker, item, ctx -> { ctx.victim = victim; ctx.damage = e.getDamage(); ctx.critical = crit; });
            plugin.engine().fire(RuneTrigger.ON_HIT, attacker, item, ctx -> { ctx.victim = victim; ctx.damage = e.getDamage(); ctx.critical = crit; });
            if (crit) plugin.engine().fire(RuneTrigger.ON_CRITICAL, attacker, item, ctx -> { ctx.victim = victim; ctx.damage = e.getDamage(); });
            if (e.getDamager() instanceof Projectile) plugin.engine().fire(RuneTrigger.ON_PROJECTILE_HIT, attacker, item, ctx -> ctx.victim = victim);
            plugin.armorEngine().fireArmor(RuneTrigger.ON_HIT, attacker, ctx -> { ctx.victim = victim; ctx.damage = e.getDamage(); });

            plugin.history().record(attacker.getUniqueId(), new dev.failxos.failrunes.api.DamageHistory.Entry(
                    System.currentTimeMillis(), e.getCause().name(), attacker.getName(), nameOf(victim), null,
                    item.getType().name(), e.getFinalDamage(), crit, false));
            if (victim instanceof Player vp) plugin.history().record(vp.getUniqueId(), new dev.failxos.failrunes.api.DamageHistory.Entry(
                    System.currentTimeMillis(), e.getCause().name(), attacker.getName(), vp.getName(), null,
                    item.getType().name(), e.getFinalDamage(), crit, true));
        } else if (e.getEntity() instanceof Player victim) {
            plugin.armorEngine().fireArmor(RuneTrigger.ON_DAMAGE, victim, ctx -> ctx.damage = e.getDamage());
            plugin.history().record(victim.getUniqueId(), new dev.failxos.failrunes.api.DamageHistory.Entry(
                    System.currentTimeMillis(), e.getCause().name(), sourceName(e), victim.getName(), null, null, e.getFinalDamage(), false, true));
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onGenericDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && !(e instanceof EntityDamageByEntityEvent)) {
            plugin.armorEngine().fireArmor(RuneTrigger.ON_DAMAGE, p, ctx -> ctx.damage = e.getDamage());
            if (p.getHealth() - e.getFinalDamage() <= p.getHealth() * 0.2)
                plugin.armorEngine().fireArmor(RuneTrigger.ON_LOW_HEALTH, p, ctx -> {});
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onDeath(EntityDeathEvent e) {
        LivingEntity victim = e.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;
        var item = killer.getInventory().getItemInMainHand();
        plugin.engine().fire(RuneTrigger.ON_KILL, killer, item, ctx -> ctx.victim = victim);
        RuneTrigger t = victim instanceof Player ? RuneTrigger.ON_PLAYER_KILL : RuneTrigger.ON_MOB_KILL;
        plugin.engine().fire(t, killer, item, ctx -> ctx.victim = victim);
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getPlayer();
        plugin.engine().fire(RuneTrigger.ON_DEATH, p, p.getInventory().getItemInMainHand(), ctx -> {});
        net.kyori.adventure.text.Component base = e.deathMessage();
        if (plugin.settings().damageHistoryMax() > 0 && base != null) {
            net.kyori.adventure.text.Component link = plugin.lang().getComponent("damage-history.death-link", net.kyori.adventure.text.Component.empty())
                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/damagehistory"));
            e.deathMessage(base.appendSpace().append(link));
        }
    }
    private Player resolveAttacker(org.bukkit.entity.Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile pr && pr.getShooter() instanceof Player p) return p;
        return null;
    }
    private String sourceName(EntityDamageEvent e) { return e.getCause().name(); }
    private String nameOf(LivingEntity e) { return e instanceof Player p ? p.getName() : e.getName(); }
}
