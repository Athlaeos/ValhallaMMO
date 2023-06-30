package me.athlaeos.valhallammo.progression.perkresourcecost.implementations;

import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.progression.perkresourcecost.ResourceExpense;
import me.athlaeos.valhallammo.utility.EntityUtils;
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

    private final boolean refundable = ConfigManager.getConfig("config.yml").get().getBoolean("forgettable_perks_refund_levels");
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
    public String getCostMessage() {
        return TranslationManager.getTranslation("status_levels_cost").replace("%cost%", "" + cost);
    }
}
