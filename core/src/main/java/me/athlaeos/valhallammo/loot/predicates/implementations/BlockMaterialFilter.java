package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.*;
import java.util.stream.Collectors;

public class BlockMaterialFilter extends LootPredicate {
    private final Collection<Material> blocks = new HashSet<>();

    @Override
    public String getKey() {
        return "container_block_material";
    }

    @Override
    public Material getIcon() {
        return Material.BARREL;
    }

    @Override
    public String getDisplayName() {
        return "&fBlock Material";
    }

    @Override
    public String getDescription() {
        return "&fRequires the container to be one of the given material types";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the container to " + (isInverted() ? "&cNOT&f " : "") + "be one of &e" + blocks.stream().map(b -> b.toString().toLowerCase()).collect(Collectors.joining(", "));
    }

    @Override
    public LootPredicate createNew() {
        return new BlockMaterialFilter();
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
                        .appendLore(blocks.isEmpty() ? List.of("&cNone, condition always passes") : blocks.stream().map(b -> "&f>" + b.toString().toLowerCase()).toList())
                        .get()).map(Set.of(
                                new Pair<>(12,
                                        new ItemBuilder(Material.TNT)
                                                .name("&eSelect Type")
                                                .lore("&6Click with item in cursor to",
                                                        "&6add required block type",
                                                        "&cShift-Click to clear list",
                                                        "&fCurrently: ")
                                                .appendLore(blocks.isEmpty() ? List.of("&cNone, condition always passes") : blocks.stream().map(b -> "&f>" + b.toString().toLowerCase()).toList())
                                                .get()))
        );
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
        else if (button == 12){
            if (!ItemUtils.isEmpty(e.getCursor()) && e.getCursor().getType().isBlock() && !e.isShiftClick()) blocks.add(e.getCursor().getType());
            else if (e.isShiftClick()) blocks.clear();
        }
    }

    @Override
    public boolean test(LootContext context) {
        if (blocks.isEmpty()) return true;
        return blocks.contains(context.getLocation().getBlock().getType()) != this.inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return type == LootTable.LootType.CONTAINER;
    }
}
