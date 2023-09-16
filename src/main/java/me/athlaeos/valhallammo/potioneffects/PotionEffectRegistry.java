package me.athlaeos.valhallammo.potioneffects;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.item.attributes.AttributeWrapper;
import me.athlaeos.valhallammo.potioneffects.implementations.ChocolateMilk;
import me.athlaeos.valhallammo.potioneffects.implementations.Milk;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.event.EntityCustomPotionEffectEvent;
import me.athlaeos.valhallammo.event.ValhallaEntityStunEvent;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.potioneffects.implementations.CustomWrapper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PotionEffectRegistry {
    private static final NamespacedKey POTION_EFFECTS = new NamespacedKey(ValhallaMMO.getInstance(), "potion_effects");
    private static final NamespacedKey ACTUAL_STORED_EFFECTS = new NamespacedKey(ValhallaMMO.getInstance(), "stored_effects");
    private static final NamespacedKey DEFAULT_STORED_EFFECTS = new NamespacedKey(ValhallaMMO.getInstance(), "default_stored_effects");
    private static final Map<String, PotionEffectWrapper> registeredEffects = new HashMap<>();

    private static final Collection<PotionEffectWrapper> stunEffects = new HashSet<>();
    private static int stunImmunityDuration = 0;

    public static void loadDefaults() {
        registerNewEffect(new CustomWrapper("SPEED", (i) -> true, "\uED00", StatFormat.ROMAN).addModifier(Material.SUGAR, 1, 5));
        registerNewEffect(new CustomWrapper("SLOW", (i) -> false, "\uED01", StatFormat.ROMAN).addModifier(Material.ICE, 1, 5));
        registerNewEffect(new CustomWrapper("FAST_DIGGING", (i) -> true, "\uED02", StatFormat.ROMAN).addModifier(Material.GOLDEN_PICKAXE, 1, 5));
        registerNewEffect(new CustomWrapper("SLOW_DIGGING", (i) -> false, "\uED03", StatFormat.ROMAN).addModifier(Material.SPONGE, 1, 5));
        registerNewEffect(new CustomWrapper("INCREASE_DAMAGE", (i) -> true, "\uED04", StatFormat.ROMAN).addModifier(Material.BLAZE_POWDER, 1, 5));
        registerNewEffect(new CustomWrapper("HEAL", (i) -> true, "\uED05", StatFormat.ROMAN).addModifier(Material.GLISTERING_MELON_SLICE, 1, 5));
        registerNewEffect(new CustomWrapper("HARM", (i) -> false, "\uED06", StatFormat.ROMAN).addModifier(Material.DRAGON_BREATH, 1, 5));
        registerNewEffect(new CustomWrapper("JUMP", (i) -> true, "\uED07", StatFormat.ROMAN).addModifier(Material.SLIME_BLOCK, 1, 5));
        registerNewEffect(new CustomWrapper("CONFUSION", (i) -> false, "\uED08", StatFormat.ROMAN).addModifier(Material.ANVIL, 1, 5));
        registerNewEffect(new CustomWrapper("REGENERATION", (i) -> true, "\uED09", StatFormat.ROMAN).addModifier(Material.GHAST_TEAR, 1, 5));
        registerNewEffect(new CustomWrapper("DAMAGE_RESISTANCE", (i) -> true, "\uED0A", StatFormat.ROMAN).addModifier(Material.ENCHANTED_GOLDEN_APPLE, 1, 5));
        registerNewEffect(new CustomWrapper("FIRE_RESISTANCE", (i) -> true, "\uED0B", StatFormat.ROMAN).addModifier(Material.MAGMA_CREAM, 1, 5));
        registerNewEffect(new CustomWrapper("WATER_BREATHING", (i) -> true, "\uED0C", StatFormat.ROMAN).addModifier(Material.PUFFERFISH, 1, 5));
        registerNewEffect(new CustomWrapper("INVISIBILITY", (i) -> true, "\uED0D", StatFormat.ROMAN).addModifier(Material.GLASS, 1, 5));
        registerNewEffect(new CustomWrapper("BLINDNESS", (i) -> false, "\uED0E", StatFormat.ROMAN).addModifier(Material.INK_SAC, 1, 5));
        registerNewEffect(new CustomWrapper("DARKNESS", (i) -> false, "\uED0F", StatFormat.ROMAN).addModifier(Material.BLACK_STAINED_GLASS, 1, 5));
        registerNewEffect(new CustomWrapper("NIGHT_VISION", (i) -> true, "\uED10", StatFormat.ROMAN).addModifier(Material.GOLDEN_CARROT, 1, 5));
        registerNewEffect(new CustomWrapper("HUNGER", (i) -> false, "\uED11", StatFormat.ROMAN).addModifier(Material.ROTTEN_FLESH, 1, 5));
        registerNewEffect(new CustomWrapper("WEAKNESS", (i) -> false, "\uED12", StatFormat.ROMAN).addModifier(Material.WOODEN_SWORD, 1, 5));
        registerNewEffect(new CustomWrapper("POISON", (i) -> false, "\uED13", StatFormat.ROMAN).addModifier(Material.SPIDER_EYE, 1, 5));
        registerNewEffect(new CustomWrapper("WITHER", (i) -> false, "\uED14", StatFormat.ROMAN).addModifier(Material.WITHER_SKELETON_SKULL, 1, 5));
        registerNewEffect(new CustomWrapper("HEALTH_BOOST", (i) -> true, "\uED15", StatFormat.ROMAN).addModifier(Material.SWEET_BERRIES, 1, 5));
        registerNewEffect(new CustomWrapper("ABSORPTION", (i) -> true, "\uED16", StatFormat.ROMAN).addModifier(Material.GOLDEN_APPLE, 1, 5));
        registerNewEffect(new CustomWrapper("SATURATION", (i) -> true, "\uED17", StatFormat.ROMAN).addModifier(Material.COOKED_BEEF, 1, 5));
        registerNewEffect(new CustomWrapper("GLOWING", (i) -> true, "\uED18", StatFormat.ROMAN).addModifier(Material.GLOWSTONE_DUST, 1, 5));
        registerNewEffect(new CustomWrapper("LEVITATION", (i) -> false, "\uED19", StatFormat.ROMAN).addModifier(Material.SHULKER_SHELL, 1, 5));
        registerNewEffect(new CustomWrapper("LUCK", (i) -> true, "\uED1A", StatFormat.ROMAN).addModifier(Material.RABBIT_FOOT, 1, 5));
        registerNewEffect(new CustomWrapper("UNLUCK", (i) -> false, "\uED1B", StatFormat.ROMAN).addModifier(Material.BONE, 1, 5));
        registerNewEffect(new CustomWrapper("SLOW_FALLING", (i) -> true, "\uED1C", StatFormat.ROMAN).addModifier(Material.PHANTOM_MEMBRANE, 1, 5));
        registerNewEffect(new CustomWrapper("CONDUIT_POWER", (i) -> true, "\uED1D", StatFormat.ROMAN).addModifier(Material.CONDUIT, 1, 5));
        registerNewEffect(new CustomWrapper("DOLPHINS_GRACE", (i) -> true, "\uED1E", StatFormat.ROMAN).addModifier(Material.HEART_OF_THE_SEA, 1, 5));
        registerNewEffect(new CustomWrapper("BAD_OMEN", (i) -> false, "\uED1F", StatFormat.ROMAN).addModifier(Material.IRON_AXE, 1, 5));
        registerNewEffect(new CustomWrapper("HERO_OF_THE_VILLAGE", (i) -> true, "\uED20", StatFormat.ROMAN).addModifier(Material.EMERALD, 1, 5));

        registerNewEffect(new CustomWrapper("BOW_STRENGTH", (i) -> i >= 0, "\uEE00", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BOW));
        registerNewEffect(new CustomWrapper("ARROW_DAMAGE", (i) -> i >= 0, "\uEE01", StatFormat.DIFFERENCE_FLOAT_P2).addModifier(Material.ARROW, 0.1, 1));
        registerNewEffect(new CustomWrapper("AMMO_CONSUMPTION", (i) -> i <= 0, "\uEE02", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SPECTRAL_ARROW));
        registerNewEffect(new CustomWrapper("ARROW_VELOCITY", (i) -> i >= 0, "\uEE03", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.CROSSBOW));
        registerNewEffect(new CustomWrapper("ARROW_ACCURACY", (i) -> i >= 0, "\uEE04", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.TARGET, 0.1, 1));
        registerNewEffect(new CustomWrapper("ARROW_PIERCING", (i) -> i >= 0, "\uEE05", StatFormat.DIFFERENCE_INT).addModifier(Material.TIPPED_ARROW, 1, 5));
        registerNewEffect(new CustomWrapper("KNOCKBACK", (i) -> i >= 0, "\uEE06", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SLIME_BLOCK));
        registerNewEffect(new CustomWrapper("STUN_CHANCE", (i) -> i >= 0, "\uEE07", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_BLOCK));
        registerNewEffect(new CustomWrapper("BLEED_CHANCE", (i) -> i >= 0, "\uEE08", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_AXE));
        registerNewEffect(new CustomWrapper("BLEED_DAMAGE", (i) -> i >= 0, "\uEE09", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.NETHERITE_AXE, 0.1, 1));
        registerNewEffect(new CustomWrapper("BLEED_DURATION", (i) -> i >= 0, "\uEE0A", StatFormat.DIFFERENCE_TIME_SECONDS_BASE_1000_P1).addModifier(Material.IRON_AXE, 20, 100));
        registerNewEffect(new CustomWrapper("CRIT_CHANCE", (i) -> i >= 0, "\uEE0B", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_SWORD));
        registerNewEffect(new CustomWrapper("CRIT_DAMAGE", (i) -> i >= 0, "\uEE0C", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_SWORD));
        registerNewEffect(new CustomWrapper("ARMOR_PENETRATION_FLAT", (i) -> i >= 0, "\uEE0D", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.LEATHER_CHESTPLATE, 0.1, 1));
        registerNewEffect(new CustomWrapper("LIGHT_ARMOR_PENETRATION_FLAT", (i) -> i >= 0, "\uEE0E", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.CHAINMAIL_CHESTPLATE, 0.1, 1));
        registerNewEffect(new CustomWrapper("HEAVY_ARMOR_PENETRATION_FLAT", (i) -> i >= 0, "\uEE0F", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.IRON_CHESTPLATE, 0.1, 1));
        registerNewEffect(new CustomWrapper("ARMOR_PENETRATION_FRACTION", (i) -> i >= 0, "\uEE10", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.LEATHER_CHESTPLATE));
        registerNewEffect(new CustomWrapper("LIGHT_ARMOR_PENETRATION_FRACTION", (i) -> i >= 0, "\uEE11", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.CHAINMAIL_CHESTPLATE));
        registerNewEffect(new CustomWrapper("HEAVY_ARMOR_PENETRATION_FRACTION", (i) -> i >= 0, "\uEE12", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_CHESTPLATE));
        registerNewEffect(new CustomWrapper("HEAVY_ARMOR_DAMAGE", (i) -> i >= 0, "\uEE13", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_HELMET));
        registerNewEffect(new CustomWrapper("LIGHT_ARMOR_DAMAGE", (i) -> i >= 0, "\uEE14", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.CHAINMAIL_HELMET));
        registerNewEffect(new CustomWrapper("IMMUNITY_BONUS_FRACTION", (i) -> i >= 0, "\uEE15", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ENCHANTED_GOLDEN_APPLE));
        registerNewEffect(new CustomWrapper("IMMUNITY_BONUS_FLAT", (i) -> i >= 0, "\uEE16", StatFormat.DIFFERENCE_INT).addModifier(Material.GOLDEN_APPLE, 1, 5));
        registerNewEffect(new CustomWrapper("IMMUNITY_REDUCTION", (i) -> i >= 0, "\uEE17", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.WITHER_ROSE));
        registerNewEffect(new CustomWrapper("DAMAGE_UNARMED", (i) -> i >= 0, "\uEE18", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BLAZE_POWDER));
        registerNewEffect(new CustomWrapper("DAMAGE_MELEE", (i) -> i >= 0, "\uEE19", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.DIAMOND_SWORD));
        registerNewEffect(new CustomWrapper("DAMAGE_RANGED", (i) -> i >= 0, "\uEE1A", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BOW));
        registerNewEffect(new CustomWrapper("DAMAGE_ALL", (i) -> i >= 0, "\uEE1B", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BLAZE_ROD));
        registerNewEffect(new CustomWrapper("ATTACK_REACH", (i) -> i >= 0, "\uEE1C", StatFormat.DIFFERENCE_FLOAT_P2).addModifier(Material.ENDER_PEARL, 0.1, 1));
        registerNewEffect(new CustomWrapper("VELOCITY_DAMAGE", (i) -> i >= 0, "\uEE1D", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.DIAMOND_HORSE_ARMOR));
        registerNewEffect(new CustomWrapper("DISMOUNT_CHANCE", (i) -> i >= 0, "\uEE1E", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SADDLE));
        registerNewEffect(new CustomWrapper("CUSTOM_DAMAGE_RESISTANCE", (i) -> i >= 0, "\uEE1F", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_INGOT));
        registerNewEffect(new CustomWrapper("EXPLOSION_RESISTANCE", (i) -> i >= 0, "\uEE20", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.TNT));
        registerNewEffect(new CustomWrapper("FALL_DAMAGE_RESISTANCE", (i) -> i >= 0, "\uEE21", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.LEATHER_BOOTS));
        registerNewEffect(new CustomWrapper("CUSTOM_FIRE_RESISTANCE", (i) -> i >= 0, "\uEE22", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.LAVA_BUCKET));
        registerNewEffect(new CustomWrapper("MAGIC_RESISTANCE", (i) -> i >= 0, "\uEE23", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.DRAGON_BREATH));
        registerNewEffect(new CustomWrapper("BLEED_RESISTANCE", (i) -> i >= 0, "\uEE24", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.REDSTONE));
        registerNewEffect(new CustomWrapper("STUN_RESISTANCE", (i) -> i >= 0, "\uEE25", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.NETHERITE_HELMET));
        registerNewEffect(new CustomWrapper("POISON_RESISTANCE", (i) -> i >= 0, "\uEE26", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SPIDER_EYE));
        registerNewEffect(new CustomWrapper("PROJECTILE_RESISTANCE", (i) -> i >= 0, "\uEE27", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ARROW));
        registerNewEffect(new CustomWrapper("MELEE_RESISTANCE", (i) -> i >= 0, "\uEE28", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SHIELD));
        registerNewEffect(new CustomWrapper("DODGE_CHANCE", (i) -> i >= 0, "\uEE29", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.LEATHER_LEGGINGS));
        registerNewEffect(new CustomWrapper("HEALING_BONUS", (i) -> i >= 0, "\uEE2A", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GLISTERING_MELON_SLICE));
        registerNewEffect(new CustomWrapper("FOOD_CONSUMPTION", (i) -> i <= 0, "\uEE2B", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.APPLE));
        registerNewEffect(new CustomWrapper("COOLDOWN_REDUCTION", (i) -> i >= 0, "\uEE2C", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.CLOCK));
        registerNewEffect(new CustomWrapper("EXPLOSION_POWER", (i) -> i >= 0, "\uEE2D", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.TNT_MINECART));
        registerNewEffect(new CustomWrapper("CRAFTING_SPEED", (i) -> i >= 0, "\uEE2E", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.CRAFTING_TABLE));
        registerNewEffect(new CustomWrapper("ALCHEMY_QUALITY", (i) -> i >= 0, "\uEE2F", StatFormat.DIFFERENCE_INT).addModifier(Material.BREWING_STAND, 1, 10));
        registerNewEffect(new CustomWrapper("ALCHEMY_QUALITY_FRACTION", (i) -> i >= 0, "\uEE30", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BREWING_STAND));
        registerNewEffect(new CustomWrapper("ENCHANTING_QUALITY", (i) -> i >= 0, "\uEE31", StatFormat.DIFFERENCE_INT).addModifier(Material.ENCHANTING_TABLE, 1, 10));
        registerNewEffect(new CustomWrapper("ENCHANTING_QUALITY_FRACTION", (i) -> i >= 0, "\uEE32", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ENCHANTING_TABLE));
        registerNewEffect(new CustomWrapper("ANVIL_QUALITY_FLAT", (i) -> i >= 0, "\uEE33", StatFormat.DIFFERENCE_INT).addModifier(Material.ANVIL, 1, 10));
        registerNewEffect(new CustomWrapper("ANVIL_QUALITY_FRACTION", (i) -> i >= 0, "\uEE34", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ANVIL));
        registerNewEffect(new CustomWrapper("BREWING_INGREDIENT_CONSUMPTION", (i) -> i <= 0, "\uEE35", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GLASS_BOTTLE));
        registerNewEffect(new CustomWrapper("BREWING_SPEED", (i) -> i >= 0, "\uEE36", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BLAZE_ROD));
        registerNewEffect(new CustomWrapper("POTION_CONSUMPTION", (i) -> i <= 0, "\uEE37", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SPLASH_POTION));
        registerNewEffect(new CustomWrapper("THROWING_VELOCITY", (i) -> i >= 0, "\uEE38", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SNOWBALL));
        registerNewEffect(new CustomWrapper("SMITHING_QUALITY", (i) -> i >= 0, "\uEE39", StatFormat.DIFFERENCE_INT).addModifier(Material.ANVIL, 1, 10));
        registerNewEffect(new CustomWrapper("SMITHING_QUALITY_FRACTION", (i) -> i >= 0, "\uEE3A", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ANVIL));
        registerNewEffect(new CustomWrapper("MINING_RARE_DROPS", (i) -> i >= 0, "\uEE3B", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_PICKAXE));
        registerNewEffect(new CustomWrapper("MINING_DROPS", (i) -> i >= 0, "\uEE3C", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_PICKAXE));
        registerNewEffect(new CustomWrapper("WOODCUTTING_RARE_DROPS", (i) -> i >= 0, "\uEE3D", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_AXE));
        registerNewEffect(new CustomWrapper("WOODCUTTING_DROPS", (i) -> i >= 0, "\uEE3E", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_AXE));
        registerNewEffect(new CustomWrapper("DIGGING_RARE_DROPS", (i) -> i >= 0, "\uEE3F", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_SHOVEL));
        registerNewEffect(new CustomWrapper("DIGGING_DROPS", (i) -> i >= 0, "\uEE40", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_SHOVEL));
        registerNewEffect(new CustomWrapper("FARMING_RARE_DROPS", (i) -> i >= 0, "\uEE41", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_HOE));
        registerNewEffect(new CustomWrapper("FARMING_DROPS", (i) -> i >= 0, "\uEE42", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_HOE));
        registerNewEffect(new CustomWrapper("FISHING_LUCK", (i) -> i >= 0, "\uEE43", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.FISHING_ROD, 0.1, 1));
        registerNewEffect(new CustomWrapper("SKILL_EXP_GAIN", (i) -> i >= 0, "\uEE44", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BOOK));
        registerNewEffect(new CustomWrapper("VANILLA_EXP_GAIN", (i) -> i >= 0, "\uEE45", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.EXPERIENCE_BOTTLE));
        registerNewEffect(new CustomWrapper("DAMAGE_TAKEN", (i) -> i <= 0, "\uEE46", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SKELETON_SKULL));
        registerNewEffect(new CustomWrapper("REFLECT_CHANCE", (i) -> i >= 0, "\uEE47", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SHIELD));
        registerNewEffect(new CustomWrapper("REFLECT_FRACTION", (i) -> i >= 0, "\uEE48", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SHIELD));

        registerNewEffect(new CustomWrapper("SMITHING_MASTERPIECE_FLAT", (i) -> i >= 0, false, true, "\uEE49", StatFormat.DIFFERENCE_INT).addModifier(Material.ANVIL, 1, 10));
        registerNewEffect(new CustomWrapper("SMITHING_MASTERPIECE_FRACTION", (i) -> i >= 0, false, true, "\uEE4A", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ANVIL));
        registerNewEffect(new CustomWrapper("ENCHANTING_MASTERPIECE_FLAT", (i) -> i >= 0, false, true, "\uEE4B", StatFormat.DIFFERENCE_INT).addModifier(Material.ENCHANTING_TABLE, 1, 10));
        registerNewEffect(new CustomWrapper("ENCHANTING_MASTERPIECE_FRACTION", (i) -> i >= 0, false, true, "\uEE4C", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ENCHANTING_TABLE));
        registerNewEffect(new CustomWrapper("ALCHEMY_MASTERPIECE_FLAT", (i) -> i >= 0, false, true, "\uEE4D", StatFormat.DIFFERENCE_INT).addModifier(Material.BREWING_STAND, 1, 10));
        registerNewEffect(new CustomWrapper("ALCHEMY_MASTERPIECE_FRACTION", (i) -> i >= 0, false, true, "\uEE4E", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BREWING_STAND));

        registerNewEffect(new CustomWrapper("CUSTOM_LUCK", (i) -> i >= 0, "\uEE4F", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.RABBIT_FOOT, 0.1, 1));
        registerNewEffect(new CustomWrapper("ARMOR_FLAT", (i) -> i >= 0, "\uEE50", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.IRON_CHESTPLATE, 0.1, 1));
        registerNewEffect(new CustomWrapper("ARMOR_FRACTION", (i) -> i >= 0, "\uEE51", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_CHESTPLATE, 0.1, 1));
        registerNewEffect(new CustomWrapper("ARMOR_TOUGHNESS_FLAT", (i) -> i >= 0, "\uEE52", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.DIAMOND_CHESTPLATE, 0.1, 1));
        registerNewEffect(new CustomWrapper("ARMOR_TOUGHNESS_FRACTION", (i) -> i >= 0, "\uEE53", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.DIAMOND_CHESTPLATE, 0.1, 1));
        registerNewEffect(new CustomWrapper("ATTACK_SPEED", (i) -> i >= 0, "\uEE55", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_SWORD, 0.1, 1));
        registerNewEffect(new CustomWrapper("KNOCKBACK_RESISTANCE", (i) -> i >= 0, "\uEE56", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.NETHERITE_CHESTPLATE));
        registerNewEffect(new CustomWrapper("MAX_HEALTH_FLAT", (i) -> i >= 0, "\uEE57", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.GOLDEN_APPLE, 0.1, 1));
        registerNewEffect(new CustomWrapper("MAX_HEALTH_FRACTION", (i) -> i >= 0, "\uEE58", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_APPLE, 0.1, 1));
        registerNewEffect(new CustomWrapper("MOVEMENT_SPEED", (i) -> i >= 0, "\uEE59", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SUGAR));

        registerNewEffect(new ChocolateMilk("CHOCOLATE_MILK", "\uEE5A").addModifier(Material.COCOA, 0, 0));
        registerNewEffect(new Milk("MILK", "\uEE5B").addModifier(Material.MILK_BUCKET, 0, 0));

        YamlConfiguration config = ConfigManager.getConfig("config.yml").get();
        ConfigurationSection stunEffectSection = config.getConfigurationSection("stun_effects");
        stunImmunityDuration = config.getInt("stun_immunity_duration", 5000);
        if (stunEffectSection != null){
            for (String effect : stunEffectSection.getKeys(false)){
                PotionEffectWrapper wrapper = getEffect(effect);
                stunEffects.add(wrapper);
            }
        }
    }

    /**
     * Stuns a target, which really means all effects that make up the "stun" effect are applied. The stunning entity may
     * be null if no entity is responsible for stunning the target. The stunned entity will be granted stun immunity
     * after being stunned unless "force" is true
     * @param entity the entity to stun
     * @param causedBy the entity that stunned them.
     * @param duration the duration (in game ticks) to stun the target
     * @param force true if the entity should be stunned regardless of immunity, false otherwise
     */
    public static void stunTarget(LivingEntity entity, Entity causedBy, int duration, boolean force){
        double durationMultiplier = force ? 1 : Math.max(0, 1 - AccumulativeStatManager.getRelationalStats("STUN_RESISTANCE", entity, causedBy, true));
        int newDuration = (int) Math.round(duration * durationMultiplier);
        ValhallaEntityStunEvent event = new ValhallaEntityStunEvent(entity, causedBy, newDuration);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()){
            if (!(event.getEntity() instanceof LivingEntity) || (!force && Timer.isCooldownPassed(entity.getUniqueId(), "stun_immunity"))) return;
            for (PotionEffectWrapper e : stunEffects){
                if (e.isVanilla()){
                    ((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(e.getVanillaEffect(), (int) e.getDuration(), (int) e.getAmplifier(), true, false));
                } else {
                    addEffect((LivingEntity) event.getEntity(), new CustomPotionEffect(e, newDuration, e.getAmplifier()), false, EntityPotionEffectEvent.Cause.ATTACK);
                }
            }
            EntityCache.resetPotionEffects((LivingEntity) event.getEntity()); // adding/removing an effect as a result of this method should reset the entity's potion effect cache
            Timer.setCooldown(entity.getUniqueId(), stunImmunityDuration * 50, "stun_immunity");
        }
    }

    /**
     * Checks if the entity is stunned or not. An entity is considered stunned if they have all of the "stun" effects
     * @param entity the entity
     * @return true if they're considered stunned, false otherwise
     */
    public static boolean isStunned(LivingEntity entity){
        Map<String, CustomPotionEffect> activeEffects = getActiveEffects(entity);
        return stunEffects.stream().map(PotionEffectWrapper::getEffect).allMatch(activeEffects::containsKey);
    }

    public static void reload(){
        stunEffects.clear();
        registeredEffects.clear();
        loadDefaults();
    }

    public static void setDefaultStoredEffects(ItemMeta meta, Map<String, PotionEffectWrapper> effects){
        if (meta == null) return;
        if (effects == null || effects.isEmpty()){
            clean(meta);
        } else {
            meta.getPersistentDataContainer().set(DEFAULT_STORED_EFFECTS, PersistentDataType.STRING,
                    effects.values().stream().map(s -> s.getEffect() + ":" + s.getAmplifier() + ":" + s.getDuration() + (s.getCharges() >= 0 ? ":" + s.getCharges() : ""))
                            .collect(Collectors.joining(";")));
        }
    }

    /**
     * Fetches all stored effects from an item
     * @param meta the item to fetch its effects from
     * @param def if the item's default (true) effects should be fetched or not (false)
     * @return the item's effects
     */
    public static Map<String, PotionEffectWrapper> getStoredEffects(ItemMeta meta, boolean def){
        return parseRawData(getRawData(meta, def));
    }

    public static String getRawData(ItemMeta meta, boolean def){
        if (meta == null) return null;
        return ItemUtils.getPDCString(def ? DEFAULT_STORED_EFFECTS : ACTUAL_STORED_EFFECTS, meta, null);
    }

    public static Map<String, PotionEffectWrapper> parseRawData(String data){
        Map<String, PotionEffectWrapper> effects = new HashMap<>();
        if (!StringUtils.isEmpty(data)){
            for (String effectDetails : data.split(";")){
                String[] args = effectDetails.split(":");
                if (args.length < 3) continue;
                try {
                    String effect = args[0];
                    double amplifier = Double.parseDouble(args[1]);
                    long duration =  Long.parseLong(args[2]);
                    int charges = args.length > 3 ? Integer.parseInt(args[3]) : -1;
                    if (charges == 0) continue;

                    PotionEffectWrapper wrapper = getEffect(effect);
                    wrapper.setAmplifier(amplifier);
                    wrapper.setDuration(wrapper.isInstant() ? 1 : duration);
                    wrapper.setCharges(charges);
                    effects.put(effect, wrapper);
                } catch (IllegalArgumentException ignored){}
            }
        }
        return effects;
    }

    public static void updateEffectLore(ItemMeta meta){
        getStoredEffects(meta, false).values().forEach(w -> w.onApply(meta));
    }

    /**
     * Sets the given effects to the item as actualized effects. All effects will have {@link PotionEffectWrapper#onApply(ItemMeta)} executed,
     * all other registered effects will have {@link PotionEffectWrapper#onRemove(ItemMeta)} executed.
     * @param meta the item to set the actual effects on
     * @param effects the effects to set
     */
    public static void setActualStoredEffects(ItemMeta meta, Map<String, PotionEffectWrapper> effects){
        if (meta == null) return;
        if (effects == null || effects.isEmpty()) {
            clean(meta);
        } else {
            if (meta instanceof PotionMeta p) p.clearCustomEffects();
            registeredEffects.values().stream().filter(w -> effects.containsKey(w.getEffect())).forEach(w -> w.onRemove(meta));

            Map<String, PotionEffectWrapper> defaultEffects = getStoredEffects(meta, true);
            Collection<String> exclude = new HashSet<>();
            List<PotionEffectWrapper> orderedWrappers = new ArrayList<>(effects.values());
            orderedWrappers.sort(Comparator.comparingInt((PotionEffectWrapper a) -> a.getEffectName().length()));
            Collections.reverse(orderedWrappers);
            for (PotionEffectWrapper effect : orderedWrappers){
                if (!defaultEffects.containsKey(effect.getEffect()) || effect.getCharges() == 0) {
                    exclude.add(effect.getEffect());
                    continue;
                }

                if (effect.isVanilla()){
                    // applying a vanilla effect
                    int amplifier = (int) Math.floor(effect.getAmplifier());
                    int duration = effect.isInstant() ? 1 : (int) effect.getDuration();

                    // TODO test if this works out without
                    //if (i.getType() == Material.LINGERING_POTION) {
                    //    duration *= 4;
                    //}
                    //if (i.getType() == Material.TIPPED_ARROW) duration *= 8;

                    if (meta instanceof PotionMeta p) p.addCustomEffect(new PotionEffect(effect.getVanillaEffect(), duration, amplifier), true);
                }
                effect.onApply(meta);
            }

            if (meta instanceof PotionMeta p) p.setBasePotionData(new PotionData(PotionType.UNCRAFTABLE, false, false));
            meta.getPersistentDataContainer().set(ACTUAL_STORED_EFFECTS, PersistentDataType.STRING,
                    effects.values().stream()
                            .filter(e -> !exclude.contains(e.getEffect()))
                            .map(e -> e.getEffect() + ":" + e.getAmplifier() + ":" + e.getDuration() + (e.getCharges() >= 0 ? ":" + e.getCharges() : ""))
                            .collect(Collectors.joining(";"))
            );
        }
    }

    /**
     * Fetches a stored effect from the item
     * @param meta the item to fetch the effect from
     * @param effect the effect name to fetch
     * @param def if the effect should be fetched from the item's default effects or actual effects
     * @return the effect if found, null otherwise
     */
    public static PotionEffectWrapper getStoredEffect(ItemMeta meta, String effect, boolean def){
        return getStoredEffects(meta, def).get(effect);
    }

    /**
     * Adds a new default effect to the item. If the item doesn't have the actual effect yet, set that also
     * @param meta the item to add the effect to
     * @param wrapper the effect to add
     */
    public static void addDefaultEffect(ItemMeta meta, PotionEffectWrapper wrapper){
        Map<String, PotionEffectWrapper> defaultEffects = getStoredEffects(meta, true);
        defaultEffects.put(wrapper.getEffect(), wrapper);
        setDefaultStoredEffects(meta, defaultEffects);

        Map<String, PotionEffectWrapper> actualEffects = getStoredEffects(meta, false);
        actualEffects.putIfAbsent(wrapper.getEffect(), wrapper);

        setActualStoredEffects(meta, actualEffects);
    }

    public static void removeEffect(ItemMeta meta, String effect){
        Map<String, PotionEffectWrapper> defaultEffects = getStoredEffects(meta, true);
        defaultEffects.remove(effect);
        setDefaultStoredEffects(meta, defaultEffects);

        Map<String, PotionEffectWrapper> actualEffects = getStoredEffects(meta, false);
        actualEffects.remove(effect);
        setActualStoredEffects(meta, actualEffects);
    }

    /**
     * Changes an existing stored effect of an item. If the item does not have the effect, nothing happens.
     * @param meta the item to set the effect to
     * @param effect the name of the effect to set
     * @param amplifier the new amplifier of the effect
     * @param duration the new duration of the effect
     * @param def if the effect should be set to the item's default (true) effects or not (false)
     */
    public static void setStoredEffect(ItemMeta meta, String effect, double amplifier, long duration, int charges, boolean def){
        PotionEffectWrapper wrapper = getEffect(effect);
        if (wrapper == null) return;
        wrapper.setAmplifier(amplifier);
        wrapper.setDuration(duration);
        wrapper.setCharges(charges);
        setStoredEffect(meta, wrapper, def);
    }

    /**
     * Changes an existing stored effect of an item. If the item does not have the effect, nothing happens.
     * @param meta the item to set the effect to
     * @param wrapper the potion effect wrapper to override the existing effect
     * @param def if the effect should be set to the item's default (true) effects or not (false)
     */
    public static void setStoredEffect(ItemMeta meta, PotionEffectWrapper wrapper, boolean def){
        Map<String, PotionEffectWrapper> stats = getStoredEffects(meta, def);
        if (!stats.containsKey(wrapper.getEffect())) return;
        stats.put(wrapper.getEffect(), wrapper);
        if (def) setDefaultStoredEffects(meta, stats);
        setActualStoredEffects(meta, stats);
    }

    /**
     * Spends a charge of the given effect. If the item has 1 charge left, the effect is removed from the item.
     * @param meta the item meta to spend a charge on.
     * @param effect the effect to spend a charge of.
     * @return true if a charge was successfully spent, or false if for whatever reason the item had 0 charges left.
     */
    public static boolean spendCharge(ItemMeta meta, String effect){
        PotionEffectWrapper wrapper = getStoredEffect(meta, effect, true);
        if (wrapper.getCharges() < 0) return true;
        if (wrapper.getCharges() == 0) return false;
        if (wrapper.getCharges() == 1) removeEffect(meta, effect);
        else {
            wrapper.setCharges(wrapper.getCharges() - 1);
            setStoredEffect(meta, wrapper, true);
        }
        return true;
    }

    /**
     * Returns the amount of charges of the effect the item meta has, or -1 if the item has infinite (default)
     * @param meta the meta to get the amount of charges from
     * @param effect the effect to get its charges for
     * @return the amount of charges left of the effect on the item. 0 if no effect exists, or -1 if it has infinite.
     */
    public static int getCharges(ItemMeta meta, String effect){
        PotionEffectWrapper wrapper = getStoredEffect(meta, effect, false);
        return wrapper == null ? 0 : wrapper.getCharges();
    }

    /**
     * Cleans an ItemMeta of custom attributes
     * @param meta the meta to clean
     */
    public static void clean(ItemMeta meta){
        meta.getPersistentDataContainer().remove(DEFAULT_STORED_EFFECTS);
        meta.getPersistentDataContainer().remove(ACTUAL_STORED_EFFECTS);
        if (meta instanceof PotionMeta p) p.clearCustomEffects();
        registeredEffects.values().forEach(a -> a.onRemove(meta));
    }

    /**
     * Fetches all active effects from the entity
     * @param p the entity to fetch effects from
     * @return the map of all effects
     */
    public static Map<String, CustomPotionEffect> getActiveEffects(LivingEntity p){
        Map<String, CustomPotionEffect> effects = new HashMap<>();
        String encodedEffects = p.getPersistentDataContainer().getOrDefault(POTION_EFFECTS, PersistentDataType.STRING, "");
        if (!StringUtils.isEmpty(encodedEffects)){
            for (String encodedEffect : encodedEffects.split(";")){
                String[] args = encodedEffect.split(":");
                if (args.length == 3){
                    String effect = args[0];
                    PotionEffectWrapper customEffect = registeredEffects.get(effect);
                    if (customEffect == null || customEffect.isVanilla()) continue;
                    long effectiveUntil = Long.parseLong(args[1]);
                    double amplifier = Double.parseDouble(args[2]);
                    if (effectiveUntil != -1 && effectiveUntil < System.currentTimeMillis()) continue; // expired
                    effects.put(effect, new CustomPotionEffect(customEffect, effectiveUntil, amplifier));
                }
            }
        }

        return effects;
    }

    /**
     * Sets the active effects on the entity. If null or empty, clears all effects regardless of effect properties.
     * @param e the entity to set the effects to
     * @param effects the effects to add
     */
    public static void setActiveEffects(LivingEntity e, Collection<CustomPotionEffect> effects){
        Map<String, CustomPotionEffect> currentEffects = getActiveEffects(e);
        if (effects == null || effects.isEmpty()){
            for (CustomPotionEffect effect : currentEffects.values()){
                effect.setEffectiveUntil(0);
                addEffect(e, effect, true, EntityPotionEffectEvent.Cause.PLUGIN, EntityPotionEffectEvent.Action.CLEARED);
            }
        } else {
            for (CustomPotionEffect effect : effects){
                addEffect(e, effect, true, EntityPotionEffectEvent.Cause.PLUGIN, currentEffects.containsKey(effect.getWrapper().getEffect()) ? EntityPotionEffectEvent.Action.CHANGED : EntityPotionEffectEvent.Action.ADDED);
            }
        }
        EntityCache.resetPotionEffects(e); // adding/removing an effect as a result of this method should reset the entity's potion effect cache
    }

    private static void setActiveEffects(LivingEntity p, Map<String, CustomPotionEffect> effects){
        p.getPersistentDataContainer().set(POTION_EFFECTS, PersistentDataType.STRING, effects.values().stream()
                .filter(e -> e.getEffectiveUntil() != -1 && e.getEffectiveUntil() > System.currentTimeMillis())
                .map(e -> String.format("%s:%d:%.6f", e.getWrapper(), e.getEffectiveUntil(), e.getAmplifier()))
                .collect(Collectors.joining(";")));
    }

    /**
     * Adds a new potion effect to the given entity
     * @param e the entity
     * @param effect the effect to add
     * @param force true if overriding old effects, false if the effect shouldn't be cancelled if the previous effect has a stronger amplifier
     * @param cause the cause/reason for adding the effect
     * @param action the action to communicate to the server for effect changes
     */
    public static void addEffect(LivingEntity e, CustomPotionEffect effect, boolean force, EntityPotionEffectEvent.Cause cause, EntityPotionEffectEvent.Action action){
        if (!registeredEffects.containsKey(effect.getWrapper().getEffect())) {
            ValhallaMMO.logWarning("Attempting to apply custom effect " + effect.getWrapper() + ", but it was not registered");
            return;
        }
        Map<String, CustomPotionEffect> currentEffects = getActiveEffects(e);

        EntityCustomPotionEffectEvent event = new EntityCustomPotionEffectEvent(e, currentEffects.get(effect.getWrapper().getEffect()), effect, cause, action, force);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()){
            CustomPotionEffect newEffect = event.getNewEffect();
            // if the effect is not forced, do not apply if the existing effect is stronger than the new effect
            if (!event.isOverride() &&
                    currentEffects.containsKey(newEffect.getWrapper().getEffect()) &&
                    currentEffects.get(newEffect.getWrapper().getEffect()).getAmplifier() > newEffect.getAmplifier()) return;

            // remove if effectiveUntil is 0 or less, unless effectiveUntil is specifically -1 (infinite)
            if (newEffect.getEffectiveUntil() == -1 || (newEffect.getEffectiveUntil() > 0 || newEffect.getWrapper().isInstant())){
                currentEffects.put(newEffect.getWrapper().getEffect(), event.getNewEffect());
                newEffect.getWrapper().onInflict(e);
            } else {
                currentEffects.remove(event.getNewEffect().getWrapper().getEffect());
                newEffect.getWrapper().onExpire(e);
            }
            setActiveEffects(e, currentEffects);
        }
    }

    /**
     * Adds a new potion effect to the given entity. The action is predicted
     * @param e the entity
     * @param effect the effect to add
     * @param force true if overriding old effects, false if the effect shouldn't be cancelled if the previous effect has a stronger amplifier
     * @param cause the cause/reason for adding the effect
     */
    public static void addEffect(LivingEntity e, CustomPotionEffect effect, boolean force, EntityPotionEffectEvent.Cause cause){
        Map<String, CustomPotionEffect> currentEffects = getActiveEffects(e);
        EntityPotionEffectEvent.Action action;
        if (currentEffects.containsKey(effect.getWrapper().getEffect())) action = EntityPotionEffectEvent.Action.CHANGED;
        else if (effect.getEffectiveUntil() != -1 && effect.getEffectiveUntil() < 0) action = EntityPotionEffectEvent.Action.REMOVED;
        else action = EntityPotionEffectEvent.Action.ADDED;
        addEffect(e, effect, force, cause, action);
    }

    /**
     * Removes all active effects matching the filter off the entity. If the filter is null, all effects are removed.
     * @param e the entity to remove their effects
     * @param cause the cause/reason of the effect removal
     * @param filter the filter the effects need to match to be removed
     */
    public static void removePotionEffects(LivingEntity e, EntityPotionEffectEvent.Cause cause, Predicate<CustomPotionEffect> filter){
        for (CustomPotionEffect eff : getActiveEffects(e).values()){
            if (filter == null || filter.test(eff)){
                eff.setEffectiveUntil(0);
                addEffect(e, eff, true, cause);
            }
        }
        EntityCache.resetPotionEffects(e); // adding/removing an effect as a result of this method should reset the entity's potion effect cache
    }

    /**
     * The remaining duration of the given active effect if found.
     * @param e the effect to get the custom effect duration on
     * @param effect the effect to fetch
     * @return the duration in milliseconds for which the effect will be active
     */
    public static long getActiveDuration(LivingEntity e, String effect){
        CustomPotionEffect activeEffect = getActiveEffects(e).get(effect);
        if (activeEffect != null) {
            if (activeEffect.getEffectiveUntil() == -1) return -1;
            if (activeEffect.getEffectiveUntil() > System.currentTimeMillis()) return activeEffect.getEffectiveUntil() - System.currentTimeMillis();
        }
        return 0;
    }

    /**
     * The remaining amplifier of the given active effect if found.
     * @param e the effect to get the custom effect amplifier of
     * @param effect the effect to fetch
     * @param use true if the effect should be removed if it's single use, false if it should stay on the entity
     * @return the amplifier of the effect, or 0 if absent
     */
    public static double getActiveAmplifier(LivingEntity e, String effect, boolean use){
        CustomPotionEffect activeEffect = getActiveEffects(e).get(effect);
        if (activeEffect != null) {
            if (use && activeEffect.getWrapper().isSingleUse()) {
                activeEffect.setEffectiveUntil(0);
                addEffect(e, activeEffect, true, EntityPotionEffectEvent.Cause.EXPIRATION, EntityPotionEffectEvent.Action.REMOVED);
            }
            EntityCache.resetPotionEffects(e); // adding/removing an effect as a result of this method should reset the entity's potion effect cache
            return activeEffect.getAmplifier();
        }
        return 0;
    }

    /**
     * An active CustomPotionEffect on the entity if found and not expired.
     * @param e the entity to get the custom effect on
     * @param effect the effect to fetch
     * @return the active CustomPotionEffect if found and not expired, null otherwise
     */
    public static CustomPotionEffect getActiveEffect(LivingEntity e, String effect){
        CustomPotionEffect activeEffect = getActiveEffects(e).get(effect);
        if (activeEffect != null && (activeEffect.getEffectiveUntil() == -1 ||
                activeEffect.getEffectiveUntil() > System.currentTimeMillis())) return activeEffect;
        return null;
    }

    /**
     * Registers a new potion effect
     */
    public static void registerNewEffect(PotionEffectWrapper effect){
        registeredEffects.put(effect.getEffect(), effect);
    }

    /**
     * @return an unmodifiable map of all registered effects.
     */
    public static Map<String, PotionEffectWrapper> getRegisteredEffects() {
        return Collections.unmodifiableMap(registeredEffects);
    }

    /**
     * @param effect the name of the effect
     * @return Returns a copy of the given PotionEffectWrapper if registered, or null otherwise
     */
    public static PotionEffectWrapper getEffect(String effect){
        if (!registeredEffects.containsKey(effect)) throw new IllegalArgumentException("Attribute " + effect + " does not exist!");
        return registeredEffects.get(effect);
    }

    /**
     * Updates an item's name based on the potion effects it has received. Any item works with this method, but only
     * potions, splash potions, lingering potions, and tipped arrows have individual name formats. Other items use
     * a generic format. <br>
     * The name is based on the first PotionEffectWrapper it finds on the item, which can be inconsistent. Therefore it is
     * recommended this method is used when the item receives its first effect so you can more accurately control which
     * effect the name will be based on. If override is enabled, a new name is placed on the item regardless of previous
     * names. If override is disabled, nothing will happen if the item already has a custom name.
     * @param override whether the name should be changed regardless if the item has a custom name already
     */
    public static void updateItemName(ItemMeta meta, boolean override){
        if (meta == null) return;
        Material base = ItemUtils.getStoredType(meta);
        if (base == null) return;
        if (!meta.hasDisplayName() || override){
            Map<String, PotionEffectWrapper> effects = getStoredEffects(meta, false);
            if (effects.isEmpty()) return;
            String format = switch (base) {
                case SPLASH_POTION -> TranslationManager.getTranslation("potion_splash_format");
                case LINGERING_POTION -> TranslationManager.getTranslation("potion_lingering_format");
                case TIPPED_ARROW -> TranslationManager.getTranslation("tipped_arrow_format");
                case POTION -> TranslationManager.getTranslation("potion_base_format");
                default -> TranslationManager.getTranslation("item_generic_format");
            };
            PotionEffectWrapper effectForName = effects.values().stream().findAny().orElse(null);
            if (effectForName != null){
                String effectName = effectForName.getPotionName();
                meta.setDisplayName(Utils.chat(format.replace("%icon%", effectForName.getEffectIcon()).replace("%effect%", effectName).replace("%item%", ItemUtils.getItemName(meta))));
            }
        }
    }
}
