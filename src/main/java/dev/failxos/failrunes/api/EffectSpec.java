package dev.failxos.failrunes.api;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** One effect entry of a rune: a type plus numeric (scalable) and string parameters. */
public final class EffectSpec {
    private final String type;
    private final Map<String, Scaling> nums = new HashMap<>();
    private final Map<String, String> strs = new HashMap<>();

    public EffectSpec(String type) { this.type = type.toUpperCase(Locale.ROOT); }

    public static EffectSpec parse(Map<?, ?> m) {
        EffectSpec s = new EffectSpec(String.valueOf(m.get("type")));
        for (Map.Entry<?, ?> e : m.entrySet()) {
            String k = String.valueOf(e.getKey());
            if (k.equals("type")) continue;
            Object v = e.getValue();
            if (v instanceof Number || v instanceof Map) s.nums.put(k, Scaling.parse(v, Scaling.of(0)));
            else if (v != null) s.strs.put(k, String.valueOf(v));
        }
        return s;
    }
    public String type() { return type; }
    public boolean has(String k) { return nums.containsKey(k) || strs.containsKey(k); }
    public double num(String k, double def, int level) { Scaling s = nums.get(k); return s == null ? def : s.at(level); }
    public String str(String k, String def) { return strs.getOrDefault(k, def); }
    public boolean bool(String k) { return "true".equalsIgnoreCase(strs.get(k)); }
    public EffectSpec put(String k, Scaling s) { nums.put(k, s); return this; }
    public EffectSpec put(String k, String v) { strs.put(k, v); return this; }
    public Map<String, Scaling> numbers() { return nums; }
    public Map<String, String> strings() { return strs; }
}
