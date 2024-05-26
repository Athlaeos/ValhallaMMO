package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.HashSet;
import java.util.Map;

public class BabyFilter extends LootPredicate {
    @Override
    public String getKey() {
        return "baby";
    }

    @Override
    public Material getIcon() {
        return Material.EGG;
    }

    @Override
    public String getDisplayName() {
        return "&fEntity killed is a baby";
    }

    @Override
    public String getDescription() {
        return "&fRequires the killed entity to be a baby";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the killed entity to " + (isInverted() ? "&cNOT&f " : "") + "be a baby";
    }

    @Override
    public LootPredicate createNew() {
        return new BabyFilter();
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(2,
                new ItemBuilder(Material.TNT)
                        .name("&eInvert Condition")
                        .lore(inverted ? "&cCondition is inverted" : "&aCondition not inverted",
                                "&fInverted conditions must &cnot &fpass",
                                "&fthis condition. ",
                                "&6Click to toggle")
                        .get()).map(new HashSet<>());
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
    }

    @Override
    public boolean test(LootContext context) {
        if (!(context.getLootedEntity() instanceof Ageable a)) return inverted;
        return a.isAdult() == inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return type == LootTable.LootType.KILL;
    }
}
