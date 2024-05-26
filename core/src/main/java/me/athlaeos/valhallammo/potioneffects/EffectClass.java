package me.athlaeos.valhallammo.potioneffects;

import me.athlaeos.valhallammo.version.PotionEffectMappings;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.HashSet;

public enum EffectClass {
    BUFF("ABSORPTION", "CONDUIT_POWER", "DAMAGE_RESISTANCE", "DOLPHINS_GRACE", "FAST_DIGGING", "FIRE_RESISTANCE",
            "HEAL", "HEALTH_BOOST", "HERO_OF_THE_VILLAGE", "INCREASE_DAMAGE", "INVISIBILITY", "JUMP_BOOST",
            "LUCK", "NIGHT_VISION", "REGENERATION", "SATURATION", "SPEED", "WATER_BREATHING"),
    DEBUFF("WITHER", "UNLUCK", "WEAKNESS", "SLOW_DIGGING", "SLOW", "HUNGER", "HARM",
            "CONFUSION", "BLINDNESS", "LEVITATION", "POISON", "DARKNESS"),
    NEUTRAL("SLOW_FALLING", "GLOWING", "BAD_OMEN");

    private final Collection<PotionEffectType> vanillaEffects = new HashSet<>();

    EffectClass(String... vanillaEffects){
        for (String effect : vanillaEffects) {
            PotionEffectType type = PotionEffectMappings.getPotionEffectType(effect);
            if (type == null) continue;
            this.vanillaEffects.add(type);
        }
    }

    public Collection<PotionEffectType> getVanillaEffects() {
        return vanillaEffects;
    }

    public static EffectClass getClass(PotionEffectType m){
        for (EffectClass potionType : EffectClass.values()){
            if (potionType.getVanillaEffects().contains(m)){
                return potionType;
            }
        }
        return null;
    }
}
