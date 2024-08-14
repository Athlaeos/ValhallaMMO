package me.athlaeos.valhallammo.loot;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.dom.Weighted;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ReplacementEntry implements Weighted {
    private final UUID uuid;
    private final String parentPool;
    private ItemStack replaceBy = new ItemBuilder(Material.IRON_PICKAXE).name("&fReplace me!").lore("&7I'm just a placeholder item!").get();
    private boolean tinker = false;
    private double weight = 10; // because replacement tables can only ever act on individual items, it will always use a weighted distribution system
    private double weightBonusLuck = 0;
    private double weightBonusLooting = 0;
    private final Collection<LootPredicate> predicates = new HashSet<>();
    private LootTable.PredicateSelection predicateSelection = LootTable.PredicateSelection.ANY;
    private List<DynamicItemModifier> modifiers = new ArrayList<>();

    public ReplacementEntry(UUID uuid, String parentPool){
        this.uuid = uuid;
        this.parentPool = parentPool;
    }

    public ReplacementEntry(UUID uuid, String parentPool, ItemStack replaceBy){
        this.uuid = uuid;
        this.parentPool = parentPool;
        this.replaceBy = replaceBy.clone();
    }

    public Collection<LootPredicate> getPredicates() { return predicates; }
    public double getWeight() { return weight; }

    @Override
    public double getWeight(double luck, double fortune) {
        return weight + (luck * weightBonusLuck) + (fortune + weightBonusLooting);
    }

    public ItemStack getReplaceBy() { return replaceBy; }
    public List<DynamicItemModifier> getModifiers() { return modifiers; }
    public LootTable.PredicateSelection getPredicateSelection() { return predicateSelection; }
    public String getParentPool() { return parentPool; }
    public UUID getUuid() { return uuid; }
    public boolean tinker() {return tinker;}
    public double getWeightBonusLooting() { return weightBonusLooting; }
    public double getWeightBonusLuck() { return weightBonusLuck; }

    public void setModifiers(List<DynamicItemModifier> modifiers) {
        this.modifiers = modifiers;
        DynamicItemModifier.sortModifiers(this.modifiers);
    }
    public void setPredicateSelection(LootTable.PredicateSelection predicateSelection) { this.predicateSelection = predicateSelection; }
    public void setReplaceBy(ItemStack replaceBy) { this.replaceBy = replaceBy; }
    public void setTinker(boolean tinker) { this.tinker = tinker; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setWeightBonusLooting(double weightBonusLooting) { this.weightBonusLooting = weightBonusLooting; }
    public void setWeightBonusLuck(double weightBonusLuck) { this.weightBonusLuck = weightBonusLuck; }
}
