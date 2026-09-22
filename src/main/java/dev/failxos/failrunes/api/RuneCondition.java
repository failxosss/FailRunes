package dev.failxos.failrunes.api;

/** A predicate evaluated before a rune may proc. Register custom keys via ConditionParser.register. */
@FunctionalInterface
public interface RuneCondition {
    boolean test(RuneContext ctx);
}
