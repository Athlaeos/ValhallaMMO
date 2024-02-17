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
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static me.athlaeos.valhallammo.gui.implementations.LootTableOverviewMenu.KEY_TABLE;

public class FishingLootTable extends LootTableCategory{
    private static final List<ItemBuilder> fishingTableEntries = new ArrayList<>();

    public FishingLootTable(int position) {
        super("loot_table_fishing",
                new ItemBuilder(Material.FISHING_ROD)
                        .name("&eFishing Loot Table").lore("&fLoot tables assigned to fishing").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF318\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_fishingtable")));
    }

    @Override
    public List<ItemStack> getCategoryOptions() {
        List<ItemBuilder> buttons = new ArrayList<>();
        LootTable fishingTable = LootTableRegistry.getFishingFishLootTable();
        LootTable treasureTable = LootTableRegistry.getFishingTreasureLootTable();
        LootTable junkTable = LootTableRegistry.getFishingJunkLootTable();

        ItemBuilder fishingBuilder = new ItemBuilder(Material.COD)
                .name((fishingTable != null ? "&a" : "&c") + "Fish Table")
                .stringTag(KEY_TABLE, "fish");
        if (fishingTable == null) fishingBuilder.lore("&cNo loot table set");
        else fishingBuilder.lore("&aHas loot table: " + fishingTable.getKey());
        fishingBuilder.appendLore("&fClick to set new loot table", "&fShift-Click to remove loot table");
        buttons.add(fishingBuilder);

        ItemBuilder treasureBuilder = new ItemBuilder(Material.COD)
                .name((treasureTable != null ? "&a" : "&c") + "Treasure Table")
                .stringTag(KEY_TABLE, "treasure");
        if (treasureTable == null) treasureBuilder.lore("&cNo loot table set");
        else treasureBuilder.lore("&aHas loot table: " + treasureTable.getKey());
        treasureBuilder.appendLore("&fClick to set new loot table", "&fShift-Click to remove loot table");
        buttons.add(treasureBuilder);

        ItemBuilder junkBuilder = new ItemBuilder(Material.COD)
                .name((junkTable != null ? "&a" : "&c") + "Junk Table")
                .stringTag(KEY_TABLE, "junk");
        if (junkTable == null) junkBuilder.lore("&cNo loot table set");
        else junkBuilder.lore("&aHas loot table: " + junkTable.getKey());
        junkBuilder.appendLore("&fClick to set new loot table", "&fShift-Click to remove loot table");
        buttons.add(junkBuilder);

        return buttons.stream().map(ItemBuilder::get).collect(Collectors.toList());
    }

    @Override
    public void onButtonClick(InventoryClickEvent e, String storedValue, Menu openedFrom) {
        if (StringUtils.isEmpty(storedValue)) return;
        if (!e.isShiftClick()) new LootTableSelectionMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()), openedFrom, (LootTable table) -> {
            switch (storedValue){
                case "fish" -> LootTableRegistry.setFishingLootTableFish(table.getKey());
                case "treasure" -> LootTableRegistry.setFishingLootTableTreasure(table.getKey());
                case "junk" -> LootTableRegistry.setFishingLootTableJunk(table.getKey());
            }
            new LootTableOverviewMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()), LootTableOverviewMenu.FISHING.getId()).open();
        }).open();
        else switch (storedValue){
            case "fish" -> LootTableRegistry.setFishingLootTableFish(null);
            case "treasure" -> LootTableRegistry.setFishingLootTableTreasure(null);
            case "junk" -> LootTableRegistry.setFishingLootTableJunk(null);
        }
    }
}
