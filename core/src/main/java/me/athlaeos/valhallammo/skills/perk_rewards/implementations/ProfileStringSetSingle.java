package me.athlaeos.valhallammo.skills.perk_rewards.implementations;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ProfileStringSetSingle extends PerkReward {
    private String value;
    private final String stat;
    private final Class<? extends Profile> type;
    public ProfileStringSetSingle(String name, String stat, Class<? extends Profile> type) {
        super(name);
        this.stat = stat;
        this.type = type;
    }

    @Override
    public void apply(Player player) {
        Profile profile = isPersistent() ? ProfileRegistry.getPersistentProfile(player, type) : ProfileRegistry.getSkillProfile(player, type);

        Collection<String> existing = profile.getStringSet(stat);
        existing.clear();
        existing.add(value);
        profile.setStringSet(stat, existing);

        if (isPersistent()) ProfileRegistry.setPersistentProfile(player, profile, type);
        else ProfileRegistry.setSkillProfile(player, profile, type);

        AccumulativeStatManager.updateStats(player);
    }

    @Override
    public void remove(Player player) {
        Profile profile = isPersistent() ? ProfileRegistry.getPersistentProfile(player, type) : ProfileRegistry.getSkillProfile(player, type);

        Collection<String> existing = profile.getStringSet(stat);
        existing.clear();
        profile.setStringSet(stat, existing);

        if (isPersistent()) ProfileRegistry.setPersistentProfile(player, profile, type);
        else ProfileRegistry.setSkillProfile(player, profile, type);

        AccumulativeStatManager.updateStats(player);
    }

    @Override
    public void parseArgument(Object argument) {
        value = parseString(argument);
    }

    @Override
    public String rewardPlaceholder() {
        return "";
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.STRING;
    }
}
