package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class PotionChoice extends RecipeOption {

    @Override
    public String getName() {
        return "CHOICE_POTIONS";
    }

    @Override
    public String getActiveDescription() {
        return "The ingredient can be replaced with any potion (splash, lingering, and normal)";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.POTION).name("&7Potions")
        .lore(
            "&aIngredient may be substituted with",
            "&aany type of potion (splash, lingering, ",
            "&anormal)"
        ).get();
    }

    @Override
    public boolean isCompatible(ItemStack i) {
        return i.getType() == Material.POTION ||
                i.getType() == Material.SPLASH_POTION ||
                i.getType() == Material.LINGERING_POTION;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public RecipeOption getNew() {
        return new PotionChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }

    @Override
    public RecipeChoice getChoice(ItemStack i) {
        return new RecipeChoice.MaterialChoice(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION);
    }

    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        return i2.getType() == Material.POTION ||
                i2.getType() == Material.SPLASH_POTION ||
                i2.getType() == Material.LINGERING_POTION;
    }

    @Override
    public String ingredientDescription(ItemStack base) {
        return ItemUtils.getGenericTranslation(base.getType());
    }
}
