package me.athlaeos.valhallammo.crafting.recipetypes;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ResultChangingModifier;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.SmithingTransformRecipeWrapper;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DynamicSmithingRecipe implements ValhallaRecipe, ValhallaKeyedRecipe {
    private final NamespacedKey key;
    private final String name;
    private String displayName = null;
    private String description = null;

    private SlotEntry template = new SlotEntry(
            new ItemBuilder(ItemUtils.stringToMaterial("NETHERITE_UPGRADE_SMITHING_TEMPLATE", Material.PAPER)).name("&r&fReplace me!").lore("&7I'm just a placeholder template!").get(),
            new MaterialChoice());
    private SlotEntry base = new SlotEntry(
            new ItemBuilder(Material.DIAMOND_SWORD).name("&r&fReplace me!").lore("&7I'm just a placeholder base!").get(),
            new MaterialChoice());
    private SlotEntry addition = new SlotEntry(
            new ItemBuilder(Material.NETHERITE_INGOT).name("&r&fReplace me!").lore("&7I'm just a placeholder addition!").get(),
            new MaterialChoice());
    private ItemStack result = new ItemBuilder(Material.NETHERITE_SWORD).name("&r&fReplace me!").lore("&7I'm just a placeholder result!").get();
    private boolean requireValhallaTools = false;
    private boolean tinkerBase = false;
    private boolean consumeAddition = true;
    private boolean hiddenFromBook = false;
    private List<DynamicItemModifier> resultModifiers = new ArrayList<>();
    private List<DynamicItemModifier> additionModifiers = new ArrayList<>();
    private boolean unlockedForEveryone = false;
    private Collection<String> validations = new HashSet<>();

    public DynamicSmithingRecipe(String name){
        this.name = name;
        this.key = new NamespacedKey(ValhallaMMO.getInstance(), "smithingrecipe_" + name);
    }

    @Override
    public void registerRecipe() {
        // if (hasEquivalentVanillaSmithingRecipe(template.getItem(), base.getItem(), addition.getItem())) return; // does not need to be registered because an identical vanilla recipe already exists
        Recipe recipe = generateRecipe();
        if (ValhallaMMO.getInstance().getServer().getRecipe(key) != null) ValhallaMMO.getInstance().getServer().removeRecipe(key);
        if (recipe != null) ValhallaMMO.getInstance().getServer().addRecipe(recipe);
    }

    @Override
    public void unregisterRecipe() {
        if (ValhallaMMO.getInstance().getServer().getRecipe(key) != null) ValhallaMMO.getInstance().getServer().removeRecipe(key);
    }

    private ItemStack translate(ItemStack i){
        List<String> def = TranslationManager.getListTranslation("default_recipe_description_smithing");
        String tinkerFormat = TranslationManager.getTranslation("tinker_result_format");
        ItemBuilder result = new ItemBuilder(this.result);
        return new ItemBuilder(i).lore(Arrays.asList(this.description == null ?
                        def.toArray(new String[0]) :
                        this.description
                                .replace("%template%", SlotEntry.toString(template))
                                .replace("%base%", SlotEntry.toString(base))
                                .replace("%tinker%", tinkerBase ? SlotEntry.toString(base) : ItemUtils.getItemName(result.getMeta()))
                                .replace("%result%", tinkerBase ? tinkerFormat.replace("%item%", SlotEntry.toString(base)) : ItemUtils.getItemName(result.getMeta()))
                                .replace("%addition%", SlotEntry.toString(addition))
                                .split("/n")
                )
        ).name(displayName == null ?
                (tinkerBase ? tinkerFormat.replace("%item%", SlotEntry.toString(base)) : ItemUtils.getItemName(result.getMeta())) :
                displayName).translate().get();
    }

    public String getName() {return name;}
    public @NotNull NamespacedKey getKey() {return key;}
    @Override public ItemStack getResult() { return result; }
    public SlotEntry getTemplate() {return template;}
    public SlotEntry getBase() {return base;}
    public SlotEntry getAddition() {return addition;}
    public boolean requireValhallaTools() {return requireValhallaTools;}
    public boolean tinkerBase() {return tinkerBase;}
    public List<DynamicItemModifier> getResultModifiers() {return resultModifiers;}
    public List<DynamicItemModifier> getAdditionModifiers() {return additionModifiers;}
    public boolean isUnlockedForEveryone() {return unlockedForEveryone;}
    public Collection<String> getValidations() {return validations;}
    public boolean consumeAddition() { return consumeAddition; }
    public String getDescription() { return description; }
    public String getDisplayName() { return displayName; }
    public boolean isHiddenFromBook() { return hiddenFromBook; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setDescription(String description) { this.description = description; }
    public void setResult(ItemStack result) { this.result = result; }
    public void setTemplate(SlotEntry template) {this.template = template;}
    public void setBase(SlotEntry base) {this.base = base;}
    public void setAddition(SlotEntry addition) {this.addition = addition;}
    public void setRequireValhallaTools(boolean requireValhallaTools) {this.requireValhallaTools = requireValhallaTools;}
    public void setTinkerBase(boolean tinkerBase) {this.tinkerBase = tinkerBase;}
    public void setResultModifiers(List<DynamicItemModifier> resultModifiers) {
        this.resultModifiers = resultModifiers;
        DynamicItemModifier.sortModifiers(this.resultModifiers);
    }
    public void setAdditionModifiers(List<DynamicItemModifier> additionModifiers) {
        this.additionModifiers = additionModifiers;
        DynamicItemModifier.sortModifiers(this.additionModifiers);
    }
    public void setUnlockedForEveryone(boolean unlockedForEveryone) {this.unlockedForEveryone = unlockedForEveryone;}
    public void setValidations(Collection<String> validations) {this.validations = validations;}
    public void setConsumeAddition(boolean consumeAddition) { this.consumeAddition = consumeAddition; }
    public void setHiddenFromBook(boolean hiddenFromBook) { this.hiddenFromBook = hiddenFromBook; }

    /**
     * Converts the recipe's template item to an updated item if possible
     */
    public void convertTemplate(){
        ItemStack t = template == null ? null : template.getItem();
        if (t == null) return;
        ItemMeta meta = t.getItemMeta();
        if (meta == null) return;
        if (!meta.hasDisplayName()) return;
        String displayName = meta.getDisplayName();
        if (!displayName.contains("REPLACEWITH:")) return;
        String[] args = displayName.split("REPLACEWITH:");
        if (args.length != 2) return;
        Material m = Catch.catchOrElse(() -> Material.valueOf(args[1]), null);
        if (m == null) return;
        template.setExactIngredient(new ItemStack(m));
    }

    @SuppressWarnings("deprecation")
    public SmithingRecipe generateRecipe() {
        RecipeChoice t = null;
        if (template != null) t = template.getOption() == null ?
                new RecipeChoice.MaterialChoice(template.getItem().getType()) :
                template.getOption().getChoice(template.getItem());
        if (base == null || ItemUtils.isEmpty(base.getItem()) ||
                addition == null || ItemUtils.isEmpty(addition.getItem())) return null;
        RecipeChoice b = base.getOption() == null ?
                new RecipeChoice.MaterialChoice(base.getItem().getType()) :
                base.getOption().getChoice(base.getItem());
        RecipeChoice a = addition.getOption() == null ?
                new RecipeChoice.MaterialChoice(addition.getItem().getType()) :
                addition.getOption().getChoice(addition.getItem());

        ItemStack i = result.clone();
        ResultChangingModifier changer = (ResultChangingModifier) resultModifiers.stream().filter(m -> m instanceof ResultChangingModifier).reduce((first, second) -> second).orElse(null);
        if (changer != null) {
            i = Utils.thisorDefault(changer.getNewResult(ModifierContext.builder(new ItemBuilder(i)).get()), i);
        }
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20) && t != null){
            return SmithingTransformRecipeWrapper.get(key, translate(i), t, b, a); // using a SmithingTransformRecipe directly results in a ClassNotFoundException on versions lower than 1.20
        } else return new SmithingRecipe(key, translate(i), b, a);
    }

    private boolean hasEquivalentVanillaSmithingRecipe(ItemStack template, ItemStack base, ItemStack addition){
        if (base == null || addition == null) return false;
        boolean isTemplateCompatible = MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20);
        Iterator<Recipe> iterator = ValhallaMMO.getInstance().getServer().recipeIterator();
        while (iterator.hasNext()){
            if (iterator.next() instanceof SmithingRecipe s && s.getBase().test(base) && s.getAddition().test(addition)){
                if (s.getBase().test(base) && s.getAddition().test(addition)) {
                    if (isTemplateCompatible && (!SmithingTransformRecipeWrapper.templatesMatch(s, template))) continue;
                    return true;
                }
            }
        }
        return false;
    }
}
