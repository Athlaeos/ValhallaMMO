package me.athlaeos.valhallammo.skills.perk_rewards.implementations;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileManager;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public class ProfileStringListAdd extends PerkReward {
    private List<String> value;
    private final String stat;
    private final Class<? extends Profile> type;
    public ProfileStringListAdd(String name, String stat, Class<? extends Profile> type) {
        super(name);
        this.stat = stat;
        this.type = type;
    }

    @Override
    public void apply(Player player) {
        if (isPersistent()) {
            Profile profile = ProfileManager.getPersistentProfile(player, type);
            Collection<String> existing = profile.getStringSet(stat);
            existing.addAll(value);
            profile.setStringSet(stat, existing);
            ProfileManager.setPersistentProfile(player, profile, type);
        } else {
            Profile profile = ProfileManager.getSkillProfile(player, type);
            Collection<String> existing = profile.getStringSet(stat);
            existing.addAll(value);
            profile.setStringSet(stat, existing);
            ProfileManager.setSkillProfile(player, profile, type);
        }
        AccumulativeStatManager.updateStats(player);
    }

    @Override
    public void remove(Player player) {
        if (isPersistent()) {
            Profile profile = ProfileManager.getPersistentProfile(player, type);
            Collection<String> existing = profile.getStringSet(stat);
            existing.removeAll(value);
            profile.setStringSet(stat, existing);
            ProfileManager.setPersistentProfile(player, profile, type);
        } else {
            Profile profile = ProfileManager.getSkillProfile(player, type);
            Collection<String> existing = profile.getStringSet(stat);
            existing.removeAll(value);
            profile.setStringSet(stat, existing);
            ProfileManager.setSkillProfile(player, profile, type);
        }
        AccumulativeStatManager.updateStats(player);
    }

    @Override
    public void parseArgument(Object argument) {
        value = parseStringList(argument);
    }

    @Override
    public String rewardPlacholder() {
        return "";
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.STRING_LIST;
    }
}
