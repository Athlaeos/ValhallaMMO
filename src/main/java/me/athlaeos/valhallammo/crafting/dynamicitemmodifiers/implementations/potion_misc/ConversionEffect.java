package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_misc;

import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ConversionEffect extends DynamicItemModifier {
    private static Map<String, InvertedEffect> invertedEffects = null;

    public ConversionEffect(String name) {
        super(name);
        if (invertedEffects == null){
            invertedEffects = new HashMap<>();
            YamlConfiguration config = ConfigManager.getConfig("skills/alchemy.yml").get();
            ConfigurationSection section = config.getConfigurationSection("effects_inverted");
            if (section != null){
                for (String effect : section.getKeys(false)){
                    String invert = config.getString("effects_inverted." + effect + ".inverted_effect");
                    if (invert == null) continue;
                    String color = config.getString("effects_inverted." + effect + ".color");
                    int duration = config.getInt("effects_inverted." + effect + ".duration");
                    double amplifier = config.getDouble("effects_inverted." + effect + ".amplifier");
                    invertedEffects.put(effect, new InvertedEffect(invert, color, duration, amplifier));
                }
            }
        }
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        Map<String, PotionEffectWrapper> defaultWrappers = PotionEffectRegistry.getStoredEffects(outputItem.getMeta(), true);
        Map<String, PotionEffectWrapper> currentWrappers = PotionEffectRegistry.getStoredEffects(outputItem.getMeta(), false);

        boolean changesMade = false;
        for (String w : defaultWrappers.keySet()){
            InvertedEffect invert = invertedEffects.get(w);
            if (invert == null) continue;
            defaultWrappers.remove(w);
            currentWrappers.remove(w);

            PotionEffectWrapper wrapper = PotionEffectRegistry.getEffect(invert.invertedEffect);
            wrapper.setDuration(invert.duration);
            wrapper.setAmplifier(invert.amplifier);
            if (invert.color != null) outputItem.color(Utils.hexToRgb(invert.color));

            defaultWrappers.put(invert.invertedEffect, wrapper);
            currentWrappers.put(invert.invertedEffect, wrapper);
            changesMade = true;
        }
        if (changesMade){
            PotionEffectRegistry.setDefaultStoredEffects(outputItem.getMeta(), defaultWrappers);
            PotionEffectRegistry.setActualStoredEffects(outputItem.getMeta(), currentWrappers);
        } else {
            failedRecipe(outputItem, TranslationManager.getTranslation("modifier_warning_no_invertible_effects"));
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) { }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.FERMENTED_SPIDER_EYE).get();
    }

    @Override
    public String getDisplayName() {
        return "&dInvert Potion Effects";
    }

    @Override
    public String getDescription() {
        return "&fInverts potion effects on the potion according to effects defined in skills/alchemy.yml. If no effects were inverted, recipe is cancelled.";
    }

    @Override
    public String getActiveDescription() {
        return "&fInverts potion effects on the potion according to effects defined in skills/alchemy.yml. If no effects were inverted, recipe is cancelled.";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.POTION_MISC.id());
    }

    @Override
    public DynamicItemModifier copy() {
        ConversionEffect m = new ConversionEffect(getName());
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 0;
    }

    private record InvertedEffect(String invertedEffect, String color, int duration, double amplifier) { }
}
