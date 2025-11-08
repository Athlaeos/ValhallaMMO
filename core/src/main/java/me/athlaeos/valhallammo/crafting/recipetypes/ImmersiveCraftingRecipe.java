package me.athlaeos.valhallammo.crafting.recipetypes;

import me.athlaeos.valhallammo.crafting.MetaRequirement;
import me.athlaeos.valhallammo.crafting.ToolRequirement;
import me.athlaeos.valhallammo.crafting.ToolRequirementType;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ImmersiveCraftingRecipe implements ValhallaRecipe {
    private final String name;
    private String displayName = null;
    private String description = null;

    private Map<ItemStack, Integer> ingredients = new HashMap<>();
    private MetaRequirement metaRequirement = MetaRequirement.MATERIAL;
    private String block = "CRAFTING_TABLE";
    private int timeToCraft = 50;
    private boolean destroyStation = false;
    private List<DynamicItemModifier> modifiers = new ArrayList<>();

    private ItemStack result = new ItemBuilder(Material.WOODEN_SWORD).name("&r&fReplace me!").lore("&7I'm just a placeholder item!").get();
    private SlotEntry tinkerInput = new SlotEntry(
            new ItemBuilder(Material.IRON_SWORD).name("&r&fReplace me!").lore("&7I'm just a placeholder &einput&7 item!").get(),
            new MaterialChoice());
    private boolean tinker = false;
    private boolean requiresValhallaTools = false;

    private Collection<String> validations = new HashSet<>();
    private ToolRequirement toolRequirement = new ToolRequirement(ToolRequirementType.NOT_REQUIRED, -1);
    private int consecutiveCrafts = 16;
    private boolean unlockedForEveryone = false;

    public ImmersiveCraftingRecipe(String name){
        this.name = name;
    }

    public String getName() {return name;}
    public SlotEntry getTinkerInput() {return tinkerInput;}
    public int getConsecutiveCrafts() {return consecutiveCrafts;}
    public ToolRequirement getToolRequirement() {return toolRequirement;}
    public int getTimeToCraft() {return timeToCraft;}
    @Override public ItemStack getResult() {return result;}
    public List<DynamicItemModifier> getModifiers() {return modifiers;}
    public Map<ItemStack, Integer> getIngredients() {return ingredients;}
    public String getBlock() {return block;}
    public String getDescription() {return description;}
    public String getDisplayName() {return displayName;}
    public Collection<String> getValidations() {return validations;}
    public boolean destroysStation() {return destroyStation;}
    public boolean tinker() {return tinker;}
    public boolean isUnlockedForEveryone() {return unlockedForEveryone;}
    public boolean requiresValhallaTools() { return requiresValhallaTools; }
    public MetaRequirement getMetaRequirement() { return metaRequirement; }

    public void setRequireExactMeta(MetaRequirement metaRequirement) { this.metaRequirement = metaRequirement; }
    public void setBlock(String block) {this.block = block;}
    public void setConsecutiveCrafts(int consecutiveCrafts) {this.consecutiveCrafts = consecutiveCrafts;}
    public void setResult(ItemStack result) {this.result = result;}
    public void setDescription(String description) {this.description = description;}
    public void setDestroyStation(boolean destroyStation) {this.destroyStation = destroyStation;}
    public void setDisplayName(String displayName) {this.displayName = displayName;}
    public void setIngredients(Map<ItemStack, Integer> ingredients) {this.ingredients = ingredients;}
    public void setModifiers(List<DynamicItemModifier> modifiers) {
        this.modifiers = modifiers;
        DynamicItemModifier.sortModifiers(this.modifiers);
    }
    public void setTimeToCraft(int timeToCraft) {this.timeToCraft = timeToCraft;}
    public void setTinker(boolean tinker) {this.tinker = tinker;}
    public void setTinkerInput(SlotEntry tinkerItem) {this.tinkerInput = tinkerItem;}
    public void setToolRequirement(ToolRequirement toolRequirement) {this.toolRequirement = toolRequirement;}
    public void setUnlockedForEveryone(boolean unlockedForEveryone) {this.unlockedForEveryone = unlockedForEveryone;}
    public void setValidations(Collection<String> validations) {this.validations = validations;}
    public void setRequiresValhallaTools(boolean requiresValhallaTools) { this.requiresValhallaTools = requiresValhallaTools; }
}
