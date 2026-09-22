package dev.failxos.failrunes.api.events;

import dev.failxos.failrunes.api.Rune;
import org.bukkit.entity.Player;

/** A cooldown is about to start; seconds can be changed. */
public class RuneCooldownEvent extends RuneEvent {
    private double seconds;
    public RuneCooldownEvent(Player player, Rune rune, int level, double seconds) { super(player, rune, level); this.seconds = seconds; }
    public double getSeconds() { return seconds; }
    public void setSeconds(double s) { seconds = s; }
}
