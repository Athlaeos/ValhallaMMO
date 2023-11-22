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

import java.util.*;

public class TimePeriodFilter extends LootPredicate {
    private int after = 8000;
    private int before = 16000;

    @Override
    public String getKey() {
        return "time_range";
    }

    @Override
    public Material getIcon() {
        return Material.CLOCK;
    }

    @Override
    public String getDisplayName() {
        return "&fTime Range";
    }

    @Override
    public String getDescription() {
        return "&fRequires world to be between specific times";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the world time to " + (isInverted() ? "&cNOT&f " : "") + "be after &e" + after + " &fand before &e" + before;
    }

    @Override
    public LootPredicate createNew() {
        return new TimePeriodFilter();
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
                        .get()).map(Set.of(
                                new Pair<>(11,
                                        new ItemBuilder(Material.CLOCK)
                                                .name("&eSelect After")
                                                .lore("&eIs currently " + after,
                                                        "&fTime must " + (isInverted() ? "&cNOT&f " : "") + "be between",
                                                        String.format("&f%d(%s) and %d(%s)", before, DayTime.getTime(before), after, DayTime.getTime(after)),
                                                        "&6Click to add/subtract 100",
                                                        "&6Shift-Click to add/subtract 1000")
                                                .get()),
                new Pair<>(13,
                        new ItemBuilder(Material.CLOCK)
                                .name("&eSelect Before")
                                .lore("&eIs currently " + before,
                                        "&fTime must " + (isInverted() ? "&cNOT&f " : "") + "be between",
                                        String.format("&f%d(%s) and %d(%s)", before, DayTime.getTime(before), after, DayTime.getTime(after)),
                                        "&6Click to add/subtract 100",
                                        "&6Shift-Click to add/subtract 1000")
                                .get()))
        );
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
        else if (button == 11) after = Math.max(0, Math.max(Math.min(24000, before), after + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1000 : 100))));
        else if (button == 13) before = Math.max(0, Math.min(Math.min(24000, after), before + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1000 : 100))));
    }

    @Override
    public boolean test(LootContext context) {
        World w = context.getLocation().getWorld();
        if (w == null) return true;
        if (after > before) {
            int temp = after;
            after = before;
            before = temp;
        }
        return (w.getTime() >= before && w.getTime() <= after) != inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return true;
    }
}
