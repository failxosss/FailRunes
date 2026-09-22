package dev.failxos.failrunes.api.events;

import dev.failxos.failrunes.api.Rune;
import org.bukkit.entity.Player;

/** Before an application is rolled (cancellable). */
public class RuneApplyEvent extends RuneEvent {
    public RuneApplyEvent(Player player, Rune rune, int level) { super(player, rune, level); }
}
