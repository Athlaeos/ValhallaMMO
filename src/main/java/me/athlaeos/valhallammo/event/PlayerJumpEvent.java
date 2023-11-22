package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerJumpEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private static final PlayerJumpEventListener listener = new PlayerJumpEventListener();

    private final Player player;

    public PlayerJumpEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static class PlayerJumpEventListener implements Listener {

        private final Map<UUID, Integer> jumps = new HashMap<>();

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerJoin(PlayerJoinEvent e) {
            jumps.put(e.getPlayer().getUniqueId(), e.getPlayer().getStatistic(Statistic.JUMP));
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent e) {
            jumps.remove(e.getPlayer().getUniqueId());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerMove(PlayerMoveEvent e) {
            if (e.getTo() == null) return;
            Player player = e.getPlayer();

            if(e.getFrom().getY() >= e.getTo().getY()) return;
            int current = player.getStatistic(Statistic.JUMP);
            int last = jumps.getOrDefault(player.getUniqueId(), -1);

            if(last == current) return;
            jumps.put(player.getUniqueId(), current);

            double yDif = (long) ((e.getTo().getY() - e.getFrom().getY()) * 1000) / 1000d;

            if((yDif < 0.035 || yDif > 0.037) && (yDif < 0.116 || yDif > 0.118)) {
                ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(new PlayerJumpEvent(player));
            }
        }

    }
    public static void register(ValhallaMMO plugin) {
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(listener, plugin);
    }
}