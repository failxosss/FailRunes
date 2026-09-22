package dev.failxos.failrunes.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Centralized pipeline every rune-generated drop passes through (requirement #28):
 * Rune -> drop event -> loot modification -> MCMMO bonus -> rune modifiers -> protection/region checks -> final reward.
 * No individual rune bypasses this unless explicitly marked "raw" in its config.
 */
public final class DropPipeline {
    private final List<DropStage> stages = new ArrayList<>();

    public void register(DropStage stage) {
        stages.add(stage);
        stages.sort(Comparator.comparingInt(DropStage::priority));
    }
    public void run(DropContext ctx) {
        for (DropStage s : stages) s.action().accept(ctx);
    }
}
