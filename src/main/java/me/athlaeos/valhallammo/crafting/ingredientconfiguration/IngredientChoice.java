package me.athlaeos.valhallammo.crafting.ingredientconfiguration;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public interface IngredientChoice {

    /**
     * @return The recipechoice this ingredient should have
     */
    RecipeChoice getChoice(ItemStack i);

    /**
     * @return True if the required ingredient (i1) matches the grid ItemStack (i2)
     */
    boolean matches(ItemStack i1, ItemStack i2);

    /**
     * @return The description of the ingredients required. Visible to players, so should be translatable
     */
    String ingredientDescription(ItemStack base);

}
