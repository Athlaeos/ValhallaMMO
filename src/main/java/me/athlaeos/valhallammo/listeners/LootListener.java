package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.BlockDestructionInfo;
import me.athlaeos.valhallammo.block.BlockExplodeBlockDestructionInfo;
import me.athlaeos.valhallammo.block.EntityExplodeBlockDestructionInfo;
import me.athlaeos.valhallammo.block.GenericBlockDestructionInfo;
import me.athlaeos.valhallammo.event.BlockDestructionEvent;
import me.athlaeos.valhallammo.event.ValhallaLootPopulateEvent;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.BlockStore;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTables;
import org.bukkit.loot.Lootable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.*;

public class LootListener implements Listener {

    private static final Map<Block, List<ItemStack>> preparedBlockDrops = new HashMap<>();
    private static final Map<Block, UUID> transferToInventory = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlayerBreak(BlockBreakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled() || e.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        AttributeInstance luckInstance = e.getPlayer().getAttribute(Attribute.GENERIC_LUCK);
        double luck = luckInstance == null ? 0 : luckInstance.getValue();
        luck += AccumulativeStatManager.getCachedStats("MINING_LUCK", e.getPlayer(), 10000, true);
        int fortune = 0;
        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        if (!ItemUtils.isEmpty(hand)){
            if (hand.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) fortune = hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
            else if (hand.containsEnchantment(Enchantment.SILK_TOUCH)) fortune = -1;
        }

        if (onBlockDestruction(e.getBlock(),
                new LootContext.Builder(e.getBlock().getLocation()).lootedEntity(e.getPlayer()).killer(null).lootingModifier(fortune).luck((float) luck).build(),
                new GenericBlockDestructionInfo(e.getBlock(), e),
                BlockDestructionEvent.BlockDestructionReason.PLAYER
        )){
            e.setDropItems(false);
        } else {
            for (ItemStack i : preparedBlockDrops.getOrDefault(e.getBlock(), new ArrayList<>())){
                if (transferToInventory.containsKey(e.getBlock()) && transferToInventory.get(e.getBlock()).equals(e.getPlayer().getUniqueId())) ItemUtils.addItem(e.getPlayer(), i, false);
                else e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), i);
            }
        }
        clear(e.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBurnBreak(BlockBurnEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled()) return;
        if (onBlockDestruction(e.getBlock(),
                new LootContext.Builder(e.getBlock().getLocation()).lootedEntity(null).killer(null).lootingModifier(0).luck(0).build(),
                new GenericBlockDestructionInfo(e.getBlock(), e),
                BlockDestructionEvent.BlockDestructionReason.BURN
        )){
            e.getBlock().setType(Material.AIR);
            // block burn events aren't gonna be dropping any items, so the event doesn't need to be cancelled and manually executed
        } else {
            for (ItemStack i : preparedBlockDrops.getOrDefault(e.getBlock(), new ArrayList<>())){
                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), i);
            }
        }
        clear(e.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockFadeBreak(BlockFadeEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled()) return;
        if (onBlockDestruction(e.getBlock(),
                new LootContext.Builder(e.getBlock().getLocation()).lootedEntity(null).killer(null).lootingModifier(0).luck(0).build(),
                new GenericBlockDestructionInfo(e.getBlock(), e),
                BlockDestructionEvent.BlockDestructionReason.FADE
        )){
            e.setCancelled(true);
            e.getBlock().setType(e.getNewState().getType());
        } else {
            for (ItemStack i : preparedBlockDrops.getOrDefault(e.getBlock(), new ArrayList<>())){
                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), i);
            }
        }
        clear(e.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDecay(LeavesDecayEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled()) return;
        if (onBlockDestruction(e.getBlock(),
                new LootContext.Builder(e.getBlock().getLocation()).lootedEntity(null).killer(null).lootingModifier(0).luck(0).build(),
                new GenericBlockDestructionInfo(e.getBlock(), e),
                BlockDestructionEvent.BlockDestructionReason.DECAY
        )){
            e.setCancelled(true);
            e.getBlock().setType(Material.AIR);
        } else {
            for (ItemStack i : preparedBlockDrops.getOrDefault(e.getBlock(), new ArrayList<>())){
                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), i);
            }
        }
        clear(e.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockReplaceBreak(BlockFromToEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled()) return;
        if (!e.getBlock().getType().isAir() && e.getBlock().getType() != e.getToBlock().getType())
            if (!onBlockDestruction(e.getBlock(),
                    new LootContext.Builder(e.getBlock().getLocation()).lootedEntity(null).killer(null).lootingModifier(0).luck(0).build(),
                    new GenericBlockDestructionInfo(e.getBlock(), e),
                    BlockDestructionEvent.BlockDestructionReason.REPLACED
            )){
                for (ItemStack i : preparedBlockDrops.getOrDefault(e.getBlock(), new ArrayList<>())){
                    e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), i);
                }
            }
        clear(e.getBlock());
    }

    private static final Map<Block, Material> explodedBlocks = new HashMap<>();

    /**
     * Marks a block as having been exploded. When a block is marked as such, regardless of if it's now air or not,
     * it will be treated as if it was its previous material and trigger loot tables and scheduled drops for it.
     * @param b the block to mark as being exploded. It's important this block is still its original material pre-explosion or this method will do nothing
     */
    public static void markExploded(Block b){
        explodedBlocks.put(b, b.getType());
    }

    public static void setFortuneLevel(Entity e, int level){
        e.setMetadata("valhalla_explosive_fortune", new FixedMetadataValue(ValhallaMMO.getInstance(), level));
    }

    public static int getFortuneLevel(Entity e){
        if (!e.hasMetadata("valhalla_explosive_fortune")) return 0;
        Optional<MetadataValue> value = e.getMetadata("valhalla_explosive_fortune").stream().findAny();
        return value.map(MetadataValue::asInt).orElse(0);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBlockExplodeBreak(BlockExplodeEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled()) return;

        List<Block> blocks = new ArrayList<>(e.blockList());
        blocks.addAll(explodedBlocks.keySet());
        for (Block b : blocks){
            if (onBlockDestruction(b,
                    new LootContext.Builder(b.getLocation()).lootedEntity(null).killer(null).lootingModifier(0).luck(0).build(),
                    new BlockExplodeBlockDestructionInfo(b, e),
                    BlockDestructionEvent.BlockDestructionReason.BLOCK_EXPLOSION
            )) {
                e.blockList().remove(b);
                b.setType(Material.AIR);
            } else {
                for (ItemStack i : preparedBlockDrops.getOrDefault(b, new ArrayList<>())){
                    b.getWorld().dropItemNaturally(b.getLocation(), i);
                }
            }
            clear(b);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockEntityExplodeBreak(EntityExplodeEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        Optional<MetadataValue> uuidMeta = e.getEntity().getMetadata("valhalla_entity_owner").stream().findAny();
        UUID uuid = uuidMeta.map(metadataValue -> UUID.fromString(metadataValue.asString())).orElse(null);
        Entity owner = uuid == null ? null : ValhallaMMO.getInstance().getServer().getEntity(uuid);

        AttributeInstance luckInstance = owner instanceof LivingEntity l ? l.getAttribute(Attribute.GENERIC_LUCK) : null;
        double luck = luckInstance == null ? 0 : luckInstance.getValue();
        if (owner != null) luck += AccumulativeStatManager.getCachedStats("BLASTING_LUCK", owner, 10000, true);

        List<Block> blocks = new ArrayList<>(e.blockList());
        blocks.addAll(explodedBlocks.keySet());
        for (Block b : blocks){
            if (onBlockDestruction(b,
                    new LootContext.Builder(b.getLocation()).lootedEntity(owner != null ? owner : e.getEntity()).killer(null).lootingModifier(getFortuneLevel(e.getEntity())).luck((float) luck).build(),
                    new EntityExplodeBlockDestructionInfo(b, e),
                    BlockDestructionEvent.BlockDestructionReason.ENTITY_EXPLOSION
            )) {
                e.blockList().remove(b);
                b.setType(Material.AIR);
            } else {
                for (ItemStack i : preparedBlockDrops.getOrDefault(b, new ArrayList<>())){
                    if (owner instanceof Player p && transferToInventory.containsKey(b) && transferToInventory.get(b).equals(p.getUniqueId())) ItemUtils.addItem(p, i, false);
                    else b.getWorld().dropItemNaturally(b.getLocation(), i);
                }
            }
            clear(b);
        }
    }

    public static void setEntityOwner(Entity entity, Entity owner){
        entity.setMetadata("valhalla_entity_owner", new FixedMetadataValue(ValhallaMMO.getInstance(), owner.getUniqueId().toString()));
    }

    private boolean onBlockDestruction(Block b, LootContext context, BlockDestructionInfo info, BlockDestructionEvent.BlockDestructionReason reason){
        BlockDestructionEvent destroyEvent = new BlockDestructionEvent(b, info, reason);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(destroyEvent);
        Material originalMaterial = explodedBlocks.getOrDefault(b, b.getType());
        explodedBlocks.remove(b);
        if (!destroyEvent.getInfo().isCancelled(info.getEvent())){
            LootTable table = LootTableRegistry.getLootTable(b, originalMaterial);
            if (table == null || BlockStore.isPlaced(b)) return false;
            LootTableRegistry.setLootTable(b, null);
            List<ItemStack> generatedLoot = LootTableRegistry.getLoot(table, context, LootTable.LootType.BREAK);
            ValhallaLootPopulateEvent loottableEvent = new ValhallaLootPopulateEvent(table, context, generatedLoot);
            ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(loottableEvent);
            if (!loottableEvent.isCancelled()){
                prepareBlockDrops(b, loottableEvent.getDrops());
                return switch (loottableEvent.getPreservationType()){
                    case CLEAR -> true;
                    case KEEP -> false;
                    case CLEAR_UNLESS_EMPTY -> !loottableEvent.getDrops().isEmpty();
                };
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onContainerPlace(BlockPlaceEvent e){
        if (e.isCancelled() || ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName())) return;
        ItemStack placed = e.getItemInHand();
        ItemBuilder item = new ItemBuilder(placed);
        LootTable table = LootTableRegistry.getLootTable(item.getMeta());
        if (table == null) return;
        LootTableRegistry.setLootTable(e.getBlock(), table);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChestOpen(PlayerInteractEvent e){
        if (e.getClickedBlock() == null || ValhallaMMO.isWorldBlacklisted(e.getClickedBlock().getWorld().getName()) ||
                e.useInteractedBlock() == Event.Result.DENY || e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() == EquipmentSlot.OFF_HAND) return;
        Block b = e.getClickedBlock();
        if (!(b.getState() instanceof Lootable l) || !(b.getState() instanceof Container c)) return;
        LootTable table = LootTableRegistry.getLootTable(b, b.getType());
        if (table == null && l.getLootTable() != null) table = LootTableRegistry.getLootTable(l.getLootTable().getKey());
        if (table == null) return;
        LootTableRegistry.setLootTable(b, null);
        AttributeInstance luckInstance = e.getPlayer().getAttribute(Attribute.GENERIC_LUCK);
        double luck = luckInstance == null ? 0 : luckInstance.getValue();
        LootContext context = new LootContext.Builder(b.getLocation()).killer(null).lootedEntity(e.getPlayer()).lootingModifier(0).luck((float) luck).build();
        List<ItemStack> loot = LootTableRegistry.getLoot(table, context, LootTable.LootType.CONTAINER);
        ValhallaLootPopulateEvent loottableEvent = new ValhallaLootPopulateEvent(table, context, loot);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(loottableEvent);
        if (!loottableEvent.isCancelled()){
            switch (loottableEvent.getPreservationType()){
                case CLEAR -> l.setLootTable(null);
                case CLEAR_UNLESS_EMPTY -> {
                    if (!loottableEvent.getDrops().isEmpty()) l.setLootTable(null);
                }
                case KEEP -> {
                    if (loottableEvent.getDrops().isEmpty()) return;
                }
            }
            List<ItemStack> drops = new ArrayList<>(loottableEvent.getDrops());
            for (int i = 0; i < c.getInventory().getSize() - drops.size(); i++) drops.add(null);
            Collections.shuffle(drops);
            for (int i = 0; i < drops.size(); i++) {
                ItemStack drop = drops.get(i);
                if (ItemUtils.isEmpty(drop)) continue;
                if (i > c.getInventory().getSize() - 1) break;
                c.getInventory().setItem(i, drop);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemClick(PlayerInteractEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || !Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "attempts_loottable_item") ||
                e.useItemInHand() == Event.Result.DENY || e.getHand() == EquipmentSlot.OFF_HAND || !(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        ItemStack clickedItem = e.getPlayer().getInventory().getItemInMainHand();
        if (ItemUtils.isEmpty(clickedItem) || clickedItem.getType().isBlock()) return;
        ItemBuilder item = new ItemBuilder(clickedItem);
        LootTable table = LootTableRegistry.getLootTable(item.getMeta());
        if (table == null) return;
        Timer.setCooldown(e.getPlayer().getUniqueId(), 500, "attempts_loottable_item");
        e.setCancelled(true);

        AttributeInstance luckInstance = e.getPlayer().getAttribute(Attribute.GENERIC_LUCK);
        double luck = luckInstance == null ? 0 : luckInstance.getValue();

        LootContext context = new LootContext.Builder(e.getPlayer().getLocation()).killer(null).lootedEntity(e.getPlayer()).lootingModifier(0).luck((float) luck).build();
        List<ItemStack> loot = LootTableRegistry.getLoot(table, context, LootTable.LootType.CONTAINER);
        ValhallaLootPopulateEvent loottableEvent = new ValhallaLootPopulateEvent(table, context, loot);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(loottableEvent);
        if (!loottableEvent.isCancelled()){
            loottableEvent.getDrops().forEach(i -> ItemUtils.addItem(e.getPlayer(), i, true));

            if (clickedItem.getAmount() <= 1) e.getPlayer().getInventory().setItemInMainHand(null);
            else clickedItem.setAmount(clickedItem.getAmount() - 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDrops(EntityDeathEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName())) return;
        LivingEntity entity = e.getEntity();
        Player killer = entity.getKiller();
        double dropMultiplier = killer == null ? 0 : AccumulativeStatManager.getCachedStats("ENTITY_DROPS", killer, 10000, true);
        List<ItemStack> newDrops = ItemUtils.multiplyItems(e.getDrops(), 1 + dropMultiplier, false, null);
        e.getDrops().clear();
        e.getDrops().addAll(newDrops);

        LootTable table = LootTableRegistry.getLootTable(entity);
        if (table == null) return;
        int looting = 0;
        double luck = 0;
        if (killer != null){
            AttributeInstance luckInstance = entity.getKiller() == null ? null : entity.getKiller().getAttribute(Attribute.GENERIC_LUCK);
            luck = luckInstance == null ? 0 : luckInstance.getValue();
            luck += AccumulativeStatManager.getCachedStats("ENTITY_DROP_LUCK", killer, 10000, true);

            looting = ItemUtils.isEmpty(killer.getInventory().getItemInMainHand()) ? 0 :
                    killer.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        }

        LootContext context = new LootContext.Builder(entity.getLocation()).killer(killer).lootedEntity(entity).lootingModifier(looting).luck((float) luck).build();
        List<ItemStack> loot = LootTableRegistry.getLoot(table, context, LootTable.LootType.KILL);
        ValhallaLootPopulateEvent loottableEvent = new ValhallaLootPopulateEvent(table, context, loot);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(loottableEvent);
        if (!loottableEvent.isCancelled()){
            switch (loottableEvent.getPreservationType()){
                case CLEAR -> e.getDrops().clear();
                case CLEAR_UNLESS_EMPTY -> {
                    if (!loottableEvent.getDrops().isEmpty()) e.getDrops().clear();
                }
                case KEEP -> {
                    if (loottableEvent.getDrops().isEmpty()) return;
                }
            }
            e.getDrops().addAll(loottableEvent.getDrops());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDrops(BlockDropItemEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled() || !transferToInventory.containsKey(e.getBlock())){
            clear(e.getBlock());
            return;
        }
        if (transferToInventory.containsKey(e.getBlock()) && transferToInventory.get(e.getBlock()).equals(e.getPlayer().getUniqueId())){
            for (Item i : e.getItems()) ItemUtils.addItem(e.getPlayer(), i.getItemStack(), false);
            e.getItems().clear();
        }
        clear(e.getBlock());
    }

    private void clear(Block b){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            transferToInventory.remove(b);
            preparedBlockDrops.remove(b);
            explodedBlocks.remove(b);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFish(PlayerFishEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.isCancelled() ||
                e.getState() != PlayerFishEvent.State.CAUGHT_FISH || !(e.getCaught() instanceof Item i)) return;
        LootTable table = LootTableRegistry.getFishingLootTable();
        if (table == null) return;

        double fishingLuck = AccumulativeStatManager.getCachedStats("FISHING_LUCK", e.getPlayer(), 10000, true);
        LootContext context = new LootContext.Builder(e.getPlayer().getLocation()).killer(null).lootedEntity(e.getPlayer()).lootingModifier(0).luck((float) fishingLuck).build();

        List<ItemStack> loot = LootTableRegistry.getLoot(table, context, LootTable.LootType.FISH);
        ValhallaLootPopulateEvent loottableEvent = new ValhallaLootPopulateEvent(table, context, loot);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(loottableEvent);
        if (!loottableEvent.isCancelled()){
            switch (loottableEvent.getPreservationType()){
                case CLEAR -> {
                    if (loottableEvent.getDrops().isEmpty()) e.getHook().setHookedEntity(null);
                    else {
                        ItemStack itemToSet = loottableEvent.getDrops().get(0);
                        loottableEvent.getDrops().remove(0);
                        i.setItemStack(itemToSet);
                    }
                }
                case CLEAR_UNLESS_EMPTY -> {
                    if (!loottableEvent.getDrops().isEmpty()) {
                        ItemStack itemToSet = loottableEvent.getDrops().get(0);
                        loottableEvent.getDrops().remove(itemToSet);
                        i.setItemStack(itemToSet);
                    }
                }
                case KEEP -> {
                    if (loottableEvent.getDrops().isEmpty()) return;
                }
            }
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> loottableEvent.getDrops().forEach(d -> {
                Item item = e.getHook().getWorld().dropItem(e.getHook().getLocation(), d);
                item.setVelocity(i.getVelocity());
            }), 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPiglinBarter(PiglinBarterEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        LootTable table = LootTableRegistry.getLootTable(LootTables.PIGLIN_BARTERING);
        if (table == null) return;

        LootContext context = new LootContext.Builder(e.getEntity().getLocation()).killer(null).lootedEntity(null).lootingModifier(0).luck(0).build();

        List<ItemStack> loot = LootTableRegistry.getLoot(table, context, LootTable.LootType.PIGLIN_BARTER);
        ValhallaLootPopulateEvent loottableEvent = new ValhallaLootPopulateEvent(table, context, loot);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(loottableEvent);
        if (!loottableEvent.isCancelled()){
            switch (loottableEvent.getPreservationType()){
                case CLEAR -> e.getOutcome().clear();
                case CLEAR_UNLESS_EMPTY -> {
                    if (!loottableEvent.getDrops().isEmpty()) e.getOutcome().clear();
                }
                case KEEP -> {
                    if (loottableEvent.getDrops().isEmpty()) return;
                }
            }

            e.getOutcome().addAll(loottableEvent.getDrops());
        }
    }

    /**
     * Returns the extra drops that have been prepared for the given block, to provide some way for other plugins
     * to see what a block will drop in case that's needed. This is necessary because spigot does not
     * allow items to be added to a block's drops directly, and so they need to be prepared instead.
     * @param b the block to check its prepared drops for
     * @return the list of extra items that will drop from the block, or an empty list if none.
     */
    public static List<ItemStack> getPreparedExtraDrops(Block b){
        return preparedBlockDrops.getOrDefault(b, new ArrayList<>());
    }

    public static void setInstantPickup(Block b, Player who){
        transferToInventory.put(b, who.getUniqueId());
    }

    public static void prepareBlockDrops(Block b, ItemStack... items){
        prepareBlockDrops(b, List.of(items));
    }

    public static void prepareBlockDrops(Block b, List<ItemStack> items){
        List<ItemStack> preparedDrops = preparedBlockDrops.getOrDefault(b, new ArrayList<>());
        preparedDrops.addAll(items);
        preparedBlockDrops.put(b, preparedDrops);
    }
}
