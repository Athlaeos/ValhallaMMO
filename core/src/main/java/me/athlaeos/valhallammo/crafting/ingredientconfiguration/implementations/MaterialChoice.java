package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class MaterialChoice extends RecipeOption {

    @Override
    public String getName() {
        return "CHOICE_MATERIAL";
    }

    @Override
    public String getActiveDescription() {
        return "This ingredient will only need to match in item base type";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.PAPER).name("&7Base type requirement")
        .lore(
            "&aRequire this ingredient to match",
            "&athe inserted item only in base type.").get();
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
        return i1.getType() == i2.getType();
    }

    @Override
    public String ingredientDescription(ItemStack base) {
        return TranslationManager.getMaterialTranslation(base.getType());
    }

    @Override
    public RecipeOption getNew() {
        return new MaterialChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }
}
