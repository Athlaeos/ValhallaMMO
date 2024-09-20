package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ToolArmorChoice extends RecipeOption implements IngredientChoice {
    private static Collection<Material> toolsAndArmor;

    static {
        List<Material> toolsAndArmor = new ArrayList<>();
        for (EquipmentClass c : EquipmentClass.values()) toolsAndArmor.addAll(c.getMatchingMaterials());
        ToolArmorChoice.toolsAndArmor = toolsAndArmor;
    }

    @Override
    public String getName() {
        return "CHOICE_TOOLS_ARMOR";
    }

    @Override
    public String getActiveDescription() {
        return "The ingredient can be replaced with any type of equipment of any material";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.LEATHER_CHESTPLATE).name("&7Any Equipment")
        .lore(
            "&aIngredient may be substituted with",
            "&aany type of equipment of any material."
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
        return new ToolArmorChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }

    @SuppressWarnings("all")
    @Override
    public RecipeChoice getChoice(ItemStack i) {
        return new RecipeChoice.MaterialChoice(toolsAndArmor.toArray(new Material[0]));
    }

    @SuppressWarnings("all")
    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        return EquipmentClass.getMatchingClass(ItemUtils.getItemMeta(i2)) != null;
    }

    @SuppressWarnings("all")
    @Override
    public String ingredientDescription(ItemStack base) {
        return TranslationManager.getTranslation("ingredient_any_tool_or_armor");
    }
}
