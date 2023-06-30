package me.athlaeos.valhallammo.crafting.recipetypes;

import me.athlaeos.valhallammo.crafting.ToolRequirement;
import me.athlaeos.valhallammo.crafting.ToolRequirementType;
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

public class ImmersiveCraftingRecipe {
    private final String name;
    private String displayName = "";
    private String description = "";

    private Map<ItemStack, Integer> ingredients = new HashMap<>();
    private Material block = Material.CRAFTING_TABLE;
    private int timeToCraft = 2500;
    private boolean destroyStation = false;
    private List<DynamicItemModifier> itemModifiers = new ArrayList<>();

    private ItemStack output = new ItemBuilder(Material.WOODEN_SWORD).name("&r&fReplace me!").lore("&7I'm just a placeholder item!").get();
    private boolean tinker = false;
    private IngredientChoice tinkerItem = new MaterialChoice();

    private String validationKey = null;
    private ToolRequirement toolRequirement = new ToolRequirement(ToolRequirementType.NOT_REQUIRED, -1);
    private int consecutiveCrafts = 16;
    private boolean unlockedForEveryone = false;

    public ImmersiveCraftingRecipe(String name){
        this.name = name;
    }

    public String getName() {return name;}
    public IngredientChoice getTinkerItem() {return tinkerItem;}
    public int getConsecutiveCrafts() {return consecutiveCrafts;}
    public ToolRequirement getToolRequirement() {return toolRequirement;}
    public int getTimeToCraft() {return timeToCraft;}
    public ItemStack getOutput() {return output;}
    public List<DynamicItemModifier> getItemModifiers() {return itemModifiers;}
    public Map<ItemStack, Integer> getIngredients() {return ingredients;}
    public Material getBlock() {return block;}
    public String getDescription() {return description;}
    public String getDisplayName() {return displayName;}
    public String getValidationKey() {return validationKey;}
    public boolean isDestroyStation() {return destroyStation;}
    public boolean isTinker() {return tinker;}
    public boolean isUnlockedForEveryone() {return unlockedForEveryone;}

    public void setBlock(Material block) {this.block = block;}
    public void setConsecutiveCrafts(int consecutiveCrafts) {this.consecutiveCrafts = consecutiveCrafts;}
    public void setOutput(ItemStack output) {this.output = output;}
    public void setDescription(String description) {this.description = description;}
    public void setDestroyStation(boolean destroyStation) {this.destroyStation = destroyStation;}
    public void setDisplayName(String displayName) {this.displayName = displayName;}
    public void setIngredients(Map<ItemStack, Integer> ingredients) {this.ingredients = ingredients;}
    public void setItemModifiers(List<DynamicItemModifier> itemModifiers) {this.itemModifiers = itemModifiers;}
    public void setTimeToCraft(int timeToCraft) {this.timeToCraft = timeToCraft;}
    public void setTinker(boolean tinker) {this.tinker = tinker;}
    public void setTinkerItem(IngredientChoice tinkerItem) {this.tinkerItem = tinkerItem;}
    public void setToolRequirement(ToolRequirement toolRequirement) {this.toolRequirement = toolRequirement;}
    public void setUnlockedForEveryone(boolean unlockedForEveryone) {this.unlockedForEveryone = unlockedForEveryone;}
    public void setValidationKey(String validationKey) {this.validationKey = validationKey;}
}
