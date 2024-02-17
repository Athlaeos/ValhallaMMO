package me.athlaeos.valhallammo.crafting.ingredientconfiguration;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SlotEntry {
    private ItemStack exactIngredient;
    private IngredientChoice option;
    public SlotEntry(ItemStack item, IngredientChoice option){
        this.exactIngredient = item;
        this.option = option;
    }

    public IngredientChoice getOption() {
        return option;
    }

    public ItemStack getItem() {
        return exactIngredient;
    }

    public void setExactIngredient(ItemStack exactIngredient) {
        this.exactIngredient = exactIngredient;
    }

    public void setOption(IngredientChoice option) {
        this.option = option == null ? new MaterialChoice() : option;
    }

    public static String toString(SlotEntry entry){
        if (entry == null || ItemUtils.isEmpty(entry.getItem())) return "&4Invalid entry";
        return entry.getOption() == null ? StringUtils.toPascalCase(entry.getItem().getType().toString()) : entry.getOption().ingredientDescription(entry.getItem());
    }

    @SuppressWarnings("all")
    public static List<String> getOptionLore(SlotEntry entry){
        List<String> lore = new ArrayList<>();
        if (entry == null) return lore;
        if (entry.getOption() != null && entry.getOption() instanceof RecipeOption r){
            lore.add(Utils.chat("&8&m                <>                "));
            lore.addAll(StringUtils.separateStringIntoLines("&a" + r.getActiveDescription(), 40));
        }
        return lore;
    }

    public boolean isSimilar(SlotEntry compareTo){
        if (compareTo == null) return false;
        if (!ItemUtils.isSimilar(this.getItem(), compareTo.getItem())) return false;
        return this.getOption().ingredientDescription(this.getItem()).equals(compareTo.getOption().ingredientDescription(compareTo.getItem()));
    }
}
