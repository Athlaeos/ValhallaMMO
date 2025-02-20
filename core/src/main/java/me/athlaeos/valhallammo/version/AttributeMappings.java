package me.athlaeos.valhallammo.version;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import org.bukkit.attribute.Attribute;

import java.util.HashMap;
import java.util.Map;

// Exists because attributes have been changed very frequently over the past couple versions (yay!!)
public enum AttributeMappings {
    ARMOR("GENERIC_ARMOR", "GENERIC_ARMOR", "GENERIC_ARMOR", "ARMOR"),
    ARMOR_TOUGHNESS("GENERIC_ARMOR_TOUGHNESS", "GENERIC_ARMOR_TOUGHNESS", "GENERIC_ARMOR_TOUGHNESS", "ARMOR_TOUGHNESS"),
    ATTACK_DAMAGE("GENERIC_ATTACK_DAMAGE", "GENERIC_ATTACK_DAMAGE", "GENERIC_ATTACK_DAMAGE", "ATTACK_DAMAGE"),
    ATTACK_KNOCKBACK("GENERIC_ATTACK_KNOCKBACK", "GENERIC_ATTACK_KNOCKBACK", "GENERIC_ATTACK_KNOCKBACK", "ATTACK_KNOCKBACK"),
    ATTACK_SPEED("GENERIC_ATTACK_SPEED", "GENERIC_ATTACK_SPEED", "GENERIC_ATTACK_SPEED", "ATTACK_SPEED"),
    BLOCK_BREAK_SPEED(null, "PLAYER_BLOCK_BREAK_SPEED", "PLAYER_BLOCK_BREAK_SPEED", "BLOCK_BREAK_SPEED"),
    BLOCK_INTERACTION_RANGE(null, "PLAYER_BLOCK_INTERACTION_RANGE", "PLAYER_BLOCK_INTERACTION_RANGE", "BLOCK_INTERACTION_RANGE"),
    BURNING_TIME(null, null, "GENERIC_BURNING_TIME", "BURNING_TIME"),
    ENTITY_INTERACTION_RANGE(null, "PLAYER_ENTITY_INTERACTION_RANGE", "PLAYER_ENTITY_INTERACTION_RANGE", "ENTITY_INTERACTION_RANGE"),
    EXPLOSION_KNOCKBACK_RESISTANCE(null, "GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE", "GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE", "EXPLOSION_KNOCKBACK_RESISTANCE"),
    FALL_DAMAGE_MULTIPLIER(null, "GENERIC_FALL_DAMAGE_MULTIPLIER", "GENERIC_FALL_DAMAGE_MULTIPLIER", "FALL_DAMAGE_MULTIPLIER"),
    FLYING_SPEED("GENERIC_FLYING_SPEED", "GENERIC_FLYING_SPEED", "GENERIC_FLYING_SPEED", "FLYING_SPEED"),
    FOLLOW_RANGE("GENERIC_FOLLOW_RANGE", "GENERIC_FOLLOW_RANGE", "GENERIC_FOLLOW_RANGE", "FOLLOW_RANGE"),
    GRAVITY(null, "GENERIC_GRAVITY", "GENERIC_GRAVITY", "GRAVITY"),
    JUMP_STRENGTH(null, "GENERIC_JUMP_STRENGTH", "GENERIC_JUMP_STRENGTH", "JUMP_STRENGTH"),
    KNOCKBACK_RESISTANCE("GENERIC_KNOCKBACK_RESISTANCE", "GENERIC_KNOCKBACK_RESISTANCE", "GENERIC_KNOCKBACK_RESISTANCE", "KNOCKBACK_RESISTANCE"),
    LUCK("GENERIC_LUCK", "GENERIC_LUCK", "GENERIC_LUCK", "LUCK"),
    MAX_ABSORPTION(null, "GENERIC_MAX_ABSORPTION", "GENERIC_MAX_ABSORPTION", "MAX_ABSORPTION"),
    MAX_HEALTH("GENERIC_MAX_HEALTH", "GENERIC_MAX_HEALTH", "GENERIC_MAX_HEALTH", "MAX_HEALTH"),
    MINING_EFFICIENCY(null, "PLAYER_MINING_EFFICIENCY", "PLAYER_MINING_EFFICIENCY", "MINING_EFFICIENCY"),
    MOVEMENT_EFFICIENCY(null, "GENERIC_MOVEMENT_EFFICIENCY", "GENERIC_MOVEMENT_EFFICIENCY", "MOVEMENT_EFFICIENCY"),
    MOVEMENT_SPEED("GENERIC_MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED", "MOVEMENT_SPEED"),
    OXYGEN_BONUS(null, "GENERIC_OXYGEN_BONUS", "GENERIC_OXYGEN_BONUS", "OXYGEN_BONUS"),
    SAFE_FALL_DISTANCE(null, "GENERIC_SAFE_FALL_DISTANCE", "GENERIC_SAFE_FALL_DISTANCE", "SAFE_FALL_DISTANCE"),
    SCALE(null, "GENERIC_SCALE", "GENERIC_SCALE", "SCALE"),
    SNEAKING_SPEED(null, "PLAYER_SNEAKING_SPEED", "PLAYER_SNEAKING_SPEED", "SNEAKING_SPEED"),
    SPAWN_REINFORCEMENTS("ZOMBIE_SPAWN_REINFORCEMENTS", "ZOMBIE_SPAWN_REINFORCEMENTS", "ZOMBIE_SPAWN_REINFORCEMENTS", "SPAWN_REINFORCEMENTS"),
    STEP_HEIGHT(null, "GENERIC_STEP_HEIGHT", "GENERIC_STEP_HEIGHT", "STEP_HEIGHT"),
    SUBMERGED_MINING_SPEED(null, "PLAYER_SUBMERGED_MINING_SPEED", "PLAYER_SUBMERGED_MINING_SPEED", "SUBMERGED_MINING_SPEED"),
    SWEEPING_DAMAGE_RATIO(null, "PLAYER_SWEEPING_DAMAGE_RATIO", "PLAYER_SWEEPING_DAMAGE_RATIO", "SWEEPING_DAMAGE_RATIO"),
    TEMPT_RANGE(null, null, null, "TEMPT_RANGE"),
    WATER_MOVEMENT_EFFICIENCY(null, "GENERIC_WATER_MOVEMENT_EFFICIENCY", "GENERIC_WATER_MOVEMENT_EFFICIENCY", "WATER_MOVEMENT_EFFICIENCY"),
    HORSE_JUMP_STRENGTH("HORSE_JUMP_STRENGTH", "GENERIC_JUMP_STRENGTH", "GENERIC_JUMP_STRENGTH", "JUMP_STRENGTH"),;
    static{
        for (AttributeMappings mapping : values()) registerToMap(mapping);
    }

    private static Map<String, AttributeMappings> oldAndNewEffects;
    private final String attribute1_19;
    private final String attribute1_20_5;
    private final String attribute1_21;
    private final String attributeNew;

    AttributeMappings(String attribute1_19, String attribute1_20_5, String attribute1_21, String attributeNew){
        this.attribute1_19 = attribute1_19;
        this.attribute1_20_5 = attribute1_20_5;
        this.attribute1_21 = attribute1_21;
        this.attributeNew = attributeNew;
    }

    private static void registerToMap(AttributeMappings mapping){
        if (oldAndNewEffects == null) oldAndNewEffects = new HashMap<>();
        oldAndNewEffects.put(mapping.attribute1_19, mapping);
        oldAndNewEffects.put(mapping.attribute1_20_5, mapping);
        oldAndNewEffects.put(mapping.attribute1_21, mapping);
        oldAndNewEffects.put(mapping.attributeNew, mapping);
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

    /**
     * Returns the version-specific attribute for the given mapping. <br>
     * May be null if attribute doesn't exist yet
     */
    public Attribute getAttribute(){
        return ValhallaMMO.getNms().getAttribute(this);
    }
}
