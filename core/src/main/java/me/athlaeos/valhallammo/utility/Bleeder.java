package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.CombatType;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.event.EntityBleedEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.athlaeos.valhallammo.utility.Utils.oldOrNew;

public class Bleeder {
    private static final Map<UUID, BleedingInstance> bleedingEntities = new HashMap<>();
    private static int delay = ValhallaMMO.getPluginConfig().getInt("bleed_delay", 40);
    private static final int stackedDelayReduction = ValhallaMMO.getPluginConfig().getInt("bleed_stacked_delay", -5);
    private static final int maxStacks = ValhallaMMO.getPluginConfig().getInt("bleed_max_stacks", 5);

    public static void reload(){
        delay = ConfigManager.getConfig("config.yml").reload().get().getInt("bleed_delay", 40);
    }

    /**
     * Attempts a bleed effect on the entity. Not chance-based
     * @param bleeder the entity to bleed
     * @param causedBy the entity causing the bleed
     */
    public static void inflictBleed(LivingEntity bleeder, Entity causedBy, CombatType combatType){
        double bleedDamage = AccumulativeStatManager.getCachedAttackerRelationalStats("BLEED_DAMAGE", bleeder, causedBy, 10000, true);
        if (bleedDamage <= 0) return;
        int bleedDuration = (int) AccumulativeStatManager.getCachedAttackerRelationalStats("BLEED_DURATION", bleeder, causedBy, 10000, true);
        BleedingInstance instance = bleedingEntities.get(bleeder.getUniqueId());
        if (instance != null) inflictBleed(bleeder, causedBy, bleedDuration, bleedDamage, Math.min(maxStacks, instance.stacks + 1), combatType);
        else inflictBleed(bleeder, causedBy, bleedDuration, bleedDamage, 1, combatType);
    }

    /**
     * Inflicts a stack of bleed, respecting the configured max stacked cap.
     * @param bleeder the entity to bleed
     * @param causedBy the entity causing the bleed
     * @param duration the duration to bleed
     * @param damage the bleed damage per tick
     * @param combatType the combat type that caused the bleed
     */
    public static void inflictBleed(LivingEntity bleeder, Entity causedBy, int duration, double damage, CombatType combatType){
        if (damage <= 0) return;
        BleedingInstance instance = bleedingEntities.get(bleeder.getUniqueId());
        if (instance != null) inflictBleed(bleeder, causedBy, duration, damage, Math.min(maxStacks, instance.stacks + 1), combatType);
        else inflictBleed(bleeder, causedBy, duration, damage, 1, combatType);
    }

