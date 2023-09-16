package me.athlaeos.valhallammo.dom;

import com.google.gson.*;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;

import java.lang.reflect.Type;

public class GsonAdapter <T> implements JsonSerializer<T>, JsonDeserializer<T> {
    private final String propertyName;
    public GsonAdapter(String propertyName){
        this.propertyName = propertyName;
    }

    public T deserialize(
            JsonElement jsonElement,
            Type type,
            JsonDeserializationContext jsonDeserializationContext
    ) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonPrimitive prim = (JsonPrimitive) jsonObject.get(propertyName);
        String className = prim.getAsString();
        Class<?> klass = getObjectClass(className);
        return jsonDeserializationContext.deserialize(jsonObject.get("DATA"), klass);
    }

    public JsonElement serialize(T jsonElement, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(propertyName, jsonElement.getClass().getName());
        jsonObject.add("DATA", jsonSerializationContext.serialize(jsonElement));
        return jsonObject;
    }

    public Class<?> getObjectClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e.getMessage());
        }
    }
}
