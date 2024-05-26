package me.athlaeos.valhallammo.skills.perk_rewards.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import org.bukkit.entity.Player;

public class SkillReset extends PerkReward {
    private final String skill;
    public SkillReset(String name, String skill) {
        super(name);
        this.skill = skill;
    }

    @Override
    public void apply(Player player) {
        Skill s = SkillRegistry.getSkill(skill);
        if (s != null) {
            ProfileRegistry.reset(player, s.getClass());
        } else ValhallaMMO.logWarning("Skill reset reward executed, but skill " + skill + " is not a valid skill!");
    }

    @Override
    public void remove(Player player) {

    }

    @Override
    public void parseArgument(Object argument) {
    }

    @Override
    public String rewardPlaceholder() {
        Skill s = SkillRegistry.getSkill(skill);
        if (s != null) return s.getDisplayName();
        return "";
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.NONE;
    }

    @Override
    public boolean isPersistent() {
        return true;
    }
}
