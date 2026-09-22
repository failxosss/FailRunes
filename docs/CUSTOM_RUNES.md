# Writing custom runes

## Via YAML

Drop a file under `plugins/FailRunes/runes/` (or add to `runes.yml`) with this shape:

```yaml
runes:
  my_rune:
    name: "My Rune"
    description: "What it does, shown in tooltips."
    rarity: RARE                 # COMMON | RARE | LEGENDARY | MYTHICAL
    category: COMBAT             # see RuneCategory
    pool: rare                   # which Infuser pool/tome this belongs to
    trigger: ON_HIT               # see RuneTrigger
    equipment: [SWORD, AXE]       # or [ANY], [WEAPON], [TOOL], [ARMOR]
    max-level: 5
    weight: 20                    # loot-table weight within its pool
    chance: {base: 15, per-level: 3}
    cooldown: {base: 5, per-level: -0.3}
    conditions: ["health-below:30", "sneaking"]
    conflicts: [thunder]
    effects:
      - {type: DAMAGE, target: TARGET, amount: {base: 4, per-level: 1}}
      - {type: PARTICLE, particle: FLAME, count: 10}
```

`/runeadmin reload` picks it up immediately.

## Via the Java API

```java
Rune rune = Rune.builder("my_rune")
    .name("My Rune").rarity(RuneRarity.RARE).category(RuneCategory.COMBAT)
    .trigger(RuneTrigger.ON_HIT).equipment("SWORD")
    .chance(new Scaling(15, 3))
    .effect(EffectSpec.parse(Map.of("type", "damage", "amount", 4)))
    .source("my-plugin").build();
RuneManager.register(rune);
```

Registered runes survive `/runeadmin reload` (only `source: "config"` runes are cleared on reload).

See `docs/CUSTOM_EFFECTS.md` for adding new effect types and condition keys.
