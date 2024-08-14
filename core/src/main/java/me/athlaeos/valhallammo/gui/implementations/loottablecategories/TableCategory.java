package me.athlaeos.valhallammo.gui.implementations.loottablecategories;

import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static me.athlaeos.valhallammo.gui.implementations.LootTableOverviewMenu.KEY_TABLE_CATEGORY;

public abstract class TableCategory {
    private final String id;
    private final ItemStack icon;
    private final String title;
    private final int position;

    public TableCategory(String id, ItemStack icon, int position, String title){
        this.id = id;
        this.icon = new ItemBuilder(icon).stringTag(KEY_TABLE_CATEGORY, id).get();
        this.position = position;
        this.title = title;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public int getPosition() {
        return position;
    }

    public String getId() {
        return id;
    }

    /**
     * The buttons the menu should show. If empty, onButtonClick is called right away
     * @return the list of items the menu should show
     */
    public abstract List<ItemStack> getCategoryOptions();

    public abstract void onButtonClick(InventoryClickEvent e, String storedValue, Menu openedFrom);
}