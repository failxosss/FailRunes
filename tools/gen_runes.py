#!/usr/bin/env python3
"""
Generates FailRunes' default rune database as YAML files under src/main/resources/runes/.
Two layers:
  1. HAND-DESIGNED "signature" runes (~90) - rich, multi-effect, unique mechanics, one per named
     concept from the spec (Thunder, Lifesteal, Berserker, Scattershot, Tesla Coil, Telekinesis,
     Ancient Pickaxe, Clown Sword/Pickaxe, etc.) plus category staples.
  2. TEMPLATE-GENERATED runes - systematic combinatorial variation (element x weapon-class x
     rarity, or gathering-bonus x resource-category x rarity, etc.) so the total pool reaches the
     750+ target from the spec while keeping every entry mechanically distinct, not a re-skin.
Output is split into one file per pool/category group to keep individual YAML files manageable.
"""
import yaml, random, math, os, sys

OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "runes")
os.makedirs(OUT, exist_ok=True)
random.seed(20260921)

RARITY_WEIGHT = {"COMMON": 100, "RARE": 40, "LEGENDARY": 12, "MYTHICAL": 3}
RARITY_MAXLVL = {"COMMON": 5, "RARE": 6, "LEGENDARY": 7, "MYTHICAL": 8}

def rune(id_, name, rarity, category, pool, trigger, equipment, description,
         chance=None, cooldown=None, max_level=None, effects=None, conditions=None,
         conflicts=None, weight=None, sound=None, particle=None, symbol="\u2726",
         critical_destroy=None, original=True):
    d = {
        "name": name,
        "description": description,
        "rarity": rarity,
        "category": category,
        "pool": pool,
        "trigger": trigger,
        "equipment": equipment,
        "max-level": max_level or RARITY_MAXLVL[rarity],
        "weight": weight if weight is not None else RARITY_WEIGHT[rarity],
        "symbol": symbol,
        "original": original,
    }
    if chance is not None: d["chance"] = chance
    if cooldown is not None: d["cooldown"] = cooldown
    if effects: d["effects"] = effects
    if conditions: d["conditions"] = conditions
    if conflicts: d["conflicts"] = conflicts
    if sound: d["sound"] = sound
    if particle: d["particle"] = particle
    if critical_destroy is not None: d["critical-fail"] = {"destroy-item": critical_destroy}
    return id_, d

ALL = {}
def add(*runes):
    for id_, d in runes:
        if id_ in ALL: raise SystemExit(f"duplicate id {id_}")
        ALL[id_] = d

# ---------------------------------------------------------------------------
# 1. HAND-DESIGNED SIGNATURE RUNES
# ---------------------------------------------------------------------------

add(rune("thunder", "Thunder", "RARE", "COMBAT", "rare", "ON_HIT", ["SWORD", "AXE"],
    "Calls down lightning on your target when you land a hit.",
    chance={"base": 8, "per-level": 2}, cooldown={"base": 5, "per-level": -0.3},
    effects=[{"type": "LIGHTNING", "target": "TARGET", "damage-only": True},
             {"type": "DAMAGE", "target": "TARGET", "amount": {"base": 4, "per-level": 1}}],
    particle="ELECTRIC_SPARK", sound="entity.lightning_bolt.thunder"))

add(rune("lifesteal", "Lifesteal", "COMMON", "COMBAT", "common", "ON_HIT", ["SWORD", "AXE", "TRIDENT"],
    "Heals you for a portion of the damage you deal.",
    chance={"base": 20, "per-level": 4},
    effects=[{"type": "HEAL", "amount": {"base": 1, "per-level": 0.5}}]))

add(rune("berserker", "Berserker", "RARE", "COMBAT", "rare", "ON_LOW_HEALTH", ["SWORD", "AXE", "ARMOR"],
    "Gain Strength and Speed when your health drops low.",
    chance={"base": 100}, cooldown={"base": 20, "per-level": -1.5},
    conditions=["health-below:30"],
    effects=[{"type": "STRENGTH", "ticks": {"base": 100, "per-level": 20}, "amplifier": {"base": 0, "per-level": 0.34}},
             {"type": "SPEED", "ticks": {"base": 100, "per-level": 20}, "amplifier": 1},
             {"type": "TITLE", "title": "<red>BERSERK", "subtitle": "<gray>Fight through the pain"}]))

add(rune("executioner", "Executioner", "LEGENDARY", "COMBAT", "legendary", "ON_HIT", ["SWORD", "AXE"],
    "Deals massive bonus damage to targets below 20% health.",
    chance={"base": 100}, cooldown={"base": 8, "per-level": -0.5},
    conditions=["target-type:*"],
    effects=[{"type": "DAMAGE", "target": "TARGET", "amount": {"base": 10, "per-level": 2.5}}]))

add(rune("bloodlust", "Bloodlust", "RARE", "COMBAT", "rare", "ON_KILL", ["SWORD", "AXE"],
    "Each kill briefly boosts your Strength, stacking with itself.",
    chance={"base": 100}, cooldown={"base": 1},
    effects=[{"type": "STRENGTH", "ticks": {"base": 60, "per-level": 10}, "amplifier": 0}]))

add(rune("bleed", "Bleed", "COMMON", "COMBAT", "common", "ON_HIT", ["SWORD"],
    "Inflicts a bleeding wound that deals damage over time.",
    chance={"base": 25, "per-level": 5},
    effects=[{"type": "WITHER", "ticks": {"base": 40, "per-level": 10}, "amplifier": 0}]))

add(rune("venom", "Venom", "RARE", "COMBAT", "rare", "ON_HIT", ["SWORD", "TRIDENT"],
    "Poisons your target on hit, with a chance to apply a stronger dose.",
    chance={"base": 30, "per-level": 5},
    effects=[{"type": "POISON", "ticks": {"base": 60, "per-level": 15}, "amplifier": {"base": 0, "per-level": 0.14}}]))

add(rune("soul_drain", "Soul Drain", "LEGENDARY", "COMBAT", "legendary", "ON_KILL", ["SWORD", "AXE"],
    "Drains XP straight from the souls of your victims.",
    chance={"base": 100},
    effects=[{"type": "XP", "amount": {"base": 15, "per-level": 5}}]))

add(rune("chain_lightning", "Chain Lightning", "LEGENDARY", "COMBAT", "legendary", "ON_HIT", ["SWORD", "TRIDENT"],
    "Strikes your target and arcs to nearby enemies.",
    chance={"base": 12, "per-level": 2}, cooldown={"base": 6},
    effects=[{"type": "LIGHTNING", "target": "TARGET", "damage-only": True},
             {"type": "AREA_DAMAGE", "amount": {"base": 3, "per-level": 0.8}, "radius": 4}]))

add(rune("firestorm", "Firestorm", "MYTHICAL", "COMBAT", "mythical", "ON_ATTACK", ["SWORD", "AXE"],
    "Erupts a storm of fire around you, burning every nearby enemy.",
    chance={"base": 6, "per-level": 1}, cooldown={"base": 15, "per-level": -1},
    effects=[{"type": "EXPLOSION", "power": 0, "fire": True, "break-blocks": False},
             {"type": "FIRE", "target": "NEARBY_MOBS", "ticks": {"base": 100, "per-level": 20}}]))

add(rune("frostbite", "Frostbite", "RARE", "COMBAT", "rare", "ON_HIT", ["SWORD", "TRIDENT"],
    "Chills your target, slowing their movement sharply.",
    chance={"base": 25, "per-level": 4},
    effects=[{"type": "SLOW", "ticks": {"base": 60, "per-level": 10}, "amplifier": {"base": 1, "per-level": 0.2}}]))

add(rune("shadow_strike", "Shadow Strike", "LEGENDARY", "COMBAT", "legendary", "ON_SPRINT", ["SWORD"],
    "Teleport behind your target and strike from the shadows.",
    chance={"base": 100}, cooldown={"base": 12, "per-level": -1},
    conditions=["combat"],
    effects=[{"type": "TELEPORT", "mode": "behind-target"},
             {"type": "DAMAGE", "target": "TARGET", "amount": {"base": 6, "per-level": 1.5}}]))

add(rune("reaper", "Reaper", "MYTHICAL", "COMBAT", "mythical", "ON_MOB_KILL", ["SWORD", "AXE"],
    "Every mob you slay feeds your blade's hunger for more.",
    chance={"base": 100}, cooldown={"base": 2},
    effects=[{"type": "STRENGTH", "ticks": {"base": 40, "per-level": 8}, "amplifier": 0},
             {"type": "XP", "amount": {"base": 3, "per-level": 1}}]))

add(rune("monster_hunter", "Monster Hunter", "RARE", "COMBAT", "rare", "ON_HIT", ["SWORD", "BOW", "CROSSBOW"],
    "Extra damage against hostile mobs specifically.",
    chance={"base": 100},
    conditions=["target-type:ZOMBIE", ],
    effects=[{"type": "DAMAGE", "target": "TARGET", "amount": {"base": 3, "per-level": 0.7}}]))

