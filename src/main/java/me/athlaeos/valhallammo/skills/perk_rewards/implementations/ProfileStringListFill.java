package me.athlaeos.valhallammo.skills.perk_rewards.implementations;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileManager;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ProfileStringListFill extends PerkReward {
    private final String stat;
    private final Fetcher fetcher;
    private final Class<? extends Profile> type;
    public ProfileStringListFill(String name, String stat, Class<? extends Profile> type, Fetcher fetcher) {
        super(name);
        this.stat = stat;
        this.type = type;
        this.fetcher = fetcher;
    }

    @Override
    public void apply(Player player) {
        if (isPersistent()) {
            Profile profile = ProfileManager.getPersistentProfile(player, type);
            profile.setStringSet(stat, fetcher.fetch());
            ProfileManager.setPersistentProfile(player, profile, type);
        } else {
            Profile profile = ProfileManager.getSkillProfile(player, type);
            profile.setStringSet(stat, fetcher.fetch());
            ProfileManager.setSkillProfile(player, profile, type);
        }
        AccumulativeStatManager.updateStats(player);
    }

    @Override
    public void remove(Player player) {

    }

    @Override
    public void parseArgument(Object argument) {
    }

    @Override
    public String rewardPlacholder() {
        return "";
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.NONE;
    }

    public interface Fetcher {
        Collection<String> fetch();
    }
}
