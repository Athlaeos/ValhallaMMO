package me.athlaeos.valhallammo.skills.perkresourcecost.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.hooks.VaultHook;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpense;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class EconomyExpense implements ResourceExpense {
    private double cost = 0;
    @Override
    public void initExpense(Object value) {
        if (value instanceof Number) {
            cost = (double) value;
        }
    }

    @Override
    public boolean canPurchase(Player p) {
        return ValhallaMMO.getHook(VaultHook.class).getEcon().getBalance(p) >= cost;
    }

    @Override
    public void purchase(Player p, boolean initialPurchase) {
        if (!initialPurchase) return;
        Economy e = ValhallaMMO.getHook(VaultHook.class).getEcon();
        if (e.getBalance(p) >= cost) e.withdrawPlayer(p, cost);
    }

    @Override
    public void refund(Player p) {
        Economy e = ValhallaMMO.getHook(VaultHook.class).getEcon();
        e.depositPlayer(p, cost);
    }

    private final boolean refundable = ConfigManager.getConfig("config.yml").get().getBoolean("forgettable_perks_refund_money");
    @Override
    public boolean isRefundable() {
        return refundable;
    }

    @Override
    public ResourceExpense createInstance() {
        return new EconomyExpense();
    }

    @Override
    public String getInsufficientFundsMessage() {
        return TranslationManager.getTranslation("warning_insufficient_money");
    }

    @Override
    public String getCostMessage() {
        return TranslationManager.getTranslation("status_economy_cost").replace("%cost%", "" + cost);
    }

    @Override
    public boolean canRegister() {
        return ValhallaMMO.isHookFunctional(VaultHook.class);
    }
}
