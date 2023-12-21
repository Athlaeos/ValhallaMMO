package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.dom.Structures;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.util.StructureSearchResult;

import java.util.*;
import java.util.stream.Collectors;

public class NearbyStructureFilter extends LootPredicate {
    private static final List<String> structs;
    static {
        structs = Arrays.stream(Structures.values()).map(Structures::toString).toList();
    }
    private final Map<Structures, Integer> structures = new HashMap<>();
    private String structure = "VILLAGE_TAIGA";
    private int range = 2;

    @Override
    public String getKey() {
        return "nearby_structure";
    }

    @Override
    public Material getIcon() {
        return Material.EMERALD;
    }

    @Override
    public String getDisplayName() {
        return "&fNearby Structure";
    }

    @Override
    public String getDescription() {
        return "&fRequires area to be within range of the given structure(s). &cNot recommended if no NMS version was registered on startup, because range parameter is ignored by many structures and can so cause lag.";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the area to " + (isInverted() ? "&cNOT&f " : "") + "be within range of &e" + structures.keySet().stream().map(Structures::toString).collect(Collectors.joining(", "));
    }

    @Override
    public LootPredicate createNew() {
        return new NearbyStructureFilter();
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        String info;
        if (structs.isEmpty()) info = "&cNo structures found(???)";
        else {
            if (structure == null) structure = "VILLAGE_TAIGA";
            int currentIndex = structs.indexOf(structure);
            String before = currentIndex <= 0 ? "" : structs.get(currentIndex - 1) + " > &e";
            String after = currentIndex + 1 >= structs.size() ? "" : "&f > " + structs.get(currentIndex + 1);
            info = "&f" + before + structure + after;
        }
        return new Pair<>(2,
                new ItemBuilder(Material.TNT)
                        .name("&eInvert Condition")
                        .lore(inverted ? "&cCondition is inverted" : "&aCondition not inverted",
                                "&fInverted conditions must &cnot &fpass",
                                "&fthis condition. ",
                                "&6Click to toggle")
                        .get()).map(Set.of(
                new Pair<>(20,
                        new ItemBuilder(Material.EMERALD)
                                .name("&eAdd Villages")
                                .lore("&fAdds all village types to the list",
                                        "&fCurrently: ")
                                .appendLore(structures.isEmpty() ? List.of("&cNone, condition always passes") : structures.keySet().stream().map(b -> "&f>" + b + ":" + structures.get(b)).toList())
                                .get()),
                new Pair<>(21,
                        new ItemBuilder(Material.CRYING_OBSIDIAN)
                                .name("&eAdd Ruined Portals")
                                .lore("&fAdds all ruined portal types",
                                        "&fto the list",
                                        "&fCurrently: ")
                                .appendLore(structures.isEmpty() ? List.of("&cNone, condition always passes") : structures.keySet().stream().map(b -> "&f>" + b + ":" + structures.get(b)).toList())
                                .get()),
                new Pair<>(22,
                        new ItemBuilder(Material.NETHER_BRICK)
                                .name("&eAdd Nether Structures")
                                .lore("&fAdds all nether structure types",
                                        "&fto the list",
                                        "&fCurrently: ")
                                .appendLore(structures.isEmpty() ? List.of("&cNone, condition always passes") : structures.keySet().stream().map(b -> "&f>" + b + ":" + structures.get(b)).toList())
                                .get()),
                new Pair<>(23,
                        new ItemBuilder(Material.KELP)
                                .name("&eAdd Ocean Ruins")
                                .lore("&fAdds all ocean ruin types",
                                        "&fto the list",
                                        "&fCurrently: ")
                                .appendLore(structures.isEmpty() ? List.of("&cNone, condition always passes") : structures.keySet().stream().map(b -> "&f>" + b + ":" + structures.get(b)).toList())
                                .get()),
                new Pair<>(24,
                        new ItemBuilder(Material.OAK_PLANKS)
                                .name("&eAdd Shipwrecks")
                                .lore("&fAdds all shipwreck types to the list",
                                        "&fCurrently: ")
                                .appendLore(structures.isEmpty() ? List.of("&cNone, condition always passes") : structures.keySet().stream().map(b -> "&f>" + b + ":" + structures.get(b)).toList())
                                .get()),
                                new Pair<>(11,
                                        new ItemBuilder(Material.GRASS_BLOCK)
                                                .name("&eSelect Structure Type")
                                                .lore(info,
                                                        "&6Click to cycle",
                                                        "&6Shift-Click to add to selection",
                                                        "&cMiddle-Click to clear selection",
                                                        "&fCurrently: ")
                                                .appendLore(structures.isEmpty() ? List.of("&cNone, condition always passes") : structures.keySet().stream().map(b -> "&f>" + b + ":" + structures.get(b)).toList())
                                                .get()),
                new Pair<>(13,
                        new ItemBuilder(Material.GRASS_BLOCK)
                                .name("&eDetection Range")
                                .lore("&fSet to " + range,
                                        "&fDetermines the detection range in",
                                        "&fwhich to find structures.",
                                        "&eIN CHUNKS&f, should not be very",
                                        "&flarge or this will lag.",
                                        "&fUnexplored structures will not work.",
                                        "&6Click to add/subtract 1 (16bl)",
                                        "&6Shift-Click to add/subtract 4 (64bl)")
                                .get()))
        );
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
        else if (button == 11){
            if (e.isShiftClick()) structures.put(Structures.valueOf(structure), range);
            else if (e.getClick() == ClickType.MIDDLE) structures.clear();
            else {
                if (structs.isEmpty()) return;
                int currentIndex = structure == null ? -1 : structs.indexOf(structure);
                currentIndex = Math.max(0, Math.min(structs.size() - 1, currentIndex + (e.isLeftClick() ? 1 : -1)));
                structure = structs.get(currentIndex);
            }
        } else if (button == 13) range = Math.max(1, range + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 4 : 1)));
        else if (button == 20) {
            structures.put(Structures.VILLAGE_DESERT, range);
            structures.put(Structures.VILLAGE_PLAINS, range);
            structures.put(Structures.VILLAGE_SAVANNA, range);
            structures.put(Structures.VILLAGE_SNOWY, range);
            structures.put(Structures.VILLAGE_TAIGA, range);
        } else if (button == 21){
            structures.put(Structures.RUINED_PORTAL, range);
            structures.put(Structures.RUINED_PORTAL_DESERT, range);
            structures.put(Structures.RUINED_PORTAL_JUNGLE, range);
            structures.put(Structures.RUINED_PORTAL_MOUNTAIN, range);
            structures.put(Structures.RUINED_PORTAL_NETHER, range);
            structures.put(Structures.RUINED_PORTAL_OCEAN, range);
            structures.put(Structures.RUINED_PORTAL_SWAMP, range);
        } else if (button == 22){
            structures.put(Structures.BASTION_REMNANT, range);
            structures.put(Structures.FORTRESS, range);
        } else if (button == 23){
            structures.put(Structures.OCEAN_RUIN_COLD, range);
            structures.put(Structures.OCEAN_RUIN_WARM, range);
        } else if (button == 24){
            structures.put(Structures.SHIPWRECK, range);
            structures.put(Structures.SHIPWRECK_BEACHED, range);
        }
    }

    @Override
    public boolean test(LootContext context) {
        if (context.getLocation().getWorld() == null) return inverted;
        if (ValhallaMMO.getNms() != null){
            if (ValhallaMMO.getNms().getNearestStructure(context.getLocation().getWorld(), context.getLocation(), structures) != null) return !inverted;
        } else {
            for (Structures s : structures.keySet()){
                Structure structure = s.getStructure();
                int range = structures.get(s);
                StructureSearchResult nearest = context.getLocation().getWorld().locateNearestStructure(context.getLocation(), structure, range, false);
                if (nearest != null) return !inverted;
            }
        }
        return inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return true;
    }
}
