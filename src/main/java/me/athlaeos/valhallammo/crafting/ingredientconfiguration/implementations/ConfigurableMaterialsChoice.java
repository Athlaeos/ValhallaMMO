package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class ConfigurableMaterialsChoice extends RecipeOption implements IngredientChoice {
    private final Collection<Material> validChoices = new HashSet<>();

    @Override
    public String getName() {
        return "CONFIGURABLE_MATERIAL_RANGE";
    }

    @Override
    public String getActiveDescription() {
        return "The ingredient can be any of " + String.join(", ", validChoices.stream().map(m -> StringUtils.toPascalCase(m.toString().replace("_", " "))).collect(Collectors.toSet()));
    }

    @Override
    public ItemStack getIcon() {
        ItemBuilder builder = new ItemBuilder(Material.WRITABLE_BOOK).name("&7Material Selection");
        if (validChoices.isEmpty()){
            builder.appendLore("&4Not Configured");
        } else builder.appendLore("&f" + StringUtils.separateStringIntoLines(String.join(", ", validChoices.stream().map(m -> StringUtils.toPascalCase(m.toString().replace("_", " "))).collect(Collectors.toSet())), 40));
        builder.appendLore(
                "&eLeft-click with item to add to list",
                "",
                "&cRight-click to clear the list",
                "",
                "&eClick without item to apply to ingredient.",
                "&aThe ingredient may then be substituted",
                "&awith any of the given materials.");
        return builder.get();
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
        return new ConfigurableMaterialsChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }

    @Override
    public RecipeChoice getChoice(ItemStack i) {
        return new RecipeChoice.MaterialChoice(validChoices.toArray(new Material[0]));
    }

    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        return validChoices.contains(i2.getType());
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if (e.isRightClick()){
            validChoices.clear();
            PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()).addOption(this);
        } else {
            if (validChoices.isEmpty() && ItemUtils.isEmpty(e.getCursor())) {
                Utils.sendMessage(e.getWhoClicked(), "&cThis option must be configured first!");
            } else {
                if (!ItemUtils.isEmpty(e.getCursor())) {
                    validChoices.add(e.getCursor().getType());
                    PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()).addOption(this);
                }
                else super.onClick(e);
            }
        }
    }

    @Override
    public String ingredientDescription(ItemStack base) {
        return String.join("/", validChoices.stream().map(m -> StringUtils.toPascalCase(m.toString().replace("_", " "))).collect(Collectors.toSet()));
    }
}