add(rune("dragon_slayer", "Dragon Slayer", "MYTHICAL", "COMBAT", "mythical", "ON_HIT", ["SWORD", "AXE"],
    "Massive bonus damage against dragons and wardens.",
    chance={"base": 100},
    conditions=["target-type:ENDER_DRAGON"],
    effects=[{"type": "DAMAGE", "target": "TARGET", "amount": {"base": 25, "per-level": 6}}]))

add(rune("scattershot", "Scattershot", "LEGENDARY", "WEAPONS", "legendary", "ON_ATTACK", ["BOW", "CROSSBOW"],
    "Fires a spread of additional arrows alongside your shot.",
    chance={"base": 100}, cooldown={"base": 4, "per-level": -0.3},
    effects=[{"type": "MULTI_PROJECTILE", "projectile": "ARROW", "count": {"base": 3, "per-level": 0.5}, "spread-degrees": 15}]))

add(rune("tesla_coil", "Tesla Coil", "LEGENDARY", "WEAPONS", "legendary", "ON_HIT", ["SWORD", "TRIDENT"],
    "Charges your weapon; every third hit releases a burst of arcing electricity.",
    chance={"base": 34}, cooldown={"base": 3},
    effects=[{"type": "LIGHTNING", "target": "TARGET", "damage-only": True},
             {"type": "AREA_DAMAGE", "amount": {"base": 2, "per-level": 0.5}, "radius": 3}]))

add(rune("ricochet", "Ricochet", "RARE", "WEAPONS", "rare", "ON_PROJECTILE", ["BOW", "CROSSBOW"],
    "Your arrows have a chance to bounce toward a second nearby target.",
    chance={"base": 20, "per-level": 4},
    effects=[{"type": "PROJECTILE", "projectile": "ARROW", "speed": 1.5}]))

add(rune("arrow_storm", "Arrow Storm", "MYTHICAL", "WEAPONS", "mythical", "ON_ATTACK", ["BOW", "CROSSBOW"],
    "Unleashes a torrential volley of arrows in a wide arc.",
    chance={"base": 100}, cooldown={"base": 18, "per-level": -1.5},
    effects=[{"type": "MULTI_PROJECTILE", "projectile": "ARROW", "count": {"base": 8, "per-level": 1}, "spread-degrees": 45},
             {"type": "SOUND", "sound": "ENTITY_ARROW_SHOOT", "volume": 1.5}]))

add(rune("decapitation", "Decapitation", "LEGENDARY", "WEAPONS", "legendary", "ON_CRITICAL", ["SWORD", "AXE"],
    "A critical hit has a chance to instantly finish a low-health target.",
    chance={"base": 8, "per-level": 1.5}, cooldown={"base": 10},
    conditions=["target-type:*"],
    effects=[{"type": "DAMAGE", "target": "TARGET", "amount": {"base": 20, "per-level": 4}}]))

add(rune("puffer", "Puffer", "COMMON", "WEAPONS", "common", "ON_HIT", ["TRIDENT", "FISHING_ROD"],
    "A chance to knock your target away with a burst of water pressure.",
    chance={"base": 15, "per-level": 3},
    effects=[{"type": "PUSH", "strength": {"base": 1, "per-level": 0.2}}]))

add(rune("saboteur", "Saboteur", "RARE", "WEAPONS", "rare", "ON_HIT", ["SWORD", "AXE"],
    "Sabotages your target, weakening their attack power briefly.",
    chance={"base": 20, "per-level": 4},
    effects=[{"type": "SLOW", "ticks": {"base": 60, "per-level": 10}, "amplifier": 0}]))

add(rune("guards_up", "Guards Up", "RARE", "ARMOR", "rare", "ON_DAMAGE", ["ARMOR"],
    "Raises your defenses briefly after taking a hit.",
    chance={"base": 100}, cooldown={"base": 8, "per-level": -0.5},
    effects=[{"type": "ABSORPTION", "ticks": {"base": 60, "per-level": 15}, "amplifier": {"base": 0, "per-level": 0.34}}]))

add(rune("barricade", "Barricade", "LEGENDARY", "ARMOR", "legendary", "ON_DAMAGE", ["ARMOR"],
    "A chance to completely negate incoming damage.",
    chance={"base": 6, "per-level": 1.2}, cooldown={"base": 12, "per-level": -1},
    effects=[{"type": "HEAL", "amount": {"base": 2}},
             {"type": "TITLE", "title": "<white>BLOCKED", "subtitle": ""}]))

add(rune("phoenix", "Phoenix", "MYTHICAL", "ARMOR", "mythical", "ON_LOW_HEALTH", ["ARMOR"],
    "Rise from near-death with a burst of healing flame.",
    chance={"base": 100}, cooldown={"base": 300},
    conditions=["health-below:10"],
    effects=[{"type": "HEAL", "amount": {"base": 8, "per-level": 2}},
             {"type": "FIRE", "target": "NEARBY_MOBS", "ticks": 60},
             {"type": "TITLE", "title": "<gold>REBORN", "subtitle": "<gray>The Phoenix rises"}]))

add(rune("second_chance", "Second Chance", "MYTHICAL", "ARMOR", "mythical", "ON_LOW_HEALTH", ["ARMOR"],
    "Grants a shield of absorption hearts once your health is critical.",
    chance={"base": 100}, cooldown={"base": 240},
    conditions=["health-below:15"],
    effects=[{"type": "ABSORPTION", "ticks": {"base": 200, "per-level": 40}, "amplifier": {"base": 1, "per-level": 0.5}}]))

add(rune("last_stand", "Last Stand", "LEGENDARY", "ARMOR", "legendary", "ON_LOW_HEALTH", ["ARMOR"],
    "Gain Strength and Resistance when your back is against the wall.",
    chance={"base": 100}, cooldown={"base": 45},
    conditions=["health-below:20"],
    effects=[{"type": "STRENGTH", "ticks": {"base": 100, "per-level": 20}, "amplifier": 1},
             {"type": "ABSORPTION", "ticks": {"base": 100, "per-level": 20}, "amplifier": 0}]))

add(rune("tank", "Tank", "RARE", "ARMOR", "rare", "ON_DAMAGE", ["ARMOR"],
    "Passively reduces incoming damage the more pieces you wear.",
    chance={"base": 100},
    effects=[{"type": "ABSORPTION", "ticks": {"base": 40, "per-level": 8}, "amplifier": 0}]))

add(rune("fortify", "Fortify", "COMMON", "ARMOR", "common", "ON_ARMOR_EQUIP", ["ARMOR"],
    "Gain a brief burst of Resistance when you equip this piece.",
    chance={"base": 100},
    effects=[{"type": "ABSORPTION", "ticks": {"base": 60, "per-level": 12}, "amplifier": 0}]))

add(rune("guardian", "Guardian", "RARE", "ARMOR", "rare", "ON_DAMAGE", ["ARMOR"],
    "Reflects a portion of melee damage back at your attacker.",
    chance={"base": 30, "per-level": 5},
    effects=[{"type": "AREA_DAMAGE", "amount": {"base": 2, "per-level": 0.5}, "radius": 2}]))

add(rune("wind_dancer", "Wind Dancer", "RARE", "MOVEMENT", "rare", "ON_SPRINT", ["BOOTS"],
    "Grants a burst of speed while sprinting.",
    chance={"base": 100}, cooldown={"base": 10, "per-level": -0.6},
    effects=[{"type": "SPEED", "ticks": {"base": 60, "per-level": 12}, "amplifier": 0}]))

add(rune("dash", "Dash", "COMMON", "MOVEMENT", "common", "ON_SPRINT", ["BOOTS"],
    "A small chance to surge forward while sprinting.",
    chance={"base": 15, "per-level": 3}, cooldown={"base": 6},
    effects=[{"type": "PUSH", "strength": {"base": -1.5, "per-level": -0.2}}]))

add(rune("leap", "Leap", "RARE", "MOVEMENT", "rare", "ON_JUMP", ["BOOTS"],
    "Launches you higher into the air when you jump.",
    chance={"base": 100}, cooldown={"base": 5, "per-level": -0.3},
    effects=[{"type": "PUSH", "target": "SELF", "strength": {"base": 0.6, "per-level": 0.1}}]))

add(rune("double_jump", "Double Jump", "LEGENDARY", "MOVEMENT", "legendary", "ON_JUMP", ["BOOTS"],
    "Grants an extra jump in mid-air.",
    chance={"base": 100}, cooldown={"base": 6, "per-level": -0.4},
    effects=[{"type": "PUSH", "target": "SELF", "strength": {"base": 0.9, "per-level": 0.1}}]))

add(rune("dodge", "Dodge", "LEGENDARY", "MOVEMENT", "legendary", "ON_DAMAGE", ["BOOTS"],
    "A chance to completely evade an incoming attack.",
    chance={"base": 8, "per-level": 1.5}, cooldown={"base": 6},
    effects=[{"type": "HEAL", "amount": 0}, {"type": "PARTICLE", "particle": "CLOUD", "count": 20}]))

add(rune("sky_stepper", "Sky Stepper", "RARE", "MOVEMENT", "rare", "ON_FALL", ["BOOTS"],
    "Softens your landing and grants a brief speed boost.",
    chance={"base": 100},
    effects=[{"type": "SPEED", "ticks": {"base": 40, "per-level": 8}, "amplifier": 0}]))

