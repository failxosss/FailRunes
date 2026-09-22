package dev.failxos.failrunes.api;

import dev.failxos.failrunes.engine.PendingBreak;
import dev.failxos.failrunes.engine.Zone;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;

import java.util.Set;

/** Mutable state of one trigger firing; shared by all runes that proc during it. */
public final class RuneContext {
    public final Player player;
    public final RuneTrigger trigger;
    public Rune rune;
    public int level;
    public RuneSnapshot snapshot;
    public Zone zone;
    public LivingEntity victim;
    public Block block;
    public Set<String> tags = Set.of();
    public double damage;
    public double damageMultiplier = 1.0;
    public boolean critical;
    public boolean cancelEvent;
    public Projectile projectile;
    public Event event;
    public Item caught;
    /** Non-null for drop-producing triggers (block break, fishing, mob kill): effects register drop stages here. */
    public PendingBreak pending;

    public RuneContext(Player player, RuneTrigger trigger) { this.player = player; this.trigger = trigger; }
}
