package me.athlaeos.valhallammo.gui;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMenuUtilManager {
    private static final Map<UUID, PlayerMenuUtility> playerMenuMap = new HashMap<>();

    /**
     * Returns a PlayerMenuUtility object belonging to the given player, or a new blank one if none exist.
     * This PlayerMenuUtility contains some required details in controlling the GUI menu's during usage.
     *
     * @return A PlayerMenuUtility object belonging to a player, or a new blank one if none were found.
     */
    public static PlayerMenuUtility getPlayerMenuUtility(Player p){
        playerMenuMap.putIfAbsent(p.getUniqueId(), new PlayerMenuUtility(p));
        return playerMenuMap.get(p.getUniqueId());
    }
}
