package dev.failxos.failrunes.api.events;

import dev.failxos.failrunes.api.Rune;
import org.bukkit.entity.Player;

/** A rune is being removed from an item with a Cleansing Wand (cancellable). */
public class RuneCleanseEvent extends RuneEvent {
    public RuneCleanseEvent(Player player, Rune rune, int level) { super(player, rune, level); }
}
