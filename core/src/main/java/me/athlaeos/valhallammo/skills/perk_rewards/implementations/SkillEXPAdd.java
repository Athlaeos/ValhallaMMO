package me.athlaeos.valhallammo.skills.perk_rewards.implementations;

import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;
import me.athlaeos.valhallammo.skills.skills.Skill;
import org.bukkit.entity.Player;

public class SkillEXPAdd extends PerkReward {
    private double exp = 0;
    private final Skill skill;
    public SkillEXPAdd(String name, Skill skill) {
        super(name);
        this.skill = skill;
    }

    @Override
    public void apply(Player player) {
        skill.addEXP(player, exp, true, PlayerSkillExperienceGainEvent.ExperienceGainReason.COMMAND);
    }

    @Override
    public void remove(Player player) {

    }

    @Override
    public void parseArgument(Object argument) {
        exp = parseDouble(argument);
    }

    @Override
    public String rewardPlaceholder() {
        return String.valueOf(exp);
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.DOUBLE;
    }
}