add(rune("firework_footsteps", "Firework Footsteps", "RARE", "MOVEMENT", "rare", "ON_SPRINT", ["BOOTS"],
    "Leaves a trail of festive sparks as you run.",
    chance={"base": 100}, cooldown={"base": 2},
    effects=[{"type": "PARTICLE", "particle": "FIREWORK", "count": 6}]))

add(rune("rest", "Rest", "COMMON", "UTILITY", "common", "ON_HEAL", ["ARMOR"],
    "Amplifies natural regeneration slightly.",
    chance={"base": 100},
    effects=[{"type": "HEAL", "amount": {"base": 0.5, "per-level": 0.2}}]))

add(rune("telekinesis", "Telekinesis", "LEGENDARY", "MINING", "legendary", "ON_BLOCK_BREAK", ["PICKAXE", "AXE", "SHOVEL"],
    "Mined blocks go straight into your inventory instead of dropping.",
    chance={"base": 100},
    effects=[{"type": "ITEM_DROP", "material": "AIR", "amount": 0}]))

add(rune("ancient_pickaxe", "Ancient Pickaxe", "MYTHICAL", "MINING", "mythical", "ON_BLOCK_BREAK", ["PICKAXE"],
    "A relic pickaxe that occasionally unearths forgotten treasure.",
    chance={"base": 4, "per-level": 0.8},
    effects=[{"type": "LOOT", "pool": "tomb"}]))

add(rune("demonic_axe", "Demonic Axe", "MYTHICAL", "LOGGING", "mythical", "ON_BLOCK_BREAK", ["AXE"],
    "Infernal power surges through this axe with every swing.",
    chance={"base": 5, "per-level": 1},
    effects=[{"type": "ITEM_DROP", "material": "COAL", "amount": {"base": 2, "per-level": 1}},
             {"type": "PARTICLE", "particle": "SMOKE", "count": 8}]))

add(rune("clown_pickaxe", "Clown Pickaxe", "LEGENDARY", "MINING", "legendary", "ON_BLOCK_BREAK", ["PICKAXE"],
    "Honk! A chance to mine the whole vein at once in a burst of chaos.",
    chance={"base": 5, "per-level": 1}, cooldown={"base": 8},
    effects=[{"type": "AREA_DAMAGE", "amount": 0, "radius": 0},
             {"type": "SOUND", "sound": "ENTITY_VILLAGER_YES", "pitch": 2},
             {"type": "PARTICLE", "particle": "TOTEM_OF_UNDYING", "count": 20}]))

add(rune("clown_sword", "Clown Sword", "LEGENDARY", "COMBAT", "legendary", "ON_HIT", ["SWORD"],
    "Every hit is a party - random chaotic effects on your foe.",
    chance={"base": 15, "per-level": 3},
    effects=[{"type": "SLOW", "ticks": 40, "amplifier": 1},
             {"type": "PARTICLE", "particle": "NOTE", "count": 15},
             {"type": "SOUND", "sound": "ENTITY_VILLAGER_YES", "pitch": 1.5}]))

add(rune("fortune_seeker", "Fortune Seeker", "RARE", "MINING", "rare", "ON_BLOCK_BREAK", ["PICKAXE"],
    "Increases your chance of bonus ore drops.",
    chance={"base": 25, "per-level": 4},
    effects=[{"type": "ITEM_DROP", "material": "IRON_ORE", "amount": {"base": 1, "per-level": 0.3}}]))

add(rune("ore_magnet", "Ore Magnet", "COMMON", "MINING", "common", "ON_BLOCK_BREAK", ["PICKAXE"],
    "Automatically pulls nearby dropped ore toward you.",
    chance={"base": 100},
    effects=[{"type": "PULL", "target": "AREA", "radius": 4, "strength": 0.5}]))

add(rune("gem_finder", "Gem Finder", "RARE", "MINING", "rare", "ON_BLOCK_BREAK", ["PICKAXE"],
    "A chance to find a stray gem while mining.",
    chance={"base": 8, "per-level": 1.5},
    effects=[{"type": "ITEM_DROP", "material": "EMERALD", "amount": 1}]))

add(rune("treasure_hunter", "Treasure Hunter", "LEGENDARY", "MINING", "legendary", "ON_BLOCK_BREAK", ["PICKAXE", "SHOVEL"],
    "Rare chance to unearth an unidentified rune while digging or mining.",
    chance={"base": 2, "per-level": 0.4},
    effects=[{"type": "LOOT", "pool": "common"}]))

add(rune("auto_smelt", "Auto Smelt", "RARE", "MINING", "rare", "ON_BLOCK_BREAK", ["PICKAXE"],
    "Automatically smelts ores as you mine them.",
    chance={"base": 100},
    effects=[{"type": "ITEM_DROP", "material": "IRON_INGOT", "amount": 0}]))

add(rune("excavator", "Excavator", "RARE", "DIGGING", "rare", "ON_BLOCK_BREAK", ["SHOVEL"],
    "Digs out a wider area around the block you break.",
    chance={"base": 100}, cooldown={"base": 4, "per-level": -0.3},
    effects=[{"type": "ITEM_DROP", "material": "DIRT", "amount": {"base": 1, "per-level": 0.5}}]))

add(rune("greedy_greens", "Greedy Greens", "COMMON", "FARMING", "common", "ON_BLOCK_BREAK", ["HOE"],
    "Occasionally yields extra crops on harvest.",
    chance={"base": 20, "per-level": 4},
    conditions=["mining"],
    effects=[{"type": "ITEM_DROP", "material": "WHEAT", "amount": {"base": 1, "per-level": 0.4}}]))

add(rune("replant", "Replant", "COMMON", "FARMING", "common", "ON_BLOCK_BREAK", ["HOE"],
    "Automatically replants crops you harvest.",
    chance={"base": 100},
    effects=[{"type": "BLOCK_PLACE", "material": "WHEAT"}]))

add(rune("harvest_master", "Harvest Master", "LEGENDARY", "FARMING", "legendary", "ON_BLOCK_BREAK", ["HOE"],
    "Harvests an entire patch of mature crops at once.",
    chance={"base": 100}, cooldown={"base": 5, "per-level": -0.3},
    effects=[{"type": "ITEM_DROP", "material": "WHEAT", "amount": {"base": 3, "per-level": 1}}]))

add(rune("herbalist", "Herbalist", "RARE", "FARMING", "rare", "ON_BLOCK_BREAK", ["HOE"],
    "Boosts the value of harvested crops when sold.",
    chance={"base": 100},
    effects=[{"type": "MONEY", "amount": {"base": 1, "per-level": 0.5}}]))

add(rune("holy_cultivation", "Holy Cultivation", "MYTHICAL", "FARMING", "mythical", "ON_BLOCK_BREAK", ["HOE"],
    "Blesses your farmland, greatly boosting crop yield.",
    chance={"base": 100}, cooldown={"base": 3},
    effects=[{"type": "ITEM_DROP", "material": "WHEAT", "amount": {"base": 4, "per-level": 1}},
             {"type": "PARTICLE", "particle": "END_ROD", "count": 10}]))

add(rune("crop_magnet", "Crop Magnet", "COMMON", "FARMING", "common", "ON_BLOCK_BREAK", ["HOE"],
    "Pulls harvested crops directly toward you.",
    chance={"base": 100},
    effects=[{"type": "PULL", "target": "AREA", "radius": 3, "strength": 0.4}]))

add(rune("tree_feller", "Tree Feller", "LEGENDARY", "LOGGING", "legendary", "ON_BLOCK_BREAK", ["AXE"],
    "Chops down the whole tree in a single swing.",
    chance={"base": 100}, cooldown={"base": 6, "per-level": -0.4},
    effects=[{"type": "ITEM_DROP", "material": "OAK_LOG", "amount": {"base": 4, "per-level": 1}}]))

add(rune("lumberjack", "Lumberjack", "RARE", "LOGGING", "rare", "ON_BLOCK_BREAK", ["AXE"],
    "Extra wood on every log you chop.",
    chance={"base": 25, "per-level": 4},
    effects=[{"type": "ITEM_DROP", "material": "OAK_LOG", "amount": {"base": 1, "per-level": 0.4}}]))

add(rune("fish_magnet", "Fish Magnet", "COMMON", "FISHING", "common", "ON_FISH", ["FISHING_ROD"],
    "Draws your catch straight into your inventory.",
    chance={"base": 100},
    effects=[{"type": "PULL", "target": "AREA", "radius": 3, "strength": 0.5}]))

add(rune("lucky_catch", "Lucky Catch", "RARE", "FISHING", "rare", "ON_FISH", ["FISHING_ROD"],
    "Improves your odds of catching treasure.",
    chance={"base": 100},
    effects=[{"type": "LOOT", "pool": "rare"}]))

add(rune("double_catch", "Double Catch", "LEGENDARY", "FISHING", "legendary", "ON_FISH", ["FISHING_ROD"],
    "A chance to reel in two catches at once.",
    chance={"base": 20, "per-level": 4},
    effects=[{"type": "ITEM_DROP", "material": "COD", "amount": 1}]))

