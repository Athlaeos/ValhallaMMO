package me.athlaeos.valhallammo.progression.perkresourcecost;

import org.bukkit.entity.Player;

public interface ResourceExpense {
    void initExpense(Object value);

    boolean canPurchase(Player p);

    /**
     * Trigger the resource expense purchase. Typically this means just reducing or removing whatever was needed for
     * the purchase. initialPurchase will be true when the player has clicked the perk button, purchasing the perk for the first time.
     * initialPurchase will be false when skill stats are being calculated, essentially meaning perks are being pseudo-executed again.
     * Since you probably don't want things like money or exp to be reduced repeatedly, you can check if this property is true before
     * doing the deduction. SkillPoints are an example where this property does not matter, and it's actually required for available skill
     * point calculation to be accurate. <br>
     * This method will also be run without {@link ResourceExpense#canPurchase(Player)} being checked
     * @param p the player who does the purchase
     * @param initialPurchase whether this purchase is an active one or not
     */
    void purchase(Player p, boolean initialPurchase);
    void refund(Player p);
    boolean isRefundable();

    ResourceExpense createInstance();

    String getInsufficientFundsMessage();

    String getCostMessage();

    /**
     * Should be used to check if the plugin is capable of registering this expense, for example if it requires
     * another plugin to be hooked before it can function.
     * @return true if the expense is safe to register
     */
    default boolean canRegister() {
        return true;
    }
}
