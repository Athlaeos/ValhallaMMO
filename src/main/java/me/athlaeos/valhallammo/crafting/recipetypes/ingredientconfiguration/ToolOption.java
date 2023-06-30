package me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration;

import org.bukkit.Material;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import me.athlaeos.valhallammo.crafting.ToolRequirement;
import me.athlaeos.valhallammo.crafting.ToolRequirementType;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.item.ItemBuilder;

public class ToolOption extends RecipeOption implements IngredientChoice, SlotIngredientBehavior {
    private ToolRequirement toolRequirement = null;

    @Override
    public void onCraft(CraftItemEvent e, int gridIndex) {
        // TODO decrease durability and break if exceeded
    }

    @Override
    public RecipeChoice getChoice(ItemStack i, boolean isShapeless) {
        return new RecipeChoice.MaterialChoice(Material.values());
    }

    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        return toolRequirement != null && toolRequirement.canCraft(ToolRequirementType.getToolID(i2));
    }

    @Override
    public String getName() {
        return "REQUIRED_TOOL";
    }

    @Override
    public String getActiveDescription() {
        return "This ingredient must be any tool with valid tool requirements";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.DIAMOND_PICKAXE).name("&7Tool")
        .lore(
            "&eDrag-and-drop onto an ingredient", 
            "&eto mark this ingredient as a crafting tool.", 
            "&7Crafting tools inherit the tool requirements", 
            "&7of this recipe and are needed to craft this", 
            "&7recipe.", 
            "&7Takes 1 durability per use if damageable").get();
    }

    @Override
    public boolean isCompatible(ItemStack i) {
        return true;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof Menu m){
            // TODO check if instance of recipe editing menu, and if so set the ToolRequirement of the recipe on this ToolOption
            ItemStack cursor = new ItemBuilder(getIcon()).stringTag(super.getDefaultKey(), getName()).get().clone();
            e.getWhoClicked().setItemOnCursor(cursor);
        }
    }

    @Override
    public RecipeOption getNew() {
        return new ToolOption();
    }
}
