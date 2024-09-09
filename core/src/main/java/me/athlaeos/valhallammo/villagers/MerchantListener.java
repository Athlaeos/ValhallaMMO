package me.athlaeos.valhallammo.villagers;

import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MerchantListener implements Listener {
    private static final Map<UUID, VirtualMerchant> activeMenus = new HashMap<>();

    public static void setActiveMenu(Player p, VirtualMerchant menu){
        activeMenus.put(p.getUniqueId(), menu);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e){
        VirtualMerchant activeMenu = activeMenus.get(e.getWhoClicked().getUniqueId());
        if (activeMenu != null && e.getInventory().equals(activeMenu.getMerchant())){
            activeMenu.handleMenu(e);
        }
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent e){
        VirtualMerchant activeMenu = activeMenus.get(e.getWhoClicked().getUniqueId());
        if (activeMenu != null && e.getInventory().equals(activeMenu.getMerchant())){
            if (!ItemUtils.isEmpty(e.getCursor())){
                activeMenu.handleMenu(e);
            }
        }
    }
}