package me.athlaeos.valhallammo.trading;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class MerchantTrade {
    private final String id;
    private ItemStack result = new ItemBuilder(Material.WHEAT).name("&fPlaceholder Trade").lore("&cReplace me!").get();
    private ItemStack scalingCostItem = new ItemStack(Material.EMERALD, 8);
    private ItemStack optionalCostItem = null;
    private List<DynamicItemModifier> modifiers = new ArrayList<>(); // modifiers executed on the result item before inserted as trade
    private Collection<LootPredicate> predicates = new HashSet<>(); // predicates that must be met for this trade to be a viable option
    private LootTable.PredicateSelection predicateSelection = LootTable.PredicateSelection.ANY;
    private double weight = 10; // the weight determines how likely this trade is to be picked as a villager's trade
    private double weightQuality = 0; // quality is really a weight modifier scaling with luck, the formula for total weight is finalWeight = weight + (quality * luck). generally speaking, better luck = better trades
    private boolean fixedPrice = false; // if price is fixed, it will not change regardless of reputation
    private int villagerExperience = 1; // amount of experience the villager gets from this trade
    private int specialPrice = 1;
    private float demandMultiplier = 1;

    public MerchantTrade(String id){
        this.id = id;
    }
}
