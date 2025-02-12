package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.item.CustomID;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;

public class MaterialWithIDChoice extends RecipeOption {

    @Override
    public String getName() {
        return "CHOICE_MATERIAL_ID";
    }

    @Override
    public String getActiveDescription() {
        return "This ingredient will need to match in item base type AND custom ID";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.NAME_TAG).name("&7Material-ID Requirement")
        .lore(
            "&aRequire this ingredient to match",
            "&abase type AND custom ID.",
            "",
            "&7The name of the ingredient will be",
            "&7used to communicate item requirement",
            "&7to the player.").get();
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
        if (i1.getType() != i2.getType()) return false;
        ItemMeta i1Meta = i1.getItemMeta();
        ItemMeta i2Meta = i2.getItemMeta();
        Integer id1 = CustomID.getID(i1Meta);
        Integer id2 = CustomID.getID(i2Meta);
        if ((i1Meta == null && i2Meta == null) || (id1 == null && id2 == null)) return true;
        if ((i1Meta == null || i2Meta == null) || (id1 == null || id2 == null)) return false;
        return i1.getType() == i2.getType() && id1.intValue() == id2.intValue();
    }

    @Override
    public String ingredientDescription(ItemStack base) {
        return ItemUtils.getItemName(ItemUtils.getItemMeta(base));
    }

    @Override
    public RecipeOption getNew() {
        return new MaterialWithIDChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }
}
