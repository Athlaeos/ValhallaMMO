package me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;

public class SimilarMaterialsChoice extends RecipeOption implements me.athlaeos.valhallammo.crafting.recipetypes.grid.ingredientoptions.GridIngredientChoice {

    @Override
    public String getName() {
        return "CHOICE_SIMILAR_MATERIALS";
    }

    @Override
    public String getActiveDescription() {
        return "The ingredient can be replaced with a number of similar materials";
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
            "&6Tools and armor can be substituted with",
            "&6their type of other materials",
            "&7(i.e. wooden pickaxe with diamond pickaxe",
            "&7gold chestplate with iron chestplate")
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
        EquipmentClass equipmentClass = EquipmentClass.getMatchingClass(i);
        if (equipmentClass != null) return new RecipeChoice.MaterialChoice(equipmentClass.getMatches().toArray(new Material[0]));
        return new RecipeChoice.MaterialChoice(ItemUtils.getSimilarMaterials(i.getType()).toArray(new Material[0]));
    }

    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        EquipmentClass materialClass = EquipmentClass.getMatchingClass(i1);
        if (materialClass != null) return materialClass == EquipmentClass.getMatchingClass(i2);
        return ItemUtils.isSimilarMaterial(i1.getType(), i2.getType());
    }
    
}
