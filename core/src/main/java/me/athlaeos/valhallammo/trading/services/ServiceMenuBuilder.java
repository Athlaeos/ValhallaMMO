package me.athlaeos.valhallammo.trading.services;

import me.athlaeos.valhallammo.trading.menu.ServiceMenu;

public interface ServiceMenuBuilder {
    int[] getButtonOffsets(ServiceMenu serviceMenu);

    int[] getButtonPlacementLocations(ServiceMenu serviceMenu);

    int getRowCount(ServiceMenu serviceMenu);

    String getTitle(ServiceMenu serviceMenu);
}
