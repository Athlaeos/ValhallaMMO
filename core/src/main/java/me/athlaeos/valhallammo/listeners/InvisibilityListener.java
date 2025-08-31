package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class InvisibilityListener implements Listener {
    private final Collection<UUID> entitiesToUpdateEquipment = new HashSet<>();

    public InvisibilityListener(){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskTimer(ValhallaMMO.getInstance(), () -> {
            for (UUID uuid : new HashSet<>(entitiesToUpdateEquipment)){
                Entity entity = ValhallaMMO.getInstance().getServer().getEntity(uuid);
                if (!(entity instanceof LivingEntity le) || entity.isDead() || !entity.isValid()) continue;

                EntityEquipment q = le.getEquipment();
                if (q == null) continue;
                EntityProperties properties = EntityCache.getAndCacheProperties(le);

                ItemStack helmet = q.getHelmet();
                ItemStack chestplate = q.getChestplate();
                ItemStack leggings = q.getLeggings();
                ItemStack boots = q.getBoots();
                entitiesToUpdateEquipment.remove(le.getUniqueId());
                boolean shouldBeVisibleInFOV = true;
                if (properties.getHelmetAttributes().containsKey("ARMOR_INVISIBILITY")) {
                    helmet = null;
                    shouldBeVisibleInFOV = properties.getHelmetAttributes().get("ARMOR_INVISIBILITY").getValue() < 0.0005;
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                if (properties.getChestPlateAttributes().containsKey("ARMOR_INVISIBILITY")) {
                    chestplate = null;
                    if (shouldBeVisibleInFOV) shouldBeVisibleInFOV = properties.getChestPlateAttributes().get("ARMOR_INVISIBILITY").getValue() < 0.0005;
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                if (properties.getLeggingsAttributes().containsKey("ARMOR_INVISIBILITY")) {
                    leggings = null;
                    if (shouldBeVisibleInFOV) shouldBeVisibleInFOV = properties.getLeggingsAttributes().get("ARMOR_INVISIBILITY").getValue() < 0.0005;
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                if (properties.getBootsAttributes().containsKey("ARMOR_INVISIBILITY")) {
                    boots = null;
                    if (shouldBeVisibleInFOV) shouldBeVisibleInFOV = properties.getBootsAttributes().get("ARMOR_INVISIBILITY").getValue() < 0.0005;
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                if (shouldBeVisibleInFOV) ValhallaMMO.getNms().sendArmorChange(le, helmet, chestplate, leggings, boots, exclusionRule);
                else ValhallaMMO.getNms().sendArmorChange(le, helmet, chestplate, leggings, boots);
            }
        }, 20, 20);
    }

    private static final BiPredicate<LivingEntity, LivingEntity> exclusionRule = (wearer, observer) ->
            EntityUtils.isEntityFacing(observer, wearer.getLocation(), EntityAttackListener.getFacingAngleCos()) ||
            EntityUtils.isEntityFacing(observer, wearer.getEyeLocation(), EntityAttackListener.getFacingAngleCos());

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInvisibilityChange(EntityPotionEffectEvent e){
        if (!(e.getEntity() instanceof LivingEntity le) || !e.getModifiedType().equals(PotionEffectMappings.INVISIBILITY.getPotionEffectType())) return;
        if (e.getAction() == EntityPotionEffectEvent.Action.CLEARED || e.getAction() == EntityPotionEffectEvent.Action.REMOVED){
            // lost invisibility
            EntityEquipment q = le.getEquipment();
            if (q == null) return;
            ValhallaMMO.getNms().resetArmorChange(le);
            entitiesToUpdateEquipment.remove(le.getUniqueId());
        } else {
            boolean shouldBeVisibleInFOV = true;
            CustomPotionEffect effect = PotionEffectRegistry.getActiveEffect(le, "ARMOR_INVISIBILITY");
            if (effect != null) {
                shouldBeVisibleInFOV = effect.getAmplifier() <= 0.995;
                entitiesToUpdateEquipment.add(le.getUniqueId());
                ValhallaMMO.getNms().sendArmorChange(le, null, null, null, null);
            } else {
                EntityEquipment q = le.getEquipment();
                entitiesToUpdateEquipment.remove(le.getUniqueId());
                if (q == null) return;
                EntityProperties properties = EntityCache.getAndCacheProperties(le);

                ItemStack helmet = q.getHelmet();
                ItemStack chestplate = q.getChestplate();
                ItemStack leggings = q.getLeggings();
                ItemStack boots = q.getBoots();
                if (properties.getHelmetAttributes().containsKey("ARMOR_INVISIBILITY")) {
                    helmet = null;
                    shouldBeVisibleInFOV = properties.getHelmetAttributes().get("ARMOR_INVISIBILITY").getValue() < 0.0005;
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                if (properties.getChestPlateAttributes().containsKey("ARMOR_INVISIBILITY")) {
                    chestplate = null;
                    if (shouldBeVisibleInFOV) shouldBeVisibleInFOV = properties.getChestPlateAttributes().get("ARMOR_INVISIBILITY").getValue() < 0.0005;
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                if (properties.getLeggingsAttributes().containsKey("ARMOR_INVISIBILITY")) {
                    leggings = null;
                    if (shouldBeVisibleInFOV) shouldBeVisibleInFOV = properties.getLeggingsAttributes().get("ARMOR_INVISIBILITY").getValue() < 0.0005;
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                if (properties.getBootsAttributes().containsKey("ARMOR_INVISIBILITY")) {
                    boots = null;
                    if (shouldBeVisibleInFOV) shouldBeVisibleInFOV = properties.getBootsAttributes().get("ARMOR_INVISIBILITY").getValue() < 0.0005;
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                if (shouldBeVisibleInFOV) ValhallaMMO.getNms().sendArmorChange(le, helmet, chestplate, leggings, boots, exclusionRule);
                else ValhallaMMO.getNms().sendArmorChange(le, helmet, chestplate, leggings, boots);
            }
        }
    }
}
