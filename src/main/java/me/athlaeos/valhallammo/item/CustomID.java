package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CustomID {
    private static final NamespacedKey ITEM_ID = new NamespacedKey(ValhallaMMO.getInstance(), "id");

    public static void setID(ItemMeta meta, Integer id){
        if (id == null) meta.getPersistentDataContainer().remove(ITEM_ID);
        else meta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.INTEGER, id);
    }

    public static Integer getID(ItemMeta meta){
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(ITEM_ID, PersistentDataType.INTEGER);
    }
}
