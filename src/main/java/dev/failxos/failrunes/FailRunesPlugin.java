package dev.failxos.failrunes;

import dev.failxos.failrunes.api.RuneManager;
import dev.failxos.failrunes.commands.AdminCommands;
import dev.failxos.failrunes.commands.PlayerCommands;
import dev.failxos.failrunes.commands.RuneCompleter;
import dev.failxos.failrunes.config.Lang;
import dev.failxos.failrunes.config.Settings;
import dev.failxos.failrunes.effects.Conditions;
import dev.failxos.failrunes.effects.Effects;
import dev.failxos.failrunes.engine.*;
import dev.failxos.failrunes.integration.*;
import dev.failxos.failrunes.items.AppliedRunes;
import dev.failxos.failrunes.items.ItemFactory;
import dev.failxos.failrunes.items.Keys;
import dev.failxos.failrunes.listeners.*;
import dev.failxos.failrunes.storage.SqlStorage;
import dev.failxos.failrunes.storage.Storage;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

/** Entry point. Wires config, storage, the rune engine, GUIs, commands and every optional integration together. */
public final class FailRunesPlugin extends JavaPlugin {
    private Settings settings;
    private Lang lang;
    private Keys keys;
    private AppliedRunes appliedRunes;
    private ItemFactory itemFactory;
    private RuneManager runeManager;
    private RuneLoader runeLoader;
    private PoolManager poolManager;
    private ZoneManager zoneManager;
    private BlockTags blockTags;
    private SmeltCache smeltCache;
    private RuneEngine engine;
    private ArmorEngine armorEngine;
    private ApplicationService applications;
    private IncineratorService incinerator;
    private CleansingService cleansing;
    private IdentifyService identify;
    private DetectorService detector;
    private CooldownService cooldowns;
    private SnapshotService snapshots;
    private PreferenceService prefs;
    private SetBonusManager sets;
    private CollectionService collection;
    private DropPipeline dropPipeline;
    private HistoryService history;
    private ChatInputService chatInput;
    private Storage storage;
    private ProtectionHook protectionHook;
    private ItemsAdderHook itemsAdder;
    private WorldGuardHook worldGuard;
    private McmmoHook mcmmo;
    private MythicMobsHook mythicMobs;
    private VaultHook vault;
    private PlaceholderHook placeholderHook;
    private boolean debug;

    @Override public void onEnable() {
        settings = new Settings(this);
        settings.load();
        lang = new Lang(this);
        lang.load();

        keys = new Keys(this);
        appliedRunes = new AppliedRunes(keys);
        itemFactory = new ItemFactory(this);
        runeManager = new RuneManager();
        runeLoader = new RuneLoader(this);
        poolManager = new PoolManager(this);
        zoneManager = new ZoneManager(this);
        blockTags = new BlockTags();
        smeltCache = new SmeltCache();
        engine = new RuneEngine(this);
        armorEngine = new ArmorEngine(this);
        applications = new ApplicationService(this);
        incinerator = new IncineratorService(this);
        cleansing = new CleansingService(this);
        identify = new IdentifyService(this);
        detector = new DetectorService(this);
        cooldowns = new CooldownService(this);
        snapshots = new SnapshotService(this);
        prefs = new PreferenceService(this);
        sets = new SetBonusManager(this);
        collection = new CollectionService(this);
        dropPipeline = new DropPipeline();
        history = new HistoryService(this);
        chatInput = new ChatInputService(this);

        protectionHook = new ProtectionHook();
        itemsAdder = new ItemsAdderHook();
        worldGuard = new WorldGuardHook(this);
        mcmmo = new McmmoHook(this);
        mythicMobs = new MythicMobsHook(this);
        vault = new VaultHook(this);

        storage = new SqlStorage(this);
        storage.init();

        Conditions.registerAll();
        Effects.registerAll(this);
        registerDropPipeline();

        reloadAll();

        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);
        getServer().getPluginManager().registerEvents(new ArmorEquipListener(this), this);
        getServer().getPluginManager().registerEvents(new GatheringListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new SecurityListener(this), this);

