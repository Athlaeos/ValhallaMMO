package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.animations.AnimationRegistry;
import me.athlaeos.valhallammo.event.EntityParryEntityEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class Parryer {
    private static final Collection<PotionEffectWrapper> parryEnemyDebuffs = new HashSet<>();
    private static final Collection<PotionEffectWrapper> parrySelfDebuffs = new HashSet<>();
    private static final boolean parrySparks = ValhallaMMO.getPluginConfig().getBoolean("parry_sparks");
    private static final boolean parryProjectiles = ValhallaMMO.getPluginConfig().getBoolean("parry_projectiles");
    private static final boolean parryProjectilesReflect = ValhallaMMO.getPluginConfig().getBoolean("parried_projectiles_reflect");
    private static final Sound parryActivationSound = Utils.getSound(ValhallaMMO.getPluginConfig().getString("parry_sound"), Sound.BLOCK_IRON_TRAPDOOR_OPEN, "Invalid parry activation sound given, used default");
    private static final Sound parrySuccessSound = Utils.getSound(ValhallaMMO.getPluginConfig().getString("parry_success_sound"), Sound.ENTITY_ITEM_BREAK, "Invalid parry success sound given, used default");
    private static final Sound parryFailedSound = Utils.getSound(ValhallaMMO.getPluginConfig().getString("parry_failed_sound"), Sound.ITEM_SHIELD_BREAK, "Invalid parry fail sound given, used default");
    private static Animation parrySuccessAnimation = AnimationRegistry.getAnimation(AnimationRegistry.ENTITY_SPARK_FLASH.id());
    private static Animation parryFailAnimation = null;

    static {
        YamlConfiguration c = ValhallaMMO.getPluginConfig();
        ConfigurationSection enemySection = c.getConfigurationSection("parry_enemy_debuffs");
        if (enemySection != null) {
            for (String effect : enemySection.getKeys(false)) {
                parryEnemyDebuffs.add(PotionEffectRegistry.getEffect(effect).setAmplifier(c.getDouble("parry_enemy_debuffs." + effect)));
            }
        }
        ConfigurationSection selfSection = c.getConfigurationSection("parry_failed_debuffs");
        if (selfSection != null) {
            for (String effect : selfSection.getKeys(false)) {
                parrySelfDebuffs.add(PotionEffectRegistry.getEffect(effect).setAmplifier(c.getDouble("parry_failed_debuffs." + effect)));
            }
        }
    }

    /**
     * Checks if the entity is parried or not. An entity is considered parried if they have all of the "parry" effects
     * @param entity the entity
     * @return true if they're considered parried, false otherwise
     */
    public static boolean isParried(LivingEntity entity){
        Map<String, CustomPotionEffect> activeEffects = PotionEffectRegistry.getActiveEffects(entity);
        return parryEnemyDebuffs.stream().map(PotionEffectWrapper::getEffect).allMatch(activeEffects::containsKey);
    }

    /**
     * Activates a parry effect, all times should be given in game ticks
     * @param e the entity to activate the parry for
     * @param activeFor how long the entity should be parrying for, in game ticks. If attacked during this time period, the damage is reduced and the attacker is debuffed
     * @param vulnerableFor how long the entity should be vulnerable for, in game ticks. Should be longer than activeFor. If attacked during the time period,
     *                      and activeFor hasn't expired yet, the entity is debuffed instead.
     * @param cooldown the cooldown of the ability, in game ticks.
     */
    public static void forceParry(LivingEntity e, int activeFor, int vulnerableFor, int cooldown) {
        Timer.setCooldown(e.getUniqueId(), activeFor * 50, "parry_effective");
        Timer.setCooldown(e.getUniqueId(), vulnerableFor * 50, "parry_vulnerable");
        Timer.setCooldownIgnoreIfPermission(e, cooldown * 50, "parry_cooldown");
        e.getWorld().playSound(e.getLocation(), parryActivationSound, 1F, 1F);
    }

    public static void attemptParry(LivingEntity e){
        if (!Timer.isCooldownPassed(e.getUniqueId(), "parry_cooldown")) {
            if (e instanceof Player p) Timer.sendCooldownStatus(p, "parry_cooldown", TranslationManager.getTranslation("ability_parry"));
            return;
        }
        int cooldown = (int) AccumulativeStatManager.getCachedStats("PARRY_COOLDOWN", e, 10000, true) - 1;
        if (cooldown < 0) return;
        int activeDuration = (int) AccumulativeStatManager.getCachedStats("PARRY_EFFECTIVENESS_DURATION", e, 10000, true);
        if (activeDuration <= 0) return;
        int vulnerableDuration = (int) AccumulativeStatManager.getCachedStats("PARRY_VULNERABLE_DURATION", e, 10000, true);
        forceParry(e, activeDuration, vulnerableDuration, cooldown);
    }

    /**
     * Handles the parry effect on an EntityDamageByEntityEvent, returning the damage multiplier after a parry
     * @param e the event
     * @return the damage multiplier of the taken damage
     */
    public static double handleParry(EntityDamageByEntityEvent e){
        Entity d = e.getDamager();
        boolean canParry = d instanceof LivingEntity || (d instanceof Projectile && parryProjectiles);
        if (canParry && e.getEntity() instanceof LivingEntity v){
            if (v instanceof Player p && WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_COMBAT_PARRY)) return 1;
            double damageReduction = (int) AccumulativeStatManager.getCachedRelationalStats("PARRY_DAMAGE_REDUCTION", v, d, 10000, true);
            double cooldownReduction = AccumulativeStatManager.getCachedRelationalStats("PARRY_SUCCESS_COOLDOWN_REDUCTION", v, d, 10000, true);
            if (!Timer.isCooldownPassed(v.getUniqueId(), "parry_effective")){
                EntityParryEntityEvent event = new EntityParryEntityEvent(v, d, EntityParryEntityEvent.ParryType.SUCCESSFUL, damageReduction, cooldownReduction);
                ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()){
                    damageReduction = event.getDamageReduction();
                    cooldownReduction = event.getCooldownReduction();
                    // parry successful
                    if (parrySparks && parrySuccessAnimation != null) parrySuccessAnimation.animate(v, v.getLocation(), v.getEyeLocation().getDirection(), 0);
                    v.getWorld().playSound(v.getLocation(), parrySuccessSound, 1F, 1F);
                    long cooldown = Timer.getCooldown(v.getUniqueId(), "parry_cooldown");
                    if (cooldown > 0) Timer.setCooldown(v.getUniqueId(), (int) (cooldown * (1 - cooldownReduction)), "parry_cooldown");
                    Timer.setCooldown(v.getUniqueId(), 0, "parry_vulnerable");
                    Timer.setCooldown(v.getUniqueId(), 0, "parry_effective");
                    if (event.isApplyEnemyDebuffs()){
                        if (d instanceof LivingEntity a){
                            int debuffDuration = (int) AccumulativeStatManager.getCachedRelationalStats("PARRY_ENEMY_DEBUFF_DURATION", v, a, 10000, true);
                            for (PotionEffectWrapper wrapper : parryEnemyDebuffs){
                                PotionEffectWrapper copy = PotionEffectRegistry.getEffect(wrapper.getEffect()).setAmplifier(wrapper.getAmplifier()).setDuration(debuffDuration);
                                if (!wrapper.isVanilla()) PotionEffectRegistry.addEffect(a, v, new CustomPotionEffect(copy, debuffDuration, copy.getAmplifier()), false, 1, EntityPotionEffectEvent.Cause.ATTACK);
                                else a.addPotionEffect(new PotionEffect(copy.getVanillaEffect(), debuffDuration, (int) copy.getAmplifier()));
                            }
                            e.setDamage(e.getDamage() * (1 - damageReduction));
                        } else if (parryProjectilesReflect && !(d instanceof Trident)) {
                            Projectile p = (Projectile) d;
                            p.setVelocity(p.getVelocity().multiply(-1));
                            p.setShooter(v);
                            double inaccuracy = AccumulativeStatManager.getCachedStats("RANGED_INACCURACY", v, 10000, true);
                            EntityUtils.applyInaccuracy(p, v.getEyeLocation().getDirection(), inaccuracy);
                            return 0;
                        }
                    }
                    return 1 - damageReduction;
                }
            } else if (!Timer.isCooldownPassed(v.getUniqueId(), "parry_vulnerable")){
                EntityParryEntityEvent event = new EntityParryEntityEvent(v, d, EntityParryEntityEvent.ParryType.SUCCESSFUL, damageReduction, cooldownReduction);
                ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()){
                    // parry failed
                    if (event.isApplySelfDebuffs()){
                        int debuffDuration = (int) AccumulativeStatManager.getCachedRelationalStats("PARRY_SELF_DEBUFF_DURATION", v, d, 10000, true);
                        for (PotionEffectWrapper wrapper : parrySelfDebuffs){
                            PotionEffectWrapper copy = PotionEffectRegistry.getEffect(wrapper.getEffect()).setAmplifier(wrapper.getAmplifier()).setDuration(debuffDuration);
                            if (!wrapper.isVanilla()) PotionEffectRegistry.addEffect(v, null, new CustomPotionEffect(copy, debuffDuration, copy.getAmplifier()), false, 1, EntityPotionEffectEvent.Cause.ATTACK);
                            else v.addPotionEffect(new PotionEffect(copy.getVanillaEffect(), debuffDuration, (int) copy.getAmplifier()));
                        }
                    }
                    if (parryFailAnimation != null) parryFailAnimation.animate(v, v.getLocation(), v.getEyeLocation().getDirection(), 0);
                    v.getWorld().playSound(v.getLocation(), parryFailedSound, 1F, 1F);
                }
            }
            Timer.setCooldown(v.getUniqueId(), 0, "parry_vulnerable");
            Timer.setCooldown(v.getUniqueId(), 0, "parry_effective");
        }
        return 1;
    }

    public static void setParrySuccessAnimation(Animation parrySuccessAnimation) {
        Parryer.parrySuccessAnimation = parrySuccessAnimation;
    }

    public static Animation getParrySuccessAnimation() {
        return parrySuccessAnimation;
    }

    public static void setParryFailAnimation(Animation parryFailAnimation) {
        Parryer.parryFailAnimation = parryFailAnimation;
    }

    public static Animation getParryFailAnimation() {
        return parryFailAnimation;
    }
}
