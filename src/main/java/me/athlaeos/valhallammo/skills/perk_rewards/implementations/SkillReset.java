package me.athlaeos.valhallammo.skills.perk_rewards.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import org.bukkit.entity.Player;

import java.util.List;

public class SkillReset extends PerkReward {
    private String skill;
    public SkillReset(String name) {
        super(name);
    }

    @Override
    public void apply(Player player) {
        Skill s = SkillRegistry.getSkill(skill);
        if (s != null) ProfileRegistry.reset(player, s.getClass());
        else ValhallaMMO.logWarning("Skill reset reward executed, but skill " + skill + " is not a valid skill!");
    }

    @Override
    public void remove(Player player) {

    }

    @Override
    public void parseArgument(Object argument) {
        skill = parseString(argument);
    }

    @Override
    public String rewardPlacholder() {
        Skill s = SkillRegistry.getSkill(skill);
        if (s != null) return s.getDisplayName();
        return "";
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.STRING;
    }

    @Override
    public List<String> getTabAutoComplete(String currentArg) {
        return SkillRegistry.getAllSkills().values().stream().map(Skill::getType).toList();
    }
}
