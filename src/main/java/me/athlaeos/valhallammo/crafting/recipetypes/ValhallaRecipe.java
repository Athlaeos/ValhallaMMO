package me.athlaeos.valhallammo.crafting.recipetypes;

import org.bukkit.inventory.ItemStack;

public interface ValhallaRecipe {
    /**
     * @return The name and identifier of this recipe
     */
    String getName();

    /**
     * @return Whether this recipe should be accessible to everyone
     */
    boolean isUnlockedForEveryone();

    /**
     * @return The result of the recipe
     */
    ItemStack getResult();
}
