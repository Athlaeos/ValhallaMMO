package me.athlaeos.valhallammo.skills.perkresourcecost.implementations;

import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpense;
import org.bukkit.entity.Player;

public class ExperienceLevelsExpense implements ResourceExpense {
    private int cost;

    @Override
    public void initExpense(Object value) {
        if (value instanceof Number){
            cost = (int) value;
        }
    }

    @Override
    public boolean canPurchase(Player p) {
        return p.getLevel() >= cost;
    }

    @Override
    public void purchase(Player p, boolean initialPurchase) {
        if (!initialPurchase) return;
        p.setLevel(p.getLevel() - cost);
    }

    @Override
    public void refund(Player p) {
        p.setLevel(p.getLevel() + cost);
    }

    private final boolean refundable = ConfigManager.getConfig("config.yml").reload().get().getBoolean("forgettable_perks_refund_levels");
    @Override
    public boolean isRefundable() {
        return refundable;
    }

    @Override
    public ResourceExpense createInstance() {
        return new ExperienceLevelsExpense();
    }

    @Override
    public String getInsufficientFundsMessage() {
        return TranslationManager.getTranslation("warning_insufficient_levels");
    }

    @Override
    public String getCostPlaceholder() {
        return "%cost_levels%";
    }

    @Override
    public String getInsufficientCostPlaceholder() {
        return "%warning_cost_levels%";
    }

    @Override
    public String getCostMessage() {
        return TranslationManager.getTranslation("status_levels_cost").replace("%cost%", "" + cost);
    }
}
