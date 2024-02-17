package me.athlaeos.valhallammo.skills.perk_rewards.implementations;

import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.ResetType;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;
import org.bukkit.entity.Player;

public class ProgressReset extends PerkReward {
    private final ResetType type;
    public ProgressReset(String name, ResetType type) {
        super(name);
        this.type = type;
    }

    @Override
    public void apply(Player player) {
        ProfileRegistry.reset(player, type);
    }

    @Override
    public void remove(Player player) {

    }

    @Override
    public void parseArgument(Object argument) {

    }

    @Override
    public String rewardPlaceholder() {
        return "";
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.NONE;
    }
}
