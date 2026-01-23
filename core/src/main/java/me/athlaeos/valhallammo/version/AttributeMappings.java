package me.athlaeos.valhallammo.version;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import org.bukkit.attribute.Attribute;

import java.util.HashMap;
import java.util.Map;

// Exists because attributes have been changed very frequently over the past couple versions (yay!!)
public enum AttributeMappings {
    ARMOR("GENERIC_ARMOR", "GENERIC_ARMOR", "GENERIC_ARMOR", "ARMOR", "minecraft:armor"),
    ARMOR_TOUGHNESS("GENERIC_ARMOR_TOUGHNESS", "GENERIC_ARMOR_TOUGHNESS", "GENERIC_ARMOR_TOUGHNESS", "ARMOR_TOUGHNESS", "minecraft:armor_toughness"),
    ATTACK_DAMAGE("GENERIC_ATTACK_DAMAGE", "GENERIC_ATTACK_DAMAGE", "GENERIC_ATTACK_DAMAGE", "ATTACK_DAMAGE", "minecraft:attack_damage"),
    ATTACK_KNOCKBACK("GENERIC_ATTACK_KNOCKBACK", "GENERIC_ATTACK_KNOCKBACK", "GENERIC_ATTACK_KNOCKBACK", "ATTACK_KNOCKBACK", "minecraft:attack_knockback"),
    ATTACK_SPEED("GENERIC_ATTACK_SPEED", "GENERIC_ATTACK_SPEED", "GENERIC_ATTACK_SPEED", "ATTACK_SPEED", "minecraft:attack_speed"),
    BLOCK_BREAK_SPEED(null, "PLAYER_BLOCK_BREAK_SPEED", "PLAYER_BLOCK_BREAK_SPEED", "BLOCK_BREAK_SPEED", "minecraft:block_break_speed"),
    BLOCK_INTERACTION_RANGE(null, "PLAYER_BLOCK_INTERACTION_RANGE", "PLAYER_BLOCK_INTERACTION_RANGE", "BLOCK_INTERACTION_RANGE", "minecraft:block_interaction_range"),
    BURNING_TIME(null, null, "GENERIC_BURNING_TIME", "BURNING_TIME", "minecraft:burning_time"),
    ENTITY_INTERACTION_RANGE(null, "PLAYER_ENTITY_INTERACTION_RANGE", "PLAYER_ENTITY_INTERACTION_RANGE", "ENTITY_INTERACTION_RANGE", "minecraft:entity_interaction_range"),
    EXPLOSION_KNOCKBACK_RESISTANCE(null, "GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE", "GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE", "EXPLOSION_KNOCKBACK_RESISTANCE", "minecraft:explosion_knockback_resistance"),
    FALL_DAMAGE_MULTIPLIER(null, "GENERIC_FALL_DAMAGE_MULTIPLIER", "GENERIC_FALL_DAMAGE_MULTIPLIER", "FALL_DAMAGE_MULTIPLIER", "minecraft:fall_damage_multiplier"),
    FLYING_SPEED("GENERIC_FLYING_SPEED", "GENERIC_FLYING_SPEED", "GENERIC_FLYING_SPEED", "FLYING_SPEED", "minecraft:flying_speed"),
    FOLLOW_RANGE("GENERIC_FOLLOW_RANGE", "GENERIC_FOLLOW_RANGE", "GENERIC_FOLLOW_RANGE", "FOLLOW_RANGE", "minecraft:follow_range"),
    GRAVITY(null, "GENERIC_GRAVITY", "GENERIC_GRAVITY", "GRAVITY", "minecraft:gravity"),
    JUMP_STRENGTH(null, "GENERIC_JUMP_STRENGTH", "GENERIC_JUMP_STRENGTH", "JUMP_STRENGTH", "minecraft:jump_strength"),
    KNOCKBACK_RESISTANCE("GENERIC_KNOCKBACK_RESISTANCE", "GENERIC_KNOCKBACK_RESISTANCE", "GENERIC_KNOCKBACK_RESISTANCE", "KNOCKBACK_RESISTANCE", "minecraft:knockback_resistance"),
    LUCK("GENERIC_LUCK", "GENERIC_LUCK", "GENERIC_LUCK", "LUCK", "minecraft:luck"),
    MAX_ABSORPTION(null, "GENERIC_MAX_ABSORPTION", "GENERIC_MAX_ABSORPTION", "MAX_ABSORPTION", "minecraft:max_absorption"),
    MAX_HEALTH("GENERIC_MAX_HEALTH", "GENERIC_MAX_HEALTH", "GENERIC_MAX_HEALTH", "MAX_HEALTH", "minecraft:max_health"),
    MINING_EFFICIENCY(null, "PLAYER_MINING_EFFICIENCY", "PLAYER_MINING_EFFICIENCY", "MINING_EFFICIENCY", "minecraft:mining_efficiency"),
    MOVEMENT_EFFICIENCY(null, "GENERIC_MOVEMENT_EFFICIENCY", "GENERIC_MOVEMENT_EFFICIENCY", "MOVEMENT_EFFICIENCY", "minecraft:movement_efficiency"),
    MOVEMENT_SPEED("GENERIC_MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED", "MOVEMENT_SPEED", "minecraft:movement_speed"),
    OXYGEN_BONUS(null, "GENERIC_OXYGEN_BONUS", "GENERIC_OXYGEN_BONUS", "OXYGEN_BONUS", "minecraft:oxygen_bonus"),
    SAFE_FALL_DISTANCE(null, "GENERIC_SAFE_FALL_DISTANCE", "GENERIC_SAFE_FALL_DISTANCE", "SAFE_FALL_DISTANCE", "minecraft:safe_fall_distance"),
    SCALE(null, "GENERIC_SCALE", "GENERIC_SCALE", "SCALE", "minecraft:scale"),
    SNEAKING_SPEED(null, "PLAYER_SNEAKING_SPEED", "PLAYER_SNEAKING_SPEED", "SNEAKING_SPEED", "minecraft:sneaking_speed"),
    SPAWN_REINFORCEMENTS("ZOMBIE_SPAWN_REINFORCEMENTS", "ZOMBIE_SPAWN_REINFORCEMENTS", "ZOMBIE_SPAWN_REINFORCEMENTS", "SPAWN_REINFORCEMENTS", "minecraft:spawn_reinforcements"),
    STEP_HEIGHT(null, "GENERIC_STEP_HEIGHT", "GENERIC_STEP_HEIGHT", "STEP_HEIGHT", "minecraft:step_height"),
    SUBMERGED_MINING_SPEED(null, "PLAYER_SUBMERGED_MINING_SPEED", "PLAYER_SUBMERGED_MINING_SPEED", "SUBMERGED_MINING_SPEED", "minecraft:submerged_mining_speed"),
    SWEEPING_DAMAGE_RATIO(null, "PLAYER_SWEEPING_DAMAGE_RATIO", "PLAYER_SWEEPING_DAMAGE_RATIO", "SWEEPING_DAMAGE_RATIO", "minecraft:sweeping_damage_ratio"),
    TEMPT_RANGE(null, null, null, "TEMPT_RANGE", "minecraft:tempt_range"),
    WATER_MOVEMENT_EFFICIENCY(null, "GENERIC_WATER_MOVEMENT_EFFICIENCY", "GENERIC_WATER_MOVEMENT_EFFICIENCY", "WATER_MOVEMENT_EFFICIENCY", "minecraft:water_movement_efficiency"),
    HORSE_JUMP_STRENGTH("HORSE_JUMP_STRENGTH", "GENERIC_JUMP_STRENGTH", "GENERIC_JUMP_STRENGTH", "JUMP_STRENGTH", "minecraft:jump_strength");
    static{
        for (AttributeMappings mapping : values()) registerToMap(mapping);
    }

