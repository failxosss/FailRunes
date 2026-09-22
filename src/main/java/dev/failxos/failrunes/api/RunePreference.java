package dev.failxos.failrunes.api;

/** Bit flags stored per player and per rune ("*" = defaults / global sound switches). */
public final class RunePreference {
    private RunePreference() {}
    public static final int SOUNDS = 1, PARTICLES = 2, MESSAGES = 4, COOLDOWN_POPUP = 8, ACTION_BAR = 16,
            TITLES = 32, CHAT = 64, VISUAL = 128;
    // global sound switches (stored under "*")
    public static final int SND_ACTIVATION = 256, SND_SUCCESS = 512, SND_FAIL = 1024, SND_CRITICAL = 2048,
            SND_COOLDOWN = 4096, SND_DISCOVERY = 8192;
    public static final int PER_RUNE_MASK = 255;
    public static final int DEFAULT = SOUNDS | PARTICLES | MESSAGES | COOLDOWN_POPUP | CHAT | VISUAL
            | SND_ACTIVATION | SND_SUCCESS | SND_FAIL | SND_CRITICAL | SND_COOLDOWN | SND_DISCOVERY;
    public static boolean has(int flags, int bit) { return (flags & bit) != 0; }
}
