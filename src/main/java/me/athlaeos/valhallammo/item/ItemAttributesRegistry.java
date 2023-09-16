package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.attributes.AttributeWrapper;
import me.athlaeos.valhallammo.item.attributes.implementations.AttributeDisplayWrapper;
import me.athlaeos.valhallammo.item.attributes.implementations.AttributeHiddenWrapper;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class ItemAttributesRegistry {
    private static final NamespacedKey DEFAULT_STATS = new NamespacedKey(ValhallaMMO.getInstance(), "default_stats");
    private static final NamespacedKey ACTUAL_STATS = new NamespacedKey(ValhallaMMO.getInstance(), "actual_stats");

    private static final Map<Material, Map<String, AttributeWrapper>> vanillaAttributes = new HashMap<>();
    private static final Map<String, AttributeWrapper> registeredAttributes = new HashMap<>();

    public static void loadDefaults() {
        register(new AttributeHiddenWrapper("CUSTOM_MAX_DURABILITY", StatFormat.INT).addModifier(Material.DIAMOND, 1, 25)); // custom durability is already displayed elsewhere, so this one is hidden
        register(new AttributeDisplayWrapper("BOW_STRENGTH", StatFormat.PERCENTILE_BASE_1_P1, "\uEE00", (i) -> i >= 1, Material.BOW, Material.CROSSBOW).addModifier(Material.BOW));
        register(new AttributeDisplayWrapper("ARROW_DAMAGE", StatFormat.DIFFERENCE_FLOAT_P2, "\uEE01", (i) -> i >= 0).addModifier(Material.ARROW, 0.1, 1));
        register(new AttributeDisplayWrapper("AMMO_CONSUMPTION", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE02", (i) -> i <= 0).addModifier(Material.SPECTRAL_ARROW));
        register(new AttributeDisplayWrapper("ARROW_VELOCITY", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE03", (i) -> i >= 0).addModifier(Material.CROSSBOW));
        register(new AttributeDisplayWrapper("ARROW_ACCURACY", StatFormat.DIFFERENCE_FLOAT_P1, "\uEE04", (i) -> i >= 0).addModifier(Material.TARGET, 0.1, 1));
        register(new AttributeDisplayWrapper("ARROW_PIERCING", StatFormat.DIFFERENCE_INT, "\uEE05", (i) -> i >= 0).addModifier(Material.TIPPED_ARROW, 1, 5));
        register(new AttributeDisplayWrapper("KNOCKBACK", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE06", (i) -> i >= 0).addModifier(Material.SLIME_BLOCK));
        register(new AttributeDisplayWrapper("STUN_CHANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE07", (i) -> i >= 0).addModifier(Material.IRON_BLOCK));
        register(new AttributeDisplayWrapper("BLEED_CHANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE08", (i) -> i >= 0).addModifier(Material.GOLDEN_AXE));
        register(new AttributeDisplayWrapper("BLEED_DAMAGE", StatFormat.DIFFERENCE_FLOAT_P1, "\uEE09", (i) -> i >= 0).addModifier(Material.NETHERITE_AXE, 0.1, 1));
        register(new AttributeDisplayWrapper("BLEED_DURATION", StatFormat.DIFFERENCE_TIME_SECONDS_BASE_1000_P1, "\uEE0A", (i) -> i >= 0).addModifier(Material.IRON_AXE, 20, 100));
        register(new AttributeDisplayWrapper("CRIT_CHANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE0B", (i) -> i >= 0).addModifier(Material.GOLDEN_SWORD));
        register(new AttributeDisplayWrapper("CRIT_DAMAGE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE0C", (i) -> i >= 0).addModifier(Material.IRON_SWORD));
        register(new AttributeDisplayWrapper("ARMOR_PENETRATION_FLAT", StatFormat.DIFFERENCE_FLOAT_P1, "\uEE0D", (i) -> i >= 0).addModifier(Material.LEATHER_CHESTPLATE, 0.1, 1));
        register(new AttributeDisplayWrapper("LIGHT_ARMOR_PENETRATION_FLAT", StatFormat.DIFFERENCE_FLOAT_P1, "\uEE0E", (i) -> i >= 0).addModifier(Material.CHAINMAIL_CHESTPLATE, 0.1, 1));
        register(new AttributeDisplayWrapper("HEAVY_ARMOR_PENETRATION_FLAT", StatFormat.DIFFERENCE_FLOAT_P1, "\uEE0F", (i) -> i >= 0).addModifier(Material.IRON_CHESTPLATE, 0.1, 1));
        register(new AttributeDisplayWrapper("ARMOR_PENETRATION_FRACTION", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE10", (i) -> i >= 0).addModifier(Material.LEATHER_CHESTPLATE));
        register(new AttributeDisplayWrapper("LIGHT_ARMOR_PENETRATION_FRACTION", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE11", (i) -> i >= 0).addModifier(Material.CHAINMAIL_CHESTPLATE));
        register(new AttributeDisplayWrapper("HEAVY_ARMOR_PENETRATION_FRACTION", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE12", (i) -> i >= 0).addModifier(Material.IRON_CHESTPLATE));
        register(new AttributeDisplayWrapper("HEAVY_ARMOR_DAMAGE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE13", (i) -> i >= 0).addModifier(Material.IRON_HELMET));
        register(new AttributeDisplayWrapper("LIGHT_ARMOR_DAMAGE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE14", (i) -> i >= 0).addModifier(Material.CHAINMAIL_HELMET));
        register(new AttributeDisplayWrapper("IMMUNITY_BONUS_FRACTION", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE15", (i) -> i >= 0).addModifier(Material.ENCHANTED_GOLDEN_APPLE));
        register(new AttributeDisplayWrapper("IMMUNITY_BONUS_FLAT", StatFormat.DIFFERENCE_INT, "\uEE16", (i) -> i >= 0).addModifier(Material.GOLDEN_APPLE, 1, 5));
        register(new AttributeDisplayWrapper("IMMUNITY_REDUCTION", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE17", (i) -> i >= 0).addModifier(Material.WITHER_ROSE));
        register(new AttributeDisplayWrapper("DAMAGE_UNARMED", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE18", (i) -> i >= 0).addModifier(Material.BLAZE_POWDER));
        register(new AttributeDisplayWrapper("DAMAGE_MELEE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE19", (i) -> i >= 0).addModifier(Material.DIAMOND_SWORD));
        register(new AttributeDisplayWrapper("DAMAGE_RANGED", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE1A", (i) -> i >= 0).addModifier(Material.BOW));
        register(new AttributeDisplayWrapper("DAMAGE_ALL", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE1B", (i) -> i >= 0).addModifier(Material.BLAZE_ROD));
        register(new AttributeDisplayWrapper("ATTACK_REACH", StatFormat.DIFFERENCE_FLOAT_P2, "\uEE1C", (i) -> i >= 0).addModifier(Material.ENDER_PEARL, 0.1, 1));
        register(new AttributeDisplayWrapper("VELOCITY_DAMAGE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE1D", (i) -> i >= 0).addModifier(Material.DIAMOND_HORSE_ARMOR));
        register(new AttributeDisplayWrapper("DISMOUNT_CHANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE1E", (i) -> i >= 0).addModifier(Material.SADDLE));
        register(new AttributeDisplayWrapper("DAMAGE_RESISTANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE1F", (i) -> i >= 0).addModifier(Material.IRON_INGOT));
        register(new AttributeDisplayWrapper("EXPLOSION_RESISTANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE20", (i) -> i >= 0).addModifier(Material.TNT));
        register(new AttributeDisplayWrapper("FALL_DAMAGE_RESISTANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE21", (i) -> i >= 0).addModifier(Material.LEATHER_BOOTS));
        register(new AttributeDisplayWrapper("FIRE_RESISTANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE22", (i) -> i >= 0).addModifier(Material.LAVA_BUCKET));
        register(new AttributeDisplayWrapper("MAGIC_RESISTANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE23", (i) -> i >= 0).addModifier(Material.DRAGON_BREATH));
        register(new AttributeDisplayWrapper("BLEED_RESISTANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE24", (i) -> i >= 0).addModifier(Material.REDSTONE));
        register(new AttributeDisplayWrapper("STUN_RESISTANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE25", (i) -> i >= 0).addModifier(Material.NETHERITE_HELMET));
        register(new AttributeDisplayWrapper("POISON_RESISTANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE26", (i) -> i >= 0).addModifier(Material.SPIDER_EYE));
        register(new AttributeDisplayWrapper("PROJECTILE_RESISTANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE27", (i) -> i >= 0).addModifier(Material.ARROW));
        register(new AttributeDisplayWrapper("MELEE_RESISTANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE28", (i) -> i >= 0).addModifier(Material.SHIELD));
        register(new AttributeDisplayWrapper("DODGE_CHANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE29", (i) -> i >= 0).addModifier(Material.LEATHER_LEGGINGS));
        register(new AttributeDisplayWrapper("HEALING_BONUS", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE2A", (i) -> i >= 0).addModifier(Material.GLISTERING_MELON_SLICE));
        register(new AttributeDisplayWrapper("FOOD_CONSUMPTION", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE2B", (i) -> i <= 0).addModifier(Material.APPLE));
        register(new AttributeDisplayWrapper("COOLDOWN_REDUCTION", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE2C", (i) -> i >= 0).addModifier(Material.CLOCK));
        register(new AttributeDisplayWrapper("EXPLOSION_POWER", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE2D", (i) -> i >= 0).addModifier(Material.TNT_MINECART));
        register(new AttributeDisplayWrapper("CRAFTING_SPEED", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE2E", (i) -> i >= 0).addModifier(Material.CRAFTING_TABLE));
        register(new AttributeDisplayWrapper("ALCHEMY_QUALITY", StatFormat.DIFFERENCE_INT, "\uEE2F", (i) -> i >= 0).addModifier(Material.BREWING_STAND, 1, 10));
        register(new AttributeDisplayWrapper("ALCHEMY_QUALITY_FRACTION", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE30", (i) -> i >= 0).addModifier(Material.BREWING_STAND));
        register(new AttributeDisplayWrapper("ENCHANTING_QUALITY", StatFormat.DIFFERENCE_INT, "\uEE31", (i) -> i >= 0).addModifier(Material.ENCHANTING_TABLE, 1, 10));
        register(new AttributeDisplayWrapper("ENCHANTING_QUALITY_FRACTION", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE32", (i) -> i >= 0).addModifier(Material.ENCHANTING_TABLE));
        register(new AttributeDisplayWrapper("ANVIL_QUALITY_FLAT", StatFormat.DIFFERENCE_INT, "\uEE33", (i) -> i >= 0).addModifier(Material.ANVIL, 1, 10));
        register(new AttributeDisplayWrapper("ANVIL_QUALITY_FRACTION", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE34", (i) -> i >= 0).addModifier(Material.ANVIL));
        register(new AttributeDisplayWrapper("BREWING_INGREDIENT_CONSUMPTION", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE35", (i) -> i <= 0).addModifier(Material.GLASS_BOTTLE));
        register(new AttributeDisplayWrapper("BREWING_SPEED", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE36", (i) -> i >= 0).addModifier(Material.BLAZE_ROD));
        register(new AttributeDisplayWrapper("POTION_CONSUMPTION", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE37", (i) -> i <= 0).addModifier(Material.SPLASH_POTION));
        register(new AttributeDisplayWrapper("THROWING_VELOCITY", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE38", (i) -> i >= 0).addModifier(Material.SNOWBALL));
        register(new AttributeDisplayWrapper("SMITHING_QUALITY", StatFormat.DIFFERENCE_INT, "\uEE39", (i) -> i >= 0).addModifier(Material.ANVIL, 1, 10));
        register(new AttributeDisplayWrapper("SMITHING_QUALITY_FRACTION", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE3A", (i) -> i >= 0).addModifier(Material.ANVIL));
        register(new AttributeDisplayWrapper("MINING_RARE_DROPS", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE3B", (i) -> i >= 0).addModifier(Material.GOLDEN_PICKAXE));
        register(new AttributeDisplayWrapper("MINING_DROPS", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE3C", (i) -> i >= 0).addModifier(Material.IRON_PICKAXE));
        register(new AttributeDisplayWrapper("WOODCUTTING_RARE_DROPS", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE3D", (i) -> i >= 0).addModifier(Material.GOLDEN_AXE));
        register(new AttributeDisplayWrapper("WOODCUTTING_DROPS", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE3E", (i) -> i >= 0).addModifier(Material.IRON_AXE));
        register(new AttributeDisplayWrapper("DIGGING_RARE_DROPS", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE3F", (i) -> i >= 0).addModifier(Material.GOLDEN_SHOVEL));
        register(new AttributeDisplayWrapper("DIGGING_DROPS", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE40", (i) -> i >= 0).addModifier(Material.IRON_SHOVEL));
        register(new AttributeDisplayWrapper("FARMING_RARE_DROPS", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE41", (i) -> i >= 0).addModifier(Material.GOLDEN_HOE));
        register(new AttributeDisplayWrapper("FARMING_DROPS", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE42", (i) -> i >= 0).addModifier(Material.IRON_HOE));
        register(new AttributeDisplayWrapper("FISHING_LUCK", StatFormat.DIFFERENCE_FLOAT_P1, "\uEE43", (i) -> i >= 0).addModifier(Material.FISHING_ROD, 0.1, 1));
        register(new AttributeDisplayWrapper("SKILL_EXP_GAIN", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE44", (i) -> i >= 0).addModifier(Material.BOOK));
        register(new AttributeDisplayWrapper("VANILLA_EXP_GAIN", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE45", (i) -> i >= 0).addModifier(Material.EXPERIENCE_BOTTLE));
        register(new AttributeDisplayWrapper("DAMAGE_TAKEN", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE46", (i) -> i <= 0).addModifier(Material.SKELETON_SKULL));
        register(new AttributeDisplayWrapper("REFLECT_CHANCE", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE47", (i) -> i >= 0).addModifier(Material.SHIELD));
        register(new AttributeDisplayWrapper("REFLECT_FRACTION", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, "\uEE48", (i) -> i >= 0).addModifier(Material.SHIELD));

        register(new AttributeDisplayWrapper("GENERIC_LUCK", "\uEE4F", (i) -> i >= 0, StatFormat.FLOAT_P1, StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, StatFormat.PERCENTILE_BASE_1_P1).addModifier(Material.RABBIT_FOOT, 0.1, 1));
        register(new AttributeDisplayWrapper("GENERIC_ARMOR", "\uEE50", (i) -> i >= 0, StatFormat.FLOAT_P1, StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, StatFormat.PERCENTILE_BASE_1_P1).addModifier(Material.IRON_CHESTPLATE, 0.1, 1));
        register(new AttributeDisplayWrapper("GENERIC_ARMOR_TOUGHNESS", "\uEE52", (i) -> i >= 0, StatFormat.FLOAT_P1, StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, StatFormat.PERCENTILE_BASE_1_P1).addModifier(Material.DIAMOND_CHESTPLATE, 0.1, 1));
        register(new AttributeDisplayWrapper("GENERIC_ATTACK_DAMAGE", "\uEE54", (i) -> i >= 0, StatFormat.FLOAT_P1, StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, StatFormat.PERCENTILE_BASE_1_P1).addModifier(Material.IRON_SWORD, 0.1, 1));
        register(new AttributeDisplayWrapper("GENERIC_ATTACK_SPEED", "\uEE55", (i) -> i >= 0, StatFormat.FLOAT_P1, StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, StatFormat.PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_SWORD, 0.1, 1));
        register(new AttributeDisplayWrapper("GENERIC_KNOCKBACK_RESISTANCE", "\uEE56", (i) -> i >= 0, StatFormat.FLOAT_P1, StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, StatFormat.PERCENTILE_BASE_1_P1).setOperation(AttributeModifier.Operation.ADD_SCALAR).addModifier(Material.NETHERITE_CHESTPLATE));
        register(new AttributeDisplayWrapper("GENERIC_MAX_HEALTH", "\uEE57", (i) -> i >= 0, StatFormat.FLOAT_P1, StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, StatFormat.PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_APPLE, 0.1, 1));
        register(new AttributeDisplayWrapper("GENERIC_MOVEMENT_SPEED", "\uEE59", (i) -> i >= 0, StatFormat.FLOAT_P1, StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1, StatFormat.PERCENTILE_BASE_1_P1).setOperation(AttributeModifier.Operation.ADD_SCALAR).addModifier(Material.SUGAR));

        addVanillaStat(Material.WOODEN_SWORD, getCopy("GENERIC_ATTACK_DAMAGE").setValue(4), getCopy("GENERIC_ATTACK_SPEED").setValue(1.6));
        addVanillaStat(Material.WOODEN_PICKAXE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(2), getCopy("GENERIC_ATTACK_SPEED").setValue(1.2));
        addVanillaStat(Material.WOODEN_SHOVEL, getCopy("GENERIC_ATTACK_DAMAGE").setValue(2.5), getCopy("GENERIC_ATTACK_SPEED").setValue(1));
        addVanillaStat(Material.WOODEN_AXE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(7), getCopy("GENERIC_ATTACK_SPEED").setValue(0.8));
        addVanillaStat(Material.WOODEN_HOE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(1), getCopy("GENERIC_ATTACK_SPEED").setValue(1));

        addVanillaStat(Material.STONE_SWORD, getCopy("GENERIC_ATTACK_DAMAGE").setValue(5), getCopy("GENERIC_ATTACK_SPEED").setValue(1.6));
        addVanillaStat(Material.STONE_PICKAXE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(3), getCopy("GENERIC_ATTACK_SPEED").setValue(1.2));
        addVanillaStat(Material.STONE_SHOVEL, getCopy("GENERIC_ATTACK_DAMAGE").setValue(3.5), getCopy("GENERIC_ATTACK_SPEED").setValue(1));
        addVanillaStat(Material.STONE_AXE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(9), getCopy("GENERIC_ATTACK_SPEED").setValue(0.8));
        addVanillaStat(Material.STONE_HOE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(1), getCopy("GENERIC_ATTACK_SPEED").setValue(2));

        addVanillaStat(Material.GOLDEN_SWORD, getCopy("GENERIC_ATTACK_DAMAGE").setValue(4), getCopy("GENERIC_ATTACK_SPEED").setValue(1.6));
        addVanillaStat(Material.GOLDEN_PICKAXE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(2), getCopy("GENERIC_ATTACK_SPEED").setValue(1.2));
        addVanillaStat(Material.GOLDEN_SHOVEL, getCopy("GENERIC_ATTACK_DAMAGE").setValue(2.5), getCopy("GENERIC_ATTACK_SPEED").setValue(1));
        addVanillaStat(Material.GOLDEN_AXE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(7), getCopy("GENERIC_ATTACK_SPEED").setValue(0.8));
        addVanillaStat(Material.GOLDEN_HOE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(1), getCopy("GENERIC_ATTACK_SPEED").setValue(1));

        addVanillaStat(Material.IRON_SWORD, getCopy("GENERIC_ATTACK_DAMAGE").setValue(6), getCopy("GENERIC_ATTACK_SPEED").setValue(1.6));
        addVanillaStat(Material.IRON_PICKAXE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(4), getCopy("GENERIC_ATTACK_SPEED").setValue(1.2));
        addVanillaStat(Material.IRON_SHOVEL, getCopy("GENERIC_ATTACK_DAMAGE").setValue(4.5), getCopy("GENERIC_ATTACK_SPEED").setValue(1));
        addVanillaStat(Material.IRON_AXE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(9), getCopy("GENERIC_ATTACK_SPEED").setValue(0.9));
        addVanillaStat(Material.IRON_HOE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(1), getCopy("GENERIC_ATTACK_SPEED").setValue(3));

        addVanillaStat(Material.DIAMOND_SWORD, getCopy("GENERIC_ATTACK_DAMAGE").setValue(7), getCopy("GENERIC_ATTACK_SPEED").setValue(1.6));
        addVanillaStat(Material.DIAMOND_PICKAXE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(5), getCopy("GENERIC_ATTACK_SPEED").setValue(1.2));
        addVanillaStat(Material.DIAMOND_SHOVEL, getCopy("GENERIC_ATTACK_DAMAGE").setValue(5.5), getCopy("GENERIC_ATTACK_SPEED").setValue(1));
        addVanillaStat(Material.DIAMOND_AXE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(9), getCopy("GENERIC_ATTACK_SPEED").setValue(1));
        addVanillaStat(Material.DIAMOND_HOE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(1), getCopy("GENERIC_ATTACK_SPEED").setValue(4));

        addVanillaStat(Material.NETHERITE_SWORD, getCopy("GENERIC_ATTACK_DAMAGE").setValue(8), getCopy("GENERIC_ATTACK_SPEED").setValue(1.6));
        addVanillaStat(Material.NETHERITE_PICKAXE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(6), getCopy("GENERIC_ATTACK_SPEED").setValue(1.2));
        addVanillaStat(Material.NETHERITE_SHOVEL, getCopy("GENERIC_ATTACK_DAMAGE").setValue(6.5), getCopy("GENERIC_ATTACK_SPEED").setValue(1));
        addVanillaStat(Material.NETHERITE_AXE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(10), getCopy("GENERIC_ATTACK_SPEED").setValue(1));
        addVanillaStat(Material.NETHERITE_HOE, getCopy("GENERIC_ATTACK_DAMAGE").setValue(1), getCopy("GENERIC_ATTACK_SPEED").setValue(4));

        addVanillaStat(Material.LEATHER_HELMET, getCopy("GENERIC_ARMOR").setValue(1));
        addVanillaStat(Material.LEATHER_CHESTPLATE, getCopy("GENERIC_ARMOR").setValue(3));
        addVanillaStat(Material.LEATHER_LEGGINGS, getCopy("GENERIC_ARMOR").setValue(2));
        addVanillaStat(Material.LEATHER_BOOTS, getCopy("GENERIC_ARMOR").setValue(1));

        addVanillaStat(Material.CHAINMAIL_HELMET, getCopy("GENERIC_ARMOR").setValue(2));
        addVanillaStat(Material.CHAINMAIL_CHESTPLATE, getCopy("GENERIC_ARMOR").setValue(5));
        addVanillaStat(Material.CHAINMAIL_LEGGINGS, getCopy("GENERIC_ARMOR").setValue(4));
        addVanillaStat(Material.CHAINMAIL_BOOTS, getCopy("GENERIC_ARMOR").setValue(1));

        addVanillaStat(Material.GOLDEN_HELMET, getCopy("GENERIC_ARMOR").setValue(2));
        addVanillaStat(Material.GOLDEN_CHESTPLATE, getCopy("GENERIC_ARMOR").setValue(5));
        addVanillaStat(Material.GOLDEN_LEGGINGS, getCopy("GENERIC_ARMOR").setValue(3));
        addVanillaStat(Material.GOLDEN_BOOTS, getCopy("GENERIC_ARMOR").setValue(1));

        addVanillaStat(Material.IRON_HELMET, getCopy("GENERIC_ARMOR").setValue(2));
        addVanillaStat(Material.IRON_CHESTPLATE, getCopy("GENERIC_ARMOR").setValue(6));
        addVanillaStat(Material.IRON_LEGGINGS, getCopy("GENERIC_ARMOR").setValue(5));
        addVanillaStat(Material.IRON_BOOTS, getCopy("GENERIC_ARMOR").setValue(2));

        addVanillaStat(Material.DIAMOND_HELMET, getCopy("GENERIC_ARMOR").setValue(3), getCopy("GENERIC_ARMOR_TOUGHNESS").setValue(2));
        addVanillaStat(Material.DIAMOND_CHESTPLATE, getCopy("GENERIC_ARMOR").setValue(8), getCopy("GENERIC_ARMOR_TOUGHNESS").setValue(2));
        addVanillaStat(Material.DIAMOND_LEGGINGS, getCopy("GENERIC_ARMOR").setValue(6), getCopy("GENERIC_ARMOR_TOUGHNESS").setValue(2));
        addVanillaStat(Material.DIAMOND_BOOTS, getCopy("GENERIC_ARMOR").setValue(3), getCopy("GENERIC_ARMOR_TOUGHNESS").setValue(2));

        addVanillaStat(Material.NETHERITE_HELMET, getCopy("GENERIC_ARMOR").setValue(3), getCopy("GENERIC_ARMOR_TOUGHNESS").setValue(3), getCopy("GENERIC_KNOCKBACK_RESISTANCE").setValue(0.1).setOperation(AttributeModifier.Operation.ADD_SCALAR));
        addVanillaStat(Material.NETHERITE_CHESTPLATE, getCopy("GENERIC_ARMOR").setValue(8), getCopy("GENERIC_ARMOR_TOUGHNESS").setValue(3), getCopy("GENERIC_KNOCKBACK_RESISTANCE").setValue(0.1).setOperation(AttributeModifier.Operation.ADD_SCALAR));
        addVanillaStat(Material.NETHERITE_LEGGINGS, getCopy("GENERIC_ARMOR").setValue(6), getCopy("GENERIC_ARMOR_TOUGHNESS").setValue(3), getCopy("GENERIC_KNOCKBACK_RESISTANCE").setValue(0.1).setOperation(AttributeModifier.Operation.ADD_SCALAR));
        addVanillaStat(Material.NETHERITE_BOOTS, getCopy("GENERIC_ARMOR").setValue(3), getCopy("GENERIC_ARMOR_TOUGHNESS").setValue(3), getCopy("GENERIC_KNOCKBACK_RESISTANCE").setValue(0.1).setOperation(AttributeModifier.Operation.ADD_SCALAR));

        addVanillaStat(Material.TURTLE_HELMET, getCopy("GENERIC_ARMOR").setValue(2));

        addVanillaStat(Material.TRIDENT, getCopy("GENERIC_ATTACK_DAMAGE").setValue(9), getCopy("GENERIC_ATTACK_SPEED").setValue(1.1));

        addVanillaStat(Material.BOW, getCopy("BOW_STRENGTH").setValue(1));
        addVanillaStat(Material.CROSSBOW, getCopy("BOW_STRENGTH").setValue(1));
    }

    public static void reload(){
        registeredAttributes.clear();
        loadDefaults();
    }

    /**
     * Registers an {@link AttributeWrapper}
     * @param wrapper the AttributeWrapper to register
     */
    public static void register(AttributeWrapper wrapper){
        registeredAttributes.put(wrapper.getAttribute(), wrapper);
    }

    /**
     * Sets the given stats as default for a specific item<br>
     * Default stats are used as reference during stat scaling, so if an item for example has a default
     * attack damage of 7 any damage scaling formulas will be applied to that 7 attack damage.<br>
     * If the given stats are null or empty, the item will will have {@link ItemAttributesRegistry#clean(ItemMeta)} executed on it.
     * @param meta the item meta to apply default stats to
     * @param stats the stats to apply
     */
    public static void setDefaultStats(ItemMeta meta, Map<String, AttributeWrapper> stats){
        if (meta == null) return;
        if (stats == null || stats.isEmpty()) {
            clean(meta);
        } else {
            meta.getPersistentDataContainer().set(DEFAULT_STATS, PersistentDataType.STRING,
                    stats.values().stream().map(s -> s.getAttribute() + ":" + s.getValue() + ":" + s.getOperation())
                            .collect(Collectors.joining(";")));
        }
    }

    /**
     * Attempts to clean an ItemMeta off any custom attributes.<br>
     * Each registered attribute will run {@link AttributeWrapper#onRemove(ItemMeta)} on it
     * @param meta the meta to clean
     */
    public static void clean(ItemMeta meta){
        meta.getPersistentDataContainer().remove(DEFAULT_STATS);
        meta.getPersistentDataContainer().remove(ACTUAL_STATS);
        meta.setAttributeModifiers(null);
        registeredAttributes.values().forEach(a -> a.onRemove(meta));
    }

    /**
     * Returns the stored stats of an item.
     * @param meta the item meta to grab its stats from
     * @param def should be true if you want the default stats, false if actual stats
     * @return the map of attributes the item has
     */
    public static Map<String, AttributeWrapper> getStats(ItemMeta meta, boolean def){
        Map<String, AttributeWrapper> attributes = new HashMap<>();
        if (meta == null) return attributes;
        String stored = ItemUtils.getPDCString(def ? DEFAULT_STATS : ACTUAL_STATS, meta, null);
        if (!StringUtils.isEmpty(stored)){
            for (String attributeDetails : stored.split(";")){
                String[] args = attributeDetails.split(":");
                if (args.length < 2) continue;
                try {
                    String attribute = args[0];
                    double value = Double.parseDouble(args[1]);
                    AttributeModifier.Operation operation = args.length > 2 ? AttributeModifier.Operation.valueOf(args[2]) : AttributeModifier.Operation.ADD_NUMBER;

                    AttributeWrapper wrapper = registeredAttributes.get(attribute);
                    if (wrapper == null) continue;
                    wrapper = wrapper.copy();
                    wrapper.setValue(value);
                    wrapper.setOperation(operation);
                    attributes.put(attribute, wrapper);
                } catch (IllegalArgumentException ignored){}
            }
        }
        return attributes;
    }

    /**
     * Fetches the vanilla stats of a material, if any.
     * @param m the item type to fetch its vanilla stats from
     * @return the map of vanilla stats registered to the material, or an empty map if none.
     */
    public static Map<String, AttributeWrapper> getVanillaStats(Material m){
        return vanillaAttributes.getOrDefault(m, new HashMap<>());
    }

    /**
     * Sets the actualized stats of the item. If a stat in the given map is not included in the item's default
     * stats it will not be applied. <br>
     * For each stat applied, {@link AttributeWrapper#onApply(ItemMeta)} is executed. <br>
     * If the given stats are null or empty, the item will will have {@link ItemAttributesRegistry#clean(ItemMeta)} executed on it.
     * @param meta the item meta to set its actual stats on
     * @param stats the stats to set to the item
     */
    public static void setActualStats(ItemStack i, ItemMeta meta, Map<String, AttributeWrapper> stats){
        if (meta == null) return;
        if (stats == null || stats.isEmpty()) {
            clean(meta);
        } else {
            meta.setAttributeModifiers(null);
            registeredAttributes.values().stream().filter(w -> stats.containsKey(w.getAttribute())).forEach(w -> w.onRemove(meta));

            Map<String, AttributeWrapper> defaultStats = getStats(meta, true);
            Collection<String> exclude = new HashSet<>();
            List<AttributeWrapper> orderedWrappers = new ArrayList<>(stats.values());
            orderedWrappers.sort(Comparator.comparingInt((AttributeWrapper a) -> a.getAttributeName().length()));
            Collections.reverse(orderedWrappers);
            for (AttributeWrapper wrapper : orderedWrappers){
                if (!defaultStats.containsKey(wrapper.getAttribute())) {
                    exclude.add(wrapper.getAttribute());
                    continue;
                }

                if (wrapper.isVanilla()){
                    Attribute attribute = wrapper.getVanillaAttribute();
                    double value = wrapper.getValue();
                    if (attribute == Attribute.GENERIC_ATTACK_SPEED && wrapper.getOperation() == AttributeModifier.Operation.ADD_NUMBER) value -= 4; // player default attack speed
                    if (attribute == Attribute.GENERIC_ATTACK_DAMAGE && wrapper.getOperation() == AttributeModifier.Operation.ADD_NUMBER) value -= 1; // player default attack damage
                    EquipmentSlot slot = ItemUtils.getEquipmentSlot(meta);
                    meta.addAttributeModifier(attribute, new AttributeModifier(
                            UUID.randomUUID(),
                            wrapper.getAttribute().replaceFirst("_", ".").toLowerCase(),
                            value,
                            wrapper.getOperation(),
                            slot
                    ));
                    if (CustomFlag.hasFlag(meta, CustomFlag.ATTRIBUTE_FOR_BOTH_HANDS))
                        meta.addAttributeModifier(attribute, new AttributeModifier(
                                UUID.randomUUID(),
                                wrapper.getAttribute().replaceFirst("_", ".").toLowerCase(),
                                value,
                                wrapper.getOperation(),
                                EquipmentSlot.OFF_HAND
                        ));
                }
                wrapper.onApply(meta);
            }

            meta.getPersistentDataContainer().set(ACTUAL_STATS, PersistentDataType.STRING,
                    stats.values().stream()
                            .filter(s -> !exclude.contains(s.getAttribute()))
                            .map(s -> s.getAttribute() + ":" + s.getValue() + ":" + s.getOperation())
                            .collect(Collectors.joining(";"))
            );
            ItemUtils.setItemMeta(i, meta);
        }
    }

    /**
     * Applies the item material's default AND actual stats on the item
     * @param meta the item meta to set vanilla stats to
     * @return the vanilla stats if applied, or an empty hashmap if none
     */
    public static Map<String, AttributeWrapper> applyVanillaStats(ItemStack i, ItemMeta meta){
        Map<String, AttributeWrapper> vanillaStats = getVanillaStats(i.getType());
        if (!vanillaStats.isEmpty()) {
            setDefaultStats(meta, vanillaStats);
            setActualStats(i, meta, vanillaStats);
            return vanillaStats;
        }
        return new HashMap<>();
    }

    /**
     * Returns the AttributeWrapper of the given attribute if located on the item.
     * @param meta the item meta to fetch the attribute from
     * @param attribute the attribute to fetch
     * @param def true if the default attribute should be returned, false if the actual attribute should be returned
     * @return the AttributeWrapper containing the details of the stat, or null if none was present
     */
    public static AttributeWrapper getAttribute(ItemMeta meta, String attribute, boolean def){
        return getStats(meta, def).get(attribute);
    }

    /**
     * Does that {@link ItemAttributesRegistry#getAttribute(ItemMeta, String, boolean)} does, except if it doesn't
     * find a matching attribute it will attempt to fetch from the item's default stats. If it still doesn't find
     * any, it will attempt to fetch from the item's vanilla stats. <br>
     * Use if you really really need to know an item's stats regardless of circumstances
     * @param meta the item to really really fetch the attribute from
     * @param attribute the attribute to really really fetch
     * @return the AttributeWrapper containing the details of the stat, or null if none were really really found
     */
    public static AttributeWrapper getAnyAttribute(ItemStack i, ItemMeta meta, String attribute){
        return Objects.requireNonNullElse(getStats(meta, false).get(attribute),
                Objects.requireNonNullElse(getStats(meta, true).get(attribute),
                        getVanillaStats(i.getType()).get(attribute)));
    }

    /**
     * Adds a single default stat to the item. If the item has no actualized stat of this type
     * it will be added to the item's actualized stats also. If it DOES have the actualized stat,
     * it will not be touched. <br>
     * If the item has no default stats yet, vanilla stats will be applied to it
     * @param meta the item to add the default stat to
     * @param wrapper the stat to add
     */
    public static void addDefaultStat(ItemStack i, ItemMeta meta, AttributeWrapper wrapper){
        Map<String, AttributeWrapper> defaultStats = getStats(meta, true);
        if (defaultStats.isEmpty()) defaultStats = applyVanillaStats(i, meta);
        defaultStats.put(wrapper.getAttribute(), wrapper);
        setDefaultStats(meta, defaultStats);

        Map<String, AttributeWrapper> actualStats = getStats(meta, false);
        actualStats.putIfAbsent(wrapper.getAttribute(), wrapper);

        setActualStats(i, meta, actualStats);
    }

    public static void removeStat(ItemStack i, ItemMeta meta, AttributeWrapper wrapper){
        Map<String, AttributeWrapper> defaultStats = getStats(meta, true);
        if (defaultStats.isEmpty()) defaultStats = applyVanillaStats(i, meta);
        defaultStats.remove(wrapper.getAttribute(), wrapper);
        setDefaultStats(meta, defaultStats);

        Map<String, AttributeWrapper> actualStats = getStats(meta, false);
        actualStats.remove(wrapper.getAttribute());

        setActualStats(i, meta, actualStats);
    }

    /**
     * Changes the value of a stat on the item.
     * @param i the item to change a stat value of
     * @param attribute the stat to change
     * @param value the value to set it to
     * @param def if the default stat should be changed (true) or the actualized stat (false)
     */
    public static void setStat(ItemStack i, ItemMeta meta, String attribute, double value, boolean def){
        Map<String, AttributeWrapper> stats = getStats(meta, def);
        AttributeWrapper wrapper = stats.get(attribute);
        if (wrapper == null) return;
        wrapper.setValue(value);
        stats.put(wrapper.getAttribute(), wrapper);
        if (def) setDefaultStats(meta, stats);
        setActualStats(i, meta, stats);
    }

    /**
     * Adds a vanilla stat to the item type. This vanilla stat will be used as default.
     * @param item the item type to set the stat to
     * @param wrappers the stat to add to the item
     */
    public static void addVanillaStat(Material item, AttributeWrapper... wrappers){
        Map<String, AttributeWrapper> modifiers = vanillaAttributes.getOrDefault(item, new HashMap<>());
        for (AttributeWrapper wrapper : wrappers){
            modifiers.put(wrapper.getAttribute(), wrapper);
        }
        vanillaAttributes.put(item, modifiers);
    }

    /**
     * Does what {@link ItemAttributesRegistry#addVanillaStat(Material, AttributeWrapper...)} does, except
     * it takes in a string argument. This can be used to add vanilla stats to an item type that don't exist
     * on newer versions of Minecraft. (for example, any netherite gear if on <1.16 if the plugin was made to
     * support those versions which it doesn't)
     * @param item the item material name to set the stat to
     * @param wrappers the stat to add to the item
     */
    public static void addVanillaStat(String item, AttributeWrapper... wrappers){
        Material m = ItemUtils.stringToMaterial(item, null);
        if (m != null) addVanillaStat(m, wrappers);
    }

    /**
     * @return an unmodifiable map of all registered AttributeWrappers
     */
    public static Map<String, AttributeWrapper> getRegisteredAttributes() {
        return Collections.unmodifiableMap(registeredAttributes);
    }

    /**
     * @return all registered vanilla stat values
     */
    public static Map<Material, Map<String, AttributeWrapper>> getVanillaAttributes() {
        return vanillaAttributes;
    }

    /**
     * Gets a copy of the attribute wrapper with the given name
     * @param name the attribute name to get
     * @return a copy of the attribute wrapper. If none by the name exist, an IllegalArgumentException is thrown
     */
    public static AttributeWrapper getCopy(String name){
        if (!registeredAttributes.containsKey(name)) throw new IllegalArgumentException("Attribute " + name + " does not exist!");
        return registeredAttributes.get(name).copy();
    }
}
