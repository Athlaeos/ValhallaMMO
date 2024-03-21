package me.athlaeos.valhallammo.potioneffects;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.potioneffects.implementations.*;
import me.athlaeos.valhallammo.utility.*;
import me.athlaeos.valhallammo.event.EntityCustomPotionEffectEvent;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PotionEffectRegistry {
    private static final NamespacedKey POTION_EFFECTS = new NamespacedKey(ValhallaMMO.getInstance(), "potion_effects");
    private static final NamespacedKey ACTUAL_STORED_EFFECTS = new NamespacedKey(ValhallaMMO.getInstance(), "stored_effects");
    private static final NamespacedKey DEFAULT_STORED_EFFECTS = new NamespacedKey(ValhallaMMO.getInstance(), "default_stored_effects");
    private static final Map<String, PotionEffectWrapper> registeredEffects = new HashMap<>();
    private static final Collection<UUID> entitiesWithEffects = new HashSet<>();
    private static CustomEffectDisplay customEffectDisplay = ValhallaMMO.getPluginConfig().getBoolean("custom_effect_display", true) ? new CustomEffectSidebarDisplay() : null;
    private static final Map<PotionType, Map<String, PotionTypeEffectWrapper>> typeToEffectWrappings = new HashMap<>();

    static {
        typeToEffectWrappings.put(PotionType.FIRE_RESISTANCE, Map.of("FIRE_RESISTANCE", new PotionTypeEffectWrapper("FIRE_RESISTANCE").dB(180).dEx(480)));
        typeToEffectWrappings.put(PotionType.INSTANT_DAMAGE, Map.of("HARM", new PotionTypeEffectWrapper("HARM").aB(0).aU(1)));
        typeToEffectWrappings.put(PotionType.INSTANT_HEAL, Map.of("HEAL", new PotionTypeEffectWrapper("HEAL").aB(0).aU(1)));
        typeToEffectWrappings.put(PotionType.INVISIBILITY, Map.of("INVISIBILITY", new PotionTypeEffectWrapper("INVISIBILITY").dB(180).dEx(480)));
        typeToEffectWrappings.put(PotionType.JUMP, Map.of("JUMP", new PotionTypeEffectWrapper("JUMP").dB(180).dEx(480).dUp(180).aB(0).aU(1)));
        typeToEffectWrappings.put(PotionType.LUCK, Map.of("LUCK", new PotionTypeEffectWrapper("LUCK").dB(300)));
        typeToEffectWrappings.put(PotionType.NIGHT_VISION, Map.of("NIGHT_VISION", new PotionTypeEffectWrapper("NIGHT_VISION").dB(180).dEx(480)));
        typeToEffectWrappings.put(PotionType.POISON, Map.of("POISON", new PotionTypeEffectWrapper("POISON").dB(45).dEx(90).dUp(22).aB(0).aU(1)));
        typeToEffectWrappings.put(PotionType.REGEN, Map.of("REGENERATION", new PotionTypeEffectWrapper("REGENERATION").dB(45).dEx(90).dUp(22).aB(0).aU(1)));
        typeToEffectWrappings.put(PotionType.SLOW_FALLING, Map.of("SLOW_FALLING", new PotionTypeEffectWrapper("SLOW_FALLING").dB(90).dEx(240)));
        typeToEffectWrappings.put(PotionType.SLOWNESS, Map.of("SLOW", new PotionTypeEffectWrapper("SLOW").dB(90).dEx(240).dUp(20).aB(0).aU(4)));
        typeToEffectWrappings.put(PotionType.SPEED, Map.of("SPEED", new PotionTypeEffectWrapper("SPEED").dB(180).dEx(480).dUp(180).aB(0).aU(1)));
        typeToEffectWrappings.put(PotionType.STRENGTH, Map.of("INCREASE_DAMAGE", new PotionTypeEffectWrapper("INCREASE_DAMAGE").dB(180).dEx(480).dUp(180).aB(0).aU(1)));
        typeToEffectWrappings.put(PotionType.TURTLE_MASTER, Map.of("SLOW", new PotionTypeEffectWrapper("SLOW").dB(20).dEx(40).dUp(20).aB(3).aU(5),
                "DAMAGE_RESISTANCE", new PotionTypeEffectWrapper("DAMAGE_RESISTANCE").dB(20).dEx(40).dUp(20).aB(2).aU(3)));
        typeToEffectWrappings.put(PotionType.WATER_BREATHING, Map.of("WATER_BREATHING", new PotionTypeEffectWrapper("WATER_BREATHING").dB(180).dEx(480)));
        typeToEffectWrappings.put(PotionType.WEAKNESS, Map.of("WEAKNESS", new PotionTypeEffectWrapper("WEAKNESS").dB(90).dEx(240)));
    }

    private static void markAsUnaffected(LivingEntity entity){
        entitiesWithEffects.remove(entity.getUniqueId());
        if (entity instanceof Player p) SideBarUtils.hideSideBarFromPlayer(p, "valhalla_effects");
    }

    public static void updatePlayerAffectedStatus(Player p){
        if (!getActiveEffects(p).isEmpty()) entitiesWithEffects.add(p.getUniqueId());
    }

    public static void registerEffects() {
        registerNewEffect(new GenericWrapper("SPEED", (i) -> true, "\uED00", StatFormat.ROMAN).addModifier(Material.SUGAR, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("SLOW", (i) -> false, "\uED01", StatFormat.ROMAN).addModifier(Material.ICE, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("FAST_DIGGING", (i) -> true, "\uED02", StatFormat.ROMAN).addModifier(Material.GOLDEN_PICKAXE, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("SLOW_DIGGING", (i) -> false, "\uED03", StatFormat.ROMAN).addModifier(Material.SPONGE, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("INCREASE_DAMAGE", (i) -> true, "\uED04", StatFormat.ROMAN).addModifier(Material.BLAZE_POWDER, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("HEAL", (i) -> true, "\uED05", StatFormat.ROMAN).addModifier(Material.GLISTERING_MELON_SLICE, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("HARM", (i) -> false, "\uED06", StatFormat.ROMAN).addModifier(Material.DRAGON_BREATH, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("JUMP", (i) -> true, "\uED07", StatFormat.ROMAN).addModifier(Material.SLIME_BLOCK, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("CONFUSION", (i) -> false, "\uED08", StatFormat.ROMAN).addModifier(Material.ANVIL, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("REGENERATION", (i) -> true, "\uED09", StatFormat.ROMAN).addModifier(Material.GHAST_TEAR, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("DAMAGE_RESISTANCE", (i) -> true, "\uED0A", StatFormat.ROMAN).addModifier(Material.ENCHANTED_GOLDEN_APPLE, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("FIRE_RESISTANCE", (i) -> true, "\uED0B", StatFormat.ROMAN).addModifier(Material.MAGMA_CREAM, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("WATER_BREATHING", (i) -> true, "\uED0C", StatFormat.ROMAN).addModifier(Material.PUFFERFISH, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("INVISIBILITY", (i) -> true, "\uED0D", StatFormat.ROMAN).addModifier(Material.GLASS, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("BLINDNESS", (i) -> false, "\uED0E", StatFormat.ROMAN).addModifier(Material.INK_SAC, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("DARKNESS", (i) -> false, "\uED0F", StatFormat.ROMAN).addModifier(Material.BLACK_STAINED_GLASS, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("NIGHT_VISION", (i) -> true, "\uED10", StatFormat.ROMAN).addModifier(Material.GOLDEN_CARROT, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("HUNGER", (i) -> false, "\uED11", StatFormat.ROMAN).addModifier(Material.ROTTEN_FLESH, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("WEAKNESS", (i) -> false, "\uED12", StatFormat.ROMAN).addModifier(Material.WOODEN_SWORD, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("POISON", (i) -> false, "\uED13", StatFormat.ROMAN).addModifier(Material.SPIDER_EYE, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("WITHER", (i) -> false, "\uED14", StatFormat.ROMAN).addModifier(Material.WITHER_SKELETON_SKULL, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("HEALTH_BOOST", (i) -> true, "\uED15", StatFormat.ROMAN).addModifier(Material.SWEET_BERRIES, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("ABSORPTION", (i) -> true, "\uED16", StatFormat.ROMAN).addModifier(Material.GOLDEN_APPLE, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("SATURATION", (i) -> true, "\uED17", StatFormat.ROMAN).addModifier(Material.COOKED_BEEF, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("GLOWING", (i) -> true, "\uED18", StatFormat.ROMAN).addModifier(Material.GLOWSTONE_DUST, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("LEVITATION", (i) -> false, "\uED19", StatFormat.ROMAN).addModifier(Material.SHULKER_SHELL, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("LUCK", (i) -> true, "\uED1A", StatFormat.ROMAN).addModifier(Material.RABBIT_FOOT, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("UNLUCK", (i) -> false, "\uED1B", StatFormat.ROMAN).addModifier(Material.BONE, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("SLOW_FALLING", (i) -> true, "\uED1C", StatFormat.ROMAN).addModifier(Material.PHANTOM_MEMBRANE, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("CONDUIT_POWER", (i) -> true, "\uED1D", StatFormat.ROMAN).addModifier(Material.CONDUIT, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("DOLPHINS_GRACE", (i) -> true, "\uED1E", StatFormat.ROMAN).addModifier(Material.HEART_OF_THE_SEA, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("BAD_OMEN", (i) -> false, "\uED1F", StatFormat.ROMAN).addModifier(Material.IRON_AXE, 0.01, 0.25));
        registerNewEffect(new GenericWrapper("HERO_OF_THE_VILLAGE", (i) -> true, "\uED20", StatFormat.ROMAN).addModifier(Material.EMERALD, 0.01, 0.25));

        registerNewEffect(new GenericWrapper("BOW_STRENGTH", (i) -> i >= 0, "\uEE00", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BOW));
        registerNewEffect(new GenericWrapper("ARROW_DAMAGE", (i) -> i >= 0, "\uEE01", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.ARROW, 0.1, 1));
        registerNewEffect(new GenericWrapper("AMMO_CONSUMPTION", (i) -> i <= 0, "\uEE02", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SPECTRAL_ARROW));
        registerNewEffect(new GenericWrapper("ARROW_VELOCITY", (i) -> i >= 0, "\uEE03", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.CROSSBOW));
        registerNewEffect(new GenericWrapper("ARROW_ACCURACY", (i) -> i >= 0, "\uEE04", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.TARGET, 0.1, 1));
        registerNewEffect(new GenericWrapper("ARROW_PIERCING", (i) -> i >= 0, "\uEE05", StatFormat.DIFFERENCE_INT).addModifier(Material.TIPPED_ARROW, 1, 5));
        registerNewEffect(new GenericWrapper("KNOCKBACK", (i) -> i >= 0, "\uEE06", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SLIME_BLOCK));
        registerNewEffect(new GenericWrapper("STUN_CHANCE", (i) -> i >= 0, "\uEE07", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_BLOCK));
        registerNewEffect(new GenericWrapper("BLEED_CHANCE", (i) -> i >= 0, "\uEE08", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_AXE));
        registerNewEffect(new GenericWrapper("BLEED_DAMAGE", (i) -> i >= 0, "\uEE09", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.NETHERITE_AXE, 0.1, 1));
        registerNewEffect(new GenericWrapper("BLEED_DURATION", (i) -> i >= 0, "\uEE0A", StatFormat.DIFFERENCE_TIME_SECONDS_BASE_20_P1).addModifier(Material.IRON_AXE, 20, 100));
        registerNewEffect(new GenericWrapper("CRIT_CHANCE", (i) -> i >= 0, "\uEE0B", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_SWORD));
        registerNewEffect(new GenericWrapper("CRIT_DAMAGE", (i) -> i >= 0, "\uEE0C", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_SWORD));
        registerNewEffect(new GenericWrapper("ARMOR_PENETRATION_FLAT", (i) -> i >= 0, "\uEE0D", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.LEATHER_CHESTPLATE, 0.1, 1));
        registerNewEffect(new GenericWrapper("LIGHT_ARMOR_PENETRATION_FLAT", (i) -> i >= 0, "\uEE0E", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.CHAINMAIL_CHESTPLATE, 0.1, 1));
        registerNewEffect(new GenericWrapper("HEAVY_ARMOR_PENETRATION_FLAT", (i) -> i >= 0, "\uEE0F", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.IRON_CHESTPLATE, 0.1, 1));
        registerNewEffect(new GenericWrapper("ARMOR_PENETRATION_FRACTION", (i) -> i >= 0, "\uEE10", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.LEATHER_CHESTPLATE));
        registerNewEffect(new GenericWrapper("LIGHT_ARMOR_PENETRATION_FRACTION", (i) -> i >= 0, "\uEE11", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.CHAINMAIL_CHESTPLATE));
        registerNewEffect(new GenericWrapper("HEAVY_ARMOR_PENETRATION_FRACTION", (i) -> i >= 0, "\uEE12", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_CHESTPLATE));
        registerNewEffect(new GenericWrapper("HEAVY_ARMOR_DAMAGE", (i) -> i >= 0, "\uEE13", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_HELMET));
        registerNewEffect(new GenericWrapper("LIGHT_ARMOR_DAMAGE", (i) -> i >= 0, "\uEE14", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.CHAINMAIL_HELMET));
        registerNewEffect(new GenericWrapper("IMMUNITY_BONUS_FRACTION", (i) -> i >= 0, "\uEE15", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ENCHANTED_GOLDEN_APPLE));
        registerNewEffect(new GenericWrapper("IMMUNITY_BONUS_FLAT", (i) -> i >= 0, "\uEE16", StatFormat.DIFFERENCE_INT).addModifier(Material.GOLDEN_APPLE, 1, 5));
        registerNewEffect(new GenericWrapper("IMMUNITY_REDUCTION", (i) -> i >= 0, "\uEE17", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.WITHER_ROSE));
        registerNewEffect(new GenericWrapper("DAMAGE_UNARMED", (i) -> i >= 0, "\uEE18", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BLAZE_POWDER));
        registerNewEffect(new GenericWrapper("DAMAGE_MELEE", (i) -> i >= 0, "\uEE19", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.DIAMOND_SWORD));
        registerNewEffect(new GenericWrapper("DAMAGE_RANGED", (i) -> i >= 0, "\uEE1A", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BOW));
        registerNewEffect(new GenericWrapper("DAMAGE_ALL", (i) -> i >= 0, "\uEE1B", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BLAZE_ROD));
        registerNewEffect(new GenericWrapper("ATTACK_REACH", (i) -> i >= 0, "\uEE1C", StatFormat.DIFFERENCE_FLOAT_P2).addModifier(Material.ENDER_PEARL, 0.1, 1));
        registerNewEffect(new GenericWrapper("VELOCITY_DAMAGE", (i) -> i >= 0, "\uEE1D", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.DIAMOND_HORSE_ARMOR));
        registerNewEffect(new GenericWrapper("DISMOUNT_CHANCE", (i) -> i >= 0, "\uEE1E", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SADDLE));
        registerNewEffect(new GenericWrapper("CUSTOM_DAMAGE_RESISTANCE", (i) -> i >= 0, "\uEE1F", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_INGOT));
        registerNewEffect(new GenericWrapper("EXPLOSION_RESISTANCE", (i) -> i >= 0, "\uEE20", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.TNT));
        registerNewEffect(new GenericWrapper("FALL_DAMAGE_RESISTANCE", (i) -> i >= 0, "\uEE21", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.LEATHER_BOOTS));
        registerNewEffect(new GenericWrapper("CUSTOM_FIRE_RESISTANCE", (i) -> i >= 0, "\uEE22", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.LAVA_BUCKET));
        registerNewEffect(new GenericWrapper("MAGIC_RESISTANCE", (i) -> i >= 0, "\uEE23", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.DRAGON_BREATH));
        registerNewEffect(new GenericWrapper("BLEED_RESISTANCE", (i) -> i >= 0, "\uEE24", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.REDSTONE));
        registerNewEffect(new GenericWrapper("STUN_RESISTANCE", (i) -> i >= 0, "\uEE25", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.NETHERITE_HELMET));
        registerNewEffect(new GenericWrapper("POISON_RESISTANCE", (i) -> i >= 0, "\uEE26", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SPIDER_EYE));
        registerNewEffect(new GenericWrapper("PROJECTILE_RESISTANCE", (i) -> i >= 0, "\uEE27", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ARROW));
        registerNewEffect(new GenericWrapper("MELEE_RESISTANCE", (i) -> i >= 0, "\uEE28", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SHIELD));
        registerNewEffect(new GenericWrapper("DODGE_CHANCE", (i) -> i >= 0, "\uEE29", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.LEATHER_LEGGINGS));
        registerNewEffect(new GenericWrapper("HEALING_BONUS", (i) -> i >= 0, "\uEE2A", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GLISTERING_MELON_SLICE));
        registerNewEffect(new GenericWrapper("FOOD_CONSUMPTION", (i) -> i <= 0, "\uEE2B", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.APPLE));
        registerNewEffect(new GenericWrapper("COOLDOWN_REDUCTION", (i) -> i >= 0, "\uEE2C", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.CLOCK));
        registerNewEffect(new GenericWrapper("EXPLOSION_POWER", (i) -> i >= 0, "\uEE2D", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.TNT_MINECART));
        registerNewEffect(new GenericWrapper("CRAFTING_SPEED", (i) -> i >= 0, "\uEE2E", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.CRAFTING_TABLE));
        registerNewEffect(new GenericWrapper("ALCHEMY_QUALITY", (i) -> i >= 0, "\uEE2F", StatFormat.DIFFERENCE_INT).addModifier(Material.BREWING_STAND, 1, 10));
        registerNewEffect(new GenericWrapper("ALCHEMY_QUALITY_FRACTION", (i) -> i >= 0, "\uEE30", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BREWING_STAND));
        registerNewEffect(new GenericWrapper("ENCHANTING_QUALITY", (i) -> i >= 0, "\uEE31", StatFormat.DIFFERENCE_INT).addModifier(Material.ENCHANTING_TABLE, 1, 10));
        registerNewEffect(new GenericWrapper("ENCHANTING_QUALITY_FRACTION", (i) -> i >= 0, "\uEE32", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ENCHANTING_TABLE));
        registerNewEffect(new GenericWrapper("ANVIL_QUALITY_FLAT", (i) -> i >= 0, "\uEE33", StatFormat.DIFFERENCE_INT).addModifier(Material.ANVIL, 1, 10));
        registerNewEffect(new GenericWrapper("ANVIL_QUALITY_FRACTION", (i) -> i >= 0, "\uEE34", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ANVIL));
        registerNewEffect(new GenericWrapper("BREWING_INGREDIENT_CONSUMPTION", (i) -> i <= 0, "\uEE35", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GLASS_BOTTLE));
        registerNewEffect(new GenericWrapper("BREWING_SPEED", (i) -> i >= 0, "\uEE36", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BLAZE_ROD));
        registerNewEffect(new GenericWrapper("POTION_CONSUMPTION", (i) -> i <= 0, "\uEE37", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SPLASH_POTION));
        registerNewEffect(new GenericWrapper("THROWING_VELOCITY", (i) -> i >= 0, "\uEE38", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SNOWBALL));
        registerNewEffect(new GenericWrapper("SMITHING_QUALITY", (i) -> i >= 0, "\uEE39", StatFormat.DIFFERENCE_INT).addModifier(Material.ANVIL, 1, 10));
        registerNewEffect(new GenericWrapper("SMITHING_QUALITY_FRACTION", (i) -> i >= 0, "\uEE3A", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ANVIL));
        registerNewEffect(new GenericWrapper("MINING_RARE_DROPS", (i) -> i >= 0, "\uEE3B", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_PICKAXE));
        registerNewEffect(new GenericWrapper("MINING_DROPS", (i) -> i >= 0, "\uEE3C", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_PICKAXE));
        registerNewEffect(new GenericWrapper("WOODCUTTING_RARE_DROPS", (i) -> i >= 0, "\uEE3D", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_AXE));
        registerNewEffect(new GenericWrapper("WOODCUTTING_DROPS", (i) -> i >= 0, "\uEE3E", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_AXE));
        registerNewEffect(new GenericWrapper("DIGGING_RARE_DROPS", (i) -> i >= 0, "\uEE3F", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_SHOVEL));
        registerNewEffect(new GenericWrapper("DIGGING_DROPS", (i) -> i >= 0, "\uEE40", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_SHOVEL));
        registerNewEffect(new GenericWrapper("FARMING_RARE_DROPS", (i) -> i >= 0, "\uEE41", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_HOE));
        registerNewEffect(new GenericWrapper("FARMING_DROPS", (i) -> i >= 0, "\uEE42", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_HOE));
        registerNewEffect(new GenericWrapper("FISHING_LUCK", (i) -> i >= 0, "\uEE43", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.FISHING_ROD, 0.1, 1));
        registerNewEffect(new GenericWrapper("SKILL_EXP_GAIN", (i) -> i >= 0, "\uEE44", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BOOK));
        registerNewEffect(new GenericWrapper("VANILLA_EXP_GAIN", (i) -> i >= 0, "\uEE45", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.EXPERIENCE_BOTTLE));
        registerNewEffect(new GenericWrapper("DAMAGE_TAKEN", (i) -> i <= 0, "\uEE46", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SKELETON_SKULL));
        registerNewEffect(new GenericWrapper("REFLECT_CHANCE", (i) -> i >= 0, "\uEE47", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SHIELD));
        registerNewEffect(new GenericWrapper("REFLECT_FRACTION", (i) -> i >= 0, "\uEE48", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SHIELD));

        registerNewEffect(new GenericWrapper("SMITHING_MASTERPIECE_FLAT", (i) -> i >= 0, false, true, "\uEE49", StatFormat.DIFFERENCE_INT).addModifier(Material.ANVIL, 1, 10));
        registerNewEffect(new GenericWrapper("SMITHING_MASTERPIECE_FRACTION", (i) -> i >= 0, false, true, "\uEE4A", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ANVIL));
        registerNewEffect(new GenericWrapper("ENCHANTING_MASTERPIECE_FLAT", (i) -> i >= 0, false, true, "\uEE4B", StatFormat.DIFFERENCE_INT).addModifier(Material.ENCHANTING_TABLE, 1, 10));
        registerNewEffect(new GenericWrapper("ENCHANTING_MASTERPIECE_FRACTION", (i) -> i >= 0, false, true, "\uEE4C", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ENCHANTING_TABLE));
        registerNewEffect(new GenericWrapper("ALCHEMY_MASTERPIECE_FLAT", (i) -> i >= 0, false, true, "\uEE4D", StatFormat.DIFFERENCE_INT).addModifier(Material.BREWING_STAND, 1, 10));
        registerNewEffect(new GenericWrapper("ALCHEMY_MASTERPIECE_FRACTION", (i) -> i >= 0, false, true, "\uEE4E", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BREWING_STAND));

        registerNewEffect(new GenericWrapper("CUSTOM_LUCK", (i) -> i >= 0, "\uEE4F", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.RABBIT_FOOT, 0.1, 1));
        registerNewEffect(new GenericWrapper("ARMOR_FLAT", (i) -> i >= 0, "\uEE50", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.IRON_CHESTPLATE, 0.1, 1));
        registerNewEffect(new GenericWrapper("ARMOR_FRACTION", (i) -> i >= 0, "\uEE51", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.IRON_CHESTPLATE, 0.1, 1));
        registerNewEffect(new GenericWrapper("ARMOR_TOUGHNESS_FLAT", (i) -> i >= 0, "\uEE52", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.DIAMOND_CHESTPLATE, 0.1, 1));
        registerNewEffect(new GenericWrapper("ARMOR_TOUGHNESS_FRACTION", (i) -> i >= 0, "\uEE53", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.DIAMOND_CHESTPLATE, 0.1, 1));
        registerNewEffect(new GenericWrapper("EXTRA_ATTACK_DAMAGE", (i) -> i >= 0, "\uEE54", StatFormat.DIFFERENCE_FLOAT_P2).addModifier(Material.IRON_SWORD, 0.1, 1));
        registerNewEffect(new GenericWrapper("ATTACK_SPEED", (i) -> i >= 0, "\uEE55", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_SWORD, 0.1, 1));
        registerNewEffect(new GenericWrapper("KNOCKBACK_RESISTANCE", (i) -> i >= 0, "\uEE56", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.NETHERITE_CHESTPLATE));
        registerNewEffect(new GenericWrapper("MAX_HEALTH_FLAT", (i) -> i >= 0, "\uEE57", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.GOLDEN_APPLE, 0.1, 1));
        registerNewEffect(new GenericWrapper("MAX_HEALTH_FRACTION", (i) -> i >= 0, "\uEE58", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_APPLE, 0.1, 1));
        registerNewEffect(new GenericWrapper("MOVEMENT_SPEED", (i) -> i >= 0, "\uEE59", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SUGAR));

        registerNewEffect(new ChocolateMilk("CHOCOLATE_MILK", "\uEE5A").addModifier(Material.COCOA, 0, 0));
        registerNewEffect(new Milk("MILK", "\uEE5B").addModifier(Material.MILK_BUCKET, 0, 0));

        registerNewEffect(new GenericWrapper("SNEAK_MOVEMENT_SPEED_BONUS", (i) -> i >= 0, "\uEE5C", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLDEN_BOOTS));
        registerNewEffect(new GenericWrapper("SPRINT_MOVEMENT_SPEED_BONUS", (i) -> i >= 0, "\uEE5D", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.LEATHER_BOOTS));
        registerNewEffect(new GenericWrapper("DAMAGE_EXPLOSION", (i) -> i >= 0, "\uEE5E", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.TNT_MINECART));
        registerNewEffect(new GenericWrapper("DAMAGE_FIRE", (i) -> i >= 0, "\uEE5F", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.FIRE_CHARGE));
        registerNewEffect(new GenericWrapper("DAMAGE_MAGIC", (i) -> i >= 0, "\uEE60", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SPLASH_POTION));
        registerNewEffect(new GenericWrapper("DAMAGE_POISON", (i) -> i >= 0, "\uEE61", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SPIDER_EYE));
        registerNewEffect(new GenericWrapper("DAMAGE_BLUDGEONING", (i) -> i >= 0, "\uEE62", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.COBBLESTONE));
        registerNewEffect(new GenericWrapper("DAMAGE_LIGHTNING", (i) -> i >= 0, "\uEE63", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.PRISMARINE_SHARD));
        registerNewEffect(new GenericWrapper("DAMAGE_FREEZING", (i) -> i >= 0, "\uEE64", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ICE));
        registerNewEffect(new GenericWrapper("DAMAGE_RADIANT", (i) -> i >= 0, "\uEE65", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLD_INGOT));
        registerNewEffect(new GenericWrapper("DAMAGE_NECROTIC", (i) -> i >= 0, "\uEE66", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BONE));
        registerNewEffect(new GenericWrapper("COOKING_SPEED", (i) -> i >= 0, "\uEE67", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BLAST_FURNACE));
        registerNewEffect(new GenericWrapper("JUMP_HEIGHT", (i) -> i >= 0, "\uEE68", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SLIME_BLOCK));
        registerNewEffect(new GenericWrapper("JUMPS", (i) -> i >= 0, "\uEE69", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.STICKY_PISTON));
        registerNewEffect(new GenericWrapper("EXTRA_EXPLOSION_DAMAGE", (i) -> i >= 0, "\uEE6A", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.TNT, 0.1, 1));
        registerNewEffect(new GenericWrapper("EXTRA_FIRE_DAMAGE", (i) -> i >= 0, "\uEE6B", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.FIRE_CHARGE, 0.1, 1));
        registerNewEffect(new GenericWrapper("EXTRA_MAGIC_DAMAGE", (i) -> i >= 0, "\uEE6C", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.END_CRYSTAL, 0.1, 1));
        registerNewEffect(new GenericWrapper("EXTRA_POISON_DAMAGE", (i) -> i >= 0, "\uEE6D", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.SPIDER_EYE, 0.1, 1));
        registerNewEffect(new GenericWrapper("EXTRA_BLUDGEONING_DAMAGE", (i) -> i >= 0, "\uEE6E", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.COBBLESTONE, 0.1, 1));
        registerNewEffect(new GenericWrapper("EXTRA_LIGHTNING_DAMAGE", (i) -> i >= 0, "\uEE6F", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.PRISMARINE_SHARD, 0.1, 1));
        registerNewEffect(new GenericWrapper("EXTRA_FREEZING_DAMAGE", (i) -> i >= 0, "\uEE70", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.ICE, 0.1, 1));
        registerNewEffect(new GenericWrapper("EXTRA_RADIANT_DAMAGE", (i) -> i >= 0, "\uEE71", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.GOLD_INGOT, 0.1, 1));
        registerNewEffect(new GenericWrapper("EXTRA_NECROTIC_DAMAGE", (i) -> i >= 0, "\uEE72", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.BONE, 0.1, 1));
        registerNewEffect(new GenericWrapper("BLUDGEONING_RESISTANCE", (i) -> i >= 0, "\uEE73", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.COBBLESTONE));
        registerNewEffect(new GenericWrapper("LIGHTNING_RESISTANCE", (i) -> i >= 0, "\uEE74", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.PRISMARINE_SHARD));
        registerNewEffect(new GenericWrapper("FREEZING_RESISTANCE", (i) -> i >= 0, "\uEE75", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ICE));
        registerNewEffect(new GenericWrapper("RADIANT_RESISTANCE", (i) -> i >= 0, "\uEE76", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.GOLD_INGOT));
        registerNewEffect(new GenericWrapper("NECROTIC_RESISTANCE", (i) -> i >= 0, "\uEE77", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BONE));
        registerNewEffect(new Stun("STUN", "\uEE78").addModifier(Material.IRON_BLOCK));
        registerNewEffect(new GenericWrapper("DURABILITY_MULTIPLIER", (i) -> i >= 0, "\uEE79", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.DIAMOND));
        registerNewEffect(new GenericWrapper("ENTITY_DROPS", (i) -> i >= 0, "\uEE7A", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.CHEST));
        registerNewEffect(new GenericWrapper("LIGHT_ARMOR", (i) -> i >= 0, "\uEE7B", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.CHAINMAIL_CHESTPLATE, 0.1, 1));
        registerNewEffect(new GenericWrapper("HEAVY_ARMOR", (i) -> i >= 0, "\uEE7C", StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.IRON_CHESTPLATE, 0.1, 1));
        registerNewEffect(new GenericWrapper("CRIT_CHANCE_RESISTANCE", (i) -> i >= 0, "\uEE7D", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.REDSTONE));
        registerNewEffect(new GenericWrapper("CRIT_DAMAGE_RESISTANCE", (i) -> i >= 0, "\uEE7E", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.REDSTONE));
        registerNewEffect(new Fire("FIRE", "\uEE7F").addModifier(Material.LAVA_BUCKET));
        registerNewEffect(new InstantCustomDamage("INSTANT_EXPLOSION_DAMAGE", "ENTITY_EXPLOSION", "\uEE80").addModifier(Material.TNT, 0.1, 1));
        registerNewEffect(new InstantCustomDamage("INSTANT_FIRE_DAMAGE", "FIRE", "\uEE81").addModifier(Material.FIRE_CHARGE, 0.1, 1));
        registerNewEffect(new InstantCustomDamage("INSTANT_POISON_DAMAGE", "POISON", "\uEE82").addModifier(Material.SPIDER_EYE, 0.1, 1));
        registerNewEffect(new InstantCustomDamage("INSTANT_MAGIC_DAMAGE", "MAGIC", "\uEE83").addModifier(Material.END_CRYSTAL, 0.1, 1));
        registerNewEffect(new InstantCustomDamage("INSTANT_MELEE_DAMAGE", "ENTITY_ATTACK", "\uEE84").addModifier(Material.IRON_SWORD, 0.1, 1));
        registerNewEffect(new InstantCustomDamage("INSTANT_PROJECTILE_DAMAGE", "PROJECTILE", "\uEE85").addModifier(Material.ARROW, 0.1, 1));
        registerNewEffect(new InstantCustomDamage("INSTANT_BLUDGEONING_DAMAGE", "BLUDGEONING", "\uEE86").addModifier(Material.COBBLESTONE, 0.1, 1));
        registerNewEffect(new InstantCustomDamage("INSTANT_LIGHTNING_DAMAGE", "LIGHTNING", "\uEE87").addModifier(Material.PRISMARINE_SHARD, 0.1, 1));
        registerNewEffect(new InstantCustomDamage("INSTANT_FREEZING_DAMAGE", "FREEZE", "\uEE88").addModifier(Material.ICE, 0.1, 1));
        registerNewEffect(new InstantCustomDamage("INSTANT_RADIANT_DAMAGE", "RADIANT", "\uEE89").addModifier(Material.GOLD_INGOT, 0.1, 1));
        registerNewEffect(new InstantCustomDamage("INSTANT_NECROTIC_DAMAGE", "NECROTIC", "\uEE8A").addModifier(Material.BONE, 0.1, 1));
        registerNewEffect(new Bleed("BLEED", "\uEE8B").addModifier(Material.REDSTONE, 0.05, 1));
        registerNewEffect(new CleanseBleed("ANTIBLEED", "\uEE8C").addModifier(Material.PAPER, 0, 0));
        registerNewEffect(new Recall("RECALL", "\uEE8D").addModifier(Material.PAPER, 0, 0));
        registerNewEffect(new InstantCustomHeal("CUSTOM_HEAL", "\uEE8E").addModifier(Material.GLISTERING_MELON_SLICE, 0.1, 1));
        registerNewEffect(new GenericWrapper("LAPIS_SAVE_CHANCE", (i) -> i <= 0, "\uEE8F", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.LAPIS_LAZULI));
        registerNewEffect(new GenericWrapper("ENCHANTING_REFUND_CHANCE", (i) -> i >= 0, "\uEE90", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ENCHANTING_TABLE));
        registerNewEffect(new GenericWrapper("ENCHANTING_REFUND_FRACTION", (i) -> i >= 0, "\uEE91", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.EXPERIENCE_BOTTLE));
        registerNewEffect(new GenericWrapper("LINGERING_DURATION_MULTIPLIER", (i) -> i >= 0, "\uEE92", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.LINGERING_POTION));
        registerNewEffect(new GenericWrapper("LINGERING_RADIUS_MULTIPLIER", (i) -> i >= 0, "\uEE93", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.LINGERING_POTION));
        registerNewEffect(new GenericWrapper("SPLASH_INTENSITY_MINIMUM", (i) -> i >= 0, "\uEE94", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.SPLASH_POTION));
        registerNewEffect(new GenericWrapper("ENTITY_RARE_DROPS", (i) -> i >= 0, "\uEE95", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.CHEST));
        registerNewEffect(new GenericWrapper("DIG_SPEED", (i) -> i >= 0, "\uEE96", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.DIAMOND_PICKAXE));
        // \uEE97 is occupied by mining speed
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5)){
            registerNewEffect(new GenericWrapper("GENERIC_SCALE", (i) -> i >= 0, "\uEE9A", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.RED_MUSHROOM, 0.01, 0.1));
            registerNewEffect(new GenericWrapper("GENERIC_BLOCK_INTERACTION_RANGE", (i) -> i >= 0, "\uEE9B", StatFormat.DIFFERENCE_FLOAT_P2).addModifier(Material.SCAFFOLDING, 0.01, 0.25));
            registerNewEffect(new GenericWrapper("GENERIC_STEP_HEIGHT", (i) -> i >= 0, "\uEE9C", StatFormat.DIFFERENCE_FLOAT_P2).addModifier(Material.RABBIT_FOOT, 0.01, 0.1));
        }
        registerNewEffect(new GenericWrapper("ATTACK_REACH_MULTIPLIER", (i) -> i >= 0, "\uEE9D", StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.ENDER_PEARL));
        registerNewEffect(new GenericWrapper("SHIELD_DISARMING", (i) -> i >= 0, "\uEE9E", StatFormat.DIFFERENCE_TIME_SECONDS_BASE_20_P1).addModifier(Material.NETHERITE_AXE));
        registerNewEffect(new GenericWrapper("LIFE_STEAL", (i) -> i >= 0, "\uEE9F", StatFormat.PERCENTILE_BASE_1_P2).addModifier(Material.GHAST_TEAR));
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_21)){
            registerNewEffect(new GenericWrapper("GENERIC_GRAVITY", (i) -> i <= 0, "\uEEA0",  StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.BEDROCK, 0.01, 0.1));
            registerNewEffect(new GenericWrapper("GENERIC_SAFE_FALL_DISTANCE", (i) -> i >= 0, "\uEEA1",  StatFormat.DIFFERENCE_FLOAT_P1).addModifier(Material.LEATHER_BOOTS, 0.01, 0.1));
            registerNewEffect(new GenericWrapper("GENERIC_FALL_DAMAGE_MULTIPLIER", (i) -> i <= 0, "\uEEA2",  StatFormat.DIFFERENCE_PERCENTILE_BASE_1_P1).addModifier(Material.NETHERITE_BOOTS, 0.01, 0.1));
        }
    }

    public static void reload(){
        registeredEffects.clear();
        registerEffects();
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
        if (meta.getPersistentDataContainer().has(def ? DEFAULT_STORED_EFFECTS : ACTUAL_STORED_EFFECTS, PersistentDataType.STRING)) return parseRawData(getRawData(meta, def));
        else if (meta instanceof PotionMeta p){
            Map<String, PotionTypeEffectWrapper> wrapper = typeToEffectWrappings.get(p.getBasePotionData().getType());
            if (wrapper == null) return new HashMap<>();
            return wrapper.values().stream().map(e -> e.get(p.getBasePotionData())).collect(Collectors.toMap(PotionEffectWrapper::getEffect, e -> e));
        }
        return new HashMap<>();
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
                    double amplifier = StringUtils.parseDouble(args[1]);
                    long duration =  Long.parseLong(args[2]);
                    int charges = args.length > 3 ? Integer.parseInt(args[3]) : -1;
                    if (charges == 0) continue;

                    PotionEffectWrapper wrapper = getEffect(effect);
                    wrapper.setAmplifier(amplifier);
                    wrapper.setDuration(duration);
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
            // sorts effects from shortest to biggest, because if a bigger name matches part of a shorter name
            // its line will be overwritten because the plugin thinks the bigger name belongs to the smaller name effect
            orderedWrappers.sort(Comparator.comparingInt((PotionEffectWrapper a) -> a.getEffectName().length()));
            Collections.reverse(orderedWrappers);
            for (PotionEffectWrapper effect : orderedWrappers){
                if (!defaultEffects.containsKey(effect.getEffect()) || effect.getCharges() == 0) {
                    exclude.add(effect.getEffect());
                    continue;
                }

                if (effect.isVanilla()){
                    // applying a vanilla effect
                    int amplifier = Math.max(0, (int) Math.floor(effect.getAmplifier()));
                    int duration = (int) effect.getDuration();

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
        if (wrapper.getAmplifier() != (wrapper.isVanilla ? -1 : 0)) defaultEffects.put(wrapper.getEffect(), wrapper);
        else defaultEffects.remove(wrapper.getEffect());
        setDefaultStoredEffects(meta, defaultEffects);

        Map<String, PotionEffectWrapper> actualEffects = getStoredEffects(meta, false);
        if (wrapper.getAmplifier() != (wrapper.isVanilla ? -1 : 0)) actualEffects.putIfAbsent(wrapper.getEffect(), wrapper);
        else actualEffects.remove(wrapper.getEffect());

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
        else setActualStoredEffects(meta, stats);
    }

    /**
     * Spends a charge of the given effect. If the item has 1 charge left, the effect is removed from the item.
     * @param meta the item meta to spend a charge on.
     * @param effect the effect to spend a charge of.
     * @return true if a charge was successfully spent, or false if for whatever reason the item had 0 charges left.
     */
    public static boolean spendCharge(ItemMeta meta, String effect){
        PotionEffectWrapper wrapper = getStoredEffect(meta, effect, false);
        if (wrapper == null) return false;
        wrapper = wrapper.copy();
        if (wrapper.getCharges() < 0) return true;
        if (wrapper.getCharges() == 0) return false;
        if (wrapper.getCharges() == 1) {
            removeEffect(meta, effect);
            CustomFlag.removeItemFlag(meta, CustomFlag.TEMPORARY_POTION_DISPLAY);
        } else {
            wrapper.setCharges(wrapper.getCharges() - 1);
            setStoredEffect(meta, wrapper, false);
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
     * Cleans an ItemMeta of custom effects
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
                    double amplifier = StringUtils.parseDouble(args[2].replace(",", "."));
                    if (effectiveUntil != -1 && effectiveUntil < System.currentTimeMillis()) continue; // expired

                    effects.put(effect, new CustomPotionEffect(customEffect, effectiveUntil, amplifier));
                }
            }
        }
        if (effects.isEmpty()) markAsUnaffected(p);

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
                addEffect(e, null, effect, true, 1, EntityPotionEffectEvent.Cause.PLUGIN, EntityPotionEffectEvent.Action.CLEARED);
            }
        } else {
            for (CustomPotionEffect effect : effects){
                addEffect(e, null, effect, true, 1, EntityPotionEffectEvent.Cause.PLUGIN, currentEffects.containsKey(effect.getWrapper().getEffect()) ? EntityPotionEffectEvent.Action.CHANGED : EntityPotionEffectEvent.Action.ADDED);
            }
        }
        EntityCache.resetPotionEffects(e); // adding/removing an effect as a result of this method should reset the entity's potion effect cache
    }

    private static void setActiveEffects(LivingEntity p, Map<String, CustomPotionEffect> effects){
        if (effects == null || effects.isEmpty()) p.getPersistentDataContainer().remove(POTION_EFFECTS);
        else {
            String effect = effects.values().stream()
                    .filter(e -> e.getEffectiveUntil() == -1 || e.getEffectiveUntil() > System.currentTimeMillis())
                    .map(e -> String.format("%s:%d:%.6f", e.getWrapper().getEffect(), e.getEffectiveUntil(), e.getAmplifier()))
                    .collect(Collectors.joining(";"));
            p.getPersistentDataContainer().set(POTION_EFFECTS, PersistentDataType.STRING, effect);
        }
        EntityCache.resetPotionEffects(p);
    }

    /**
     * Adds a new potion effect to the given entity
     * @param e the entity
     * @param effect the effect to add
     * @param force true if overriding old effects, false if the effect shouldn't be cancelled if the previous effect has a stronger amplifier
     * @param cause the cause/reason for adding the effect
     * @param action the action to communicate to the server for effect changes
     */
    public static void addEffect(LivingEntity e, LivingEntity causedBy, CustomPotionEffect effect, boolean force, double intensity, EntityPotionEffectEvent.Cause cause, EntityPotionEffectEvent.Action action){
        if (!registeredEffects.containsKey(effect.getWrapper().getEffect())) {
            ValhallaMMO.logWarning("Attempting to apply custom effect " + effect.getWrapper() + ", but it was not registered");
            return;
        }
        Map<String, CustomPotionEffect> currentEffects = getActiveEffects(e);

        EntityCustomPotionEffectEvent event = new EntityCustomPotionEffectEvent(e, currentEffects.get(effect.getWrapper().getEffect()), effect, cause, action, force);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()){
            CustomPotionEffect newEffect = event.getNewEffect();
            if (newEffect.getWrapper().isInstant()){
                // effect inflicted, but since it's instant it doesn't need to be added
                newEffect.getWrapper().onInflict(e, causedBy, newEffect.getAmplifier(), newEffect.getOriginalDuration(), intensity);
                return;
            }
            // if the effect is not forced, do not apply if the existing effect is stronger than the new effect
            if (!event.isOverride() &&
                    currentEffects.containsKey(newEffect.getWrapper().getEffect()) &&
                    currentEffects.get(newEffect.getWrapper().getEffect()).getAmplifier() > newEffect.getAmplifier()) return;

            // remove if effectiveUntil is 0 or less, unless effectiveUntil is specifically -1 (infinite)
            if (newEffect.getEffectiveUntil() == -1 || newEffect.getEffectiveUntil() > 0){
                currentEffects.put(newEffect.getWrapper().getEffect(), event.getNewEffect());
                newEffect.getWrapper().onInflict(e, causedBy, newEffect.getAmplifier(), newEffect.getOriginalDuration(), intensity);
            } else {
                currentEffects.remove(event.getNewEffect().getWrapper().getEffect());
                newEffect.getWrapper().onExpire(e);
            }
            if (currentEffects.isEmpty()) markAsUnaffected(e);
            else entitiesWithEffects.add(e.getUniqueId());

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
    public static void addEffect(LivingEntity e, LivingEntity causedBy, CustomPotionEffect effect, boolean force, double intensity, EntityPotionEffectEvent.Cause cause){
        Map<String, CustomPotionEffect> currentEffects = getActiveEffects(e);
        EntityPotionEffectEvent.Action action;
        if (currentEffects.containsKey(effect.getWrapper().getEffect())) action = EntityPotionEffectEvent.Action.CHANGED;
        else if (effect.getEffectiveUntil() != -1 && effect.getEffectiveUntil() < 0) action = EntityPotionEffectEvent.Action.REMOVED;
        else action = EntityPotionEffectEvent.Action.ADDED;
        addEffect(e, causedBy, effect, force, intensity, cause, action);
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
                addEffect(e, null, eff, true, 1, cause);
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
                addEffect(e, null, activeEffect, true, 1, EntityPotionEffectEvent.Cause.EXPIRATION, EntityPotionEffectEvent.Action.REMOVED);
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
        if (!registeredEffects.containsKey(effect)) throw new IllegalArgumentException("Custom potion effect " + effect + " does not exist!");
        return registeredEffects.get(effect).copy();
    }

    public static Collection<UUID> affectedEntityTracker() {
        return entitiesWithEffects;
    }

    public static CustomEffectDisplay getCustomEffectDisplay() {
        return customEffectDisplay;
    }

    public static void setCustomEffectDisplay(CustomEffectDisplay customEffectDisplay) {
        PotionEffectRegistry.customEffectDisplay = customEffectDisplay;
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
    public static void updateItemName(ItemMeta meta, boolean override, boolean combined){
        if (meta == null) return;
        Material base = ItemUtils.getStoredType(meta);
        if (base == null) return;
        String c = combined ? "combined_" : "";
        String key = switch (base) {
            case SPLASH_POTION -> "potion_splash_format";
            case LINGERING_POTION -> "potion_lingering_format";
            case TIPPED_ARROW -> "tipped_arrow_format";
            case POTION -> "potion_base_format";
            default -> "item_generic_format";
        };
        String plainFormat = TranslationManager.getTranslation(key);
        String format = TranslationManager.getTranslation(c + key);
        // if the meta has no display name, override is enabled, or the display name already contains the format it was previously, set the new display name
        if (!meta.hasDisplayName() || override ||
                ChatColor.stripColor(meta.getDisplayName()).contains(ChatColor.stripColor(Utils.chat(plainFormat.replace("%effect%", ""))))){
            Map<String, PotionEffectWrapper> effects = getStoredEffects(meta, true);
            if (effects.isEmpty()) return;
            PotionEffectWrapper effectForName = effects.values().stream().findAny().orElse(null);
            String effectName = effectForName.getPotionName();
            meta.setDisplayName(Utils.chat(format.replace("%icon%", effectForName.getEffectIcon()).replace("%effect%", effectName).replace("%item%", ItemUtils.getItemName(meta))));
        }
    }

    private static class PotionTypeEffectWrapper {
        private final String potionEffectType;
        private int durationBase = 0;
        private int durationUpgraded = 0;
        private int durationExtended = 0;
        private int amplifierBase = 0;
        private int amplifierUpgraded = 0;

        public PotionTypeEffectWrapper(String type){
            this.potionEffectType = type;
        }

        public PotionTypeEffectWrapper dB(int duration){
            this.durationBase = duration * 20;
            return this;
        }

        public PotionTypeEffectWrapper dUp(int duration){
            this.durationUpgraded = duration * 20;
            return this;
        }

        public PotionTypeEffectWrapper dEx(int duration){
            this.durationExtended = duration * 20;
            return this;
        }

        public PotionTypeEffectWrapper aB(int amplifier){
            this.amplifierBase = amplifier * 20;
            return this;
        }

        public PotionTypeEffectWrapper aU(int amplifier){
            this.amplifierUpgraded = amplifier * 20;
            return this;
        }

        public PotionEffectWrapper get(PotionData data){
            return getEffect(potionEffectType).setAmplifier(data.isUpgraded() ? amplifierUpgraded : amplifierBase).setDuration(data.isExtended() ? durationExtended : data.isUpgraded() ? durationUpgraded : durationBase);
        }
    }
}
