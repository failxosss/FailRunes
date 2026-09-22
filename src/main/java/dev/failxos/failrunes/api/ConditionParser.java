package dev.failxos.failrunes.api;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Parses condition strings from runes.yml (e.g. "health-below:30", "sneaking", "world:tomb_1")
 * into {@link RuneCondition}s. Other plugins can register their own keys.
 */
public final class ConditionParser {
    private ConditionParser() {}
    private static final Map<String, BiFunction<String, RuneCondition, RuneCondition>> FACTORIES = new HashMap<>();

    public static void register(String key, BiFunction<String, RuneCondition, RuneCondition> factory) {
        FACTORIES.put(key.toLowerCase(Locale.ROOT), factory);
    }
    public static boolean has(String key) { return FACTORIES.containsKey(key.toLowerCase(Locale.ROOT)); }

    /** "key:arg" or bare "key". Falls back to an always-true condition (with a warning) for unknown keys. */
    public static RuneCondition parse(String raw, java.util.function.BiConsumer<String, String> onUnknown) {
        int i = raw.indexOf(':');
        String key = (i < 0 ? raw : raw.substring(0, i)).trim().toLowerCase(Locale.ROOT);
        String arg = i < 0 ? "" : raw.substring(i + 1).trim();
        BiFunction<String, RuneCondition, RuneCondition> f = FACTORIES.get(key);
        if (f == null) { if (onUnknown != null) onUnknown.accept(key, raw); return ctx -> true; }
        return f.apply(arg, null);
    }
}