    /**
     * Inflicts the given amount of stacks of bleed, not respecting the configured max stack cap.
     * @param bleeder the entity to bleed
     * @param causedBy the entity causing the bleed
     * @param duration the duration to bleed
     * @param damage the bleed damage per tick
     * @param stacks the amount of stacks of bleed. Each stack causes the bleed to tick faster and therefore do more damage, up to once per tick.
     * @param combatType the combat type that caused the bleed
     */
    public static void inflictBleed(LivingEntity bleeder, Entity causedBy, int duration, double damage, int stacks, CombatType combatType){
        if (damage <= 0) return;
        if (bleeder instanceof Player p && (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR)) return;
        if (bleeder instanceof Player p && WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_COMBAT_BLEED)) return;
        else if (WorldGuardHook.inDisabledRegion(bleeder.getLocation(), WorldGuardHook.VMMO_COMBAT_BLEED)) return;
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            BleedingInstance instance = bleedingEntities.get(bleeder.getUniqueId());
            double resistance = AccumulativeStatManager.getCachedRelationalStats("BLEED_RESISTANCE", bleeder, causedBy, 10000, true);
            EntityBleedEvent event = new EntityBleedEvent(bleeder, causedBy, combatType, damage, resistance, duration, stacks);
            ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()){
                if (event.getBleedResistance() >= 1) return;
                if (instance == null) instance = new BleedingInstance(bleeder, causedBy, event.getDuration(), event.getBleedDamage() * (1 + event.getBleedResistance()));
                else {
                    instance.setDuration(Math.max(event.getDuration(), instance.duration));
                    instance.setBleedingDamage(Math.max(event.getBleedDamage() * (1 + event.getBleedResistance()), instance.bleedingDamage));
                }
                instance.setStacks(event.getStack());
                bleeder.getWorld().spawnParticle(Particle.valueOf(oldOrNew("BLOCK_DUST", "BLOCK")), bleeder.getEyeLocation().add(0, -(bleeder.getHeight()/2), 0),
                        25, 0.4, 0.4, 0.4, Material.REDSTONE_BLOCK.createBlockData());
                if (!bleedingEntities.containsKey(bleeder.getUniqueId())) {
                    bleedingEntities.put(bleeder.getUniqueId(), instance);
                    instance.runTaskTimer(ValhallaMMO.getInstance(), 1L, 1L);
                }
            }
        }, 1L);
    }

    /**
     * Attempts a chance-based bleed effect on the entity. The chance is (if not configured otherwise) affected by the LUCK
     * stat of both the bleeder and the entity. The attacker's luck is decreased by the victim's luck.
     * @param bleeder the entity to bleed
     * @param causedBy the entity causing the bleed
     */
    public static void attemptBleed(LivingEntity bleeder, Entity causedBy, CombatType combatType){
        double chance = AccumulativeStatManager.getCachedAttackerRelationalStats("BLEED_CHANCE", causedBy, bleeder, 10000, true);
        AttributeInstance bleederLuck = bleeder.getAttribute(Attribute.GENERIC_LUCK);
        AttributeInstance causedByLuck = causedBy instanceof LivingEntity l ? l.getAttribute(Attribute.GENERIC_LUCK) : null;
        if (Utils.proc(chance, (causedByLuck == null ? 0 : causedByLuck.getValue()) - (bleederLuck == null ? 0 : bleederLuck.getValue()), false)){
            double damage = AccumulativeStatManager.getCachedAttackerRelationalStats("BLEED_DAMAGE", causedBy, bleeder, 10000, true);
            int duration = (int) AccumulativeStatManager.getCachedAttackerRelationalStats("BLEED_DURATION", causedBy, bleeder, 10000, true);
            inflictBleed(bleeder, causedBy, duration, damage, combatType);
        }
    }

    public static void removeBleed(LivingEntity bleeder){
        BleedingInstance instance = bleedingEntities.get(bleeder.getUniqueId());
        if (instance != null) instance.cancel();
        bleedingEntities.remove(bleeder.getUniqueId());
    }

    public static int getMaxStacks() {
        return maxStacks;
    }

    public static int getDelay() {
        return delay;
    }

    public static int getStackedDelayReduction() {
        return stackedDelayReduction;
    }

    public static Map<UUID, BleedingInstance> getBleedingEntities() {
        return bleedingEntities;
    }

    public static class BleedingInstance extends BukkitRunnable {
        private final LivingEntity bleedingEntity;
        private final Entity causedBy;
        private double bleedingDamage;
        private int duration;
        private final int offset;
        private int stacks = 1;

        public BleedingInstance(LivingEntity bleedingEntity, Entity causedBy, int duration, double damagePerTick){
            this.bleedingEntity = bleedingEntity;
            this.causedBy = causedBy;
            this.duration = duration;
            this.bleedingDamage = damagePerTick;
            this.offset = duration % delay; // the offset is to make sure the bleeding instance deals bleed damage right away and "starts" from there.
            // example: duration: 140, delay: 40. 140%40 = 20, 140+20%40 = 0, proc bleed
        }

        public double getBleedingDamage() { return bleedingDamage; }
        public LivingEntity getBleedingEntity() { return bleedingEntity; }
        public Entity getCausedBy() { return causedBy; }
        public void setStacks(int stacks){ this.stacks = stacks; }
        public int getStacks() { return stacks; }
        public void setBleedingDamage(double bleedingDamage) { this.bleedingDamage = bleedingDamage; }
        public void setDuration(int duration) { this.duration = duration; }

        @SuppressWarnings("deprecation")
        @Override
        public void run() {
            if (duration >= 0 && bleedingEntity.isValid()){
                int tickDelay = delay + (stackedDelayReduction * (stacks - 1));
                if ((duration + offset) % Math.max(1, tickDelay) == 0){
                    if (bleedingEntity instanceof Player p && (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR)){
                        cancel();
                        bleedingEntities.remove(p.getUniqueId());
                        return;
                    }
                    int immunityFramesBefore = bleedingEntity.getNoDamageTicks();
                    bleedingEntity.setNoDamageTicks(0);
                    if (!EntityUtils.hasActiveDamageProcess(bleedingEntity))
                        EntityUtils.damage(bleedingEntity, causedBy, bleedingDamage, "BLEED", true);
                    bleedingEntity.setNoDamageTicks(immunityFramesBefore); // makes sure the entity doesn't immune attacks they shouldn't after taking bleed damage

                    int particleCount = (int) (3 * Math.min(10, bleedingDamage));
                    bleedingEntity.getWorld().spawnParticle(Particle.valueOf(oldOrNew("BLOCK_DUST", "BLOCK")), bleedingEntity.getEyeLocation().add(0, -(bleedingEntity.getHeight()/2), 0),
                            particleCount, 0.4, 0.4, 0.4, Material.REDSTONE_BLOCK.createBlockData());
                    bleedingEntity.playEffect(EntityEffect.HURT);
                }
                bleedingEntity.getWorld().spawnParticle(Particle.valueOf(oldOrNew("BLOCK_DUST", "BLOCK")), bleedingEntity.getEyeLocation().add(0, -(bleedingEntity.getHeight()/2), 0),
                        1, 0.4, 0.1, 0.1, Material.REDSTONE_BLOCK.createBlockData());
                duration--;
            } else {
                cancel();
                bleedingEntities.remove(bleedingEntity.getUniqueId());
            }
        }
    }
}
