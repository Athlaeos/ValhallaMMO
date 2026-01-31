package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SweepStatus {
    private static final NamespacedKey PREVENT_SWEEPING = ValhallaMMO.key("cannot_sweep");

    public static void setSweepable(ItemMeta meta, boolean canSweep){
        if (canSweep) meta.getPersistentDataContainer().remove(PREVENT_SWEEPING);
        else meta.getPersistentDataContainer().set(PREVENT_SWEEPING, PersistentDataType.BYTE, (byte) 1);
    }

    public static boolean preventSweeping(ItemMeta meta){
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(PREVENT_SWEEPING, PersistentDataType.BYTE);
    }
}
