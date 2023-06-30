package me.athlaeos.valhallammo.crafting.recipetypes;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.ToolRequirement;
import me.athlaeos.valhallammo.crafting.ToolRequirementType;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.*;
import java.util.stream.Collectors;

public class DynamicGridRecipe {
    private final NamespacedKey key;
    private final String name;
    private Map<Integer, SlotEntry> items = new HashMap<>();
    private ItemStack result = new ItemBuilder(Material.WOODEN_SWORD).name("&r&fReplace me!").lore("&7I'm just a placeholder item!").get();
    private boolean requireValhallaTools = false;
    private boolean tinker = false;
    private int tinkerGridIndex = -1;
    private boolean shapeless = false;
    private List<DynamicItemModifier> modifiers = new ArrayList<>();
    private boolean unlockedForEveryone = false;
    private ToolRequirement toolRequirement = new ToolRequirement(ToolRequirementType.NOT_REQUIRED, -1);

    public DynamicGridRecipe(String name){
        this.name = name;
        this.key = new NamespacedKey(ValhallaMMO.getInstance(), "gridrecipe_" + name);
    }

    public String getName() { return name; }
    public NamespacedKey getKey() { return key; }
    public ToolRequirement getToolRequirement() { return toolRequirement; }
    public ItemStack getResult() { return result; }
    public List<DynamicItemModifier> getModifiers() { return modifiers; }
    public Map<Integer, SlotEntry> getItems() { return items; }
    public int getTinkerGridIndex() { return tinkerGridIndex; }
    public boolean isRequireValhallaTools() { return requireValhallaTools; }
    public boolean isShapeless() { return shapeless; }
    public boolean isUnlockedForEveryone() { return unlockedForEveryone; }
    public boolean isTinker() { return tinker; }
    public void setToolRequirement(ToolRequirement toolRequirement) { this.toolRequirement = toolRequirement; }
    public void setUnlockedForEveryone(boolean unlockedForEveryone) { this.unlockedForEveryone = unlockedForEveryone; }
    public void setItems(Map<Integer, SlotEntry> items) { this.items = items; }
    public void setTinkerGridIndex(int tinkerGridIndex) { this.tinkerGridIndex = tinkerGridIndex; }
    public void setModifiers(List<DynamicItemModifier> modifiers) { this.modifiers = modifiers; }
    public void setRequireValhallaTools(boolean requireValhallaTools) { this.requireValhallaTools = requireValhallaTools; }
    public void setResult(ItemStack result) { this.result = result; }
    public void setShapeless(boolean shapeless) { this.shapeless = shapeless; }
    public void setTinker(boolean tinker) { this.tinker = tinker; }

    public Recipe generateRecipe() {
        if (this.items.isEmpty()) return null;
        if (shapeless){
            ShapelessRecipe recipe = new ShapelessRecipe(key, tinker ? getGridTinkerEquipment() : result);
            for (Integer i : items.keySet()){
                SlotEntry entry = items.get(i);
                ItemStack item = entry.getItem().clone();
                IngredientChoice ingredient = entry.getOption();
                RecipeChoice choice = ingredient.getChoice(item, true); // if the ingredient or its choice are null, default to RecipeChoice.MaterialChoice
                if (choice == null) choice = new RecipeChoice.MaterialChoice(item.getType());

                recipe.addIngredient(choice);
            }
            return recipe;
        } else {
            ShapedRecipe recipe = new ShapedRecipe(key, tinker ? getGridTinkerEquipment() : result);
            ShapeDetails details = getRecipeShapeStrings();
            recipe.shape(details.shape);
            for (char ci : details.items.keySet()){
                SlotEntry entry = details.items.get(ci);
                ItemStack i = entry.getItem();
                if (ItemUtils.isEmpty(i)) continue;
                RecipeChoice choice = entry.getOption().getChoice(i, false);
                if (choice == null) choice = new RecipeChoice.MaterialChoice(i.getType());
                recipe.setIngredient(ci, choice);
            }
            return recipe;
        }
    }

    public ItemStack getGridTinkerEquipment(){
        if (tinkerGridIndex >= 0 && items.containsKey(tinkerGridIndex) && !ItemUtils.isEmpty(items.get(tinkerGridIndex).getItem()))
            return items.get(tinkerGridIndex).getItem();
        for (int i = 0; i < 9; i++){
            SlotEntry entry = items.get(i);
            if (entry == null) continue;
            ItemStack matrixItem = entry.getItem();
            if (ItemUtils.isEmpty(matrixItem)) continue;

            if (EquipmentClass.getMatchingClass(matrixItem) != null) return matrixItem.clone();
        }
        return null;
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
                ItemStack item = entry.getItem();
                i++;
                Character itemChar = ingredientMap.inverse().get(entry);
                if (itemChar == null) itemChar = getItemChar(item, usedChars.toString());
                row.append(itemChar);
                if (!ingredientMap.containsValue(entry)) {
                    usedChars.append(itemChar);
                    ingredientMap.put(itemChar, entry);
                }
            }
            shape.add(row.toString());
        }

        // trimming shape
        if (shape.get(0).equalsIgnoreCase("   ") && shape.get(1).equalsIgnoreCase("   ")) { shape.remove(0); shape.remove(0); } // if top two rows are empty, remove them
        else if (shape.get(0).equalsIgnoreCase("   ")) shape.remove(0); // if only top row is empty, remove it

        if (shape.size() == 3 && shape.get(shape.size() - 1).equalsIgnoreCase("   ") && shape.get(shape.size() - 2).equalsIgnoreCase("   ")) { shape.remove(shape.size() - 1); shape.remove(shape.size() - 1); } // if bottom two rows are empty, remove them
        else if (shape.get(shape.size() - 1).equalsIgnoreCase("   ")) shape.remove(shape.size() - 1); // if only bottom row is empty, remove it

        if (shape.stream().allMatch(s -> s.endsWith("  "))) shape = shape.stream().map(s -> s = s.substring(0, s.length() - 2)).collect(Collectors.toList()); // if last 2 characters of each line is empty, remove them
        if (shape.stream().allMatch(s -> s.endsWith(" "))) shape = shape.stream().map(s -> s = s.substring(0, s.length() - 1)).collect(Collectors.toList()); // if last character of each line is empty, remove it
        if (shape.stream().allMatch(s -> s.startsWith("  "))) shape = shape.stream().map(s -> s = s.substring(2)).collect(Collectors.toList()); // if first 2 characters of each line is empty, remove them
        if (shape.stream().allMatch(s -> s.startsWith(" "))) shape = shape.stream().map(s -> s = s.substring(1)).collect(Collectors.toList()); // if first character of each line is empty, remove it

        return new ShapeDetails(shape.toArray(new String[0]), ingredientMap);
    }

    private char getItemChar(ItemStack i, String usedChars){
        if (ItemUtils.isEmpty(i)) return ' ';
        String itemName = ChatColor.stripColor(ItemUtils.getItemName(i));
        char possibleCharacter = (itemName == null || itemName.isEmpty() ? i.getType().toString() : itemName).toUpperCase().charAt(0);
        if (usedChars.contains(String.valueOf(possibleCharacter))){
            possibleCharacter = i.getType().toString().toUpperCase().charAt(0);
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
