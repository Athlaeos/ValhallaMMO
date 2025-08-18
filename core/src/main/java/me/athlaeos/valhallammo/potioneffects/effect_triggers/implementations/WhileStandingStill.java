package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WhileStandingStill implements EffectTrigger.ConstantTrigger, Listener {
    private static Listener singleListenerInstance = null;
    private static final Map<UUID, Long> lastTimeMoved = new HashMap<>();
    private static final int timeUntilStandingStill = ValhallaMMO.getPluginConfig().getInt("duration_for_standing_stil");
    private final boolean shouldBeStandingStill;

    public WhileStandingStill(boolean shouldBeStandingStill){
        this.shouldBeStandingStill = shouldBeStandingStill;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity) {
        if (!(entity instanceof Player p)) return false;
        return (System.currentTimeMillis() - lastTimeMoved.getOrDefault(p.getUniqueId(), 0L)) >= timeUntilStandingStill == shouldBeStandingStill;
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return "while_" + (shouldBeStandingStill ? "standing_still" : "moving");
    }

    @Override
    public void onRegister() {
        if (singleListenerInstance != null) return;
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
        singleListenerInstance = this;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMovement(PlayerMoveEvent e){
        if (e.getTo() == null) return;
        Location l1 = e.getFrom();
        Location l2 = e.getTo();
        double xDif = Math.abs(l1.getX() - l2.getX());
        double zDif = Math.abs(l1.getZ() - l2.getZ());
        if (xDif > 0.001 || zDif > 0.001) lastTimeMoved.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }
}
