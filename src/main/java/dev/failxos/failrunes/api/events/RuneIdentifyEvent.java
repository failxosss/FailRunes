package dev.failxos.failrunes.api.events;

import dev.failxos.failrunes.api.Rune;
import org.bukkit.entity.Player;

/** An unidentified rune is being identified (cancellable). */
public class RuneIdentifyEvent extends RuneEvent {
    public RuneIdentifyEvent(Player player, Rune rune, int level) { super(player, rune, level); }
}
