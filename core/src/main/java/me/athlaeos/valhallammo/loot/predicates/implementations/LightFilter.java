package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.Map;
import java.util.Set;

public class LightFilter extends LootPredicate {
    private int from = 0;
    private int to = 15;

    @Override
    public String getKey() {
        return "light";
    }

    @Override
    public Material getIcon() {
        return Material.LIGHT;
    }

    @Override
    public String getDisplayName() {
        return "&fLight";
    }

    @Override
    public String getDescription() {
        return "&fRequires environment to be between specific light level values";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the light level to " + (isInverted() ? "&cNOT&f " : "") + "be between &e" + from + " &fand &e" + to;
    }

    @Override
    public LootPredicate createNew() {
        return new LightFilter();
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
                                        new ItemBuilder(Material.LIGHT)
                                                .name("&eSelect From")
                                                .lore("&eIs currently " + from,
                                                        "&fLight must " + (isInverted() ? "&cNOT&f " : "") + "be between",
                                                        String.format("&f%d and %d", from, to),
                                                        "&6Click to add/subtract 1",
                                                        "&6Shift-Click to add/subtract 5")
                                                .get()),
                new Pair<>(13,
                        new ItemBuilder(Material.LIGHT)
                                .name("&eSelect To")
                                .lore("&eIs currently " + to,
                                        "&fLight must " + (isInverted() ? "&cNOT&f " : "") + "be between",
                                        String.format("&f%d and %d", from, to),
                                        "&6Click to add/subtract 1",
                                        "&6Shift-Click to add/subtract 5")
                                .get()))
        );
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
        else if (button == 11) from = Math.max(0, Math.max(Math.min(15, to), from + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 5 : 1))));
        else if (button == 13) to = Math.max(0, Math.min(Math.min(15, from), to + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 5 : 1))));
    }

    @Override
    public boolean test(LootContext context) {
        int lightLevel = context.getLocation().getBlock().getLightLevel();
        return (lightLevel >= from && lightLevel <= to) != inverted;
    }
}
