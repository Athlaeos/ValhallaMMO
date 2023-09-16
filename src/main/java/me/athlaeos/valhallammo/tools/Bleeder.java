package me.athlaeos.valhallammo.tools;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Bleeder {
    private static final Map<UUID, BleedingInstance> bleedingEntities = new HashMap<>();
    private static int delay = ConfigManager.getConfig("config.yml").get().getInt("bleed_delay", 40);

    public static void startBleedTask(){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskTimer(ValhallaMMO.getInstance(), () ->
                bleedingEntities.values().forEach(instance -> {
                    if (!instance.getBleedingEntity().isValid() || instance.getBleedingUntil() < System.currentTimeMillis()){
                        bleedingEntities.remove(instance.getBleedingEntity().getUniqueId());
                    } else {
                        EntityDamageEvent event = (instance.getCausedBy() == null) ?
                                new EntityDamageEvent(instance.getBleedingEntity(), EntityDamageEvent.DamageCause.DRYOUT, instance.getBleedingDamage()) :
                                new EntityDamageByEntityEvent(instance.getCausedBy(), instance.getBleedingEntity(), EntityDamageEvent.DamageCause.DRYOUT, instance.getBleedingDamage());

                        instance.getBleedingEntity().setLastDamageCause(event);
                        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
                        if (!event.isCancelled()){
                            int particleCount = (int) (3 * Math.min(10, event.getDamage()));
                            instance.getBleedingEntity().getWorld().spawnParticle(Particle.BLOCK_DUST, instance.getBleedingEntity().getEyeLocation().add(0, -(instance.getBleedingEntity().getHeight()/2), 0),
                                    particleCount, 0.4, 0.4, 0.4, Material.REDSTONE_BLOCK.createBlockData());
                            instance.getBleedingEntity().playEffect(EntityEffect.HURT);
                        }
                    }
                }), 0L, delay
        );

        ValhallaMMO.getInstance().getServer().getScheduler().runTaskTimer(ValhallaMMO.getInstance(), () ->
                bleedingEntities.values().forEach(instance ->
                        instance.getBleedingEntity().getWorld().spawnParticle(Particle.BLOCK_DUST, instance.getBleedingEntity().getEyeLocation().add(0, -(instance.getBleedingEntity().getHeight()/2), 0),
                                2, 0.4, 0.1, 0.1, Material.REDSTONE_BLOCK.createBlockData())
                ), 0L, 2
        );
    }

    public static void reload(){
        delay = ConfigManager.getConfig("config.yml").get().getInt("bleed_delay", 40);
    }

    public static void bleed(LivingEntity bleeder, LivingEntity causedBy, int duration, double damage){
        if (damage <= 0) return;
        BleedingInstance instance = bleedingEntities.get(bleeder.getUniqueId());
        if (instance != null && instance.getBleedingDamage() > damage) return;

        damage = Math.max(0, damage * (1 - AccumulativeStatManager.getRelationalStats("BLEED_RESISTANCE", bleeder, causedBy, true)));
        if (damage <= 0) return;
        bleedingEntities.put(bleeder.getUniqueId(), new BleedingInstance(bleeder, causedBy, duration, Math.max(0, damage)));
        bleeder.getWorld().spawnParticle(Particle.BLOCK_DUST, bleeder.getEyeLocation().add(0, -(bleeder.getHeight()/2), 0),
                25, 0.4, 0.4, 0.4, Material.REDSTONE_BLOCK.createBlockData());
    }

    public static void removeBleed(LivingEntity bleeder){
        bleedingEntities.remove(bleeder.getUniqueId());
    }

    private static class BleedingInstance{
        private final LivingEntity bleedingEntity;
        private final LivingEntity causedBy;
        private final long bleedingUntil;
        private final double bleedingDamage;

        public BleedingInstance(LivingEntity bleedingEntity, LivingEntity causedBy, int duration, double damagePerTick){
            this.bleedingEntity = bleedingEntity;
            this.causedBy = causedBy;
            this.bleedingUntil = System.currentTimeMillis() + (duration * 50L);
            this.bleedingDamage = damagePerTick;
        }

        public double getBleedingDamage() { return bleedingDamage; }
        public LivingEntity getBleedingEntity() { return bleedingEntity; }
        public LivingEntity getCausedBy() { return causedBy; }
        public long getBleedingUntil() { return bleedingUntil; }
    }
}
