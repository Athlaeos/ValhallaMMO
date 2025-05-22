package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.FoodClass;
import me.athlaeos.valhallammo.item.FoodPropertyManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.EffectClass;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class ItemConsumptionListener implements Listener {
    private final Collection<UUID> cancelNextFoodEffects = new HashSet<>();
    private final Collection<UUID> cancelNextFoodEvent = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent e){
        ItemStack item = e.getItem();
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || ItemUtils.isEmpty(item)) return;
        Player p = e.getPlayer();
        for (PotionEffectWrapper wrapper : PotionEffectRegistry.getStoredEffects(ItemUtils.getItemMeta(item), false).values()){
            if (!wrapper.isVanilla()) PotionEffectRegistry.addEffect(p, null, new CustomPotionEffect(wrapper, (int) wrapper.getDuration(), wrapper.getAmplifier()), false, 1, EntityPotionEffectEvent.Cause.POTION_DRINK);
            else p.addPotionEffect(new PotionEffect(wrapper.getVanillaEffect(), (int) wrapper.getDuration(), (int) wrapper.getAmplifier()));
        }

        ItemMeta meta = ItemUtils.getItemMeta(item);
        if (meta == null) return;
        FoodClass type = FoodPropertyManager.getFoodClass(item, meta);
        double multiplier = 1 + ((type == null ? 0 : AccumulativeStatManager.getCachedStats("FOOD_BONUS_" + type, e.getPlayer(), 10000, true)));

        int hungerBefore = e.getPlayer().getFoodLevel();
        float saturationBefore = e.getPlayer().getSaturation();
        if (FoodPropertyManager.isCustomFood(meta)){
            if (FoodPropertyManager.shouldCancelDefaultPotionEffects(meta)) cancelNextFoodEffects.add(e.getPlayer().getUniqueId());
            cancelNextFoodEvent.add(e.getPlayer().getUniqueId());

            int foodToReplenish = FoodPropertyManager.getFoodValue(item, meta);
            float saturationToReplenish = FoodPropertyManager.getSaturationValue(item, meta);

            foodToReplenish = (int) Math.round(multiplier * foodToReplenish);
            saturationToReplenish *= (float) multiplier;

            int finalFoodToReplenish = foodToReplenish;
            float finalSaturationToReplenish = saturationBefore + saturationToReplenish;
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                cancelNextFoodEffects.remove(e.getPlayer().getUniqueId());
                cancelNextFoodEvent.remove(e.getPlayer().getUniqueId());
                FoodLevelChangeEvent event = new FoodLevelChangeEvent(e.getPlayer(), Math.max(0, Math.min(20, hungerBefore + finalFoodToReplenish)), item);
                ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()){
                    e.getPlayer().setFoodLevel(Math.max(0, Math.min(20, event.getFoodLevel())));
                    e.getPlayer().setSaturation(Math.max(0, Math.min(20, finalSaturationToReplenish)));
                }
            }, 1L);
        } else {
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                int hungerDifference = e.getPlayer().getFoodLevel() - hungerBefore;
                float saturationDifference = e.getPlayer().getSaturation() - saturationBefore;
                int newFoodLevel = hungerBefore + (int) Math.round(hungerDifference * multiplier);
                float newSaturationLevel = saturationBefore + (saturationDifference * (float) multiplier);
                FoodLevelChangeEvent event = new FoodLevelChangeEvent(e.getPlayer(), Math.max(0, Math.min(20, newFoodLevel)), item);
                ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()){
                    e.getPlayer().setFoodLevel(Math.max(0, Math.min(20, event.getFoodLevel())));
                    e.getPlayer().setSaturation(Math.max(0, Math.min(20, newSaturationLevel)));
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void cancelHunger(FoodLevelChangeEvent e){
        if (cancelNextFoodEvent.contains(e.getEntity().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHungerChange(FoodLevelChangeEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName())) return;
        if (e.getFoodLevel() < e.getEntity().getFoodLevel()){
            // entity lost hunger
            double chance = AccumulativeStatManager.getCachedStats("HUNGER_SAVE_CHANCE", e.getEntity(), 10000, true);
            if (chance >= 0){
                if (Utils.proc(e.getEntity(), chance, false)) e.setCancelled(true);
            } else {
                int foodDifference = e.getFoodLevel() - e.getEntity().getFoodLevel();
                // food lost is a negative integer
                foodDifference = Utils.randomAverage((double) foodDifference * -(1-chance));
                e.setFoodLevel(Math.max(0, Math.min(20, e.getEntity().getFoodLevel() - foodDifference)));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodPotionEffect(EntityPotionEffectEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof Player p)) return;
        if (e.getCause() == EntityPotionEffectEvent.Cause.FOOD) {
            PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
            if (cancelNextFoodEffects.contains(e.getEntity().getUniqueId())) e.setCancelled(true);
            else if (e.getNewEffect() != null && profile.isBadFoodImmune() && EffectClass.getClass(e.getNewEffect().getType()) == EffectClass.DEBUFF) e.setCancelled(true);
        }
    }
}
