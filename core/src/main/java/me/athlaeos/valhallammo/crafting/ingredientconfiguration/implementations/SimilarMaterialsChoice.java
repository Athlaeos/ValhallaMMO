package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;

public class SimilarMaterialsChoice extends RecipeOption implements IngredientChoice {

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
        return new ItemBuilder(Material.IRON_PICKAXE).name("&7Similar items (Tool Material)")
        .lore(
            "&aIngredient may be substituted with",
            "&aother similar items types (meta ignored).",
            "&7(i.e. white wool with red wool, cobblestone ", 
            "&7with blackstone)",
            "",
            "&aTools and armor can be substituted with",
            "&atheir type of other materials",
            "&7(i.e. Wooden Pickaxe with Diamond Pickaxe",
            "&7Gold Chestplate with Iron Chestplate)")
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
        return new SimilarMaterialsChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }

    @Override
    public RecipeChoice getChoice(ItemStack i) {
        EquipmentClass equipmentClass = EquipmentClass.getMatchingClass(ItemUtils.getItemMeta(i));
        if (equipmentClass != null) return new RecipeChoice.MaterialChoice(equipmentClass.getMatchingMaterials().toArray(new Material[0])); // because technically any item can be configured to have an EquipmentClass
        return new RecipeChoice.MaterialChoice(ItemUtils.getSimilarMaterials(i.getType()).toArray(new Material[0]));
    }

    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        EquipmentClass equipmentClass = EquipmentClass.getMatchingClass(ItemUtils.getItemMeta(i1));
        if (equipmentClass != null) return equipmentClass == EquipmentClass.getMatchingClass(ItemUtils.getItemMeta(i2));
        return ItemUtils.isSimilarMaterial(i1.getType(), i2.getType());
    }

    @Override
    public String ingredientDescription(ItemStack base) {
        EquipmentClass equipmentClass = EquipmentClass.getMatchingClass(ItemUtils.getItemMeta(base));
        if (equipmentClass != null) return ItemUtils.getGenericTranslation(equipmentClass);
        return ItemUtils.getGenericTranslation(base.getType());
    }
}
