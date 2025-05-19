package me.athlaeos.valhallammo.gui;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class Menu {
    protected Inventory inventory;
    protected PlayerMenuUtility playerMenuUtility;

    public Menu(PlayerMenuUtility playerMenuUtility){
        this.playerMenuUtility = playerMenuUtility;
    }

    public abstract String getMenuName();

    public abstract int getSlots();

    public abstract void handleMenu(InventoryClickEvent e);

    public abstract void handleMenu(InventoryDragEvent e);

    public abstract void setMenuItems();

    public PlayerMenuUtility getPlayerMenuUtility() {
        return playerMenuUtility;
    }

    public void open(){
        inventory = Bukkit.createInventory(null, getSlots(), getMenuName());

        this.setMenuItems();
        MenuListener.setActiveMenu(playerMenuUtility.getOwner(), this);

        playerMenuUtility.getOwner().openInventory(inventory);
    }

    public Inventory getInventory(){
        return inventory;
    }

    protected static ItemStack getButtonData(String path, Material def){
        return getButtonData(path, "gui_details.yml", def);
    }

    @SuppressWarnings("all")
    protected static ItemStack getButtonData(String path, String config, Material def){
        String value = ConfigManager.getConfig(config).get().getString(path, "");
        if (StringUtils.isEmpty(value)) ValhallaMMO.logWarning(config + ":" + path + " was called, but has no value!");
        String[] parts = value.split(":");
        Material m = ItemUtils.stringToMaterial(parts[0], def);
        int modelData = -1;
        if (parts.length > 1) {
            try {
                modelData = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored){
                ValhallaMMO.logWarning("Invalid integer value for custom model data in gui_details.yml: " + parts[1]);
            }
        }
        return new ItemBuilder(m).data(modelData).get();
    }
}

//Credit for menu and manager go to Kody Simpson
