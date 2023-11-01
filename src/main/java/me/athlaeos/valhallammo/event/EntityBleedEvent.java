package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.dom.CombatType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntityBleedEvent extends EntityEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled = false;
    private double bleedDamage;
    private double bleedResistance;
    private int duration;
    private int stack;
    private final CombatType type;
    private final Entity bleeder;

    public EntityBleedEvent(LivingEntity target, Entity bleeder, CombatType type, double bleedDamage, double bleedResistance, int duration, int stack) {
        super(target);
        this.type = type;
        this.bleeder = bleeder;
        this.bleedDamage = bleedDamage;
        this.bleedResistance = bleedResistance;
        this.duration = duration;
        this.stack = stack;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Entity getBleeder() {
        return bleeder;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public CombatType getType() {
        return type;
    }

    public double getBleedResistance() {
        return bleedResistance;
    }

    public void setBleedResistance(double bleedResistance) {
        this.bleedResistance = bleedResistance;
    }

    public double getBleedDamage() {
        return bleedDamage;
    }

    public void setBleedDamage(double bleedDamage) {
        this.bleedDamage = bleedDamage;
    }

    public int getStack() {
        return stack;
    }

    public void setStack(int stack) {
        this.stack = stack;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
