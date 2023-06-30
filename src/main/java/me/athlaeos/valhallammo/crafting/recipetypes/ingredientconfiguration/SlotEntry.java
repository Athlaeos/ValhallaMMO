package me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration;

import org.bukkit.inventory.ItemStack;

public class SlotEntry {
    private ItemStack exactIngredient;
    private IngredientChoice option;
    private SlotIngredientBehavior behavior;
    public SlotEntry(ItemStack item, IngredientChoice option, SlotIngredientBehavior behavior){
        this.exactIngredient = item;
        this.option = option;
        this.behavior = behavior;
    }

    public IngredientChoice getOption() {
        return option;
    }

    public SlotIngredientBehavior getBehavior() {
        return behavior;
    }

    public ItemStack getItem() {
        return exactIngredient;
    }

    public void setBehavior(SlotIngredientBehavior behavior) {
        this.behavior = behavior;
    }

    public void setExactIngredient(ItemStack exactIngredient) {
        this.exactIngredient = exactIngredient;
    }

    public void setOption(IngredientChoice option) {
        this.option = option;
    }
}
