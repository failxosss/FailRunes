package dev.failxos.failrunes.api.events;

import dev.failxos.failrunes.api.Rune;
import org.bukkit.entity.Player;

/** Player owns this rune for the first time. */
public class RuneDiscoverEvent extends RuneEvent {
    public RuneDiscoverEvent(Player player, Rune rune, int level) { super(player, rune, level); }
}
