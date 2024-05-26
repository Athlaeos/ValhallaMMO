package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.*;

public class WorldFilter extends LootPredicate {
    private final Collection<String> worlds = new HashSet<>();
    private String world = null;

    @Override
    public String getKey() {
        return "world";
    }

    @Override
    public Material getIcon() {
        return Material.GLOBE_BANNER_PATTERN;
    }

    @Override
    public String getDisplayName() {
        return "&fWorld";
    }

    @Override
    public String getDescription() {
        return "&fRequires the environment to be within one or several worlds";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the environment to " + (isInverted() ? "&cNOT&f " : "") + "be in one of &e" + String.join(", ", worlds);
    }

    @Override
    public LootPredicate createNew() {
        return new WorldFilter();
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        List<String> worlds = ValhallaMMO.getInstance().getServer().getWorlds().stream().map(World::getName).toList();
        String info;
        if (worlds.isEmpty()) info = "&cNo worlds found(???)";
        else {
            if (world == null) world = worlds.get(0);
            int currentIndex = worlds.indexOf(world);
            String before = currentIndex <= 0 ? "" : worlds.get(currentIndex - 1) + " > &e";
            String after = currentIndex + 1 >= worlds.size() ? "" : "&f > " + worlds.get(currentIndex + 1);
            info = "&f" + before + world + after;
        }
        return new Pair<>(2,
                new ItemBuilder(Material.TNT)
                        .name("&eInvert Condition")
                        .lore(inverted ? "&cCondition is inverted" : "&aCondition not inverted",
                                "&fInverted conditions must &cnot &fpass",
                                "&fthis condition. ",
                                "&6Click to toggle")
                        .appendLore(worlds.isEmpty() ? List.of("&cNone, condition always passes") : worlds.stream().map(b -> "&f>" + b).toList())
                        .get()).map(Set.of(
                                new Pair<>(12,
                                        new ItemBuilder(Material.GRASS_BLOCK)
                                                .name("&eSelect World")
                                                .lore(info,
                                                        "&6Click to cycle",
                                                        "&6Shift-Click to add to selection",
                                                        "&cMiddle-Click to clear selection",
                                                        "&fCurrently: ")
                                                .appendLore(worlds.isEmpty() ? List.of("&cNone, condition always passes") : worlds.stream().map(b -> "&f>" + b).toList())
                                                .get()))
        );
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
        else if (button == 12){
            if (e.isShiftClick()) worlds.add(world);
            else if (e.getClick() == ClickType.MIDDLE) worlds.clear();
            else {
                List<String> worlds = ValhallaMMO.getInstance().getServer().getWorlds().stream().map(World::getName).toList();
                if (worlds.isEmpty()) return;
                int currentIndex = world == null ? -1 : worlds.indexOf(world);
                currentIndex = Math.max(0, Math.min(worlds.size() - 1, currentIndex + (e.isLeftClick() ? 1 : -1)));
                world = worlds.get(currentIndex);
            }
        }
    }

    @Override
    public boolean test(LootContext context) {
        if (context.getLocation().getWorld() == null || worlds.isEmpty()) return true;
        return worlds.contains(context.getLocation().getWorld().getName()) != inverted;
    }
}
