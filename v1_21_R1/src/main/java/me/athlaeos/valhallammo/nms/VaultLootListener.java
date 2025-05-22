package me.athlaeos.valhallammo.nms;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.event.ValhallaLootPopulateEvent;
import me.athlaeos.valhallammo.event.ValhallaLootReplacementEvent;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.loot.ReplacementTable;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.data.type.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseLootEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTables;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VaultLootListener implements Listener {
    private static final Map<Material, Collection<LootTables>> vaultMaterialToLikelyTableMappings = new HashMap<>();
    private static final Map<Material, Collection<LootTables>> vaultMaterialToLikelyOminousTableMappings = new HashMap<>();
    private static final Map<Material, Collection<LootTables>> spawnerMaterialToLikelyTableMappings = new HashMap<>();
    private static final Map<Material, Collection<LootTables>> spawnerMaterialToLikelyOminousTableMappings = new HashMap<>();

    static {
        map(false, true, "EMERALD", "TRIAL_CHAMBERS_REWARD_COMMON", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "ARROW", "TRIAL_CHAMBERS_REWARD_COMMON", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "TIPPED_ARROW", "TRIAL_CHAMBERS_REWARD_COMMON", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "IRON_INGOT", "TRIAL_CHAMBERS_REWARD_COMMON", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "WIND_CHARGE", "TRIAL_CHAMBERS_REWARD_COMMON", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "HONEY_BOTTLE", "TRIAL_CHAMBERS_REWARD_COMMON", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "OMINOUS_BOTTLE", "TRIAL_CHAMBERS_REWARD_COMMON", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "SHIELD", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "BOW", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "WIND_CHARGE", "TRIAL_CHAMBERS_REWARD_COMMON", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "DIAMOND", "TRIAL_CHAMBERS_REWARD_COMMON", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "GOLDEN_APPLE", "TRIAL_CHAMBERS_REWARD_UNIQUE");
        map(false, true, "ENCHANTED_BOOK", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "CROSSBOW", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "IRON_AXE", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "IRON_CHESTPLATE", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "DIAMOND_AXE", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "DIAMOND_CHESTPLATE", "TRIAL_CHAMBERS_REWARD_RARE");
        map(false, true, "BOLT_ARMOR_TRIM_SMITHING_TEMPLATE", "TRIAL_CHAMBERS_REWARD_UNIQUE");
        map(false, true, "MUSIC_DISC", "TRIAL_CHAMBERS_REWARD_UNIQUE");
        map(false, true, "MUSIC_DISC_PRECIPICE", "TRIAL_CHAMBERS_REWARD_UNIQUE");
        map(false, true, "GUSTER_BANNER_PATTERN", "TRIAL_CHAMBERS_REWARD_UNIQUE");
        map(false, true, "BANNER_PATTERN", "TRIAL_CHAMBERS_REWARD_UNIQUE");
        map(false, true, "TRIDENT", "TRIAL_CHAMBERS_REWARD_UNIQUE");

        map(true, true, "EMERALD", "TRIAL_CHAMBERS_REWARD_OMINOUS_COMMON", "TRIAL_CHAMBERS_REWARD_OMINOUS_RARE");
        map(true, true, "WIND_CHARGE", "TRIAL_CHAMBERS_REWARD_OMINOUS_COMMON", "TRIAL_CHAMBERS_REWARD_OMINOUS_RARE");
        map(true, true, "TIPPED_ARROW", "TRIAL_CHAMBERS_REWARD_OMINOUS_COMMON", "TRIAL_CHAMBERS_REWARD_OMINOUS_RARE");
        map(true, true, "DIAMOND", "TRIAL_CHAMBERS_REWARD_OMINOUS_COMMON", "TRIAL_CHAMBERS_REWARD_OMINOUS_RARE");
        map(true, true, "FLOW_ARMOR_TRIM_SMITHING_TEMPLATE", "TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE");
        map(true, true, "ENCHANTED_GOLDEN_APPLE", "TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE");
        map(true, true, "FLOW_BANNER_PATTERN", "TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE");
        map(true, true, "BANNER_PATTERN", "TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE");
        map(true, true, "OMINOUS_BOTTLE", "TRIAL_CHAMBERS_REWARD_OMINOUS_COMMON", "TRIAL_CHAMBERS_REWARD_OMINOUS_RARE");
        map(true, true, "EMERALD_BLOCK", "TRIAL_CHAMBERS_REWARD_OMINOUS_RARE");
        map(true, true, "CROSSBOW", "TRIAL_CHAMBERS_REWARD_OMINOUS_RARE");
        map(true, true, "IRON_BLOCK", "TRIAL_CHAMBERS_REWARD_OMINOUS_RARE");
        map(true, true, "GOLDEN_APPLE", "TRIAL_CHAMBERS_REWARD_OMINOUS_RARE");
        map(true, true, "DIAMOND_AXE", "TRIAL_CHAMBERS_REWARD_OMINOUS_RARE");
        map(true, true, "DIAMOND_CHESTPLATE", "TRIAL_CHAMBERS_REWARD_OMINOUS_RARE");
        map(true, true, "MUSIC_DISC", "TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE");
        map(true, true, "MUSIC_DISC_CREATOR", "TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE");
        map(true, true, "HEAVY_CORE", "TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE");
        map(true, true, "ENCHANTED_BOOK", "TRIAL_CHAMBERS_REWARD_OMINOUS_RARE");
        map(true, true, "DIAMOND_BLOCK", "TRIAL_CHAMBERS_REWARD_OMINOUS_RARE");

        map(false, false, "TRIAL_KEY", "TRIAL_CHAMBER_KEY");
        map(false, false, "BREAD", "TRIAL_CHAMBER_CONSUMABLES");
        map(false, false, "COOKED_CHICKEN", "TRIAL_CHAMBER_CONSUMABLES");
        map(false, false, "BAKED_POTATO", "TRIAL_CHAMBER_CONSUMABLES");
        map(false, false, "POTION", "TRIAL_CHAMBER_CONSUMABLES");

        map(true, false, "OMINOUS_TRIAL_KEY", "OMINOUS_TRIAL_CHAMBER_KEY");
        map(true, false, "BAKED_POTATO", "OMINOUS_TRIAL_CHAMBER_CONSUMABLES");
        map(true, false, "COOKED_BEEF", "OMINOUS_TRIAL_CHAMBER_CONSUMABLES");
        map(true, false, "GOLDEN_CARROT", "OMINOUS_TRIAL_CHAMBER_CONSUMABLES");
        map(true, false, "POTION", "OMINOUS_TRIAL_CHAMBER_CONSUMABLES");
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVaultDispenseItem(BlockDispenseLootEvent e){
        BlockData data = e.getBlock().getBlockData();
        if (!(data instanceof Vault ||
                data instanceof TrialSpawner)) return;
        boolean isOminous = data instanceof Vault v ? v.isOminous() :
                ((TrialSpawner) data).isOminous();
        boolean isVault = data instanceof Vault;
        LikelyTables likelyTables = getLikelyLootTables(e.getDispensedLoot(), isOminous, isVault);

        List<DropDetails> details = new ArrayList<>();

        Player player = e.getPlayer();
        AttributeInstance luckAttribute = player == null ? null : player.getAttribute(Attribute.GENERIC_LUCK);
        float luck = (float) (luckAttribute == null ? 0 : luckAttribute.getValue());
        LootContext context = new LootContext.Builder(e.getBlock().getLocation()).luck(luck).killer(player).lootedEntity(player).build();
        boolean clearVanilla = false;

        for (LootTables table : likelyTables.likelyTables){
            LootTable valhallaTable = LootTableRegistry.getLootTable(table);
            // Add vanilla loot if no valhalla table is present and execute replacement table on the vanilla drop

            if (valhallaTable != null){
                List<ItemStack> loot = LootTableRegistry.getLoot(valhallaTable, context, LootTable.LootType.VAULT);

                ValhallaLootPopulateEvent loottableEvent = new ValhallaLootPopulateEvent(valhallaTable, context, loot);
                ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(loottableEvent);
                if (!loottableEvent.isCancelled()) {
                    boolean clear = switch (loottableEvent.getPreservationType()){
                        case CLEAR -> true;
                        case CLEAR_UNLESS_EMPTY -> !loottableEvent.getDrops().isEmpty();
                        case KEEP -> false;
                    };
                    if (clear) clearVanilla = true; // The valhalla loot wants the vanilla loot to be removed, so we're skipping all the vanilla drops

                    details.addAll(loottableEvent.getDrops().stream().map(i -> new DropDetails(i, true, table)).toList());
                }
            } else {
                List<ItemStack> vanillaLoot = new ArrayList<>(table.getLootTable().populateLoot(Utils.getRandom(), context));
                ItemStack vanillaDrop = vanillaLoot.isEmpty() ? null : vanillaLoot.get(0);
                if (!ItemUtils.isEmpty(vanillaDrop)) {
                    details.add(new DropDetails(vanillaDrop, false, table));
                }
            }
            if (clearVanilla) details.removeIf(d -> !d.isValhalla);

            List<ItemStack> newItems = new ArrayList<>(likelyTables.unmatchedItems);

            for (DropDetails d : details){
                if (clearVanilla && !d.isValhalla) continue;
                ReplacementTable replacementTable = LootTableRegistry.getReplacementTable(d.table);
                ReplacementTable globalTable = LootTableRegistry.getGlobalReplacementTable();
                ValhallaLootReplacementEvent event = new ValhallaLootReplacementEvent(replacementTable, context);
                if (replacementTable != null) ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
                if (replacementTable == null || !event.isCancelled()){
                    ItemStack item = d.drop;
                    if (ItemUtils.isEmpty(item)) continue;
                    ItemStack replacement = LootTableRegistry.getReplacement(replacementTable, context, LootTable.LootType.VAULT, item);
                    if (!ItemUtils.isEmpty(replacement)) item = replacement;
                    ItemStack globalReplacement = LootTableRegistry.getReplacement(globalTable, context, LootTable.LootType.VAULT, item);
                    if (!ItemUtils.isEmpty(globalReplacement)) item = globalReplacement;
                    if (!ItemUtils.isEmpty(item)) newItems.add(item);
                }
            }

            e.setDispensedLoot(newItems);
        }
    }

    private static LikelyTables getLikelyLootTables(List<ItemStack> drops, boolean isOminous, boolean isVault){
        List<LootTables> likelyTables = new ArrayList<>();
        List<ItemStack> unmatchedItems = new ArrayList<>();
        Map<Material, Collection<LootTables>> map = isOminous ?
                (isVault ? vaultMaterialToLikelyOminousTableMappings : spawnerMaterialToLikelyOminousTableMappings) :
                (isVault ? vaultMaterialToLikelyTableMappings : spawnerMaterialToLikelyTableMappings);
        int rareCount = 0;
        int commonCount = 0;
        int uniqueCount = 0;
        // counting up potential common and rare drops, prioritizing common first
        for (ItemStack i : drops){
            if (isVault && map.getOrDefault(i.getType(), new HashSet<>()).stream().anyMatch(l -> l.getLootTable().toString().contains("COMMON"))){
                commonCount++;
            } else if (isVault && map.getOrDefault(i.getType(), new HashSet<>()).stream().anyMatch(l -> l.getLootTable().toString().contains("RARE"))){
                rareCount++;
            } else if (isVault && map.getOrDefault(i.getType(), new HashSet<>()).stream().anyMatch(l -> l.getLootTable().toString().contains("UNIQUE"))){
                uniqueCount++;
            } else {
                List<LootTables> possibilities = new ArrayList<>(map.getOrDefault(i.getType(), new HashSet<>()));
                if (possibilities.isEmpty()) unmatchedItems.add(i);
                else {
                    ItemMeta meta = i.getItemMeta();
                    if (meta != null && (meta.hasDisplayName() || meta.hasLore() || meta.hasCustomModelData())) unmatchedItems.add(i);
                    else likelyTables.add(possibilities.get(0));
                }
            }
        }
        if (isVault){
            // if there are at least 3 common drops and no rare drops, reduce common drops and increase rare drops
            if (rareCount == 0 && commonCount >= 3) {
                rareCount++;
                commonCount--;
            }
            Utils.repeat(rareCount, i -> likelyTables.add(isOminous ? LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_RARE : LootTables.TRIAL_CHAMBERS_REWARD_RARE));
            Utils.repeat(commonCount, i -> likelyTables.add(isOminous ? LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_COMMON : LootTables.TRIAL_CHAMBERS_REWARD_COMMON));
            Utils.repeat(uniqueCount, i -> likelyTables.add(isOminous ? LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE : LootTables.TRIAL_CHAMBERS_REWARD_UNIQUE));
        }
        return new LikelyTables(likelyTables, unmatchedItems);
    }

    private static void map(boolean ominous, boolean vault, String material, String... likelyLootTables){
        Material mat = Catch.catchOrElse(() -> Material.valueOf(material), null);
        Collection<LootTables> tables = Stream.of(likelyLootTables).map(s -> Catch.catchOrElse(() -> LootTables.valueOf(s), null)).filter(Objects::nonNull).collect(Collectors.toSet());
        if (mat == null || tables.isEmpty()) return;
        if (ominous) (vault ? vaultMaterialToLikelyOminousTableMappings : spawnerMaterialToLikelyOminousTableMappings).put(mat, tables);
        else (vault ? vaultMaterialToLikelyTableMappings : spawnerMaterialToLikelyTableMappings).put(mat, tables);
    }

    private record LikelyTables(List<LootTables> likelyTables, List<ItemStack> unmatchedItems){}

    private record DropDetails(ItemStack drop, boolean isValhalla, LootTables table){}
}
