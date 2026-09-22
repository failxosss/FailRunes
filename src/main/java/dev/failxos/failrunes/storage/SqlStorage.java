package dev.failxos.failrunes.storage;

import dev.failxos.failrunes.FailRunesPlugin;
import dev.failxos.failrunes.api.RunePreference;

import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** SQLite (default) or MySQL/MariaDB storage. All I/O runs on the async scheduler. */
public final class SqlStorage implements Storage {
    private final FailRunesPlugin plugin;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private Connection sqliteConn; // SQLite: one shared connection; MySQL: one per operation via DriverManager pool-free connect

    public SqlStorage(FailRunesPlugin plugin) { this.plugin = plugin; }
    public Map<UUID, PlayerData> cache() { return cache; }

    @Override public void init() {
        try (Connection c = open()) {
            try (Statement st = c.createStatement()) {
                st.execute("""
                    CREATE TABLE IF NOT EXISTS failrunes_players (
                        uuid VARCHAR(36) PRIMARY KEY,
                        discovered TEXT, detector_level INTEGER DEFAULT 0,
                        success_c BIGINT DEFAULT 0, fail_c BIGINT DEFAULT 0, critical_c BIGINT DEFAULT 0,
                        destroyed_c BIGINT DEFAULT 0, gems_used BIGINT DEFAULT 0, incinerated_c BIGINT DEFAULT 0,
                        cleansed_c BIGINT DEFAULT 0, detector_finds BIGINT DEFAULT 0, xp_spent BIGINT DEFAULT 0,
                        xp_recovered BIGINT DEFAULT 0, prefs_global INTEGER DEFAULT -1, prefs_per_rune TEXT
                    )""");
            }
        } catch (SQLException e) { plugin.getLogger().severe("[FailRunes] DB init failed: " + e.getMessage()); }
    }
    private Connection open() throws SQLException {
        if ("MYSQL".equals(plugin.settings().databaseType()) || "MARIADB".equals(plugin.settings().databaseType())) {
            String url = "jdbc:mysql://" + plugin.settings().mysqlHost() + ":" + plugin.settings().mysqlPort()
                    + "/" + plugin.settings().mysqlDb() + "?useSSL=false&autoReconnect=true";
            return DriverManager.getConnection(url, plugin.settings().mysqlUser(), plugin.settings().mysqlPass());
        }
        if (sqliteConn == null || sqliteConn.isClosed()) {
            java.io.File f = new java.io.File(plugin.getDataFolder(), "failrunes.db");
            sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + f.getAbsolutePath());
        }
        return sqliteConn;
    }
    @Override public void close() { try { if (sqliteConn != null) sqliteConn.close(); } catch (SQLException ignored) {} }

    @Override public PlayerData load(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement("SELECT * FROM failrunes_players WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    for (String id : rs.getString("discovered") == null ? new String[0] : rs.getString("discovered").split(",")) if (!id.isBlank()) data.discover(id);
                    data.detectorLevel(rs.getInt("detector_level"));
                    data.stats(new PlayerData.Stats(rs.getLong("success_c"), rs.getLong("fail_c"), rs.getLong("critical_c"),
                            rs.getLong("destroyed_c"), rs.getLong("gems_used"), rs.getLong("incinerated_c"), rs.getLong("cleansed_c"),
                            rs.getLong("detector_finds"), rs.getLong("xp_spent"), rs.getLong("xp_recovered"), null, null));
                    int g = rs.getInt("prefs_global");
                    data.globalPref(g <= 0 ? RunePreference.DEFAULT : g);
                    String per = rs.getString("prefs_per_rune");
                    if (per != null) for (String part : per.split(";")) {
                        String[] kv = part.split(":");
                        if (kv.length == 2) try { data.pref(kv[0], Integer.parseInt(kv[1])); } catch (NumberFormatException ignored) {}
                    }
                    data.clearDirty();
                }
            }
        } catch (SQLException e) { plugin.getLogger().warning("[FailRunes] load failed for " + uuid + ": " + e.getMessage()); }
        cache.put(uuid, data);
        return data;
    }
    @Override public void save(PlayerData d) {
        String discovered = String.join(",", d.discovered());
        StringBuilder per = new StringBuilder();
        for (var e : d.allPrefs().entrySet()) { if (per.length() > 0) per.append(';'); per.append(e.getKey()).append(':').append(e.getValue()); }
        String sql = """
            INSERT INTO failrunes_players (uuid, discovered, detector_level, success_c, fail_c, critical_c, destroyed_c,
                gems_used, incinerated_c, cleansed_c, detector_finds, xp_spent, xp_recovered, prefs_global, prefs_per_rune)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            ON CONFLICT(uuid) DO UPDATE SET discovered=excluded.discovered, detector_level=excluded.detector_level,
                success_c=excluded.success_c, fail_c=excluded.fail_c, critical_c=excluded.critical_c,
                destroyed_c=excluded.destroyed_c, gems_used=excluded.gems_used, incinerated_c=excluded.incinerated_c,
                cleansed_c=excluded.cleansed_c, detector_finds=excluded.detector_finds, xp_spent=excluded.xp_spent,
                xp_recovered=excluded.xp_recovered, prefs_global=excluded.prefs_global, prefs_per_rune=excluded.prefs_per_rune
            """;
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(mysqlIfNeeded(sql))) {
            var st = d.stats();
            ps.setString(1, d.uuid.toString());
            ps.setString(2, discovered);
            ps.setInt(3, d.detectorLevel());
            ps.setLong(4, st.success()); ps.setLong(5, st.fail()); ps.setLong(6, st.critical()); ps.setLong(7, st.destroyed());
            ps.setLong(8, st.gemsUsed()); ps.setLong(9, st.incinerated()); ps.setLong(10, st.cleansed()); ps.setLong(11, st.detectorFinds());
            ps.setLong(12, st.xpSpent()); ps.setLong(13, st.xpRecovered());
            ps.setInt(14, d.globalPref());
            ps.setString(15, per.toString());
            ps.executeUpdate();
            d.clearDirty();
        } catch (SQLException e) { plugin.getLogger().warning("[FailRunes] save failed for " + d.uuid + ": " + e.getMessage()); }
    }
    private String mysqlIfNeeded(String sqliteSql) {
        if (!plugin.settings().databaseType().startsWith("MY") && !plugin.settings().databaseType().equals("MARIADB")) return sqliteSql;
        return sqliteSql.replace(
                "ON CONFLICT(uuid) DO UPDATE SET discovered=excluded.discovered, detector_level=excluded.detector_level,\n" +
                "                success_c=excluded.success_c, fail_c=excluded.fail_c, critical_c=excluded.critical_c,\n" +
                "                destroyed_c=excluded.destroyed_c, gems_used=excluded.gems_used, incinerated_c=excluded.incinerated_c,\n" +
                "                cleansed_c=excluded.cleansed_c, detector_finds=excluded.detector_finds, xp_spent=excluded.xp_spent,\n" +
                "                xp_recovered=excluded.xp_recovered, prefs_global=excluded.prefs_global, prefs_per_rune=excluded.prefs_per_rune",
                "ON DUPLICATE KEY UPDATE discovered=VALUES(discovered), detector_level=VALUES(detector_level), " +
                "success_c=VALUES(success_c), fail_c=VALUES(fail_c), critical_c=VALUES(critical_c), destroyed_c=VALUES(destroyed_c), " +
                "gems_used=VALUES(gems_used), incinerated_c=VALUES(incinerated_c), cleansed_c=VALUES(cleansed_c), " +
                "detector_finds=VALUES(detector_finds), xp_spent=VALUES(xp_spent), xp_recovered=VALUES(xp_recovered), " +
                "prefs_global=VALUES(prefs_global), prefs_per_rune=VALUES(prefs_per_rune)");
    }
    @Override public void loadAsync(UUID uuid, java.util.function.Consumer<PlayerData> cb) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            PlayerData d = load(uuid);
            plugin.getServer().getGlobalRegionScheduler().run(plugin, t2 -> cb.accept(d));
        });
    }
    @Override public void saveAsync(PlayerData d) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> save(d));
    }
    public PlayerData cached(UUID uuid) { return cache.get(uuid); }
    public PlayerData cachedOrLoad(UUID uuid) { return cache.computeIfAbsent(uuid, this::load); }
    public void unload(UUID uuid) { PlayerData d = cache.remove(uuid); if (d != null) save(d); }
}
