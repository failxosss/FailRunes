package dev.failxos.failrunes.api.events;

import dev.failxos.failrunes.api.Rune;
import org.bukkit.entity.Player;

/** A rune is being incinerated for XP (cancellable). */
public class RuneIncinerateEvent extends RuneEvent {
    public RuneIncinerateEvent(Player player, Rune rune, int level) { super(player, rune, level); }
}
