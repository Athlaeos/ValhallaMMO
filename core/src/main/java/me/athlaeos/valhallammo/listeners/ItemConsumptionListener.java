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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExhaustionEvent;
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
        if (e.getItem().getType() == Material.MILK_BUCKET) PotionEffectRegistry.addEffect(p, null, new CustomPotionEffect(PotionEffectRegistry.getEffect("MILK"), 1, 1), true, 1, EntityPotionEffectEvent.Cause.MILK);

        ItemMeta meta = ItemUtils.getItemMeta(item);
        if (meta == null) return;
        Collection<FoodClass> types = FoodPropertyManager.getFoodClasses(item, meta);
        double multiplier = 1;
        int foodNutritionAddition = 0;
        float foodSaturationAddition = 0;
        if (types != null){
            for (FoodClass type : types){
                multiplier *= (1 + AccumulativeStatManager.getCachedStats("FOOD_BONUS_" + type, e.getPlayer(), 10000, true));
                foodNutritionAddition += (int) Math.round(AccumulativeStatManager.getCachedStats("FOOD_NUTRITION_ADDITION_" + type, e.getPlayer(), 10000, true));
                foodSaturationAddition += (float) (AccumulativeStatManager.getCachedStats("FOOD_SATURATION_ADDITION_" + type, e.getPlayer(), 10000, true));
            }
        }

        int hungerBefore = e.getPlayer().getFoodLevel();
        float saturationBefore = e.getPlayer().getSaturation();
        if (FoodPropertyManager.isCustomFood(meta)){
            if (FoodPropertyManager.shouldCancelDefaultPotionEffects(meta)) cancelNextFoodEffects.add(e.getPlayer().getUniqueId());
            cancelNextFoodEvent.add(e.getPlayer().getUniqueId());

            int foodToReplenish = FoodPropertyManager.getFoodValue(item, meta) + foodNutritionAddition;
            float saturationToReplenish = FoodPropertyManager.getSaturationValue(item, meta) + foodSaturationAddition;

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
            double finalMultiplier = multiplier;
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                int hungerDifference = e.getPlayer().getFoodLevel() - hungerBefore;
                float saturationDifference = e.getPlayer().getSaturation() - saturationBefore;
                int newFoodLevel = hungerBefore + (int) Math.round(hungerDifference * finalMultiplier);
                float newSaturationLevel = saturationBefore + (saturationDifference * (float) finalMultiplier);
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
    public void onHungerEvent(EntityExhaustionEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof Player p)) return;
        if (e.getExhaustion() > 0){
            double chance = AccumulativeStatManager.getCachedStats("HUNGER_SAVE_CHANCE", e.getEntity(), 10000, true);
            float exhaustion = (float) (e.getExhaustion() * (1 / (1 + Math.max(-0.999F, chance))));
            e.setExhaustion(exhaustion);
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
