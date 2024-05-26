package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.DayTime;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.HashSet;
import java.util.Map;

public class DayFilter extends LootPredicate {
    @Override
    public String getKey() {
        return "daytime";
    }

    @Override
    public Material getIcon() {
        return Material.CLOCK;
    }

    @Override
    public String getDisplayName() {
        return "&fDaytime";
    }

    @Override
    public String getDescription() {
        return "&fRequires world time to be day";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the world time to " + (isInverted() ? "&cNOT&f " : "") + "be day";
    }

    @Override
    public LootPredicate createNew() {
        return new DayFilter();
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
        World w = context.getLocation().getWorld();
        if (w == null) return inverted;
        return DayTime.getTime(w).isDay() != inverted;
    }

}