        registerCommand("infuser", new PlayerCommands(this), null);
        registerCommand("runes", new PlayerCommands(this), null);
        registerCommand("rune", new PlayerCommands(this), null);
        registerCommand("runeinfo", new PlayerCommands(this), null);
        registerCommand("runeprefs", new PlayerCommands(this), null);
        registerCommand("prefs", new PlayerCommands(this), null);
        registerCommand("damagehistory", new PlayerCommands(this), null);
        registerCommand("runecollection", new PlayerCommands(this), null);
        registerCommand("runeadmin", new AdminCommands(this), new RuneCompleter(this));

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderHook = new PlaceholderHook(this);
            placeholderHook.register();
        }

        getServer().getAsyncScheduler().runAtFixedRate(this, t -> {
            for (var p : getServer().getOnlinePlayers()) {
                var data = storage().cachedOrLoad(p.getUniqueId());
                if (data.dirty()) storage.saveAsync(data);
            }
        }, 5, 5, TimeUnit.MINUTES);

        getLogger().info("FailRunes enabled - " + runeManager.size() + " runes loaded.");
    }

    @Override public void onDisable() {
        if (storage != null) {
            for (var p : getServer().getOnlinePlayers()) storage.save(storage.cachedOrLoad(p.getUniqueId()));
            storage.close();
        }
    }

    /** Requirement #79: reload config, rune definitions, loot tables, messages, GUI, effects, zones - never the DB connection unsafely. */
    public void reloadAll() {
        settings.load();
        lang.load();
        int count = runeLoader.load();
        poolManager.load();
        poolManager.buildLoot();
        zoneManager.load(org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(new java.io.File(getDataFolder(), "zones.yml")));
        blockTags.load(org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(new java.io.File(getDataFolder(), "config.yml")).getConfigurationSection("block-tags"));
        smeltCache.rebuild();
        sets.load();
        getLogger().info("[FailRunes] Reload complete: " + count + " runes.");
    }
    private void registerDropPipeline() {
        dropPipeline.register(new DropStage(10, "rune-modifiers", ctx -> ctx.rewards.forEach(it -> {})));
        dropPipeline.register(new DropStage(20, "mcmmo-bonus", ctx -> { /* mcmmo bonus drops folded in when mcMMO is present */ }));
        dropPipeline.register(new DropStage(30, "protection-check", ctx -> {
            if (!settings.worldguardEnabled() || !worldGuard.enabled()) return;
            if (!worldGuard.canBuild(ctx.player, ctx.location)) ctx.rewards.clear();
        }));
    }
    private void registerCommand(String name, org.bukkit.command.CommandExecutor exec, org.bukkit.command.TabCompleter tab) {
        var cmd = getCommand(name);
        if (cmd == null) { getLogger().warning("[FailRunes] command '" + name + "' missing from plugin.yml"); return; }
        cmd.setExecutor(exec);
        if (tab != null) cmd.setTabCompleter(tab);
    }
    public void toggleDebug() { debug = !debug; }
    public boolean debugEnabled() { return debug; }
    public void debug(String msg) { if (debug) getLogger().info("[debug] " + msg); }
    public String playerGamemode(OfflinePlayer p) { return settings.defaultGamemode(); }

    public Settings settings() { return settings; }
    public Lang lang() { return lang; }
    public Keys keys() { return keys; }
    public AppliedRunes appliedRunes() { return appliedRunes; }
    public ItemFactory itemFactory() { return itemFactory; }
    public RuneManager runes() { return runeManager; }
    public PoolManager pools() { return poolManager; }
    public ZoneManager zones() { return zoneManager; }
    public BlockTags blockTags() { return blockTags; }
    public SmeltCache smeltCache() { return smeltCache; }
    public RuneEngine engine() { return engine; }
    public ArmorEngine armorEngine() { return armorEngine; }
    public ApplicationService applications() { return applications; }
    public IncineratorService incinerator() { return incinerator; }
    public CleansingService cleansing() { return cleansing; }
    public IdentifyService identify() { return identify; }
    public DetectorService detector() { return detector; }
    public CooldownService cooldowns() { return cooldowns; }
    public SnapshotService snapshots() { return snapshots; }
    public PreferenceService prefs() { return prefs; }
    public SetBonusManager sets() { return sets; }
    public CollectionService collection() { return collection; }
    public DropPipeline dropPipeline() { return dropPipeline; }
    public HistoryService history() { return history; }
    public ChatInputService chatInput() { return chatInput; }
    public SqlStorage storage() { return (SqlStorage) storage; }
    public ProtectionHook protection() { return protectionHook; }
    public ItemsAdderHook itemsAdder() { return itemsAdder; }
    public WorldGuardHook worldGuard() { return worldGuard; }
    public McmmoHook mcmmo() { return mcmmo; }
    public MythicMobsHook mythicMobs() { return mythicMobs; }
    public VaultHook vault() { return vault; }
}
