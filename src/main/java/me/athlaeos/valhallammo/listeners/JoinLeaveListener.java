package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCookingRecipe;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicGridRecipe;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicSmithingRecipe;
import me.athlaeos.valhallammo.entities.EntityAttributeStats;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.utility.GlobalEffect;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;

public class JoinLeaveListener implements Listener {
    private final NamespacedKey HEALTH = new NamespacedKey(ValhallaMMO.getInstance(), "cached_health");

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!e.getPlayer().isOnline()) return;
        ProfileRegistry.getPersistence().loadProfile(e.getPlayer());
        EntityCache.getAndCacheProperties(e.getPlayer());
        PotionEffectRegistry.updatePlayerAffectedStatus(e.getPlayer());
        GlobalEffect.temporarilyRevealBossBar(e.getPlayer());
        EntityAttributeStats.updateStats(e.getPlayer());

        double health = e.getPlayer().getPersistentDataContainer().getOrDefault(HEALTH, PersistentDataType.DOUBLE, -1D);
        if (health > 0){
            AttributeInstance maxHealth = e.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth != null){
                e.getPlayer().setHealth(Math.max(maxHealth.getValue(), health));
            }
        }

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
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        // the following code is to remove valhallammo's attribute modifiers off players when they log off
        // this is to prevent, in the case valhallammo is being uninstalled, no unintended attributes remain
        // stuck on the player.
        EntityUtils.removeUniqueAttribute(e.getPlayer(), "valhalla_negative_knockback_taken", Attribute.GENERIC_KNOCKBACK_RESISTANCE);

        e.getPlayer().getPersistentDataContainer().set(HEALTH, PersistentDataType.DOUBLE, e.getPlayer().getHealth());
        for (EntityAttributeStats.AttributeDataHolder holder : EntityAttributeStats.getAttributesToUpdate().values()){
            EntityUtils.removeUniqueAttribute(e.getPlayer(), holder.name(), holder.type());
        }

        EntityUtils.removeUniqueAttribute(e.getPlayer(), "armor_nullifier", Attribute.GENERIC_ARMOR);
        EntityUtils.removeUniqueAttribute(e.getPlayer(), "armor_display", Attribute.GENERIC_ARMOR);

        ProfileRegistry.getPersistence().saveProfile(e.getPlayer());
    }
}
