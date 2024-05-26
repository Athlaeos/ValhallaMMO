package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.MoonPhase;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.*;
import java.util.stream.Collectors;

public class MoonPhaseFilter extends LootPredicate {
    private final Collection<MoonPhase> phases = new HashSet<>();
    private MoonPhase phase = null;

    @Override
    public String getKey() {
        return "moon_phase";
    }

    @Override
    public Material getIcon() {
        return Material.END_STONE;
    }

    @Override
    public String getDisplayName() {
        return "&fMoon Phase";
    }

    @Override
    public String getDescription() {
        return "&fRequires the world to have a specific moon phase. ";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the world's moon phase to " + (isInverted() ? "&cNOT&f " : "") + "be in one of &e" + phases.stream().map(MoonPhase::toString).collect(Collectors.joining(", "));
    }

    @Override
    public LootPredicate createNew() {
        return new MoonPhaseFilter();
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        List<MoonPhase> phases = List.of(MoonPhase.values());
        String info;
        if (phase == null) phase = phases.get(0);
        int currentIndex = phases.indexOf(phase);
        String before = currentIndex <= 0 ? "" : phases.get(currentIndex - 1) + " > &e";
        String after = currentIndex + 1 >= phases.size() ? "" : "&f > " + phases.get(currentIndex + 1);
        info = "&f" + before + phase + after;

        return new Pair<>(2,
                new ItemBuilder(Material.TNT)
                        .name("&eInvert Condition")
                        .lore(inverted ? "&cCondition is inverted" : "&aCondition not inverted",
                                "&fInverted conditions must &cnot &fpass",
                                "&fthis condition. ",
                                "&6Click to toggle")
                        .get()).map(Set.of(
                                new Pair<>(12,
                                        new ItemBuilder(Material.GRASS_BLOCK)
                                                .name("&eSelect Phase")
                                                .lore(info,
                                                        "&6Click to cycle",
                                                        "&6Shift-Click to add to selection",
                                                        "&cMiddle-Click to clear selection",
                                                        "&fCurrently: ")
                                                .appendLore(phases.isEmpty() ? List.of("&cNone, condition always passes") : phases.stream().map(b -> "&f>" + b).toList())
                                                .get()))
        );
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
        else if (button == 12){
            if (e.isShiftClick()) phases.add(phase);
            else if (e.getClick() == ClickType.MIDDLE) phases.clear();
            else {
                List<MoonPhase> phases = List.of(MoonPhase.values());
                if (phases.isEmpty()) return;
                int currentIndex = phase == null ? -1 : phases.indexOf(phase);
                currentIndex = Math.max(0, Math.min(phases.size() - 1, currentIndex + (e.isLeftClick() ? 1 : -1)));
                phase = phases.get(currentIndex);
            }
        }
    }

    @Override
    public boolean test(LootContext context) {
        if (phases.isEmpty() || context.getLocation().getWorld() == null) return inverted;
        return phases.contains(MoonPhase.getPhase(context.getLocation().getWorld())) != inverted;
    }
}
