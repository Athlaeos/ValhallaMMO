package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCookingRecipe;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicGridRecipe;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicSmithingRecipe;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.utility.GlobalEffect;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            if (!e.getPlayer().isOnline()) return;
            ProfileRegistry.getPersistence().loadProfile(e.getPlayer());
            EntityCache.getAndCacheProperties(e.getPlayer());
            PotionEffectRegistry.updatePlayerAffectedStatus(e.getPlayer());
            GlobalEffect.temporarilyRevealBossBar(e.getPlayer());

            // TODO tutorial book giving
            PowerProfile p = ProfileCache.getOrCache(e.getPlayer(), PowerProfile.class);
            boolean allPermission = e.getPlayer().hasPermission("valhalla.allrecipes");
            for (DynamicGridRecipe recipe : CustomRecipeRegistry.getGridRecipes().values()){
                if (recipe.isUnlockedForEveryone() || p.getUnlockedRecipes().contains(recipe.getName()) || allPermission)
                    e.getPlayer().discoverRecipe(recipe.getKey());
                else e.getPlayer().undiscoverRecipe(recipe.getKey());
            }
            for (DynamicCookingRecipe recipe : CustomRecipeRegistry.getCookingRecipes().values()){
                if (recipe.isUnlockedForEveryone() || p.getUnlockedRecipes().contains(recipe.getName()) || allPermission)
                    e.getPlayer().discoverRecipe(recipe.getKey());
                else e.getPlayer().undiscoverRecipe(recipe.getKey());
            }
            for (DynamicSmithingRecipe recipe : CustomRecipeRegistry.getSmithingRecipes().values()){
                if (recipe.isUnlockedForEveryone() || p.getUnlockedRecipes().contains(recipe.getName()) || allPermission)
                    e.getPlayer().discoverRecipe(recipe.getKey());
                else e.getPlayer().undiscoverRecipe(recipe.getKey());
            }
        }, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        // the following code is to remove valhallammo's attribute modifiers off players when they log off
        // this is to prevent, in the case valhallammo is being uninstalled, no unintended attributes remain
        // stuck on the player.
        EntityUtils.removeUniqueAttribute(e.getPlayer(), "valhalla_negative_knockback_taken", Attribute.GENERIC_KNOCKBACK_RESISTANCE);

        for (MovementListener.AttributeDataHolder holder : MovementListener.getAttributesToUpdate().values()){
            EntityUtils.removeUniqueAttribute(e.getPlayer(), holder.name(), holder.type());
        }

        EntityUtils.removeUniqueAttribute(e.getPlayer(), "armor_nullifier", Attribute.GENERIC_ARMOR);
        EntityUtils.removeUniqueAttribute(e.getPlayer(), "armor_display", Attribute.GENERIC_ARMOR);

        ProfileRegistry.getPersistence().saveProfile(e.getPlayer());
    }
}
