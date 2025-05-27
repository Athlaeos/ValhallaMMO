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

    public static final ServiceType SERVICE_TRADING = new TradingService();
    public static final ServiceType SERVICE_ORDERING = new OrderingService();
    public static final ServiceType SERVICE_TRAINING = new TrainingService();
    public static final ServiceType SERVICE_UPGRADING = new UpgradingService();

    static {
        registerType(SERVICE_TRADING);
        registerType(SERVICE_ORDERING);
        registerType(SERVICE_TRAINING);
        registerType(SERVICE_UPGRADING);

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
        services.put(service.getID(), service);
    }

    public static Map<String, Service> getServices() {
        return new HashMap<>(services);
    }

    public static void removeService(String serviceName){
        if (serviceName.equals("trading") || serviceName.equals("ordering")) return;
        services.remove(serviceName);
    }
}
