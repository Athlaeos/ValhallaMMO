package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.stream.Collectors;

public class PotionMatchEffectsChoice extends RecipeOption {

    private static final Map<PotionType, Collection<PotionEffectMappings>> typeMapping = new HashMap<>();
    static {
        map(Set.of("AWKWARD"), new HashSet<>());
        map(Set.of("FIRE_RESISTANCE", "LONG_FIRE_RESISTANCE"), Set.of(PotionEffectMappings.FIRE_RESISTANCE));
        map(Set.of("INSTANT_DAMAGE", "HARMING", "STRONG_HARMING"), Set.of(PotionEffectMappings.INSTANT_DAMAGE));
        map(Set.of("INSTANT_HEALTH", "HEALING", "STRONG_HEALING"), Set.of(PotionEffectMappings.INSTANT_HEALTH));
        map(Set.of("INVISIBILITY", "LONG_INVISIBILITY"), Set.of(PotionEffectMappings.INVISIBILITY));
        map(Set.of("JUMP", "LEAPING", "LONG_LEAPING", "STRONG_LEAPING"), Set.of(PotionEffectMappings.JUMP_BOOST));
        map(Set.of("LUCK"), Set.of(PotionEffectMappings.LUCK));
        map(Set.of("MUNDANE"), new HashSet<>());
        map(Set.of("NIGHT_VISION", "LONG_NIGHT_VISION"), Set.of(PotionEffectMappings.NIGHT_VISION));
        map(Set.of("POISON", "LONG_POISON", "STRONG_POISON"), Set.of(PotionEffectMappings.POISON));
        map(Set.of("REGEN", "REGENERATION", "LONG_REGENERATION", "STRONG_REGENERATION"), Set.of(PotionEffectMappings.REGENERATION));
        map(Set.of("SLOW_FALLING", "LONG_SLOW_FALLING"), Set.of(PotionEffectMappings.SLOW_FALLING));
        map(Set.of("SLOW", "SLOWNESS", "LONG_SLOWNESS", "STRONG_SLOWNESS"), Set.of(PotionEffectMappings.SLOWNESS));
        map(Set.of("SPEED", "SWIFTNESS", "LONG_SWIFTNESS", "STRONG_SWIFTNESS"), Set.of(PotionEffectMappings.SPEED));
        map(Set.of("STRENGTH", "LONG_STRENGTH", "STRONG_STRENGTH"), Set.of(PotionEffectMappings.STRENGTH));
        map(Set.of("THICK"), new HashSet<>());
        map(Set.of("TURTLE_MASTER", "LONG_TURTLE_MASTER", "STRONG_TURTLE_MASTER"), Set.of(PotionEffectMappings.SLOWNESS, PotionEffectMappings.RESISTANCE));
        map(Set.of("UNCRAFTABLE"), new HashSet<>());
        map(Set.of("WATER"), new HashSet<>());
        map(Set.of("WATER_BREATHING", "LONG_WATER_BREATHING"), Set.of(PotionEffectMappings.WATER_BREATHING));
        map(Set.of("WEAKNESS", "LONG_WEAKNESS"), Set.of(PotionEffectMappings.WEAKNESS));
    }
    private static void map(Collection<String> potionTypes, Collection<PotionEffectMappings> effects){
        for (String t : potionTypes){
            PotionType type = Catch.catchOrElse(() -> PotionType.valueOf(t), null);
            if (type == null) continue;
            Collection<PotionEffectMappings> existingTypes = typeMapping.getOrDefault(type, new HashSet<>());
            existingTypes.addAll(effects);
            typeMapping.put(type, existingTypes);
        }
    }

    @Override
    public String getName() {
        return "CHOICE_POTION_EFFECTS";
    }

    @Override
    public String getActiveDescription() {
        return "The ingredient can be replaced with any potion (splash, lingering, and normal) but only if its effects match that of the item put";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.LINGERING_POTION).name("&7Potion Effects")
        .lore(
            "&aIngredient may be substituted with",
            "&aany type of potion (splash, lingering, ",
            "&anormal) that also matches in effects, or",
                "&atype if it has no effects."
        ).get();
    }

    @Override
    public boolean isCompatible(ItemStack i) {
        return i.getType() == Material.POTION ||
                i.getType() == Material.SPLASH_POTION ||
                i.getType() == Material.LINGERING_POTION;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public RecipeOption getNew() {
        return new PotionMatchEffectsChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }

    @Override
    public RecipeChoice getChoice(ItemStack i) {
        return new RecipeChoice.MaterialChoice(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION);
    }

    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        ItemMeta i1Meta = ItemUtils.getItemMeta(i1);
        ItemMeta i2Meta = ItemUtils.getItemMeta(i2);
        if (!(i1Meta instanceof PotionMeta p1) || !(i2Meta instanceof PotionMeta p2)) return false;
        Map<String, PotionEffectWrapper> p1ValEffects = PotionEffectRegistry.getStoredEffects(p1, false);
        Map<String, PotionEffectWrapper> p2ValEffects = PotionEffectRegistry.getStoredEffects(p2, false);
        if (p1ValEffects.isEmpty() != p2ValEffects.isEmpty() || p1ValEffects.size() != p2ValEffects.size()) return false; // return false if one potion has more effects than the other
        if (p1ValEffects.isEmpty()){
            if (p1.getCustomEffects().isEmpty() != p2.getCustomEffects().isEmpty() ||
                    p1.getCustomEffects().size() != p2.getCustomEffects().size()) return false; // return false if one potion has more effects than the other
            if (p1.getCustomEffects().isEmpty()){
                if (ValhallaMMO.getNms().getPotionType(p1) == ValhallaMMO.getNms().getPotionType(p2)) return true;
                Collection<PotionEffectMappings> p1Types = typeMapping.getOrDefault(ValhallaMMO.getNms().getPotionType(p1), new HashSet<>());
                Collection<PotionEffectMappings> p2Types = typeMapping.getOrDefault(ValhallaMMO.getNms().getPotionType(p2), new HashSet<>());
                if (p1Types.isEmpty() != p2Types.isEmpty() || p1Types.size() != p2Types.size()) return false;
                if (p1Types.isEmpty()) return ValhallaMMO.getNms().getPotionType(p1) == ValhallaMMO.getNms().getPotionType(p2);
                for (PotionEffectMappings t : p1Types){
                    if (!p2Types.contains(t)) return false;
                }
            } else {
                for (PotionEffect effect : p1.getCustomEffects()){
                    if (!p2.getCustomEffects().stream().map(PotionEffect::getType).collect(Collectors.toSet()).contains(effect.getType())) return false;
                }
            }
            return true;
        } else {
            for (String effect : p1ValEffects.keySet()){
                if (!p2ValEffects.containsKey(effect)) return false;
            }
        }
        return true;
    }

    @Override
    public String ingredientDescription(ItemStack base) {
        return TranslationManager.translatePlaceholders(ItemUtils.getItemName(new ItemBuilder(base)));
    }
}
