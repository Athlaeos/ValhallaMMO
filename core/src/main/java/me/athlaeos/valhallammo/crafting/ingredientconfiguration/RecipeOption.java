package me.athlaeos.valhallammo.crafting.ingredientconfiguration;

import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.athlaeos.valhallammo.ValhallaMMO;

public abstract class RecipeOption implements IngredientChoice {
    /**
     * @return The name and identifier for this option
     */
    public abstract String getName();

    /**
     * The description added to the ingredient lore in the crafting grid once it's applied
     */
    public abstract String getActiveDescription();

    /**
     * @return The icon used in the editor menu to display this option
     */
    public abstract ItemStack getIcon();

    /**
     * @return True if this recipe option is compatible with the given item, and prevents application if it's not
     */
    public abstract boolean isCompatible(ItemStack i);

    /**
     * @return True if only one of this option is allowed per recipe, overwrites previous applications of this option if true
     */
    public abstract boolean isUnique();

    public abstract RecipeOption getNew();

    private final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "recipe_option_id");
    public void onClick(InventoryClickEvent e){
        e.setCancelled(false);
        PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()).addOption(this);
        // do nothing by default
    }

    /**
     * Informs whether the option is functional with input items as well<br>
     * For example: The "replace by item" option should only work on ingredients and not the input (tinkered) item. So if isInput is
     * true, that option's compatibleWithInputItem should return false.
     * @param isInput whether the item in question removes itself
     * @return true if the option can perform on the output. False otherwise
     */
    public abstract boolean isCompatibleWithInputItem(boolean isInput);

    public NamespacedKey getDefaultKey() {
        return key;
    }
}
