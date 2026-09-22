package dev.failxos.failrunes.api;

/** One pluggable effect. Register with EffectRegistry#register(name, effect); no engine changes needed. */
@FunctionalInterface
public interface RuneEffect {
    void apply(RuneContext ctx, EffectSpec spec);
}
