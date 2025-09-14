package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.TradingProfile;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.dom.MerchantType;
import me.athlaeos.valhallammo.trading.listeners.MerchantListener;
import me.athlaeos.valhallammo.trading.services.*;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ServiceMenu extends Menu implements MerchantMenu {
    private static ServiceMenuBuilder menuBuilder = new ServiceMenuBuilderImplementation();

    private final List<Service> services = new ArrayList<>();
    private final Map<Integer, Service> buttonToServiceMap = new HashMap<>();
    private final MerchantData data;
    Map<Integer, ServiceMenuBuilder.PlacementDetails> details = new HashMap<>();

    public ServiceMenu(PlayerMenuUtility playerMenuUtility, MerchantData data) {
        super(playerMenuUtility);
        this.data = data;
        MerchantListener.setActiveTradingMenu(playerMenuUtility.getOwner(), this);

        TradingProfile profile = ProfileCache.getOrCache(playerMenuUtility.getOwner(), TradingProfile.class);
        MerchantType type = CustomMerchantManager.getMerchantType(data.getType());
        if (type == null) return;
        Collection<String> putServices = new HashSet<>();
        for (String s : type.getServices()){
            if (putServices.contains(s) || !profile.getUnlockedServices().contains(s)) continue;
            Service service = ServiceRegistry.getService(s);
            if (service == null) continue;
            putServices.add(s);
            services.add(service);
        }
        details = menuBuilder.ofServices(services);
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
        // buttonPlacements and details should be the same size
        details = menuBuilder.ofServices(services);
        int[] buttonPlacements = menuBuilder.getButtonPlacementLocations(this);
        for (int i = 0; i < details.size(); i++){
            ServiceMenuBuilder.PlacementDetails d = details.get(i);
            Service service = d.spotServices().stream().findFirst().orElse(null);
            ServiceType type = d.spotType();
            if (service == null) continue;
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

    @Override
    public UUID getMerchantID() {
        return data.getVillagerUUID();
    }

    public MerchantData getData() {
        return data;
    }

    @Override
    public void onOpen() {

    }

    public static ServiceMenuBuilder getMenuBuilder() {
        return menuBuilder;
    }

    public static void setMenuBuilder(ServiceMenuBuilder menuBuilder) {
        ServiceMenu.menuBuilder = menuBuilder;
    }

    public Map<Integer, ServiceMenuBuilder.PlacementDetails> getDetails() {
        return details;
    }
}
