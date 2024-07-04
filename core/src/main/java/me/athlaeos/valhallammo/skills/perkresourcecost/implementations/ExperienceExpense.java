package me.athlaeos.valhallammo.skills.perkresourcecost.implementations;

import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpense;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.entity.Player;

public class ExperienceExpense implements ResourceExpense {
    private int cost;

    @Override
    public void initExpense(Object value) {
        if (value instanceof Number){
            cost = (int) value;
        }
    }

    @Override
    public boolean canPurchase(Player p) {
        return EntityUtils.getTotalExperience(p) >= cost;
    }

    @Override
    public void purchase(Player p, boolean initialPurchase) {
        if (!initialPurchase) return;
        EntityUtils.setTotalExperience(p, EntityUtils.getTotalExperience(p) - cost);
    }

    @Override
    public void refund(Player p) {
        EntityUtils.setTotalExperience(p, EntityUtils.getTotalExperience(p) + cost);
    }

    private final boolean refundable = ConfigManager.getConfig("config.yml").reload().get().getBoolean("forgettable_perks_refund_exp");
    @Override
    public boolean isRefundable() {
        return refundable;
    }

    @Override
    public ResourceExpense createInstance() {
        return new ExperienceExpense();
    }

    @Override
    public String getInsufficientFundsMessage() {
        return TranslationManager.getTranslation("warning_insufficient_experience");
    }

    @Override
    public String getCostPlaceholder() {
        return "%cost_experience%";
    }

    @Override
    public String getInsufficientCostPlaceholder() {
        return "%warning_cost_experience%";
    }

    @Override
    public String getCostMessage() {
        return TranslationManager.getTranslation("status_experience_cost").replace("%cost%", "" + cost);
    }
}
