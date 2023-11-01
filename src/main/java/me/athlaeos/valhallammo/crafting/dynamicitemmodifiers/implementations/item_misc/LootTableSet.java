package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LootTableSet extends DynamicItemModifier {
    private String lootTable = null;


    public LootTableSet(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        LootTableRegistry.setLootTable(outputItem.getMeta(), LootTableRegistry.getLootTables().get(lootTable));
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        List<LootTable> tables = new ArrayList<>(LootTableRegistry.getLootTables().values());
        if (button == 12){
            if (e.isShiftClick()) lootTable = null;
            else {
                LootTable table = LootTableRegistry.getLootTables().get(lootTable);
                int currentTable = tables.indexOf(table);
                if (e.isLeftClick()) lootTable = tables.get(Math.max(0, Math.min(tables.size() - 1, currentTable + 1))).getKey();
                else lootTable = tables.get(Math.max(0, Math.min(tables.size() - 1, currentTable - 1))).getKey();
            }
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.CHEST)
                        .name("&fWhich loot table?")
                        .lore("&fLoot table set to &e" + lootTable,
                                "&6Click to cycle",
                                "&6Shift-Click to remove")
                        .get()).map(Set.of(
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.CHEST).get();
    }

    @Override
    public String getDisplayName() {
        return "&dLoot Table";
    }

    @Override
    public String getDescription() {
        return "&fAdds a loot table to/removes it from the item. Placeable blocks get this loot table applied to them when placed, items gift the loot table when clicked (consumed afterwards)";
    }

    @Override
    public String getActiveDescription() {
        return lootTable == null ? "&fRemoving loot table from item" : "&fSetting the loot table to &e" + lootTable;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    @Override
    public DynamicItemModifier createNew() {
        return new LootTableSet(getName());
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: the name of the loot table";
        lootTable = args[0];
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return new ArrayList<>(LootTableRegistry.getLootTables().keySet());
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
