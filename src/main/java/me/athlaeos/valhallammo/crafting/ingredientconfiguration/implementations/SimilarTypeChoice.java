package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.MaterialClass;
import me.athlaeos.valhallammo.utility.ItemUtils;

public class SimilarTypeChoice extends RecipeOption implements IngredientChoice {

    @Override
    public String getName() {
        return "CHOICE_SIMILAR_TYPES";
    }

    @Override
    public String getActiveDescription() {
        return "The ingredient can be replaced with a number of similar types";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.IRON_INGOT).name("&7Similar items (Tool Material)")
        .lore(
            "&aIngredient may be substituted with",
            "&aother similar items types (meta ignored).",
            "&7(i.e. white wool with red wool, cobblestone ", 
            "&7with blackstone)",
            "",
            "&aTools can be substituted with other tools",
            "&aof the same material, same with armor.",
            "&7(i.e. Wooden Pickaxe with Wooden Shovel",
            "&7Gold Chestplate with Gold Boots"
        ).get();
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
        return new SimilarTypeChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }

    @SuppressWarnings("all")
    @Override
    public RecipeChoice getChoice(ItemStack i) {
        MaterialClass materialClass = MaterialClass.getMatchingClass(ItemUtils.getItemMeta(i));
        if (materialClass != null) return new RecipeChoice.MaterialChoice(materialClass.getMatchingMaterials().toArray(new Material[0]));
        return new RecipeChoice.MaterialChoice(ItemUtils.getSimilarMaterials(i.getType()).toArray(new Material[0]));
    }

    @SuppressWarnings("all")
    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        MaterialClass materialClass = MaterialClass.getMatchingClass(ItemUtils.getItemMeta(i1));
        if (materialClass != null) return materialClass == MaterialClass.getMatchingClass(ItemUtils.getItemMeta(i2));
        return ItemUtils.isSimilarMaterial(i1.getType(), i2.getType());
    }

    @SuppressWarnings("all")
    @Override
    public String ingredientDescription(ItemStack base) {
        MaterialClass materialClass = MaterialClass.getMatchingClass(ItemUtils.getItemMeta(base));
        if (materialClass != null) return ItemUtils.getGenericTranslation(materialClass);
        return ItemUtils.getGenericTranslation(base.getType());
    }
}
