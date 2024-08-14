package me.athlaeos.valhallammo.gui.implementations.loottablecategories;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.LootTableOverviewMenu;
import me.athlaeos.valhallammo.gui.implementations.LootTableSelectionMenu;
import me.athlaeos.valhallammo.gui.implementations.ReplacementTableSelectionMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.loot.ReplacementTable;
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

public class FishingReplacementTable extends TableCategory {
    private static final List<ItemBuilder> fishingTableEntries = new ArrayList<>();

    public FishingReplacementTable(int position) {
        super("replacement_table_fishing",
                new ItemBuilder(Material.SALMON)
                        .name("&eFishing Replacement Table").lore("&fReplacement tables assigned to fishing", "", "&eReplacement tables scan loot items", "&eand replace them with others", "&eunder certain conditions").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF318\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_fishingtable")));
    }

    @Override
    public List<ItemStack> getCategoryOptions() {
        List<ItemBuilder> buttons = new ArrayList<>();
        ReplacementTable fishingTable = LootTableRegistry.getFishingFishReplacementTable();
        ReplacementTable treasureTable = LootTableRegistry.getFishingTreasureReplacementTable();
        ReplacementTable junkTable = LootTableRegistry.getFishingJunkLReplacementTable();

        ItemBuilder fishingBuilder = new ItemBuilder(Material.COD)
                .name((fishingTable != null ? "&a" : "&c") + "Fish Table")
                .stringTag(KEY_TABLE, "fish");
        if (fishingTable == null) fishingBuilder.lore("&cNo replacement table set");
        else fishingBuilder.lore("&aHas replacement table: " + fishingTable.getKey());
        fishingBuilder.appendLore("&fClick to set new replacement table", "&fShift-Click to remove replacement table");
        buttons.add(fishingBuilder);

        ItemBuilder treasureBuilder = new ItemBuilder(Material.COD)
                .name((treasureTable != null ? "&a" : "&c") + "Treasure Table")
                .stringTag(KEY_TABLE, "treasure");
        if (treasureTable == null) treasureBuilder.lore("&cNo replacement table set");
        else treasureBuilder.lore("&aHas replacement table: " + treasureTable.getKey());
        treasureBuilder.appendLore("&fClick to set new replacement table", "&fShift-Click to remove replacement table");
        buttons.add(treasureBuilder);

        ItemBuilder junkBuilder = new ItemBuilder(Material.COD)
                .name((junkTable != null ? "&a" : "&c") + "Junk Table")
                .stringTag(KEY_TABLE, "junk");
        if (junkTable == null) junkBuilder.lore("&cNo replacement table set");
        else junkBuilder.lore("&aHas replacement table: " + junkTable.getKey());
        junkBuilder.appendLore("&fClick to set new replacement table", "&fShift-Click to remove replacement table");
        buttons.add(junkBuilder);

        return buttons.stream().map(ItemBuilder::get).collect(Collectors.toList());
    }

    @Override
    public void onButtonClick(InventoryClickEvent e, String storedValue, Menu openedFrom) {
        if (StringUtils.isEmpty(storedValue)) return;
        if (!e.isShiftClick()) new ReplacementTableSelectionMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()), openedFrom, (ReplacementTable table) -> {
            switch (storedValue){
                case "fish" -> LootTableRegistry.setFishingReplacementTableFish(table.getKey());
                case "treasure" -> LootTableRegistry.setFishingReplacementTableTreasure(table.getKey());
                case "junk" -> LootTableRegistry.setFishingReplacementTableJunk(table.getKey());
            }
            new LootTableOverviewMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()), LootTableOverviewMenu.FISHING_REPLACEMENT.getId()).open();
        }).open();
        else switch (storedValue){
            case "fish" -> LootTableRegistry.setFishingReplacementTableFish(null);
            case "treasure" -> LootTableRegistry.setFishingReplacementTableTreasure(null);
            case "junk" -> LootTableRegistry.setFishingReplacementTableJunk(null);
        }
    }
}
