package me.athlaeos.valhallammo.resourcepack;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;

public class ResourcePackListener implements Listener {
    private static final NamespacedKey RESOURCEPACK_VERSION = new NamespacedKey(ValhallaMMO.getInstance(), "resourcepack_version");

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        String currentVersion = e.getPlayer().getPersistentDataContainer().getOrDefault(RESOURCEPACK_VERSION, PersistentDataType.STRING, "");
        String packVersion = ResourcePack.getVersion();
        if (!currentVersion.equals(packVersion)) e.getPlayer().getPersistentDataContainer().set(RESOURCEPACK_VERSION, PersistentDataType.STRING, packVersion);
        ResourcePack.sendUpdate(e.getPlayer());
    }

    public static void resetPackVersion(Player p){
        p.getPersistentDataContainer().remove(RESOURCEPACK_VERSION);
    }
}
