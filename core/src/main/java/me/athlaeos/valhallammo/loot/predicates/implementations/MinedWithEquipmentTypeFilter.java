package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.MiningSpeed;
import me.athlaeos.valhallammo.listeners.LootListener;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;

import java.util.*;
import java.util.stream.Collectors;

public class MinedWithEquipmentTypeFilter extends LootPredicate {
    private static final List<EquipmentClass> types = List.of(EquipmentClass.values());
    private final Collection<EquipmentClass> equipmentClasses = new HashSet<>();
    private EquipmentClass equipmentClass = EquipmentClass.PICKAXE;

    @Override
    public String getKey() {
        return "mined_with_equipment_type";
    }

    @Override
    public Material getIcon() {
        return Material.IRON_PICKAXE;
    }

    @Override
    public String getDisplayName() {
        return "&fMined with Equipment Type";
    }

    @Override
    public String getDescription() {
        return "&fRequires the block to be mined with a type of equipment";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the block to " + (isInverted() ? "&cNOT&f " : "") + "be mined with &e" + equipmentClasses.stream().map(EquipmentClass::toString).collect(Collectors.joining(", "));
    }

    @Override
    public LootPredicate createNew() {
        return new MinedWithEquipmentTypeFilter();
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        int currentIndex = types.indexOf(equipmentClass);
        String before = currentIndex <= 0 ? "" : types.get(currentIndex - 1) + " > &e";
        String after = currentIndex + 1 >= types.size() ? "" : "&f > " + types.get(currentIndex + 1);
        String info = "&f" + before + equipmentClass + after;

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
                                                .name("&eSelect Entity Type")
                                                .lore(info,
                                                        "&6Click to cycle",
                                                        "&6Shift-Click to add to selection",
                                                        "&cMiddle-Click to clear selection",
                                                        "&fCurrently: ")
                                                .appendLore(types.isEmpty() ? List.of("&cNone, condition always passes") : types.stream().map(b -> "&f>" + b).toList())
                                                .get()))
        );
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
        else if (button == 12){
            if (e.isShiftClick()) equipmentClasses.add(equipmentClass);
            else if (e.getClick() == ClickType.MIDDLE) equipmentClasses.clear();
            else {
                if (types.isEmpty()) return;
                int currentIndex = equipmentClass == null ? -1 : types.indexOf(equipmentClass);
                currentIndex = Math.max(0, Math.min(types.size() - 1, currentIndex + (e.isLeftClick() ? 1 : -1)));
                equipmentClass = types.get(currentIndex);
            }
        }
    }

    @Override
    public boolean test(LootContext context) {
        if (!(context.getLootedEntity() instanceof Player p) || equipmentClasses.isEmpty()) return inverted;
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (ItemUtils.isEmpty(tool)) return inverted;
        ItemMeta meta = ItemUtils.getItemMeta(tool);
        if (meta == null) return inverted;

        return equipmentClasses.contains(EquipmentClass.getMatchingClass(meta)) != inverted;
    }

    @Override
    public boolean isCompatibleWithLootType(LootTable.LootType type) {
        return type == LootTable.LootType.BREAK;
    }
}
