package me.athlaeos.valhallammo.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.persistence.GsonAdapter;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ArmorSetRegistry {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(DynamicItemModifier.class, new GsonAdapter<DynamicItemModifier>("MOD_TYPE"))
            .registerTypeAdapter(AttributeWrapper.class, new GsonAdapter<AttributeWrapper>("ATTRIBUTE_TYPE"))
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();
    private static final NamespacedKey ARMOR_SET = ValhallaMMO.key("armor_set");
    private static final Map<String, ArmorSet> registeredSets = new HashMap<>();

    public static void register(ArmorSet set){
        registeredSets.put(set.getId(), set);
    }

    public static Map<String, ArmorSet> getRegisteredSets() {
        return registeredSets;
    }

    @SuppressWarnings("all")
    public static void loadFromFile(File f){
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (BufferedReader setsReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))){
            ArmorSet[] sets = gson.fromJson(setsReader, ArmorSet[].class);
            if (sets == null) return;
            for (ArmorSet set : sets) register(set);
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not load armor sets from armor_sets.json, " + exception.getMessage());
        } catch (NoClassDefFoundError ignored){}
    }

    @SuppressWarnings("all")
    public static void saveArmorSets(){
        File f = new File(ValhallaMMO.getInstance().getDataFolder(), "/armor_sets.json");
        try {
            f.createNewFile();
        } catch (IOException ignored){}
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8))){
            JsonElement element = gson.toJsonTree(new ArrayList<>(registeredSets.values()), new TypeToken<ArrayList<ArmorSet>>(){}.getType());
            gson.toJson(element, writer);
            writer.flush();
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not save armor sets to armor_sets.json, " + exception.getMessage());
        }
    }

    public static void setArmorSet(ItemBuilder item, ArmorSet set){
        for (ArmorSet s : registeredSets.values()) {
            if (item.getLore() != null){
                item.getLore().remove(Utils.chat(s.getName()));
                item.getLore().removeAll(Utils.chat(s.getLore()));
            }
        }
        if (set == null) {
            item.getMeta().getPersistentDataContainer().remove(ARMOR_SET);
        } else {
            item.stringTag(ARMOR_SET, set.getId());
            if (set.getName() != null) item.appendLore(set.getName());
            if (set.getLore() != null) item.appendLore(set.getLore());
        }
    }

    public static ArmorSet getArmorSet(ItemMeta meta){
        return registeredSets.get(meta.getPersistentDataContainer().getOrDefault(ARMOR_SET, PersistentDataType.STRING, ""));
    }

    private static void updateItem(ItemBuilder reference, ItemStack i, boolean remove){
        if (reference == null || ItemUtils.isEmpty(i)) return;
        ArmorSet itemSet = getArmorSet(reference.getMeta());
        if (itemSet == null) return; // if the worn item has no set, nothing needs to happen
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return;
        List<String> lore = ItemUtils.getLore(meta);
        List<String> itemSetLore = Utils.chat(itemSet.getLore());
        if (remove) lore.removeAll(itemSetLore);
        else if (!itemSetLore.isEmpty() && !new HashSet<>(lore).containsAll(itemSetLore)){
            if (itemSet.getName() != null && lore.contains(Utils.chat(itemSet.getName()))){
                int index = lore.indexOf(Utils.chat(itemSet.getName()));
                if (index == lore.size() - 1) lore.addAll(itemSetLore);
                else lore.addAll(index, itemSetLore);
            } else lore.addAll(itemSetLore);
        } else return;
        meta.setLore(lore);
        i.setItemMeta(meta);
    }

    public static Collection<ArmorSet> getActiveArmorSets(LivingEntity e){
        EntityProperties properties = EntityCache.getAndCacheProperties(e);
        Map<ArmorSet, Integer> sets = new HashMap<>();
        for (ItemBuilder item : properties.getIterable(false, null)){
            ArmorSet set = getArmorSet(item.getMeta());
            if (set == null) continue;
            sets.put(set, sets.getOrDefault(set, 0) + 1);
        }
        return sets.isEmpty() ? Set.of() : sets.entrySet().stream().filter(s -> s.getValue() >= s.getKey().getPiecesRequired()).map(Map.Entry::getKey).collect(Collectors.toSet());
    }
}
