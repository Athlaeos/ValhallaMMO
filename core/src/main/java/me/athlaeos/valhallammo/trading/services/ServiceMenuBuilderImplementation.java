package me.athlaeos.valhallammo.trading.services;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.trading.menu.ServiceMenu;

public class ServiceMenuBuilderImplementation implements ServiceMenuBuilder {
    @Override
    public int[] getButtonOffsets(ServiceMenu serviceMenu){
        return switch (serviceMenu.getServices().size()) {
            case 1 -> new int[]{};
            case 2, 3 -> new int[]{0, 1, 2, 9, 10, 11, 18, 19, 20};
            case 4, 5, 6, 7, 8, 9 -> new int[]{0, 1, 2, 9, 10, 11};
            default -> new int[]{0};
        };
    }

    @Override
    public int[] getButtonPlacementLocations(ServiceMenu serviceMenu){
        return switch (serviceMenu.getServices().size()){
            case 2 -> new int[]{1, 5};
            case 3 -> new int[]{0, 3, 6};
            case 4 -> new int[]{1, 5, 19, 23};
            case 5 -> new int[]{1, 5, 18, 21, 24};
            case 6 -> new int[]{0, 3, 6, 18, 21, 24};
            case 7 -> new int[]{1, 5, 18, 21, 24, 37, 41};
            case 8 -> new int[]{0, 3, 6, 19, 23, 36, 39, 42};
            case 9 -> new int[]{0, 3, 6, 18, 21, 24, 36, 39, 42};
            default -> new int[]{};
        };
    }

    @Override
    public int getRowCount(ServiceMenu serviceMenu){
        return switch (serviceMenu.getServices().size()){
            case 2, 3 -> 3;
            case 4, 5, 6 -> 4;
            case 7, 8, 9 -> 6;
            default -> 1;
        };
    }

    @Override
    public String getTitle(ServiceMenu serviceMenu){
        if (!ValhallaMMO.isResourcePackConfigForced()) return "&8Services"; // TODO data driven
        return switch(serviceMenu.getServices().size()){
            case 1, 2, 3 -> "&f\uF808\uF31E";
            case 4, 5, 6 -> "&f\uF808\uF31F";
            case 7, 8, 9 -> "&f\uF808\uF321";
            default -> "";
        };
    }
}
