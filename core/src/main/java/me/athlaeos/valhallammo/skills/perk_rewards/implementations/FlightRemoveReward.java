package me.athlaeos.valhallammo.skills.perk_rewards.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class FlightRemoveReward extends PerkReward {
    public FlightRemoveReward() {
        super("disable_flight");
    }

    @Override
    public void apply(Player player) {
        Profile profile = isPersistent() ? ProfileRegistry.getPersistentProfile(player, PowerProfile.class) : ProfileRegistry.getSkillProfile(player, PowerProfile.class);

        profile.setBoolean("flight", false);

        if (isPersistent()) ProfileRegistry.setPersistentProfile(player, profile, PowerProfile.class);
        else ProfileRegistry.setSkillProfile(player, profile, PowerProfile.class);
        ProfileCache.resetCache(player);

        FlightReward.setFlight(player, false);
    }

    @Override
    public void remove(Player player) {
        Profile profile = isPersistent() ? ProfileRegistry.getPersistentProfile(player, PowerProfile.class) : ProfileRegistry.getSkillProfile(player, PowerProfile.class);

        profile.setBoolean("flight", false);

        if (isPersistent()) ProfileRegistry.setPersistentProfile(player, profile, PowerProfile.class);
        else ProfileRegistry.setSkillProfile(player, profile, PowerProfile.class);
        ProfileCache.resetCache(player);

        FlightReward.setFlight(player, false);
    }

    @Override
    public void parseArgument(Object o) {

    }

    @Override
    public String rewardPlaceholder() {
        return TranslationManager.getTranslation("translation_toggles");
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.NONE;
    }
}
