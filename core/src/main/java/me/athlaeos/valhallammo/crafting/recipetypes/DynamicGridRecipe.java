package me.athlaeos.valhallammo.crafting.recipetypes;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.ToolRequirement;
import me.athlaeos.valhallammo.crafting.ToolRequirementType;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ResultChangingModifier;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This recipe type registers two recipes, one shaped and one not. <br>
 * If the recipe is a shapeless, the shaped recipe that will be registered will have the purpose of displaying the
 * ingredients properly within the recipe book. Shapeless recipes cannot do this. <br>
 * The shapeless recipe that is registered (only if the recipe is actually shapeless) will not be displayed in the
 * recipe book and has the purpose to actually allow the recipe to be crafted in a shapeless manner
 */
public class DynamicGridRecipe implements ValhallaRecipe, ValhallaKeyedRecipe {
    private final NamespacedKey shapedKey;
    private final NamespacedKey shapelessKey;
    private final String name;
    private String displayName = null;
    private String description = null;

    private Map<Integer, SlotEntry> items = new HashMap<>();
    private ItemStack result = new ItemBuilder(Material.WOODEN_SWORD).name("&r&fReplace me!").lore("&7I'm just a placeholder item!").get();
    private boolean requireValhallaTools = false;
    private boolean tinker = false;
    private int tinkerGridIndex = -1;
    private int toolIndex = -1;
    private boolean shapeless = false;
    private boolean hiddenFromBook = false;
    private List<DynamicItemModifier> modifiers = new ArrayList<>();
    private boolean unlockedForEveryone = false;
    private ToolRequirement toolRequirement = new ToolRequirement(ToolRequirementType.NOT_REQUIRED, -1);
    private Collection<String> validations = new HashSet<>();

    public DynamicGridRecipe(String name){
        this.name = name;
        this.shapedKey = new NamespacedKey(ValhallaMMO.getInstance(), "gridrecipe_" + name + "_shaped");
        this.shapelessKey = new NamespacedKey(ValhallaMMO.getInstance(), "gridrecipe_" + name + "_shapeless");
    }

    public String getName() { return name; }
    public @NotNull NamespacedKey getKey() { return shapedKey; }
    public NamespacedKey getKey2() { return shapelessKey; }

    @Override
    public void registerRecipe() {
        ShapedRecipe shaped = getShapedRecipe();
        ShapelessRecipe shapeless = getShapelessRecipe();
        if (ValhallaMMO.getInstance().getServer().getRecipe(shapedKey) != null) ValhallaMMO.getInstance().getServer().removeRecipe(shapedKey);
        if (ValhallaMMO.getInstance().getServer().getRecipe(shapelessKey) != null) ValhallaMMO.getInstance().getServer().removeRecipe(shapelessKey);
        if (shaped != null) ValhallaMMO.getInstance().getServer().addRecipe(shaped);
        else ValhallaMMO.logWarning("Could not generate recipe for " + getName() + ", it has no ingredients!");
        if (shapeless != null) ValhallaMMO.getInstance().getServer().addRecipe(shapeless);
    }

    @Override
    public void unregisterRecipe() {
        if (ValhallaMMO.getInstance().getServer().getRecipe(shapedKey) != null) ValhallaMMO.getInstance().getServer().removeRecipe(shapedKey);
        if (ValhallaMMO.getInstance().getServer().getRecipe(shapelessKey) != null) ValhallaMMO.getInstance().getServer().removeRecipe(shapelessKey);
    }

