package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
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

public class PotionEffectListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPotionSplash(PotionSplashEvent e){
        ItemStack potion = e.getPotion().getItem();
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled() || ItemUtils.isEmpty(potion)) return;
        Collection<PotionEffectWrapper> effects = PotionEffectRegistry.getStoredEffects(ItemUtils.getItemMeta(potion), false).values();
        for (LivingEntity entity : e.getAffectedEntities()){
            double intensity = e.getIntensity(entity);
            for (PotionEffectWrapper effect : effects){
                PotionEffectRegistry.addEffect(entity, new CustomPotionEffect(effect, (int) (intensity * effect.getDuration()), effect.getAmplifier()), false, EntityPotionEffectEvent.Cause.POTION_SPLASH);
            }
        }
    }

    private final NamespacedKey potionCloudKey = new NamespacedKey(ValhallaMMO.getInstance(), "lingering_custom_effects");

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPotionLinger(LingeringPotionSplashEvent e){
        ItemStack potion = e.getEntity().getItem();
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled() || ItemUtils.isEmpty(potion)) return;
        ItemMeta potionMeta = ItemUtils.getItemMeta(potion);
        if (potionMeta == null) return;


        if (!e.isCancelled()){
            String encodedEffects = PotionEffectRegistry.getRawData(potionMeta, false);
            if (!StringUtils.isEmpty(encodedEffects)){
                e.getAreaEffectCloud().getPersistentDataContainer().set(potionCloudKey, PersistentDataType.STRING, encodedEffects);
                e.getAreaEffectCloud().addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 0, 0, false, false, false), false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLingeringCloudHit(AreaEffectCloudApplyEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        AreaEffectCloud cloud = e.getEntity();
        String cloudString = cloud.getPersistentDataContainer().getOrDefault(potionCloudKey, PersistentDataType.STRING, "");
        if (StringUtils.isEmpty(cloudString)) return;

        for (LivingEntity entity : e.getAffectedEntities()){
            for (PotionEffectWrapper wrapper : PotionEffectRegistry.parseRawData(cloudString).values()){
                int duration = (int) Math.floor(wrapper.getDuration() / 4D); // the duration displayed on the item will not
                // match the actual effect applied with vanilla potion effects. Arrows have their duration reduced to 1/8th
                if (wrapper.isVanilla()) entity.addPotionEffect(new PotionEffect(wrapper.getVanillaEffect(), duration, (int) wrapper.getAmplifier(), false));
                else PotionEffectRegistry.addEffect(entity, new CustomPotionEffect(wrapper, duration, wrapper.getAmplifier()), false, EntityPotionEffectEvent.Cause.ARROW);
            }
        }
        e.setCancelled(true);
    }
}
