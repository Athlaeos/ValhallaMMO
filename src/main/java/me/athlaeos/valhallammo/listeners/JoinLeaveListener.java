package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCookingRecipe;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicGridRecipe;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicSmithingRecipe;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileManager;
import me.athlaeos.valhallammo.skills.skills.implementations.power.PowerProfile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            if (!e.getPlayer().isOnline()) return;
            ProfileManager.getPersistence().loadProfile(e.getPlayer());

            // TODO world blacklisting
            // TODO tutorial book giving
            // TODO global effect boss bar revealing
            PowerProfile p = ProfileCache.getOrCache(e.getPlayer(), PowerProfile.class);
            for (DynamicGridRecipe recipe : CustomRecipeRegistry.getGridRecipes().values()){
                if (recipe.isUnlockedForEveryone() || p.getUnlockedRecipes().contains(recipe.getName()) || e.getPlayer().hasPermission("valhalla.allrecipes"))
                    e.getPlayer().discoverRecipe(recipe.getKey());
                else e.getPlayer().undiscoverRecipe(recipe.getKey());
            }
            for (DynamicCookingRecipe recipe : CustomRecipeRegistry.getCookingRecipes().values()){
                if (recipe.isUnlockedForEveryone() || p.getUnlockedRecipes().contains(recipe.getName()) || e.getPlayer().hasPermission("valhalla.allrecipes"))
                    e.getPlayer().discoverRecipe(recipe.getKey());
                else e.getPlayer().undiscoverRecipe(recipe.getKey());
            }
            for (DynamicSmithingRecipe recipe : CustomRecipeRegistry.getSmithingRecipes().values()){
                if (recipe.isUnlockedForEveryone() || p.getUnlockedRecipes().contains(recipe.getName()) || e.getPlayer().hasPermission("valhalla.allrecipes"))
                    e.getPlayer().discoverRecipe(recipe.getKey());
                else e.getPlayer().undiscoverRecipe(recipe.getKey());
            }
        }, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        // TODO remove unique attributes

        ProfileManager.getPersistence().saveProfile(e.getPlayer());
    }
}
