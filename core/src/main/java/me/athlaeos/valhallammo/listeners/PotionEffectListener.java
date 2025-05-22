package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.Map;

public class PotionEffectListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent e){
        ItemStack potion = e.getPotion().getItem();
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || ItemUtils.isEmpty(potion)) return;
        Collection<PotionEffectWrapper> effects = PotionEffectRegistry.getStoredEffects(ItemUtils.getItemMeta(potion), false).values();
        LivingEntity thrower = e.getEntity().getShooter() instanceof LivingEntity l ? l : null;
        double minimumIntensity = 0;
        if (thrower != null) minimumIntensity = AccumulativeStatManager.getCachedStats("SPLASH_INTENSITY_MINIMUM", thrower, 10000, true);

        for (LivingEntity entity : e.getAffectedEntities()){
            double intensity = e.getIntensity(entity);
            if (minimumIntensity > 0) {
                e.setIntensity(entity, Math.max(intensity, minimumIntensity));
                intensity = Math.max(intensity, minimumIntensity);
            }
            for (PotionEffectWrapper effect : effects){
                PotionEffectRegistry.addEffect(entity, thrower, new CustomPotionEffect(effect, (int) (intensity * effect.getDuration()), effect.getAmplifier()), false, intensity, EntityPotionEffectEvent.Cause.POTION_SPLASH);
            }
        }
    }

    private final NamespacedKey potionCloudKey = new NamespacedKey(ValhallaMMO.getInstance(), "lingering_custom_effects");

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionLinger(LingeringPotionSplashEvent e){
        ItemStack potion = e.getEntity().getItem();
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || ItemUtils.isEmpty(potion)) return;
        ItemMeta potionMeta = ItemUtils.getItemMeta(potion);
        if (potionMeta == null) return;
        if (e.getEntity().getShooter() instanceof LivingEntity l){
            float radiusMultiplier = 1 + (float) AccumulativeStatManager.getCachedStats("LINGERING_RADIUS_MULTIPLIER", l, 10000, true);
            float durationMultiplier = 1 + (float) AccumulativeStatManager.getCachedStats("LINGERING_DURATION_MULTIPLIER", l, 10000, true);
            e.getAreaEffectCloud().setRadius(e.getAreaEffectCloud().getRadius() * radiusMultiplier);
            e.getAreaEffectCloud().setDuration((int) (e.getAreaEffectCloud().getDuration() * durationMultiplier));
        }

        String encodedEffects = PotionEffectRegistry.getRawData(potionMeta, false);
        if (!StringUtils.isEmpty(encodedEffects)){
            e.getAreaEffectCloud().getPersistentDataContainer().set(potionCloudKey, PersistentDataType.STRING, encodedEffects);
            e.getAreaEffectCloud().addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 0, 0, false, false, false), false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLingeringCloudHit(AreaEffectCloudApplyEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName())) return;
        AreaEffectCloud cloud = e.getEntity();
        String cloudString = cloud.getPersistentDataContainer().getOrDefault(potionCloudKey, PersistentDataType.STRING, "");
        if (StringUtils.isEmpty(cloudString)) return;
        LivingEntity thrower = e.getEntity().getSource() instanceof LivingEntity l ? l : null;

        Map<String, PotionEffectWrapper> wrappers = PotionEffectRegistry.parseRawData(cloudString);
        if (wrappers.isEmpty()) return;
        for (LivingEntity entity : e.getAffectedEntities()){
            for (PotionEffectWrapper wrapper : wrappers.values()){
                if (wrapper.isVanilla()) continue;
                int duration = (int) Math.floor(wrapper.getDuration() / 4D); // the duration displayed on the item will not
                // match the actual effect applied with vanilla potion effects. Arrows have their duration reduced to 1/8th
                PotionEffectRegistry.addEffect(entity, thrower, new CustomPotionEffect(wrapper, duration, wrapper.getAmplifier()), false, 1, EntityPotionEffectEvent.Cause.ARROW);
            }
        }
    }
}
