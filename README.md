# FailRunes

Complete Rune / custom-enchantment endgame progression system for Paper 1.21+, inspired by the publicly
documented gameplay structure of large-network Skyblock rune systems (Complex Skyblock style). Original
code, wording, IDs and rune content — see `docs/ORIGINALITY.md`.

## What's included

- **Infuser GUI** (`/infuser`) — browse every pool, purchase Rune Tomes with XP, buy Lucky Gems, identify,
  incinerate, cleanse.
- **750 rune definitions** across Common/Rare/Legendary/Mythical plus Tomb, Darkzone, Dojo, 6 Crate pools,
  5 Seasonal pools, Event and Boss pools — see `docs/RUNE_DATABASE.md` for how they were built.
- **Full application flow**: drag rune → item, Success/Fail/Critical Fail, Lucky Gems with a live-updating
  chance formula, Cleansing Wands, Rune Incinerator.
- **Rune Detector** + **Rune Identifier** for unidentified runes.
- **Modular effect engine** (30 built-in effect types) and **condition engine** (20+ condition keys), both
  extensible at runtime by other plugins with zero engine changes.
- Cooldowns tracked independently of held/worn state, with live action-bar popups.
- Active-rune **snapshotting** so switching gear mid-effect can't retroactively change an already-fired proc.
- Centralized **drop pipeline** (mining/logging/farming/digging/fishing) with MCMMO/WorldGuard hooks.
- Equipment **set bonuses**, damage history, rune collection tracking, full stats.
- SQLite (zero-config) or MySQL/MariaDB storage, all async.
- ItemsAdder, PlaceholderAPI, WorldGuard, mcMMO, MythicMobs, Vault integrations (all optional/soft-depend).
- Public API (`dev.failxos.failrunes.api`) for other plugins to register custom runes and effects.
- English + Czech language files.

## Building

This was developed and syntax/type-checked in a sandbox with **no access to Maven Central or the PaperMC
repository** — only GitHub was reachable, so the core engine and every plugin file were compiled directly
against the real **Paper API and Adventure source trees** pulled from GitHub (see `docs/BUILD_VERIFICATION.md`
for exactly what that did and didn't cover). To produce the actual `FailRunes.jar`, build normally on a
machine with internet access:

```bash
mvn clean package
```

The resulting jar is at `target/FailRunes-1.0.0.jar`. Drop it into `plugins/`.

Requires Java 21 and Paper 1.21+. Optional integrations (ItemsAdder, WorldGuard, Vault, MythicMobs,
PlaceholderAPI) only need to be installed on the server if you actually use them — FailRunes starts and
runs fully without any of them, with vanilla-material fallbacks for every item.

## First run

1. Start the server once to generate `plugins/FailRunes/*.yml`.
2. Set your XP prices per gamemode in `config.yml` (`gamemodes:`).
3. Point `database.yml` at MySQL if you don't want SQLite.
4. If you use ItemsAdder, fill in `items.yml` and see `itemsadder/README.md` for the resource-pack layout.
5. `/runeadmin reload` after any config change.

## Commands

| Command | Description |
|---|---|
| `/infuser`, `/runes`, `/rune` | Open the Infuser |
| `/runeinfo` | Runes on your held item |
| `/runeinfo search <term>` | Search all runes |
| `/runeprefs`, `/prefs` | Your sound/particle/message preferences |
| `/damagehistory` | Recent damage history |
| `/runecollection` | Your rune collection progress |
| `/runeadmin ...` | Admin tools (`failrunes.admin`) — see `docs/ADMIN.md` |

## Documentation

- `docs/CONFIGURATION.md` — every config file explained
- `docs/RUNE_DATABASE.md` — how the 750 runes were designed, and how to add your own
- `docs/CUSTOM_RUNES.md` — writing new runes in YAML, and the public Java API
- `docs/CUSTOM_EFFECTS.md` — registering new effect types / conditions from another plugin
- `docs/ADMIN.md` — admin commands and permissions
- `docs/BUILD_VERIFICATION.md` — exactly what was and wasn't verified to compile in this environment
