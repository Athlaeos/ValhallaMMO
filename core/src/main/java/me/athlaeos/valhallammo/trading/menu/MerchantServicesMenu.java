package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.trading.dom.MerchantType;
import me.athlaeos.valhallammo.trading.services.Service;
import me.athlaeos.valhallammo.trading.services.ServiceRegistry;
import me.athlaeos.valhallammo.trading.services.ServiceType;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class MerchantServicesMenu extends Menu {
    private static final NamespacedKey KEY_BUTTON = new NamespacedKey(ValhallaMMO.getInstance(), "button_functionality");
    private static final NamespacedKey KEY_SERVICE = new NamespacedKey(ValhallaMMO.getInstance(), "button_service");
    private static final int INDEX_BACK_TO_MENU = 49;

    private View view = View.ACTIVE_SERVICES;
    private final Menu previousMenu;
    private final MerchantType type;

    public MerchantServicesMenu(PlayerMenuUtility playerMenuUtility, Menu previousMenu, MerchantType type) {
        super(playerMenuUtility);
        this.previousMenu = previousMenu;
        this.type = type;
    }

    @Override
    public String getMenuName() {
        return Utils.chat("&8Services"); // TODO data driven
    }

    @Override
    public int getSlots() {
        return 54;
    }

    private String confirmDeletion = null;

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(!(e.getClickedInventory() instanceof PlayerInventory));

        ItemBuilder clickedItem = ItemUtils.isEmpty(e.getCurrentItem()) ? null : new ItemBuilder(e.getCurrentItem());
        String buttonFunction = clickedItem == null ? null : clickedItem.getMeta().getPersistentDataContainer().get(KEY_BUTTON, PersistentDataType.STRING);
        String clickedServiceData = clickedItem == null ? null : clickedItem.getMeta().getPersistentDataContainer().get(KEY_SERVICE, PersistentDataType.STRING);

        if (buttonFunction == null && clickedServiceData == null) {
            setMenuItems();
            return;
        }

        switch (view){
            case PRE_EXISTING_SERVICES -> {
                if (clickedServiceData != null){
                    if (e.isShiftClick()) {
                        if (e.isLeftClick() || (confirmDeletion != null && !confirmDeletion.equals(clickedServiceData))) confirmDeletion = clickedServiceData;
                        else ServiceRegistry.removeService(clickedServiceData);
                        setMenuItems();
                        return;
                    } else {
                        type.getServices().add(clickedServiceData);
                        view = View.ACTIVE_SERVICES;
                    }
                } else {
                    if (buttonFunction.equals("createNewButton")) view = View.SERVICE_TYPES;
                    else if (buttonFunction.equals("backToMenuButton")) view = View.ACTIVE_SERVICES;
                }
            }
            case ACTIVE_SERVICES -> {
                if (clickedServiceData != null){
                    if (e.isShiftClick()) {
                        type.getServices().remove(clickedServiceData);
                    } else {
                        Service service = ServiceRegistry.getService(clickedServiceData);
                        if (service != null) {
                            ServiceType serviceType = ServiceRegistry.getServiceType(service.getType());
                            serviceType.onTypeConfigurationSelect(e, service, this);
                        }
                    }
                } else {
                    if (buttonFunction.equals("createNewButton")) view = View.PRE_EXISTING_SERVICES;
                    else if (buttonFunction.equals("backToMenuButton") && previousMenu != null) previousMenu.open();
                }
            }
            case SERVICE_TYPES -> {
                if (clickedServiceData != null){
                    ServiceType serviceType = ServiceRegistry.getServiceType(clickedServiceData);
                    if (serviceType != null) serviceType.onTypeConfigurationSelect(e, null, this);
                } else {
                    if (buttonFunction.equals("backToMenuButton")) view = View.PRE_EXISTING_SERVICES;
                }
            }
        }
        confirmDeletion = null;
        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void setMenuItems() {
        inventory.clear();

        switch (view){
            case SERVICE_TYPES -> {
                List<ItemStack> buttons = new ArrayList<>();
                for (ServiceType serviceType : ServiceRegistry.getServiceTypes().values()){
                    if (ItemUtils.isEmpty(serviceType.getDefaultButton())) continue;
                    buttons.add(new ItemBuilder(serviceType.getDefaultButton())
                            .stringTag(KEY_SERVICE, serviceType.getID())
                            .appendLore(
                                    "&6Click to add/create"
                            )
                            .get()
                    );
                }

                for (int i = 0; i < 45 && i < buttons.size(); i++){
                    ItemStack button = buttons.get(i);
                    if (ItemUtils.isEmpty(button)) continue;
                    inventory.addItem(button);
                }
                inventory.setItem(INDEX_BACK_TO_MENU, backToMenuButton);
            }
            case PRE_EXISTING_SERVICES -> {
                List<ItemStack> buttons = new ArrayList<>();
                for (Service service : ServiceRegistry.getServices().values()){
                    if (type.getServices().contains(service.getID())) continue;
                    ServiceType serviceType = ServiceRegistry.getServiceType(service.getType());
                    if (serviceType == null || ItemUtils.isEmpty(serviceType.getDefaultButton())) continue;
                    buttons.add(new ItemBuilder(serviceType.getDefaultButton())
                            .stringTag(KEY_SERVICE, serviceType.getID())
                            .appendLore("&6Click to add")
                            .appendLore(
                                    (service.getID().equals(confirmDeletion)) ?
                                            "&cShift-Right-Click to confirm deletion" :
                                            "&cShift-Left-Click to delete")
                            .get()
                    );
                }
                buttons.add(new ItemBuilder(createNewButton).name("&b&lCreate New Service").get());

                for (int i = 0; i < 45 && i < buttons.size(); i++){
                    ItemStack button = buttons.get(i);
                    if (ItemUtils.isEmpty(button)) continue;
                    inventory.addItem(button);
                }
                inventory.setItem(INDEX_BACK_TO_MENU, backToMenuButton);
            }
            case ACTIVE_SERVICES -> {
                List<ItemStack> buttons = new ArrayList<>();
                for (String s : type.getServices()) {
                    Service service = ServiceRegistry.getService(s);
                    if (service == null) continue;
                    ServiceType serviceType = ServiceRegistry.getServiceType(service.getType());
                    if (serviceType == null || ItemUtils.isEmpty(serviceType.getDefaultButton())) continue;
                    buttons.add(new ItemBuilder(serviceType.getDefaultButton())
                            .stringTag(KEY_SERVICE, s)
                            .appendLore(
                                    "&6Click to edit (if available)",
                                    "&6Shift-click to remove",
                                    "&cRemoval here does not equate to",
                                    "&cdeletion. Check 'pre-existing'",
                                    "&cservices for permanent deletion"
                            )
                            .get()
                    );
                }
                buttons.add(new ItemBuilder(createNewButton).name("&b&lAdd Service").get());

                for (int i = 0; i < 45 && i < buttons.size(); i++){
                    ItemStack button = buttons.get(i);
                    if (ItemUtils.isEmpty(button)) continue;
                    inventory.addItem(button);
                }
                inventory.setItem(INDEX_BACK_TO_MENU, backToMenuButton);
            }
        }
    }

    public MerchantType getType() {
        return type;
    }

    private enum View{
        ACTIVE_SERVICES,
        SERVICE_TYPES,
        PRE_EXISTING_SERVICES
    }

    private static final ItemStack backToMenuButton =
            new ItemBuilder(getButtonData("editor_backtomenu", Material.LIME_DYE))
                    .name("&fBack")
                    .stringTag(KEY_BUTTON, "backToMenuButton")
                    .get();
    private static final ItemStack createNewButton = new ItemBuilder(getButtonData("editor_newrecipe", Material.LIME_DYE))
            .name("&b&lAdd Service")
            .stringTag(KEY_BUTTON, "createNewButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
}
