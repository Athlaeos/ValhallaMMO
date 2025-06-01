package me.athlaeos.valhallammo.trading.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.persistence.GsonAdapter;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import me.athlaeos.valhallammo.trading.services.service_implementations.PlainService;
import me.athlaeos.valhallammo.trading.services.type_implementations.OrderingService;
import me.athlaeos.valhallammo.trading.services.type_implementations.TradingService;
import me.athlaeos.valhallammo.trading.services.type_implementations.TrainingService;
import me.athlaeos.valhallammo.trading.services.type_implementations.UpgradingService;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServiceRegistry {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(DynamicItemModifier.class, new GsonAdapter<DynamicItemModifier>("MOD_TYPE"))
            .registerTypeAdapter(Service.class, new GsonAdapter<Service>("SERVICE_IMPL"))
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ItemStackGSONAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();
    private static final Map<String, ServiceType> serviceTypes = new HashMap<>();
    private static final Map<String, Service> services = new HashMap<>();

    public static final ServiceType SERVICE_TYPE_TRADING = new TradingService();
    public static final ServiceType SERVICE_TYPE_ORDERING = new OrderingService();
    public static final ServiceType SERVICE_TYPE_TRAINING = new TrainingService();
    public static final ServiceType SERVICE_TYPE_UPGRADING = new UpgradingService();

    public static final Service SERVICE_TRADING = new PlainService("trading", SERVICE_TYPE_TRADING);

    static {
        registerType(SERVICE_TYPE_TRADING);
        registerType(SERVICE_TYPE_ORDERING);
//        registerType(SERVICE_TYPE_TRAINING);
//        registerType(SERVICE_TYPE_UPGRADING);

        registerService(SERVICE_TRADING);
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

    @SuppressWarnings("all")
    public static void loadFromFile(File f){
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (BufferedReader setsReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))){
            Service[] collectedServices = gson.fromJson(setsReader, Service[].class);
            if (collectedServices == null) return;
            for (Service service : collectedServices) registerService(service);
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not load services from trading/services.json, " + exception.getMessage());
        } catch (NoClassDefFoundError ignored){}
    }

    @SuppressWarnings("all")
    public static void saveServices(){
        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/trading/services.json");
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8))){
            JsonElement element = gson.toJsonTree(new ArrayList<>(services.values()), new TypeToken<ArrayList<Service>>(){}.getType());
            gson.toJson(element, writer);
            writer.flush();
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not save items to trading/services.json, " + exception.getMessage());
        }
    }
}
