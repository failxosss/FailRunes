package dev.failxos.failrunes.api;

import java.util.Map;
import java.util.UUID;

/** Immutable picture of the player's active runes/sets/zone at activation time. */
public record RuneSnapshot(UUID player, Map<String, Integer> runes, Map<String, Integer> setPieces,
                           String zone, String heldItem, double health, long time) {
    public int level(String runeId) { return runes.getOrDefault(runeId, 0); }
}
