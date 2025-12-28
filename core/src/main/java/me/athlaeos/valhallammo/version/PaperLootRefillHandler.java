package me.athlaeos.valhallammo.version;

import com.destroystokyo.paper.loottable.LootableInventory;
import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PaperLootRefillHandler {

    public static boolean canGenerateLoot(BlockState i, Player opener){
        if (!ValhallaMMO.isUsingPaperMC()) return true;
        if (i instanceof LootableInventory l){
            return !l.hasPendingRefill() && !l.canPlayerLoot(opener.getUniqueId());
        }
        return true;
    }

    public static boolean canGenerateLoot(Entity i, Player opener){
        if (!ValhallaMMO.isUsingPaperMC()) return true;
        if (i instanceof LootableInventory l){
            return !l.hasPendingRefill() && !l.canPlayerLoot(opener.getUniqueId());
        }
        return true;
    }
}
