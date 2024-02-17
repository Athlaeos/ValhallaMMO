package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.item.CustomID;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;

public class MaterialWithDataChoice extends RecipeOption implements IngredientChoice {

    @Override
    public String getName() {
        return "CHOICE_MATERIAL_DATA";
    }

    @Override
    public String getActiveDescription() {
        return "This ingredient will need to match in item base type AND custom model data";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.RED_DYE).name("&7Material-data Requirement")
        .lore(
            "&aRequire this ingredient to match",
            "&abase type and custom model data.",
            "",
            "&7The name of the ingredient will be",
            "&7used to communicate item requirement",
            "&7to the player.").get();
    }

    @Override
    public boolean isCompatible(ItemStack i) {
        return true;
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
        if (i1.getType() != i2.getType()) return false;
        ItemMeta i1Meta = i1.getItemMeta();
        ItemMeta i2Meta = i2.getItemMeta();
        if (i1Meta == null && i2Meta == null) return true;
        if (i1Meta == null || i2Meta == null) return false;
        if (!i1Meta.hasCustomModelData() && !i2Meta.hasCustomModelData()) return true;
        if (!i1Meta.hasCustomModelData() || !i2Meta.hasCustomModelData()) return false;
        return i1.getType() == i2.getType() && i1Meta.getCustomModelData() == i2Meta.getCustomModelData();
    }

    @Override
    public String ingredientDescription(ItemStack base) {
        return ItemUtils.getItemName(ItemUtils.getItemMeta(base));
    }

    @Override
    public RecipeOption getNew() {
        return new MaterialWithDataChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }
}
