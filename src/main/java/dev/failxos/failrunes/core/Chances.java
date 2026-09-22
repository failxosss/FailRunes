package dev.failxos.failrunes.core;

import dev.failxos.failrunes.api.RuneResult;

/** Success / fail / critical-fail percentages (sum = 100) and the configurable Lucky Gem formula. */
public record Chances(double success, double fail, double critical) {

    /**
     * @param bonusPerGem  percentage points of success added per gem
     * @param maxGems      gems that count at most
     * @param maxSuccess   success can never exceed this (e.g. 100)
     * @param critShare    share of the bonus taken from critical-fail (rest comes from fail)
     */
    public record GemFormula(double bonusPerGem, int maxGems, double maxSuccess, double critShare) {}

    public static Chances compute(double baseSuccess, double baseCrit, int gems, GemFormula f) {
        double s = clamp(baseSuccess), c = clamp(baseCrit);
        double fail = Math.max(0, 100 - s - c);
        double bonus = Math.min(Math.max(0, gems), f.maxGems()) * f.bonusPerGem();
        bonus = Math.max(0, Math.min(bonus, Math.min(f.maxSuccess() - s, fail + c)));
        double takeCrit = Math.min(c, bonus * f.critShare());
        double takeFail = Math.min(fail, bonus - takeCrit);
        double spill = bonus - takeCrit - takeFail;
        takeCrit += Math.min(spill, c - takeCrit);
        return new Chances(s + takeCrit + takeFail, fail - takeFail, c - takeCrit);
    }

    /** @param roll uniform value in [0,100) */
    public RuneResult roll(double roll) {
        if (roll < success) return RuneResult.SUCCESS;
        if (roll < success + fail) return RuneResult.FAIL;
        return RuneResult.CRITICAL_FAIL;
    }
    private static double clamp(double v) { return Math.max(0, Math.min(100, v)); }
}
