package me.athlaeos.valhallammo.crafting.recipetypes;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration.MaterialChoice;
import me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DynamicBrewingRecipe {
    private final String name;

    private SlotEntry ingredient = new SlotEntry(
            new ItemBuilder(Material.NETHER_WART).name("&r&fReplace me!").lore("&7I'm just a placeholder ingredient!").get(),
            new MaterialChoice(),
            null);
    private ItemStack applyOn;
    private IngredientChoice applyOnChoice;
    private List<DynamicItemModifier> modifiers = new ArrayList<>();
    private boolean unlockedForEveryone = false;

    public DynamicBrewingRecipe(String name){this.name = name;}
    public String getName() {return name;}
    public SlotEntry getIngredient() {return ingredient;}
    public ItemStack getApplyOn() {return applyOn;}
    public IngredientChoice getApplyOnChoice() {return applyOnChoice;}
    public List<DynamicItemModifier> getModifiers() {return modifiers;}
    public boolean isUnlockedForEveryone() {return unlockedForEveryone;}

    public void setIngredient(SlotEntry ingredient) {this.ingredient = ingredient;}
    public void setApplyOn(ItemStack applyOn) {this.applyOn = applyOn;}
    public void setApplyOnChoice(IngredientChoice applyOnChoice) {this.applyOnChoice = applyOnChoice;}
    public void setModifiers(List<DynamicItemModifier> modifiers) {this.modifiers = modifiers;}
    public void setUnlockedForEveryone(boolean unlockedForEveryone) {this.unlockedForEveryone = unlockedForEveryone;}
}
