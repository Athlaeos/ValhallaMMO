package me.athlaeos.valhallammo.crafting.recipetypes;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration.MaterialChoice;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicCauldronRecipe {
    private final String name;

    private List<ItemStack> ingredients = new ArrayList<>();
    private ItemStack catalyst = new ItemBuilder(Material.IRON_SWORD).name("&r&fReplace me!").lore("&7I'm just a placeholder catalyst item!").get();
    private IngredientChoice catalystChoice = new MaterialChoice();
    private ItemStack output = new ItemBuilder(Material.IRON_SWORD).name("&r&fReplace me!").lore("&7I'm just a placeholder result item!").get();
    private List<DynamicItemModifier> itemModifiers = new ArrayList<>();
    private boolean requiresValhallaTools = false;
    private boolean tinkerCatalyst = false;
    private boolean requiresBoilingWater = false;
    private boolean consumesWaterLevel = false;
    private boolean unlockedForEveryone = false;
    private String validationKey = null;

    public DynamicCauldronRecipe(String name){this.name = name;}

    public String getName() {return name;}
    public List<ItemStack> getIngredients() {return ingredients;}
    public ItemStack getCatalyst() {return catalyst;}
    public IngredientChoice getCatalystChoice() {return catalystChoice;}
    public ItemStack getOutput() {return output;}
    public List<DynamicItemModifier> getItemModifiers() {return itemModifiers;}
    public boolean isRequiresValhallaTools() {return requiresValhallaTools;}
    public boolean isTinkerCatalyst() {return tinkerCatalyst;}
    public boolean isRequiresBoilingWater() {return requiresBoilingWater;}
    public boolean isConsumesWaterLevel() {return consumesWaterLevel;}
    public boolean isUnlockedForEveryone() {return unlockedForEveryone;}
    public String getValidationKey() {return validationKey;}

    public void setIngredients(List<ItemStack> ingredients) {this.ingredients = ingredients;}
    public void setCatalyst(ItemStack catalyst) {this.catalyst = catalyst;}
    public void setCatalystChoice(IngredientChoice catalystChoice) {this.catalystChoice = catalystChoice;}
    public void setOutput(ItemStack output) {this.output = output;}
    public void setItemModifiers(List<DynamicItemModifier> itemModifiers) {this.itemModifiers = itemModifiers;}
    public void setRequiresValhallaTools(boolean requiresValhallaTools) {this.requiresValhallaTools = requiresValhallaTools;}
    public void setTinkerCatalyst(boolean tinkerCatalyst) {this.tinkerCatalyst = tinkerCatalyst;}
    public void setRequiresBoilingWater(boolean requiresBoilingWater) {this.requiresBoilingWater = requiresBoilingWater;}
    public void setConsumesWaterLevel(boolean consumesWaterLevel) {this.consumesWaterLevel = consumesWaterLevel;}
    public void setUnlockedForEveryone(boolean unlockedForEveryone) {this.unlockedForEveryone = unlockedForEveryone;}
    public void setValidationKey(String validationKey) {this.validationKey = validationKey;}
}
