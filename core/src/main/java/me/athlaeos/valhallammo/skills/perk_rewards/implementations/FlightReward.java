package me.athlaeos.valhallammo.skills.perk_rewards.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.localization.TranslationManager;
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
        if (!player.getAllowFlight()) {
            // player may not fly
            player.setAllowFlight(true);
            player.getPersistentDataContainer().set(KEY_GRANTED_FLIGHT, PersistentDataType.BYTE, (byte) 0);
        } // otherwise player is already allowed to fly, and they shouldn't be granted or revoked more
    }

    @Override
    public void remove(Player player) {
        if (player.getAllowFlight() && player.getPersistentDataContainer().has(KEY_GRANTED_FLIGHT, PersistentDataType.BYTE)){
            // player may fly, and it's because they've been granted it through this reward
            player.setAllowFlight(false);
            player.getPersistentDataContainer().remove(KEY_GRANTED_FLIGHT);
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
