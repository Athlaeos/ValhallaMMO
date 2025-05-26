package me.athlaeos.valhallammo.trading.services;

import me.athlaeos.valhallammo.trading.services.service_implementations.PlainService;
import me.athlaeos.valhallammo.trading.services.type_implementations.OrderingService;
import me.athlaeos.valhallammo.trading.services.type_implementations.TradingService;
import me.athlaeos.valhallammo.trading.services.type_implementations.TrainingService;
import me.athlaeos.valhallammo.trading.services.type_implementations.UpgradingService;

import java.util.HashMap;
import java.util.Map;

public class ServiceRegistry {
    private static final Map<String, ServiceType> serviceTypes = new HashMap<>();
    private static final Map<String, Service> services = new HashMap<>();

    static {
        registerType(new TradingService());
        registerType(new OrderingService());
        registerType(new TrainingService());
        registerType(new UpgradingService());

        registerService(new PlainService("trading", getServiceType("TRADING")));
        registerService(new PlainService("ordering", getServiceType("ORDERING")));
    }

    public static void registerType(ServiceType service){
        serviceTypes.put(service.getID(), service);
    }

    public static ServiceType getServiceType(String id){
        return serviceTypes.get(id);
    }

    public static Map<String, ServiceType> getServiceTypes() {
        return new HashMap<>(serviceTypes);
    }

    public static Service getService(String id) {
        return services.get(id);
    }

    public static void registerService(Service service) {
        services.put(service.getId(), service);
    }

    public static Map<String, Service> getServices() {
        return new HashMap<>(services);
    }

    public static void removeService(String serviceName){
        if (serviceName.equals("trading") || serviceName.equals("ordering")) return;
        services.remove(serviceName);
    }
}
