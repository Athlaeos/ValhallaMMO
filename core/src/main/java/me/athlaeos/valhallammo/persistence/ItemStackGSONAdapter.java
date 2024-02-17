package me.athlaeos.valhallammo.persistence;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Type;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provided by user Schottky on spigotmc.org
 */
public class ItemStackGSONAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        String element = ItemUtils.serialize(src);
        if (element == null) throw new IllegalStateException("ItemStack could not be serialized");
        return new JsonPrimitive(element);
    }

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        return convert(Catch.catchOrElse(() -> ItemUtils.deserialize(jsonElement.getAsString()), null));
    }

    /**
     * Changes an item's type if its display name contains a formatted string telling the plugin it should be turned
     * into another item if it exists
     * @param i the item to convert
     * @return the converted item
     */
    private ItemStack convert(ItemStack i){
        if (i == null) return null;
        if (!i.hasItemMeta()) return i;
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return null;
        if (!meta.hasDisplayName()) return i;
        String displayName = meta.getDisplayName();
        if (!displayName.contains("REPLACEWITH:")) return i;
        String[] args = displayName.split("REPLACEWITH:");
        if (args.length != 2) return i;
        Material m = Catch.catchOrElse(() -> Material.valueOf(args[1]), null);
        if (m == null) return i;
        return new ItemStack(m);
    }

//    final Type objectStringMapType = new TypeToken<Map<String, Object>>() {}.getType();
//
//    @Override
//    public ConfigurationSerializable deserialize(
//            JsonElement json,
//            Type typeOfT,
//            JsonDeserializationContext context) throws JsonParseException
//    {
//        final Map<String, Object> map = new LinkedHashMap<>();
//
//        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
//            final JsonElement value = entry.getValue();
//            final String name = entry.getKey();
//
//            if (value.isJsonObject() && value.getAsJsonObject().has(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
//                map.put(name, this.deserialize(value, value.getClass(), context));
//            } else {
//                map.put(name, context.deserialize(value, Object.class));
//            }
//        }
//
//        return ConfigurationSerialization.deserializeObject(map);
//    }
//
//    @Override
//    public JsonElement serialize(
//            ConfigurationSerializable src,
//            Type typeOfSrc,
//            JsonSerializationContext context)
//    {
//        final Map<String, Object> map = new LinkedHashMap<>();
//        map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(src.getClass()));
//        map.putAll(src.serialize());
//        return context.serialize(map, objectStringMapType);
//    }
}
