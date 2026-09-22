package dev.failxos.failrunes.api.events;

import dev.failxos.failrunes.api.Rune;
import org.bukkit.entity.Player;

/** Application failed; rune consumed, item kept. */
public class RuneFailEvent extends RuneEvent {
    public RuneFailEvent(Player player, Rune rune, int level) { super(player, rune, level); }
}
