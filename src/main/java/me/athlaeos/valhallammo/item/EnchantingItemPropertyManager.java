package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.dom.Scaling;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;

import java.util.HashMap;
import java.util.Map;

public class EnchantingItemPropertyManager {
    private static final Map<Enchantment, Scaling> enchantmentScaling = new HashMap<>();

    static {
        String config = "skills/enchanting.yml";
        registerScaling("power", Scaling.fromConfig(config, "scaling_amplifier.power"));
        registerScaling("punch", Scaling.fromConfig(config, "scaling_amplifier.punch"));
        registerScaling("sharpness", Scaling.fromConfig(config, "scaling_amplifier.sharpness"));
        registerScaling("bane_of_arthropods", Scaling.fromConfig(config, "scaling_amplifier.bane_of_arthropods"));
        registerScaling("smite", Scaling.fromConfig(config, "scaling_amplifier.smite"));
        registerScaling("depth_strider", Scaling.fromConfig(config, "scaling_amplifier.depth_strider"));
        registerScaling("efficiency", Scaling.fromConfig(config, "scaling_amplifier.efficiency"));
        registerScaling("unbreaking", Scaling.fromConfig(config, "scaling_amplifier.unbreaking"));
        registerScaling("fire_aspect", Scaling.fromConfig(config, "scaling_amplifier.fire_aspect"));
        registerScaling("frost_walker", Scaling.fromConfig(config, "scaling_amplifier.frost_walker"));
        registerScaling("impaling", Scaling.fromConfig(config, "scaling_amplifier.impaling"));
        registerScaling("knockback", Scaling.fromConfig(config, "scaling_amplifier.knockback"));
        registerScaling("fortune", Scaling.fromConfig(config, "scaling_amplifier.fortune"));
        registerScaling("looting", Scaling.fromConfig(config, "scaling_amplifier.looting"));
        registerScaling("loyalty", Scaling.fromConfig(config, "scaling_amplifier.loyalty"));
        registerScaling("luck_of_the_sea", Scaling.fromConfig(config, "scaling_amplifier.luck"));
        registerScaling("lure", Scaling.fromConfig(config, "scaling_amplifier.lure"));
        registerScaling("respiration", Scaling.fromConfig(config, "scaling_amplifier.respiration"));
        registerScaling("piercing", Scaling.fromConfig(config, "scaling_amplifier.piercing"));
        registerScaling("protection", Scaling.fromConfig(config, "scaling_amplifier.protection"));
        registerScaling("projectile_protection", Scaling.fromConfig(config, "scaling_amplifier.projectile_protection"));
        registerScaling("blast_protection", Scaling.fromConfig(config, "scaling_amplifier.blast_protection"));
        registerScaling("fire_protection", Scaling.fromConfig(config, "scaling_amplifier.fire_protection"));
        registerScaling("feather_falling", Scaling.fromConfig(config, "scaling_amplifier.feather_falling"));
        registerScaling("quick_charge", Scaling.fromConfig(config, "scaling_amplifier.quick_charge"));
        registerScaling("riptide", Scaling.fromConfig(config, "scaling_amplifier.riptide"));
        registerScaling("soul_speed", Scaling.fromConfig(config, "scaling_amplifier.soul_speed"));
        registerScaling("sweeping_edge", Scaling.fromConfig(config, "scaling_amplifier.sweeping_edge"));
        registerScaling("thorns", Scaling.fromConfig(config, "scaling_amplifier.thorns"));
        registerScaling("swift_sneak", Scaling.fromConfig(config, "scaling_amplifier.swift_sneak"));
    }

    /**
     * Registers an enchantment scalings for a custom enchantment type.
     * @param enchantment the enchantment to set its amplifier scalings for
     * @param scaling the scaling formula for the material
     */
    public static void registerScaling(String enchantment, Scaling scaling){
        Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(enchantment));
        if (e == null || scaling == null) return;
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