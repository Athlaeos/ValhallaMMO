package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.event.EntityCustomPotionEffectEvent;
import me.athlaeos.valhallammo.event.PlayerLeaveCombatEvent;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.listeners.EntityDamagedListener;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.HeavyArmorProfile;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.skills.ChunkEXPNerf;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class HeavyArmorSkill extends Skill implements Listener {
    private double expPerDamage = 0;
    private double expPerCombatSecond = 0;
    private double expBonusPerPoint = 0;

    private Animation rageActivation;
    private final Map<EntityType, Double> entityExpMultipliers = new HashMap<>();
    private final Collection<RagePotionEffect> ragePotionEffects = new HashSet<>();

    public HeavyArmorSkill(String type) {
        super(type);
    }

    @Override
    public void loadConfiguration() {
        ValhallaMMO.getInstance().save("skills/heavy_armor_progression.yml");
        ValhallaMMO.getInstance().save("skills/heavy_armor.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/heavy_armor.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/heavy_armor_progression.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);

        expPerDamage = progressionConfig.getDouble("experience.exp_damage_piece");
        expPerCombatSecond = progressionConfig.getDouble("experience.exp_second_piece");
        expBonusPerPoint = progressionConfig.getDouble("experience.exp_multiplier_point");

        ConfigurationSection entitySection = progressionConfig.getConfigurationSection("experience.entity_exp_multipliers");
        if (entitySection != null){
            entitySection.getKeys(false).forEach(s -> {
                EntityType e = Catch.catchOrElse(() -> EntityType.valueOf(s), null, "Invalid entity type given in skills/heavy_armor_progression.yml experience.entity_exp_multipliers." + s);
                if (e == null) return;
                double multiplier = progressionConfig.getDouble("experience.entity_exp_multipliers." + s);
                entityExpMultipliers.put(e, multiplier);
            });
        }

        for (String potionEffectString : skillConfig.getStringList("rage_effects")){
            String[] args = potionEffectString.split(";");
            if (args.length < 5) {
                ValhallaMMO.logWarning("Could not register Rage potion effect, not enough arguments: POTIONEFFECT;AMPLIFIERBASE;DURATIONBASE;AMPLIFIERLV;DURATIONLV");
                continue;
            }
            try {
                double baseAmplifier = StringUtils.parseDouble(args[1]);
                int baseDuration = Integer.parseInt(args[2]);
                double lvAmplifier = StringUtils.parseDouble(args[3]);
                int lvDuration = Integer.parseInt(args[4]);
                ragePotionEffects.add(new RagePotionEffect(args[0], baseAmplifier, baseDuration, lvAmplifier, lvDuration));
            } catch (NumberFormatException e){
                ValhallaMMO.logWarning("Could not register Rage potion effect, invalid number: " + potionEffectString);
            }
        }

        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageTaken(EntityDamageByEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        Entity trueDamager = EntityUtils.getTrueDamager(e);
        if (!(trueDamager instanceof LivingEntity) || !(e.getEntity() instanceof Player p) || p.isBlocking()) return;
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_HEAVYARMOR)) return;

        HeavyArmorProfile profile = ProfileCache.getOrCache(p, HeavyArmorProfile.class);

        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            double chunkNerf = ChunkEXPNerf.getChunkEXPNerf(p.getLocation().getChunk(), p, "armors");
            int count = EntityCache.getAndCacheProperties(p).getHeavyArmorCount();
            double totalHeavyArmor = AccumulativeStatManager.getCachedStats("TOTAL_HEAVY_ARMOR", p, 10000, false);
            double entityExpMultiplier = entityExpMultipliers.getOrDefault(trueDamager.getType(), 1D);
            double lastDamageTaken = e.getDamage();
            double exp = expPerDamage * lastDamageTaken * entityExpMultiplier * (1 + (totalHeavyArmor * expBonusPerPoint)) * chunkNerf;
            addEXP(p, count * exp, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);

            if (profile.isRageUnlocked() && profile.getRageLevel() > 0 && Timer.isCooldownPassed(p.getUniqueId(), "cooldown_heavy_armor_rage") &&
                    !WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_COMBAT_RAGE)){
                EntityProperties properties = EntityCache.getAndCacheProperties(p);
                if (properties.getHeavyArmorCount() < profile.getSetCount()) return;
                AttributeInstance healthInstance = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (healthInstance == null) return;
                if (p.getHealth() / healthInstance.getValue() > profile.getRageThreshold()) return;
                ragePotionEffects.forEach(r -> r.applyPotionEffect(p, profile.getRageLevel()));
                if (rageActivation != null) rageActivation.animate(p, p.getLocation(), p.getEyeLocation().getDirection(), 0);
                Timer.setCooldownIgnoreIfPermission(p, profile.getRageCooldown() * 50, "cooldown_heavy_armor_rage");
            }
            ChunkEXPNerf.increment(p.getLocation().getChunk(), p, "armors");
        }, 2L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionEffect(EntityPotionEffectEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled() || e.getNewEffect() == null || e.getCause() == EntityPotionEffectEvent.Cause.POTION_DRINK) return;
        if (!(e.getEntity() instanceof Player p)) return;
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_HEAVYARMOR)) return;
        HeavyArmorProfile profile = ProfileCache.getOrCache(p, HeavyArmorProfile.class);
        EntityProperties properties = EntityCache.getAndCacheProperties(p);
        if (properties.getHeavyArmorCount() < profile.getSetCount()) return;
        if (profile.getImmuneEffects().contains(e.getNewEffect().getType().toString())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionEffect(EntityCustomPotionEffectEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled() || e.getNewEffect() == null) return;
        if (!(e.getEntity() instanceof Player p)) return;
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_HEAVYARMOR) ||
                WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_COMBAT_POTIONIMMUNITY)) return;
        HeavyArmorProfile profile = ProfileCache.getOrCache(p, HeavyArmorProfile.class);
        EntityProperties properties = EntityCache.getAndCacheProperties(p);
        if (properties.getHeavyArmorCount() < profile.getSetCount()) return;
        if (profile.getImmuneEffects().contains(e.getNewEffect().getWrapper().getEffect())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCombatLeave(PlayerLeaveCombatEvent e){
        long timeInCombat = e.getTimeInCombat();
        EntityProperties properties = EntityCache.getAndCacheProperties(e.getPlayer());

        int armorCount = properties.getHeavyArmorCount();
        int expRewardTimes = (int) (timeInCombat / 1000D);

        addEXP(e.getPlayer(), expRewardTimes * armorCount * expPerCombatSecond, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return HeavyArmorProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 65;
    }

    @Override
    public boolean isExperienceScaling() {
        return true;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= (1 + AccumulativeStatManager.getStats("HEAVY_ARMOR_EXP_GAIN", p, true));
        }
        super.addEXP(p, amount, silent, reason);
    }

    public void setRageActivation(Animation rageActivation) {
        this.rageActivation = rageActivation;
    }

    private static class RagePotionEffect{
        private final PotionEffectWrapper wrapper;
        private final double baseAmplifier;
        private final int baseDuration;
        private final double lvAmplifier;
        private final int lvDuration;

        public RagePotionEffect(String type, double baseAmplifier, int baseDuration, double lvAmplifier, int lvDuration){
            this.wrapper = PotionEffectRegistry.getEffect(type);
            this.baseAmplifier = baseAmplifier;
            this.baseDuration = baseDuration;
            this.lvAmplifier = lvAmplifier;
            this.lvDuration = lvDuration;
        }

        public void applyPotionEffect(Player p, int level){
            int duration = baseDuration + (lvDuration * (level - 1));
            double amplifier = baseAmplifier + (lvAmplifier * (level - 1));
            if (wrapper.isVanilla()) {
                int amp = (int) amplifier - 1;
                if (amp < 0) return;
                p.addPotionEffect(new PotionEffect(wrapper.getVanillaEffect(), duration, amp, false));
            } else PotionEffectRegistry.addEffect(p, null, new CustomPotionEffect(wrapper, duration, amplifier), false, 1, EntityPotionEffectEvent.Cause.ARROW);
        }
    }
}
