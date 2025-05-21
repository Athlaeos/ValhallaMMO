package me.athlaeos.valhallammo.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.persistence.GsonAdapter;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomItemRegistry {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(DynamicItemModifier.class, new GsonAdapter<DynamicItemModifier>("MOD_TYPE"))
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ItemStackGSONAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();
    private static final Map<String, CustomItem> items = new HashMap<>();

    public static Map<String, CustomItem> getItems() {
        return items;
    }

    public static CustomItem getItem(String id){
        return items.get(id);
    }

    public static ItemStack getProcessedItem(String id){
        return getProcessedItem(id, null);
    }

    public static ItemStack getProcessedItem(String id, Player p){
        CustomItem item = items.get(id);
        if (item == null) return null;
        ItemBuilder builder = new ItemBuilder(item.getItem().clone());
        DynamicItemModifier.modify(ModifierContext.builder(builder).crafter(p).executeUsageMechanics().validate().get(), item.getModifiers());
        return builder.get();
    }

    public static CustomItem register(String id, ItemStack item){
        item = item.clone();
        item.setAmount(1);
        CustomItem customItem = new CustomItem(id, item);
        items.put(id, customItem);
        return customItem;
    }

    public static CustomItem register(String id, CustomItem item){
        items.put(id, item);
        return item;
    }

    @SuppressWarnings("all")
    public static void loadFromFile(File f){
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (BufferedReader setsReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))){
            CustomItem[] items = gson.fromJson(setsReader, CustomItem[].class);
            if (items == null) return;
            for (CustomItem item : items) {
                if (item.getItem() == null) {
                    ValhallaMMO.logWarning("Could not load custom item " + item.getId() + ", item was improperly loaded!");
                    continue;
                }
                DynamicItemModifier.sortModifiers(item.getModifiers());
                register(item.getId(), item);
            }
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
            JsonElement element = gson.toJsonTree(new ArrayList<>(items.values()), new TypeToken<ArrayList<CustomItem>>(){}.getType());
            gson.toJson(element, writer);
            writer.flush();
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not save items to items.json, " + exception.getMessage());
        }
    }
}
