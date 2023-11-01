package me.athlaeos.valhallammo.gui;

import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.PlayerInventory;

public class MenuListener implements Listener {

    @EventHandler
    public void onMenuClick(InventoryClickEvent e){
        if (e.getView().getTopInventory().getHolder() instanceof Menu m &&
                e.getView().getBottomInventory() instanceof PlayerInventory){
            m.handleMenu(e);
        }
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent e){
        if (e.getView().getTopInventory().getHolder() instanceof Menu m &&
                e.getView().getBottomInventory() instanceof PlayerInventory){
            if (!ItemUtils.isEmpty(e.getCursor())){
                m.handleMenu(e);
            }
        }
    }
}