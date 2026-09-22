package dev.failxos.failrunes.util;

import org.bukkit.entity.Player;

/** Exact vanilla XP maths: players pay with real experience points, not levels. */
public final class XpUtil {
    private XpUtil() {}

    private static int toNext(int level) {
        if (level <= 15) return 2 * level + 7;
        if (level <= 30) return 5 * level - 38;
        return 9 * level - 158;
    }
    private static long atLevel(int l) {
        if (l <= 16) return (long) l * l + 6L * l;
        if (l <= 31) return Math.round(2.5 * l * l - 40.5 * l + 360);
        return Math.round(4.5 * l * l - 162.5 * l + 2220);
    }
    public static long total(Player p) {
        return atLevel(p.getLevel()) + Math.round(p.getExp() * toNext(p.getLevel()));
    }
    public static void set(Player p, long total) {
        total = Math.max(0, total);
        p.setExp(0); p.setLevel(0); p.setTotalExperience(0);
        int level = 0;
        while (atLevel(level + 1) <= total) level++;
        p.setLevel(level);
        long rest = total - atLevel(level);
        int need = toNext(level);
        p.setExp(need <= 0 ? 0 : Math.min(0.999f, (float) rest / need));
    }
    public static boolean take(Player p, long amount) {
        long t = total(p);
        if (t < amount) return false;
        set(p, t - amount);
        return true;
    }
    public static void give(Player p, long amount) { set(p, total(p) + amount); }
}
