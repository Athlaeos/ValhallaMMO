package me.athlaeos.valhallammo.crafting.recipetypes;

import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.HashSet;
import java.util.Set;

/**
 * Assigns the vanilla recipe-book category (Equipment / Building / Redstone / Misc) to ValhallaMMO recipes based on
 * their result, so weapons, tools and armor show up under the correct tab instead of all landing in "Misc".
 * <p>
 * IMPORTANT: this class references {@link CraftingBookCategory}, which only exists on Minecraft 1.19.3+. It is
 * deliberately kept in its own class so the JVM never has to load it on older servers. Callers MUST gate every
 * reference to this class behind a version check (see callers in {@code DynamicGridRecipe}); because the class is only
 * loaded the first time one of its methods is invoked, an older server that never passes the version check will never
 * trigger class verification/resolution here, avoiding a {@code NoClassDefFoundError}.
 */
public final class RecipeBookCategorizer {

    private RecipeBookCategorizer(){}

    // Redstone has no single Bukkit property that identifies it, so it stays an explicit set.
    // Built through ItemUtils.getMaterialSet so unknown names on older versions are silently skipped.
    private static final Set<Material> REDSTONE_ITEMS = new HashSet<>(ItemUtils.getMaterialSet(
            "REDSTONE", "REDSTONE_BLOCK", "REDSTONE_TORCH", "REDSTONE_LAMP", "REPEATER", "COMPARATOR",
            "PISTON", "STICKY_PISTON", "OBSERVER", "HOPPER", "DROPPER", "DISPENSER", "LEVER", "TARGET",
            "DAYLIGHT_DETECTOR", "TRIPWIRE_HOOK", "NOTE_BLOCK"));

    public static void apply(ShapedRecipe recipe){
        recipe.setCategory(categoryFor(recipe.getResult()));
    }

    public static void apply(ShapelessRecipe recipe){
        recipe.setCategory(categoryFor(recipe.getResult()));
    }

    private static CraftingBookCategory categoryFor(ItemStack item){
        if (ItemUtils.isEmpty(item)) return CraftingBookCategory.MISC;
        Material type = item.getType();
        // Weapons, tools and armor: EquipmentClass already maps every vanilla + ValhallaMMO equipment
        // material, so we reuse it instead of pattern-matching material names by hand.
        if (EquipmentClass.getMatchingClass(type) != null) return CraftingBookCategory.EQUIPMENT;
        // Redstone components (curated set, see above).
        if (REDSTONE_ITEMS.contains(type) || type.name().endsWith("_BUTTON") ||
                type.name().endsWith("_PRESSURE_PLATE") || type.name().endsWith("_RAIL"))
            return CraftingBookCategory.REDSTONE;
        // Anything else that's a placeable block goes under Building Blocks.
        if (type.isBlock()) return CraftingBookCategory.BUILDING;
        return CraftingBookCategory.MISC;
    }
}
