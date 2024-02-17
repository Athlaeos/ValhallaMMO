package me.athlaeos.valhallammo.loot;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.dom.Weighted;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class LootEntry implements Weighted {
    private final UUID uuid;
    private final String parentPool;
    private ItemStack drop;
    private int baseQuantityMin = 1; // item quantity is a random number between min and max
    private int baseQuantityMax = 1;
    private float quantityMinFortuneBase = 0; // the min and max modifiers based on player fortune/looting
    private float quantityMaxFortuneBase = 0;
    private final Collection<LootPredicate> predicates = new HashSet<>();
    private double chance = 0.1;
    private double chanceQuality = 0.5; // quality is really a chance modifier scaling with luck, the formula for total chance is finalWeight = chance + (quality * luck)
    private double weight = 10;
    private double weightQuality = 10; // same for chance quality, but for weight
    private boolean guaranteedPresent = false; // if true, this drop will always be present in the generated loot if the predicates all pass
    private List<DynamicItemModifier> modifiers = new ArrayList<>();
    private LootTable.PredicateSelection predicateSelection = LootTable.PredicateSelection.ANY;

    public LootEntry(UUID uuid, ItemStack drop, String parentPool){
        this.uuid = uuid;
        this.drop = drop;
        this.parentPool = parentPool;
    }

    public UUID getUuid() { return uuid; }
    public ItemStack getDrop() { return drop; }
    public String getParentPool() { return parentPool; }
    public void setDrop(ItemStack drop) { this.drop = drop; }
    public int getBaseQuantityMin() { return baseQuantityMin; }
    public void setBaseQuantityMin(int baseQuantityMin) { this.baseQuantityMin = baseQuantityMin; }
    public int getBaseQuantityMax() { return baseQuantityMax; }
    public void setBaseQuantityMax(int baseQuantityMax) { this.baseQuantityMax = baseQuantityMax; }
    public float getQuantityMinFortuneBase() { return quantityMinFortuneBase; }
    public void setQuantityMinFortuneBase(float quantityMinFortuneBase) { this.quantityMinFortuneBase = quantityMinFortuneBase; }
    public float getQuantityMaxFortuneBase() { return quantityMaxFortuneBase; }
    public void setQuantityMaxFortuneBase(float quantityMaxFortuneBase) { this.quantityMaxFortuneBase = quantityMaxFortuneBase; }
    public Collection<LootPredicate> getPredicates() { return predicates; }
    public double getChance() { return chance; }
    public void setChance(double chance) { this.chance = chance; }
    public double getChance(double luck) { return Math.max(0, chance + (chanceQuality * luck)); }
    public void setChanceQuality(double chanceQuality) { this.chanceQuality = chanceQuality; }
    public double getChanceQuality() { return chanceQuality; }
    @Override public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    @Override public double getWeight(double luck) { return Math.max(0, weight + (weightQuality * luck)); }
    public void setWeightQuality(double weightQuality) { this.weightQuality = weightQuality; }
    public double getWeightQuality() { return weightQuality; }
    public List<DynamicItemModifier> getModifiers() { return modifiers; }
    public LootTable.PredicateSelection getPredicateSelection() { return predicateSelection; }
    public void setPredicateSelection(LootTable.PredicateSelection predicateSelection) { this.predicateSelection = predicateSelection; }
    public void setModifiers(List<DynamicItemModifier> modifiers) {
        this.modifiers = modifiers;
        DynamicItemModifier.sortModifiers(this.modifiers);
    }
    public boolean isGuaranteedPresent() { return guaranteedPresent; }
    public void setGuaranteedPresent(boolean guaranteedPresent) { this.guaranteedPresent = guaranteedPresent; }
}
