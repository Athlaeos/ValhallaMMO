package me.athlaeos.valhallammo.gui.implementations.recipecategories;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCauldronRecipe;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.CauldronRecipeEditor;
import me.athlaeos.valhallammo.gui.implementations.RecipeOverviewMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.SmithingTransformRecipeWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import java.util.*;
import java.util.stream.Collectors;

public class DisabledRecipesCategory extends RecipeCategory{
    private static final NamespacedKey SORT_PRIORITY = new NamespacedKey(ValhallaMMO.getInstance(), "recipe_category_priority");
    private static final MaterialChoice choice = new MaterialChoice();

    public DisabledRecipesCategory(int position) {
        super("disabled", new ItemBuilder(Material.BARRIER).name("&cDisabled Vanilla Recipes").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF30E\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_recipecategory_disabled")));
    }

    @Override
    public List<ItemStack> getRecipeButtons() {
        List<ItemStack> icons = new ArrayList<>();
        Iterator<Recipe> recipes = ValhallaMMO.getInstance().getServer().recipeIterator();
        while (recipes.hasNext()){
            Recipe recipe = recipes.next();
            if (!(recipe instanceof Keyed k) || ItemUtils.isEmpty(recipe.getResult())) continue;
            if (CustomRecipeRegistry.getAllKeyedRecipes().containsKey(k.getKey()) || !k.getKey().getNamespace().equalsIgnoreCase(NamespacedKey.MINECRAFT)) continue;
            int priority;
            String prefix;
            List<String> lore = new ArrayList<>();
            if (recipe instanceof ShapedRecipe r){
                priority = 0;
                prefix = "&7[&eShaped&7] ";
                ShapeDetails details = getRecipeShapeStrings(r);
                for (String shapeLine : details.getShape()){
                    lore.add("&7[&e" + shapeLine + "&7]&7");
                }
                for (Character c : details.getItems().keySet()){
                    if (details.getItems().get(c) == null) continue;
                    lore.add("&e" + c + "&7: &e" + ItemUtils.getItemName(ItemUtils.getItemMeta(details.getItems().get(c))));
                }
            } else if (recipe instanceof ShapelessRecipe r){
                priority = 1;
                prefix = "&7[&aShapeless&7] ";
                Map<SlotEntry, Integer> contents = ItemUtils.getItemTotals(r.getIngredientList().stream().map(i -> new SlotEntry(i, choice)).collect(Collectors.toList()));
                for (SlotEntry entry : contents.keySet()){
                    int amount = contents.get(entry);
                    lore.add("&e" + amount + "&7x &e" + SlotEntry.toString(entry));
                }
            } else if (recipe instanceof CookingRecipe<?> r){
                priority = 2;
                prefix = "&7[&6Cooking&7] ";
                ItemStack base = convertChoice(r.getInputChoice());
                lore.add("&e" + choice.ingredientDescription(base) + " &f>>> &e" + ItemUtils.getItemName(ItemUtils.getItemMeta(r.getResult())));
            } else if (recipe instanceof SmithingRecipe r){
                priority = 3;
                prefix = "&7[&8Smithing&7] ";
                boolean templateCompatible = MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20);
                ItemStack template = templateCompatible ? SmithingTransformRecipeWrapper.getTemplate(r) : null;
                ItemStack base = convertChoice(r.getBase());
                ItemStack addition = convertChoice(r.getAddition());

                if (templateCompatible && template != null)
                    lore.add("&eTemplate: &f" + choice.ingredientDescription(template));

                lore.add("&eBase: &f" + choice.ingredientDescription(base));
                lore.add("&eAddition: &f" + choice.ingredientDescription(addition));
                lore.add("&f&m                          &r&f =");
                lore.add("&e" + ItemUtils.getItemName(ItemUtils.getItemMeta(r.getResult())));
            } else continue;
            boolean enabled = !CustomRecipeRegistry.getDisabledRecipes().contains(((Keyed) recipe).getKey());
            icons.add(new ItemBuilder(recipe.getResult())
                    .name(prefix + (enabled ? "&a" : "&c") + StringUtils.toPascalCase(((Keyed) recipe).getKey().getKey().replace("-", " ").replace("_", " ")))
                    .lore(lore)
                    .intTag(SORT_PRIORITY, priority)
                    .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DYE)
                    .stringTag(RecipeOverviewMenu.KEY_RECIPE, ((Keyed) recipe).getKey().getKey()).get());
        }
        icons.sort(Comparator.comparingInt((ItemStack i) -> ItemUtils.getPDCInt(SORT_PRIORITY, i, 999)).thenComparing(ItemStack::getType).thenComparing(item -> ChatColor.stripColor(ItemUtils.getItemName(ItemUtils.getItemMeta(item)))));
        return icons;
    }

    private ItemStack convertChoice(RecipeChoice choice){
        if (choice instanceof RecipeChoice.MaterialChoice m){
            return m.getItemStack();
        } else if (choice instanceof RecipeChoice.ExactChoice m){
            return m.getItemStack();
        } else return null;
    }

    @Override
    public void onRecipeButtonClick(String recipe, Player editor) {
        try {
            NamespacedKey keyToToggle = NamespacedKey.minecraft(recipe);
            if (CustomRecipeRegistry.getDisabledRecipes().contains(keyToToggle)) CustomRecipeRegistry.removeDisabledRecipe(keyToToggle);
            else CustomRecipeRegistry.addDisabledRecipe(keyToToggle);
            CustomRecipeRegistry.setChangesMade();
        } catch (IllegalArgumentException ignored) {}
    }

    private ShapeDetails getRecipeShapeStrings(ShapedRecipe recipe){
        List<String> shape = new ArrayList<>();
        BiMap<Character, ItemStack> ingredientMap = HashBiMap.create();

        StringBuilder usedChars = new StringBuilder();
        for (String row : recipe.getShape()){
            StringBuilder newRow = new StringBuilder();
            for (char c : row.toCharArray()){
                ItemStack charItem = recipe.getIngredientMap().get(c);
                Character itemChar = ingredientMap.inverse().get(charItem);
                if (itemChar == null) itemChar = getItemChar(charItem, usedChars.toString());
                newRow.append(itemChar);
                if (!ingredientMap.containsValue(charItem)){
                    usedChars.append(itemChar);
                    ingredientMap.put(itemChar, charItem);
                }
            }
            shape.add(newRow.toString());
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
        String itemName = ChatColor.stripColor(ItemUtils.getItemName(ItemUtils.getItemMeta(i)));
        char possibleCharacter = (itemName.isEmpty() ? i.getType().toString() : itemName).toUpperCase().charAt(0);
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

    private static class ShapeDetails{
        String[] shape;
        Map<Character, ItemStack> items;

        public ShapeDetails(String[] shape, Map<Character, ItemStack> items){
            this.shape = shape;
            this.items = items;
        }

        public String[] getShape() {
            return shape;
        }

        public Map<Character, ItemStack> getItems() {
            return items;
        }
    }

    @Override
    public void createNew(String name, Player editor) {
        // do nothing
    }
}