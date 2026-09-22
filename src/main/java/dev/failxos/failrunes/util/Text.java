package dev.failxos.failrunes.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Text {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private Text() {}

    public static Component mm(String s, TagResolver... r) { return MM.deserialize(s, r); }
    /** Non-italic component for item names / lore. */
    public static Component item(String s, TagResolver... r) {
        return Component.empty().decoration(TextDecoration.ITALIC, false).append(MM.deserialize(s, r));
    }
    public static String plain(Component c) { return PlainTextComponentSerializer.plainText().serialize(c); }
    public static String escape(String s) { return MM.escapeTags(s); }

    public static String num(double v) { return String.format(Locale.US, "%,.0f", v); }
    public static String dec(double v) {
        String s = String.format(Locale.US, "%.1f", v);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
    }
    public static String pct(double v) { return dec(v) + "%"; }

    public static List<String> wrap(String s, int width) {
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String w : s.split(" ")) {
            if (line.length() + w.length() + 1 > width && line.length() > 0) { out.add(line.toString()); line.setLength(0); }
            if (line.length() > 0) line.append(' ');
            line.append(w);
        }
        if (line.length() > 0) out.add(line.toString());
        return out;
    }
}
