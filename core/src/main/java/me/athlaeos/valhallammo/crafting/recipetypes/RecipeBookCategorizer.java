package me.athlaeos.valhallammo.crafting.recipetypes;

import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Version-safe resolver for the default recipe-book category of a grid recipe.
 * <p>
 * It returns the category name as a plain {@link String} ("EQUIPMENT" / "BUILDING" / "REDSTONE" / "MISC"), so this class
 * never references the 1.19.3+ {@code CraftingBookCategory} API and is safe to load on any supported version. The actual
 * assignment to the Bukkit recipe (which does need that API) lives in the NMS layer
 * ({@code NMS#applyRecipeBookCategory}), which is overridden as a no-op on the 1.19 - 1.19.2 module.
 * <p>
 * This is used purely as a <i>default</i>: a recipe may store an explicit category, and only falls back here when none
 * is set (see {@code DynamicGridRecipe#getEffectiveBookCategory()}).
 */
public final class RecipeBookCategorizer {

    private RecipeBookCategorizer(){}

    public static final String EQUIPMENT = "EQUIPMENT";
    public static final String BUILDING = "BUILDING";
    public static final String REDSTONE = "REDSTONE";
    public static final String MISC = "MISC";

    /** Selectable explicit categories, in cycle order. "Automatic" is represented by a null category on the recipe. */
    public static final List<String> CATEGORIES = List.of(EQUIPMENT, BUILDING, REDSTONE, MISC);

    // Redstone has no single Bukkit property that identifies it, so it stays an explicit set.
    // Built through ItemUtils.getMaterialSet so unknown names on older versions are silently skipped.
    private static final Set<Material> REDSTONE_ITEMS = new HashSet<>(ItemUtils.getMaterialSet(
            "REDSTONE", "REDSTONE_BLOCK", "REDSTONE_TORCH", "REDSTONE_LAMP", "REPEATER", "COMPARATOR",
            "PISTON", "STICKY_PISTON", "OBSERVER", "HOPPER", "DROPPER", "DISPENSER", "LEVER", "TARGET",
            "DAYLIGHT_DETECTOR", "TRIPWIRE_HOOK", "NOTE_BLOCK"));

    /**
     * Auto-detects the most fitting category for a recipe result, used when a recipe has no explicit category.
     * @param item the recipe result
     * @return one of {@link #EQUIPMENT}, {@link #BUILDING}, {@link #REDSTONE} or {@link #MISC}
     */
    public static String defaultCategoryFor(ItemStack item){
        if (ItemUtils.isEmpty(item)) return MISC;
        Material type = item.getType();
        // Weapons, tools and armor: EquipmentClass already maps every vanilla + ValhallaMMO equipment
        // material, so we reuse it instead of pattern-matching material names by hand.
        if (EquipmentClass.getMatchingClass(type) != null) return EQUIPMENT;
        // Redstone components (curated set, see above).
        if (REDSTONE_ITEMS.contains(type) || type.name().endsWith("_BUTTON") ||
                type.name().endsWith("_PRESSURE_PLATE") || type.name().endsWith("_RAIL"))
            return REDSTONE;
        // Anything else that's a placeable block goes under Building Blocks.
        if (type.isBlock()) return BUILDING;
        return MISC;
    }
}
