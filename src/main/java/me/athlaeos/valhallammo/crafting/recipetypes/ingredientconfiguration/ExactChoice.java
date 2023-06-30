package me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import me.athlaeos.valhallammo.item.ItemBuilder;

public class ExactChoice extends RecipeOption implements IngredientChoice {

    @Override
    public String getName() {
        return "CHOICE_EXACT";
    }

    @Override
    public String getActiveDescription() {
        return "This ingredient will need to match the item exactly";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.ENCHANTED_BOOK).name("&7Exact meta requirement")
        .lore(
            "&eDrag-and-drop onto an ingredient", 
            "&eto require this ingredient to match", 
            "&ethe inserted item exactly.", 
            "&7Not compatible with damageable items").get();
    }

    @Override
    public boolean isCompatible(ItemStack i) {
        return i.getType().getMaxDurability() <= 0;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public RecipeChoice getChoice(ItemStack i, boolean isShapeless) {
        return isShapeless ? new RecipeChoice.MaterialChoice(i.getType()) : new RecipeChoice.ExactChoice(i);
    }

    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        ItemStack i1Clone = i1.clone();
        ItemStack i2Clone = i2.clone();
        i1Clone.setAmount(1);
        i2Clone.setAmount(1);
        return i1Clone.toString().equals(i2Clone.toString());
    }

    @Override
    public RecipeOption getNew() {
        return new ExactChoice();
    }
    
}
