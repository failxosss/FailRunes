package dev.failxos.failrunes.api;

import java.util.*;

/**
 * Immutable rune definition. The {@code id} is the stable identifier (never the display name).
 * Build with {@link #builder(String)}; register with {@link RuneManager#register(Rune)}.
 */
public record Rune(String id, String name, String description, RuneRarity rarity, RuneCategory category, String pool,
                   int maxLevel, int overmaxLevel, double weight, Set<String> equipment, RuneTrigger trigger,
                   Scaling chance, Scaling cooldown, List<RuneCondition> conditions, List<String> conditionStrings,
                   List<EffectSpec> effects, Set<String> conflicts, Set<String> blockTags, Set<String> disabledZones,
                   String sound, String particle, String icon, String symbol, Boolean criticalFailDestroy,
                   Double incinerateValue, boolean messageDefault, boolean original, String source) {

    public RuneLevel level(int n) {
        return new RuneLevel(n, chance.at(n), Math.max(0, cooldown.at(n)), n > maxLevel);
    }
    public int cap(boolean overmaxEnabled) { return overmaxEnabled ? Math.max(maxLevel, overmaxLevel) : maxLevel; }
    public boolean isDetector() { return effects.stream().anyMatch(e -> e.type().equals("DETECT")); }

    public static Builder builder(String id) { return new Builder(id); }

    public static final class Builder {
        private final String id;
        private String name, description = "", pool = "special", sound = "entity.experience_orb.pickup",
                particle = "CRIT", icon = "", symbol = "✦", source = "api";
        private RuneRarity rarity = RuneRarity.COMMON;
        private RuneCategory category = RuneCategory.SPECIAL;
        private int maxLevel = 5, overmaxLevel = 0;
        private double weight = 10;
        private final Set<String> equipment = new LinkedHashSet<>(), conflicts = new LinkedHashSet<>(),
                blockTags = new LinkedHashSet<>(), disabledZones = new LinkedHashSet<>();
        private RuneTrigger trigger = RuneTrigger.ON_HIT;
        private Scaling chance = Scaling.of(10), cooldown = Scaling.of(0);
        private final List<RuneCondition> conditions = new ArrayList<>();
        private final List<String> conditionStrings = new ArrayList<>();
        private final List<EffectSpec> effects = new ArrayList<>();
        private Boolean critDestroy;
        private Double incinerate;
        private boolean messageDefault = true, original = false;

        private Builder(String id) { this.id = id.toLowerCase(Locale.ROOT); this.name = id; }
        public Builder name(String v) { name = v; return this; }
        public Builder description(String v) { description = v; return this; }
        public Builder rarity(RuneRarity v) { rarity = v; return this; }
        public Builder category(RuneCategory v) { category = v; return this; }
        public Builder pool(String v) { pool = v; return this; }
        public Builder maxLevel(int v) { maxLevel = Math.max(1, v); return this; }
        public Builder overmaxLevel(int v) { overmaxLevel = v; return this; }
        public Builder weight(double v) { weight = v; return this; }
        public Builder equipment(String... v) { equipment.addAll(Arrays.asList(v)); return this; }
        public Builder equipment(Collection<String> v) { equipment.addAll(v); return this; }
        public Builder trigger(RuneTrigger v) { trigger = v; return this; }
        public Builder chance(Scaling v) { chance = v; return this; }
        public Builder cooldown(Scaling v) { cooldown = v; return this; }
        public Builder condition(RuneCondition c) { conditions.add(c); return this; }
        public Builder conditionString(String s) { conditionStrings.add(s); return this; }
        public Builder effect(EffectSpec e) { effects.add(e); return this; }
        public Builder conflicts(Collection<String> v) { conflicts.addAll(v); return this; }
        public Builder blockTags(Collection<String> v) { blockTags.addAll(v); return this; }
        public Builder disabledZones(Collection<String> v) { disabledZones.addAll(v); return this; }
        public Builder sound(String v) { sound = v; return this; }
        public Builder particle(String v) { particle = v; return this; }
        public Builder icon(String v) { icon = v; return this; }
        public Builder symbol(String v) { symbol = v; return this; }
        public Builder criticalFailDestroy(Boolean v) { critDestroy = v; return this; }
        public Builder incinerateValue(Double v) { incinerate = v; return this; }
        public Builder messageDefault(boolean v) { messageDefault = v; return this; }
        public Builder original(boolean v) { original = v; return this; }
        public Builder source(String v) { source = v; return this; }

        public Rune build() {
            if (equipment.isEmpty()) equipment.add("ANY");
            String ic = icon.isEmpty() ? "failrunes:rune_" + id : icon;
            return new Rune(id, name, description, rarity, category, pool, maxLevel, overmaxLevel, weight,
                    Set.copyOf(equipment), trigger, chance, cooldown, List.copyOf(conditions), List.copyOf(conditionStrings),
                    List.copyOf(effects), Set.copyOf(conflicts), Set.copyOf(blockTags), Set.copyOf(disabledZones),
                    sound, particle, ic, symbol, critDestroy, incinerate, messageDefault, original, source);
        }
    }
}
