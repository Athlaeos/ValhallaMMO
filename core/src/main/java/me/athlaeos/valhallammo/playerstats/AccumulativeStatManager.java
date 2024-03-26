package me.athlaeos.valhallammo.playerstats;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.DigPacketInfo;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.entities.EntityClassification;
import me.athlaeos.valhallammo.entities.MonsterScalingManager;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.listeners.MovementListener;
import me.athlaeos.valhallammo.parties.PartyManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.*;
import me.athlaeos.valhallammo.playerstats.statsources.*;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

@SuppressWarnings("unused")
public class AccumulativeStatManager {
    private static final Map<String, StatCollector> sources = new HashMap<>();

    public static Map<String, StatCollector> getSources() {
        return sources;
    }

    static {
        register("GLOBAL_EXP_GAIN", new ProfileStatSource(PowerProfile.class, "allSkillEXPMultiplier"), new GlobalBuffSource("percent_skill_exp_gain"), new PotionEffectSource("SKILL_EXP_GAIN"));

        // armors
        register("ARMOR_TOTAL", new DefensiveSourceSource("TOTAL_LIGHT_ARMOR"), new DefensiveSourceSource("TOTAL_HEAVY_ARMOR"), new DefensiveSourceSource("TOTAL_WEIGHTLESS_ARMOR"));
        register("TOTAL_LIGHT_ARMOR", new TotalLightArmorSource());
        register("LIGHT_ARMOR", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setArmorBonus", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "armorBonusPerPiece", WeightClass.LIGHT), new VanillaAttributeDefenderSource(Attribute.GENERIC_ARMOR, AttributeModifier.Operation.ADD_NUMBER).weight(WeightClass.LIGHT).penalty("armor"), new PotionEffectSource("LIGHT_ARMOR"));
        register("LIGHT_ARMOR_MULTIPLIER", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setArmorMultiplier", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "armorMultiplierPerPiece", WeightClass.LIGHT), new ProfileStatSource(LightArmorProfile.class, "lightArmorMultiplier"));
        register("TOTAL_HEAVY_ARMOR", new TotalHeavyArmorSource());
        register("HEAVY_ARMOR", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setArmorBonus", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "armorBonusPerPiece", WeightClass.HEAVY), new VanillaAttributeDefenderSource(Attribute.GENERIC_ARMOR, AttributeModifier.Operation.ADD_NUMBER).weight(WeightClass.HEAVY).penalty("armor"), new PotionEffectSource("HEAVY_ARMOR"));
        register("HEAVY_ARMOR_MULTIPLIER", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setArmorMultiplier", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "armorMultiplierPerPiece", WeightClass.HEAVY), new ProfileStatSource(HeavyArmorProfile.class, "heavyArmorMultiplier"));
        register("TOTAL_WEIGHTLESS_ARMOR", new ProfileArmorlessArmorSource(), new SetBonusSource("GENERIC_ARMOR"), new TotalWeightlessArmorSource());
        register("WEIGHTLESS_ARMOR", new VanillaAttributeDefenderSource(Attribute.GENERIC_ARMOR, AttributeModifier.Operation.ADD_NUMBER).weight(WeightClass.WEIGHTLESS).penalty("armor"), new ProfileStatSource(PowerProfile.class, "armorBonus"), new GlobalBuffSource("armor_bonus"), new PotionEffectSource("ARMOR_FLAT"));
        register("ARMOR_MULTIPLIER_BONUS", new VanillaAttributeDefenderSource(Attribute.GENERIC_ARMOR, AttributeModifier.Operation.ADD_SCALAR), new ProfileStatSource(PowerProfile.class, "armorMultiplierBonus"), new GlobalBuffSource("fraction_armor_bonus"), new PotionEffectSource("ARMOR_FRACTION"));
        register("TOUGHNESS", new VanillaAttributeDefenderSource(Attribute.GENERIC_ARMOR_TOUGHNESS, AttributeModifier.Operation.ADD_NUMBER).penalty("armor"), new DefensiveSourceSource("TOUGHNESS_BONUS"));
        registerOffensive("LIGHT_ARMOR_FLAT_IGNORED", new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "penetrationFlatLight", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "penetrationFlatLight", WeightClass.LIGHT), new AttributeAttackerSource("LIGHT_ARMOR_PENETRATION_FLAT").penalty("attribute"), new PotionEffectAttackerSource("LIGHT_ARMOR_PENETRATION_FLAT"));
        registerOffensive("LIGHT_ARMOR_FRACTION_IGNORED", new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "penetrationFractionLight", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "penetrationFractionLight", WeightClass.LIGHT), new AttributeAttackerSource("LIGHT_ARMOR_PENETRATION_FRACTION").penalty("attribute"), new PotionEffectAttackerSource("LIGHT_ARMOR_PENETRATION_FRACTION"));
        registerOffensive("HEAVY_ARMOR_FLAT_IGNORED", new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "penetrationFlatHeavy", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "penetrationFlatHeavy", WeightClass.LIGHT), new AttributeAttackerSource("HEAVY_ARMOR_PENETRATION_FLAT").penalty("attribute"), new PotionEffectAttackerSource("HEAVY_ARMOR_PENETRATION_FLAT"));
        registerOffensive("HEAVY_ARMOR_FRACTION_IGNORED", new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "penetrationFractionHeavy", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "penetrationFractionHeavy", WeightClass.LIGHT), new AttributeAttackerSource("HEAVY_ARMOR_PENETRATION_FRACTION").penalty("attribute"), new PotionEffectAttackerSource("HEAVY_ARMOR_PENETRATION_FRACTION"));
        registerOffensive("ARMOR_FLAT_IGNORED", new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "penetrationFlat", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "penetrationFlat", WeightClass.LIGHT), new GlobalBuffSource("armor_penetration"), new AttributeAttackerSource("ARMOR_PENETRATION_FLAT").penalty("attribute"), new PotionEffectAttackerSource("ARMOR_PENETRATION_FLAT"));
        registerOffensive("ARMOR_FRACTION_IGNORED", new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "penetrationFraction", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "penetrationFraction", WeightClass.LIGHT), new GlobalBuffSource("fraction_armor_penetration"), new AttributeAttackerSource("ARMOR_PENETRATION_FRACTION").penalty("attribute"), new PotionEffectAttackerSource("ARMOR_PENETRATION_FRACTION"));

        // vanilla non-customized attributes (handled in MovementListener in the form of unique attributes), equipment does not need to be scanned
        register("HEALTH_BONUS", new SetBonusSource("GENERIC_MAX_HEALTH"), new ProfileStatSource(PowerProfile.class, "healthBonus"), new GlobalBuffSource("health_bonus"), new PotionEffectSource("MAX_HEALTH_FLAT"));
        register("HEALTH_MULTIPLIER_BONUS", new ProfileStatSource(PowerProfile.class, "healthMultiplier"), new GlobalBuffSource("fraction_health_bonus"), new PotionEffectSource("MAX_HEALTH_FRACTION"));
        register("MOVEMENT_SPEED_BONUS", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setMovementSpeed", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setMovementSpeed", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "movementSpeedPerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "movementSpeedPerPiece", WeightClass.LIGHT), new SetBonusSource("GENERIC_MOVEMENT_SPEED"), new ProfileStatSource(PowerProfile.class, "movementSpeedBonus"), new GlobalBuffSource("movement_speed"), new PotionEffectSource("MOVEMENT_SPEED"));
        register("KNOCKBACK_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setKnockbackResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setKnockbackResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "knockbackResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "knockbackResistancePerPiece", WeightClass.LIGHT), new SetBonusSource("GENERIC_KNOCKBACK_RESISTANCE"), new ResistanceArmorWeightClassSource("knockback_resistance"), new ProfileStatSource(PowerProfile.class, "knockbackResistanceBonus"), new GlobalBuffSource("knockback_resistance"), new PotionEffectSource("KNOCKBACK_RESISTANCE"));
        register("TOUGHNESS_BONUS", new SetBonusSource("GENERIC_ARMOR_TOUGHNESS"), new ProfileStatSource(PowerProfile.class, "toughnessBonus"), new GlobalBuffSource("toughness_bonus"), new PotionEffectSource("ARMOR_TOUGHNESS_FLAT"));
        register("ATTACK_DAMAGE_BONUS", new SetBonusSource("GENERIC_ATTACK_DAMAGE"), new ProfileStatSource(PowerProfile.class, "attackDamageBonus"), new GlobalBuffSource("attack_damage_bonus"), new PotionEffectSource("EXTRA_ATTACK_DAMAGE"));
        register("ATTACK_SPEED_BONUS", new LightWeaponsDualWieldingAttackSpeedBuffSource(), new HeavyWeaponsDualWieldingDebuffSource(), new SetBonusSource("GENERIC_ATTACK_SPEED"), new ProfileStatWeightSource(HeavyWeaponsProfile.class, "attackSpeedMultiplier", WeightClass.HEAVY, false), new ProfileStatWeightSource(LightWeaponsProfile.class, "attackSpeedMultiplier", WeightClass.LIGHT, false), new MainHandPenalty("speed"), new ProfileStatSource(PowerProfile.class, "attackSpeedBonus"), new GlobalBuffSource("fraction_attack_speed_bonus"), new PotionEffectSource("ATTACK_SPEED"));
        register("LUCK_BONUS", new SetBonusSource("GENERIC_LUCK"), new ProfileStatSource(PowerProfile.class, "luckBonus"), new GlobalBuffSource("luck_bonus"), new PotionEffectSource("CUSTOM_LUCK"));
        register("BLOCK_REACH", new SetBonusSource("GENERIC_BLOCK_INTERACTION_RANGE"), new ProfileStatSource(PowerProfile.class, "blockReachBonus"), new PotionEffectSource("GENERIC_BLOCK_INTERACTION_RANGE"), new GlobalBuffSource("block_reach"));
        register("STEP_HEIGHT", new SetBonusSource("GENERIC_STEP_HEIGHT"), new ProfileStatSource(PowerProfile.class, "stepHeightBonus"), new PotionEffectSource("GENERIC_STEP_HEIGHT"), new GlobalBuffSource("step_height"));
        register("SCALE", new SetBonusSource("GENERIC_SCALE"), new ProfileStatSource(PowerProfile.class, "scaleMultiplier"), new PotionEffectSource("GENERIC_SCALE"), new GlobalBuffSource("scale"));
        register("GRAVITY", new SetBonusSource("GENERIC_GRAVITY"), new ProfileStatSource(PowerProfile.class, "gravity"), new PotionEffectSource("GENERIC_GRAVITY"), new GlobalBuffSource("gravity"));
        register("SAFE_FALLING_DISTANCE", new SetBonusSource("GENERIC_SAFE_FALL_DISTANCE"), new ProfileStatSource(PowerProfile.class, "safeFallingDistance"), new PotionEffectSource("GENERIC_SAFE_FALL_DISTANCE"), new GlobalBuffSource("safe_falling_distance"));
        register("FALL_DAMAGE_MULTIPLIER", new SetBonusSource("GENERIC_FALL_DAMAGE_MULTIPLIER"), new PotionEffectSource("GENERIC_FALL_DAMAGE_MULTIPLIER"), new GlobalBuffSource("fall_damage_multiplier"));

        // custom damage types and multipliers
        registerOffensive("DAMAGE_DEALT", new ProfileStatAttackerVictimClassSource(FarmingProfile.class, "butcheryDamageMultiplier", EntityClassification.ANIMAL), new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "damageMultiplier", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "damageMultiplier", WeightClass.LIGHT), new AttributeAttackerSource("DAMAGE_ALL").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "attackDamageMultiplier"), new GlobalBuffSource("fraction_damage_bonus"), new PotionEffectAttackerSource("DAMAGE_ALL"));
        registerOffensive("MELEE_DAMAGE_DEALT", new AttributeAttackerSource("DAMAGE_MELEE").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "meleeAttackDamageMultiplier"), new GlobalBuffSource("fraction_melee_damage_bonus"), new PotionEffectAttackerSource("DAMAGE_MELEE"));
        registerOffensive("RANGED_DAMAGE_DEALT", new ProfileStatAttackerHeldItemSource(ArcheryProfile.class, "bowDamageMultiplier", Material.BOW), new ProfileStatAttackerHeldItemSource(ArcheryProfile.class, "crossbowDamageMultiplier", Material.CROSSBOW), new AttributeAttackerSource("DAMAGE_RANGED").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "rangedAttackDamageMultiplier"), new GlobalBuffSource("fraction_ranged_damage_bonus"), new PotionEffectAttackerSource("DAMAGE_RANGED"));
        registerOffensive("UNARMED_DAMAGE_DEALT", new AttributeAttackerSource("DAMAGE_UNARMED").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "unarmedAttackDamageMultiplier"), new GlobalBuffSource("fraction_unarmed_damage_bonus"), new PotionEffectAttackerSource("DAMAGE_UNARMED"));
        registerOffensive("VELOCITY_DAMAGE_BONUS", new AttributeAttackerSource("VELOCITY_DAMAGE"), new PotionEffectAttackerSource("VELOCITY_DAMAGE"));
        registerOffensive("LIGHT_ARMOR_DAMAGE_BONUS", new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "damageToLightArmorMultiplier", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "damageToLightArmorMultiplier", WeightClass.LIGHT), new AttributeAttackerSource("LIGHT_ARMOR_DAMAGE"), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "damageToLightArmorMultiplier", WeightClass.LIGHT), new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "damageToLightArmorMultiplier", WeightClass.HEAVY));
        registerOffensive("HEAVY_ARMOR_DAMAGE_BONUS", new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "damageToHeavyArmorMultiplier", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "damageToHeavyArmorMultiplier", WeightClass.LIGHT), new AttributeAttackerSource("HEAVY_ARMOR_DAMAGE"), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "damageToHeavyArmorMultiplier", WeightClass.LIGHT), new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "damageToHeavyArmorMultiplier", WeightClass.HEAVY));
        registerOffensive("FIRE_DAMAGE_BONUS", new AttributeAttackerSource("EXTRA_FIRE_DAMAGE").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "fireDamageBonus"), new PotionEffectAttackerSource("EXTRA_FIRE_DAMAGE"));
        registerOffensive("EXPLOSION_DAMAGE_BONUS", new AttributeAttackerSource("EXTRA_EXPLOSION_DAMAGE").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "explosionDamageBonus"), new PotionEffectAttackerSource("EXTRA_EXPLOSION_DAMAGE"));
        registerOffensive("POISON_DAMAGE_BONUS", new AttributeAttackerSource("EXTRA_POISON_DAMAGE").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "poisonDamageBonus"), new PotionEffectAttackerSource("EXTRA_POISON_DAMAGE"));
        registerOffensive("MAGIC_DAMAGE_BONUS", new AttributeAttackerSource("EXTRA_MAGIC_DAMAGE").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "magicDamageBonus"), new PotionEffectAttackerSource("EXTRA_MAGIC_DAMAGE"));
        registerOffensive("LIGHTNING_DAMAGE_BONUS", new AttributeAttackerSource("EXTRA_LIGHTNING_DAMAGE").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "lightningDamageBonus"), new PotionEffectAttackerSource("EXTRA_LIGHTNING_DAMAGE"));
        registerOffensive("FREEZING_DAMAGE_BONUS", new AttributeAttackerSource("EXTRA_FREEZING_DAMAGE").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "freezingDamageBonus"), new PotionEffectAttackerSource("EXTRA_FREEZING_DAMAGE"));
        registerOffensive("RADIANT_DAMAGE_BONUS", new AttributeAttackerSource("EXTRA_RADIANT_DAMAGE").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "radiantDamageBonus"), new PotionEffectAttackerSource("EXTRA_RADIANT_DAMAGE"));
        registerOffensive("NECROTIC_DAMAGE_BONUS", new AttributeAttackerSource("EXTRA_NECROTIC_DAMAGE").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "necroticDamageBonus"), new PotionEffectAttackerSource("EXTRA_NECROTIC_DAMAGE"));
        registerOffensive("BLUDGEONING_DAMAGE_BONUS", new AttributeAttackerSource("EXTRA_BLUDGEONING_DAMAGE").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "bludgeoningDamageBonus"), new PotionEffectAttackerSource("EXTRA_BLUDGEONING_DAMAGE"));
        registerOffensive("FIRE_DAMAGE_DEALT", new AttributeAttackerSource("DAMAGE_FIRE").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "fireDamageMultiplier"), new GlobalBuffSource("fraction_fire_damage_bonus"), new PotionEffectAttackerSource("DAMAGE_FIRE"));
        registerOffensive("EXPLOSION_DAMAGE_DEALT", new AttributeAttackerSource("DAMAGE_EXPLOSION").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "explosionDamageMultiplier"), new GlobalBuffSource("fraction_explosion_damage_bonus"), new PotionEffectAttackerSource("DAMAGE_EXPLOSION"));
        registerOffensive("POISON_DAMAGE_DEALT", new AttributeAttackerSource("DAMAGE_POISON").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "poisonDamageMultiplier"), new GlobalBuffSource("fraction_poison_damage_bonus"), new PotionEffectAttackerSource("DAMAGE_POISON"));
        registerOffensive("BLUDGEONING_DAMAGE_DEALT", new AttributeAttackerSource("DAMAGE_BLUDGEONING").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "bludgeoningDamageMultiplier"), new GlobalBuffSource("fraction_bludgeoning_damage_bonus"), new PotionEffectAttackerSource("DAMAGE_BLUDGEONING"));
        registerOffensive("MAGIC_DAMAGE_DEALT", new AttributeAttackerSource("DAMAGE_MAGIC").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "magicDamageMultiplier"), new GlobalBuffSource("fraction_magic_damage_bonus"), new PotionEffectAttackerSource("DAMAGE_MAGIC"));
        registerOffensive("LIGHTNING_DAMAGE_DEALT", new AttributeAttackerSource("DAMAGE_LIGHTNING").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "lightningDamageMultiplier"), new GlobalBuffSource("fraction_lightning_damage_bonus"), new PotionEffectAttackerSource("DAMAGE_LIGHTNING"));
        registerOffensive("FREEZING_DAMAGE_DEALT", new AttributeAttackerSource("DAMAGE_FREEZING").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "freezingDamageMultiplier"), new GlobalBuffSource("fraction_freezing_damage_bonus"), new PotionEffectAttackerSource("DAMAGE_FREEZING"));
        registerOffensive("RADIANT_DAMAGE_DEALT", new AttributeAttackerSource("DAMAGE_RADIANT").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "radiantDamageMultiplier"), new GlobalBuffSource("fraction_radiant_damage_bonus"), new PotionEffectAttackerSource("DAMAGE_RADIANT"));
        registerOffensive("NECROTIC_DAMAGE_DEALT", new AttributeAttackerSource("DAMAGE_NECROTIC").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "necroticDamageMultiplier"), new GlobalBuffSource("fraction_necrotic_damage_bonus"), new PotionEffectAttackerSource("DAMAGE_NECROTIC"));

        // combat related
        registerOffensive("POWER_ATTACK_DAMAGE_MULTIPLIER", new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "powerAttackDamageMultiplier", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "powerAttackDamageMultiplier", WeightClass.LIGHT), new ProfileStatAttackerSource(PowerProfile.class, "powerAttackDamageMultiplier"));
        registerOffensive("POWER_ATTACK_RADIUS", new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "powerAttackRadius", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "powerAttackRadius", WeightClass.LIGHT), new ProfileStatAttackerSource(PowerProfile.class, "powerAttackRadius"));
        registerOffensive("POWER_ATTACK_DAMAGE_FRACTION", new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "powerAttackFraction", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "powerAttackFraction", WeightClass.LIGHT), new ProfileStatAttackerSource(PowerProfile.class, "powerAttackFraction"));
        register("ATTACK_REACH_BONUS", new ProfileStatWeightSource(HeavyWeaponsProfile.class, "attackReachBonus", WeightClass.HEAVY, false), new ProfileStatWeightSource(LightWeaponsProfile.class, "attackReachBonus", WeightClass.LIGHT, false), new AttributeSource("ATTACK_REACH").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "attackReachBonus"), new GlobalBuffSource("attack_reach_bonus"), new PotionEffectSource("ATTACK_REACH"));
        register("ATTACK_REACH_MULTIPLIER", new ProfileStatWeightSource(HeavyWeaponsProfile.class, "attackReachMultiplier", WeightClass.HEAVY, false), new ProfileStatWeightSource(LightWeaponsProfile.class, "attackReachMultiplier", WeightClass.LIGHT, false), new AttributeSource("ATTACK_REACH_MULTIPLIER").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "attackReachMultiplier"), new GlobalBuffSource("attack_reach_multiplier"), new PotionEffectSource("ATTACK_REACH_MULTIPLIER"));
        register("RANGED_INACCURACY", new ArcheryChargedShotInaccuracySource(), new ProfileStatSource(ArcheryProfile.class, "inaccuracy"), new AttributeSource("ARROW_ACCURACY", true).penalty("attribute"), new PotionEffectSource("ARROW_ACCURACY", true));
        register("RANGED_VELOCITY_BONUS", new ArcheryChargedShotVelocitySource(), new ProfileStatSource(ArcheryProfile.class, "arrowVelocityMultiplier"), new AttributeSource("BOW_STRENGTH").penalty("damage"), new PotionEffectSource("BOW_STRENGTH"), new PotionEffectSource("ARROW_VELOCITY"));
        registerOffensive("KNOCKBACK_BONUS", new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "knockbackMultiplier", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "knockbackMultiplier", WeightClass.LIGHT), new AttributeAttackerSource("KNOCKBACK").penalty("attribute"), new ProfileStatAttackerSource(PowerProfile.class, "attackKnockbackBonus"), new GlobalBuffSource("knockback_bonus"), new PotionEffectAttackerSource("KNOCKBACK"));
        register("IMMUNITY_FRAME_BONUS", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setImmunityFlatBonus", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setImmunityFlatBonus", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "immunityFlatBonusPerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "immunityFlatBonusPerPiece", WeightClass.LIGHT), new AttributeDefenderSource("IMMUNITY_BONUS_FLAT").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "immunityFrameBonus"), new GlobalBuffSource("immunity_frame_bonus"), new PotionEffectSource("IMMUNITY_BONUS_FLAT"));
        register("IMMUNITY_FRAME_MULTIPLIER", new LightWeaponsDualWieldingImmunityReductionBuffSource(), new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setImmunityFractionBonus", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setImmunityFractionBonus", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "immunityFractionBonusPerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "immunityFractionBonusPerPiece", WeightClass.LIGHT), new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "immunityReductionFraction", WeightClass.HEAVY).n(), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "immunityReductionFraction", WeightClass.LIGHT).n(), new AttributeDefenderSource("IMMUNITY_BONUS_FRACTION").penalty("attribute"), new AttributeAttackerSource("IMMUNITY_REDUCTION", true).penalty("attribute"), new ProfileStatSource(PowerProfile.class, "immunityFrameMultiplier"), new GlobalBuffSource("fraction_immunity_frame_bonus"), new PotionEffectAttackerSource("IMMUNITY_REDUCTION", true), new PotionEffectSource("IMMUNITY_BONUS_FRACTION"));
        registerOffensive("BLEED_CHANCE", new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "bleedChance", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "bleedChance", WeightClass.LIGHT), new AttributeAttackerSource("BLEED_CHANCE").penalty("attribute"), new ProfileStatAttackerSource(PowerProfile.class, "bleedChance"), new GlobalBuffSource("bleed_chance"), new PotionEffectAttackerSource("BLEED_CHANCE"));
        registerOffensive("BLEED_DAMAGE", new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "bleedDamage", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "bleedDamage", WeightClass.LIGHT), new AttributeAttackerSource("BLEED_DAMAGE").penalty("attribute"), new ProfileStatAttackerSource(PowerProfile.class, "bleedDamage"), new GlobalBuffSource("bleed_damage"), new PotionEffectAttackerSource("BLEED_DAMAGE"));
        registerOffensive("BLEED_DURATION", new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "bleedDuration", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "bleedDuration", WeightClass.LIGHT), new AttributeAttackerSource("BLEED_DURATION").penalty("attribute"), new ProfileStatAttackerSource(PowerProfile.class, "bleedDuration"), new GlobalBuffSource("bleed_duration"), new PotionEffectAttackerSource("BLEED_DURATION"));
        register("DODGE_CHANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setDodgeChance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setDodgeChance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "dodgeChancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "dodgeChancePerPiece", WeightClass.LIGHT), new AttributeDefenderSource("DODGE_CHANCE").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "dodgeChance"), new GlobalBuffSource("dodge_chance"), new PotionEffectSource("DODGE_CHANCE"));
        register("REFLECT_CHANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setReflectChance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setReflectChance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "reflectChancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "reflectChancePerPiece", WeightClass.LIGHT), new AttributeDefenderSource("REFLECT_CHANCE").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "reflectChance"), new GlobalBuffSource("reflect_chance"), new PotionEffectSource("REFLECT_CHANCE"));
        register("REFLECT_FRACTION", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setReflectFraction", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setReflectFraction", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "reflectFractionPerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "reflectFractionPerPiece", WeightClass.LIGHT), new AttributeDefenderSource("REFLECT_FRACTION").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "reflectFraction"), new GlobalBuffSource("reflect_fraction"), new PotionEffectSource("REFLECT_FRACTION"));
        registerOffensive("DISMOUNT_CHANCE", new AttributeAttackerSource("DISMOUNT_CHANCE").penalty("attribute"), new ProfileStatAttackerSource(PowerProfile.class, "dismountChance"), new PotionEffectAttackerSource("DISMOUNT_CHANCE"), new GlobalBuffSource("dismount_chance"));
        registerOffensive("STUN_CHANCE", new ProfileStatAttackerHeldItemSource(ArcheryProfile.class, "bowStunChance", Material.BOW), new ProfileStatAttackerHeldItemSource(ArcheryProfile.class, "crossbowStunChance", Material.CROSSBOW), new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "stunChance", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "stunChance", WeightClass.LIGHT), new AttributeAttackerSource("STUN_CHANCE").penalty("attribute"), new ProfileStatAttackerSource(PowerProfile.class, "stunChance"), new GlobalBuffSource("stun_chance"), new PotionEffectAttackerSource("STUN_CHANCE"));
        registerOffensive("STUN_DURATION_BONUS", new ProfileStatAttackerHeldItemSource(ArcheryProfile.class, "stunDuration", Material.BOW, Material.CROSSBOW), new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "stunDuration", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "stunDuration", WeightClass.LIGHT), new ProfileStatAttackerSource(PowerProfile.class, "stunDurationBonus"));
        registerOffensive("CRIT_CHANCE", new ProfileStatAttackerHeldItemSource(ArcheryProfile.class, "bowCritChance", Material.BOW), new ProfileStatAttackerHeldItemSource(ArcheryProfile.class, "crossbowCritChance", Material.CROSSBOW), new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "critChance", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "critChance", WeightClass.LIGHT), new AttributeAttackerSource("CRIT_CHANCE").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "critChance"), new PotionEffectAttackerSource("CRIT_CHANCE"));
        registerOffensive("CRIT_DAMAGE", new ProfileStatAttackerHeldItemSource(ArcheryProfile.class, "critDamage", Material.BOW, Material.CROSSBOW), new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "critDamage", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "critDamage", WeightClass.LIGHT), new AttributeAttackerSource("CRIT_DAMAGE").penalty("damage"), new ProfileStatAttackerSource(PowerProfile.class, "critDamage"), new PotionEffectAttackerSource("CRIT_DAMAGE"));
        register("CROSSBOW_MAGAZINE", new AttributeSource("CROSSBOW_MAGAZINE"), new ProfileStatSource(PowerProfile.class, "crossbowMagazine"));
        registerOffensive("SHIELD_DISARMING", new AttributeAttackerSource("SHIELD_DISARMING"), new PotionEffectAttackerSource("SHIELD_DISARMING"), new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "shieldDisarming", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "shieldDisarming", WeightClass.LIGHT), new ProfileStatAttackerSource(PowerProfile.class, "shieldDisarming"));
        registerOffensive("LIFE_STEAL", new AttributeAttackerSource("LIFE_STEAL"), new PotionEffectAttackerSource("LIFE_STEAL"), new ProfileStatAttackerWeightSource(HeavyWeaponsProfile.class, "lifeSteal", WeightClass.HEAVY), new ProfileStatAttackerWeightSource(LightWeaponsProfile.class, "lifeSteal", WeightClass.LIGHT), new ProfileStatAttackerSource(PowerProfile.class, "lifeSteal"));

        register("PARRY_EFFECTIVENESS_DURATION", new ProfileStatSource(PowerProfile.class, "parryEffectiveDuration"), new ProfileStatWeightSource(LightWeaponsProfile.class, "parryEffectiveDuration", WeightClass.LIGHT, false), new ProfileStatWeightSource(HeavyWeaponsProfile.class, "parryEffectiveDuration", WeightClass.HEAVY, false));
        register("PARRY_VULNERABLE_DURATION", new ProfileStatSource(PowerProfile.class, "parryVulnerableDuration"), new ProfileStatWeightSource(LightWeaponsProfile.class, "parryVulnerableDuration", WeightClass.LIGHT, false), new ProfileStatWeightSource(HeavyWeaponsProfile.class, "parryVulnerableDuration", WeightClass.HEAVY, false));
        register("PARRY_ENEMY_DEBUFF_DURATION", new ProfileStatSource(PowerProfile.class, "parryEnemyDebuffDuration"), new ProfileStatWeightSource(LightWeaponsProfile.class, "parryEnemyDebuffDuration", WeightClass.LIGHT, false), new ProfileStatWeightSource(HeavyWeaponsProfile.class, "parryEnemyDebuffDuration", WeightClass.HEAVY, false));
        register("PARRY_SELF_DEBUFF_DURATION", new ProfileStatSource(PowerProfile.class, "parrySelfDebuffDuration"), new ProfileStatWeightSource(LightWeaponsProfile.class, "parrySelfDebuffDuration", WeightClass.LIGHT, false), new ProfileStatWeightSource(HeavyWeaponsProfile.class, "parrySelfDebuffDuration", WeightClass.HEAVY, false));
        register("PARRY_DAMAGE_REDUCTION", new ProfileStatSource(PowerProfile.class, "parryDamageReduction"), new ProfileStatWeightSource(LightWeaponsProfile.class, "parryDamageReduction", WeightClass.LIGHT, false), new ProfileStatWeightSource(HeavyWeaponsProfile.class, "parryDamageReduction", WeightClass.HEAVY, false));
        register("PARRY_COOLDOWN", new ProfileStatSource(PowerProfile.class, "parryCooldown"), new ProfileStatWeightSource(LightWeaponsProfile.class, "parryCooldown", WeightClass.LIGHT, false), new ProfileStatWeightSource(HeavyWeaponsProfile.class, "parryCooldown", WeightClass.HEAVY, false));
        register("PARRY_SUCCESS_COOLDOWN_REDUCTION", new ProfileStatSource(PowerProfile.class, "parryCooldownSuccessReduction"), new ProfileStatWeightSource(LightWeaponsProfile.class, "parryCooldownSuccessReduction", WeightClass.LIGHT, false), new ProfileStatWeightSource(HeavyWeaponsProfile.class, "parryCooldownSuccessReduction", WeightClass.HEAVY, false));

        // resistances
        register("DAMAGE_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setDamageResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setDamageResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "damageResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "damageResistancePerPiece", WeightClass.LIGHT), new ResistanceEnchantmentSource(Enchantment.PROTECTION_ENVIRONMENTAL, "protection"), new ResistanceArmorWeightClassSource("damage_resistance"), new ResistanceDamageResistanceSource(), new AttributeDefenderSource("DAMAGE_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "damageResistance"), new GlobalBuffSource("damage_resistance"), new AttributeDefenderSource("DAMAGE_TAKEN").negative(), new PotionEffectSource("DAMAGE_TAKEN", true), new PotionEffectSource("CUSTOM_DAMAGE_RESISTANCE"));
        register("MELEE_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setMeleeResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setMeleeResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "meleeResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "meleeResistancePerPiece", WeightClass.LIGHT), new ResistanceEnchantmentSource(Enchantment.PROTECTION_ENVIRONMENTAL, "protection_melee"), new ResistanceArmorWeightClassSource("melee_resistance"), new AttributeDefenderSource("MELEE_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "meleeResistance"), new GlobalBuffSource("melee_resistance"), new PotionEffectSource("MELEE_RESISTANCE"));
        register("PROJECTILE_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setProjectileResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setProjectileResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "projectileResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "projectileResistancePerPiece", WeightClass.LIGHT), new ResistanceEnchantmentSource(Enchantment.PROTECTION_PROJECTILE, "projectile_protection"), new ResistanceArmorWeightClassSource("projectile_resistance"), new AttributeDefenderSource("PROJECTILE_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "projectileResistance"), new GlobalBuffSource("projectile_resistance"), new PotionEffectSource("PROJECTILE_RESISTANCE"));
        register("BLUDGEONING_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setBludgeoningResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setBludgeoningResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "bludgeoningResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "bludgeoningResistancePerPiece", WeightClass.LIGHT), new ResistanceEnchantmentSource(Enchantment.PROTECTION_ENVIRONMENTAL, "protection_bludgeoning"), new ResistanceArmorWeightClassSource("bludgeoning_resistance"), new AttributeDefenderSource("BLUDGEONING_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "bludgeoningResistance"), new GlobalBuffSource("bludgeoning_resistance"), new PotionEffectSource("BLUDGEONING_RESISTANCE"));
        register("FIRE_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setFireResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setFireResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "fireResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "fireResistancePerPiece", WeightClass.LIGHT), new ResistanceEnchantmentSource(Enchantment.PROTECTION_FIRE, "fire_protection"), new ResistanceArmorWeightClassSource("fire_resistance"), new AttributeDefenderSource("FIRE_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "fireResistance"), new GlobalBuffSource("fire_resistance"), new PotionEffectSource("CUSTOM_FIRE_RESISTANCE"));
        register("EXPLOSION_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setExplosionResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setExplosionResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "explosionResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "explosionResistancePerPiece", WeightClass.LIGHT), new ResistanceEnchantmentSource(Enchantment.PROTECTION_EXPLOSIONS, "blast_protection"), new ResistanceArmorWeightClassSource("explosion_resistance"), new AttributeDefenderSource("EXPLOSION_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "explosionResistance"), new GlobalBuffSource("explosion_resistance"), new PotionEffectSource("EXPLOSION_RESISTANCE"));
        register("MAGIC_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setMagicResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setMagicResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "magicResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "magicResistancePerPiece", WeightClass.LIGHT), new ResistanceEnchantmentSource(Enchantment.PROTECTION_ENVIRONMENTAL, "protection_magic"), new ResistanceArmorWeightClassSource("magic_resistance"), new AttributeDefenderSource("MAGIC_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "magicResistance"), new GlobalBuffSource("magic_resistance"), new PotionEffectSource("MAGIC_RESISTANCE"));
        register("POISON_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setPoisonResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setPoisonResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "poisonResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "poisonResistancePerPiece", WeightClass.LIGHT), new ResistanceEnchantmentSource(Enchantment.PROTECTION_ENVIRONMENTAL, "protection_poison"), new ResistanceArmorWeightClassSource("poison_resistance"), new AttributeDefenderSource("POISON_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "poisonResistance"), new GlobalBuffSource("poison_resistance"), new PotionEffectSource("POISON_RESISTANCE"));
        register("FREEZING_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setFreezingResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setFreezingResistance", "setAmount", WeightClass.LIGHT), new ResistanceEnchantmentSource(Enchantment.PROTECTION_ENVIRONMENTAL, "protection_freezing"), new ResistanceArmorWeightClassSource("freezing_resistance"), new AttributeDefenderSource("FREEZING_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "freezingResistance"), new GlobalBuffSource("freezing_resistance"), new PotionEffectSource("FREEZING_RESISTANCE"));
        register("LIGHTNING_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setLightningResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setLightningResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "lightningResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "lightningResistancePerPiece", WeightClass.LIGHT), new ResistanceEnchantmentSource(Enchantment.PROTECTION_ENVIRONMENTAL, "protection_lightning"), new ResistanceArmorWeightClassSource("lightning_resistance"), new AttributeDefenderSource("LIGHTNING_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "lightningResistance"), new GlobalBuffSource("lightning_resistance"), new PotionEffectSource("LIGHTNING_RESISTANCE"));
        register("RADIANT_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setRadiantResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setRadiantResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "radiantResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "radiantResistancePerPiece", WeightClass.LIGHT), new ResistanceEnchantmentSource(Enchantment.PROTECTION_ENVIRONMENTAL, "protection_radiant"), new ResistanceArmorWeightClassSource("radiant_resistance"), new AttributeDefenderSource("RADIANT_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "radiantResistance"), new GlobalBuffSource("radiant_resistance"), new PotionEffectSource("RADIANT_RESISTANCE"));
        register("NECROTIC_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setNecroticResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setNecroticResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "necroticResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "necroticResistancePerPiece", WeightClass.LIGHT), new ResistanceEnchantmentSource(Enchantment.PROTECTION_ENVIRONMENTAL, "protection_necrotic"), new ResistanceArmorWeightClassSource("necrotic_resistance"), new AttributeDefenderSource("NECROTIC_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "necroticResistance"), new GlobalBuffSource("necrotic_resistance"), new PotionEffectSource("NECROTIC_RESISTANCE"));
        register("FALLING_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setFallDamageResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setFallDamageResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "fallDamageResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "fallDamageResistancePerPiece", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "freezingResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "freezingResistancePerPiece", WeightClass.LIGHT), new ResistanceEnchantmentSource(Enchantment.PROTECTION_FALL, "feather_falling"), new ResistanceArmorWeightClassSource("fall_resistance"), new AttributeDefenderSource("FALL_DAMAGE_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "fallDamageResistance"), new GlobalBuffSource("fall_damage_resistance"), new PotionEffectSource("FALL_DAMAGE_RESISTANCE"));
        register("STUN_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setStunResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setStunResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "stunResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "stunResistancePerPiece", WeightClass.LIGHT), new ResistanceArmorWeightClassSource("stun_resistance"), new AttributeDefenderSource("STUN_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "stunResistance"), new GlobalBuffSource("stun_resistance"), new PotionEffectSource("STUN_RESISTANCE"));
        register("BLEED_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setBleedResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setBleedResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "bleedResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "bleedResistancePerPiece", WeightClass.LIGHT), new ResistanceArmorWeightClassSource("bleeding_resistance"), new AttributeDefenderSource("BLEED_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "bleedResistance"), new GlobalBuffSource("bleeding_resistance"), new PotionEffectSource("BLEED_RESISTANCE"));
        register("CRIT_CHANCE_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setCritChanceResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setCritChanceResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "critChanceResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "critChanceResistancePerPiece", WeightClass.LIGHT), new ResistanceArmorWeightClassSource("crit_chance_resistance"), new AttributeDefenderSource("CRIT_CHANCE_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "critChanceResistance"), new PotionEffectSource("CRIT_CHANCE_RESISTANCE"));
        register("CRIT_DAMAGE_RESISTANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setCritDamageResistance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setCritDamageResistance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "critDamageResistancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "critDamageResistancePerPiece", WeightClass.LIGHT), new ResistanceArmorWeightClassSource("crit_damage_resistance"), new AttributeDefenderSource("CRIT_DAMAGE_RESISTANCE").penalty("resistance"), new ProfileStatSource(PowerProfile.class, "critDamageResistance"), new PotionEffectSource("CRIT_DAMAGE_RESISTANCE"));

        // utility related
        register("HEALING_BONUS", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setHealingBonus", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setHealingBonus", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "healingBonusPerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "healingBonusPerPiece", WeightClass.LIGHT), new AttributeSource("HEALING_BONUS").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "healthRegenerationBonus"), new GlobalBuffSource("healing_bonus"), new PotionEffectSource("HEALING_BONUS"));
        register("HUNGER_SAVE_CHANCE", new ProfileStatDefenderArmorWeightSetSource(HeavyArmorProfile.class, "setHungerSaveChance", "setAmount", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSetSource(LightArmorProfile.class, "setHungerSaveChance", "setAmount", WeightClass.LIGHT), new ProfileStatDefenderArmorWeightSource(HeavyArmorProfile.class, "hungerSaveChancePerPiece", WeightClass.HEAVY), new ProfileStatDefenderArmorWeightSource(LightArmorProfile.class, "hungerSaveChancePerPiece", WeightClass.LIGHT), new AttributeSource("FOOD_CONSUMPTION").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "hungerSaveChance"), new GlobalBuffSource("fraction_hunger_reduction"), new PotionEffectSource("FOOD_CONSUMPTION"));
        register("COOLDOWN_REDUCTION", new AttributeSource("COOLDOWN_REDUCTION").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "cooldownReduction"), new GlobalBuffSource("cooldown_reduction"), new PotionEffectSource("COOLDOWN_REDUCTION"));
        register("CRAFTING_TIME_REDUCTION", new AttributeSource("CRAFTING_SPEED").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "craftingTimeReduction"), new GlobalBuffSource("crafting_time_reduction"), new PotionEffectSource("CRAFTING_SPEED"));
        register("COOKING_SPEED_BONUS", new AttributeSource("COOKING_SPEED").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "cookingSpeedBonus"), new GlobalBuffSource("cooking_time_reduction"), new PotionEffectSource("COOKING_SPEED"));
        register("AMMO_SAVE_CHANCE", new ProfileStatSource(ArcheryProfile.class, "ammoSaveChance"), new AttributeSource("AMMO_CONSUMPTION", true));
        register("DURABILITY_BONUS", new AttributeSource("DURABILITY"), new ProfileStatSource(PowerProfile.class, "durabilityMultiplier"), new GlobalBuffSource("fraction_durability_bonus"), new PotionEffectSource("DURABILITY"));
        register("ENTITY_DROPS", new ProfileStatWeightSource(HeavyWeaponsProfile.class, "dropsMultiplier", WeightClass.HEAVY, false), new ProfileStatWeightSource(LightWeaponsProfile.class, "dropsMultiplier", WeightClass.LIGHT, false), new AttributeSource("ENTITY_DROPS").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "entityDropMultiplier"), new GlobalBuffSource("entity_drops"), new PotionEffectSource("ENTITY_DROPS"));
        register("ENTITY_DROP_LUCK", new ProfileStatAttackerVictimClassSource(FarmingProfile.class, "butcheryLuck", EntityClassification.ANIMAL), new ProfileStatWeightSource(HeavyWeaponsProfile.class, "rareDropsMultiplier", WeightClass.HEAVY, false), new ProfileStatWeightSource(LightWeaponsProfile.class, "rareDropsMultiplier", WeightClass.LIGHT, false), new AttributeSource("ENTITY_RARE_DROPS").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "entityRareDropMultiplier"), new GlobalBuffSource("entity_rare_drops"), new PotionEffectSource("ENTITY_RARE_DROPS"));
        register("JUMP_HEIGHT_MULTIPLIER", new AttributeSource("JUMP_HEIGHT").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "jumpHeightBonus"), new PotionEffectSource("JUMP_HEIGHT"));
        register("JUMPS_BONUS", new AttributeSource("JUMPS").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "jumpBonus"), new PotionEffectSource("JUMPS"));
        register("SNEAK_MOVEMENT_SPEED_BONUS", new AttributeSource("SNEAK_MOVEMENT_SPEED_BONUS").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "sneakMovementSpeedBonus"), new GlobalBuffSource("movement_sneak_speed"), new PotionEffectSource("SNEAK_MOVEMENT_SPEED_BONUS"));
        register("SPRINT_MOVEMENT_SPEED_BONUS", new AttributeSource("SPRINT_MOVEMENT_SPEED_BONUS").penalty("attribute"), new ProfileStatSource(PowerProfile.class, "sprintMovementSpeedBonus"), new GlobalBuffSource("movement_sprint_speed"), new PotionEffectSource("SPRINT_MOVEMENT_SPEED_BONUS"));
        register("DIG_SPEED", new PotionEffectSource("DIG_SPEED"), new DrillingMiningSpeedSource());
        if (MinecraftVersion.currentVersionOlderThan(MinecraftVersion.MINECRAFT_1_20)) register("DIG_SPEED", new AttributeSource("DIG_SPEED"));
        register("BLOCK_SPECIFIC_DIG_SPEED", new DiggingStatSource("diggingSpeedBonus"), new WoodcuttingStatSource("woodcuttingSpeedBonus"), new MiningDrillingActiveSource(), new MiningStatSource("miningSpeedBonus"), new MiningUnbreakableBlocksSource()); // should never be cached as this stat is dependent on the block currently being looked at
        register("FISHING_LUCK", new AttributeSource("FISHING_LUCK"), new PotionEffectSource("FISHING_LUCK"), new FishingLuckLotSSource(), new FishingLuckFullMoonSource(), new FishingLuckNewMoonSource());
        register("FISHING_SPEED_MULTIPLIER", new AttributeSource("FISHING_LUCK"), new PotionEffectSource("FISHING_LUCK"), new FishingLuckLotSSource(), new FishingLuckFullMoonSource(), new FishingLuckNewMoonSource());
        register("EXPLOSION_RADIUS_MULTIPLIER", new ProfileStatSource(MiningProfile.class, "tntBlastRadius"), new AttributeSource("EXPLOSION_POWER"), new PotionEffectSource("EXPLOSION_POWER"), new GlobalBuffSource("blast_mining_radius_multiplier"));

        // food related
        register("FOOD_BONUS_VEGETABLE", new ProfileStatSource(PowerProfile.class, "foodBonusVegetable"));
        register("FOOD_BONUS_SEASONING", new ProfileStatSource(PowerProfile.class, "foodBonusSeasoning"));
        register("FOOD_BONUS_ALCOHOLIC", new ProfileStatSource(PowerProfile.class, "foodBonusAlcoholic"));
        register("FOOD_BONUS_BEVERAGE", new ProfileStatSource(PowerProfile.class, "foodBonusBeverage"));
        register("FOOD_BONUS_SPOILED", new ProfileStatSource(PowerProfile.class, "foodBonusSpoiled"));
        register("FOOD_BONUS_SEAFOOD", new ProfileStatSource(PowerProfile.class, "foodBonusSeafood"));
        register("FOOD_BONUS_MAGICAL", new ProfileStatSource(PowerProfile.class, "foodBonusMagical"));
        register("FOOD_BONUS_SWEET", new ProfileStatSource(PowerProfile.class, "foodBonusSweet"));
        register("FOOD_BONUS_GRAIN", new ProfileStatSource(PowerProfile.class, "foodBonusGrain"));
        register("FOOD_BONUS_FRUIT", new ProfileStatSource(PowerProfile.class, "foodBonusFruit"));
        register("FOOD_BONUS_NUTS", new ProfileStatSource(PowerProfile.class, "foodBonusNuts"));
        register("FOOD_BONUS_DAIRY", new ProfileStatSource(PowerProfile.class, "foodBonusDairy"));
        register("FOOD_BONUS_MEAT", new ProfileStatSource(PowerProfile.class, "foodBonusMeat"));
        register("FOOD_BONUS_FATS", new ProfileStatSource(PowerProfile.class, "foodBonusFats"));

        // smithing stats
        register("SMITHING_QUALITY_GENERAL", new AttributeSource("SMITHING_QUALITY").penalty("attribute"), new ProfileStatSource(SmithingProfile.class, "genericCraftingSkill"), new GlobalBuffSource("smithing_quality"), new PotionEffectSource("SMITHING_QUALITY"), new PotionEffectSingleUseSource("SMITHING_MASTERPIECE_FLAT"));
        register("SMITHING_QUALITY_WOOD", new ProfileStatSource(SmithingProfile.class, "woodCraftingSkill"));
        register("SMITHING_QUALITY_LEATHER", new ProfileStatSource(SmithingProfile.class, "leatherCraftingSkill"));
        register("SMITHING_QUALITY_STONE", new ProfileStatSource(SmithingProfile.class, "stoneCraftingSkill"));
        register("SMITHING_QUALITY_CHAINMAIL", new ProfileStatSource(SmithingProfile.class, "chainCraftingSkill"));
        register("SMITHING_QUALITY_GOLD", new ProfileStatSource(SmithingProfile.class, "goldCraftingSkill"));
        register("SMITHING_QUALITY_IRON", new ProfileStatSource(SmithingProfile.class, "ironCraftingSkill"));
        register("SMITHING_QUALITY_DIAMOND", new ProfileStatSource(SmithingProfile.class, "diamondCraftingSkill"));
        register("SMITHING_QUALITY_NETHERITE", new ProfileStatSource(SmithingProfile.class, "netheriteCraftingSkill"));
        register("SMITHING_QUALITY_BOW", new ProfileStatSource(SmithingProfile.class, "bowCraftingSkill"));
        register("SMITHING_QUALITY_CROSSBOW", new ProfileStatSource(SmithingProfile.class, "crossbowCraftingSkill"));
        register("SMITHING_QUALITY_PRISMARINE", new ProfileStatSource(SmithingProfile.class, "prismarineCraftingSkill"));
        register("SMITHING_QUALITY_ENDERIC", new ProfileStatSource(SmithingProfile.class, "endericCraftingSkill"));
        register("SMITHING_FRACTION_QUALITY_GENERAL", new AttributeSource("SMITHING_QUALITY_FRACTION").penalty("attribute"), new ProfileStatSource(SmithingProfile.class, "genericCraftingSkillFractionBonus"), new GlobalBuffSource("smithing_quality_fraction"), new PotionEffectSource("SMITHING_QUALITY_FRACTION"), new PotionEffectSingleUseSource("SMITHING_MASTERPIECE_FRACTION"));
        register("SMITHING_FRACTION_QUALITY_WOOD", new ProfileStatSource(SmithingProfile.class, "woodCraftingSkillFractionBonus"));
        register("SMITHING_FRACTION_QUALITY_LEATHER", new ProfileStatSource(SmithingProfile.class, "leatherCraftingSkillFractionBonus"));
        register("SMITHING_FRACTION_QUALITY_STONE", new ProfileStatSource(SmithingProfile.class, "stoneCraftingSkillFractionBonus"));
        register("SMITHING_FRACTION_QUALITY_CHAINMAIL", new ProfileStatSource(SmithingProfile.class, "chainCraftingSkillFractionBonus"));
        register("SMITHING_FRACTION_QUALITY_GOLD", new ProfileStatSource(SmithingProfile.class, "goldCraftingSkillFractionBonus"));
        register("SMITHING_FRACTION_QUALITY_IRON", new ProfileStatSource(SmithingProfile.class, "ironCraftingSkillFractionBonus"));
        register("SMITHING_FRACTION_QUALITY_DIAMOND", new ProfileStatSource(SmithingProfile.class, "diamondCraftingSkillFractionBonus"));
        register("SMITHING_FRACTION_QUALITY_NETHERITE", new ProfileStatSource(SmithingProfile.class, "netheriteCraftingSkillFractionBonus"));
        register("SMITHING_FRACTION_QUALITY_BOW", new ProfileStatSource(SmithingProfile.class, "bowCraftingSkillFractionBonus"));
        register("SMITHING_FRACTION_QUALITY_CROSSBOW", new ProfileStatSource(SmithingProfile.class, "crossbowCraftingSkillFractionBonus"));
        register("SMITHING_FRACTION_QUALITY_PRISMARINE", new ProfileStatSource(SmithingProfile.class, "prismarineCraftingSkillFractionBonus"));
        register("SMITHING_FRACTION_QUALITY_ENDERIC", new ProfileStatSource(SmithingProfile.class, "endericCraftingSkillFractionBonus"));
        register("SMITHING_EXP_GAIN_GENERAL", new ProfileStatSource(SmithingProfile.class, "genericEXPMultiplier"), new GlobalBuffSource("smithing_experience"));
        register("SMITHING_EXP_GAIN_WOOD", new ProfileStatSource(SmithingProfile.class, "woodEXPMultiplier"));
        register("SMITHING_EXP_GAIN_LEATHER", new ProfileStatSource(SmithingProfile.class, "leatherEXPMultiplier"));
        register("SMITHING_EXP_GAIN_STONE", new ProfileStatSource(SmithingProfile.class, "stoneEXPMultiplier"));
        register("SMITHING_EXP_GAIN_CHAINMAIL", new ProfileStatSource(SmithingProfile.class, "chainEXPMultiplier"));
        register("SMITHING_EXP_GAIN_GOLD", new ProfileStatSource(SmithingProfile.class, "goldEXPMultiplier"));
        register("SMITHING_EXP_GAIN_IRON", new ProfileStatSource(SmithingProfile.class, "ironEXPMultiplier"));
        register("SMITHING_EXP_GAIN_DIAMOND", new ProfileStatSource(SmithingProfile.class, "diamondEXPMultiplier"));
        register("SMITHING_EXP_GAIN_NETHERITE", new ProfileStatSource(SmithingProfile.class, "netheriteEXPMultiplier"));
        register("SMITHING_EXP_GAIN_BOW", new ProfileStatSource(SmithingProfile.class, "bowEXPMultiplier"));
        register("SMITHING_EXP_GAIN_CROSSBOW", new ProfileStatSource(SmithingProfile.class, "crossbowEXPMultiplier"));
        register("SMITHING_EXP_GAIN_PRISMARINE", new ProfileStatSource(SmithingProfile.class, "prismarineEXPMultiplier"));
        register("SMITHING_EXP_GAIN_ENDERIC", new ProfileStatSource(SmithingProfile.class, "endericEXPMultiplier"));

        register("ALCHEMY_QUALITY_GENERAL", new AttributeSource("ALCHEMY_QUALITY").penalty("attribute"), new ProfileStatSource(AlchemyProfile.class, "genericBrewingSkill"), new GlobalBuffSource("alchemy_quality"), new PotionEffectSource("ALCHEMY_QUALITY"), new PotionEffectSingleUseSource("ALCHEMY_MASTERPIECE_FLAT"));
        register("ALCHEMY_QUALITY_DEBUFF", new ProfileStatSource(AlchemyProfile.class, "debuffBrewingSkill"));
        register("ALCHEMY_QUALITY_BUFF", new ProfileStatSource(AlchemyProfile.class, "buffBrewingSkill"));
        register("ALCHEMY_FRACTION_QUALITY_GENERAL", new AttributeSource("ALCHEMY_QUALITY_FRACTION").penalty("attribute"), new ProfileStatSource(AlchemyProfile.class, "genericBrewingSkillFractionBonus"), new GlobalBuffSource("alchemy_quality_fraction"), new PotionEffectSource("ALCHEMY_QUALITY_FRACTION"), new PotionEffectSingleUseSource("ALCHEMY_MASTERPIECE_FRACTION"));
        register("ALCHEMY_FRACTION_QUALITY_DEBUFF", new ProfileStatSource(AlchemyProfile.class, "buffBrewingSkillFractionBonus"));
        register("ALCHEMY_FRACTION_QUALITY_BUFF", new ProfileStatSource(AlchemyProfile.class, "debuffBrewingSkillFractionBonus"));
        register("BREWING_SPEED_BONUS", new AttributeSource("BREWING_SPEED").penalty("attribute"), new ProfileStatSource(AlchemyProfile.class, "brewingTimeReduction"), new GlobalBuffSource("brewing_speed"), new PotionEffectSource("BREWING_SPEED"));
        register("BREWING_INGREDIENT_SAVE_CHANCE", new AttributeSource("BREWING_INGREDIENT_CONSUMPTION").penalty("attribute"), new ProfileStatSource(AlchemyProfile.class, "brewingIngredientSaveChance"), new GlobalBuffSource("brewing_ingredient_save_chance"), new PotionEffectSource("BREWING_INGREDIENT_CONSUMPTION", true));
        register("POTION_SAVE_CHANCE", new AttributeSource("POTION_CONSUMPTION").penalty("attribute"), new ProfileStatSource(AlchemyProfile.class, "potionSaveChance"), new GlobalBuffSource("potion_save_chance"), new PotionEffectSource("POTION_CONSUMPTION"));
        register("THROW_VELOCITY_BONUS", new AttributeSource("THROWING_VELOCITY").penalty("attribute"), new ProfileStatSource(AlchemyProfile.class, "throwVelocity"), new GlobalBuffSource("throw_velocity"), new PotionEffectSource("THROWING_VELOCITY"));
        register("ALCHEMY_EXP_GAIN", new ProfileStatSource(AlchemyProfile.class, "alchemyEXPMultiplier"), new GlobalBuffSource("alchemy_experience"));
        register("SPLASH_INTENSITY_MINIMUM", new AttributeSource("SPLASH_INTENSITY_MINIMUM"), new PotionEffectSource("SPLASH_INTENSITY_MINIMUM"), new ProfileStatSource(AlchemyProfile.class, "splashIntensityMinimum"), new GlobalBuffSource("splash_intensity_minimum"));
        register("LINGERING_DURATION_MULTIPLIER", new AttributeSource("LINGERING_DURATION_MULTIPLIER"), new PotionEffectSource("LINGERING_DURATION_MULTIPLIER"), new ProfileStatSource(AlchemyProfile.class, "lingeringDurationMultiplier"), new GlobalBuffSource("lingering_duration"));
        register("LINGERING_RADIUS_MULTIPLIER", new AttributeSource("LINGERING_RADIUS_MULTIPLIER"), new PotionEffectSource("LINGERING_RADIUS_MULTIPLIER"), new ProfileStatSource(AlchemyProfile.class, "lingeringRadiusMultiplier"), new GlobalBuffSource("lingering_radius"));

        register("ENCHANTING_QUALITY", new AttributeSource("ENCHANTING_QUALITY"), new PotionEffectSource("ENCHANTING_QUALITY"), new ProfileStatSource(EnchantingProfile.class, "enchantingSkill"), new GlobalBuffSource("enchanting_quality"), new PotionEffectSingleUseSource("ENCHANTING_MASTERPIECE_FLAT"));
        register("ENCHANTING_FRACTION_QUALITY", new AttributeSource("ENCHANTING_QUALITY_FRACTION"), new PotionEffectSource("ENCHANTING_QUALITY_FRACTION"), new ProfileStatSource(EnchantingProfile.class, "enchantingSkillFractionBonus"), new GlobalBuffSource("enchanting_quality"), new PotionEffectSingleUseSource("ENCHANTING_MASTERPIECE_FRACTION"));
        register("ENCHANTING_QUALITY_ANVIL", new AttributeSource("ANVIL_QUALITY"), new PotionEffectSource("ANVIL_QUALITY"), new ProfileStatSource(EnchantingProfile.class, "anvilSkill"));
        register("ENCHANTING_FRACTION_QUALITY_ANVIL", new AttributeSource("ANVIL_QUALITY_FRACTION"), new PotionEffectSource("ANVIL_QUALITY_FRACTION"), new ProfileStatSource(EnchantingProfile.class, "anvilSkillFractionBonus"));
        register("ENCHANTING_AMPLIFY_CHANCE", new ProfileStatSource(EnchantingProfile.class, "enchantmentAmplificationChance"), new GlobalBuffSource("enchanting_amplify_chance"));
        register("ENCHANTING_LAPIS_SAVE_CHANCE", new AttributeSource("LAPIS_SAVE_CHANCE", true), new PotionEffectSource("LAPIS_SAVE_CHANCE", true), new ProfileStatSource(EnchantingProfile.class, "lapisSaveChance"), new GlobalBuffSource("enchanting_lapis_save_chance"));
        register("ENCHANTING_VANILLA_EXP_GAIN", new AttributeSource("VANILLA_EXP_GAIN"), new PotionEffectSource("VANILLA_EXP_GAIN"), new ProfileStatSource(EnchantingProfile.class, "essenceMultiplier"), new GlobalBuffSource("vanilla_exp_multiplier"));
        register("ENCHANTING_REFUND_CHANCE", new AttributeSource("ENCHANTING_REFUND_CHANCE"), new PotionEffectSource("ENCHANTING_REFUND_CHANCE"), new ProfileStatSource(EnchantingProfile.class, "essenceRefundChance"), new GlobalBuffSource("enchanting_exp_refund_chance"));
        register("ENCHANTING_REFUND_AMOUNT", new AttributeSource("ENCHANTING_REFUND_FRACTION"), new PotionEffectSource("ENCHANTING_REFUND_FRACTION"), new ProfileStatSource(EnchantingProfile.class, "essenceRefundFraction"), new GlobalBuffSource("enchanting_exp_refund_amount"));
        register("ENCHANTING_EXP_GAIN", new ProfileStatSource(EnchantingProfile.class, "enchantingEXPMultiplier"), new GlobalBuffSource("enchanting_experience"));

        register("BUTCHERY_DROP_MULTIPLIER", new ProfileStatSource(FarmingProfile.class, "butcheryDrops"), new AttributeSource("FARMING_DROPS"), new PotionEffectSource("FARMING_DROPS"), new GlobalBuffSource("butchery_drop_multiplier"));
        register("FARMING_DROP_MULTIPLIER", new ProfileStatSource(FarmingProfile.class, "farmingDrops"), new AttributeSource("FARMING_DROPS"), new PotionEffectSource("FARMING_DROPS"), new GlobalBuffSource("farming_drop_multiplier"));
        register("FARMING_LUCK", new ProfileStatSource(FarmingProfile.class, "farmingLuck"), new AttributeSource("FARMING_RARE_DROPS"), new PotionEffectSource("FARMING_RARE_DROPS"), new GlobalBuffSource("farming_luck"));
        register("FARMING_EXP_GAIN", new ProfileStatSource(FarmingProfile.class, "farmingEXPMultiplier"), new GlobalBuffSource("farming_experience"));

        register("MINING_DROP_MULTIPLIER", new ProfileStatSource(MiningProfile.class, "miningDrops"), new AttributeSource("MINING_DROPS"), new PotionEffectSource("MINING_DROPS"), new GlobalBuffSource("mining_drop_multiplier"));
        register("MINING_LUCK", new ProfileStatSource(MiningProfile.class, "miningLuck"), new AttributeSource("MINING_RARE_DROPS"), new PotionEffectSource("MINING_RARE_DROPS"), new GlobalBuffSource("mining_luck"));
        register("BLASTING_DROP_MULTIPLIER", new ProfileStatSource(MiningProfile.class, "blastingDrops"), new AttributeSource("MINING_DROPS"), new PotionEffectSource("MINING_DROPS"), new GlobalBuffSource("blasting_drop_multiplier"));
        register("BLASTING_LUCK", new ProfileStatSource(MiningProfile.class, "blastingLuck"), new AttributeSource("MINING_RARE_DROPS"), new PotionEffectSource("MINING_RARE_DROPS"), new GlobalBuffSource("blasting_luck"));
        register("MINING_EXP_GAIN", new ProfileStatSource(MiningProfile.class, "miningEXPMultiplier"), new GlobalBuffSource("mining_experience"));

        register("DIGGING_DROP_MULTIPLIER", new ProfileStatSource(DiggingProfile.class, "diggingDrops"), new AttributeSource("DIGGING_DROPS"), new PotionEffectSource("DIGGING_DROPS"), new GlobalBuffSource("digging_drop_multiplier"));
        register("DIGGING_LUCK", new ProfileStatSource(DiggingProfile.class, "diggingLuck"), new AttributeSource("DIGGING_RARE_DROPS"), new PotionEffectSource("DIGGING_RARE_DROPS"), new GlobalBuffSource("digging_luck"));
        register("DIGGING_ARCHAEOLOGY_LUCK", new ProfileStatSource(DiggingProfile.class, "archaeologyLuck"), new AttributeSource("DIGGING_RARE_DROPS"), new PotionEffectSource("DIGGING_RARE_DROPS"), new GlobalBuffSource("archaeology_luck"));
        register("DIGGING_EXP_GAIN", new ProfileStatSource(DiggingProfile.class, "diggingEXPMultiplier"), new GlobalBuffSource("digging_experience"));

        register("WOODCUTTING_DROP_MULTIPLIER", new ProfileStatSource(WoodcuttingProfile.class, "woodcuttingDrops"), new AttributeSource("WOODCUTTING_DROPS"), new PotionEffectSource("WOODCUTTING_DROPS"), new GlobalBuffSource("woodcutting_drop_multiplier"));
        register("WOODCUTTING_LUCK", new ProfileStatSource(WoodcuttingProfile.class, "woodcuttingLuck"), new AttributeSource("WOODCUTTING_RARE_DROPS"), new PotionEffectSource("WOODCUTTING_RARE_DROPS"), new GlobalBuffSource("woodcutting_luck"));
        register("WOODCUTTING_EXP_GAIN", new ProfileStatSource(WoodcuttingProfile.class, "woodcuttingEXPMultiplier"), new GlobalBuffSource("woodcutting_experience"));

        register("FISHING_EXP_GAIN", new ProfileStatSource(FishingProfile.class, "fishingEXPMultiplier"), new GlobalBuffSource("fishing_experience"));

        register("ARCHERY_EXP_GAIN", new ProfileStatSource(ArcheryProfile.class, "archeryEXPMultiplier"));

        register("LIGHT_ARMOR_EXP_GAIN", new ProfileStatSource(LightArmorProfile.class, "lightArmorEXPMultiplier"));

        register("HEAVY_ARMOR_EXP_GAIN", new ProfileStatSource(HeavyArmorProfile.class, "heavyArmorEXPMultiplier"));

        register("LIGHT_WEAPONS_EXP_GAIN", new ProfileStatSource(LightWeaponsProfile.class, "lightWeaponsEXPMultiplier"));

        register("HEAVY_WEAPONS_EXP_GAIN", new ProfileStatSource(HeavyWeaponsProfile.class, "heavyWeaponsEXPMultiplier"));
    }

    /**
     * Registers the given stat sources to an existing stat collector, or creates a simple new one if none under the stat name were registered.
     * This collector will be used to conditionally collect a specific stat from a given entity.
     * @param stat the stat to register new sources to
     * @param s the sources to add to the collector
     */
    public static void register(String stat, AccumulativeStatSource... s){
        StatCollector existingSource = sources.get(stat);
        if (existingSource == null) existingSource = new StatCollectorBuilder().addSources(s).build();
        else existingSource.getStatSources().addAll(Arrays.asList(s));
        register(stat, existingSource);
    }

    public static void registerOffensive(String stat, AccumulativeStatSource... s){
        StatCollector existingSource = sources.get(stat);
        if (existingSource == null) existingSource = new StatCollectorBuilder().addSources(s).setAttackerPossessive().build();
        else existingSource.getStatSources().addAll(Arrays.asList(s));
        register(stat, existingSource);
    }

    public static void register(String stat, StatCollector collector){
        sources.put(stat, collector);
    }

    public static StatCollector getStatCollector(String stat){
        if (!sources.containsKey(stat)) throw new IllegalArgumentException("A stat collector with the name " + stat + " was fetched, but it does not exist");
        return sources.get(stat);
    }

    /**
     * Collects all the stats of the given stat name. For example, if 'SMITHING_SKILL_ALL' is given, it will return the
     * given entity's total skill level in general smithing, which would be the accumulation of actual skill, potion
     * effects, global boosters, etc.
     * This method should only be used in situations only involving a single entity (such as a player crafting, taking damage
     * from non-entity related sources, or walking)
     * @param stat the stat to gather its total from
     * @param e the entity to gather their stats from
     * @param use if true, it will be assumed the stat is actually being used in practice rather than being a visual
     *            for show. Example: if a player were to craft an item and had some potion effect to boost their
     *            crafting quality, use would be true. If the player were to only look at the item in the crafting menu,
     *            so not actually having crafted it just yet, use would be false.
     * @return the combined stat number
     */
    public static double getStats(String stat, Entity e, boolean use) {
        StatCollector collector = getStatCollector(stat);
        Collection<AccumulativeStatSource> existingSources = collector.getStatSources();
        double value = 0;
        for (AccumulativeStatSource s : existingSources){
            value += s.fetch(e, use);
        }
        if (!collector.isAttackerPossessive() && e instanceof LivingEntity l && !(l instanceof Player)) value += MonsterScalingManager.getStatValue(l, stat);
        if (!collector.isAttackerPossessive() && e instanceof Player p) value += PartyManager.getCompanyStats(p, stat);
        return (double) Math.round(value * 1000000d) / 1000000d; // round to 6 decimals
    }

    /**
     * Collects all the stats of the given stat name. For example, if 'LIGHT_GENERIC_ARMOR' is given, it will return the<br>
     * given entity's total armor points influenced by the second entity given, which would be the accumulation of<br>
     * actual skill, potion effects, global boosters, etc.<br>
     * This method should be used in situations involving two entities, where the FIRST entity is the defending entity in<br>
     * question, and the SECOND entity attacking entity.<br>
     *<br>
     * If the second entity is null, {@link AccumulativeStatManager#getStats(String, Entity, boolean)} is being called instead.<br>
     * If a stat source associated with the given stat is not an EvEAccumulativeStatSource, a regular AccumulativeStatSource<br>
     * is being used instead where the second entity is not involved.
     * @param stat the stat to gather its total from
     * @param e1 the defending entity to gather their stats from
     * @param e2 the attacking entity involved in stat accumulation
     * @param use if true, it will be assumed the stat is actually being used in practice rather than being a visual
     *            for show. Example: if a player were to craft an item and had some potion effect to boost their
     *            crafting quality, use would be true. If the player were to only look at the item in the crafting menu,
     *            so not actually having crafted it just yet, use would be false.
     * @return the collective stat number
     */
    public static double getRelationalStats(String stat, Entity e1, Entity e2, boolean use) {
        if (e1 == null) return 0;
        if (e2 == null) return getStats(stat, e1, use);
        StatCollector collector = getStatCollector(stat);
        Collection<AccumulativeStatSource> existingSources = collector.getStatSources();
        double value = 0;
        for (AccumulativeStatSource s : existingSources){
            if (s instanceof EvEAccumulativeStatSource os) value += os.fetch(e1, e2, use);
            else value += s.fetch(e1, use);
        }
        if (collector.isAttackerPossessive() && e2 instanceof LivingEntity l && !(l instanceof Player)) value += MonsterScalingManager.getStatValue(l, stat);
        else if (!collector.isAttackerPossessive() && e1 instanceof LivingEntity l && !(l instanceof Player)) value += MonsterScalingManager.getStatValue(l, stat);
        if (collector.isAttackerPossessive() && e2 instanceof Player p) value += PartyManager.getCompanyStats(p, stat);
        else if (!collector.isAttackerPossessive() && e1 instanceof Player p) value += PartyManager.getCompanyStats(p, stat);

        return Utils.round6Decimals(value); // round to 6 decimals
    }

    public static double getCachedStats(String stat, Entity p, long refreshAfter, boolean use){
        if (p == null) return 0;
        if (isStatCached(p, stat)){
            return getCachedStatIgnoringExpiration(p, stat);
        } else {
            double statValue = getStats(stat, p, use);
            cacheStat(p, stat, statValue, refreshAfter);
            return statValue;
        }
    }

    public static double getCachedRelationalStats(String stat, Entity victimPrimary, Entity attacker, long refreshAfter, boolean use){
        if (victimPrimary == null) return 0;
        if (isStatCached(victimPrimary, stat)){
            return getCachedStatIgnoringExpiration(victimPrimary, stat);
        } else {
            double statValue = getRelationalStats(stat, victimPrimary, attacker, use);
            cacheStat(victimPrimary, stat, statValue, refreshAfter);
            return statValue;
        }
    }

    public static double getCachedAttackerRelationalStats(String stat, Entity victim, Entity attackerPrimary, long refreshAfter, boolean use){
        if (victim == null || attackerPrimary == null) return 0;
        if (isStatCached(attackerPrimary, stat)){
            return getCachedStatIgnoringExpiration(attackerPrimary, stat);
        } else {
            double statValue = getRelationalStats(stat, victim, attackerPrimary, use);
            cacheStat(attackerPrimary, stat, statValue, refreshAfter);
            return statValue;
        }
    }

    private static final Map<UUID, Map<String, Map.Entry<Long, Double>>> statCache = new HashMap<>();

    private static long lastMapCleanup = System.currentTimeMillis();

    /**
     * Attempts to get a cached stat from an entity
     * isStatCached() should be checked beforehand, if the stat is not cached this method will return 0, but will still return
     * the value even if the stat is expired
     * @param e the entity to check the cache for
     * @param stat the stat to get from the cache
     * @return the cached stat value, or 0 if the stat is not cached. This will still return the value even if the stat is expired
     */
    private static double getCachedStatIgnoringExpiration(Entity e, String stat){
        Map<String, Map.Entry<Long, Double>> currentCachedStats = statCache.getOrDefault(e.getUniqueId(), new HashMap<>());
        if (currentCachedStats.get(stat) != null) return currentCachedStats.get(stat).getValue();
        return 0;
    }

    /**
     * Caches a stat for an entity for a specified amount of time
     * @param e the entity to cache for
     * @param stat the stat to cache
     * @param amount the value that will be cached
     * @param cacheFor the amount of time the cached value will be valid for, in milliseconds
     */
    public static void cacheStat(Entity e, String stat, double amount, long cacheFor){
        Map<String, Map.Entry<Long, Double>> currentCachedStats = statCache.getOrDefault(e.getUniqueId(), new HashMap<>());
        currentCachedStats.put(stat, new Map.Entry<>() {
            private final long time = System.currentTimeMillis() + cacheFor;
            @Override
            public Long getKey() { return time; }
            @Override
            public Double getValue() { return amount; }
            @Override
            public Double setValue(Double value) { return null; }
        });
        statCache.put(e.getUniqueId(), currentCachedStats);
    }

    /**
     * Checks if a certain stat is cached, and if so, if the stat hasn't expired.
     * This method also cleans up the cache every 2 minutes, removing any entity where isValid() returns false
     * @param e the entity to check their cached stat
     * @param stat the stat to see if it's cached
     * @return true if the stat is cached and unexpired, false otherwise
     */
    private static boolean isStatCached(Entity e, String stat){
        attemptMapCleanup();
        if (statCache.containsKey(e.getUniqueId())){
            if (statCache.getOrDefault(e.getUniqueId(), new HashMap<>()).get(stat) != null){
                return statCache.get(e.getUniqueId()).get(stat).getKey() > System.currentTimeMillis();
            }
        }
        return false;
    }

    private static void attemptMapCleanup(){
        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
            if (lastMapCleanup + 120000 < System.currentTimeMillis()){
                // cleaning up map every 2 minutes
                Map<UUID, Map<String, Map.Entry<Long, Double>>> clone = new HashMap<>(statCache);
                for (UUID uuid : clone.keySet()){
                    Entity entity = ValhallaMMO.getInstance().getServer().getEntity(uuid);
                    if (entity != null && !entity.isValid()) statCache.remove(entity.getUniqueId()); // remove invalid entities from the map
                }
                lastMapCleanup = System.currentTimeMillis();
            }
        });
    }

    public static void resetCache(Entity e){
        statCache.remove(e.getUniqueId());
    }

    public static void updateStats(Entity e) {
        resetCache(e);
        if (e instanceof Player p) {
            MovementListener.resetAttributeStats(p);
            ProfileCache.resetCache(p);
            DigPacketInfo.resetMinerCache(e.getUniqueId());
        }
    }
}
