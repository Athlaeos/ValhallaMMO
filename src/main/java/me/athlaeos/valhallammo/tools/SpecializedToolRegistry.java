package me.athlaeos.valhallammo.tools;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SpecializedToolRegistry {
    private static final Map<String, ItemStack> tools = new HashMap<>();

    static {
        register("hardness_stick", BlockHardnessStick.getStick());
    }

    public static void register(String key, ItemStack tool){
        tools.put(key, tool);
    }

    public static Map<String, ItemStack> getTools() {
        return tools;
    }
}
