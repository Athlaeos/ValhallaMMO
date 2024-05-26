package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.dom.Scaling;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;

import java.util.HashMap;
import java.util.Map;

import static me.athlaeos.valhallammo.utility.Utils.oldOrNew;

public class EnchantingItemPropertyManager {
    private static final Map<Enchantment, Scaling> enchantmentScaling = new HashMap<>();

    static {
        String config = "skills/enchanting.yml";
        registerScaling("power", Scaling.fromConfig(config, "scalings.power"));
        registerScaling("punch", Scaling.fromConfig(config, "scalings.punch"));
        registerScaling("sharpness", Scaling.fromConfig(config, "scalings.sharpness"));
        registerScaling("bane_of_arthropods", Scaling.fromConfig(config, "scalings.bane_of_arthropods"));
        registerScaling("smite", Scaling.fromConfig(config, "scalings.smite"));
        registerScaling("depth_strider", Scaling.fromConfig(config, "scalings.depth_strider"));
        registerScaling("efficiency", Scaling.fromConfig(config, "scalings.efficiency"));
        registerScaling("unbreaking", Scaling.fromConfig(config, "scalings.unbreaking"));
        registerScaling("fire_aspect", Scaling.fromConfig(config, "scalings.fire_aspect"));
        registerScaling("frost_walker", Scaling.fromConfig(config, "scalings.frost_walker"));
        registerScaling("impaling", Scaling.fromConfig(config, "scalings.impaling"));
        registerScaling("knockback", Scaling.fromConfig(config, "scalings.knockback"));
        registerScaling("fortune", Scaling.fromConfig(config, "scalings.fortune"));
        registerScaling("looting", Scaling.fromConfig(config, "scalings.looting"));
        registerScaling("loyalty", Scaling.fromConfig(config, "scalings.loyalty"));
        registerScaling("luck_of_the_sea", Scaling.fromConfig(config, "scalings.luck"));
        registerScaling("lure", Scaling.fromConfig(config, "scalings.lure"));
        registerScaling("respiration", Scaling.fromConfig(config, "scalings.respiration"));
        registerScaling("piercing", Scaling.fromConfig(config, "scalings.piercing"));
        registerScaling("protection", Scaling.fromConfig(config, "scalings.protection"));
        registerScaling("projectile_protection", Scaling.fromConfig(config, "scalings.projectile_protection"));
        registerScaling("blast_protection", Scaling.fromConfig(config, "scalings.blast_protection"));
        registerScaling("fire_protection", Scaling.fromConfig(config, "scalings.fire_protection"));
        registerScaling("feather_falling", Scaling.fromConfig(config, "scalings.feather_falling"));
        registerScaling("quick_charge", Scaling.fromConfig(config, "scalings.quick_charge"));
        registerScaling("riptide", Scaling.fromConfig(config, "scalings.riptide"));
        registerScaling("soul_speed", Scaling.fromConfig(config, "scalings.soul_speed"));
        registerScaling("sweeping", Scaling.fromConfig(config, "scalings.sweeping"));
        registerScaling("thorns", Scaling.fromConfig(config, "scalings.thorns"));
        registerScaling("swift_sneak", Scaling.fromConfig(config, "scalings.swift_sneak"));
    }

    /**
     * Registers an enchantment scalings for a custom enchantment type.
     * @param enchantment the enchantment to set its amplifier scalings for
     * @param scaling the scaling formula for the material
     */
    @SuppressWarnings("deprecation")
    public static void registerScaling(String enchantment, Scaling scaling){
        if (enchantment.equalsIgnoreCase("sweeping")) enchantment = oldOrNew("sweeping", "sweeping_edge");
        Enchantment e = MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantment)) : Enchantment.getByKey(NamespacedKey.minecraft(enchantment));
        if (e == null || scaling == null) {
            ValhallaMMO.logWarning("Could not register scaling for " + enchantment + ", it's not a valid enchantment!");
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