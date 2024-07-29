package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
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

public class MeleeWeaponChoice extends RecipeOption implements IngredientChoice {
    private static Collection<Material> weapons;

    static {
        List<Material> weapons = new ArrayList<>();
        weapons.addAll(EquipmentClass.SWORD.getMatchingMaterials());
        weapons.addAll(EquipmentClass.PICKAXE.getMatchingMaterials());
        weapons.addAll(EquipmentClass.AXE.getMatchingMaterials());
        weapons.addAll(EquipmentClass.SHOVEL.getMatchingMaterials());
        weapons.addAll(EquipmentClass.HOE.getMatchingMaterials());
        weapons.addAll(EquipmentClass.TRIDENT.getMatchingMaterials());
        MeleeWeaponChoice.weapons = weapons;
    }

    @Override
    public String getName() {
        return "CHOICE_MELEE_WEAPON";
    }

    @Override
    public String getActiveDescription() {
        return "The ingredient can be replaced with any melee weapon of any material";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.IRON_SWORD).name("&7Any Weapon")
        .lore(
            "&aIngredient may be substituted with",
            "&aany weapon of any material. Includes tridents."
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
        return new MeleeWeaponChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }

    @SuppressWarnings("all")
    @Override
    public RecipeChoice getChoice(ItemStack i) {
        return new RecipeChoice.MaterialChoice(weapons.toArray(new Material[0]));
    }

    @SuppressWarnings("all")
    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        return weapons.contains(i2.getType());
    }

    @SuppressWarnings("all")
    @Override
    public String ingredientDescription(ItemStack base) {
        return TranslationManager.getTranslation("ingredient_any_melee_weapon");
    }
}
