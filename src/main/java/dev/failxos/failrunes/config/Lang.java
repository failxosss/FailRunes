package dev.failxos.failrunes.config;

import dev.failxos.failrunes.FailRunesPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/** Fully configurable, translatable messages. No message is ever hardcoded in Java. */
public final class Lang {
    private final FailRunesPlugin plugin;
    private YamlConfiguration cfg = new YamlConfiguration();
    private YamlConfiguration fallback = new YamlConfiguration();

    public Lang(FailRunesPlugin plugin) { this.plugin = plugin; }

    public void load() {
        String code = plugin.settings().language();
        File dir = new File(plugin.getDataFolder(), "lang");
        dir.mkdirs();
        File target = new File(dir, code + ".yml");
        if (!target.exists()) plugin.saveResource("lang/" + code + ".yml", false);
        if (!target.exists()) plugin.saveResource("lang/en_US.yml", false);
        cfg = YamlConfiguration.loadConfiguration(target);
        try (InputStream in = plugin.getResource("lang/en_US.yml")) {
            if (in != null) fallback.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException ignored) {}
    }
    public String get(String key, Object... placeholders) {
        String raw = cfg.getString(key, fallback.getString(key, key));
        return format(raw, placeholders);
    }
    public List<String> list(String key) {
        List<String> l = cfg.getStringList(key);
        return l.isEmpty() ? fallback.getStringList(key) : l;
    }

    /** Message templates may contain {rune} which is replaced with a live, hoverable component (not text). */
    public net.kyori.adventure.text.Component getComponent(String key, net.kyori.adventure.text.Component rune, Object... placeholders) {
        String raw = format(cfg.getString(key, fallback.getString(key, key)), placeholders);
        int i = raw.indexOf("{rune}");
        if (i < 0) return dev.failxos.failrunes.util.Text.mm(raw);
        net.kyori.adventure.text.Component before = dev.failxos.failrunes.util.Text.mm(raw.substring(0, i));
        net.kyori.adventure.text.Component after = dev.failxos.failrunes.util.Text.mm(raw.substring(i + 6));
        return before.append(rune).append(after);
    }
    private String format(String s, Object... ph) {
        if (s == null) return "";
        for (int i = 0; i + 1 < ph.length; i += 2) s = s.replace("{" + ph[i] + "}", String.valueOf(ph[i + 1]));
        return s;
    }
}
