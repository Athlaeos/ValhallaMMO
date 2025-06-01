package me.athlaeos.valhallammo.trading.dom;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.dom.Weighted;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class MerchantTrade implements Weighted {
    private final String id;
    private ItemStack result = new ItemBuilder(Material.WHEAT).name("&fPlaceholder Trade").lore("&cReplace me!").get();
    private ItemStack scalingCostItem = new ItemStack(Material.EMERALD, 8);
    private ItemStack optionalCostItem = null;
    private List<DynamicItemModifier> modifiers = new ArrayList<>(); // modifiers executed on the result item before inserted as trade
    private Collection<LootPredicate> predicates = new HashSet<>(); // predicates that must be met for this trade to be a viable option
    private LootTable.PredicateSelection predicateSelection = LootTable.PredicateSelection.ANY;
    private float weight = 10; // the weight determines how likely this trade is to be picked as a villager's trade. a weight of exactly -1 means the trade is ALWAYS SELECTED and not included in the random selection
    private float weightQuality = 0; // quality is really a weight modifier scaling with luck, the formula for total weight is finalWeight = weight + (quality * luck). generally speaking, better luck = better trades. this stat may never bring weight below 0
    private float demandWeightModifier = 0; // determines how much more/less rare a trade is depending on how many times it's been traded in the past
    private int demandWeightMaxQuantity = 0; // determines the max amount of weight a trade's rarity can be offset with at maximum demand
    private int maxUses = 6;
    private int priceRandomNegativeOffset = 0;
    private int priceRandomPositiveOffset = 0;
    private float demandMaxUsesModifier = 0; // determines how many more/less times a trade can be traded depending on how many times it's been traded in the past
    private int demandMaxUsesMaxQuantity = 0; // determines the max amount of extra times a trade can be traded with at maximum demand
    private int villagerExperience = 1; // amount of experience the villager gets from this trade
    private float rewardsExperience = 1F;
    private float demandPriceMultiplier = 0;
    private float positiveReputationMultiplier = 1F; // if reputation is positive, multiply it by this amount
    private float negativeReputationMultiplier = 1F; // if reputation is negative, multiply it by this amount
    private int demandPriceMax = 0;
    private boolean fixedUseCount = false; // if true, the maxUses property may be scaled by the player's "TRADE_USE_MULTIPLIER" stat
    private boolean exclusive = false; // if true, the player must have this trade in their "exclusive trades" list in their TradingProfile to be able to access this trade
    private long restockDelay = 0; // allowed delay between restocking. A trade will not restock if the duration has not passed, even if the villager tries to.
    private float giftWeight = -1; // if positive, this trade may be gifted indefinitely. if 0, it may not be gifted. If negative, the weight is instead treated as a positive value, but the trade may only be gifted once per player
    private boolean refreshes = false; // (only works with daily restocks disabled) if enabled, the trade remains the same but modifiers are re-applied per day
    private double skillExp = 100; // the amount of trading skill exp granted when traded
    private boolean isTradeable = true; // if false, this item is only available as a gift
    private int maxOrderCount = 0; // max times this may be ordered. if 0, this trade cannot be ordered

    public MerchantTrade(String id){
        this.id = id;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public double getWeight(double luck, double fortune) {
        return weight == -1 ? -1 : (weight + Math.max(0, weightQuality * luck));
    }

    public String getID() { return id; }
    public ItemStack getResult() { return result; }
    public ItemStack getScalingCostItem() { return scalingCostItem; }
    public ItemStack getOptionalCostItem() { return optionalCostItem; }
    public List<DynamicItemModifier> getModifiers() { return modifiers; }
    public Collection<LootPredicate> getPredicates() { return predicates; }
    public LootTable.PredicateSelection getPredicateSelection() { return predicateSelection; }
    public float getWeightQuality() { return weightQuality; }
    public int getVillagerExperience() { return villagerExperience; }
    public float getDemandPriceMultiplier() { return demandPriceMultiplier; }
    public int getMaxUses() { return maxUses; }
    public float getDemandWeightModifier() { return demandWeightModifier; }
    public int getDemandWeightMaxQuantity() { return demandWeightMaxQuantity; }
    public float getDemandMaxUsesModifier() { return demandMaxUsesModifier; }
    public int getDemandMaxUsesMaxQuantity() { return demandMaxUsesMaxQuantity; }
    public int getDemandPriceMax() { return demandPriceMax; }
    public float getNegativeReputationMultiplier() { return negativeReputationMultiplier; }
    public float getPositiveReputationMultiplier() { return positiveReputationMultiplier; }
    public long getRestockDelay() { return restockDelay; }
    public int getPriceRandomNegativeOffset() { return priceRandomNegativeOffset; }
    public int getPriceRandomPositiveOffset() { return priceRandomPositiveOffset; }
    public float getGiftWeight() { return giftWeight; }
    public boolean refreshes() { return refreshes; }
    public double getSkillExp() { return skillExp; }
    public boolean isTradeable() { return isTradeable; }
    public int getMaxOrderCount() { return maxOrderCount; }

    public void setFixedUseCount(boolean fixedUseCount) { this.fixedUseCount = fixedUseCount; }
    public void setResult(ItemStack result) { this.result = result; }
    public void setEnchantingExperience(float rewardsExperience) { this.rewardsExperience = rewardsExperience; }
    public void setExclusive(boolean exclusive) { this.exclusive = exclusive; }
    public void setDemandWeightMaxQuantity(int demandWeightMaxQuantity) { this.demandWeightMaxQuantity = demandWeightMaxQuantity; }
    public void setDemandWeightModifier(float demandWeightModifier) { this.demandWeightModifier = demandWeightModifier; }
    public void setScalingCostItem(ItemStack scalingCostItem) { this.scalingCostItem = scalingCostItem; }
    public void setOptionalCostItem(ItemStack optionalCostItem) { this.optionalCostItem = optionalCostItem; }
    public void setModifiers(List<DynamicItemModifier> modifiers) { this.modifiers = modifiers; DynamicItemModifier.sortModifiers(this.modifiers); }
    public void setPredicates(Collection<LootPredicate> predicates) { this.predicates = predicates; }
    public void setPredicateSelection(LootTable.PredicateSelection predicateSelection) { this.predicateSelection = predicateSelection; }
    public void setWeight(float weight) { this.weight = weight; }
    public void setWeightQuality(float weightQuality) { this.weightQuality = weightQuality; }
    public void setVillagerExperience(int villagerExperience) { this.villagerExperience = villagerExperience; }
    public void setDemandPriceMultiplier(float demandPriceMultiplier) { this.demandPriceMultiplier = demandPriceMultiplier; }
    public void setMaxUses(int maxUses) { this.maxUses = maxUses; }
    public boolean hasFixedUseCount() { return fixedUseCount; }
    public float getEnchantingExperience() { return rewardsExperience; }
    public boolean isExclusive() { return exclusive; }
    public void setDemandMaxUsesMaxQuantity(int demandMaxUsesMaxQuantity) { this.demandMaxUsesMaxQuantity = demandMaxUsesMaxQuantity; }
    public void setDemandMaxUsesModifier(float demandMaxUsesModifier) { this.demandMaxUsesModifier = demandMaxUsesModifier; }
    public void setDemandPriceMax(int demandPriceMax) { this.demandPriceMax = demandPriceMax; }
    public void setNegativeReputationMultiplier(float negativeReputationMultiplier) { this.negativeReputationMultiplier = negativeReputationMultiplier; }
    public void setPositiveReputationMultiplier(float positiveReputationMultiplier) { this.positiveReputationMultiplier = positiveReputationMultiplier; }
    public void setRestockDelay(long delay) { this.restockDelay = delay; }
    public void setPriceRandomNegativeOffset(int priceRandomNegativeOffset) { this.priceRandomNegativeOffset = priceRandomNegativeOffset; }
    public void setPriceRandomPositiveOffset(int priceRandomPositiveOffset) { this.priceRandomPositiveOffset = priceRandomPositiveOffset; }
    public void setGiftWeight(float giftWeight) { this.giftWeight = giftWeight; }
    public void setRefreshes(boolean refreshes) { this.refreshes = refreshes; }
    public void setSkillExp(double skillExp) { this.skillExp = skillExp; }
    public void setTradeable(boolean tradeable) { isTradeable = tradeable; }
    public void setMaxOrderCount(int maxOrderCount) { this.maxOrderCount = maxOrderCount; }

    public float getPerTradeWeight(Player player, MerchantData.TradeData tradeData){
        if (!fixedUseCount){
            float perTradeWeight = 1;
            double expectedMaxTrades = maxUses * (1 + AccumulativeStatManager.getCachedStats("TRADING_USES_MULTIPLIER", player, 10000, true));
            expectedMaxTrades += Math.min(demandMaxUsesMaxQuantity, tradeData.getDemand() * demandMaxUsesMaxQuantity);
            expectedMaxTrades += AccumulativeStatManager.getCachedStats("TRADING_USES_BONUS", player, 10000, true);
            if (expectedMaxTrades <= 0) perTradeWeight = Integer.MAX_VALUE;
            else perTradeWeight = (float) (maxUses / expectedMaxTrades);
            return perTradeWeight;
        } else return 1;
    }

    public boolean failsPredicates(LootTable.PredicateSelection predicateSelection, LootContext context){
        if (predicates.isEmpty()) return false;
        return !switch (predicateSelection) {
            case ALL -> predicates.stream().allMatch(p -> p.test(context));
            case ANY -> predicates.stream().anyMatch(p -> p.test(context));
        };
    }
}
