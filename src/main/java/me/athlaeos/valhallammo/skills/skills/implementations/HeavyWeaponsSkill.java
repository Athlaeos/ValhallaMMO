package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.CombatType;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.event.EntityCriticallyHitEvent;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.listeners.EntityAttackListener;
import me.athlaeos.valhallammo.listeners.EntityDamagedListener;
import me.athlaeos.valhallammo.listeners.EntitySpawnListener;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.HeavyWeaponsProfile;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.potioneffects.implementations.Stun;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.Bleeder;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class HeavyWeaponsSkill extends Skill implements Listener {
    private final Collection<Material> validCoatingItems = new HashSet<>();
    private final Map<EntityType, Double> entityExpMultipliers = new HashMap<>();
    private final double expPerDamage;
    private final double spawnerMultiplier;
    
    public HeavyWeaponsSkill(String type) {
        super(type);
        ValhallaMMO.getInstance().save("skills/heavy_weapons_progression.yml");
        ValhallaMMO.getInstance().save("skills/heavy_weapons.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/heavy_weapons.yml").reload().get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/heavy_weapons_progression.yml").reload().get();

        loadCommonConfig(skillConfig, progressionConfig);

        validCoatingItems.addAll(ItemUtils.getMaterialSet(skillConfig.getStringList("valid_coating_items")));

        ConfigurationSection entitySection = progressionConfig.getConfigurationSection("experience.entity_exp_multipliers");
        if (entitySection != null){
            entitySection.getKeys(false).forEach(s -> {
                EntityType e = Catch.catchOrElse(() -> EntityType.valueOf(s), null, "Invalid entity type given in skills/heavy_weapons_progression.yml experience.entity_exp_multipliers." + s);
                if (e == null) return;
                double multiplier = progressionConfig.getDouble("experience.entity_exp_multipliers." + s);
                entityExpMultipliers.put(e, multiplier);
            });
        }
        expPerDamage = progressionConfig.getDouble("experience.exp_per_damage");
        spawnerMultiplier = progressionConfig.getDouble("experience.spawner_spawned_multiplier");

        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCoatingApply(InventoryClickEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getWhoClicked().getWorld().getName()) || e.isCancelled() || !Timer.isCooldownPassed(e.getWhoClicked().getUniqueId(), "delay_coating_attempts")) return;
        if (!(e.getClickedInventory() instanceof PlayerInventory) || !e.isRightClick()) return; // player inventory must be right-clicked
        Timer.setCooldown(e.getWhoClicked().getUniqueId(), 500, "delay_coating_attempts"); // setting cooldown between attempts so this can't be spammed with some macro
        if (ItemUtils.isEmpty(e.getCurrentItem()) || ItemUtils.isEmpty(e.getCursor())) return; // neither items must be empty
        if (!validCoatingItems.contains(e.getCursor().getType())) return; // must be a valid item for coating
        HeavyWeaponsProfile profile = ProfileCache.getOrCache((Player) e.getWhoClicked(), HeavyWeaponsProfile.class);
        if (!profile.isCoatingUnlocked()) return;
        ItemBuilder clicked = new ItemBuilder(e.getCurrentItem());
        if (!PotionEffectRegistry.getStoredEffects(clicked.getMeta(), false).isEmpty()) return; // items that already have any coating cannot be coated again
        ItemBuilder cursor = new ItemBuilder(e.getCursor());
        if (WeightClass.getWeightClass(clicked.getMeta()) != WeightClass.HEAVY || !EquipmentClass.isHandHeld(clicked.getMeta())) return; // clicked item must be heavy handheld item
        Map<String, PotionEffectWrapper> effects = PotionEffectRegistry.getStoredEffects(cursor.getMeta(), false);
        if (effects.isEmpty()) return; // cursor must have potion effects stored
        Map<String, PotionEffectWrapper> newEffects = new HashMap<>();
        for (PotionEffectWrapper wrapper : effects.values()){
            newEffects.put(wrapper.getEffect(), wrapper
                    .setAmplifier(profile.getCoatingAmplifierMultiplier() * wrapper.getAmplifier())
                    .setDuration((int) Math.floor(profile.getCoatingDurationMultiplier() * wrapper.getDuration()))
                    .setCharges(profile.getCoatingCharges())
            );
        }
        PotionEffectRegistry.setDefaultStoredEffects(clicked.getMeta(), newEffects);
        PotionEffectRegistry.setActualStoredEffects(clicked.getMeta(), newEffects);
        clicked.flag(CustomFlag.TEMPORARY_POTION_DISPLAY);
        e.setCurrentItem(clicked.get());
        e.setCancelled(true);
        e.getWhoClicked().getWorld().playSound(e.getWhoClicked().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1F, 1F);
        e.getWhoClicked().getWorld().playSound(e.getWhoClicked().getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1F, 1F);

        if (cursor.getItem().getAmount() == 1) e.getWhoClicked().setItemOnCursor(null);
        else e.getCursor().setAmount(e.getCursor().getAmount() - 1);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAttack(EntityDamageByEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        if (e.getDamager() instanceof Player p && e.getEntity() instanceof LivingEntity l){
            ItemBuilder weapon = EntityCache.getAndCacheProperties(p).getMainHand();
            if (weapon == null || WeightClass.getWeightClass(weapon.getMeta()) != WeightClass.HEAVY) return;
            HeavyWeaponsProfile profile = ProfileCache.getOrCache(p, HeavyWeaponsProfile.class);
            if (profile.doesCritOnBleed() && Bleeder.getBleedingEntities().containsKey(l.getUniqueId())) EntityAttackListener.critNextAttack(p);
            else if (profile.doesCritOnStun() && Stun.isStunned(l)) EntityAttackListener.critNextAttack(p);
            else if (profile.doesCritOnStealth()) {
                boolean facing = EntityUtils.isEntityFacing(l, e.getDamager().getLocation(), EntityAttackListener.getFacingAngleCos());
                if (!facing && p.isSneaking() && !EntityAttackListener.isInCombat(p)) EntityAttackListener.critNextAttack(p);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onExpAttack(EntityDamageByEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        if (e.getDamager() instanceof Player p && e.getEntity() instanceof Monster l){
            ItemBuilder weapon = EntityCache.getAndCacheProperties(p).getMainHand();
            if (weapon == null || WeightClass.getWeightClass(weapon.getMeta()) != WeightClass.HEAVY) return;

            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                double entityExpMultiplier = entityExpMultipliers.getOrDefault(l.getType(), 1D);
                addEXP(p,
                        EntityDamagedListener.getLastDamageTaken(l.getUniqueId()) *
                                expPerDamage *
                                entityExpMultiplier *
                                (EntitySpawnListener.getSpawnReason(l) == CreatureSpawnEvent.SpawnReason.SPAWNER ? spawnerMultiplier : 1),
                        false,
                        PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
            }, 2L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCrit(EntityCriticallyHitEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        if (e.getCritter() instanceof Player p && e.getEntity() instanceof LivingEntity l){
            ItemBuilder weapon = EntityCache.getAndCacheProperties(p).getMainHand();
            if (weapon == null || WeightClass.getWeightClass(weapon.getMeta()) != WeightClass.HEAVY) return;
            HeavyWeaponsProfile profile = ProfileCache.getOrCache(p, HeavyWeaponsProfile.class);
            if (profile.doesBleedOnCrit()) Bleeder.inflictBleed(l, p, CombatType.MELEE_ARMED);
            if (profile.doesStunOnCrit()) Stun.attemptStun(l, p);
        }
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return HeavyWeaponsProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 45;
    }

    @Override
    public boolean isExperienceScaling() {
        return true;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= (1 + AccumulativeStatManager.getStats("HEAVY_WEAPONS_EXP_GAIN", p, true));
        }
        super.addEXP(p, amount, silent, reason);
    }
}
