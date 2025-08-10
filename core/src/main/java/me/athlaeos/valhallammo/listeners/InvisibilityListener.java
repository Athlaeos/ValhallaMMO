package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
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
                if (properties.getHelmetAttributes().containsKey("ARMOR_INVISIBILITY")) {
                    helmet = null;
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                if (properties.getChestPlateAttributes().containsKey("ARMOR_INVISIBILITY")) {
                    chestplate = null;
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                if (properties.getLeggingsAttributes().containsKey("ARMOR_INVISIBILITY")) {
                    leggings = null;
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                if (properties.getBootsAttributes().containsKey("ARMOR_INVISIBILITY")) {
                    boots = null;
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                ValhallaMMO.getNms().sendArmorChange(le, helmet, chestplate, leggings, boots);
            }
        }, 20, 20);
    }

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
            if (PotionEffectRegistry.getActiveEffect(le, "ARMOR_INVISIBILITY") != null) {
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
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                if (properties.getChestPlateAttributes().containsKey("ARMOR_INVISIBILITY")) {
                    chestplate = null;
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                if (properties.getLeggingsAttributes().containsKey("ARMOR_INVISIBILITY")) {
                    leggings = null;
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                if (properties.getBootsAttributes().containsKey("ARMOR_INVISIBILITY")) {
                    boots = null;
                    entitiesToUpdateEquipment.add(le.getUniqueId());
                }
                ValhallaMMO.getNms().sendArmorChange(le, helmet, chestplate, leggings, boots);
            }
        }
    }
}