    private static Map<String, AttributeMappings> oldAndNewEffects;
    private final String attribute1_19;
    private final String attribute1_20_5;
    private final String attribute1_21;
    private final String attributeNew;
    private final String attributeKey;

    AttributeMappings(String attribute1_19, String attribute1_20_5, String attribute1_21, String attributeNew, String attributeKey){
        this.attribute1_19 = attribute1_19;
        this.attribute1_20_5 = attribute1_20_5;
        this.attribute1_21 = attribute1_21;
        this.attributeNew = attributeNew;
        this.attributeKey = attributeKey;
    }

    private static void registerToMap(AttributeMappings mapping){
        if (oldAndNewEffects == null) oldAndNewEffects = new HashMap<>();
        oldAndNewEffects.put(mapping.attribute1_19, mapping);
        oldAndNewEffects.put(mapping.attribute1_20_5, mapping);
        oldAndNewEffects.put(mapping.attribute1_21, mapping);
        oldAndNewEffects.put(mapping.attributeNew, mapping);
        oldAndNewEffects.put(mapping.attributeKey, mapping);
    }

    public String currentEffectName(){
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_21_3)) return attributeNew;
        else if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_21)) return attribute1_21;
        else if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5)) return attribute1_20_5;
        else return attribute1_19;
    }

    public static AttributeMappings getAttributeMapping(String effect){
        return oldAndNewEffects.get(effect);
    }

    public static Attribute getAttribute(String attribute){
        if (attribute == null) return null;
        if (oldAndNewEffects.containsKey(attribute)) return oldAndNewEffects.get(attribute).getAttribute();
        return null;
    }

    public String getAttribute1_19() {
        return attribute1_19;
    }

    public String getAttribute1_20_5() {
        return attribute1_20_5;
    }

    public String getAttribute1_21() {
        return attribute1_21;
    }

    public String getAttributeNew() {
        return attributeNew;
    }

    public String getAttributeKey() {
        return attributeKey;
    }

    /**
     * Returns the version-specific attribute for the given mapping. <br>
     * May be null if attribute doesn't exist yet
     */
    public Attribute getAttribute(){
        return ValhallaMMO.getNms().getAttribute(this);
    }
}
