package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.jetbrains.annotations.NotNull;

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
        @EventHandler(ignoreCancelled = true)
        public void onStatisticJump(EntityExhaustionEvent e){
            if (!e.getExhaustionReason().toString().contains("JUMP") || !(e.getEntity() instanceof Player p)) return;
            ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(new PlayerJumpEvent(p));
        }
    }
    public static void register(ValhallaMMO plugin) {
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(listener, plugin);
    }
}