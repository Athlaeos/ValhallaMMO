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

public class LuckFilter extends LootPredicate {
    private int from = 0;

    @Override
    public String getKey() {
        return "luck";
    }

    @Override
    public Material getIcon() {
        return Material.RABBIT_FOOT;
    }

    @Override
    public String getDisplayName() {
        return "&fLuck";
    }

    @Override
    public String getDescription() {
        return "&fRequires a minimum of luck";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires luck to " + (isInverted() ? "&cNOT&f " : "") + "be above &e" + from;
    }

    @Override
    public LootPredicate createNew() {
        return new LuckFilter();
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
                                new Pair<>(12,
                                        new ItemBuilder(Material.LIGHT)
                                                .name("&eSelect Luck Requirement")
                                                .lore("&eIs currently " + from,
                                                        "&fLuck must " + (isInverted() ? "&cNOT&f " : "") + "be above " + from,
                                                        "&6Click to add/subtract 1",
                                                        "&6Shift-Click to add/subtract 5")
                                                .get())));
    }

    public void setFrom(int from) {
        this.from = from;
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
        else if (button == 12) from += (e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 5 : 1);
    }

    @Override
    public boolean test(LootContext context) {
        return context.getLuck() >= from != inverted;
    }
}
