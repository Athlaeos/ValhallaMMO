package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ToolWeaponChoice extends RecipeOption {
    private static Collection<Material> tools;

    static {
        List<Material> tools = new ArrayList<>();
        tools.addAll(EquipmentClass.PICKAXE.getMatchingMaterials());
        tools.addAll(EquipmentClass.AXE.getMatchingMaterials());
        tools.addAll(EquipmentClass.SHOVEL.getMatchingMaterials());
        tools.addAll(EquipmentClass.HOE.getMatchingMaterials());
        tools.addAll(EquipmentClass.SHEARS.getMatchingMaterials());
        tools.addAll(EquipmentClass.SWORD.getMatchingMaterials());
        tools.addAll(EquipmentClass.TRIDENT.getMatchingMaterials());
        tools.addAll(EquipmentClass.BOW.getMatchingMaterials());
        tools.addAll(EquipmentClass.CROSSBOW.getMatchingMaterials());
        ToolWeaponChoice.tools = tools;
    }

    @Override
    public String getName() {
        return "CHOICE_TOOLS_WEAPONS";
    }

    @Override
    public String getActiveDescription() {
        return "The ingredient can be replaced with any tool or weapon of any material";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.IRON_AXE).name("&7Any Tool or Weapon")
        .lore(
            "&aIngredient may be substituted with",
            "&aany tool or weapon of any material."
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
        return new ToolWeaponChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }

    @SuppressWarnings("all")
    @Override
    public RecipeChoice getChoice(ItemStack i) {
        return new RecipeChoice.MaterialChoice(tools.toArray(new Material[0]));
    }

    @SuppressWarnings("all")
    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        return tools.contains(i2.getType());
    }

    @SuppressWarnings("all")
    @Override
    public String ingredientDescription(ItemStack base) {
        return TranslationManager.getTranslation("ingredient_any_tool");
    }
}
