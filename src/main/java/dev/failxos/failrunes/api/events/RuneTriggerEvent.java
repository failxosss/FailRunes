package dev.failxos.failrunes.api.events;

import dev.failxos.failrunes.api.Rune;
import org.bukkit.entity.Player;

/** A rune is about to execute its effects (cancellable). */
public class RuneTriggerEvent extends RuneEvent {
    public RuneTriggerEvent(Player player, Rune rune, int level) { super(player, rune, level); }
}
