package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.skills.skills.Skill;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerSkillExperienceGainEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled;
    private double amount;
    private Skill leveledSkill;
    private ExperienceGainReason reason;

    public PlayerSkillExperienceGainEvent(Player player, double amount, Skill leveledSkill, ExperienceGainReason reason){
        super(player);
        this.amount = amount;
        this.leveledSkill = leveledSkill;
        this.reason = reason;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    public double getAmount() {
        return amount;
    }

    public Skill getLeveledSkill() {
        return leveledSkill;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setLeveledSkill(Skill leveledSkill) {
        this.leveledSkill = leveledSkill;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public ExperienceGainReason getReason() {
        return reason;
    }

    public void setReason(ExperienceGainReason reason) {
        this.reason = reason;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum ExperienceGainReason{
        SKILL_ACTION,
        EXP_SHARE,
        COMMAND,
        PLUGIN,
        RESET,
        REDEEM,
        ALPHA_REFUND,
        MERCHANT_TRAINING,
        OTHER
    }
}
