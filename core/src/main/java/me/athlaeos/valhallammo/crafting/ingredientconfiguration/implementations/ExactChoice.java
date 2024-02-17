package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.utility.ItemUtils;
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
                "&aRequire this ingredient to match",
                "&athe inserted item exactly.",
                "",
                "&cIncompatible with damageable items",
                "&c(only need to match in base type)").get();
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
    public RecipeChoice getChoice(ItemStack i) {
        return new RecipeChoice.MaterialChoice(i.getType());
    }

    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        if (EquipmentClass.getMatchingClass(ItemUtils.getItemMeta(i2)) != null) return i1.getType() == i2.getType();
        return ItemUtils.isSimilar(i1, i2);
    }

    @Override
    public String ingredientDescription(ItemStack base) {
        return ItemUtils.getItemName(ItemUtils.getItemMeta(base));
    }

    @Override
    public RecipeOption getNew() {
        return new ExactChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }
}
