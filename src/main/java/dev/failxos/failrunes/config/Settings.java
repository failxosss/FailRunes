package dev.failxos.failrunes.config;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.core.Chances;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Loads config.yml + rarities.yml + preferences.yml + database.yml. All numbers configurable, never hardcoded. */
public final class Settings {
    private final FailRunesPlugin plugin;
    private String language = "en_US";
    private String defaultGamemode = "skyblock";
    private final Map<String, GamemodeSettings> gamemodes = new LinkedHashMap<>();
    private Chances.GemFormula gemFormula = new Chances.GemFormula(10, 3, 100, 0.3);
    private boolean overmaxEnabled = false;
    private boolean mcmmoEnabled = true, worldguardEnabled = true, vaultEnabled = false;
    private int damageHistoryMax = 100;
    private long damageHistoryRetentionMs = 3600_000L;
    private int gemsMaxApply = 3;
    private String databaseType = "SQLITE";
    private String mysqlHost = "localhost"; private int mysqlPort = 3306; private String mysqlDb = "failrunes",
            mysqlUser = "root", mysqlPass = "";

    public Settings(FailRunesPlugin plugin) { this.plugin = plugin; }

    public void load() {
        plugin.saveDefaultConfig();
        for (String f : new String[]{"runes.yml", "rarities.yml", "pools.yml", "loot-tables.yml", "messages.yml",
                "gui.yml", "items.yml", "effects.yml", "preferences.yml", "zones.yml", "database.yml", "compatibility.yml"})
            saveIfMissing(f);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        language = cfg.getString("language", "en_US");
        defaultGamemode = cfg.getString("default-gamemode", "skyblock");
        overmaxEnabled = cfg.getBoolean("overmax.enabled", false);
        gemsMaxApply = cfg.getInt("lucky-gems.max-per-application", 3);
        gemFormula = new Chances.GemFormula(
                cfg.getDouble("lucky-gems.bonus-per-gem", 10),
                gemsMaxApply,
                cfg.getDouble("lucky-gems.max-success", 100),
                cfg.getDouble("lucky-gems.critical-fail-share", 0.3));
        damageHistoryMax = cfg.getInt("damage-history.max-events", 100);
        damageHistoryRetentionMs = cfg.getLong("damage-history.retention-seconds", 3600) * 1000L;

        gamemodes.clear();
        ConfigurationSection gm = cfg.getConfigurationSection("gamemodes");
        if (gm != null) for (String id : gm.getKeys(false)) {
            ConfigurationSection s = gm.getConfigurationSection(id);
            Map<String, Long> costs = new HashMap<>();
            for (String r : new String[]{"common", "rare", "legendary", "mythical"}) costs.put(r, s.getLong(r, 0));
            gamemodes.put(id.toLowerCase(), new GamemodeSettings(id, costs,
                    s.getLong("lucky-gem", 500), s.getLong("identify", 250), s.getLong("cleansing-wand", 1000),
                    s.getDouble("incinerate-return-percent", 50)));
        }
        if (gamemodes.isEmpty()) {
            Map<String, Long> sky = Map.of("common", 6250L, "rare", 25000L, "legendary", 62500L, "mythical", 125000L);
            gamemodes.put("skyblock", new GamemodeSettings("skyblock", sky, 500, 250, 1000, 50));
        }

        YamlConfiguration comp = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "compatibility.yml"));
        mcmmoEnabled = comp.getBoolean("mcmmo", true);
        worldguardEnabled = comp.getBoolean("worldguard", true);
        vaultEnabled = comp.getBoolean("vault", false);

        YamlConfiguration db = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "database.yml"));
        databaseType = db.getString("type", "SQLITE").toUpperCase();
        mysqlHost = db.getString("mysql.host", "localhost");
        mysqlPort = db.getInt("mysql.port", 3306);
        mysqlDb = db.getString("mysql.database", "failrunes");
        mysqlUser = db.getString("mysql.username", "root");
        mysqlPass = db.getString("mysql.password", "");
    }
    private void saveIfMissing(String f) { if (!new File(plugin.getDataFolder(), f).exists()) plugin.saveResource(f, false); }

    public String language() { return language; }
    public String defaultGamemode() { return defaultGamemode; }
    public Map<String, GamemodeSettings> gamemodes() { return gamemodes; }
    public GamemodeSettings gamemode(String id) { return gamemodes.getOrDefault(id == null ? "" : id.toLowerCase(), gamemodes.get(defaultGamemode)); }
    public Chances.GemFormula gemFormula() { return gemFormula; }
    public boolean overmaxEnabled() { return overmaxEnabled; }
    public int gemsMaxApply() { return gemsMaxApply; }
    public int damageHistoryMax() { return damageHistoryMax; }
    public long damageHistoryRetentionMs() { return damageHistoryRetentionMs; }
    public boolean mcmmoEnabled() { return mcmmoEnabled; }
    public boolean worldguardEnabled() { return worldguardEnabled; }
    public boolean vaultEnabled() { return vaultEnabled; }
    public String databaseType() { return databaseType; }
    public String mysqlHost() { return mysqlHost; }
    public int mysqlPort() { return mysqlPort; }
    public String mysqlDb() { return mysqlDb; }
    public String mysqlUser() { return mysqlUser; }
    public String mysqlPass() { return mysqlPass; }
}
