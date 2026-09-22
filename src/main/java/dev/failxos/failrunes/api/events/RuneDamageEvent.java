package dev.failxos.failrunes.api.events;

import dev.failxos.failrunes.api.Rune;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/** A rune is about to deal damage to a target; damage can be changed. */
public class RuneDamageEvent extends RuneEvent {
    private final LivingEntity target;
    private double damage;
    public RuneDamageEvent(Player player, Rune rune, int level, LivingEntity target, double damage) {
        super(player, rune, level); this.target = target; this.damage = damage;
    }
    public LivingEntity getTarget() { return target; }
    public double getDamage() { return damage; }
    public void setDamage(double d) { damage = d; }
}
