package me.athlaeos.valhallammo.crafting;

import me.athlaeos.valhallammo.dom.Comparator;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.athlaeos.valhallammo.ValhallaMMO;

public enum ToolRequirementType {
    // if an item has no tool ID, -1 is given. So anything <0 is regarded as "not a tool"
    NOT_REQUIRED((i1, i2) -> true), // no custom tool is required, and will work regardless of held item
    EQUAL_OR_LESSER((i1, i2) -> i2 < 0 || i1 <= i2), // custom tool is required, and must be of same or lesser ID as required tool
    EQUAL((i1, i2) -> i2 < 0 || i1.equals(i2)), // custom tool is required, and must be of the same ID as required tool
    EQUAL_OR_GREATER((i1, i2) -> i2 < 0 || i1 >= i2), // custom tool is required, and must be of same or greater ID as required tool
    NONE_MANDATORY((i1, i2) -> i1 < 0); // no custom tool is allowed to be used
    private final Comparator<Integer, Integer> comparator;
    private static final NamespacedKey key_tool_id = ValhallaMMO.key("valhalla_tool_id");

    ToolRequirementType(Comparator<Integer, Integer> comparator) {
        this.comparator = comparator;
    }

    /**
     * Checks if the given tool ID is valid against the required tool id.
     * @param heldToolID the ID of the tool held
     * @param requiredToolID the ID of the tool required
     * @return true if the condition is met
     */
    public boolean check(int heldToolID, int requiredToolID){
        return comparator.compare(heldToolID, requiredToolID);
    }

    public static int getToolID(ItemMeta meta){
        if (meta == null) return -1;
        return meta.getPersistentDataContainer().getOrDefault(key_tool_id, PersistentDataType.INTEGER, -1);
    }
    
    public static void setItemsToolID(ItemMeta meta, int id){
        meta.getPersistentDataContainer().set(key_tool_id, PersistentDataType.INTEGER, id);
    }
}