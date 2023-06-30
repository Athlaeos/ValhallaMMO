package me.athlaeos.valhallammo.crafting.recipetypes;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration.MaterialChoice;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.List;

public class DynamicCookingRecipe {
    private final NamespacedKey key;
    private final String name;

    private ItemStack input = new ItemBuilder(Material.IRON_ORE).name("&r&fReplace me!").lore("&7I'm just a placeholder &einput&7 item!").get();
    private ItemStack result = new ItemBuilder(Material.IRON_INGOT).name("&r&fReplace me!").lore("&7I'm just a placeholder &eresult&7 item!").get();
    private boolean tinker = false;
    private IngredientChoice tinkerItem = new MaterialChoice();
    private int cookTime = 200;
    private boolean requireValhallaTools = false;
    private List<DynamicItemModifier> itemModifiers = new ArrayList<>();
    private float experience = 1F;
    private String validationKey = null;
    private boolean unlockedForEveryone = false;
    private final CookingRecipeType type;

    public DynamicCookingRecipe(String name, CookingRecipeType type){
        this.name = name;
        this.key = new NamespacedKey(ValhallaMMO.getInstance(), "cookingrecipe_" + name);
        this.type = type;
    }

    public NamespacedKey getKey() { return key; }
    public String getName() { return name; }
    public ItemStack getInput() { return input; }
    public ItemStack getResult() { return result; }
    public boolean isTinker() { return tinker; }
    public IngredientChoice getTinkerItem() { return tinkerItem; }
    public int getCookTime() { return cookTime; }
    public boolean isRequireValhallaTools() { return requireValhallaTools; }
    public List<DynamicItemModifier> getItemModifiers() { return itemModifiers; }
    public float getExperience() { return experience; }
    public String getValidationKey() { return validationKey; }
    public boolean isUnlockedForEveryone() { return unlockedForEveryone; }
    public CookingRecipeType getType() { return type; }

    public void setInput(ItemStack input) { this.input = input; }
    public void setResult(ItemStack result) { this.result = result; }
    public void setTinker(boolean tinker) { this.tinker = tinker; }
    public void setTinkerItem(IngredientChoice tinkerItem) { this.tinkerItem = tinkerItem; }
    public void setCookTime(int cookTime) { this.cookTime = cookTime; }
    public void setRequireValhallaTools(boolean requireValhallaTools) { this.requireValhallaTools = requireValhallaTools; }
    public void setItemModifiers(List<DynamicItemModifier> itemModifiers) { this.itemModifiers = itemModifiers; }
    public void setExperience(float experience) { this.experience = experience; }
    public void setValidationKey(String validationKey) { this.validationKey = validationKey; }
    public void setUnlockedForEveryone(boolean unlockedForEveryone) { this.unlockedForEveryone = unlockedForEveryone; }

    public CookingRecipe<?> generateRecipe() {
        return switch (type){
            case SMOKER -> new SmokingRecipe(key, result, tinkerItem.getChoice(input, false), experience, cookTime);
            case BLAST_FURNACE -> new BlastingRecipe(key, result, tinkerItem.getChoice(input, false), experience, cookTime);
            case CAMPFIRE, CAMPFIRES, SOUL_CAMPFIRE -> new CampfireRecipe(key, result, tinkerItem.getChoice(input, false), experience, cookTime);
            default -> new FurnaceRecipe(key, result, tinkerItem.getChoice(input, false), experience, cookTime);
        };
    }

    public enum CookingRecipeType{
        SMOKER,
        FURNACE,
        BLAST_FURNACE,
        CAMPFIRES,
        CAMPFIRE,
        SOUL_CAMPFIRE
    }
}