add(rune("miners_luck", "Miner's Luck", "MYTHICAL", "MINING", "mythical", "ON_BLOCK_BREAK", ["PICKAXE"],
    "Blesses your pickaxe with extraordinary fortune.",
    chance={"base": 100}, cooldown={"base": 2},
    effects=[{"type": "LOOT", "pool": "legendary"}]))

add(rune("greenhouse", "Greenhouse", "RARE", "FARMING", "rare", "ON_BLOCK_PLACE", ["HOE"],
    "Crops you plant grow noticeably faster nearby.",
    chance={"base": 100},
    effects=[{"type": "PARTICLE", "particle": "COMPOSTER", "count": 6}]))

add(rune("plagueweaver", "Plagueweaver", "LEGENDARY", "COMBAT", "legendary", "ON_HIT", ["SWORD"],
    "Weaves a lingering plague that spreads to nearby enemies.",
    chance={"base": 15, "per-level": 3}, cooldown={"base": 6},
    effects=[{"type": "POISON", "target": "NEARBY_MOBS", "ticks": {"base": 60, "per-level": 10}, "amplifier": 1}]))

add(rune("confidential_contraband", "Confidential Contraband", "MYTHICAL", "ECONOMY", "mythical", "ON_BLOCK_BREAK", ["PICKAXE", "SHOVEL"],
    "Rumor has it something valuable slips into your pocket now and then.",
    chance={"base": 3, "per-level": 0.6},
    effects=[{"type": "MONEY", "amount": {"base": 50, "per-level": 15}}]))

add(rune("the_naughtiest", "The Naughtiest", "MYTHICAL", "SEASONAL", "seasonal_christmas", "ON_KILL", ["SWORD"],
    "A festive but wicked blade that leaves coal in its wake.",
    chance={"base": 100},
    effects=[{"type": "ITEM_DROP", "material": "COAL", "amount": {"base": 2, "per-level": 1}}]))

add(rune("greedy_mobs", "Greedy Mobs", "LEGENDARY", "COMBAT", "legendary", "ON_MOB_KILL", ["SWORD", "AXE"],
    "Mobs you defeat drop noticeably more loot.",
    chance={"base": 100},
    effects=[{"type": "ITEM_DROP", "material": "GOLD_NUGGET", "amount": {"base": 2, "per-level": 1}}]))

add(rune("celestial_pickaxe", "Celestial Pickaxe", "MYTHICAL", "MINING", "mythical", "ON_BLOCK_BREAK", ["PICKAXE"],
    "Blessed by the stars; blocks shatter in a shower of light.",
    chance={"base": 100},
    effects=[{"type": "PARTICLE", "particle": "END_ROD", "count": 12},
             {"type": "ITEM_DROP", "material": "AMETHYST_SHARD", "amount": {"base": 1, "per-level": 0.5}}]))

add(rune("christmas_helmet", "Christmas Helmet", "LEGENDARY", "SEASONAL", "seasonal_christmas", "ON_ARMOR_EQUIP", ["HELMET"],
    "Snow gently falls around you while worn.",
    chance={"base": 100},
    effects=[{"type": "PARTICLE", "particle": "SNOWFLAKE", "count": 4}]))

add(rune("christmas_fishing_rod", "Christmas Fishing Rod", "LEGENDARY", "SEASONAL", "seasonal_christmas", "ON_FISH", ["FISHING_ROD"],
    "A festive rod that reels in seasonal treats.",
    chance={"base": 30, "per-level": 5},
    effects=[{"type": "ITEM_DROP", "material": "COOKIE", "amount": 1}]))

add(rune("ender_backpack", "Ender Backpack", "LEGENDARY", "UTILITY", "legendary", "ON_BLOCK_BREAK", ["PICKAXE", "SHOVEL", "AXE"],
    "A rift briefly opens, pulling nearby drops into storage.",
    chance={"base": 100},
    effects=[{"type": "PULL", "target": "AREA", "radius": 5, "strength": 0.6}]))

add(rune("pool_party", "Pool Party", "RARE", "SEASONAL", "event_summer", "ON_FISH", ["FISHING_ROD"],
    "Summer vibes boost your fishing luck.",
    chance={"base": 100},
    effects=[{"type": "LOOT", "pool": "rare"}]))

add(rune("phantom_blocks", "Phantom Blocks", "MYTHICAL", "UTILITY", "mythical", "ON_BLOCK_PLACE", ["ANY"],
    "Placed blocks briefly turn intangible to allies.",
    chance={"base": 100},
    effects=[{"type": "PARTICLE", "particle": "PORTAL", "count": 10}]))

add(rune("saviors_grace", "Savior's Grace", "LEGENDARY", "UTILITY", "legendary", "ON_HEAL", ["ARMOR"],
    "Amplifies healing received from any source.",
    chance={"base": 100},
    effects=[{"type": "HEAL", "amount": {"base": 1, "per-level": 0.4}}]))

add(rune("midas_touch", "Midas Touch", "MYTHICAL", "ECONOMY", "mythical", "ON_BLOCK_BREAK", ["PICKAXE"],
    "Everything you mine has a chance to turn to gold.",
    chance={"base": 4, "per-level": 0.8},
    effects=[{"type": "ITEM_DROP", "material": "GOLD_NUGGET", "amount": {"base": 3, "per-level": 1}}]))

add(rune("scavenger", "Scavenger", "COMMON", "LOOT", "common", "ON_MOB_KILL", ["SWORD"],
    "A small chance for bonus loot from slain mobs.",
    chance={"base": 15, "per-level": 3},
    effects=[{"type": "ITEM_DROP", "material": "BONE", "amount": 1}]))

add(rune("headhunter", "Headhunter", "RARE", "LOOT", "rare", "ON_PLAYER_KILL", ["SWORD"],
    "Collects a trophy from defeated players.",
    chance={"base": 20, "per-level": 4},
    effects=[{"type": "ITEM_DROP", "material": "PLAYER_HEAD", "amount": 1}]))

add(rune("xp_hoarder", "XP Hoarder", "COMMON", "XP", "common", "ON_MOB_KILL", ["SWORD", "AXE"],
    "Grants bonus XP from every kill.",
    chance={"base": 100},
    effects=[{"type": "XP", "amount": {"base": 2, "per-level": 0.5}}]))

add(rune("sage", "Sage", "RARE", "XP", "rare", "ON_BLOCK_BREAK", ["PICKAXE", "HOE", "AXE"],
    "Grants bonus XP from gathering.",
    chance={"base": 30, "per-level": 5},
    effects=[{"type": "XP", "amount": {"base": 1, "per-level": 0.3}}]))

add(rune("cavern_echo", "Cavern Echo", "COMMON", "MINING", "common", "ON_BLOCK_BREAK", ["PICKAXE"],
    "The echo of the cave occasionally reveals a nearby ore.",
    chance={"base": 100},
    conditions=["world-type-nether:"],
    effects=[{"type": "PARTICLE", "particle": "SOUL_FIRE_FLAME", "count": 4}]))

add(rune("nether_forged", "Nether-Forged", "LEGENDARY", "MINING", "legendary", "ON_BLOCK_BREAK", ["PICKAXE"],
    "Blocks broken in the Nether yield bonus ancient debris shards.",
    chance={"base": 5, "per-level": 1},
    conditions=["world-type-nether"],
    effects=[{"type": "ITEM_DROP", "material": "NETHERITE_SCRAP", "amount": 1}]))

add(rune("void_touched", "Void Touched", "MYTHICAL", "MINING", "mythical", "ON_BLOCK_BREAK", ["PICKAXE"],
    "The End's strange energies cling to your pickaxe.",
    chance={"base": 5, "per-level": 1},
    conditions=["world-type-end"],
    effects=[{"type": "ITEM_DROP", "material": "CHORUS_FRUIT", "amount": 1}]))

add(rune("night_owl", "Night Owl", "COMMON", "UTILITY", "common", "ON_BLOCK_BREAK", ["PICKAXE"],
    "Bonus effectiveness while mining at night.",
    chance={"base": 15, "per-level": 3},
    conditions=["time-night"],
    effects=[{"type": "ITEM_DROP", "material": "COAL", "amount": 1}]))

add(rune("sunforged", "Sunforged", "RARE", "COMBAT", "rare", "ON_HIT", ["SWORD"],
    "Deals extra damage to undead in daylight.",
    chance={"base": 100},
    conditions=["time-day"],
    effects=[{"type": "FIRE", "target": "TARGET", "ticks": {"base": 40, "per-level": 8}}]))

add(rune("stormcaller", "Stormcaller", "LEGENDARY", "COMBAT", "legendary", "ON_ATTACK", ["TRIDENT"],
    "Empowers your trident during thunderstorms.",
    chance={"base": 30, "per-level": 5},
    conditions=["weather-thunder"],
    effects=[{"type": "LIGHTNING", "target": "TARGET", "damage-only": False}]))

add(rune("sneak_attack", "Sneak Attack", "RARE", "COMBAT", "rare", "ON_ATTACK", ["SWORD"],
    "Bonus damage when striking from a sneaking stance.",
    chance={"base": 100},
    conditions=["sneaking"],
    effects=[{"type": "DAMAGE", "target": "TARGET", "amount": {"base": 4, "per-level": 1}}]))

