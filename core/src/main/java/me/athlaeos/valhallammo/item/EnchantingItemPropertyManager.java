package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Scaling;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;

import java.util.HashMap;
import java.util.Map;

public class EnchantingItemPropertyManager {
    private static final Map<Enchantment, Scaling> enchantmentScaling = new HashMap<>();

    static {
        String config = "skills/enchanting.yml";
        registerScaling(EnchantmentMappings.POWER.getEnchantment(), Scaling.fromConfig(config, "scalings.power"));
        registerScaling(EnchantmentMappings.PUNCH.getEnchantment(), Scaling.fromConfig(config, "scalings.punch"));
        registerScaling(EnchantmentMappings.SHARPNESS.getEnchantment(), Scaling.fromConfig(config, "scalings.sharpness"));
        registerScaling(EnchantmentMappings.BANE_OF_ARTHROPODS.getEnchantment(), Scaling.fromConfig(config, "scalings.bane_of_arthropods"));
        registerScaling(EnchantmentMappings.SMITE.getEnchantment(), Scaling.fromConfig(config, "scalings.smite"));
        registerScaling(EnchantmentMappings.DEPTH_STRIDER.getEnchantment(), Scaling.fromConfig(config, "scalings.depth_strider"));
        registerScaling(EnchantmentMappings.EFFICIENCY.getEnchantment(), Scaling.fromConfig(config, "scalings.efficiency"));
        registerScaling(EnchantmentMappings.UNBREAKING.getEnchantment(), Scaling.fromConfig(config, "scalings.unbreaking"));
        registerScaling(EnchantmentMappings.FIRE_ASPECT.getEnchantment(), Scaling.fromConfig(config, "scalings.fire_aspect"));
        registerScaling(EnchantmentMappings.FROST_WALKER.getEnchantment(), Scaling.fromConfig(config, "scalings.frost_walker"));
        registerScaling(EnchantmentMappings.IMPALING.getEnchantment(), Scaling.fromConfig(config, "scalings.impaling"));
        registerScaling(EnchantmentMappings.KNOCKBACK.getEnchantment(), Scaling.fromConfig(config, "scalings.knockback"));
        registerScaling(EnchantmentMappings.FORTUNE.getEnchantment(), Scaling.fromConfig(config, "scalings.fortune"));
        registerScaling(EnchantmentMappings.LOOTING.getEnchantment(), Scaling.fromConfig(config, "scalings.looting"));
        registerScaling(EnchantmentMappings.LOYALTY.getEnchantment(), Scaling.fromConfig(config, "scalings.loyalty"));
        registerScaling(EnchantmentMappings.LUCK_OF_THE_SEA.getEnchantment(), Scaling.fromConfig(config, "scalings.luck"));
        registerScaling(EnchantmentMappings.LURE.getEnchantment(), Scaling.fromConfig(config, "scalings.lure"));
        registerScaling(EnchantmentMappings.RESPIRATION.getEnchantment(), Scaling.fromConfig(config, "scalings.respiration"));
        registerScaling(EnchantmentMappings.PIERCING.getEnchantment(), Scaling.fromConfig(config, "scalings.piercing"));
        registerScaling(EnchantmentMappings.PROTECTION.getEnchantment(), Scaling.fromConfig(config, "scalings.protection"));
        registerScaling(EnchantmentMappings.PROJECTILE_PROTECTION.getEnchantment(), Scaling.fromConfig(config, "scalings.projectile_protection"));
        registerScaling(EnchantmentMappings.BLAST_PROTECTION.getEnchantment(), Scaling.fromConfig(config, "scalings.blast_protection"));
        registerScaling(EnchantmentMappings.FIRE_PROTECTION.getEnchantment(), Scaling.fromConfig(config, "scalings.fire_protection"));
        registerScaling(EnchantmentMappings.FEATHER_FALLING.getEnchantment(), Scaling.fromConfig(config, "scalings.feather_falling"));
        registerScaling(EnchantmentMappings.QUICK_CHARGE.getEnchantment(), Scaling.fromConfig(config, "scalings.quick_charge"));
        registerScaling(EnchantmentMappings.RIPTIDE.getEnchantment(), Scaling.fromConfig(config, "scalings.riptide"));
        registerScaling(EnchantmentMappings.SOUL_SPEED.getEnchantment(), Scaling.fromConfig(config, "scalings.soul_speed"));
        registerScaling(EnchantmentMappings.SWEEPING_EDGE.getEnchantment(), Scaling.fromConfig(config, "scalings.sweeping"));
        registerScaling(EnchantmentMappings.THORNS.getEnchantment(), Scaling.fromConfig(config, "scalings.thorns"));
        registerScaling(EnchantmentMappings.SWIFT_SNEAK.getEnchantment(), Scaling.fromConfig(config, "scalings.swift_sneak"));
        registerScaling(EnchantmentMappings.BREACH.getEnchantment(), Scaling.fromConfig(config, "scalings.breach"));
        registerScaling(EnchantmentMappings.WIND_BURST.getEnchantment(), Scaling.fromConfig(config, "scalings.wind_burst"));
        registerScaling(EnchantmentMappings.DENSITY.getEnchantment(), Scaling.fromConfig(config, "scalings.density"));
    }

    /**
     * Registers an enchantment scalings for a custom enchantment type.
     * @param e the enchantment to set its amplifier scalings for
     * @param scaling the scaling formula for the material
     */
    public static void registerScaling(Enchantment e, Scaling scaling){
        if (e == null) return;
        if (scaling == null) {
            ValhallaMMO.logWarning("Could not register scaling for " + e + ", it's not a valid enchantment!");
            return;
        }
        enchantmentScaling.put(e, scaling);
    }

    public static Scaling getScaling(Enchantment enchantment){
        return enchantmentScaling.get(enchantment);
    }

    /**
     * Scales an enchantment level according to the given amount of skill, returns the new level for the enchantment.
     * If no scaling is registered for the enchantment, or the max level is 1, then this same level is returned.
     * @param enchantment the enchantment to scale
     * @param skill the amount of skill to scale with
     * @param originalLevel the original level of the enchantment
     * @return the scaled enchantment level of the given enchantment
     */
    public static int getScaledLevel(Enchantment enchantment, int skill, int originalLevel){
        Scaling scaling = getScaling(enchantment);
        if (scaling == null || enchantment.getMaxLevel() <= 1) return originalLevel;
        return (int) Math.max(1, Math.floor(scaling.evaluate(scaling.getExpression().replace("%rating%", String.valueOf(skill)), originalLevel)));
    }

    /**
     * Scales an enchanting session's enchantment offers according to the given skill points
     * @param skill the amount of skill points to use with the item's enchantment scaling
     * @param enchantments the enchantment offers to scale
     */
    public static void scaleEnchantmentOffers(int skill, EnchantmentOffer[] enchantments, double chanceForApplication){
        for (EnchantmentOffer offer : enchantments) {
            if (offer == null) continue;
            if (Utils.proc(chanceForApplication, 0, false)){ // this proc does not utilize the entity's luck because the outcome can be positive or negative depending on skill level
                offer.setEnchantmentLevel(getScaledLevel(offer.getEnchantment(), skill, offer.getEnchantmentLevel()));
            }
        }
    }

    public static int getScaledAnvilLevel(Enchantment enchantment, int skill){
        return getScaledLevel(enchantment, skill, enchantment.getMaxLevel());
    }
}