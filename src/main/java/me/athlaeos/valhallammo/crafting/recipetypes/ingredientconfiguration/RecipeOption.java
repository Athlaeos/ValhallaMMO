package me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration;

import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;

public abstract class RecipeOption {
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
        ItemStack cursor = new ItemBuilder(getIcon()).stringTag(key, getName()).get().clone();
        e.getWhoClicked().setItemOnCursor(cursor);
    }

    public NamespacedKey getDefaultKey() {
        return key;
    }
}
