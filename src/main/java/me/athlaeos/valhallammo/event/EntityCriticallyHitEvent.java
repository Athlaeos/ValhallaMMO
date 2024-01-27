package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.dom.CombatType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntityCriticallyHitEvent extends EntityEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private double critMultiplier;
    private double damageBeforeCrit;
    private final CombatType type;
    private final Entity critter;

    public EntityCriticallyHitEvent(LivingEntity target, Entity critter, CombatType type, double damageBeforeCrit, double criticalHitDamageMultiplier) {
        super(target);
        this.type = type;
        this.critter = critter;
        this.damageBeforeCrit = damageBeforeCrit;
        this.critMultiplier = criticalHitDamageMultiplier;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Entity getCritter() {
        return critter;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public double getCritMultiplier() {
        return critMultiplier;
    }

    public CombatType getType() {
        return type;
    }

    public double getDamageBeforeCrit() {
        return damageBeforeCrit;
    }

    public void setCritMultiplier(double critMultiplier) {
        this.critMultiplier = critMultiplier;
    }

    public void setDamageBeforeCrit(double damageBeforeCrit) {
        this.damageBeforeCrit = damageBeforeCrit;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
