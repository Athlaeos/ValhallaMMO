package me.athlaeos.valhallammo.gui.implementations.loottablecategories;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.LootTableOverviewMenu;
import me.athlaeos.valhallammo.gui.implementations.LootTableSelectionMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FishingLootTable extends LootTableCategory{
    public FishingLootTable(int position) {
        super("loot_table_fishing",
                new ItemBuilder(Material.FISHING_ROD)
                        .name("&eFishing Loot Table")
                        .lore("&fLoot table assigned to fishing")
                        .appendLore(
                                LootTableRegistry.getFishingLootTable() == null ?
                                        "&cNo loot table set" :
                                        "&aHas loot table: " + LootTableRegistry.getFishingTableName(),
                                "&fClick to set new loot table",
                                "&fShift-Click to remove loot table").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF318\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_fishingtable")));
    }

    @Override
    public List<ItemStack> getCategoryOptions() {
        return new ArrayList<>();
    }

    @Override
    public void onButtonClick(InventoryClickEvent e, String storedValue, Menu openedFrom) {
        if (!e.isShiftClick()) new LootTableSelectionMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()), openedFrom, (LootTable table) -> {
            LootTableRegistry.setFishingLootTable(table.getKey());
            new LootTableOverviewMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked())).open();
        }).open();
        else LootTableRegistry.setFishingLootTable(null);
    }
}
