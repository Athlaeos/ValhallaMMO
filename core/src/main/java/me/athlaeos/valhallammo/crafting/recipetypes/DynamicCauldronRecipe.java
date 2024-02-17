package me.athlaeos.valhallammo.crafting.recipetypes;

import me.athlaeos.valhallammo.crafting.MetaRequirement;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DynamicCauldronRecipe implements ValhallaRecipe {
    private final String name;
    private String displayName = null;
    private String description = null;

    private Map<ItemStack, Integer> ingredients = new HashMap<>();
    private SlotEntry catalyst = new SlotEntry(
            new ItemBuilder(Material.MAGMA_BLOCK).name("&r&fReplace me!").lore("&7I'm just a placeholder catalyst!").get(),
            new MaterialChoice());
    private ItemStack result = new ItemBuilder(Material.STONE).name("&r&fReplace me!").lore("&7I'm just a placeholder result item!").get();
    private List<DynamicItemModifier> modifiers = new ArrayList<>();
    private boolean requiresValhallaTools = false;
    private boolean tinkerCatalyst = false;
    private int cookTime = 600;
    private boolean timedRecipe = false; // timed recipes do not trigger when a catalyst is thrown in, and instead trigger when cookTime has passed with the required ingredients. No item can be tinkered with such a recipe
    private boolean unlockedForEveryone = false;
    private MetaRequirement metaRequirement = MetaRequirement.MATERIAL;
    private Collection<String> validations = new HashSet<>();

    public DynamicCauldronRecipe(String name){this.name = name;}

    public String getName() {return name;}
    public Map<ItemStack, Integer> getIngredients() {return ingredients;}
    public SlotEntry getCatalyst() {return catalyst;}
    @Override public ItemStack getResult() {return result;}
    public List<DynamicItemModifier> getModifiers() {return modifiers;}
    public boolean requiresValhallaTools() {return requiresValhallaTools;}
    public boolean tinkerCatalyst() {return tinkerCatalyst;}
    public boolean isTimedRecipe() { return timedRecipe; }
    public int getCookTime() { return cookTime; }
    public boolean isUnlockedForEveryone() {return unlockedForEveryone;}
    public MetaRequirement getMetaRequirement() { return metaRequirement; }
    public Collection<String> getValidations() {return validations;}
    public String getDescription() { return description; }
    public String getDisplayName() { return displayName; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setDescription(String description) { this.description = description; }
    public void setIngredients(Map<ItemStack, Integer> ingredients) {this.ingredients = ingredients;}
    public void setCatalyst(SlotEntry catalyst) {this.catalyst = catalyst;}
    public void setResult(ItemStack result) {this.result = result;}
    public void setModifiers(List<DynamicItemModifier> modifiers) {
        this.modifiers = modifiers;
        DynamicItemModifier.sortModifiers(this.modifiers);
    }
    public void setRequiresValhallaTools(boolean requiresValhallaTools) {this.requiresValhallaTools = requiresValhallaTools;}
    public void setTinkerCatalyst(boolean tinkerCatalyst) {this.tinkerCatalyst = tinkerCatalyst;}
    public void setCookTime(int cookTime) { this.cookTime = cookTime; }
    public void setTimedRecipe(boolean timedRecipe) { this.timedRecipe = timedRecipe; }
    public void setUnlockedForEveryone(boolean unlockedForEveryone) {this.unlockedForEveryone = unlockedForEveryone;}
    public void setValidations(Collection<String> validations) {this.validations = validations;}
    public void setMetaRequirement(MetaRequirement metaRequirement) { this.metaRequirement = metaRequirement; }
}
