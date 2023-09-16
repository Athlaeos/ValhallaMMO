package me.athlaeos.valhallammo.crafting.recipetypes;

import org.bukkit.NamespacedKey;

public interface ValhallaKeyedRecipe {
    NamespacedKey getKey();
    void registerRecipe();
    void unregisterRecipe();
}
