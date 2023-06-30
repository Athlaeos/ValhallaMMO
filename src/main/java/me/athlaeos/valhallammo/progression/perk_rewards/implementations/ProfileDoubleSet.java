package me.athlaeos.valhallammo.progression.perk_rewards.implementations;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.progression.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.progression.perk_rewards.PerkRewardArgumentType;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileManager;
import me.athlaeos.valhallammo.playerstats.profiles.properties.StatProperties;
import org.bukkit.entity.Player;

public class ProfileDoubleSet extends PerkReward {
    private double value;
    private final String stat;
    private final Class<? extends Profile> type;
    public ProfileDoubleSet(String name, String stat, Class<? extends Profile> type) {
        super(name);
        this.stat = stat;
        this.type = type;
    }

    @Override
    public void apply(Player player) {
        if (isPersistent()) {
            Profile profile = ProfileManager.getPersistentProfile(player, type);
            profile.setDouble(stat, value);
            ProfileManager.setPersistentProfile(player, profile, type);
        } else {
            Profile profile = ProfileManager.getSkillProfile(player, type);
            profile.setDouble(stat, value);
            ProfileManager.setSkillProfile(player, profile, type);
        }
        AccumulativeStatManager.updateStats(player);
    }

    @Override
    public void remove(Player player) { }

    @Override
    public void parseArgument(Object argument) {
        value = parseDouble(argument);
    }

    @Override
    public String rewardPlacholder() {
        StatProperties properties = ProfileManager.getRegisteredProfiles().get(type).getNumberStatProperties().get(stat);
        if (properties == null) return StatFormat.FLOAT_P2.format(value);
        return properties.getFormat().format(value);
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.DOUBLE;
    }
}
