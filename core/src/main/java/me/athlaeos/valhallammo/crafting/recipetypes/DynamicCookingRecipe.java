package me.athlaeos.valhallammo.crafting.recipetypes;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ResultChangingModifier;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DynamicCookingRecipe implements ValhallaRecipe, ValhallaKeyedRecipe {
    private final NamespacedKey key;
    private final String name;
    private String displayName = null;
    private String description = null;

    private SlotEntry input = new SlotEntry(
            new ItemBuilder(Material.IRON_ORE).name("&r&fReplace me!").lore("&7I'm just a placeholder &einput&7 item!").get(),
            new MaterialChoice());
    private ItemStack result = new ItemBuilder(Material.IRON_INGOT).name("&r&fReplace me!").lore("&7I'm just a placeholder &eresult&7 item!").get();
    private boolean tinker = false;
    private int cookTime = 200;
    private boolean hiddenFromBook = false;
    private boolean requireValhallaTools = false;
    private List<DynamicItemModifier> modifiers = new ArrayList<>();
    private float experience = 1;
    private Collection<String> validations = new HashSet<>();
    private boolean unlockedForEveryone = false;
    private final CookingRecipeType type;

    public DynamicCookingRecipe(String name, CookingRecipeType type){
        this.name = name;
        this.key = new NamespacedKey(ValhallaMMO.getInstance(), "cookingrecipe_" + name);
        this.type = type;
    }

    public @NotNull NamespacedKey getKey() { return key; }

    @Override
    public void registerRecipe() {
        Recipe recipe = generateRecipe();
        if (ValhallaMMO.getInstance().getServer().getRecipe(key) != null) ValhallaMMO.getInstance().getServer().removeRecipe(key);
        if (recipe != null) ValhallaMMO.getInstance().getServer().addRecipe(recipe);
    }

    @Override
    public void unregisterRecipe() {
        if (ValhallaMMO.getInstance().getServer().getRecipe(key) != null) ValhallaMMO.getInstance().getServer().removeRecipe(key);
    }

    public String getName() { return name; }
    public SlotEntry getInput() { return input; }
    public ItemStack getResult() { return result; }
    public boolean tinker() { return tinker; }
    public int getCookTime() { return cookTime; }
    public boolean requireValhallaTools() { return requireValhallaTools; }
    public List<DynamicItemModifier> getModifiers() { return modifiers; }
    public float getExperience() { return experience; }
    public Collection<String> getValidations() {return validations;}
    public boolean isUnlockedForEveryone() { return unlockedForEveryone; }
    public CookingRecipeType getType() { return type; }
    public String getDescription() { return description; }
    public String getDisplayName() { return displayName; }
    public boolean isHiddenFromBook() { return hiddenFromBook; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setDescription(String description) { this.description = description; }
    public void setInput(SlotEntry input) { this.input = input; }
    public void setResult(ItemStack result) { this.result = result; }
    public void setTinker(boolean tinker) { this.tinker = tinker; }
    public void setCookTime(int cookTime) { this.cookTime = cookTime; }
    public void setRequireValhallaTools(boolean requireValhallaTools) { this.requireValhallaTools = requireValhallaTools; }
    public void setModifiers(List<DynamicItemModifier> modifiers) {
        this.modifiers = modifiers;
        DynamicItemModifier.sortModifiers(this.modifiers);
    }
    public void setExperience(float experience) { this.experience = experience; }
    public void setValidations(Collection<String> validations) {this.validations = validations;}
    public void setUnlockedForEveryone(boolean unlockedForEveryone) { this.unlockedForEveryone = unlockedForEveryone; }
    public void setHiddenFromBook(boolean hiddenFromBook) { this.hiddenFromBook = hiddenFromBook; }

    public CookingRecipe<?> generateRecipe() {
        if (input == null || ItemUtils.isEmpty(input.getItem()) ||
                ItemUtils.isEmpty(result)) return null;
        if (input.getOption() == null) input.setOption(new MaterialChoice());
        ItemStack i = result.clone();
        ResultChangingModifier changer = (ResultChangingModifier) modifiers.stream().filter(m -> m instanceof ResultChangingModifier).reduce((first, second) -> second).orElse(null);
        if (changer != null) {
            i = Utils.thisorDefault(changer.getNewResult(ModifierContext.builder(new ItemBuilder(i)).get()), i);
        }
        i = new ItemBuilder(i).translate().get();
        return switch (type){
            case SMOKER -> new SmokingRecipe(key, i, input.getOption().getChoice(input.getItem()), experience, cookTime);
            case BLAST_FURNACE -> new BlastingRecipe(key, i, input.getOption().getChoice(input.getItem()), experience, cookTime);
            case CAMPFIRE -> new CampfireRecipe(key, i, input.getOption().getChoice(input.getItem()), experience, cookTime);
            default -> new FurnaceRecipe(key, i, input.getOption().getChoice(input.getItem()), experience, cookTime);
        };
    }

    private ItemStack recipeBookIcon(ItemStack i){
        List<String> def = TranslationManager.getListTranslation("default_recipe_description_cooking");
        String tinkerFormat = TranslationManager.getTranslation("tinker_result_format");
        ItemBuilder result = new ItemBuilder(this.result);
        return new ItemBuilder(i).lore(Arrays.asList(this.description == null ?
                        def.toArray(new String[0]) :
                        this.description
                                .replace("%input%", SlotEntry.toString(input))
                                .replace("%tinker%", tinker ? SlotEntry.toString(input) : ItemUtils.getItemName(result.getMeta()))
                                .replace("%result%", tinker ? tinkerFormat.replace("%item%", SlotEntry.toString(input)) : ItemUtils.getItemName(result.getMeta()))
                                .split("/n")
                )
        ).name(displayName == null ?
                (tinker ? tinkerFormat.replace("%item%", SlotEntry.toString(input)) : ItemUtils.getItemName(result.getMeta())) :
                displayName).translate().get();
    }

    public enum CookingRecipeType{
        SMOKER("smoking"),
        FURNACE("furnace"),
        BLAST_FURNACE("blasting"),
        CAMPFIRE("campfire");

        private final String category;
        CookingRecipeType(String category){
            this.category = category;
        }

        public String getCategory() {
            return category;
        }
    }
}
