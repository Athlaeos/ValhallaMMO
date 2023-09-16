package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.skills.skills.Skill;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerSkillLevelUpEvent extends PlayerEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Skill leveledSkill;
    private final int levelFrom;
    private final int levelTo;

    public PlayerSkillLevelUpEvent(Player player, Skill leveledSkill, int levelFrom, int levelTo){
        super(player);
        this.leveledSkill = leveledSkill;
        this.levelFrom = levelFrom;
        this.levelTo = levelTo;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Skill getSkill() {
        return leveledSkill;
    }

    public int getLevelTo() {
        return levelTo;
    }

    public int getLevelFrom() {
        return levelFrom;
    }
}
