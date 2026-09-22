package dev.failxos.failrunes.api.events;

import dev.failxos.failrunes.api.Rune;
import org.bukkit.entity.Player;

/** Critical failure; rune consumed, item may be destroyed. */
public class RuneCriticalFailEvent extends RuneEvent {
    public RuneCriticalFailEvent(Player player, Rune rune, int level) { super(player, rune, level); }
}
