package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.dom.MinecraftVersion;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

import java.util.Collection;
import java.util.HashSet;

public enum EnchantmentClassification {
    OFFENSIVE("thorns", "bane_of_arthropods", "smite", "sharpness", "knockback", "fire_aspect",
            "impaling", "sweeping_edge", "channeling", "flame", "infinity", "loyalty", "multishot",
            "piercing", "power", "punch", "quick_charge"),
    DEFENSIVE("projectile_protection", "feather_falling", "fire_protection", "blast_protection",
            "protection"),
    UTILITY("mending", "unbreaking", "aqua_affinity", "depth_strider", "frost_walker", "respiration",
            "soul_speed", "swift_sneak", "efficiency", "looting", "fortune", "riptide", "luck_of_the_sea", "lure",
            "silk_touch"),
    CURSE("curse_of_vanishing", "curse_of_binding"),
    UNDEFINED;
    private final Collection<Enchantment> matches = new HashSet<>();

    @SuppressWarnings("deprecation")
    EnchantmentClassification(String... matches){
        for (String m : matches){
            Enchantment match = MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? Registry.ENCHANTMENT.get(NamespacedKey.minecraft(m)) : Enchantment.getByKey(NamespacedKey.minecraft(m));
            if (match == null) continue;
            this.matches.add(match);
        }
    }

    public Collection<Enchantment> getMatches() {
        return matches;
    }

    public static EnchantmentClassification getClassification(Enchantment e){
        for (EnchantmentClassification classification : values()){
            if (classification.getMatches().contains(e)) return classification;
        }
        return UNDEFINED;
    }
}