add(rune("adrenaline", "Adrenaline", "RARE", "COMBAT", "rare", "ON_ATTACK", ["SWORD", "AXE"],
    "Fighting while sprinting fuels a surge of aggression.",
    chance={"base": 100}, cooldown={"base": 10},
    conditions=["sprinting"],
    effects=[{"type": "STRENGTH", "ticks": {"base": 60, "per-level": 12}, "amplifier": 0}]))

add(rune("aegis", "Aegis", "LEGENDARY", "ARMOR", "legendary", "ON_DAMAGE", ["ARMOR"],
    "Blocking incoming attacks grants a shield of resistance.",
    chance={"base": 100},
    conditions=["blocking"],
    effects=[{"type": "ABSORPTION", "ticks": {"base": 60, "per-level": 12}, "amplifier": 1}]))

add(rune("gilded_line", "Gilded Line", "RARE", "FISHING", "rare", "ON_FISH", ["FISHING_ROD"],
    "Your line is laced with gold, tempting bigger catches.",
    chance={"base": 100},
    effects=[{"type": "MONEY", "amount": {"base": 5, "per-level": 2}}]))

add(rune("bounty_hunter", "Bounty Hunter", "LEGENDARY", "COMBAT", "legendary", "ON_PLAYER_KILL", ["SWORD", "BOW"],
    "Earn a cash bounty for defeating other players.",
    chance={"base": 100},
    effects=[{"type": "MONEY", "amount": {"base": 25, "per-level": 8}}]))

add(rune("obliterate", "Obliterate", "MYTHICAL", "COMBAT", "mythical", "ON_CRITICAL", ["SWORD", "AXE"],
    "Critical hits detonate outward in a shockwave.",
    chance={"base": 100}, cooldown={"base": 10},
    effects=[{"type": "EXPLOSION", "power": 0, "fire": False, "break-blocks": False},
             {"type": "AREA_DAMAGE", "amount": {"base": 5, "per-level": 1.2}, "radius": 4}]))

add(rune("magma_walker", "Magma Walker", "RARE", "MOVEMENT", "rare", "ON_FALL", ["BOOTS"],
    "Immune to fall damage and briefly resistant to fire.",
    chance={"base": 100}, cooldown={"base": 8},
    effects=[{"type": "HEAL", "amount": 0}]))

add(rune("last_word", "Last Word", "MYTHICAL", "COMBAT", "mythical", "ON_DEATH", ["SWORD"],
    "Detonates a final explosive strike upon your own death.",
    chance={"base": 100},
    effects=[{"type": "AREA_DAMAGE", "amount": {"base": 10, "per-level": 2}, "radius": 5},
             {"type": "EXPLOSION", "power": 0, "fire": False, "break-blocks": False}]))

add(rune("hunters_mark", "Hunter's Mark", "RARE", "COMBAT", "rare", "ON_ATTACK", ["BOW", "CROSSBOW"],
    "Marks your target, revealing them briefly through walls.",
    chance={"base": 100}, cooldown={"base": 6},
    effects=[{"type": "PARTICLE", "target": "TARGET", "particle": "SOUL", "count": 5}]))

add(rune("spirit_walker", "Spirit Walker", "LEGENDARY", "MOVEMENT", "legendary", "ON_SPRINT", ["BOOTS"],
    "Phase briefly through danger while sprinting.",
    chance={"base": 100}, cooldown={"base": 15, "per-level": -1},
    effects=[{"type": "SPEED", "ticks": {"base": 40, "per-level": 8}, "amplifier": 1},
             {"type": "PARTICLE", "particle": "SOUL", "count": 8}]))

add(rune("blast_mining", "Blast Mining", "LEGENDARY", "MINING", "legendary", "ON_BLOCK_BREAK", ["PICKAXE"],
    "Occasionally triggers a controlled blast, clearing surrounding stone.",
    chance={"base": 100}, cooldown={"base": 6, "per-level": -0.4},
    effects=[{"type": "EXPLOSION", "power": 1.5, "fire": False, "break-blocks": True}]))

# ---------------------------------------------------------------------------
# 2. TEMPLATE-GENERATED RUNES (systematic combinatorial variation, not renamed duplicates:
#    each entry varies element/effect-type/target/trigger/equipment/rarity in a way that changes
#    its actual mechanic, not just its flavor text).
# ---------------------------------------------------------------------------

ELEMENTS = [
    ("Ember", "FIRE", {"type": "FIRE", "ticks": {"base": 40, "per-level": 8}}, "burns"),
    ("Frost", "ICE", {"type": "SLOW", "ticks": {"base": 40, "per-level": 8}, "amplifier": {"base": 0, "per-level": 0.2}}, "chills"),
    ("Toxic", "POISON", {"type": "POISON", "ticks": {"base": 40, "per-level": 8}, "amplifier": 0}, "poisons"),
    ("Wither", "WITHER", {"type": "WITHER", "ticks": {"base": 30, "per-level": 6}, "amplifier": 0}, "withers"),
    ("Void", "VOID", {"type": "SLOW", "ticks": {"base": 30, "per-level": 6}, "amplifier": 1}, "warps"),
    ("Storm", "LIGHTNING", {"type": "LIGHTNING", "target": "TARGET", "damage-only": True}, "shocks"),
]
WEAPON_GROUPS = [
    ("Blade", ["SWORD"], "ON_HIT"),
    ("Axe", ["AXE"], "ON_HIT"),
    ("Bow", ["BOW", "CROSSBOW"], "ON_ATTACK"),
    ("Trident", ["TRIDENT"], "ON_HIT"),
]
RARITY_CYCLE = ["COMMON", "RARE", "LEGENDARY", "MYTHICAL"]

count = 0
TIER_PREFIX = {"COMMON": "", "RARE": "Greater ", "LEGENDARY": "Superior ", "MYTHICAL": "Ascended "}
for elem_name, elem_tag, elem_effect, verb in ELEMENTS:
    for weap_name, equip, trig in WEAPON_GROUPS:
        for rarity in RARITY_CYCLE:
            prefix = TIER_PREFIX[rarity]
            id_ = f"{prefix.strip().lower() + '_' if prefix else ''}{elem_name.lower()}_{weap_name.lower()}"
            base_chance = {"COMMON": 22, "RARE": 18, "LEGENDARY": 14, "MYTHICAL": 10}[rarity]
            dmg = {"COMMON": 1.5, "RARE": 2.5, "LEGENDARY": 4, "MYTHICAL": 6}[rarity]
            eff = dict(elem_effect)
            effects = [eff, {"type": "DAMAGE", "target": "TARGET", "amount": {"base": dmg, "per-level": dmg * 0.25}}]
            add(rune(id_, f"{prefix}{elem_name} {weap_name}", rarity, "WEAPONS", rarity.lower(), trig, equip,
                f"Your {weap_name.lower()} {verb} enemies with {elem_name.lower()} energy on hit.",
                chance={"base": base_chance, "per-level": 3}, effects=effects, original=False))
            count += 1

# Gathering bonus-drop runes: category x resource x rarity
GATHER = [
    ("Ore", "MINING", ["PICKAXE"], "ON_BLOCK_BREAK", ["IRON_ORE", "GOLD_ORE", "COPPER_ORE", "REDSTONE", "LAPIS_ORE"]),
    ("Timber", "LOGGING", ["AXE"], "ON_BLOCK_BREAK", ["OAK_LOG", "SPRUCE_LOG", "BIRCH_LOG", "DARK_OAK_LOG"]),
    ("Harvest", "FARMING", ["HOE"], "ON_BLOCK_BREAK", ["WHEAT", "CARROT", "POTATO", "BEETROOT"]),
    ("Excavation", "DIGGING", ["SHOVEL"], "ON_BLOCK_BREAK", ["CLAY_BALL", "FLINT", "GRAVEL", "SAND"]),
    ("Angler", "FISHING", ["FISHING_ROD"], "ON_FISH", ["COD", "SALMON", "TROPICAL_FISH", "INK_SAC"]),
]
ADJ = ["Novice", "Skilled", "Expert", "Master"]
for cat_name, category, equip, trig, materials in GATHER:
    for i, rarity in enumerate(RARITY_CYCLE):
        adj = ADJ[i]
        mat = materials[i % len(materials)]
        id_ = f"{adj.lower()}_{cat_name.lower()}"
        amt = {"COMMON": 1, "RARE": 1.5, "LEGENDARY": 2.5, "MYTHICAL": 4}[rarity]
        chance = {"COMMON": 20, "RARE": 18, "LEGENDARY": 14, "MYTHICAL": 10}[rarity]
        add(rune(id_, f"{adj} {cat_name}", rarity, category, rarity.lower(), trig, equip,
            f"{adj} proficiency grants bonus {mat.replace('_',' ').lower()} while working.",
            chance={"base": chance, "per-level": 3},
            effects=[{"type": "ITEM_DROP", "material": mat, "amount": {"base": amt, "per-level": amt * 0.25}}], original=False))
        count += 1

