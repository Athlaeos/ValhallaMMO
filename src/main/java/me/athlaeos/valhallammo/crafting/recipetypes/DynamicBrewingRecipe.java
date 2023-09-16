package me.athlaeos.valhallammo.crafting.recipetypes;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DynamicBrewingRecipe implements ValhallaRecipe {
    private final String name;
    private String displayName = null;
    private String description = null;

    private SlotEntry ingredient = new SlotEntry(
            new ItemBuilder(Material.NETHER_WART).name("&r&fReplace me!").lore("&7I'm just a placeholder ingredient!").get(),
            new MaterialChoice());
    private ItemStack result = new ItemBuilder(Material.DIAMOND).name("&r&fReplace me!").lore("&7I'm just a placeholder result!").get();
    private SlotEntry applyOn = new SlotEntry(
            new ItemBuilder(Material.POTION).flag(ItemFlag.HIDE_POTION_EFFECTS).color(Color.fromRGB(0, 170, 230)).name("&r&fReplace me!").lore("&7I'm just a placeholder ingredient!").get(),
            new MaterialChoice());
    private boolean tinker = true;
    private boolean requireValhallaTools = false;
    private boolean consumeIngredient = true;
    private List<DynamicItemModifier> modifiers = new ArrayList<>();
    private int brewTime = 400;
    private boolean unlockedForEveryone = false;

    public DynamicBrewingRecipe(String name){this.name = name;}

    public String getName() {return name;}
    @Override public ItemStack getResult() { return result; }
    public SlotEntry getIngredient() {return ingredient;}
    public SlotEntry getApplyOn() {return applyOn;}
    public List<DynamicItemModifier> getModifiers() {return modifiers;}
    public boolean isUnlockedForEveryone() {return unlockedForEveryone;}
    public boolean tinker() { return tinker; }
    public int getBrewTime() { return brewTime; }
    public boolean requireValhallaTools() { return requireValhallaTools; }
    public boolean consumeIngredient() { return consumeIngredient; }
    public String getDescription() { return description; }
    public String getDisplayName() { return displayName; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setDescription(String description) { this.description = description; }
    public void setIngredient(SlotEntry ingredient) {this.ingredient = ingredient;}
    public void setApplyOn(SlotEntry applyOn) {this.applyOn = applyOn;}
    public void setResult(ItemStack result) { this.result = result; }
    public void setTinker(boolean tinker) { this.tinker = tinker; }
    public void setBrewTime(int brewTime) { this.brewTime = brewTime; }
    public void setRequireValhallaTools(boolean requireValhallaTools) { this.requireValhallaTools = requireValhallaTools; }
    public void setModifiers(List<DynamicItemModifier> modifiers) {
        this.modifiers = modifiers;
        DynamicItemModifier.sortModifiers(this.modifiers);
    }
    public void setUnlockedForEveryone(boolean unlockedForEveryone) {this.unlockedForEveryone = unlockedForEveryone;}
    public void setConsumeIngredient(boolean consumeIngredient) { this.consumeIngredient = consumeIngredient; }
}
