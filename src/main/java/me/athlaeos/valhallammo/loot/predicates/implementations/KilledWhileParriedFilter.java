package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.utility.Parryer;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.HashSet;
import java.util.Map;

public class KilledWhileParriedFilter extends LootPredicate {
    @Override
    public String getKey() {
        return "killed_while_parried";
    }

    @Override
    public Material getIcon() {
        return Material.SHIELD;
    }

    @Override
    public String getDisplayName() {
        return "&fKilled while Parried";
    }

    @Override
    public String getDescription() {
        return "&fRequires the entity to be killed while under the effects of being parried";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the entity to " + (isInverted() ? "&cNOT&f " : "") + "be killed while under the effects of being parried";
    }

    @Override
    public LootPredicate createNew() {
        return new KilledWhileParriedFilter();
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
        if (!(context.getLootedEntity() instanceof LivingEntity l)) return inverted;
        return Parryer.isParried(l) != inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return type == LootTable.LootType.KILL;
    }
}
