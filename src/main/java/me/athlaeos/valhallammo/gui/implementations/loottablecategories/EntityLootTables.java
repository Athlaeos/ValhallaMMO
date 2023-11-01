package me.athlaeos.valhallammo.gui.implementations.loottablecategories;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.EntityClassification;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.LootTableEditor;
import me.athlaeos.valhallammo.gui.implementations.LootTableOverviewMenu;
import me.athlaeos.valhallammo.gui.implementations.LootTableSelectionMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

import static me.athlaeos.valhallammo.gui.implementations.LootTableOverviewMenu.KEY_TABLE;

public class EntityLootTables extends LootTableCategory{
    private static final Map<EntityType, Material> entityIconMapping = new HashMap<>();
    static {
        for (EntityType type : EntityType.values()){
            if (!EntityClassification.matchesClassification(type, EntityClassification.UNALIVE)) {
                entityIconMapping.put(type, ItemUtils.stringToMaterial(getEntityIcon(type.toString()), Material.STRUCTURE_VOID));
            }
        }
    }


    public EntityLootTables(int position) {
        super("loot_table_entities",
                new ItemBuilder(Material.PARROT_SPAWN_EGG).name("&eEntity Loot Tables").lore("&fLoot tables assigned to entities, ", "&fdropped when killed.").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF317\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_entitytables")));
    }

    @Override
    public List<ItemStack> getCategoryOptions() {
        List<ItemBuilder> buttons = new ArrayList<>();
        for (EntityType e : entityIconMapping.keySet()){
            LootTable typeTable = LootTableRegistry.getLootTable(e);
            ItemBuilder builder = new ItemBuilder(entityIconMapping.get(e))
                    .name((typeTable != null ? "&a" : "&c") + e)
                    .stringTag(KEY_TABLE, e.toString());
            if (typeTable == null) builder.lore("&cNo loot table set");
            else builder.lore("&aHas loot table: " + typeTable.getKey());
            builder.appendLore("&fClick to set new loot table", "&fShift-Click to remove loot table");
            buttons.add(builder);
        }
        buttons.sort(Comparator.comparing(b -> ItemUtils.getItemName(b.getMeta())));
        return buttons.stream().map(ItemBuilder::get).collect(Collectors.toList());
    }

    @Override
    public void onButtonClick(InventoryClickEvent e, String storedValue, Menu openedFrom) {
        if (StringUtils.isEmpty(storedValue)) return;
        EntityType en = Catch.catchOrElse(() -> EntityType.valueOf(storedValue), null);
        if (en == null) return;
        if (!e.isShiftClick()) new LootTableSelectionMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()), openedFrom, (LootTable table) -> {
            LootTableRegistry.getEntityLootTables().put(en, table.getKey());
            new LootTableOverviewMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) e.getWhoClicked()), LootTableOverviewMenu.ENTITIES.getId()).open();
        }).open();
        else LootTableRegistry.getEntityLootTables().remove(en);
    }

    private static String getEntityIcon(String type){
        return switch(type){
            case "BAT" -> "BAT_SPAWN_EGG";
            case "BEE" -> "HONEY_BOTTLE";
            case "CAT" -> "STRING";
            case "COD" -> "COD";
            case "COW" -> "BEEF";
            case "FOX" -> "SWEET_BERRIES";
            case "PIG" -> "PORKCHOP";
            case "VEX" -> "VEX_SPAWN_EGG";
            case "FROG" -> "OCHRE_FROGLIGHT";
            case "GOAT" -> "GOAT_HORN";
            case "HUSK" -> "SAND";
            case "MULE" -> "SADDLE";
            case "WOLF" -> "WOLF_SPAWN_EGG";
            case "ALLAY" -> "ALLAY_SPAWN_EGG";
            case "BLAZE" -> "BLAZE_ROD";
            case "CAMEL" -> "CAMEL_SPAWN_EGG";
            case "GHAST" -> "GHAST_TEAR";
            case "HORSE" -> "DIAMOND_HORSE_ARMOR";
            case "LLAMA" -> "LLAMA_SPAWN_EGG";
            case "PANDA" -> "BAMBOO";
            case "SHEEP" -> "WHITE_WOOL";
            case "SLIME" -> "SLIME_BALL";
            case "SQUID" -> "INK_SAC";
            case "STRAY" -> "TIPPED_ARROW";
            case "WITCH" -> "SPLASH_POTION";
            case "DONKEY" -> "CHEST";
            case "EVOKER" -> "TOTEM_OF_UNDYING";
            case "HOGLIN" -> "HOGLIN_SPAWN_EGG";
            case "OCELOT" -> "OCELOT_SPAWN_EGG";
            case "PARROT" -> "PARROT_SPAWN_EGG";
            case "PIGLIN" -> "GOLD_INGOT";
            case "PLAYER" -> "DIAMOND";
            case "RABBIT" -> "RABBIT_FOOT";
            case "SALMON" -> "SALMON";
            case "SPIDER" -> "SPIDER_EYE";
            case "TURTLE" -> "SCUTE";
            case "WARDEN" -> "SCULK_SHRIEKER";
            case "WITHER" -> "NETHER_STAR";
            case "ZOGLIN" -> "ZOGLIN_SPAWN_EGG";
            case "ZOMBIE" -> "ROTTEN_FLESH";
            case "AXOLOTL" -> "AXOLOTL_SPAWN_EGG";
            case "CHICKEN" -> "CHICKEN";
            case "CREEPER" -> "GUNPOWDER";
            case "DOLPHIN" -> "HEART_OF_THE_SEA";
            case "DROWNED" -> "TRIDENT";
            case "PHANTOM" -> "PHANTOM_MEMBRANE";
            case "RAVAGER" -> "RAVAGER_SPAWN_EGG";
            case "SHULKER" -> "SHULKER_SHELL";
            case "SNIFFER" -> "SNIFFER_EGG";
            case "SNOWMAN" -> "SNOWBALL";
            case "STRIDER" -> "STRIDER_SPAWN_EGG";
            case "TADPOLE" -> "FROGSPAWN";
            case "ENDERMAN" -> "ENDER_PEARL";
            case "GUARDIAN" -> "PRISMARINE_SHARD";
            case "PILLAGER" -> "CROSSBOW";
            case "SKELETON" -> "BONE";
            case "VILLAGER" -> "EMERALD";
            case "ENDERMITE" -> "ENDERMITE_SPAWN_EGG";
            case "GLOW_SQUID" -> "GLOW_INK_SAC";
            case "ILLUSIONER" -> "ENDER_EYE";
            case "IRON_GOLEM" -> "IRON_BLOCK";
            case "MAGMA_CUBE" -> "MAGMA_CREAM";
            case "POLAR_BEAR" -> "POLAR_BEAR_SPAWN_EGG";
            case "PUFFERFISH" -> "PUFFERFISH";
            case "VINDICATOR" -> "IRON_AXE";
            case "SILVERFISH" -> "STONE_BRICKS";
            case "CAVE_SPIDER" -> "COBWEB";
            case "ENDER_DRAGON" -> "DRAGON_EGG";
            case "MUSHROOM_COW" -> "RED_MUSHROOM";
            case "PIGLIN_BRUTE" -> "GOLDEN_AXE";
            case "ZOMBIE_HORSE" -> "ZOMBIE_HORSE_SPAWN_EGG";
            case "TROPICAL_FISH" -> "TROPICAL_FISH";
            case "ELDER_GUARDIAN" -> "PRISMARINE_CRYSTALS";
            case "WITHER_SKELETON" -> "WITHER_SKELETON_SKULL";
            case "TRADER_LLAMA" -> "TRADER_LLAMA_SPAWN_EGG";
            case "SKELETON_HORSE" -> "SKELETON_HORSE_SPAWN_EGG";
            case "ZOMBIE_VILLAGER" -> "ZOMBIE_VILLAGER_SPAWN_EGG";
            case "ZOMBIFIED_PIGLIN" -> "GOLD_NUGGET";
            case "WANDERING_TRADER" -> "WANDERING_TRADER_SPAWN_EGG";
            default -> "STRUCTURE_VOID";
        };
    }
}