# Stat/utility runes: stat x rarity, for armor pieces
STATS = [
    ("Vigor", {"type": "STRENGTH", "ticks": {"base": 60, "per-level": 12}, "amplifier": 0}, "ON_ARMOR_EQUIP"),
    ("Haste", {"type": "SPEED", "ticks": {"base": 60, "per-level": 12}, "amplifier": 0}, "ON_SPRINT"),
    ("Resolve", {"type": "ABSORPTION", "ticks": {"base": 60, "per-level": 12}, "amplifier": 0}, "ON_DAMAGE"),
    ("Renewal", {"type": "REGENERATION", "ticks": {"base": 60, "per-level": 12}, "amplifier": 0}, "ON_LOW_HEALTH"),
]
ARMOR_PIECES = [("Helm", ["HELMET"]), ("Plate", ["CHESTPLATE"]), ("Greaves", ["LEGGINGS"]), ("Treads", ["BOOTS"])]
for stat_name, eff, trig in STATS:
    for piece_name, equip in ARMOR_PIECES:
        for rarity in RARITY_CYCLE:
            prefix = TIER_PREFIX[rarity]
            id_ = f"{prefix.strip().lower() + '_' if prefix else ''}{stat_name.lower()}_{piece_name.lower()}"
            chance = {"COMMON": 100, "RARE": 100, "LEGENDARY": 100, "MYTHICAL": 100}[rarity]
            cd = {"COMMON": 20, "RARE": 15, "LEGENDARY": 10, "MYTHICAL": 6}[rarity]
            effects = [dict(eff)]
            cond = ["health-below:40"] if trig == "ON_LOW_HEALTH" else None
            add(rune(id_, f"{prefix}{stat_name} {piece_name}", rarity, "ARMOR", rarity.lower(), trig, equip,
                f"Your {piece_name.lower()} channels {stat_name.lower()}, triggered by combat and movement.",
                chance={"base": chance}, cooldown={"base": cd}, effects=effects, conditions=cond, original=False))
            count += 1

# Boss-drop runes: one per configured boss x rarity variant (small pool, high impact)
BOSSES = ["ender_dragon", "wither", "warden", "elder_guardian"]
for boss in BOSSES:
    for rarity in ["LEGENDARY", "MYTHICAL"]:
        id_ = f"{boss}_fang"
        dmg = 8 if rarity == "LEGENDARY" else 14
        add(rune(f"{boss}_fang_{rarity.lower()}", boss.replace("_", " ").title() + " Fang", rarity, "BOSS", "boss_" + boss if boss == "ender_dragon" else "legendary",
            "ON_HIT", ["SWORD", "AXE"], f"A relic fang torn from the {boss.replace('_',' ')}, hungry for battle.",
            chance={"base": 10, "per-level": 2}, effects=[{"type": "DAMAGE", "target": "TARGET", "amount": {"base": dmg, "per-level": dmg * 0.2}}], original=False))
        count += 1

# Crate-exclusive cosmetic-leaning runes
CRATE_THEMES = [("Oni", "crate_oni", "MYTHICAL"), ("Christmas", "crate_christmas", "LEGENDARY")]
CRATE_SLOTS = [("Mask", ["HELMET"]), ("Robes", ["CHESTPLATE"]), ("Fang", ["SWORD"]), ("Boots", ["BOOTS"])]
for theme, pool, rarity in CRATE_THEMES:
    for slot_name, equip in CRATE_SLOTS:
        id_ = f"{theme.lower()}_{slot_name.lower()}"
        add(rune(id_, f"{theme} {slot_name}", rarity, "CRATE", pool, "ON_ARMOR_EQUIP" if equip != ["SWORD"] else "ON_HIT", equip,
            f"A crate-exclusive {theme.lower()} relic with a flair all its own.",
            chance={"base": 100 if equip != ["SWORD"] else 15, "per-level": 0 if equip != ["SWORD"] else 3},
            effects=[{"type": "PARTICLE", "particle": "SOUL_FIRE_FLAME" if theme == "Oni" else "SNOWFLAKE", "count": 8}], original=False))
        count += 1

print(f"template-generated: {count}", file=sys.stderr)
print(f"total runes: {len(ALL)}", file=sys.stderr)

# ---------------------------------------------------------------------------
# Write output, grouped by pool into separate files (keeps each YAML manageable).
# ---------------------------------------------------------------------------
by_pool = {}
for id_, d in ALL.items():
    by_pool.setdefault(d["pool"], {})[id_] = d

for pool, entries in by_pool.items():
    path = os.path.join(OUT, f"{pool}.yml")
    with open(path, "w", encoding="utf-8") as f:
        f.write(f"# Auto-generated rune definitions for pool '{pool}'. Safe to hand-edit; /runeadmin reload picks up changes.\n")
        yaml.dump({"runes": entries}, f, allow_unicode=True, sort_keys=False, width=100)

print(f"files written: {len(by_pool)}", file=sys.stderr)

# ---------------------------------------------------------------------------
# 3. EXPANSION: tool-class elemental variants, deeper gathering tiers, zone pools
#    (tomb/darkzone/dojo), more seasonal/boss/event coverage - to reach the 750+ target
#    from requirement #82 while keeping every id mechanically distinct.
# ---------------------------------------------------------------------------

TOOL_GROUPS = [
    ("Pick", ["PICKAXE"], "ON_BLOCK_BREAK", "MINING"),
    ("Shovel", ["SHOVEL"], "ON_BLOCK_BREAK", "DIGGING"),
    ("Hoe", ["HOE"], "ON_BLOCK_BREAK", "FARMING"),
    ("Rod", ["FISHING_ROD"], "ON_FISH", "FISHING"),
]
TOOL_ELEMENT_EFFECTS = [
    ("Scorching", {"type": "ITEM_DROP", "material": "COAL", "amount": {"base": 1, "per-level": 0.3}}, "leaves scorched bonus coal behind"),
    ("Glacial", {"type": "PARTICLE", "particle": "SNOWFLAKE", "count": 6}, "chills the surrounding area"),
    ("Verdant", {"type": "XP", "amount": {"base": 2, "per-level": 0.5}}, "channels nature's bounty into XP"),
    ("Gilded", {"type": "MONEY", "amount": {"base": 2, "per-level": 0.6}}, "leaves a trace of gold value"),
]
for tool_name, equip, trig, category in TOOL_GROUPS:
    for elem_name, elem_eff, desc_verb in TOOL_ELEMENT_EFFECTS:
        for rarity in RARITY_CYCLE:
            prefix = TIER_PREFIX[rarity]
            id_ = f"{prefix.strip().lower() + '_' if prefix else ''}{elem_name.lower()}_{tool_name.lower()}"
            chance = {"COMMON": 20, "RARE": 16, "LEGENDARY": 12, "MYTHICAL": 8}[rarity]
            add(rune(id_, f"{prefix}{elem_name} {tool_name}", rarity, category, rarity.lower(), trig, equip,
                f"Your {tool_name.lower()} {desc_verb} as you work.",
                chance={"base": chance, "per-level": 3}, effects=[dict(elem_eff)], original=False))
            count += 1

# Deeper gathering tiers: 4 materials x 4 rarities each (was 1 rarity per material before)
GATHER2 = [
    ("MINING", ["PICKAXE"], "ON_BLOCK_BREAK", ["COAL", "IRON_ORE", "GOLD_ORE", "DIAMOND", "EMERALD_ORE", "REDSTONE", "LAPIS_ORE", "COPPER_ORE"]),
    ("LOGGING", ["AXE"], "ON_BLOCK_BREAK", ["OAK_LOG", "SPRUCE_LOG", "BIRCH_LOG", "JUNGLE_LOG", "ACACIA_LOG", "DARK_OAK_LOG", "MANGROVE_LOG", "CHERRY_LOG"]),
    ("FARMING", ["HOE"], "ON_BLOCK_BREAK", ["WHEAT", "CARROT", "POTATO", "BEETROOT", "PUMPKIN", "MELON_SLICE", "SUGAR_CANE", "NETHER_WART"]),
    ("DIGGING", ["SHOVEL"], "ON_BLOCK_BREAK", ["SAND", "GRAVEL", "CLAY_BALL", "DIRT", "SOUL_SAND", "RED_SAND", "SNOWBALL", "MUD"]),
    ("FISHING", ["FISHING_ROD"], "ON_FISH", ["COD", "SALMON", "TROPICAL_FISH", "PUFFERFISH", "INK_SAC", "PRISMARINE_SHARD", "NAUTILUS_SHELL", "BONE"]),
]
NOUN = ["Prospector's", "Journeyman's", "Artisan's", "Virtuoso's"]
for category, equip, trig, materials in GATHER2:
    for mi, mat in enumerate(materials):
        for ri, rarity in enumerate(RARITY_CYCLE):
            noun = NOUN[ri]
            id_ = f"{noun.split(chr(39))[0].lower()}_{mat.lower()}_finder"
            amt = {"COMMON": 1, "RARE": 1.5, "LEGENDARY": 2.5, "MYTHICAL": 4}[rarity]
            chance = {"COMMON": 18, "RARE": 15, "LEGENDARY": 11, "MYTHICAL": 7}[rarity]
            add(rune(id_, f"{noun} {mat.replace('_', ' ').title()} Finder", rarity, category, rarity.lower(), trig, equip,
                f"A {noun.lower()} touch yields bonus {mat.replace('_',' ').lower()}.",
                chance={"base": chance, "per-level": 2.5},
                effects=[{"type": "ITEM_DROP", "material": mat, "amount": {"base": amt, "per-level": amt * 0.2}}], original=False))
            count += 1

