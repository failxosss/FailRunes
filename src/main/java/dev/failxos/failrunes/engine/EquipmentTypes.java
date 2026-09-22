package dev.failxos.failrunes.engine;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/** Maps items to equipment types by material name, so vanilla, copper and custom (ItemsAdder) gear all work. */
public final class EquipmentTypes {
    private EquipmentTypes() {}
    private static final Map<Material, String> EXTRA = new EnumMap<>(Material.class);
    public static final Set<String> ARMOR = Set.of("HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS");
    private static final Set<String> WEAPON = Set.of("SWORD", "AXE", "BOW", "CROSSBOW", "TRIDENT");
    private static final Set<String> TOOL = Set.of("PICKAXE", "SHOVEL", "HOE", "FISHING_ROD", "AXE");

    public static void setExtra(Map<Material, String> m) { EXTRA.clear(); EXTRA.putAll(m); }

    public static String of(ItemStack it) { return it == null ? null : of(it.getType()); }
    public static String of(Material m) {
        String e = EXTRA.get(m);
        if (e != null) return e;
        String n = m.name();
        if (n.endsWith("_SWORD")) return "SWORD";
        if (n.endsWith("_PICKAXE")) return "PICKAXE";
        if (n.endsWith("_AXE")) return "AXE";
        if (n.endsWith("_SHOVEL")) return "SHOVEL";
        if (n.endsWith("_HOE")) return "HOE";
        if (n.equals("BOW")) return "BOW";
        if (n.equals("CROSSBOW")) return "CROSSBOW";
        if (n.equals("TRIDENT")) return "TRIDENT";
        if (n.equals("FISHING_ROD")) return "FISHING_ROD";
        if (n.endsWith("_HELMET") || n.equals("TURTLE_HELMET")) return "HELMET";
        if (n.endsWith("_CHESTPLATE") || n.equals("ELYTRA")) return "CHESTPLATE";
        if (n.endsWith("_LEGGINGS")) return "LEGGINGS";
        if (n.endsWith("_BOOTS")) return "BOOTS";
        return null;
    }
    public static boolean isArmor(String type) { return ARMOR.contains(type); }

    /** allowed may contain concrete types or the groups WEAPON, TOOL, ARMOR, ANY. */
    public static boolean matches(Set<String> allowed, String type) {
        if (type == null) return false;
        if (allowed.contains("ANY") || allowed.contains(type)) return true;
        return (allowed.contains("WEAPON") && WEAPON.contains(type))
                || (allowed.contains("TOOL") && TOOL.contains(type))
                || (allowed.contains("ARMOR") && ARMOR.contains(type));
    }
}
