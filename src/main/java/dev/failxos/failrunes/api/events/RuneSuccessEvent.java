package dev.failxos.failrunes.api.events;

import dev.failxos.failrunes.api.Rune;
import org.bukkit.entity.Player;

/** Application succeeded. */
public class RuneSuccessEvent extends RuneEvent {
    public RuneSuccessEvent(Player player, Rune rune, int level) { super(player, rune, level); }
}
