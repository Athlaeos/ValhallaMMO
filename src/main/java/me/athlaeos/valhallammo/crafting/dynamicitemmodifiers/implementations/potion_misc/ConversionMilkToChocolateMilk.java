package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ConversionMilkToChocolateMilk extends DynamicItemModifier {

    public ConversionMilkToChocolateMilk(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        Map<String, PotionEffectWrapper> defaultPotionEffects = PotionEffectRegistry.getStoredEffects(outputItem.getMeta(), true);
        if (defaultPotionEffects.remove("MILK") == null) return;

        Map<String, PotionEffectWrapper> currentPotionEffects = PotionEffectRegistry.getStoredEffects(outputItem.getMeta(), false);
        if (currentPotionEffects.remove("MILK") == null) return;

        PotionEffectWrapper chocolateMilkWrapper = PotionEffectRegistry.getEffect("CHOCOLATE_MILK");
        defaultPotionEffects.put(chocolateMilkWrapper.getEffect(), chocolateMilkWrapper);
        currentPotionEffects.put(chocolateMilkWrapper.getEffect(), chocolateMilkWrapper);

        PotionEffectRegistry.setDefaultStoredEffects(outputItem.getMeta(), defaultPotionEffects);
        PotionEffectRegistry.setActualStoredEffects(outputItem.getMeta(), currentPotionEffects);
        PotionEffectRegistry.updateItemName(outputItem.getMeta(), true, false);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) { }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.COCOA_BEANS).get();
    }

    @Override
    public String getDisplayName() {
        return "&fMilk -> Chocolate Milk";
    }

    @Override
    public String getDescription() {
        return "&fTasty yummy cholki mik.";
    }

    @Override
    public String getActiveDescription() {
        return "&fTatasy chohomoilit mimnnlk.";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.POTION_MISC.id());
    }

    @Override
    public DynamicItemModifier createNew() {
        return new ConversionMilkToChocolateMilk(getName());
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
}
