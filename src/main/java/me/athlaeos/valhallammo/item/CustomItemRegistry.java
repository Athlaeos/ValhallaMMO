package me.athlaeos.valhallammo.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomItemRegistry {
    private static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ItemStackGSONAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();
    private static final Map<String, ItemStack> items = new HashMap<>();

    public static Map<String, ItemStack> getItems() {
        return items;
    }

    public static ItemStack getItem(String id){
        return items.get(id);
    }

    public static void register(String id, ItemStack item){
        item = item.clone();
        item.setAmount(1);
        items.put(id, item);
    }

    @SuppressWarnings("all")
    public static void loadFile(){
        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/items.json");
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (BufferedReader setsReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))){
            CustomItem[] items = gson.fromJson(setsReader, CustomItem[].class);
            if (items == null) return;
            for (CustomItem item : items) register(item.id, item.item);
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not load items from items.json, " + exception.getMessage());
        } catch (NoClassDefFoundError ignored){}
    }

    @SuppressWarnings("all")
    public static void saveItems(){
        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/items.json");
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8))){
            JsonElement element = gson.toJsonTree(new ArrayList<>(items.entrySet().stream().map(e -> new CustomItem(e.getKey(), e.getValue())).toList()), new TypeToken<ArrayList<CustomItem>>(){}.getType());
            gson.toJson(element, writer);
            writer.flush();
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not save items to items.json, " + exception.getMessage());
        }
    }

    private record CustomItem(String id, ItemStack item) {}
}