    public ToolRequirement getToolRequirement() { return toolRequirement; }
    public ItemStack getResult() { return result; }
    public List<DynamicItemModifier> getModifiers() { return modifiers; }
    public Map<Integer, SlotEntry> getItems() { return items; }
    public int getTinkerGridIndex() { return tinkerGridIndex; }
    public int getToolIndex() { return toolIndex; }
    public boolean requireValhallaTools() { return requireValhallaTools; }
    public boolean isShapeless() { return shapeless; }
    public boolean isUnlockedForEveryone() { return unlockedForEveryone; }
    public boolean tinker() { return tinker; }
    public Collection<String> getValidations() {return validations;}
    public String getDescription() { return description; }
    public String getDisplayName() { return displayName; }
    public void setHiddenFromBook(boolean hiddenFromBook) { this.hiddenFromBook = hiddenFromBook; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setDescription(String description) { this.description = description; }
    public void setToolRequirement(ToolRequirement toolRequirement) { this.toolRequirement = toolRequirement; }
    public void setUnlockedForEveryone(boolean unlockedForEveryone) { this.unlockedForEveryone = unlockedForEveryone; }
    public void setItems(Map<Integer, SlotEntry> items) { this.items = items; }
    public void setTinkerGridIndex(int tinkerGridIndex) { this.tinkerGridIndex = tinkerGridIndex; }
    public void setModifiers(List<DynamicItemModifier> modifiers) {
        this.modifiers = modifiers;
        DynamicItemModifier.sortModifiers(this.modifiers);
    }
    public void setRequireValhallaTools(boolean requireValhallaTools) { this.requireValhallaTools = requireValhallaTools; }
    public void setResult(ItemStack result) { this.result = result; }
    public void setShapeless(boolean shapeless) { this.shapeless = shapeless; }
    public void setTinker(boolean tinker) { this.tinker = tinker; }
    public void setToolIndex(int toolIndex) { this.toolIndex = toolIndex; }
    public void setValidations(Collection<String> validations) {this.validations = validations;}
    public boolean isHiddenFromBook() { return hiddenFromBook; }

    public ShapelessRecipe getShapelessRecipe(){
        if (!shapeless || this.items.isEmpty()) return null;
        ShapelessRecipe recipe = new ShapelessRecipe(shapelessKey, recipeBookIcon(tinker ? getGridTinkerEquipment().getItem() : result));
        for (Integer i : items.keySet()){
            SlotEntry entry = items.get(i);
            ItemStack item = entry.getItem().clone();
            ItemMeta meta = ItemUtils.getItemMeta(item);
            TranslationManager.translateItemMeta(meta);
            ItemUtils.setMetaNoClone(item, meta);
            RecipeChoice choice = entry.getOption() == null ? null : entry.getOption().getChoice(item); // if the ingredient or its choice are null, default to RecipeChoice.MaterialChoice
            if (choice == null) choice = new RecipeChoice.MaterialChoice(item.getType());
            if (i == toolIndex) choice = new RecipeChoice.MaterialChoice(ItemUtils.getNonAirMaterialsArray());

            recipe.addIngredient(choice);
        }

        return recipe;
    }

    public ShapedRecipe getShapedRecipe(){
        if (this.items.isEmpty()) return null;
        ShapedRecipe recipe = new ShapedRecipe(shapedKey, recipeBookIcon(tinker ? getGridTinkerEquipment().getItem() : result));
        ShapeDetails details = getRecipeShapeStrings();
        recipe.shape(details.shape);
        for (char ci : details.items.keySet()){
            SlotEntry entry = details.items.get(ci);
            ItemStack i = entry.getItem().clone();
            if (ItemUtils.isEmpty(i)) continue;
            ItemMeta meta = ItemUtils.getItemMeta(i);
            TranslationManager.translateItemMeta(meta);
            ItemUtils.setMetaNoClone(i, meta);
            RecipeChoice choice = entry.getOption() == null ? null : entry.getOption().getChoice(i);
            if (choice == null) choice = new RecipeChoice.MaterialChoice(i.getType());
            if (items.get(toolIndex) != null && items.get(toolIndex).equals(entry)) choice = new RecipeChoice.MaterialChoice(ItemUtils.getNonAirMaterialsArray());
            recipe.setIngredient(ci, choice);
        }

        return recipe;
    }

    private ItemStack recipeBookIcon(ItemStack i){
        ResultChangingModifier changer = (ResultChangingModifier) modifiers.stream().filter(m -> m instanceof ResultChangingModifier).reduce((first, second) -> second).orElse(null);
        if (changer != null) {
            i = Utils.thisorDefault(changer.getNewResult(ModifierContext.builder(new ItemBuilder(i)).get()), i);
        }
        List<String> gridDetails = new ArrayList<>();
        if (shapeless){
            String shapelessFormat = TranslationManager.getTranslation("ingredient_format_shapeless");
            Map<SlotEntry, Integer> contents = ItemUtils.getItemTotals(items.values());
            for (SlotEntry entry : contents.keySet()){
                int amount = contents.get(entry);
                gridDetails.add(shapelessFormat.replace("%amount%", String.valueOf(amount)).replace("%ingredient%", TranslationManager.translatePlaceholders(SlotEntry.toString(entry))));
            }
        } else {
            String shapeFormat = TranslationManager.getTranslation("ingredient_format_grid_shape");
            String charFormat = TranslationManager.getTranslation("ingredient_format_grid_ingredient");
            ShapeDetails details = getRecipeShapeStrings();
            for (String shapeLine : details.getShape()){
                gridDetails.add(shapeFormat.replace("%characters%", shapeLine));
            }
            for (Character c : details.getItems().keySet()){
                if (details.getItems().get(c) == null) continue;
                gridDetails.add(charFormat.replace("%character%", String.valueOf(c)).replace("%ingredient%", SlotEntry.toString(details.getItems().get(c))));
            }
        }
        SlotEntry tinkerItem = getGridTinkerEquipment();

        ItemBuilder result = new ItemBuilder(tinker ? (tinkerItem == null || ItemUtils.isEmpty(tinkerItem.getItem()) ? this.result : tinkerItem.getItem()) : this.result);
        List<String> def = TranslationManager.getListTranslation("default_recipe_description_grid");
        String tinkerFormat = TranslationManager.getTranslation("tinker_result_format");

        List<String> lore = new ArrayList<>();
        for (String l : this.description == null ? def.toArray(new String[0]) : this.description.split("/n")){
            lore.add(TranslationManager.translatePlaceholders(l.replace("%result%", tinker ? tinkerFormat.replace("%item%", SlotEntry.toString(getGridTinkerEquipment())) : ItemUtils.getItemName(result.getMeta()))
                    .replace("%tinker%", tinker ? SlotEntry.toString(getGridTinkerEquipment()) : ItemUtils.getItemName(result.getMeta()))));
        }

        ItemBuilder icon = new ItemBuilder(i)
                .lore(ItemUtils.setListPlaceholder(lore, "%ingredients%", gridDetails))
                .translate();
        if (displayName != null) icon.name(TranslationManager.translatePlaceholders(displayName).replace("%item%", ItemUtils.getItemName(result.getMeta())));
        else if (tinker) icon.name(tinkerFormat.replace("%item%", SlotEntry.toString(tinkerItem)));
        else if (result.getMeta().hasDisplayName()) icon.name(result.getMeta().getDisplayName());

        return icon.translate().get();
    }

    public SlotEntry getGridTinkerEquipment(){
        if (tinkerGridIndex >= 0 && items.containsKey(tinkerGridIndex) && !ItemUtils.isEmpty(items.get(tinkerGridIndex).getItem()))
            return items.get(tinkerGridIndex);
        SlotEntry tinker = null;
        for (int i = 0; i < 9; i++){
            SlotEntry entry = items.get(i);
            if (entry == null) continue;
            ItemStack matrixItem = entry.getItem();
            if (ItemUtils.isEmpty(matrixItem)) continue;
            if (tinker == null) tinker = entry; // return the first item found in the grid if no others are found
            if (EquipmentClass.getMatchingClass(ItemUtils.getItemMeta(matrixItem)) != null) return entry;
        }
        return tinker;
    }

    public ShapeDetails getRecipeShapeStrings(){
        List<String> shape = new ArrayList<>();
        BiMap<Character, SlotEntry> ingredientMap = HashBiMap.create();
        int i = 0;
        StringBuilder usedChars = new StringBuilder();
        for (int r = 0; r < 3; r++) {
            StringBuilder row = new StringBuilder();
            for (int c = 0; c < 3; c++){
                SlotEntry entry = items.get(i);
                i++;
                if (entry == null) {
                    row.append(' ');
                    continue;
                }
                SlotEntry entry2 = ingredientMap.values().stream().filter(entry::isSimilar).findAny().orElse(null);
                Character itemChar = entry2 == null ? null : ingredientMap.inverse().get(entry2);
                if (itemChar == null) itemChar = getItemChar(entry, usedChars.toString());
                row.append(itemChar);
                if (!ingredientMap.containsValue(entry)) {
                    usedChars.append(itemChar);
                    ingredientMap.put(itemChar, entry);
                }
            }
            shape.add(row.toString());
        }

        // trimming shape
        shape = trimShape(shape);

        return new ShapeDetails(shape.toArray(new String[0]), ingredientMap);
    }

    public static List<String> trimShape(List<String> shape){
        if (shape.get(0).equalsIgnoreCase("   ") && shape.get(1).equalsIgnoreCase("   ")) { shape.remove(0); shape.remove(0); } // if top two rows are empty, remove them
        else if (shape.get(0).equalsIgnoreCase("   ")) shape.remove(0); // if only top row is empty, remove it

        if (shape.size() == 3 && shape.get(shape.size() - 1).equalsIgnoreCase("   ") && shape.get(shape.size() - 2).equalsIgnoreCase("   ")) { shape.remove(shape.size() - 1); shape.remove(shape.size() - 1); } // if bottom two rows are empty, remove them
        else if (shape.get(shape.size() - 1).equalsIgnoreCase("   ")) shape.remove(shape.size() - 1); // if only bottom row is empty, remove it

        if (shape.stream().allMatch(s -> s.endsWith("  "))) shape = shape.stream().map(s -> s.substring(0, s.length() - 2)).collect(Collectors.toList()); // if last 2 characters of each line is empty, remove them
        if (shape.stream().allMatch(s -> s.endsWith(" "))) shape = shape.stream().map(s -> s.substring(0, s.length() - 1)).collect(Collectors.toList()); // if last character of each line is empty, remove it
        if (shape.stream().allMatch(s -> s.startsWith("  "))) shape = shape.stream().map(s -> s.substring(2)).collect(Collectors.toList()); // if first 2 characters of each line is empty, remove them
        if (shape.stream().allMatch(s -> s.startsWith(" "))) shape = shape.stream().map(s -> s.substring(1)).collect(Collectors.toList()); // if first character of each line is empty, remove it
        return shape;
    }

    private char getItemChar(SlotEntry i, String usedChars){
        if (i == null) return ' ';
        if (ItemUtils.isEmpty(i.getItem())) return ' ';
        String itemName = ChatColor.stripColor(Utils.chat(TranslationManager.translatePlaceholders(SlotEntry.toString(i))));
        char possibleCharacter = (itemName.isEmpty() ? i.getItem().getType().toString() : itemName).toUpperCase(java.util.Locale.US).charAt(0);
        if (usedChars.contains(String.valueOf(possibleCharacter))){
            possibleCharacter = i.getItem().getType().toString().toUpperCase(java.util.Locale.US).charAt(0);
            if (usedChars.contains(String.valueOf(possibleCharacter))) {
                for (Character c : Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I')) {
                    if (!usedChars.contains(String.valueOf(c))) {
                        return c;
                    }
                }
            }
        }
        return possibleCharacter;
    }

    public static class ShapeDetails{
        String[] shape;
        Map<Character, SlotEntry> items;

        public ShapeDetails(String[] shape, Map<Character, SlotEntry> items){
            this.shape = shape;
            this.items = items;
        }

        public String[] getShape() {
            return shape;
        }

        public Map<Character, SlotEntry> getItems() {
            return items;
        }
    }
}
