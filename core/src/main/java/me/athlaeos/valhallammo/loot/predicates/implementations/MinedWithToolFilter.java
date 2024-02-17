package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;

import java.util.*;
import java.util.stream.Collectors;

public class MinedWithToolFilter extends LootPredicate {
    private final List<Pair<Material, Integer>> tools = new ArrayList<>();

    @Override
    public String getKey() {
        return "mined_with_tool";
    }

    @Override
    public Material getIcon() {
        return Material.DIAMOND_PICKAXE;
    }

    @Override
    public String getDisplayName() {
        return "&fMined with Tool";
    }

    @Override
    public String getDescription() {
        return "&fRequires the block to be mined with specific tool types";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the block to " + (isInverted() ? "&cNOT&f " : "") + "be mined with &e" + tools.stream().map(b -> b.getOne().toString() + (b.getTwo() == null ? "" : "(" + b.getTwo() + ")")).collect(Collectors.joining(", "));
    }

    @Override
    public LootPredicate createNew() {
        return new MinedWithToolFilter();
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
                                        new ItemBuilder(Material.TNT)
                                                .name("&eSelect Tool")
                                                .lore("&6Click with item in cursor to",
                                                        "&6add required tool type",
                                                        "&fIf tool also has custom model",
                                                        "&fdata, it is required also",
                                                        "&cShift-Click to clear list",
                                                        "&fCurrently: ")
                                                .appendLore(tools.isEmpty() ? List.of("&cNone, condition always passes") : tools.stream().map(b -> "&f>" + b.getOne().toString() + (b.getTwo() == null ? "" : "(" + b.getTwo() + ")")).toList())
                                                .get()))
        );
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        ItemStack cursor = e.getCursor();
        if (button == 2) inverted = !inverted;
        else if (button == 12){
            if (!ItemUtils.isEmpty(cursor) && !e.isShiftClick()) {
                ItemMeta meta = ItemUtils.getItemMeta(cursor);
                if (meta == null) return;
                Integer data = meta.hasCustomModelData() ? meta.getCustomModelData() : null;
                tools.add(new Pair<>(e.getCursor().getType(), data));
            }
            else if (e.isShiftClick()) tools.clear();
        }
    }

    @Override
    public boolean test(LootContext context) {
        if (tools.isEmpty() || !(context.getLootedEntity() instanceof Player p)) return inverted;
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (ItemUtils.isEmpty(tool)) return inverted;
        ItemMeta meta = ItemUtils.getItemMeta(tool);
        if (meta == null) return inverted;
        return tools.stream().anyMatch(pair ->
                pair.getOne() == tool.getType() &&
                ((pair.getTwo() == null && !meta.hasCustomModelData()) || (meta.hasCustomModelData() && pair.getTwo() != null && meta.getCustomModelData() == pair.getTwo()))
        ) != this.inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return type == LootTable.LootType.BREAK;
    }
}
