import dev.failxos.failrunes.api.*;
import dev.failxos.failrunes.core.*;
import java.util.*;

public class CoreTest {
    static int failures = 0;
    static void check(boolean ok, String what) { if (!ok) { failures++; System.out.println("FAIL: " + what); } else System.out.println("ok:   " + what); }
    static boolean near(double a, double b) { return Math.abs(a - b) < 1e-9; }

    public static void main(String[] a) {
        var f = new Chances.GemFormula(10, 3, 100, 0.3);
        Chances base = Chances.compute(80, 5, 0, f);
        check(near(base.success(), 80) && near(base.fail(), 15) && near(base.critical(), 5), "base 80/15/5");
        Chances g1 = Chances.compute(80, 5, 1, f);
        check(near(g1.success(), 90) && near(g1.fail(), 8) && near(g1.critical(), 2), "1 gem => 90/8/2 (spec example)");
        Chances g3 = Chances.compute(80, 5, 3, f);
        check(near(g3.success() + g3.fail() + g3.critical(), 100) && near(g3.success(), 100), "3 gems capped at 100, sum=100");
        var cap = new Chances.GemFormula(10, 3, 95, 0.3);
        check(near(Chances.compute(80, 5, 3, cap).success(), 95), "maxSuccess respected");
        check(near(Chances.compute(80, 5, 99, f).success(), 100), "maxGems respected");
        Chances c = new Chances(80, 15, 5);
        check(c.roll(0) == RuneResult.SUCCESS && c.roll(79.99) == RuneResult.SUCCESS, "roll success band");
        check(c.roll(80) == RuneResult.FAIL && c.roll(94.99) == RuneResult.FAIL, "roll fail band");
        check(c.roll(95) == RuneResult.CRITICAL_FAIL && c.roll(99.99) == RuneResult.CRITICAL_FAIL, "roll critical band");

        Scaling s = new Scaling(8, 2);
        check(near(s.at(1), 8) && near(s.at(5), 16), "scaling L1=8, L5=16");
        check(near(Scaling.parse(3, null).at(9), 3), "scaling plain number");
        check(near(Scaling.parse(Map.of("base", 5, "per-level", 1.5), null).at(3), 8), "scaling map");

        WeightedTable<String> t = new WeightedTable<String>().add("a", 10).add("b", 15).add("c", 5);
        Random r = new Random(42); Map<String, Integer> n = new HashMap<>();
        for (int i = 0; i < 300000; i++) n.merge(t.roll(r), 1, Integer::sum);
        check(Math.abs(n.get("b") / 300000.0 - 0.5) < 0.01, "weighted table b ~ 50%");
        check(Math.abs(n.get("c") / 300000.0 - 1.0 / 6) < 0.01, "weighted table c ~ 16.7%");
        check(near(t.chance(t.entries().get(0)), 10.0 / 30 * 100), "chance() percent");
        check(new WeightedTable<String>().roll(r) == null, "empty table -> null");

        EffectSpec e = EffectSpec.parse(Map.of("type", "damage", "amount", Map.of("base", 4, "per-level", 1), "target", "MOB", "of-damage", true));
        check(e.type().equals("DAMAGE") && near(e.num("amount", 0, 3), 6) && e.str("target", "").equals("MOB") && e.bool("of-damage"), "EffectSpec parse");
        check(RuneTrigger.parse("on_hit", null) == RuneTrigger.ON_HIT && RuneRarity.parse("zzz", RuneRarity.COMMON) == RuneRarity.COMMON, "enum parse");
        System.out.println(failures == 0 ? "ALL PASSED" : failures + " FAILED");
        System.exit(failures == 0 ? 0 : 1);
    }
}
