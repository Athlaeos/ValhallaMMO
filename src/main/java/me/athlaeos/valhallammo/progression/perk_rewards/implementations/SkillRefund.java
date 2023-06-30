package me.athlaeos.valhallammo.progression.perk_rewards.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileManager;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.progression.Perk;
import me.athlaeos.valhallammo.progression.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.progression.perk_rewards.PerkRewardArgumentType;
import me.athlaeos.valhallammo.progression.perkresourcecost.ResourceExpense;
import me.athlaeos.valhallammo.progression.skills.Skill;
import me.athlaeos.valhallammo.progression.skills.SkillRegistry;
import me.athlaeos.valhallammo.progression.skills.implementations.PowerSkill;
import org.bukkit.entity.Player;

import java.util.List;

public class SkillRefund extends PerkReward {
    private String skill;
    public SkillRefund(String name) {
        super(name);
    }

    @Override
    public void apply(Player player) {
        Skill s = SkillRegistry.getSkill(skill);
        PowerProfile power = ProfileManager.getPersistentProfile(player, PowerProfile.class);
        if (s != null) {
            for (Perk perk : s.getPerks()){
                if (!power.getUnlockedPerks().contains(perk.getName())) continue;
                if (power.getFakeUnlockedPerks().contains(perk.getName())) continue;
                if (power.getPermanentlyLockedPerks().contains(perk.getName())) continue;
                for (ResourceExpense expense : perk.getExpenses()) {
                    if (expense.isRefundable()) expense.refund(player);
                }
            }
        }
        else ValhallaMMO.logWarning("Skill refund reward executed, but skill " + skill + " is not a valid skill!");
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
