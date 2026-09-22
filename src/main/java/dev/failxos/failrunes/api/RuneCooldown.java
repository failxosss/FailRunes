package dev.failxos.failrunes.api;

public record RuneCooldown(String runeId, long expiresAtMillis) {
    public double remainingSeconds(long now) { return Math.max(0, (expiresAtMillis - now) / 1000.0); }
    public boolean active(long now) { return expiresAtMillis > now; }
}
