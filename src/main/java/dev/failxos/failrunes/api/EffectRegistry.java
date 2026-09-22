package dev.failxos.failrunes.api;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Pluggable effect types. Register with {@code EffectRegistry.register("MY_EFFECT", (ctx, spec) -> {...})}. */
public final class EffectRegistry {
    private EffectRegistry() {}
    private static final Map<String, RuneEffect> EFFECTS = new HashMap<>();

    public static void register(String type, RuneEffect effect) { EFFECTS.put(type.toUpperCase(Locale.ROOT), effect); }
    public static RuneEffect get(String type) { return type == null ? null : EFFECTS.get(type.toUpperCase(Locale.ROOT)); }
    public static boolean has(String type) { return type != null && EFFECTS.containsKey(type.toUpperCase(Locale.ROOT)); }
    public static java.util.Set<String> registered() { return EFFECTS.keySet(); }
}
