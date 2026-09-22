# ItemsAdder resource pack skeleton for FailRunes

This is a starting skeleton, not a finished resource pack — you still need to draw/commission the actual
textures. Namespace: `failrunes`.

```
contents/
  failrunes/
    config.yml          # points ItemsAdder at items/ below
    items/
      runes.yml          # one CustomStack entry per icon id referenced in items.yml
    resourcepack/
    textures/item/        # 16x16 PNGs, one per icon
    models/item/           # matching item models
```

Required icon IDs (see `plugins/FailRunes/items.yml` for the full, editable list):

```
failrunes:common_rune
failrunes:rare_rune
failrunes:legendary_rune
failrunes:mythical_rune
failrunes:unidentified_rune
failrunes:lucky_gem
failrunes:rune_identifier
failrunes:cleansing_wand
failrunes:rune_incinerator
failrunes:rune_detector
failrunes:hidden_treasure
```

Plus one `failrunes:rune_<id>` per individual rune if you want unique per-rune icons instead of falling
back to the shared rarity icon (Paper vanilla materials are used automatically for anything not defined
here — the plugin never requires ItemsAdder to function).
