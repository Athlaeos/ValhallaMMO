package me.athlaeos.valhallammo.gui.implementations.loottablecategories;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.LootTableOverviewMenu;
import me.athlaeos.valhallammo.gui.implementations.ReplacementTableSelectionMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.loot.ReplacementTable;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static me.athlaeos.valhallammo.gui.implementations.LootTableOverviewMenu.KEY_TABLE;

public class BlockReplacementTables extends TableCategory {
    public BlockReplacementTables(int position) {
        super("replacement_table_blocks",
                new ItemBuilder(Material.BRICKS).name("&eBlock Replacement Tables").lore("&fReplacement tables assigned to blocks, ", "&fexecuted on block drops.", "", "&eReplacement tables scan loot items", "&eand replace them with others", "&eunder certain conditions").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF315\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_blocktables")));
    }

    @Override
    public List<ItemStack> getCategoryOptions() {
        List<ItemBuilder> buttons = new ArrayList<>();
        for (Material m : Material.values()){
            if (!m.isBlock() || m.isAir()) continue;
            ReplacementTable typeTable = LootTableRegistry.getReplacementTable(m);
            ItemBuilder builder = new ItemBuilder(m.isItem() ? m : Material.BARRIER)
                    .name((typeTable != null ? "&a" : "&c") + m)
                    .stringTag(KEY_TABLE, m.toString());
            if (typeTable == null) builder.lore("&cNo replacement table set");
            else builder.lore("&aHas replacement table: " + typeTable.getKey());
            builder.appendLore("&fClick to set new replacement table", "&fShift-Click to remove replacement table");
            buttons.add(builder);
        }
        buttons.sort(Comparator.comparing(ItemUtils::getItemName));
        return buttons.stream().map(ItemBuilder::get).collect(Collectors.toList());
    }

    @Override
    public void onButtonClick(InventoryClickEvent e, String storedValue, Menu openedFrom) {
        if (StringUtils.isEmpty(storedValue)) return;
        Material m = Catch.catchOrElse(() -> Material.valueOf(storedValue), null);
        if (m == null) return;
        if (!e.isShiftClick()) new ReplacementTableSelectionMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()), openedFrom, (ReplacementTable table) -> {
            LootTableRegistry.getBlockReplacementTables().put(m.toString(), table.getKey());
            new LootTableOverviewMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()), LootTableOverviewMenu.BLOCKS_REPLACEMENT.getId()).open();
        }).open();
        else LootTableRegistry.getBlockReplacementTables().remove(m.toString());
    }
}
