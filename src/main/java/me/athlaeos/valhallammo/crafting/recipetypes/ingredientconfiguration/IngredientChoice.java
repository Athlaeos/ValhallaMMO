package me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public interface IngredientChoice {

    /**
     * @return The recipechoice this ingredient should have
     */
    RecipeChoice getChoice(ItemStack i, boolean isShapeless);

    /**
     * @return True if the required ingredient (i1) matches the grid ItemStack (i2)
     */
    boolean matches(ItemStack i1, ItemStack i2);
}
