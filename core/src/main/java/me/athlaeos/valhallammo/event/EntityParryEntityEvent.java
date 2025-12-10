package me.athlaeos.valhallammo.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntityParryEntityEvent extends EntityEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final ParryType type;
    private double damageReduction;
    private double cooldownReduction;
    private boolean applySelfDebuffs = true;
    private boolean applyEnemyDebuffs = true;
    private final Entity parried;

    public EntityParryEntityEvent(Entity parrier, Entity parried, ParryType type, double damageReduction, double cooldownReduction) {
        super(parrier);
        this.type = type;
        this.parried = parried;
        this.damageReduction = damageReduction;
        this.cooldownReduction = cooldownReduction;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Entity getParried() {
        return parried;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public double getCooldownReduction() {
        return cooldownReduction;
    }

    public ParryType getType() {
        return type;
    }

    public double getDamageReduction() {
        return damageReduction;
    }

    public void setApplyEnemyDebuffs(boolean applyEnemyDebuffs) {
        this.applyEnemyDebuffs = applyEnemyDebuffs;
    }

    public void setApplySelfDebuffs(boolean applySelfDebuffs) {
        this.applySelfDebuffs = applySelfDebuffs;
    }

    public void setDamageReduction(double damageReduction) {
        this.damageReduction = damageReduction;
    }

    public void setCooldownReduction(double cooldownReduction) {
        this.cooldownReduction = cooldownReduction;
    }

    public boolean isApplyEnemyDebuffs() {
        return applyEnemyDebuffs;
    }

    public boolean isApplySelfDebuffs() {
        return applySelfDebuffs;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public static enum ParryType{
        SUCCESSFUL,
        FAILED
    }
}
