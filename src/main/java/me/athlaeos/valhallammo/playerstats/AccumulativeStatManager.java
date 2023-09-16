package me.athlaeos.valhallammo.playerstats;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.skills.skills.implementations.power.PowerProfile;
import me.athlaeos.valhallammo.playerstats.statsources.GlobalBuffSource;
import me.athlaeos.valhallammo.playerstats.statsources.ProfileStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

@SuppressWarnings("unused")
public class AccumulativeStatManager {
    private static final Map<String, StatCollector> sources = new HashMap<>();

    public static Map<String, StatCollector> getSources() {
        return sources;
    }

    public AccumulativeStatManager(){
        register("GLOBAL_EXP_GAIN", new ProfileStatSource(PowerProfile.class, "allSkillEXPGain"), new GlobalBuffSource("percent_skill_exp_gain"));

        register("DAMAGE_RESISTANCE", new ProfileStatSource(PowerProfile.class, "damageResistance"), new GlobalBuffSource("damage_resistance"));
        register("EXPLOSION_RESISTANCE", new ProfileStatSource(PowerProfile.class, "explosionResistance"), new GlobalBuffSource("explosion_resistance"));
        register("FIRE_RESISTANCE", new ProfileStatSource(PowerProfile.class, "fireResistance"), new GlobalBuffSource("fire_resistance"));
        register("MAGIC_RESISTANCE", new ProfileStatSource(PowerProfile.class, "magicResistance"), new GlobalBuffSource("magic_resistance"));
        register("POISON_RESISTANCE", new ProfileStatSource(PowerProfile.class, "poisonResistance"), new GlobalBuffSource("poison_resistance"));
        register("PROJECTILE_RESISTANCE", new ProfileStatSource(PowerProfile.class, "projectileResistance"), new GlobalBuffSource("projectile_resistance"));
        register("MELEE_RESISTANCE", new ProfileStatSource(PowerProfile.class, "meleeResistance"), new GlobalBuffSource("melee_resistance"));
        register("FALLING_RESISTANCE", new ProfileStatSource(PowerProfile.class, "fallDamageResistance"), new GlobalBuffSource("fall_damage_resistance"));
        register("KNOCKBACK_RESISTANCE", new ProfileStatSource(PowerProfile.class, "knockbackResistanceBonus"), new GlobalBuffSource("knockback_resistance"));
        register("BLEED_RESISTANCE", new ProfileStatSource(PowerProfile.class, "bleedResistance"), new GlobalBuffSource("bleeding_resistance"));
        register("STUN_RESISTANCE", new ProfileStatSource(PowerProfile.class, "stunResistance"), new GlobalBuffSource("stun_resistance"));

        register("CRAFTING_TIME_REDUCTION", new ProfileStatSource(PowerProfile.class, "craftingTimeReduction"), new GlobalBuffSource("crafting_time_reduction"));

        registerOffensive("ARMOR_FLAT_IGNORED", new GlobalBuffSource("armor_penetration"));
        registerOffensive("ARMOR_FRACTION_IGNORED", new GlobalBuffSource("fraction_armor_penetration"));
        register("TOUGHNESS");
        register("LIGHT_ARMOR");
        registerOffensive("LIGHT_ARMOR_FLAT_IGNORED");
        registerOffensive("LIGHT_ARMOR_FRACTION_IGNORED");
        register("HEAVY_ARMOR");
        registerOffensive("HEAVY_ARMOR_FLAT_IGNORED");
        registerOffensive("HEAVY_ARMOR_FRACTION_IGNORED");
        // returns all armor that came from non-equipment sources, like attributes/potion effects
        register("NON_EQUIPMENT_ARMOR", new ProfileStatSource(PowerProfile.class, "armorBonus"), new GlobalBuffSource("armor_bonus"));

        register("ARMOR_MULTIPLIER_BONUS", new ProfileStatSource(PowerProfile.class, "armorMultiplierBonus"), new GlobalBuffSource("fraction_armor_bonus"));
        register("HEALTH_MULTIPLIER_BONUS", new ProfileStatSource(PowerProfile.class, "healthMultiplier"), new GlobalBuffSource("fraction_health_bonus"));
        register("TOUGHNESS_BONUS", new ProfileStatSource(PowerProfile.class, "toughnessBonus"), new GlobalBuffSource("toughness_bonus"));
        register("HUNGER_SAVE_CHANCE", new ProfileStatSource(PowerProfile.class, "hungerSaveChance"), new GlobalBuffSource("fraction_hunger_reduction"));
        register("ATTACK_REACH_BONUS", new ProfileStatSource(PowerProfile.class, "attackReachBonus"), new GlobalBuffSource("attack_reach_bonus"));
        registerOffensive("DAMAGE_DEALT", new ProfileStatSource(PowerProfile.class, "attackDamageMultiplier"), new GlobalBuffSource("fraction_damage_bonus"));
        registerOffensive("MELEE_DAMAGE_DEALT", new ProfileStatSource(PowerProfile.class, "meleeAttackDamageMultiplier"), new GlobalBuffSource("fraction_melee_damage_bonus"));
        registerOffensive("ATTACK_DAMAGE_BONUS", new ProfileStatSource(PowerProfile.class, "attackDamageBonus"), new GlobalBuffSource("attack_damage_bonus"));
        register("ATTACK_SPEED_BONUS", new ProfileStatSource(PowerProfile.class, "attackSpeedBonus"), new GlobalBuffSource("fraction_attack_speed_bonus"));
        register("DODGE_CHANCE", new ProfileStatSource(PowerProfile.class, "dodgeChance"), new GlobalBuffSource("dodge_chance"));
        register("MOVEMENT_SPEED_BONUS", new ProfileStatSource(PowerProfile.class, "movementSpeedBonus"), new GlobalBuffSource("movement_speed"));
        register("HEALTH_BONUS", new ProfileStatSource(PowerProfile.class, "healthBonus"), new GlobalBuffSource("health_bonus"));
        registerOffensive("KNOCKBACK_BONUS", new ProfileStatSource(PowerProfile.class, "attackKnockbackBonus"), new GlobalBuffSource("knockback_bonus"));
        register("COOLDOWN_REDUCTION", new ProfileStatSource(PowerProfile.class, "cooldownReduction"), new GlobalBuffSource("cooldown_reduction"));
        register("LUCK_BONUS", new ProfileStatSource(PowerProfile.class, "luckBonus"), new GlobalBuffSource("luck_bonus"));
        register("IMMUNITY_FRAME_BONUS", new ProfileStatSource(PowerProfile.class, "immunityFrameBonus"), new GlobalBuffSource("immunity_frame_bonus"));
        register("IMMUNITY_FRAME_MULTIPLIER", new ProfileStatSource(PowerProfile.class, "immunityFrameMultiplier"), new GlobalBuffSource("fraction_immunity_frame_bonus"));
        register("HEALING_BONUS", new ProfileStatSource(PowerProfile.class, "healthRegenerationBonus"), new GlobalBuffSource("healing_bonus"));
        register("REFLECT_CHANCE", new ProfileStatSource(PowerProfile.class, "reflectChance"), new GlobalBuffSource("reflect_chance"));
        register("REFLECT_FRACTION", new ProfileStatSource(PowerProfile.class, "reflectFraction"), new GlobalBuffSource("reflect_fraction"));
        registerOffensive("BLEED_CHANCE", new ProfileStatSource(PowerProfile.class, "bleedChance"), new GlobalBuffSource("bleed_chance"));
        registerOffensive("BLEED_DAMAGE", new ProfileStatSource(PowerProfile.class, "bleedDamage"), new GlobalBuffSource("bleed_damage"));
        registerOffensive("BLEED_DURATION", new ProfileStatSource(PowerProfile.class, "bleedDuration"), new GlobalBuffSource("bleed_duration"));
        register("PARRY_ENEMY_DEBUFF_DURATION");
        register("PARRY_DAMAGE_REDUCTION");
        register("PARRY_DEBUFF_DURATION");
        register("ENTITY_DROP_MULTIPLIER_BONUS", new ProfileStatSource(PowerProfile.class, "entityDropMultiplier"), new GlobalBuffSource("fraction_entity_drop_bonus"));
        register("DURABILITY_MULTIPLIER_BONUS", new ProfileStatSource(PowerProfile.class, "durabilityMultiplier"), new GlobalBuffSource("fraction_durability_bonus"));
        register("DISMOUNT_CHANCE");

//        register("SMITHING_QUALITY_GENERAL", new SmithingProfileQualitySource(null), new SmithingPotionQualitySingleUseSource(), new SmithingPotionQualitySource(), new ArbitraryEnchantmentAmplifierSource("SMITHING_QUALITY"), new GlobalBuffSource("smithing_quality"));
//        register("SMITHING_QUALITY_WOOD", new SmithingProfileQualitySource(MaterialClass.WOOD));
//        register("SMITHING_QUALITY_LEATHER", new SmithingProfileQualitySource(MaterialClass.LEATHER));
//        register("SMITHING_QUALITY_STONE", new SmithingProfileQualitySource(MaterialClass.STONE));
//        register("SMITHING_QUALITY_CHAINMAIL", new SmithingProfileQualitySource(MaterialClass.CHAINMAIL));
//        register("SMITHING_QUALITY_GOLD", new SmithingProfileQualitySource(MaterialClass.GOLD));
//        register("SMITHING_QUALITY_IRON", new SmithingProfileQualitySource(MaterialClass.IRON));
//        register("SMITHING_QUALITY_DIAMOND", new SmithingProfileQualitySource(MaterialClass.DIAMOND));
//        register("SMITHING_QUALITY_NETHERITE", new SmithingProfileQualitySource(MaterialClass.NETHERITE));
//        register("SMITHING_QUALITY_BOW", new SmithingProfileQualitySource(MaterialClass.BOW));
//        register("SMITHING_QUALITY_CROSSBOW", new SmithingProfileQualitySource(MaterialClass.CROSSBOW));
//        register("SMITHING_QUALITY_PRISMARINE", new SmithingProfileQualitySource(MaterialClass.PRISMARINE));
//        register("SMITHING_QUALITY_MEMBRANE", new SmithingProfileQualitySource(MaterialClass.MEMBRANE));
//        register("SMITHING_EXP_GAIN_GENERAL", new SmithingProfileEXPSource(null), new PermissionExpGainSource("SMITHING"), new GlobalBuffSource("smithing_experience"));
//        register("SMITHING_EXP_GAIN_WOOD", new SmithingProfileEXPSource(MaterialClass.WOOD));
//        register("SMITHING_EXP_GAIN_LEATHER", new SmithingProfileEXPSource(MaterialClass.LEATHER));
//        register("SMITHING_EXP_GAIN_STONE", new SmithingProfileEXPSource(MaterialClass.STONE));
//        register("SMITHING_EXP_GAIN_CHAINMAIL", new SmithingProfileEXPSource(MaterialClass.CHAINMAIL));
//        register("SMITHING_EXP_GAIN_GOLD", new SmithingProfileEXPSource(MaterialClass.GOLD));
//        register("SMITHING_EXP_GAIN_IRON", new SmithingProfileEXPSource(MaterialClass.IRON));
//        register("SMITHING_EXP_GAIN_DIAMOND", new SmithingProfileEXPSource(MaterialClass.DIAMOND));
//        register("SMITHING_EXP_GAIN_NETHERITE", new SmithingProfileEXPSource(MaterialClass.NETHERITE));
//        register("SMITHING_EXP_GAIN_BOW", new SmithingProfileEXPSource(MaterialClass.BOW));
//        register("SMITHING_EXP_GAIN_CROSSBOW", new SmithingProfileEXPSource(MaterialClass.CROSSBOW));
//        register("SMITHING_EXP_GAIN_PRISMARINE", new SmithingProfileEXPSource(MaterialClass.PRISMARINE));
//        register("SMITHING_EXP_GAIN_MEMBRANE", new SmithingProfileEXPSource(MaterialClass.MEMBRANE));
//
//        register("ALCHEMY_QUALITY_GENERAL", new AlchemyQualityPlayerSource(null), new ArbitraryEnchantmentAmplifierSource("ALCHEMY_QUALITY"), new AlchemyPotionQualitySingleUseSource(), new GlobalBuffSource("alchemy_quality"));
//        register("ALCHEMY_QUALITY_DEBUFF", new AlchemyQualityPlayerSource(PotionType.DEBUFF));
//        register("ALCHEMY_QUALITY_BUFF", new AlchemyQualityPlayerSource(PotionType.BUFF));
//        register("ALCHEMY_BREW_SPEED", new AlchemyProfileBrewSpeedSource(), new AlchemyPotionBrewSpeedSource(), new ArbitraryEnchantmentAmplifierSource("ALCHEMY_BREW_SPEED"), new GlobalBuffSource("alchemy_brewing_speed"));
//        register("ALCHEMY_INGREDIENT_SAVE", new AlchemyProfileIngredientSaveSource(), new AlchemyPotionIngredientSaveSource(), new ArbitraryEnchantmentAmplifierSource("ALCHEMY_INGREDIENT_SAVE"), new GlobalBuffSource("alchemy_ingredient_save"));
//        register("ALCHEMY_POTION_SAVE", new AlchemyProfilePotionSaveSource(), new AlchemyPotionPotionSaveSource(), new ArbitraryEnchantmentAmplifierSource("ALCHEMY_POTION_SAVE"), new GlobalBuffSource("alchemy_potion_save"));
//        register("ALCHEMY_POTION_VELOCITY", new AlchemyProfileThrowVelocitySource(), new AlchemyPotionThrowVelocitySource(), new ArbitraryEnchantmentAmplifierSource("ALCHEMY_THROW_VELOCITY"), new GlobalBuffSource("alchemy_potion_velocity"));
//        register("ALCHEMY_EXP_GAIN", new AlchemyProfileEXPSource(), new PermissionExpGainSource("ALCHEMY"), new GlobalBuffSource("alchemy_experience"));
//
//        register("ENCHANTING_QUALITY_GENERAL", new EnchantingProfileQualitySource(null), new EnchantingPotionQualitySource(), new EnchantingPotionQualitySingleUseSource(), new GlobalBuffSource("enchanting_quality"));
//        register("ENCHANTING_QUALITY_VANILLA", new EnchantingProfileQualitySource(EnchantmentType.VANILLA));
//        register("ENCHANTING_QUALITY_CUSTOM", new EnchantingProfileQualitySource(EnchantmentType.CUSTOM));
//        register("ENCHANTING_EXP_GAIN_GENERAL", new EnchantingProfileEXPSource(null), new PermissionExpGainSource("ENCHANTING"), new GlobalBuffSource("enchanting_experience"));
//        register("ENCHANTING_EXP_GAIN_VANILLA", new EnchantingProfileEXPSource(EnchantmentType.VANILLA));
//        register("ENCHANTING_EXP_GAIN_CUSTOM", new EnchantingProfileEXPSource(EnchantmentType.CUSTOM));
//        register("ENCHANTING_VANILLA_EXP_GAIN", new EnchantingProfileVanillaEXPGainSource(), new EnchantingPotionVanillaEXPGainSource(), new EnchantmentVanillaExpGainSource());
//        register("ENCHANTING_AMPLIFY_CHANCE", new EnchantingProfileAmplifyChanceSource(), new GlobalBuffSource("enchanting_amplify_chance"));
//        register("ENCHANTING_MAX_CUSTOM_ALLOWED", new EnchantingProfileMaxCustomAllowedSource(), new GlobalBuffSource("enchanting_max_custom_allowed"));
//        register("ENCHANTING_LAPIS_SAVE_CHANCE", new EnchantingProfileLapisSaveChanceSource(), new GlobalBuffSource("enchanting_lapis_save_chance"));
//        register("ENCHANTING_REFUND_CHANCE", new EnchantingProfileRefundChanceSource(), new GlobalBuffSource("enchanting_exp_refund_chance"));
//        register("ENCHANTING_REFUND_AMOUNT", new EnchantingProfileRefundFractionSource(), new GlobalBuffSource("enchanting_exp_refund_amount"));
//        register("ENCHANTING_QUALITY_ANVIL", new EnchantingProfileAnvilQualitySource(), new EnchantingPotionAnvilQualitySource());
//
//        register("FARMING_BREEDING_AGE_REDUCTION", new FarmingProfileBabyAnimalAgeMultiplierSource(), new GlobalBuffSource("farming_animal_age_reduction"));
//        register("FARMING_DAMAGE_ANIMAL_MULTIPLIER", true, new FarmingProfileAnimalDamageMultiplierSource());
//        register("FARMING_ANIMAL_DROP_MULTIPLIER", new GlobalBuffSource("farming_animal_drop_multiplier"));
//        register("FARMING_ANIMAL_RARE_DROP_MULTIPLIER", new FarmingProfileAnimalRareDropChanceMultiplierSource(), new GlobalBuffSource("farming_animal_rare_drop_multiplier"));
//        register("FARMING_BREEDING_VANILLA_EXP_MULTIPLIER", new FarmingProfileBreedingVanillaEXPMultiplierSource());
//        register("FARMING_DROP_MULTIPLIER", new FarmingProfileDropMultiplierSource(), new ArbitraryEnchantmentAmplifierSource("FARMING_EXTRA_DROPS"), new PotionExtraDropsSource(), new GlobalBuffSource("farming_drop_multiplier"));
//        register("FARMING_VANILLA_EXP_REWARD", new FarmingProfileFarmingVanillaEXPRewardSource(), new GlobalBuffSource("farming_vanilla_exp"));
//        register("FARMING_FISHING_REWARD_TIER", new FarmingProfileFishingRewardTierSource(), new FarmingLOTSFishingRewardTierSource(), new FarmingLuckFishingRewardTierSource(), new ArbitraryEnchantmentAmplifierSource("FARMING_FISHING_TIER"), new GlobalBuffSource("farming_fishing_tier"));
//        register("FARMING_FISHING_TIME_MULTIPLIER", new FarmingProfileFishingTimeMultiplierSource(), new GlobalBuffSource("farming_fishing_speed_multiplier"));
//        register("FARMING_FISHING_VANILLA_EXP_MULTIPLIER", new FarmingProfileFishingVanillaEXPMultiplierSource(), new GlobalBuffSource("farming_fishing_vanilla_exp_multiplier"));
//        register("FARMING_HONEY_SAVE_CHANCE", new FarmingProfileHiveHoneySaveChanceSource(), new GlobalBuffSource("farming_honey_save_chance"));
//        register("FARMING_INSTANT_GROWTH_RATE", new FarmingProfileInstantGrowthRateSource(), new GlobalBuffSource("farming_instant_growth_rate"));
//        register("FARMING_RARE_DROP_CHANCE_MULTIPLIER", new FarmingProfileRareDropChanceMultiplierSource(), new FarmingPotionRareDropsSource(), new ArbitraryEnchantmentAmplifierSource("FARMING_RARE_DROPS"), new GlobalBuffSource("farming_rare_drop_multiplier"));
//        register("FARMING_HUNGER_MULTIPLIER_FISH", new FarmingProfileFoodMultiplierSource(FarmingProfileFoodMultiplierSource.FoodType.FISH));
//        register("FARMING_HUNGER_MULTIPLIER_MEAT", new FarmingProfileFoodMultiplierSource(FarmingProfileFoodMultiplierSource.FoodType.MEAT));
//        register("FARMING_HUNGER_MULTIPLIER_VEGETARIAN", new FarmingProfileFoodMultiplierSource(FarmingProfileFoodMultiplierSource.FoodType.VEG));
//        register("FARMING_HUNGER_MULTIPLIER_GARBAGE", new FarmingProfileFoodMultiplierSource(FarmingProfileFoodMultiplierSource.FoodType.GARBAGE));
//        register("FARMING_HUNGER_MULTIPLIER_MAGICAL", new FarmingProfileFoodMultiplierSource(FarmingProfileFoodMultiplierSource.FoodType.MAGICAL));
//        register("FARMING_EXP_GAIN_GENERAL", new FarmingProfileGeneralEXPSource(), new PermissionExpGainSource("FARMING"), new GlobalBuffSource("farming_experience"));
//        register("FARMING_EXP_GAIN_BREEDING", new FarmingProfileBreedingEXPSource());
//        register("FARMING_EXP_GAIN_FARMING", new FarmingProfileFarmingEXPSource());
//        register("FARMING_EXP_GAIN_FISHING", new FarmingProfileFishingEXPSource());
//
//        register("MINING_BLAST_DROP_MULTIPLIER", new MiningProfileBlastDropMultiplierSource(), new GlobalBuffSource("blast_mining_drop_multiplier"));
//        register("MINING_BLAST_EXPLOSION_DAMAGE_MULTIPLIER", new MiningProfileBlastExplosionDamageMultiplierSource(), new GlobalBuffSource("blast_mining_damage_taken_multiplier"));
//        register("MINING_BLAST_RADIUS_MULTIPLIER", new ArbitraryEnchantmentAmplifierSource("EXPLOSION_POWER"), new MiningProfileBlastRadiusMultiplierSource(), new GlobalBuffSource("blast_mining_radius_multiplier"));
//        register("MINING_BLAST_RARE_DROP_CHANCE_MULTIPLIER", new MiningProfileBlastRareDropChanceMultiplierSource(), new GlobalBuffSource("blast_mining_rare_drop_multiplier"));
//        register("MINING_VANILLA_EXP_REWARD", new MiningProfileBlockExperienceRateSource(), new GlobalBuffSource("mining_vanilla_exp_reward"));
//        register("MINING_MINING_DROP_MULTIPLIER", new MiningProfileMiningDropMultiplierSource(), new GlobalBuffSource("mining_drop_multiplier"));
//        register("MINING_QUICK_MINE_DRAIN_RATE", new MiningProfileQuickMineDrainRateSource(), new GlobalBuffSource("mining_quick_mine_drain_rate"));
//        register("MINING_ORE_EXPERIENCE_MULTIPLIER", new MiningProfileMiningOreExperienceMultiplierSource(), new GlobalBuffSource("mining_ore_experience_multiplier"));
//        register("MINING_MINING_RARE_DROP_CHANCE_MULTIPLIER", new MiningProfileMiningRareDropChanceMultiplierSource(), new GlobalBuffSource("mining_rare_drop_multiplier"));
//        register("MINING_EXP_GAIN_GENERAL", new MiningProfileGeneralEXPSource(), new GlobalBuffSource("mining_experience"));
//        register("MINING_EXP_GAIN_MINING", new MiningProfileMiningEXPSource());
//        register("MINING_EXP_GAIN_BLAST", new MiningProfileBlastEXPSource());
//
//        register("LANDSCAPING_DIGGING_DROP_MULTIPLIER", new ArbitraryEnchantmentAmplifierSource("DIGGING_EXTRA_DROPS"), new LandscapingProfileDiggingDropMultiplierSource(), new GlobalBuffSource("landscaping_digging_drop_multiplier"));
//        register("LANDSCAPING_DIGGING_RARE_DROP_MULTIPLIER", new ArbitraryEnchantmentAmplifierSource("DIGGING_RARE_DROPS"), new LandscapingProfileDiggingRareDropChanceMultiplierSource(), new GlobalBuffSource("landscaping_digging_rare_drop_multiplier"));
//        register("LANDSCAPING_WOODCUTTING_DROP_MULTIPLIER", new ArbitraryEnchantmentAmplifierSource("WOODCUTTING_EXTRA_DROPS"), new LandscapingProfileWoodcuttingDropMultiplierSource(), new GlobalBuffSource("landscaping_woodcutting_drop_multiplier"));
//        register("LANDSCAPING_WOODCUTTING_RARE_DROP_MULTIPLIER", new ArbitraryEnchantmentAmplifierSource("WOODCUTTING_RARE_DROPS"), new LandscapingProfileWoodcuttingRareDropChanceMultiplierSource(), new GlobalBuffSource("landscaping_woodcutting_rare_drop_multiplier"));
//        register("LANDSCAPING_WOODSTRIPPING_RARE_DROP_MULTIPLIER", new LandscapingProfileWoodstrippingRareDropChanceMultiplierSource(), new GlobalBuffSource("landscaping_woodstripping_rare_drop_multiplier"));
//        register("LANDSCAPING_INSTANT_GROWTH_RATE", new LandscapingProfileInstantGrowthRateSource(), new GlobalBuffSource("landscaping_instant_growth_rate"));
//        register("LANDSCAPING_PLACEMENT_REACH_BONUS", new LandscapingProfilePlaceReachBonusSource(), new GlobalBuffSource("landscaping_placement_reach"));
//        register("LANDSCAPING_EXP_GAIN_GENERAL", new LandscapingProfileGeneralEXPSource(), new GlobalBuffSource("landscaping_experience"));
//        register("LANDSCAPING_WOODCUTTING_VANILLA_EXP_REWARD", new LandscapingProfileWoodcuttingExperienceRateSource(), new GlobalBuffSource("landscaping_woodcutting_vanilla_exp_reward"));
//        register("LANDSCAPING_DIGGING_VANILLA_EXP_REWARD", new LandscapingProfileDiggingExperienceRateSource(), new GlobalBuffSource("landscaping_digging_vanilla_exp_reward"));
//        register("LANDSCAPING_EXP_GAIN_WOODCUTTING", new LandscapingProfileWoodcuttingEXPSource());
//        register("LANDSCAPING_EXP_GAIN_WOODSTRIPPING", new LandscapingProfileWoodstrippingEXPSource());
//        register("LANDSCAPING_EXP_GAIN_DIGGING", new LandscapingProfileDiggingEXPSource());
//
//        register("ARCHERY_DAMAGE", true, new OverleveledEquipmentArcheryDamagePenaltySource(), new ArbitraryOffensiveEnchantmentAmplifierSource("ARCHERY_DAMAGE"), new ArbitraryOffensivePotionAmplifierSource("ARCHERY_DAMAGE", false));
//        register("ARCHERY_BOW_DAMAGE_MULTIPLIER", true, new ArcheryProfileBowDamageMultiplierSource());
//        register("ARCHERY_CROSSBOW_DAMAGE_MULTIPLIER", true, new ArcheryProfileCrossBowDamageMultiplierSource());
//        register("ARCHERY_AMMO_SAVE_CHANCE", true, new ArbitraryEnchantmentAmplifierSource("ARCHERY_AMMO_SAVE"), new ArbitraryPotionAmplifierSource("ARCHERY_AMMO_SAVE", false), new ArcheryProfileAmmoSaveChanceSource());
//        register("ARCHERY_BOW_CRIT_CHANCE", true, new ArcheryProfileBowCritChanceSource(), new ArbitraryAttributeOnAttackSource("CUSTOM_CRIT_CHANCE", false), new ArbitraryEnchantmentAmplifierOnAttackSource("CRIT_CHANCE"));
//        register("ARCHERY_CROSSBOW_CRIT_CHANCE", true, new ArcheryProfileCrossBowCritChanceSource(), new ArbitraryAttributeOnAttackSource("CUSTOM_CRIT_CHANCE", false), new ArbitraryEnchantmentAmplifierOnAttackSource("CRIT_CHANCE"));
//        register("ARCHERY_CRIT_DAMAGE_MULTIPLIER", true, new ArcheryProfileCritDamageMultiplierSource(), new ArbitraryAttributeOnAttackSource("CUSTOM_CRIT_DAMAGE", false), new ArbitraryEnchantmentAmplifierOnAttackSource("CRIT_DAMAGE"));
//        register("ARCHERY_CHARGED_SHOT_COOLDOWN", new ArcheryProfileChargeShotCooldownSource());
//        register("ARCHERY_CHARGED_SHOT_KNOCKBACK_BONUS", new ArcheryProfileChargeShotKnockbackBonusSource());
//        register("ARCHERY_CHARGED_SHOT_DAMAGE_MULTIPLIER", new ArcheryProfileChargeShotDamageMultiplierSource());
//        register("ARCHERY_CHARGED_SHOT_PIERCING_BONUS", new ArcheryProfileChargeShotPiercingBonusSource());
//        register("ARCHERY_CHARGED_SHOT_VELOCITY_BONUS", new ArcheryProfileChargeShotVelocityBonusSource());
//        register("ARCHERY_CHARGED_SHOT_CHARGES", new ArcheryProfileChargeShotChargesSource());
//        register("ARCHERY_STUN_CHANCE", true, new ArcheryProfileStunChanceSource());
//        register("ARCHERY_STUN_DURATION", true, new ArcheryProfileStunDurationSource());
//        register("ARCHERY_INFINITY_DAMAGE_MULTIPLIER", true, new ArcheryProfileInfinityDamageMultiplierSource());
//        register("ARCHERY_INACCURACY", new ArbitraryEnchantmentAmplifierSource("ARCHERY_ACCURACY", true), new ArbitraryPotionAmplifierSource("ARCHERY_ACCURACY", true), new ArcheryProfileInaccuracySource());
//        register("ARCHERY_DISTANCE_DAMAGE_MULTIPLIER_BASE", true, new ArcheryProfileDistanceDamageBaseSource());
//        register("ARCHERY_DISTANCE_DAMAGE_MULTIPLIER", true, new ArcheryProfileDistanceDamageMultiplierSource());
//        register("ARCHERY_EXP_GAIN_BOW", new ArcheryProfileBowEXPSource());
//        register("ARCHERY_EXP_GAIN_CROSSBOW", new ArcheryProfileCrossBowEXPSource());
//        register("ARCHERY_EXP_GAIN_GENERAL", new ArcheryProfileGeneralEXPSource());
//
//        register("LIGHT_ARMOR_MULTIPLIER", new LightArmorProfileArmorValueMultiplierSource(), new LightArmorProfileFullArmorArmorValueBonusSource(), new ArbitraryPotionAmplifierSource("LIGHT_ARMOR_FRACTION_BONUS", false));
//        register("LIGHT_ARMOR_EXP_GAIN", new LightArmorEXPSource());
//
//        register("HEAVY_ARMOR_MULTIPLIER", new HeavyArmorProfileArmorValueMultiplierSource(), new HeavyArmorProfileFullArmorArmorValueBonusSource(), new ArbitraryPotionAmplifierSource("HEAVY_ARMOR_FRACTION_BONUS", false));
//        register("HEAVY_ARMOR_EXP_GAIN", new HeavyArmorEXPSource());
//
//        register("LIGHT_WEAPONS_CRIT_CHANCE", true, new LightWeaponsCritChanceSource(), new ArbitraryAttributeOnAttackSource("CUSTOM_CRIT_CHANCE", false), new ArbitraryEnchantmentAmplifierOnAttackSource("CRIT_CHANCE"));
//        register("LIGHT_WEAPONS_CRIT_DAMAGE", true, new LightWeaponsCritDamageMultiplierSource(), new ArbitraryAttributeOnAttackSource("CUSTOM_CRIT_DAMAGE", false), new ArbitraryEnchantmentAmplifierOnAttackSource("CRIT_DAMAGE"));
//        register("LIGHT_WEAPONS_DAMAGE_MULTIPLIER", true, new LightWeaponsProfileDamageMultiplierSource(), new LightWeaponsProfileHeavyArmorDamageBonusSource(), new LightWeaponsProfileLightArmorDamageBonusSource(), new ArbitraryPotionAmplifierSource("WEAPONS_DAMAGE", false));
//        register("LIGHT_WEAPONS_EXP_GAIN", new LightWeaponsEXPSource());
//        register("LIGHT_WEAPONS_STUN_CHANCE", true, new LightWeaponsProfileStunChanceSource(), new ArbitraryAttributeOnAttackSource("CUSTOM_STUN_CHANCE", false), new ArbitraryEnchantmentAmplifierOnAttackSource("STUN_CHANCE"));
//        register("LIGHT_WEAPONS_RARE_DROP_MULTIPLIER", new LightWeaponsProfileRareDropMultiplierSource());
//
//        register("HEAVY_WEAPONS_CRIT_CHANCE", true, new HeavyWeaponsCritChanceSource(), new ArbitraryAttributeOnAttackSource("CUSTOM_CRIT_CHANCE", false), new ArbitraryEnchantmentAmplifierOnAttackSource("CRIT_CHANCE"));
//        register("HEAVY_WEAPONS_CRIT_DAMAGE", true, new HeavyWeaponsCritDamageMultiplierSource(), new ArbitraryAttributeOnAttackSource("CUSTOM_CRIT_DAMAGE", false), new ArbitraryEnchantmentAmplifierOnAttackSource("CRIT_DAMAGE"));
//        register("HEAVY_WEAPONS_DAMAGE_MULTIPLIER", true, new HeavyWeaponsProfileDamageMultiplierSource(), new HeavyWeaponsProfileHeavyArmorDamageBonusSource(), new HeavyWeaponsProfileLightArmorDamageBonusSource(), new ArbitraryPotionAmplifierSource("WEAPONS_DAMAGE", false));
//        register("HEAVY_WEAPONS_EXP_GAIN", new HeavyWeaponsEXPSource());
//        register("HEAVY_WEAPONS_STUN_CHANCE", true, new HeavyWeaponsProfileStunChanceSource(), new ArbitraryAttributeOnAttackSource("CUSTOM_STUN_CHANCE", false), new ArbitraryEnchantmentAmplifierOnAttackSource("STUN_CHANCE"));
//        register("HEAVY_WEAPONS_RARE_DROP_MULTIPLIER", new HeavyWeaponsProfileRareDropMultiplierSource());
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
        existingSource.getStatSources().addAll(Arrays.asList(s));
        register(stat, existingSource);
    }

