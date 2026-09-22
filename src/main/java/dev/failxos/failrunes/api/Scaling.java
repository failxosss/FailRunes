package dev.failxos.failrunes.api;

import java.util.Map;

/** value(level) = base + perLevel * (level - 1). YAML: a plain number or {base:, per-level:}. */
public record Scaling(double base, double perLevel) {
    public double at(int level) { return base + perLevel * Math.max(0, level - 1); }
    public static Scaling of(double v) { return new Scaling(v, 0); }

    public static Scaling parse(Object o, Scaling def) {
        if (o instanceof Number n) return of(n.doubleValue());
        if (o instanceof Map<?, ?> m) return new Scaling(num(m.get("base")), num(m.get("per-level")));
        if (o instanceof String s) {
            try { return of(Double.parseDouble(s)); } catch (NumberFormatException e) { return def; }
        }
        return def;
    }
    private static double num(Object o) { return o instanceof Number n ? n.doubleValue() : 0; }
}
