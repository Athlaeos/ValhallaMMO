package me.athlaeos.valhallammo.gui.implementations.loottablecategories;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.LootTableOverviewMenu;
import me.athlaeos.valhallammo.gui.implementations.ReplacementTableSelectionMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
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

public class GlobalReplacementTable extends TableCategory {
    public GlobalReplacementTable(int position) {
        super("replacement_table_global",
                new ItemBuilder(Material.GRASS_BLOCK)
                        .name("&eGlobal Replacement Table").lore("&fReplacement tables assigned to any loot", "", "&eReplacement tables scan loot items", "&eand replace them with others", "&eunder certain conditions").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF318\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_fishingtable")));
    }

    @Override
    public List<ItemStack> getCategoryOptions() {
        List<ItemBuilder> buttons = new ArrayList<>();
        ReplacementTable globalTable = LootTableRegistry.getGlobalReplacementTable();

        ItemBuilder globalBuilder = new ItemBuilder(Material.GRASS_BLOCK)
                .name((globalTable != null ? "&a" : "&c") + "Global Table")
                .stringTag(KEY_TABLE, "global");
        if (globalTable == null) globalBuilder.lore("&cNo replacement table set");
        else globalBuilder.lore("&aHas replacement table: " + globalTable.getKey());
        globalBuilder.appendLore("&fClick to set new replacement table", "&fShift-Click to remove replacement table");
        buttons.add(globalBuilder);

        return buttons.stream().map(ItemBuilder::get).collect(Collectors.toList());
    }

    @Override
    public void onButtonClick(InventoryClickEvent e, String storedValue, Menu openedFrom) {
        if (StringUtils.isEmpty(storedValue)) return;
        if (!e.isShiftClick()) new ReplacementTableSelectionMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()), openedFrom, (ReplacementTable table) -> {
            if (storedValue.equals("global")) {
                LootTableRegistry.setGlobalReplacementTable(table.getKey());
            }
            new LootTableOverviewMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()), LootTableOverviewMenu.GLOBAL_REPLACEMENT.getId()).open();
        }).open();
        else if (storedValue.equals("global")) {
            LootTableRegistry.setGlobalReplacementTable(null);
        }
    }
}
