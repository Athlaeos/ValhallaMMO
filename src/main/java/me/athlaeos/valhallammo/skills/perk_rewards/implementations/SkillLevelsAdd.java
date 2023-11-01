package me.athlaeos.valhallammo.skills.perk_rewards.implementations;

import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;
import me.athlaeos.valhallammo.skills.skills.Skill;
import org.bukkit.entity.Player;

public class SkillLevelsAdd extends PerkReward {
    private int levels = 0;
    private final Skill skill;
    public SkillLevelsAdd(String name, Skill skill) {
        super(name);
        this.skill = skill;
    }

    @Override
    public void apply(Player player) {
        skill.addLevels(player, levels, true, PlayerSkillExperienceGainEvent.ExperienceGainReason.COMMAND);
    }

    @Override
    public void remove(Player player) {

    }

    @Override
    public void parseArgument(Object argument) {
        levels = parseInt(argument);
    }

    @Override
    public String rewardPlacholder() {
        return String.valueOf(levels);
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.INTEGER;
    }
}
