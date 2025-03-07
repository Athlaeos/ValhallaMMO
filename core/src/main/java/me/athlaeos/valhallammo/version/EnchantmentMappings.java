package me.athlaeos.valhallammo.version;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.enchantments.Enchantment;

// Exists because enchantment naming conventions were changed in 1.20.5+
public enum EnchantmentMappings {
    FLAME,
    POWER,
    INFINITY,
    PUNCH,
    CURSE_OF_BINDING,
    CHANNELING,
    SHARPNESS,
    BANE_OF_ARTHROPODS,
    SMITE,
    DEPTH_STRIDER,
    EFFICIENCY,
    UNBREAKING,
    FIRE_ASPECT,
    FROST_WALKER,
    IMPALING,
    KNOCKBACK,
    FORTUNE,
    LOOTING,
    LOYALTY,
    LUCK_OF_THE_SEA,
    LURE,
    MENDING,
    MULTISHOT,
    RESPIRATION,
    PIERCING,
    PROTECTION,
    BLAST_PROTECTION,
    FEATHER_FALLING,
    FIRE_PROTECTION,
    PROJECTILE_PROTECTION,
    QUICK_CHARGE,
    RIPTIDE,
    SILK_TOUCH,
    SOUL_SPEED,
    SWEEPING_EDGE,
    THORNS,
    CURSE_OF_VANISHING,
    AQUA_AFFINITY,
    BREACH,
    SWIFT_SNEAK,
    DENSITY,
    WIND_BURST;

    /**
     * Returns the version-specific enchantment for the given mapping. <br>
     * May be null if enchantment doesn't exist yet
     */
    public Enchantment getEnchantment(){
        return ValhallaMMO.getNms().getEnchantment(this);
    }
}
