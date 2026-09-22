package dev.failxos.failrunes.engine;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

/** Furnace results indexed once so that auto-smelting never iterates recipes at runtime. */
public final class SmeltCache {
    private final Map<Material, ItemStack> map = new EnumMap<>(Material.class);

    public void rebuild() {
        map.clear();
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            Recipe r = it.next();
            if (!(r instanceof FurnaceRecipe fr)) continue;
            if (fr.getInputChoice() instanceof RecipeChoice.MaterialChoice mc)
                for (Material m : mc.getChoices()) map.putIfAbsent(m, fr.getResult());
        }
    }
    public ItemStack result(Material m) { ItemStack s = map.get(m); return s == null ? null : s.clone(); }
}