    public static void registerOffensive(String stat, AccumulativeStatSource... s){
        StatCollector existingSource = sources.get(stat);
        if (existingSource == null) existingSource = new StatCollectorBuilder().addSources(s).setAttackerPossessive().build();
        existingSource.getStatSources().addAll(Arrays.asList(s));
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
     * given player's total skill level in general smithing, which would be the accumulation of actual skill, potion
     * effects, global boosters, etc.
     * This method should only be used in situations only involving a single entity (such as a player crafting, taking damage
     * from non-entity related sources, or walking)
     * @param stat the stat to gather its total from
     * @param p the player to gather their stats from
     * @param use if true, it will be assumed the stat is actually being used in practice rather than being a visual
     *            for show. Example: if a player were to craft an item and had some potion effect to boost their
     *            crafting quality, use would be true. If the player were to only look at the item in the crafting menu,
     *            so not actually having crafted it just yet, use would be false.
     * @return the combined stat number
     */
    public static double getStats(String stat, Entity p, boolean use) {
        Collection<AccumulativeStatSource> existingSources = getStatCollector(stat).getStatSources();
        double value = 0;
        for (AccumulativeStatSource s : existingSources){
            value += s.fetch(p, use);
        }
        return (double) Math.round(value * 1000000d) / 1000000d; // round to 6 decimals
    }

    /**
     * Collects all the stats of the given stat name. For example, if 'LIGHT_GENERIC_ARMOR' is given, it will return the<br>
     * given entity's total armor points influenced by the second entity given, which would be the accumulation of<br>
     * actual skill, potion effects, global boosters, etc.<br>
     * This method should be used in situations involving two entities, where the FIRST entity is the "main" entity in<br>
     * question, and the SECOND entity the... well.. other entity.<br>
     *<br>
     * It's a bit confusing, but for example in the context of getting an entity's total armor value when getting<br>
     * damaged the FIRST entity is the entity being damaged and the SECOND entity a possible entity attacking,<br>
     * so things like armor penetration may apply.<br>
     *<br>
     * For an entity attacking another entity though where you want to gather the CRIT CHANCE of the attacking entity,<br>
     * this attacking entity is the main entity and the damaged entity is the other entity.<br>
     *<br>
     * As a rule of thumb: with OFFENSIVE stats the ATTACKING entity is the main entity, with DEFENSIVE stats the<br>
     * DEFENDING entity is the main entity.<br>
     * Exceptions to this rule are stats where a second entity is not relevant such as ARCHERY_INACCURACY and so<br>
     * this method should not be used to obtain them to begin with.<br>
     *<br>
     * If the second entity is null, getStats(String, Entity, boolean) is being called instead.<br>
     * If a stat source associated with the given stat is not an EvEAccumulativeStatSource, a regular AccumulativeStatSource<br>
     * is being used instead where the second entity is not involved.
     * @param stat the stat to gather its total from
     * @param e1 the entity to gether their stats from
     * @param e2 the second entity involved in stat accumulation
     * @param use if true, it will be assumed the stat is actually being used in practice rather than being a visual
     *            for show. Example: if a player were to craft an item and had some potion effect to boost their
     *            crafting quality, use would be true. If the player were to only look at the item in the crafting menu,
     *            so not actually having crafted it just yet, use would be false.
     * @return the collective stat number
     */
    public static double getRelationalStats(String stat, Entity e1, Entity e2, boolean use) {
        if (e2 == null) return getStats(stat, e1, use);
        Collection<AccumulativeStatSource> existingSources = getStatCollector(stat).getStatSources();
        double value = 0;
        for (AccumulativeStatSource s : existingSources){
            if (s instanceof EvEAccumulativeStatSource os) value += os.fetch(e1, e2, use);
            else value += s.fetch(e1, use);
        }
        return (double) Math.round(value * 1000000d) / 1000000d; // round to 6 decimals
    }

    public static double getCachedStats(String stat, Entity p, long refreshAfter, boolean use){
        if (isStatCached(p, stat)){
            return getCachedStatIgnoringExpiration(p, stat);
        } else {
            double statValue = getStats(stat, p, use);
            cacheStat(p, stat, statValue, refreshAfter);
            return statValue;
        }
    }

    public static double getCachedRelationalStats(String stat, Entity p, Entity e, long refreshAfter, boolean use){
        if (isStatCached(p, stat)){
            return getCachedStatIgnoringExpiration(p, stat);
        } else {
            double statValue = getRelationalStats(stat, p, e, use);
            cacheStat(p, stat, statValue, refreshAfter);
            return statValue;
        }
    }

    public static double getCachedAttackerRelationalStats(String stat, Entity p, Entity e, long refreshAfter, boolean use){
        if (isStatCached(e, stat)){
            return getCachedStatIgnoringExpiration(e, stat);
        } else {
            double statValue = getRelationalStats(stat, p, e, use);
            cacheStat(e, stat, statValue, refreshAfter);
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
        if (lastMapCleanup + 120000 < System.currentTimeMillis()){
            // cleaning up map every 2 minutes
            Map<UUID, Map<String, Map.Entry<Long, Double>>> clone = new HashMap<>(statCache);
            for (UUID uuid : clone.keySet()){
                Entity entity = ValhallaMMO.getInstance().getServer().getEntity(uuid);
                if (entity != null && !entity.isValid()) statCache.remove(entity.getUniqueId()); // remove invalid entities from the map
            }
            lastMapCleanup = System.currentTimeMillis();
        }
        if (statCache.containsKey(e.getUniqueId())){
            if (statCache.getOrDefault(e.getUniqueId(), new HashMap<>()).get(stat) != null){
                return statCache.get(e.getUniqueId()).get(stat).getKey() > System.currentTimeMillis();
            }
        }
        return false;
    }

    public static void resetCache(Entity e){
        statCache.remove(e.getUniqueId());
    }

    public static void updateStats(Player e) {
        ProfileCache.resetCache(e);
        resetCache(e);
    }
}
