package me.athlaeos.valhallammo.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PotionBelt {
    private static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ItemStackGSONAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();
    private static final Map<UUID, List<ItemStack>> potionBelts = new HashMap<>();

    private static final NamespacedKey KEY_BELT_ID = new NamespacedKey(ValhallaMMO.getInstance(), "belt_id");
    private static final NamespacedKey KEY_BELT_ITEM = new NamespacedKey(ValhallaMMO.getInstance(), "belt_item");
    private static final NamespacedKey KEY_CAPACITY = new NamespacedKey(ValhallaMMO.getInstance(), "belt_capacity");
    private static final NamespacedKey KEY_SELECTED_POTION = new NamespacedKey(ValhallaMMO.getInstance(), "belt_selected");

    @SuppressWarnings("all")
    public static void loadFromFile(){
        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/potion_belts.json");
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (BufferedReader setsReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))){
            Map<UUID, List<ItemStack>> items = gson.fromJson(setsReader, new TypeToken<Map<UUID, List<ItemStack>>>(){}.getType());
            if (items == null) return;
            potionBelts.putAll(items);
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not load items from potion_belt_data.json, " + exception.getMessage());
        } catch (NoClassDefFoundError ignored){}
    }

    @SuppressWarnings("all")
    public static void saveToFile(){
        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/potion_belt_data.json");
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8))){
            gson.toJson(potionBelts, writer);
            writer.flush();
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not save items to potion_belt_data.json, " + exception.getMessage());
        }
    }

    public static boolean isPotionBelt(ItemMeta meta){
        return meta.getPersistentDataContainer().has(KEY_BELT_ID, PersistentDataType.STRING);
    }

    public static ItemStack addPotion(ItemBuilder belt, ItemStack potion){
        List<ItemStack> potions = getPotions(belt.getMeta());
        ItemMeta meta = ItemUtils.getItemMeta(potion);
        if (meta == null || isPotionBelt(meta)) return null;
        if (potions.size() + 1 > getCapacity(belt.getMeta())) return null;
        potions.add(potion);
        setPotions(belt.getMeta(), potions);
        setIndex(belt.getMeta(), potions.size() - 1);
        return getNewBelt(belt);
    }

    public static ItemStack getSelectedPotion(ItemMeta meta){
        List<ItemStack> potions = getPotions(meta);
        if (potions.isEmpty()) return null;
        int selected = Math.max(0, Math.min(potions.size() - 1, getIndex(meta)));
        return potions.get(selected);
    }

    public static ItemStack swapSelectedPotion(ItemBuilder belt, int bySlots){
        if (!isPotionBelt(belt.getMeta())) return null; // item is not a belt, cant swap potion
        List<ItemStack> potions = getPotions(belt.getMeta());
        if (potions.isEmpty()) {
            return belt.get(); // belt has no potions, return plain belt only
        }
        int selected = getIndex(belt.getMeta());
        int newIndex = (selected + bySlots < 0) ?
                (selected + bySlots + potions.size()) :
                (selected + bySlots >= potions.size()) ?
                        (selected + bySlots - potions.size()) :
                        (selected + bySlots);
        setIndex(belt.getMeta(), Math.max(0, Math.min(potions.size() - 1, newIndex)));
        ItemStack newBelt = getNewBelt(belt);
        belt.setItem(newBelt);
        belt.setMeta(ItemUtils.getItemMeta(newBelt));
        return belt.get();
    }

    public static ItemStack deleteSelectedPotion(ItemBuilder belt){
        if (!isPotionBelt(belt.getMeta())) return null; // item is not a belt, do not select potion
        List<ItemStack> potions = getPotions(belt.getMeta());
        if (potions.isEmpty()) return belt.translate().get(); // belt has no potions, return plain belt only
        int selected = Math.max(0, Math.min(potions.size() - 1, getIndex(belt.getMeta())));
        potions.remove(selected);
        setPotions(belt.getMeta(), potions);
        setIndex(belt.getMeta(), Math.max(0, Math.min(potions.size() - 1, getIndex(belt.getMeta()))));
        ItemStack newBelt = getNewBelt(belt);
        belt.setItem(newBelt);
        belt.setMeta(ItemUtils.getItemMeta(newBelt));
        return potions.isEmpty() ? getStoredBelt(belt.getMeta()) : belt.translate().get();
    }

    public static PotionExtractionDetails removeSelectedPotion(ItemBuilder belt){
        if (!isPotionBelt(belt.getMeta())) return null; // item is not a belt, do not select potion
        List<ItemStack> potions = getPotions(belt.getMeta());
        if (potions.isEmpty()) return new PotionExtractionDetails(null, belt.get()); // belt has no potions, return plain belt only
        int selected = Math.max(0, Math.min(potions.size() - 1, getIndex(belt.getMeta())));
        ItemStack potion = potions.remove(selected);
        setPotions(belt.getMeta(), potions);
        setIndex(belt.getMeta(), Math.max(0, Math.min(potions.size() - 1, getIndex(belt.getMeta()))));
        ItemStack oldBelt = getStoredBelt(belt);
        ItemStack newBelt = getNewBelt(belt);
        belt.setItem(newBelt);
        belt.setMeta(ItemUtils.getItemMeta(newBelt));
        return new PotionExtractionDetails(potion, potions.isEmpty() ? oldBelt : belt.get());
    }

    public static ItemStack getNewBelt(ItemBuilder belt){
        ItemStack beltItem = getStoredBelt(belt);
        if (ItemUtils.isEmpty(beltItem)) return belt.get();
        ItemBuilder beltBuilder = new ItemBuilder(beltItem);
        ItemStack storedBelt = getStoredBelt(beltBuilder);
        if (ItemUtils.isEmpty(storedBelt)) {
            setStoredBelt(beltBuilder, beltItem);
            ItemUtils.setItemMeta(beltItem, ItemUtils.getItemMeta(beltBuilder.get()));
        }

        UUID beltID = getPotionBeltID(beltBuilder.getMeta(), false);
        ItemStack selectedPotion = getSelectedPotion(belt.getMeta());
        if (ItemUtils.isEmpty(selectedPotion)) return belt.get();
        List<ItemStack> potions = getPotions(belt.getMeta());
        int selectedIndex = getIndex(belt.getMeta());
        ItemBuilder potionBuilder = new ItemBuilder(selectedPotion);
        List<String> format = TranslationManager.getListTranslation("potion_belt_lore_format");
        format = ItemUtils.setListPlaceholder(format, "%belt_lore%", beltBuilder.getLore() != null ? beltBuilder.getLore() : new ArrayList<>());
        format = ItemUtils.setListPlaceholder(format, "%item_lore%", potionBuilder.getLore() != null ? potionBuilder.getLore() : new ArrayList<>());
        format = ItemUtils.setListPlaceholder(format, "%current_count%", String.valueOf(potions.size()));
        format = ItemUtils.setListPlaceholder(format, "%max_count%", String.valueOf(getCapacity(belt.getMeta())));

        List<String> entries = new ArrayList<>();
        for (int i = 0; i < potions.size(); i++){
            ItemStack potion = potions.get(i);
            ItemMeta meta = ItemUtils.getItemMeta(potion);
            if (meta == null) continue;
            String name = (i == selectedIndex ? TranslationManager.getTranslation("potion_belt_entry_format_selected") : TranslationManager.getTranslation("potion_belt_entry_format_deselected")).replace("%potion%", getName(meta, potion));
            entries.add(Utils.chat(name));
        }
        format = ItemUtils.setListPlaceholder(format, "%entries%", entries);

        String name = getName(potionBuilder, selectedPotion);
        potionBuilder.lore(Utils.chat(format));
        potionBuilder.name(Utils.chat(TranslationManager.getTranslation("potion_belt_name_format").replace("%item_name%", name)));
        setPotionBeltID(potionBuilder.getMeta(), beltID);
        setPotions(potionBuilder, potions);
        setIndex(potionBuilder, selectedIndex);
        setStoredBelt(potionBuilder, beltItem);
        setCapacity(potionBuilder, getCapacity(belt.getMeta()));
        ItemUtils.setItemMeta(selectedPotion, potionBuilder);
        return selectedPotion;
    }

    private static String getName(ItemBuilder potionBuilder, ItemStack potion){
        if (potionBuilder.getName() != null) return potionBuilder.getName();
        String name = PotionEffectRegistry.getItemName(potionBuilder, false);
        if (name == null) name = ItemUtils.getItemName(potionBuilder);
        return name;
    }

    public static ItemStack getStoredBelt(ItemBuilder meta){
        String data = meta.getMeta().getPersistentDataContainer().get(KEY_BELT_ITEM, PersistentDataType.STRING);
        if (data == null) return null;
        ItemStack stored = ItemUtils.deserialize(data);
        if (stored == null) return null;
        ItemBuilder storedItem = new ItemBuilder(stored);
        TranslationManager.translateItem(storedItem);
        return storedItem.get();
    }

    public static void setStoredBelt(ItemBuilder item, ItemStack belt){
        if (ItemUtils.isEmpty(belt)) {
            item.getMeta().getPersistentDataContainer().remove(KEY_BELT_ITEM);
        } else {
            String data = ItemUtils.serialize(belt);
            if (data == null) return;
            item.stringTag(KEY_BELT_ITEM, data);
        }
    }

    public static int getIndex(ItemMeta meta){
        return meta.getPersistentDataContainer().getOrDefault(KEY_SELECTED_POTION, PersistentDataType.INTEGER, -1);
    }

    public static void setIndex(ItemMeta meta, int index){
        meta.getPersistentDataContainer().set(KEY_SELECTED_POTION, PersistentDataType.INTEGER, index);
    }

    public static int getCapacity(ItemMeta meta){
        return meta.getPersistentDataContainer().getOrDefault(KEY_CAPACITY, PersistentDataType.INTEGER, 1);
    }

    public static void setCapacity(ItemMeta meta, int capacity){
        meta.getPersistentDataContainer().set(KEY_CAPACITY, PersistentDataType.INTEGER, capacity);
    }

    public static UUID getPotionBeltID(ItemMeta meta, boolean addIfAbsent){
        if (meta.getPersistentDataContainer().get(KEY_BELT_ID, PersistentDataType.STRING) == null) {
            if (addIfAbsent) {
                meta.getPersistentDataContainer().set(KEY_BELT_ID, PersistentDataType.STRING, UUID.randomUUID().toString());
            }
            else return null;
        }
        return UUID.fromString(meta.getPersistentDataContainer().getOrDefault(KEY_BELT_ID, PersistentDataType.STRING, ""));
    }

    public static void setPotionBeltID(ItemMeta meta, UUID uuid){
        if (uuid == null) meta.getPersistentDataContainer().remove(KEY_BELT_ID);
        else meta.getPersistentDataContainer().set(KEY_BELT_ID, PersistentDataType.STRING, uuid.toString());
    }

    public static void setPotions(ItemMeta meta, List<ItemStack> items){
        UUID uuid = getPotionBeltID(meta, true);
        potionBelts.put(uuid, new ArrayList<>(items.stream().map(ItemStack::clone).toList()));
    }

    public static List<ItemStack> getPotions(ItemMeta meta) {
        if (!isPotionBelt(meta)) return new ArrayList<>();
        UUID id = getPotionBeltID(meta, false);
        if (id != null) return new ArrayList<>(potionBelts.getOrDefault(id, new ArrayList<>()).stream().map(ItemStack::clone).toList());
        else return new ArrayList<>();
    }

    public record PotionExtractionDetails(ItemStack removed, ItemStack newBelt){}
}
