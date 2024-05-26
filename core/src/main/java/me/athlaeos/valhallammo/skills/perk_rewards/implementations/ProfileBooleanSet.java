package me.athlaeos.valhallammo.skills.perk_rewards.implementations;

import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import org.bukkit.entity.Player;

public class ProfileBooleanSet extends PerkReward {
    private boolean value;
    private final String stat;
    private final Class<? extends Profile> type;
    public ProfileBooleanSet(String name, String stat, Class<? extends Profile> type) {
        super(name);
        this.stat = stat;
        this.type = type;
    }

    @Override
    public void apply(Player player) {
        Profile profile = isPersistent() ? ProfileRegistry.getPersistentProfile(player, type) : ProfileRegistry.getSkillProfile(player, type);

        profile.setBoolean(stat, value);

        if (isPersistent()) ProfileRegistry.setPersistentProfile(player, profile, type);
        else ProfileRegistry.setSkillProfile(player, profile, type);

        AccumulativeStatManager.updateStats(player);
    }

    @Override
    public void remove(Player player) {
        if (isPersistent()) {
            Profile profile = ProfileRegistry.getPersistentProfile(player, type);
            profile.setBoolean(stat, !value);
            ProfileRegistry.setPersistentProfile(player, profile, type);
        } else {
            Profile profile = ProfileRegistry.getSkillProfile(player, type);
            profile.setBoolean(stat, !value);
            ProfileRegistry.setSkillProfile(player, profile, type);
        }
        AccumulativeStatManager.updateStats(player);
    }

    @Override
    public void parseArgument(Object argument) {
        value = parseBoolean(argument);
    }

    @Override
    public String rewardPlaceholder() {
        return value ? TranslationManager.getTranslation("translation_enables") : TranslationManager.getTranslation("translation_disables");
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.BOOLEAN;
    }
}
