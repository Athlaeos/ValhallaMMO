package me.athlaeos.valhallammo.trading.services;

import me.athlaeos.valhallammo.trading.services.implementations.OrderingService;
import me.athlaeos.valhallammo.trading.services.implementations.TradingService;
import me.athlaeos.valhallammo.trading.services.implementations.TrainingService;
import me.athlaeos.valhallammo.trading.services.implementations.UpgradingService;
import org.bukkit.entity.Villager;

import java.util.HashMap;
import java.util.Map;

public class ServiceRegistry {
    private static final Map<String, Service> services = new HashMap<>();

    static {
        for (Villager.Profession p : Villager.Profession.values()) register(new TradingService(p));
        register(new TradingService(null));

        register(new OrderingService());
        register(new TrainingService());
        register(new UpgradingService());
    }

    public static void register(Service service){
        services.put(service.getID(), service);
    }

    public static Service getService(String id){
        return services.get(id);
    }
}
