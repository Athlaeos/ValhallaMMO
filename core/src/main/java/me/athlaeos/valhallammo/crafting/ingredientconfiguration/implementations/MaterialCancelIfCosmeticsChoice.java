package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;

public class MaterialCancelIfCosmeticsChoice extends RecipeOption implements IngredientChoice {

    @Override
    public String getName() {
        return "CHOICE_MATERIAL_NO_COSMETICS";
    }

    @Override
    public String getActiveDescription() {
        return "This ingredient will only need to match in item base type, and must not have a custom display name or lore";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.PAPER).name("&7Selective base type requirement")
        .lore(
                "&aRequire this ingredient to match",
                "&athe inserted item only in base type.",
                "&cIf the item has a custom name or",
                "&clore, it will not match.").get();
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
        ItemMeta meta = i2.getItemMeta();
        if (meta == null || meta.hasDisplayName() || meta.hasLore()) return false;
        return i1.getType() == i2.getType();
    }

    @Override
    public String ingredientDescription(ItemStack base) {
        return TranslationManager.getMaterialTranslation(base.getType());
    }

    @Override
    public RecipeOption getNew() {
        return new MaterialCancelIfCosmeticsChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }
}
