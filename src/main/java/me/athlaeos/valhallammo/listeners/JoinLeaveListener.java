package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            if (!e.getPlayer().isOnline()) return;
            ProfileManager.getPersistence().loadProfile(e.getPlayer());

            // TODO world blacklisting
            // TODO tutorial book giving
            // TODO global effect boss bar revealing
            // TODO recipe discoverage
        }, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        // TODO remove unique attributes

        ProfileManager.getPersistence().saveProfile(e.getPlayer());
    }
}
