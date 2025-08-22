package me.athlaeos.valhallammo.trading.services;

import me.athlaeos.valhallammo.trading.menu.ServiceMenu;

import java.util.*;

public interface ServiceMenuBuilder {
    int[] getButtonOffsets(ServiceMenu serviceMenu);

    int[] getButtonPlacementLocations(ServiceMenu serviceMenu);

    int getRowCount(ServiceMenu serviceMenu);

    String getTitle(ServiceMenu serviceMenu);

    default Map<Integer, PlacementDetails> ofServices(Collection<Service> services){
        List<Service> sorted = new ArrayList<>(services);
        sorted.sort(Comparator.comparing(Service::getType));
        Map<Integer, PlacementDetails> details = new HashMap<>();
        Map<String, Integer> typeToIndexMap = new HashMap<>();
        int index = 0;
        for (Service s : sorted){
            if (s.getServiceType().singularButton()) {
                int existingIndex = typeToIndexMap.getOrDefault(s.getType(), -1);
                if (existingIndex >= 0) {
                    // add to existing placement details
                    PlacementDetails d = details.get(index);
                    if (d == null) continue; // should never happen
                    d.spotServices.add(s);
                    continue;
                } // distinct type has not been added yet, add normally
            }
            PlacementDetails d = new PlacementDetails(index, s.getServiceType(), new HashSet<>());
            d.spotServices.add(s);
            typeToIndexMap.put(s.getType(), index);
            details.put(index, d);
            index++;
        }
        return details;
    }

    record PlacementDetails(int spot, ServiceType spotType, Collection<Service> spotServices){}
}
