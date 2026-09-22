package dev.failxos.failrunes.api;

/** Resolved numbers for one level of a rune (used by GUIs to show scaling progression). */
public record RuneLevel(int level, double chance, double cooldown, boolean overmax) {}
