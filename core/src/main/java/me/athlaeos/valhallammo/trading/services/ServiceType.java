package me.athlaeos.valhallammo.trading.services;

import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.menu.MerchantServicesMenu;
import me.athlaeos.valhallammo.trading.menu.ServiceMenu;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class ServiceType {
    public abstract String getID();

    public abstract void onServiceSelect(InventoryClickEvent e, ServiceMenu menu, Service service, MerchantData data);

    public abstract ItemStack getButtonIcon(ServiceMenu serviceMenu, Service service, MerchantData data);

    public abstract void onTypeConfigurationSelect(InventoryClickEvent e, Service service, MerchantServicesMenu menu);

    public abstract ItemStack getDefaultButton();

    public static class DynamicButton {
        private final Material baseMaterial;
        private final int bigData;
        private final int mediumData;
        private final int smallData;

        public DynamicButton(String fromString){
            String[] args = fromString.split("/");
            if (args.length < 4) throw new IllegalArgumentException("Invalid dynamic button string " + fromString + ", must be formatted MATERIAL/SMALLDATA/MEDIUMDATA/LARGEDATA");
            baseMaterial = Catch.catchOrElse(() -> Material.valueOf(args[0]), null);
            smallData = Catch.catchOrElse(() -> Integer.valueOf(args[1]), -1);
            mediumData = Catch.catchOrElse(() -> Integer.valueOf(args[2]), -1);
            bigData = Catch.catchOrElse(() -> Integer.valueOf(args[3]), -1);
            if (baseMaterial == null || smallData < 0 || mediumData < 0 || bigData < 0)
                throw new IllegalArgumentException("Invalid dynamic button string " + fromString + ", must be formatted MATERIAL/SMALLDATA/MEDIUMDATA/LARGEDATA");
        }

        public int getBigData() { return bigData; }
        public int getMediumData() { return mediumData; }
        public int getSmallData() { return smallData; }
        public Material getBaseMaterial() { return baseMaterial; }

        public ItemBuilder get(ButtonSize size){
            return new ItemBuilder(baseMaterial).data(size == ButtonSize.BIG ? bigData : size == ButtonSize.MEDIUM ? mediumData : smallData);
        }
    }

    public enum ButtonSize{
        BIG,
        MEDIUM,
        SMALL;

        public static ButtonSize defaultFromButtonCount(int buttonCount){
            return switch(buttonCount){
                case 1, 2, 3 -> BIG;
                case 4, 5, 6, 7, 8, 9 -> MEDIUM;
                default -> SMALL;
            };
        }
    }
}
