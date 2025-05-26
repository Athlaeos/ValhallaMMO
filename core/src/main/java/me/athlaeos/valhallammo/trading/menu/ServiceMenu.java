package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.dom.MerchantType;
import me.athlaeos.valhallammo.trading.services.*;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceMenu extends Menu {
    private static ServiceMenuBuilder menuBuilder = new ServiceMenuBuilderImplementation();

    private final List<Service> services = new ArrayList<>();
    private final Map<Integer, Service> buttonToServiceMap = new HashMap<>();
    private final MerchantData data;

    public ServiceMenu(PlayerMenuUtility playerMenuUtility, MerchantData data) {
        super(playerMenuUtility);
        this.data = data;

        MerchantType type = CustomMerchantManager.getMerchantType(data.getType());
        if (type == null) return;
        for (String s : type.getServices()){
            Service service = ServiceRegistry.getService(s);
            if (service == null) continue;
            services.add(service);
        }
    }

    @Override
    public String getMenuName() {
        return Utils.chat(menuBuilder.getTitle(this));
    }

    @Override
    public int getSlots() {
        return menuBuilder.getRowCount(this) * 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);

        Service clickedService = buttonToServiceMap.get(e.getRawSlot());
        ServiceType serviceType = clickedService == null ? null : clickedService.getServiceType();
        if (serviceType != null) serviceType.onServiceSelect(e, this, clickedService, data);

        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void setMenuItems() {
        inventory.clear();
        buttonToServiceMap.clear();
        int[] buttonPlacements = menuBuilder.getButtonPlacementLocations(this);
        for (int i = 0; i < services.size(); i++){
            Service service = services.get(i);
            ServiceType type = service.getServiceType();
            if (type == null) continue;
            ItemStack baseIcon = type.getButtonIcon(this, service, data);
            buttonToServiceMap.put(buttonPlacements[i], service);

            ItemStack blankServiceButton = new ItemBuilder(baseIcon).type(Material.LIME_DYE).data(9199200).get();
            for (int index : menuBuilder.getButtonOffsets(this)) {
                buttonToServiceMap.put(buttonPlacements[i] + index, service);
                inventory.setItem(buttonPlacements[i] + index, blankServiceButton);
            }
            inventory.setItem(buttonPlacements[i], baseIcon);
        }
    }

    public List<Service> getServices() {
        services.removeIf(s -> s.getServiceType() == null);
        return services;
    }

    public MerchantData getData() {
        return data;
    }

    public static ServiceMenuBuilder getMenuBuilder() {
        return menuBuilder;
    }

    public static void setMenuBuilder(ServiceMenuBuilder menuBuilder) {
        ServiceMenu.menuBuilder = menuBuilder;
    }
}
