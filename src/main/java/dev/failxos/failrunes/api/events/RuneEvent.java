package dev.failxos.failrunes.api.events;

import dev.failxos.failrunes.api.Rune;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Base of all FailRunes events (subclasses share one handler list; Bukkit filters by class). */
public abstract class RuneEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Rune rune;
    private final int level;
    private boolean cancelled;

    protected RuneEvent(Player player, Rune rune, int level) { this.player = player; this.rune = rune; this.level = level; }
    public Player getPlayer() { return player; }
    public Rune getRune() { return rune; }
    public int getLevel() { return level; }
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean c) { cancelled = c; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
