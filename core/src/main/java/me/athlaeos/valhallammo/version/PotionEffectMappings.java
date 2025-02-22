package me.athlaeos.valhallammo.version;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

import static me.athlaeos.valhallammo.utility.Utils.oldOrNew;

// Exists because enchantment naming conventions were changed in 1.20.5+
public enum PotionEffectMappings {
    SPEED("SPEED", "SWIFTNESS"),
    SLOWNESS("SLOW", "SLOWNESS"),
    HASTE("FAST_DIGGING", "HASTE"),
    MINING_FATIGUE("SLOW_DIGGING", "MINING_FATIGUE"),
    STRENGTH("INCREASE_DAMAGE", "STRENGTH"),
    INSTANT_HEALTH("HEAL", "INSTANT_HEALTH"),
    INSTANT_DAMAGE("HARM", "INSTANT_DAMAGE"),
    JUMP_BOOST("JUMP", "JUMP_BOOST"),
    NAUSEA("CONFUSION", "NAUSEA"),
    REGENERATION("REGENERATION", "REGENERATION"),
    RESISTANCE("DAMAGE_RESISTANCE", "RESISTANCE"),
    FIRE_RESISTANCE("FIRE_RESISTANCE", "FIRE_RESISTANCE"),
    WATER_BREATHING("WATER_BREATHING", "WATER_BREATHING"),
    INVISIBILITY("INVISIBILITY", "INVISIBILITY"),
    BLINDNESS("BLINDNESS", "BLINDNESS"),
    NIGHT_VISION("NIGHT_VISION", "NIGHT_VISION"),
    HUNGER("HUNGER", "HUNGER"),
    WEAKNESS("WEAKNESS", "WEAKNESS"),
    POISON("POISON", "POISON"),
    WITHER("WITHER", "WITHER"),
    HEALTH_BOOST("HEALTH_BOOST", "HEALTH_BOOST"),
    ABSORPTION("ABSORPTION", "ABSORPTION"),
    SATURATION("SATURATION", "SATURATION"),
    LEVITATION("LEVITATION", "LEVITATION"),
    SLOW_FALLING("SLOW_FALLING", "SLOW_FALLING"),
    CONDUIT_POWER("CONDUIT_POWER", "CONDUIT_POWER"),
    GLOWING("GLOWING", "GLOWING"),
    LUCK("LUCK", "LUCK"),
    BAD_LUCK("UNLUCK", "UNLUCK"),
    DOLPHINS_GRACE("DOLPHINS_GRACE", "DOLPHINS_GRACE"),
    BAD_OMEN("BAD_OMEN", "BAD_OMEN"),
    HERO_OF_THE_VILLAGE("HERO_OF_THE_VILLAGE", "HERO_OF_THE_VILLAGE"),
    DARKNESS("DARKNESS", "DARKNESS"),
    TRIAL_OMEN("TRIAL_OMEN", "TRIAL_OMEN"),
    RAID_OMEN("RAID_OMEN", "RAID_OMEN"),
    WIND_CHARGED("WIND_CHARGED", "WIND_CHARGED"),
    WEAVING("WEAVING", "WEAVING"),
    OOZING("OOZING", "OOZING"),
    INFESTED("INFESTED", "INFESTED");
    static{
        for (PotionEffectMappings mapping : values()) registerToMap(mapping);
    }

    private static Map<String, PotionEffectMappings> oldAndNewEffects;
    private final String oldEffect;
    private final String newEffect;

    PotionEffectMappings(String oldEffect, String newEffect){
        this.oldEffect = oldEffect;
        this.newEffect = newEffect;
    }

    private static void registerToMap(PotionEffectMappings mapping){
        if (oldAndNewEffects == null) oldAndNewEffects = new HashMap<>();
        oldAndNewEffects.put(mapping.oldEffect, mapping);
        oldAndNewEffects.put(mapping.newEffect, mapping);
    }

    public String currentEffectName(){
        return oldOrNew(oldEffect, newEffect);
    }

    public static PotionEffectMappings getEffect(String effect){
        return oldAndNewEffects.get(effect);
    }

    public static PotionEffectType getPotionEffectType(String effect){
        if (oldAndNewEffects.containsKey(effect)) return oldAndNewEffects.get(effect).getPotionEffectType();
        return null;
    }

    public String getOldEffect() {
        return oldEffect;
    }

    public String getNewEffect() {
        return newEffect;
    }

    /**
     * Returns the version-specific potion effect type for the given mapping. <br>
     * May be null if potion effect doesn't exist yet
     */
    public PotionEffectType getPotionEffectType(){
        return ValhallaMMO.getNms().getPotionEffectType(this);
    }
}
