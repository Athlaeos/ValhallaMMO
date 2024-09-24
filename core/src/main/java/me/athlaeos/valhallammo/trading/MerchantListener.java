package me.athlaeos.valhallammo.trading;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class MerchantListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent e){
        if (e.getInventory().getType() != InventoryType.MERCHANT) return;
        System.out.println("closed");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onOpen(InventoryOpenEvent e){
        if (e.getInventory().getType() != InventoryType.MERCHANT) return;
        System.out.println("open");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVillagerInteract(PlayerInteractAtEntityEvent e){
        if (!(e.getRightClicked() instanceof Villager v)) return;
        int reputation = ValhallaMMO.getNms().getReputation(e.getPlayer(), v);
        System.out.println("reputation: " + reputation);
        if (reputation >= 0) ValhallaMMO.getNms().modifyReputation(e.getPlayer(), v, GossipTypeWrapper.MAJOR_NEGATIVE);
        else ValhallaMMO.getNms().modifyReputation(e.getPlayer(), v, GossipTypeWrapper.MAJOR_POSITIVE);

        List<MerchantRecipe> recipes = new ArrayList<>();
        MerchantRecipe r1 = new MerchantRecipe(new ItemStack(Material.TOTEM_OF_UNDYING), 0, 3, true, 1, 1.1F, 3, 6);
        r1.addIngredient(new ItemStack(Material.DIAMOND, 5));
        r1.addIngredient(new ItemStack(Material.REDSTONE, 10));
        recipes.add(r1);
        new MerchantTradeInterface(PlayerMenuUtilManager.getPlayerMenuUtility(e.getPlayer()), v, recipes).open();
        e.setCancelled(true);
    }

}
