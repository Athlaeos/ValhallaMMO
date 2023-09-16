package me.athlaeos.valhallammo.skills.perk_rewards.implementations;

import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileManager;
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
        if (levels == 0) return;
        Profile p = ProfileManager.getPersistentProfile(player, skill.getProfileType());
        double expToGive = -p.getEXP();
        if (levels < 0) {
            for (int level = p.getLevel() - 1; level >= p.getLevel() + levels; level--) {
                expToGive -= skill.expForLevel(level);
            }
        } else {
            for (int level = p.getLevel() + 1; level <= p.getLevel() + levels; level++){
                expToGive += skill.expForLevel(level);
            }
        }
        skill.addEXP(player, expToGive, true, PlayerSkillExperienceGainEvent.ExperienceGainReason.COMMAND);
        // make sure the player ends up at 0 exp for the level
        if (p.getLevel() < levels)
            skill.addEXP(player, skill.expForLevel(p.getLevel() + 1) - p.getEXP(), true, PlayerSkillExperienceGainEvent.ExperienceGainReason.COMMAND);
        else if (p.getLevel() == levels)
            skill.addEXP(player, -p.getEXP(), true, PlayerSkillExperienceGainEvent.ExperienceGainReason.COMMAND);
        else
            skill.addEXP(player, skill.expForLevel(p.getLevel()) + p.getEXP(), true, PlayerSkillExperienceGainEvent.ExperienceGainReason.COMMAND);
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
