# Admin commands (`failrunes.admin`)

| Command | Description |
|---|---|
| `/runeadmin give <player> <rune> [level]` | Give an identified rune item |
| `/runeadmin tome <player> <rarity>` | Give a Rune Tome |
| `/runeadmin unidentified [player]` | Give an Unidentified Rune |
| `/runeadmin gem <player> <amount>` | Give Lucky Gems |
| `/runeadmin wand [player] [amount]` | Give Cleansing Wands |
| `/runeadmin inspect [player]` | Show runes on the target's held item |
| `/runeadmin list` | Show how many runes are loaded |
| `/runeadmin reload` | Reload config, runes, pools, zones, sets, messages |
| `/runeadmin debug` | Toggle verbose proc/effect logging |

## Permissions

- `failrunes.use` (default: true) — every player command
- `failrunes.admin` (default: op) — `/runeadmin`
