package me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.MaterialClass;
import me.athlaeos.valhallammo.utility.ItemUtils;

public class SimilarTypeChoice extends me.athlaeos.valhallammo.crafting.recipetypes.grid.ingredientoptions.GridIngredientChoice {

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
        return new ItemBuilder(Material.BOOK).name("&7Similar items (Tool Material)")
        .lore(
            "&eDrag-and-drop onto an ingredient", 
            "&eto allow this ingredient to be substituted", 
            "&ewith other similar items types (meta ignored).",
            "&7(i.e. white wool with red wool, cobblestone ", 
            "&7with blackstone)",
            "&6Tools can be substituted with other tools",
            "&6of the same material, same with armor.",
            "&7(i.e. wooden pickaxe with wooden shovel",
            "&7gold chestplate with gold boots")
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
    public RecipeChoice getChoice(ItemStack i, boolean isShapeless) {
        MaterialClass materialClass = MaterialClass.getMatchingClass(i);
        if (materialClass != null) return new RecipeChoice.MaterialChoice(materialClass.getMatchingMaterials().toArray(new Material[0]));
        return new RecipeChoice.MaterialChoice(ItemUtils.getSimilarMaterials(i.getType()).toArray(new Material[0]));
    }

    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        MaterialClass materialClass = MaterialClass.getMatchingClass(i1);
        if (materialClass != MaterialClass.OTHER) return materialClass == MaterialClass.getMatchingClass(i2);
        return ItemUtils.isSimilarMaterial(i1.getType(), i2.getType());
    }
}
