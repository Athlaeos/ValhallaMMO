package me.athlaeos.valhallammo.gui;

import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuListener implements Listener {
    private static final Map<UUID, Menu> activeMenus = new HashMap<>();

    public static void setActiveMenu(Player p, Menu menu){
        activeMenus.put(p.getUniqueId(), menu);
    }

    /**
     * There exists a bug in spigot that prevents InventoryClickEvents while the player
     * is sleeping, so to patch this we prevent inventories opening while the player
     * is sleeping.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMenuOpen(InventoryOpenEvent e){
        if (!e.getPlayer().isSleeping()) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerSleep(PlayerBedEnterEvent e){
        e.getPlayer().closeInventory();
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e){
        Menu activeMenu = activeMenus.get(e.getWhoClicked().getUniqueId());
        if (activeMenu != null && e.getInventory().equals(activeMenu.getInventory())){
            if (!Timer.isCooldownPassed(e.getWhoClicked().getUniqueId(), "cooldown_delay_inventory_click")) {
                e.setCancelled(true);
                return;
            }
            Timer.setCooldown(e.getWhoClicked().getUniqueId(), 30, "cooldown_delay_inventory_click");
            activeMenu.handleMenu(e);
        }
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent e){
        Menu activeMenu = activeMenus.get(e.getWhoClicked().getUniqueId());
        if (activeMenu != null && e.getInventory().equals(activeMenu.getInventory())){
            if (!ItemUtils.isEmpty(e.getCursor())){
                if (!Timer.isCooldownPassed(e.getWhoClicked().getUniqueId(), "cooldown_delay_inventory_drag")) {
                    e.setCancelled(true);
                    return;
                }
                Timer.setCooldown(e.getWhoClicked().getUniqueId(), 30, "cooldown_delay_inventory_drag");
                activeMenu.handleMenu(e);
            }
        }
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent e){
        Menu activeMenu = activeMenus.get(e.getPlayer().getUniqueId());
        if (activeMenu != null && e.getInventory().equals(activeMenu.getInventory())){
            activeMenu.onClose();
        }
    }
}