package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCookingRecipe;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicGridRecipe;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicSmithingRecipe;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;

public class RecipeDiscoveryListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRecipeDiscover(PlayerRecipeDiscoverEvent e){
        if (CustomRecipeRegistry.getDisabledRecipes().contains(e.getRecipe())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        for (NamespacedKey key : CustomRecipeRegistry.getDisabledRecipes()){
            e.getPlayer().undiscoverRecipe(key);
        }
        PowerProfile profile = ProfileRegistry.getMergedProfile(e.getPlayer(), PowerProfile.class);
        for (DynamicGridRecipe recipe : CustomRecipeRegistry.getGridRecipes().values()){
            if (profile.getUnlockedRecipes().contains(recipe.getName()))e.getPlayer().discoverRecipe(recipe.getKey()); // all recipes have a shaped variant because these display properly in the recipe book
            else e.getPlayer().undiscoverRecipe(recipe.getKey());

            e.getPlayer().undiscoverRecipe(recipe.getKey2()); // shapeless recipes cant contain custom metadata options and so will always display wrongly in the recipe book, forget those
        }
        for (DynamicSmithingRecipe recipe : CustomRecipeRegistry.getSmithingRecipes().values()){
            if (profile.getUnlockedRecipes().contains(recipe.getName())) e.getPlayer().discoverRecipe(recipe.getKey());
            else e.getPlayer().undiscoverRecipe(recipe.getKey());
        }
        for (DynamicCookingRecipe recipe : CustomRecipeRegistry.getCookingRecipes().values()){
            if (profile.getUnlockedRecipes().contains(recipe.getName())) e.getPlayer().discoverRecipe(recipe.getKey());
            else e.getPlayer().undiscoverRecipe(recipe.getKey());
        }
    }
}
