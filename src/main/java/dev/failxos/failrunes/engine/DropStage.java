package dev.failxos.failrunes.engine;

import java.util.function.Consumer;

/** One step of the central drop pipeline. Lower priority runs first. */
public record DropStage(int priority, String name, Consumer<DropContext> action) {}
