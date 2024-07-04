package me.athlaeos.valhallammo.skills.perk_rewards.implementations;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.skills.perk_rewards.MultiplicativeReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.properties.StatProperties;
import org.bukkit.entity.Player;

public class ProfileFloatAdd extends PerkReward implements MultiplicativeReward {
    private float value;
    private final String stat;
    private final Class<? extends Profile> type;
    public ProfileFloatAdd(String name, String stat, Class<? extends Profile> type) {
        super(name);
        this.stat = stat;
        this.type = type;
    }

    @Override
    public void apply(Player player) {
        apply(player, 1);
    }

    @Override
    public void remove(Player player) {
        remove(player, 1);
    }

    @Override
    public void apply(Player player, int multiplyBy) {
        Profile profile = isPersistent() ? ProfileRegistry.getPersistentProfile(player, type) : ProfileRegistry.getSkillProfile(player, type);

        profile.setFloat(stat, profile.getFloat(stat) + (value * multiplyBy));

        if (isPersistent()) ProfileRegistry.setPersistentProfile(player, profile, type);
        else ProfileRegistry.setSkillProfile(player, profile, type);

        AccumulativeStatManager.updateStats(player);
    }

    @Override
    public void remove(Player player, int multiplyBy) {
        if (isPersistent()) {
            Profile profile = ProfileRegistry.getPersistentProfile(player, type);
            profile.setFloat(stat, profile.getFloat(stat) - (value * multiplyBy));
            ProfileRegistry.setPersistentProfile(player, profile, type);
        } else {
            Profile profile = ProfileRegistry.getSkillProfile(player, type);
            profile.setFloat(stat, profile.getFloat(stat) - (value * multiplyBy));
            ProfileRegistry.setSkillProfile(player, profile, type);
        }
        AccumulativeStatManager.updateStats(player);
    }

    @Override
    public void parseArgument(Object argument) {
        value = parseFloat(argument);
    }

    @Override
    public String rewardPlaceholder() {
        StatProperties properties = ProfileRegistry.getRegisteredProfiles().get(type).getNumberStatProperties().get(stat);
        if (properties == null) return StatFormat.FLOAT_P2.format(value);
        return properties.getFormat().format(value);
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.FLOAT;
    }
}
