package me.athlaeos.valhallammo.crafting.recipetypes;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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
