package dev.failxos.failrunes.api;

public enum RuneTrigger {
    ON_ATTACK, ON_HIT, ON_KILL, ON_DEATH, ON_BLOCK_BREAK, ON_BLOCK_PLACE, ON_FISH, ON_DAMAGE, ON_PROJECTILE,
    ON_PROJECTILE_HIT, ON_ARMOR_EQUIP, ON_SPRINT, ON_JUMP, ON_FALL, ON_LOW_HEALTH, ON_HEAL, ON_CRITICAL,
    ON_MOB_KILL, ON_PLAYER_KILL,
    /** Fired only by other plugins through the API. */
    CUSTOM;

    public static RuneTrigger parse(String s, RuneTrigger def) {
        if (s == null) return def;
        try { return valueOf(s.trim().toUpperCase(java.util.Locale.ROOT)); } catch (IllegalArgumentException e) { return def; }
    }
}
