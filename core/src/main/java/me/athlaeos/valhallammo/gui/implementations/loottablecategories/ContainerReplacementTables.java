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
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;

import java.util.*;
import java.util.stream.Collectors;

import static me.athlaeos.valhallammo.gui.implementations.LootTableOverviewMenu.KEY_TABLE;

public class ContainerReplacementTables extends TableCategory {
    private static final Map<NamespacedKey, Material> lootTablesIconMapping = new HashMap<>();
    private static final Map<NamespacedKey, LootTables> lootTablesMapping = new HashMap<>();
    static {
        for (LootTables type : LootTables.values()){
            String baseType = getEntityIcon(type.toString());
            if (baseType == null) continue;
            Material icon = ItemUtils.stringToMaterial(baseType, Material.STRUCTURE_VOID);
            if (icon == null) continue;
            lootTablesIconMapping.put(type.getKey(), icon);
            lootTablesMapping.put(type.getKey(), type);
        }
    }


    public ContainerReplacementTables(int position) {
        super("replacement_table_containers",
                new ItemBuilder(Material.BARREL).name("&eDungeon Replacement Tables").lore("&fReplacement tables assigned to dungeon", "&floot, executed when generated.", "", "&eReplacement tables scan loot items", "&eand replace them with others", "&eunder certain conditions").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF316\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_containertables")));
    }

    @Override
    public List<ItemStack> getCategoryOptions() {
        List<ItemBuilder> buttons = new ArrayList<>();
        for (NamespacedKey l : lootTablesIconMapping.keySet()){
            ReplacementTable typeTable = LootTableRegistry.getReplacementTable(l);
            ItemBuilder builder = new ItemBuilder(lootTablesIconMapping.getOrDefault(l, Material.BARREL))
                    .name((typeTable != null ? "&a" : "&c") + l)
                    .stringTag(KEY_TABLE, lootTablesMapping.get(l).toString());
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
        LootTables en = Catch.catchOrElse(() -> LootTables.valueOf(storedValue), null);
        if (en == null) return;
        if (!e.isShiftClick()) new ReplacementTableSelectionMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()), openedFrom, (ReplacementTable table) -> {
            LootTableRegistry.getKeyedReplacementTables().put(en.getKey(), table.getKey());
            new LootTableOverviewMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()), LootTableOverviewMenu.CONTAINERS_REPLACEMENT.getId()).open();
        }).open();
        else LootTableRegistry.getKeyedReplacementTables().remove(en.getKey());
    }

    private static String getEntityIcon(String type){
        return switch(type){
            case "TRAIL_RUINS_ARCHAEOLOGY_COMMON" -> "TERRACOTTA";
            case "TRAIL_RUINS_ARCHAEOLOGY_RARE" -> "YELLOW_GLAZED_TERRACOTTA";
            case "OCEAN_RUIN_WARM_ARCHAEOLOGY" -> "BRAIN_CORAL";
            case "OCEAN_RUIN_COLD_ARCHAEOLOGY" -> "KELP";
            case "DESERT_PYRAMID_ARCHAEOLOGY" -> "SANDSTONE";
            case "JUNGLE_TEMPLE_DISPENSER" -> "DISPENSER";
            case "DESERT_WELL_ARCHAEOLOGY" -> "SAND";
            case "VILLAGE_SAVANNA_HOUSE" -> "ACACIA_PLANKS";
            case "UNDERWATER_RUIN_SMALL" -> "CRACKED_STONE_BRICKS";
            case "BASTION_HOGLIN_STABLE" -> "POLISHED_BLACKSTONE_BRICKS";
            case "VILLAGE_PLAINS_HOUSE" -> "OAK_PLANKS";
            case "VILLAGE_DESERT_HOUSE" -> "CHISELED_SANDSTONE";
            case "VILLAGE_CARTOGRAPHER" -> "CARTOGRAPHY_TABLE";
            case "ANCIENT_CITY_ICE_BOX" -> "PACKED_ICE";
            case "VILLAGE_WEAPONSMITH" -> "GRINDSTONE";
            case "VILLAGE_TAIGA_HOUSE" -> "SPRUCE_PLANKS";
            case "VILLAGE_SNOWY_HOUSE" -> "SNOW";
            case "UNDERWATER_RUIN_BIG" -> "SEA_LANTERN";
            case "STRONGHOLD_CROSSING" -> "CHAIN";
            case "STRONGHOLD_CORRIDOR" -> "IRON_BARS";
            case "ABANDONED_MINESHAFT" -> "CHEST_MINECART";
            case "STRONGHOLD_LIBRARY" -> "BOOKSHELF";
            case "SHIPWRECK_TREASURE" -> "WATER_BUCKET";
            case "VILLAGE_TOOLSMITH" -> "SMITHING_TABLE";
            case "SPAWN_BONUS_CHEST" -> "NETHER_STAR";
            case "END_CITY_TREASURE" -> "SHULKER_BOX";
            case "WOODLAND_MANSION" -> "TOTEM_OF_UNDYING";
            case "VILLAGE_SHEPHERD" -> "SHEARS";
            case "VILLAGE_FLETCHER" -> "WHITE_WOOL";
            case "SHIPWRECK_SUPPLY" -> "BARREL";
            case "PILLAGER_OUTPOST" -> "CROSSBOW";
            case "PIGLIN_BARTERING" -> "GOLD_INGOT";
            case "BASTION_TREASURE" -> "GOLD_BLOCK";
            case "VILLAGE_TANNERY" -> "CAULDRON";
            case "VILLAGE_BUTCHER" -> "BEEF";
            case "VILLAGE_ARMORER" -> "BLAST_FURNACE";
            case "SNIFFER_DIGGING" -> "SNIFFER_EGG";
            case "BURIED_TREASURE" -> "HEART_OF_THE_SEA";
            case "VILLAGE_TEMPLE" -> "EMERALD";
            case "VILLAGE_FISHER" -> "COD";
            case "SIMPLE_DUNGEON" -> "SPAWNER";
            case "DESERT_PYRAMID" -> "CHEST";
            case "BASTION_BRIDGE" -> "POLISHED_BLACKSTONE";
            case "VILLAGE_MASON" -> "STONECUTTER";
            case "RUINED_PORTAL" -> "CRYING_OBSIDIAN";
            case "NETHER_BRIDGE" -> "NETHER_BRICK";
            case "JUNGLE_TEMPLE" -> "MOSSY_COBBLESTONE";
            case "BASTION_OTHER" -> "BLACKSTONE";
            case "ANCIENT_CITY" -> "SCULK";
            case "IGLOO_CHEST" -> "ICE";
            case "TRIAL_CHAMBERS_REWARD" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_REWARD_COMMON" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_REWARD_RARE" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_REWARD_UNIQUE" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_REWARD_OMINOUS" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_REWARD_OMINOUS_COMMON" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_REWARD_OMINOUS_RARE" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_SUPPLY" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_CORRIDOR" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_INTERSECTION" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_INTERSECTION_BARREL" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_ENTRANCE" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_CORRIDOR_DISPENSER" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_CHAMBER_DISPENSER" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_WATER_DISPENSER" -> "TRIAL_KEY";
            case "TRIAL_CHAMBERS_CORRIDOR_POT" -> "TRIAL_KEY";
            case "EQUIPMENT_TRIAL_CHAMBER" -> "TRIAL_KEY";
            case "EQUIPMENT_TRIAL_CHAMBER_RANGED" -> "TRIAL_KEY";
            case "EQUIPMENT_TRIAL_CHAMBER_MELEE" -> "TRIAL_KEY";
            default -> null;
        };
    }
}
