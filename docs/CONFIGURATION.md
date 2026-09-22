# Configuration files

All under `plugins/FailRunes/` after first run.

| File | Purpose |
|---|---|
| `config.yml` | Language, default gamemode, per-gamemode XP prices, Lucky Gem formula, overmax toggle, Rune Detector tuning, damage-history limits, block tags |
| `rarities.yml` | Display color / weight multiplier per core rarity |
| `pools.yml` | Every special pool's visibility/purchasability/source/drop-method/tier |
| `runes/*.yml` | The 750 rune definitions, one file per pool (see `RUNE_DATABASE.md`) |
| `loot-tables.yml` | Optional per-rune weight overrides within a pool |
| `zones.yml` | Per-zone proc/cooldown/damage/loot multipliers and enabled/disabled runes |
| `sets.yml` | Equipment set bonuses (2/3/4-piece thresholds) |
| `preferences.yml` | Default per-player preference flags |
| `items.yml` | ItemsAdder namespace/IDs for every FailRunes item |
| `effects.yml` | Informational list of built-in effect types |
| `compatibility.yml` | Toggle each optional integration independently of whether it's installed |
| `database.yml` | SQLite (default) or MySQL/MariaDB connection |
| `lang/en_US.yml`, `lang/cs_CZ.yml` | All player-facing text |

Everything reloads with `/runeadmin reload` except the database connection.