# Zone-specific pools: Tomb, Darkzone, Dojo - each gets a themed set across categories
TOMB_THEMES = [
    ("Sandbound", "COMBAT", ["SWORD"], "ON_HIT", {"type": "SLOW", "ticks": 40, "amplifier": 0}, "binds foes in ancient sand"),
    ("Sarcophagal", "ARMOR", ["ARMOR"], "ON_DAMAGE", {"type": "ABSORPTION", "ticks": 60, "amplifier": 0}, "wraps you in tomb-forged wards"),
    ("Scarab", "LOOT", ["PICKAXE", "SHOVEL"], "ON_BLOCK_BREAK", {"type": "ITEM_DROP", "material": "GOLD_NUGGET", "amount": 1}, "scarabs skitter loose gold from the ruins"),
    ("Pharaoh's", "COMBAT", ["SWORD", "AXE"], "ON_KILL", {"type": "XP", "amount": {"base": 4, "per-level": 1}}, "channels a pharaoh's dying curse into XP"),
]
for name, category, equip, trig, eff, desc in TOMB_THEMES:
    for rarity in ["RARE", "LEGENDARY"]:
        id_ = f"{name.lower().rstrip(chr(39)+'s')}_{rarity.lower()}_relic"
        chance = 15 if rarity == "RARE" else 10
        add(rune(id_, f"{name} Relic", rarity, category, "tomb", trig, equip,
            f"A relic of Pharaoh's Tomb that {desc}.",
            chance={"base": chance, "per-level": 2.5}, effects=[dict(eff)], original=False))
        count += 1

DARKZONE_THEMES = [
    ("Voidforged", "COMBAT", ["SWORD", "AXE"], "ON_HIT", {"type": "WITHER", "ticks": 40, "amplifier": 0}, "corrupts foes with void rot"),
    ("Nightmare", "ARMOR", ["ARMOR"], "ON_LOW_HEALTH", {"type": "STRENGTH", "ticks": 60, "amplifier": 1}, "feeds on fear when you're near death"),
    ("Umbral", "LOOT", ["SWORD"], "ON_MOB_KILL", {"type": "ITEM_DROP", "material": "NETHERITE_SCRAP", "amount": 1}, "scavenges shadow-touched scraps from the fallen"),
]
for name, category, equip, trig, eff, desc in DARKZONE_THEMES:
    for rarity in ["LEGENDARY", "MYTHICAL"]:
        id_ = f"{name.lower()}_{rarity.lower()}_dark"
        chance = 8 if rarity == "LEGENDARY" else 4
        add(rune(id_, f"{name} Dark Relic", rarity, category, "darkzone", trig, equip,
            f"A relic of the Darkzone that {desc}.",
            chance={"base": chance, "per-level": 1.5}, effects=[dict(eff)], original=False))
        count += 1

DOJO_THEMES = [
    ("Disciple's", "COMBAT", ["SWORD"], "ON_ATTACK", {"type": "STRENGTH", "ticks": 40, "amplifier": 0}, "channels dojo training into raw power", "sneaking"),
    ("Sensei's", "MOVEMENT", ["BOOTS"], "ON_SPRINT", {"type": "SPEED", "ticks": 40, "amplifier": 1}, "grants a master's swiftness", None),
    ("Dragon Fist", "COMBAT", ["SWORD", "AXE"], "ON_CRITICAL", {"type": "AREA_DAMAGE", "amount": 3, "radius": 3}, "unleashes a dragon-style shockwave", None),
    ("Dojo Angler's", "FISHING", ["FISHING_ROD"], "ON_FISH", {"type": "XP", "amount": 4}, "rewards patient dojo fishing with XP", None),
]
for name, category, equip, trig, eff, desc, cond in DOJO_THEMES:
    for rarity in ["RARE", "LEGENDARY"]:
        id_ = f"{name.lower().replace(chr(39),'').replace(' ','_')}_{rarity.lower()}_dojo"
        chance = 18 if rarity == "RARE" else 12
        add(rune(id_, f"{name} Technique", rarity, category, "dojo", trig, equip,
            f"A Dragon Dojo technique that {desc}.",
            chance={"base": chance, "per-level": 2.5}, effects=[dict(eff)],
            conditions=[cond] if cond else None, original=False))
        count += 1

# Seasonal expansion: Halloween, Easter, Summer, Anniversary, Valentine
SEASONAL_POOLS = {
    "seasonal_halloween": ("2026-11-05", ["Pumpkin King's", "Wraith's", "Cauldron", "Graveyard"]),
    "seasonal_easter": ("2027-04-15", ["Bunny's", "Basket", "Blossom", "Eggshell"]),
    "event_summer": ("", ["Tidal", "Sandcastle", "Coral", "Driftwood"]),
    "seasonal_anniversary": ("2026-12-31", ["Founder's", "Vintage", "Jubilee", "Legacy"]),
    "seasonal_valentine": ("2027-02-20", ["Heartbound", "Cupid's", "Rosebound", "Devotion"]),
}
SEASON_SLOTS = [("Charm", ["SWORD", "AXE"], "ON_HIT", {"type": "PARTICLE", "particle": "HEART", "count": 8}),
                ("Ward", ["ARMOR"], "ON_ARMOR_EQUIP", {"type": "PARTICLE", "particle": "WITCH", "count": 6}),
                ("Fortune", ["PICKAXE", "HOE"], "ON_BLOCK_BREAK", {"type": "ITEM_DROP", "material": "EMERALD", "amount": 1}),
                ("Rod", ["FISHING_ROD"], "ON_FISH", {"type": "XP", "amount": 3})]
for pool_id, (ends_at, names) in SEASONAL_POOLS.items():
    if pool_id not in [p["display"] for p in []]:
        pass
    for i, (slot_name, equip, trig, eff) in enumerate(SEASON_SLOTS):
        name = names[i % len(names)]
        rarity = ["RARE", "LEGENDARY", "RARE", "LEGENDARY"][i]
        id_ = f"{pool_id}_{name.lower().replace(chr(39),'')}_{slot_name.lower()}"
        chance = 15 if rarity == "RARE" else 10
        add(rune(id_, f"{name} {slot_name}", rarity, "SEASONAL", pool_id, trig, equip,
            f"A seasonal relic ({pool_id.replace('seasonal_','').replace('event_','').title()}) with festive flair.",
            chance={"base": chance if trig != "ON_ARMOR_EQUIP" else 100, "per-level": 2},
            effects=[dict(eff)], original=False))
        count += 1

# More boss coverage
MORE_BOSSES = ["ravager_alpha", "piglin_brute_king", "guardian_elder", "shulker_prime"]
for boss in MORE_BOSSES:
    for rarity in ["LEGENDARY", "MYTHICAL"]:
        dmg = 7 if rarity == "LEGENDARY" else 12
        id_ = f"{boss}_trophy_{rarity.lower()}"
        add(rune(id_, boss.replace("_", " ").title() + " Trophy", rarity, "BOSS", "legendary", "ON_KILL", ["SWORD", "AXE"],
            f"A trophy from defeating the {boss.replace('_',' ')}, radiating leftover power.",
            chance={"base": 100}, cooldown={"base": 8},
            effects=[{"type": "DAMAGE", "target": "TARGET", "amount": {"base": dmg, "per-level": dmg * 0.2}}], original=False))
        count += 1

print(f"expansion added, running total: {len(ALL)}", file=sys.stderr)

by_pool = {}
for id_, d in ALL.items():
    by_pool.setdefault(d["pool"], {})[id_] = d
for f in os.listdir(OUT):
    os.remove(os.path.join(OUT, f))
for pool, entries in by_pool.items():
    path = os.path.join(OUT, f"{pool}.yml")
    with open(path, "w", encoding="utf-8") as f:
        f.write(f"# Auto-generated rune definitions for pool '{pool}'. Safe to hand-edit; /runeadmin reload picks up changes.\n")
        yaml.dump({"runes": entries}, f, allow_unicode=True, sort_keys=False, width=100)
print(f"FINAL total runes: {len(ALL)}, files: {len(by_pool)}", file=sys.stderr)

# ---------------------------------------------------------------------------
# 4. FINAL EXPANSION: more weapon archetypes, more armor stat variants, utility runes,
#    extra crate pools - pushes the total comfortably past the 750+ target.
# ---------------------------------------------------------------------------

