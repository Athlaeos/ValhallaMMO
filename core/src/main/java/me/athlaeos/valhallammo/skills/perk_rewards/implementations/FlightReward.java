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
import org.bukkit.persistence.PersistentDataType;

public class FlightReward extends PerkReward {
    private static final NamespacedKey KEY_GRANTED_FLIGHT = new NamespacedKey(ValhallaMMO.getInstance(), "granted_flight");
    public FlightReward() {
        super("enable_flight");
    }

    @Override
    public void apply(Player player) {
        Profile profile = isPersistent() ? ProfileRegistry.getPersistentProfile(player, PowerProfile.class) : ProfileRegistry.getSkillProfile(player, PowerProfile.class);

        profile.setBoolean("flight", true);

        if (isPersistent()) ProfileRegistry.setPersistentProfile(player, profile, PowerProfile.class);
        else ProfileRegistry.setSkillProfile(player, profile, PowerProfile.class);

        setFlight(player, true);
    }

    @Override
    public void remove(Player player) {
        Profile profile = isPersistent() ? ProfileRegistry.getPersistentProfile(player, PowerProfile.class) : ProfileRegistry.getSkillProfile(player, PowerProfile.class);

        profile.setBoolean("flight", false);

        if (isPersistent()) ProfileRegistry.setPersistentProfile(player, profile, PowerProfile.class);
        else ProfileRegistry.setSkillProfile(player, profile, PowerProfile.class);

        setFlight(player, false);
    }

    public static void setFlight(Player p, boolean flight){
        if (flight){
            PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
            if (!p.getAllowFlight() && profile.getBoolean("flight")) {
                // player may not fly
                p.setAllowFlight(true);
                p.getPersistentDataContainer().set(KEY_GRANTED_FLIGHT, PersistentDataType.BYTE, (byte) 0);
            }
        } else {
            if (p.getAllowFlight() && p.getPersistentDataContainer().has(KEY_GRANTED_FLIGHT, PersistentDataType.BYTE)){
                // player may fly, and it's because they've been granted it through this reward
                p.setAllowFlight(false);
                p.getPersistentDataContainer().remove(KEY_GRANTED_FLIGHT);
            }
        }
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
