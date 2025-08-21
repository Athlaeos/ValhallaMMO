package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class MaterialWithDataChoice extends RecipeOption {
    private Collection<Material> validChoices = new HashSet<>();

    @Override
    public String getName() {
        if (validChoices == null) validChoices = new HashSet<>();
        return "CHOICE_MATERIAL_DATA";
    }

    @Override
    public String getActiveDescription() {
        if (validChoices == null) validChoices = new HashSet<>();
        return "This ingredient will need to match in item base type" + (validChoices.isEmpty() ? "" : ("(or any of " + String.join(", ", validChoices.stream().map(m -> StringUtils.toPascalCase(m.toString().replace("_", " "))).collect(Collectors.toSet())) + ")")) + " AND custom model data";
    }

    @Override
    public ItemStack getIcon() {
        if (validChoices == null) validChoices = new HashSet<>();
        ItemBuilder builder = new ItemBuilder(Material.RED_DYE).name("&7Material-data Requirement");
        if (!validChoices.isEmpty()) builder.appendLore("&f" + StringUtils.separateStringIntoLines(String.join(", ", validChoices.stream().map(m -> StringUtils.toPascalCase(m.toString().replace("_", " "))).collect(Collectors.toSet())), 40));
        builder.appendLore(
            "&aRequire this ingredient to match in",
            "&abase type or any of the configured types",
            "&aand in custom model data.",
            "",
            "&7The name of the ingredient will be",
            "&7used to communicate item requirement",
            "&7to the player.");
        return builder.get();
    }

    @Override
    public boolean isCompatible(ItemStack i) {
        if (validChoices == null) validChoices = new HashSet<>();
        return true;
    }

    @Override
    public boolean isUnique() {
        if (validChoices == null) validChoices = new HashSet<>();
        return false;
    }

    @Override
    public RecipeChoice getChoice(ItemStack i) {
        if (validChoices == null) validChoices = new HashSet<>();
        List<Material> combined = new ArrayList<>(validChoices);
        combined.add(i.getType());
        return new RecipeChoice.MaterialChoice(combined.toArray(new Material[0]));
    }

    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        if (validChoices == null) validChoices = new HashSet<>();
        if (i1.getType() != i2.getType() && !validChoices.contains(i2.getType())) return false;
        ItemMeta i1Meta = i1.getItemMeta();
        ItemMeta i2Meta = i2.getItemMeta();
        if (i1Meta == null && i2Meta == null) return true;
        if (i1Meta == null || i2Meta == null) return false;
        if (!i1Meta.hasCustomModelData() && !i2Meta.hasCustomModelData()) return true;
        if (!i1Meta.hasCustomModelData() || !i2Meta.hasCustomModelData()) return false;
        return i1.getType() == i2.getType() && i1Meta.getCustomModelData() == i2Meta.getCustomModelData();
    }

    @Override
    public String ingredientDescription(ItemStack base) {
        if (validChoices == null) validChoices = new HashSet<>();
        return ItemUtils.getItemName(new ItemBuilder(base));
    }

    @Override
    public RecipeOption getNew() {
        if (validChoices == null) validChoices = new HashSet<>();
        MaterialWithDataChoice choice = new MaterialWithDataChoice();
        choice.validChoices = this.validChoices;
        return choice;
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        if (validChoices == null) validChoices = new HashSet<>();
        return true;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if (validChoices == null) validChoices = new HashSet<>();
        if (e.isRightClick()){
            validChoices.clear();
            PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()).addOption(this);
        } else {
            if (!ItemUtils.isEmpty(e.getCursor())) {
                validChoices.add(e.getCursor().getType());
                PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()).addOption(this);
            }
            else super.onClick(e);
        }
    }
}
