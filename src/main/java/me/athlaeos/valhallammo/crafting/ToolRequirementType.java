package me.athlaeos.valhallammo.crafting;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.utility.ItemUtils;

public enum ToolRequirementType {
    // if an item has no tool ID, -1 is given. So anything <0 is regarded as "not a tool"
    NOT_REQUIRED((i1, i2) -> true), // no custom tool is required, and will work regardless of held item
    EQUAL_OR_LESSER((i1, i2) -> i2 < 0 || i1 <= i2), // custom tool is required, and must be of same or lesser ID as required tool
    EQUAL((i1, i2) -> i2 < 0 || i1 == i2), // custom tool is required, and must be of the same ID as required tool
    EQUAL_OR_GREATER((i1, i2) -> i2 < 0 || i1 >= i2), // custom tool is required, and must be of same or greater ID as required tool
    NONE_MANDATORY((i1, i2) -> i1 < 0); // no custom tool is allowed to be used
    private final Comparator comparator;
    private static final NamespacedKey key_tool_id = new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_tool_id");

    ToolRequirementType(Comparator comparator) {
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

    private interface Comparator{
        boolean compare(int i1, int i2);
    }

    public static int getToolID(ItemStack i){
        if (ItemUtils.isEmpty(i)) return -1;
        if (i.getItemMeta() == null) return -1;
        ItemMeta meta = i.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(key_tool_id, PersistentDataType.INTEGER, -1);
    }
    
    public static void setItemsToolID(ItemStack i, int id){
        if (ItemUtils.isEmpty(i)) return;
        if (i.getItemMeta() == null) return;
        ItemMeta meta = i.getItemMeta();
        meta.getPersistentDataContainer().set(key_tool_id, PersistentDataType.INTEGER, id);
        i.setItemMeta(meta);
    }
}