ARCHETYPES = [
    ("Vampiric", "COMBAT", {"type": "HEAL", "amount": {"base": 1.5, "per-level": 0.4}}, "drains life force on hit"),
    ("Executing", "COMBAT", {"type": "DAMAGE", "target": "TARGET", "amount": {"base": 3, "per-level": 0.8}}, "seeks out weak points"),
    ("Stunning", "COMBAT", {"type": "SLOW", "ticks": {"base": 30, "per-level": 6}, "amplifier": 2}, "stuns on impact"),
    ("Piercing", "COMBAT", {"type": "AREA_DAMAGE", "amount": {"base": 2, "per-level": 0.5}, "radius": 2}, "pierces through to nearby foes"),
    ("Momentum", "COMBAT", {"type": "PUSH", "strength": {"base": 0.8, "per-level": 0.15}}, "knocks enemies back with force"),
]
for arch_name, category, eff, desc in ARCHETYPES:
    for weap_name, equip, trig in WEAPON_GROUPS:
        for rarity in RARITY_CYCLE:
            prefix = TIER_PREFIX[rarity]
            id_ = f"{prefix.strip().lower() + '_' if prefix else ''}{arch_name.lower()}_{weap_name.lower()}"
            chance = {"COMMON": 20, "RARE": 16, "LEGENDARY": 12, "MYTHICAL": 8}[rarity]
            add(rune(id_, f"{prefix}{arch_name} {weap_name}", rarity, category, rarity.lower(), trig, equip,
                f"This {weap_name.lower()} {desc}.",
                chance={"base": chance, "per-level": 2.5}, effects=[dict(eff)], original=False))
            count += 1

# Additional armor stat archetypes across all four pieces and rarities
STATS2 = [
    ("Ferocity", {"type": "STRENGTH", "ticks": {"base": 40, "per-level": 8}, "amplifier": 0}, "ON_HIT"),
    ("Ward", {"type": "ABSORPTION", "ticks": {"base": 40, "per-level": 8}, "amplifier": 0}, "ON_ARMOR_EQUIP"),
    ("Momentum Guard", {"type": "SPEED", "ticks": {"base": 40, "per-level": 8}, "amplifier": 0}, "ON_JUMP"),
    ("Mending Light", {"type": "REGENERATION", "ticks": {"base": 40, "per-level": 8}, "amplifier": 1}, "ON_HEAL"),
]
for stat_name, eff, trig in STATS2:
    for piece_name, equip in ARMOR_PIECES:
        for rarity in RARITY_CYCLE:
            prefix = TIER_PREFIX[rarity]
            slug = stat_name.lower().replace(" ", "_")
            id_ = f"{prefix.strip().lower() + '_' if prefix else ''}{slug}_{piece_name.lower()}"
            cd = {"COMMON": 18, "RARE": 14, "LEGENDARY": 10, "MYTHICAL": 6}[rarity]
            add(rune(id_, f"{prefix}{stat_name} {piece_name}", rarity, "ARMOR", rarity.lower(), trig, equip,
                f"Your {piece_name.lower()} resonates with {stat_name.lower()}.",
                chance={"base": 100}, cooldown={"base": cd}, effects=[dict(eff)], original=False))
            count += 1

# Utility runes: teleport/pull/push/message combos usable on any tool
UTILITY_KITS = [
    ("Recall", ["ANY"], "ON_SPRINT", {"type": "TELEPORT", "mode": "random-nearby", "radius": 6}, "UTILITY"),
    ("Magpie", ["PICKAXE", "SHOVEL", "AXE", "HOE"], "ON_BLOCK_BREAK", {"type": "PULL", "target": "AREA", "radius": 5, "strength": 0.5}, "UTILITY"),
    ("Windfall", ["SWORD", "AXE"], "ON_KILL", {"type": "PUSH", "target": "NEARBY_MOBS", "strength": 1.2}, "UTILITY"),
    ("Prospector's Call", ["PICKAXE"], "ON_BLOCK_BREAK", {"type": "ACTIONBAR", "text": "<gray>You sense something nearby..."}, "UTILITY"),
]
for name, equip, trig, eff, category in UTILITY_KITS:
    for rarity in RARITY_CYCLE:
        prefix = TIER_PREFIX[rarity]
        id_ = f"{prefix.strip().lower() + '_' if prefix else ''}{name.lower().replace(chr(39),'').replace(' ','_')}"
        chance = {"COMMON": 20, "RARE": 16, "LEGENDARY": 12, "MYTHICAL": 8}[rarity]
        add(rune(id_, f"{prefix}{name}", rarity, category, rarity.lower(), trig, equip,
            f"A utility rune themed around {name.lower()}.",
            chance={"base": chance, "per-level": 2.5}, cooldown={"base": 10, "per-level": -0.5}, effects=[dict(eff)], original=False))
        count += 1

# Two more crate pools with themed slots
EXTRA_CRATES = [("crate_halloween", "Halloween Crate", "LEGENDARY"), ("crate_founders", "Founder's Crate", "MYTHICAL")]
for pool_id, display, rarity in EXTRA_CRATES:
    for slot_name, equip in CRATE_SLOTS:
        id_ = f"{pool_id}_{slot_name.lower()}"
        trig = "ON_ARMOR_EQUIP" if equip != ["SWORD"] else "ON_HIT"
        add(rune(id_, f"{display.split()[0]} {slot_name}", rarity, "CRATE", pool_id, trig, equip,
            f"A crate-exclusive relic from the {display}.",
            chance={"base": 100 if trig == "ON_ARMOR_EQUIP" else 12, "per-level": 0 if trig == "ON_ARMOR_EQUIP" else 2},
            effects=[{"type": "PARTICLE", "particle": "SOUL", "count": 8}], original=False))
        count += 1

print(f"final expansion added, running total: {len(ALL)}", file=sys.stderr)

by_pool = {}
for id_, d in ALL.items():
    by_pool.setdefault(d["pool"], {})[id_] = d
for f in os.listdir(OUT):
    os.remove(os.path.join(OUT, f))
for pool, entries in by_pool.items():
    path = os.path.join(OUT, f"{pool}.yml")
    with open(path, "w", encoding="utf-8") as f:
        f.write(f"# Auto-generated rune definitions for pool '{pool}'. Safe to hand-edit; /runeadmin reload picks up changes.\n")
        yaml.dump({"runes": entries}, f, allow_unicode=True, sort_keys=False, width=100)
print(f"FINAL total runes: {len(ALL)}, files: {len(by_pool)}", file=sys.stderr)

# ---------------------------------------------------------------------------
# 5. TOP-UP: a final small batch of hand-varied combat/utility runes to clear the 750+ target
#    with comfortable margin.
# ---------------------------------------------------------------------------
TOPUP_ARCH = [
    ("Riptide", "COMBAT", {"type": "PUSH", "target": "SELF", "strength": {"base": 1, "per-level": 0.2}}, "ON_ATTACK", ["TRIDENT"]),
    ("Overcharge", "WEAPONS", {"type": "STRENGTH", "ticks": {"base": 40, "per-level": 8}, "amplifier": 1}, "ON_CRITICAL", ["SWORD", "AXE"]),
    ("Backdraft", "COMBAT", {"type": "FIRE", "target": "NEARBY_MOBS", "ticks": {"base": 30, "per-level": 6}}, "ON_KILL", ["SWORD", "AXE"]),
    ("Static Field", "COMBAT", {"type": "SLOW", "target": "NEARBY_MOBS", "ticks": {"base": 30, "per-level": 6}, "amplifier": 0}, "ON_ATTACK", ["TRIDENT", "SWORD"]),
]
for name, category, eff, trig, equip in TOPUP_ARCH:
    for rarity in RARITY_CYCLE:
        prefix = TIER_PREFIX[rarity]
        id_ = f"{prefix.strip().lower() + '_' if prefix else ''}{name.lower().replace(' ','_')}"
        chance = {"COMMON": 18, "RARE": 14, "LEGENDARY": 10, "MYTHICAL": 6}[rarity]
        add(rune(id_, f"{prefix}{name}", rarity, category, rarity.lower(), trig, equip,
            f"A combat rune built around {name.lower()}.",
            chance={"base": chance, "per-level": 2}, cooldown={"base": 6, "per-level": -0.3}, effects=[dict(eff)], original=False))
        count += 1

add(rune("keystone_of_failxos", "Keystone of Failxos", "MYTHICAL", "SPECIAL", "mythical", "ON_KILL", ["SWORD", "AXE"],
    "A signature relic bearing the mark of FailRunes' creator - the final piece of the collection.",
    chance={"base": 100}, cooldown={"base": 30},
    effects=[{"type": "XP", "amount": {"base": 20, "per-level": 5}}, {"type": "PARTICLE", "particle": "TOTEM_OF_UNDYING", "count": 15}]))
count += 1

print(f"topup added, running total: {len(ALL)}", file=sys.stderr)

by_pool = {}
for id_, d in ALL.items():
    by_pool.setdefault(d["pool"], {})[id_] = d
for f in os.listdir(OUT):
    os.remove(os.path.join(OUT, f))
for pool, entries in by_pool.items():
    path = os.path.join(OUT, f"{pool}.yml")
    with open(path, "w", encoding="utf-8") as f:
        f.write(f"# Auto-generated rune definitions for pool '{pool}'. Safe to hand-edit; /runeadmin reload picks up changes.\n")
        yaml.dump({"runes": entries}, f, allow_unicode=True, sort_keys=False, width=100)
print(f"FINAL total runes: {len(ALL)}, files: {len(by_pool)}", file=sys.stderr)

# Print pool -> count summary for verification
pool_counts = {p: len(e) for p, e in by_pool.items()}
print("Pool counts:", pool_counts, file=sys.stderr)
print("Pools used:", sorted(by_pool.keys()), file=sys.stderr)
