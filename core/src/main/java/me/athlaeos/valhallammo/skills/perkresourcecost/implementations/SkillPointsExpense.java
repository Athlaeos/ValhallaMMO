package me.athlaeos.valhallammo.skills.perkresourcecost.implementations;

import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpense;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import org.bukkit.entity.Player;

public class SkillPointsExpense implements ResourceExpense {
    private int cost;
    @Override
    public void initExpense(Object value) {
        if (value instanceof Number) cost = (Integer) value;
    }

    @Override
    public boolean canPurchase(Player p) {
        PowerProfile profile = ProfileRegistry.getMergedProfile(p, PowerProfile.class);
        return profile.getSpendableSkillPoints() - profile.getSpentSkillPoints() >= cost;
    }

    @Override
    public void purchase(Player p, boolean initialPurchase) {
        PowerProfile profile = ProfileRegistry.getSkillProfile(p, PowerProfile.class);
        // this may look odd first, since the skill profile will not be persisted, but it's actually intended for the
        // skill profile points to be able to go into the negatives

        // since the spent skill points are increased regardless if it's the initial purchase or not, the player's spent skill
        // points are increased when the skill stats are being calculated also.
        profile.setSpentSkillPoints(profile.getSpentSkillPoints() + cost);
        ProfileRegistry.setSkillProfile(p, profile, PowerProfile.class);
    }

    @Override
    public void refund(Player p) {
        PowerProfile profile = ProfileRegistry.getSkillProfile(p, PowerProfile.class);
        profile.setSpentSkillPoints(profile.getSpentSkillPoints() - cost);
        ProfileRegistry.setSkillProfile(p, profile, PowerProfile.class);
    }

    private final boolean refundable = ConfigManager.getConfig("config.yml").reload().get().getBoolean("forgettable_perks_refund_skillpoints", true);
    @Override
    public boolean isRefundable() {
        return refundable;
    }

    @Override
    public ResourceExpense createInstance() {
        return new SkillPointsExpense();
    }

    @Override
    public String getInsufficientFundsMessage() {
        return TranslationManager.getTranslation("warning_insufficient_skillpoints");
    }

    @Override
    public String getCostPlaceholder() {
        return "%cost%";
    }

    @Override
    public String getInsufficientCostPlaceholder() {
        return "%warning_cost%";
    }

    @Override
    public String getCostMessage() {
        return TranslationManager.getTranslation("status_skillpoints_cost").replace("%cost%", "" + cost);
    }
}
