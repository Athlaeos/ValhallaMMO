package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class ArmorChoice extends RecipeOption {

    @Override
    public String getName() {
        return "CHOICE_ARMOR_ANY";
    }

    @Override
    public String getActiveDescription() {
        return "The ingredient can be replaced with any piece of armor";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.IRON_CHESTPLATE).name("&7Armor")
        .lore(
            "&aIngredient may be substituted",
            "&awith any piece of armor")
            .get();
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
    public RecipeOption getNew() {
        return new ArmorChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }

    @Override
    public RecipeChoice getChoice(ItemStack i) {
        return new RecipeChoice.MaterialChoice(ItemUtils.getNonAirMaterialsArray());
    }

    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        return EquipmentClass.isArmor(ItemUtils.getItemMeta(i2));
    }

    @Override
    public String ingredientDescription(ItemStack base) {
        return TranslationManager.getTranslation("ingredient_any_armor");
    }
}
