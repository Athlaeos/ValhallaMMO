package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.*;

public class RegionFilter extends LootPredicate {
    private final Collection<String> regions = new HashSet<>();
    private String region = null;

    @Override
    public String getKey() {
        return "region";
    }

    @Override
    public Material getIcon() {
        return Material.RED_BANNER;
    }

    @Override
    public String getDisplayName() {
        return "&fRegion";
    }

    @Override
    public String getDescription() {
        return "&fRequires the environment to be within one or several regions";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the environment to " + (isInverted() ? "&cNOT&f " : "") + "be in one of &e" + String.join(", ", regions);
    }

    @Override
    public LootPredicate createNew() {
        return new RegionFilter();
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        if (!ValhallaMMO.isHookFunctional(WorldGuardHook.class)) return new HashMap<>();
        List<String> regions = new ArrayList<>(WorldGuardHook.getRegions());
        String info;
        if (regions.isEmpty()) info = "&cNo regions found";
        else {
            if (region == null) region = regions.get(0);
            int currentIndex = regions.indexOf(region);
            String before = currentIndex <= 0 ? "" : regions.get(currentIndex - 1) + " > &e";
            String after = currentIndex + 1 >= regions.size() ? "" : "&f > " + regions.get(currentIndex + 1);
            info = "&f" + before + region + after;
        }
        return new Pair<>(2,
                new ItemBuilder(Material.TNT)
                        .name("&eInvert Condition")
                        .lore(inverted ? "&cCondition is inverted" : "&aCondition not inverted",
                                "&fInverted conditions must &cnot &fpass",
                                "&fthis condition. ",
                                "&6Click to toggle")
                        .appendLore(regions.isEmpty() ? List.of("&cNone, condition always passes") : regions.stream().map(b -> "&f>" + b).toList())
                        .get()).map(Set.of(
                                new Pair<>(12,
                                        new ItemBuilder(Material.GRASS_BLOCK)
                                                .name("&eSelect Region")
                                                .lore(info,
                                                        "&6Click to cycle",
                                                        "&6Shift-Click to add to selection",
                                                        "&cMiddle-Click to clear selection",
                                                        "&fCurrently: ")
                                                .appendLore(regions.isEmpty() ? List.of("&cNone, condition always passes") : regions.stream().map(b -> "&f>" + b).toList())
                                                .get()))
        );
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (!ValhallaMMO.isHookFunctional(WorldGuardHook.class)) return;
        if (button == 2) inverted = !inverted;
        else if (button == 12){
            if (e.isShiftClick()) regions.add(region);
            else if (e.getClick() == ClickType.MIDDLE) regions.clear();
            else {
                List<String> regions = new ArrayList<>(WorldGuardHook.getRegions());
                if (regions.isEmpty()) return;
                int currentIndex = region == null ? -1 : regions.indexOf(region);
                currentIndex = Math.max(0, Math.min(regions.size() - 1, currentIndex + (e.isLeftClick() ? 1 : -1)));
                region = regions.get(currentIndex);
            }
        }
    }

    @Override
    public boolean test(LootContext context) {
        if (!ValhallaMMO.isHookFunctional(WorldGuardHook.class)) return true;
        if (regions.isEmpty()) return true;
        return regions.stream().anyMatch(r -> WorldGuardHook.isInRegion(context.getLocation(), r)) != inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return true;
    }
}
