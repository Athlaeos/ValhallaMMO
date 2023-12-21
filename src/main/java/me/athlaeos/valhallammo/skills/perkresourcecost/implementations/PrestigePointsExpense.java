package me.athlaeos.valhallammo.skills.perkresourcecost.implementations;

import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpense;
import org.bukkit.entity.Player;

public class PrestigePointsExpense implements ResourceExpense {
    private int cost;
    @Override
    public void initExpense(Object value) {
        if (value instanceof Number) cost = (Integer) value;
    }

    @Override
    public boolean canPurchase(Player p) {
        PowerProfile profile = ProfileRegistry.getMergedProfile(p, PowerProfile.class);
        return profile.getSpendablePrestigePoints() - profile.getSpentPrestigePoints() >= cost;
    }

    @Override
    public void purchase(Player p, boolean initialPurchase) {
        PowerProfile profile = ProfileRegistry.getSkillProfile(p, PowerProfile.class);
        profile.setSpentPrestigePoints(profile.getSpentPrestigePoints() + cost);
        ProfileRegistry.setSkillProfile(p, profile, PowerProfile.class);
    }

    @Override
    public void refund(Player p) {
        PowerProfile profile = ProfileRegistry.getSkillProfile(p, PowerProfile.class);
        profile.setSpentPrestigePoints(profile.getSpentPrestigePoints() - cost);
        ProfileRegistry.setSkillProfile(p, profile, PowerProfile.class);
    }

    private final boolean refundable = ConfigManager.getConfig("config.yml").reload().get().getBoolean("forgettable_perks_refund_prestigepoints", true);
    @Override
    public boolean isRefundable() {
        return refundable;
    }

    @Override
    public ResourceExpense createInstance() {
        return new PrestigePointsExpense();
    }

    @Override
    public String getInsufficientFundsMessage() {
        return TranslationManager.getTranslation("warning_insufficient_prestigepoints");
    }

    @Override
    public String getCostMessage() {
        return TranslationManager.getTranslation("status_prestigepoints_cost").replace("%cost%", "" + cost);
    }
}
