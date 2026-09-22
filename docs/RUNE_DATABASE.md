# The 750-rune database

`src/main/resources/runes/*.yml` (one file per pool) ships 750 unique rune IDs, built in two layers:

1. **~90 hand-designed "signature" runes** implementing the publicly documented *gameplay concepts* named
   in the brief (Thunder, Lifesteal, Berserker, Scattershot, Tesla Coil, Telekinesis, Ancient Pickaxe,
   Clown Sword/Pickaxe, Decapitation, Puffer, Guards Up, Plagueweaver, Barricade, Saboteur, Greenhouse,
   Confidential Contraband, Demonic Axe, Christmas Helmet/Fishing Rod, Celestial Pickaxe, Ender Backpack,
   Pool Party, Zenith/Prismatic set bonuses, Phantom Blocks, The Naughtiest, Greedy Mobs, and more) —
   each with its own multi-effect mechanic, not a generic "+X% damage" stat stick.
2. **~660 template-generated runes** built by `tools/gen_runes.py` through systematic, mechanically
   meaningful combinatorics — element × weapon-class, gathering-bonus × resource × proficiency tier,
   armor-stat × piece × rarity, zone-themed relics for Tomb/Darkzone/Dojo, and per-pool seasonal/crate/boss
   variants. Every combination changes an actual game mechanic (different trigger, different effect
   parameters, different equipment slot) — none are copy-pasted duplicates, but a "Greater Ember Blade" and
   an "Ascended Ember Blade" are, honestly, power-tiered variations on one idea rather than 660 independently
   hand-authored fantasy mechanics. Re-run `tools/gen_runes.py` any time to regenerate or extend the set.

`docs/CUSTOM_RUNES.md` explains the YAML schema if you want to hand-write more, and every generated file is
plain, readable YAML you're free to edit directly — `/runeadmin reload` picks up changes immediately.
