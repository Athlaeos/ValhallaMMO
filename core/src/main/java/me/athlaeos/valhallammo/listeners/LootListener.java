package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.BlockDestructionInfo;
import me.athlaeos.valhallammo.block.BlockExplodeBlockDestructionInfo;
import me.athlaeos.valhallammo.block.EntityExplodeBlockDestructionInfo;
import me.athlaeos.valhallammo.block.GenericBlockDestructionInfo;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.entities.EntityClassification;
import me.athlaeos.valhallammo.event.BlockDestructionEvent;
import me.athlaeos.valhallammo.event.ValhallaLootPopulateEvent;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.LootFreeSelectionMenu;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootEntry;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.ArchaeologyListener;
import me.athlaeos.valhallammo.version.PaperLootRefillHandler;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTables;
import org.bukkit.loot.Lootable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class LootListener implements Listener {

    private static final Map<Location, List<ItemStack>> preparedBlockDrops = new HashMap<>();
    private static final Map<Location, Double> preparedLuckBuffs = new HashMap<>();
    private static final Map<Location, UUID> transferToInventory = new HashMap<>();
    private static final Map<UUID, Double> preparedFishingLuckBuffs = new HashMap<>();
    private static final Map<Location, UUID> blockBreakerMap = new HashMap<>();

    public LootListener(){
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20)) ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new ArchaeologyListener(), ValhallaMMO.getInstance());
    }

    public static void setResponsibleBreaker(Block block, Player breaker){
        blockBreakerMap.put(block.getLocation(), breaker.getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlayerBreak(BlockBreakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled() || e.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        Player p = e.getPlayer();
        Pair<Double, Integer> details = getFortuneAndLuck(p, e.getBlock());
        int fortune = details.getTwo();
        double luck = details.getOne();

        if (onBlockDestruction(e.getBlock(),
                new LootContext.Builder(e.getBlock().getLocation()).lootedEntity(p).killer(null).lootingModifier(fortune).luck((float) luck).build(),
                new GenericBlockDestructionInfo(e.getBlock(), e),
                BlockDestructionEvent.BlockDestructionReason.PLAYER_BREAK
        )) e.setDropItems(false);

        clear(e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlayerBreakFinal(BlockBreakEvent e){
        if (e.isCancelled() || ArchaeologyListener.isBrushable(e.getBlock().getType())){
            preparedBlockDrops.remove(e.getBlock().getLocation());
            return;
        }
        for (ItemStack i : preparedBlockDrops.getOrDefault(e.getBlock().getLocation(), new ArrayList<>())){
            if (transferToInventory.containsKey(e.getBlock().getLocation()) && transferToInventory.get(e.getBlock().getLocation()).equals(e.getPlayer().getUniqueId())) ItemUtils.addItem(e.getPlayer(), i, false);
            else e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), i);
        }
        preparedBlockDrops.remove(e.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockHarvest(PlayerHarvestBlockEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getHarvestedBlock().getWorld().getName()) || e.isCancelled()) return;

        Player p = e.getPlayer();
        Pair<Double, Integer> details = getFortuneAndLuck(p, e.getHarvestedBlock());
        int fortune = details.getTwo();
        double luck = details.getOne();

        if (onBlockDestruction(e.getHarvestedBlock(),
                new LootContext.Builder(e.getHarvestedBlock().getLocation()).lootedEntity(p).killer(null).lootingModifier(fortune).luck((float) luck).build(),
                new GenericBlockDestructionInfo(e.getHarvestedBlock(), e),
                BlockDestructionEvent.BlockDestructionReason.PLAYER_HARVEST
        )) e.getItemsHarvested().clear();

        clear(e.getHarvestedBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockHarvestFinal(PlayerHarvestBlockEvent e){
        if (e.isCancelled() || ArchaeologyListener.isBrushable(e.getHarvestedBlock().getType())){
            preparedBlockDrops.remove(e.getHarvestedBlock().getLocation());
            return;
        }
        List<ItemStack> drops = preparedBlockDrops.getOrDefault(e.getHarvestedBlock().getLocation(), new ArrayList<>());
        drops.addAll(e.getItemsHarvested());
        e.getItemsHarvested().clear();
        for (ItemStack i : drops){
            if (transferToInventory.containsKey(e.getHarvestedBlock().getLocation()) && transferToInventory.get(e.getHarvestedBlock().getLocation()).equals(e.getPlayer().getUniqueId())) ItemUtils.addItem(e.getPlayer(), i, false);
            else e.getHarvestedBlock().getWorld().dropItemNaturally(e.getHarvestedBlock().getLocation(), i);
        }
        preparedBlockDrops.remove(e.getHarvestedBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBurnBreak(BlockBurnEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled()) return;

        UUID uuid = blockBreakerMap.get(e.getBlock().getLocation());
        blockBreakerMap.remove(e.getBlock().getLocation());
        Player p = uuid == null ? null : ValhallaMMO.getInstance().getServer().getPlayer(uuid);
        Pair<Double, Integer> details = getFortuneAndLuck(p, e.getBlock());
        int fortune = details.getTwo();
        double luck = details.getOne();

        onBlockDestruction(e.getBlock(),
                new LootContext.Builder(e.getBlock().getLocation()).lootedEntity(p).killer(null).lootingModifier(fortune).luck((float) luck).build(),
                new GenericBlockDestructionInfo(e.getBlock(), e),
                BlockDestructionEvent.BlockDestructionReason.BURN
        );
        clear(e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBurnBreakFinal(BlockBurnEvent e){
        if (e.isCancelled() || ArchaeologyListener.isBrushable(e.getBlock().getType())){
            preparedBlockDrops.remove(e.getBlock().getLocation());
            return;
        }
        for (ItemStack i : preparedBlockDrops.getOrDefault(e.getBlock().getLocation(), new ArrayList<>())){
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), i);
        }
        preparedBlockDrops.remove(e.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockFadeBreak(BlockFadeEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled()) return;

        UUID uuid = blockBreakerMap.get(e.getBlock().getLocation());
        blockBreakerMap.remove(e.getBlock().getLocation());
        Player p = uuid == null ? null : ValhallaMMO.getInstance().getServer().getPlayer(uuid);
        Pair<Double, Integer> details = getFortuneAndLuck(p, e.getBlock());
        int fortune = details.getTwo();
        double luck = details.getOne();

        if (onBlockDestruction(e.getBlock(),
                new LootContext.Builder(e.getBlock().getLocation()).lootedEntity(p).killer(null).lootingModifier(fortune).luck((float) luck).build(),
                new GenericBlockDestructionInfo(e.getBlock(), e),
                BlockDestructionEvent.BlockDestructionReason.FADE
        )){
            e.setCancelled(true);
            e.getBlock().setType(e.getNewState().getType());
        }
        clear(e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockFadeBreakFinal(BlockFadeEvent e){
        if (e.isCancelled() || ArchaeologyListener.isBrushable(e.getBlock().getType())){
            preparedBlockDrops.remove(e.getBlock().getLocation());
            return;
        }
        for (ItemStack i : preparedBlockDrops.getOrDefault(e.getBlock().getLocation(), new ArrayList<>())){
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), i);
        }
        preparedBlockDrops.remove(e.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockDecay(LeavesDecayEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled()) return;

        UUID uuid = blockBreakerMap.get(e.getBlock().getLocation());
        blockBreakerMap.remove(e.getBlock().getLocation());
        Player p = uuid == null ? null : ValhallaMMO.getInstance().getServer().getPlayer(uuid);
        Pair<Double, Integer> details = getFortuneAndLuck(p, e.getBlock());
        int fortune = details.getTwo();
        double luck = details.getOne();

        if (onBlockDestruction(e.getBlock(),
                new LootContext.Builder(e.getBlock().getLocation()).lootedEntity(p).killer(null).lootingModifier(fortune).luck((float) luck).build(),
                new GenericBlockDestructionInfo(e.getBlock(), e),
                BlockDestructionEvent.BlockDestructionReason.DECAY
        )){
            e.setCancelled(true);
            e.getBlock().setType(Material.AIR);
        }
        clear(e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDecayFinal(LeavesDecayEvent e){
        if (e.isCancelled() || ArchaeologyListener.isBrushable(e.getBlock().getType())){
            preparedBlockDrops.remove(e.getBlock().getLocation());
            return;
        }
        for (ItemStack i : preparedBlockDrops.getOrDefault(e.getBlock().getLocation(), new ArrayList<>())){
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), i);
        }
        preparedBlockDrops.remove(e.getBlock().getLocation());
    }

    private Pair<Double, Integer> getFortuneAndLuck(Player p, Block b){
        int fortune = 0;
        double luck = 0;
        if (p != null){
            AttributeInstance luckInstance = p.getAttribute(Attribute.GENERIC_LUCK);
            luck = luckInstance == null ? 0 : luckInstance.getValue();
            luck += preparedLuckBuffs.getOrDefault(b.getLocation(), 0D);
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (!ItemUtils.isEmpty(hand)){
                if (hand.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) fortune = hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
                else if (hand.containsEnchantment(Enchantment.SILK_TOUCH)) fortune = -1;
            }
        }
        return new Pair<>(luck, fortune);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockReplaceBreak(BlockFromToEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled()) return;

        UUID uuid = blockBreakerMap.get(e.getBlock().getLocation());
        blockBreakerMap.remove(e.getBlock().getLocation());
        Player p = uuid == null ? null : ValhallaMMO.getInstance().getServer().getPlayer(uuid);
        Pair<Double, Integer> details = getFortuneAndLuck(p, e.getBlock());
        int fortune = details.getTwo();
        double luck = details.getOne();

        if (!e.getBlock().getType().isAir() && e.getBlock().getType() != e.getToBlock().getType())
            onBlockDestruction(e.getBlock(),
                    new LootContext.Builder(e.getBlock().getLocation()).lootedEntity(p).killer(null).lootingModifier(fortune).luck((float) luck).build(),
                    new GenericBlockDestructionInfo(e.getBlock(), e),
                    BlockDestructionEvent.BlockDestructionReason.REPLACED
            );
        clear(e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockReplaceBreakFinal(BlockFromToEvent e){
        if (e.isCancelled() || ArchaeologyListener.isBrushable(e.getBlock().getType())){
            preparedBlockDrops.remove(e.getBlock().getLocation());
            return;
        }
        for (ItemStack i : preparedBlockDrops.getOrDefault(e.getBlock().getLocation(), new ArrayList<>())){
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), i);
        }
        preparedBlockDrops.remove(e.getBlock().getLocation());
    }

    private static final Map<Location, Material> explodedBlocks = new HashMap<>();

    /**
     * Marks a block as having been exploded. When a block is marked as such, regardless of if it's now air or not,
     * it will be treated as if it was its previous material and trigger loot tables and scheduled drops for it.
     * @param b the block to mark as being exploded. It's important this block is still its original material pre-explosion or this method will do nothing
     */
    public static void markExploded(Block b){
        explodedBlocks.put(b.getLocation(), b.getType());
    }

    public static void setFortuneLevel(Entity e, int level){
        e.setMetadata("valhalla_explosive_fortune", new FixedMetadataValue(ValhallaMMO.getInstance(), level));
    }

    public static int getFortuneLevel(Entity e){
        if (!e.hasMetadata("valhalla_explosive_fortune")) return 0;
        Optional<MetadataValue> value = e.getMetadata("valhalla_explosive_fortune").stream().findAny();
        return value.map(MetadataValue::asInt).orElse(0);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBlockExplodeBreak(BlockExplodeEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled()) return;

        List<Block> blocks = new ArrayList<>(e.blockList());
        blocks.addAll(explodedBlocks.keySet().stream().map(Location::getBlock).collect(Collectors.toList()));
        for (Block b : blocks){
            exploded.add(b);
            double extraLuck = preparedLuckBuffs.getOrDefault(b.getLocation(), 0D);
            if (onBlockDestruction(b,
                    new LootContext.Builder(b.getLocation()).lootedEntity(null).killer(null).lootingModifier(0).luck((float) extraLuck).build(),
                    new BlockExplodeBlockDestructionInfo(b, e),
                    BlockDestructionEvent.BlockDestructionReason.BLOCK_EXPLOSION
            )) {
                e.blockList().remove(b);
                b.setType(Material.AIR);
            }
            clear(b);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBlockExplodeBreakFinal(BlockExplodeEvent e){
        if (e.isCancelled() || ArchaeologyListener.isBrushable(e.getBlock().getType())){
            preparedBlockDrops.remove(e.getBlock().getLocation());
            return;
        }

        List<Block> blocks = new ArrayList<>(e.blockList());
        blocks.addAll(explodedBlocks.keySet().stream().map(Location::getBlock).collect(Collectors.toList()));
        for (Block b : blocks){
            if (!ArchaeologyListener.isBrushable(b.getType())) {
                for (ItemStack i : preparedBlockDrops.getOrDefault(b.getLocation(), new ArrayList<>())){
                    b.getWorld().dropItemNaturally(b.getLocation(), i);
                }
            }
            preparedBlockDrops.remove(b.getLocation());
        }
    }

    private static final Collection<Block> exploded = new HashSet<>();
    public static boolean destroyedByExplosion(Block b){
        return exploded.contains(b);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockEntityExplodeBreak(EntityExplodeEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        Optional<MetadataValue> uuidMeta = e.getEntity().getMetadata("valhalla_entity_owner").stream().findAny();
        UUID uuid = uuidMeta.map(metadataValue -> UUID.fromString(metadataValue.asString())).orElse(null);
        Entity owner = uuid == null ? null : ValhallaMMO.getInstance().getServer().getEntity(uuid);

        AttributeInstance luckInstance = owner instanceof LivingEntity l ? l.getAttribute(Attribute.GENERIC_LUCK) : null;
        double luck = luckInstance == null ? 0 : luckInstance.getValue();

        List<Block> blocks = new ArrayList<>(e.blockList());
        blocks.addAll(explodedBlocks.keySet().stream().map(Location::getBlock).collect(Collectors.toList()));
        for (Block b : blocks){
            if (ArchaeologyListener.isBrushable(b.getType())) continue;
            exploded.add(b);
            double extraLuck = preparedLuckBuffs.getOrDefault(b.getLocation(), 0D);
            if (onBlockDestruction(b,
                    new LootContext.Builder(b.getLocation()).lootedEntity(owner != null ? owner : e.getEntity()).killer(null).lootingModifier(getFortuneLevel(e.getEntity())).luck((float) (luck + extraLuck)).build(),
                    new EntityExplodeBlockDestructionInfo(b, e),
                    BlockDestructionEvent.BlockDestructionReason.ENTITY_EXPLOSION
            )) {
                e.blockList().remove(b);
                b.setType(Material.AIR);
            }
            clear(b);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockEntityExplodeBreakFinal(EntityExplodeEvent e){
        if (e.isCancelled()) return;
        Optional<MetadataValue> uuidMeta = e.getEntity().getMetadata("valhalla_entity_owner").stream().findAny();
        UUID uuid = uuidMeta.map(metadataValue -> UUID.fromString(metadataValue.asString())).orElse(null);
        Entity owner = uuid == null ? null : ValhallaMMO.getInstance().getServer().getEntity(uuid);

        List<Block> blocks = new ArrayList<>(e.blockList());
        blocks.addAll(explodedBlocks.keySet().stream().map(Location::getBlock).collect(Collectors.toList()));
        for (Block b : blocks){
            if (!ArchaeologyListener.isBrushable(b.getType())){
                for (ItemStack i : preparedBlockDrops.getOrDefault(b.getLocation(), new ArrayList<>())){
                    if (owner instanceof Player p && transferToInventory.containsKey(b.getLocation()) && transferToInventory.get(b.getLocation()).equals(p.getUniqueId())) ItemUtils.addItem(p, i, false);
                    else b.getWorld().dropItemNaturally(b.getLocation(), i);
                }
            }
            preparedBlockDrops.remove(b.getLocation());
        }
    }

    public static void setEntityOwner(Entity entity, Entity owner){
        entity.setMetadata("valhalla_entity_owner", new FixedMetadataValue(ValhallaMMO.getInstance(), owner.getUniqueId().toString()));
    }

    /**
     * Handles the loot following the destruction of a block and prepares them for dropping. Placed blocks will not drop special
     * loot tables.
     * @param b the block destroyed
     * @param context the loot context in which it is destroyed
     * @param info the destruction info which contains the block and the event responsible for destroying it. Block loot is skipped if according to this info the event is cancelled
     * @param reason the type of block destruction the block experienced
     * @return true if the loot table of the block (if any) resulted in the vanilla loot being cleared. False if vanilla loot was untouched
     */
    private boolean onBlockDestruction(Block b, LootContext context, BlockDestructionInfo info, BlockDestructionEvent.BlockDestructionReason reason){
        BlockDestructionEvent destroyEvent = new BlockDestructionEvent(b, info, reason);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(destroyEvent);
        Material originalMaterial = explodedBlocks.getOrDefault(b.getLocation(), b.getType());
        explodedBlocks.remove(b.getLocation());
        if (!destroyEvent.getInfo().isCancelled(info.getEvent())){
            LootTable table = LootTableRegistry.getLootTable(b, originalMaterial);
            if (table == null || !BlockUtils.canReward(b) || ArchaeologyListener.isBrushable(b.getType())) return false;
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
        if (ItemUtils.isEmpty(placed)) return;
        ItemBuilder item = new ItemBuilder(placed);
        LootTable table = LootTableRegistry.getLootTable(item.getMeta());
        if (table == null) return;
        LootTableRegistry.setLootTable(e.getBlock(), table);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChestOpen(PlayerInteractEvent e){
        if (e.getClickedBlock() == null || ValhallaMMO.isWorldBlacklisted(e.getClickedBlock().getWorld().getName()) ||
                e.useInteractedBlock() == Event.Result.DENY || e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() == EquipmentSlot.OFF_HAND) return;
        Block b = e.getClickedBlock();
        if (!(b.getState() instanceof Lootable l) || !(b.getState() instanceof Container c) || ArchaeologyListener.isBrushable(b.getState())) return;
        if (ValhallaMMO.isUsingPaperMC() && !PaperLootRefillHandler.canGenerateLoot(b.getState(), e.getPlayer())) return;
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
                case CLEAR -> {
                    l.setLootTable(null);
                    b.getState().update();
                    c.getInventory().clear();
                }
                case CLEAR_UNLESS_EMPTY -> {
                    if (!loottableEvent.getDrops().isEmpty()) {
                        l.setLootTable(null);
                        b.getState().update();
                        c.getInventory().clear();
                    }
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

    @EventHandler(priority = EventPriority.NORMAL)
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

        Sound sound = LootTableRegistry.getLootSound(item.getMeta());
        AttributeInstance luckInstance = e.getPlayer().getAttribute(Attribute.GENERIC_LUCK);
        double luck = luckInstance == null ? 0 : luckInstance.getValue();

        LootContext context = new LootContext.Builder(e.getPlayer().getLocation()).killer(null).lootedEntity(e.getPlayer()).lootingModifier(0).luck((float) luck).build();
        if (LootTableRegistry.isFreeSelectionTable(item.getMeta())){
            new LootFreeSelectionMenu(PlayerMenuUtilManager.getPlayerMenuUtility(e.getPlayer()), table, context, LootTableRegistry.allowRepeatedFreeSelection(item.getMeta()), (selection) -> {
                ItemBuilder handItem = ItemUtils.isEmpty(e.getPlayer().getInventory().getItemInMainHand()) ? null : new ItemBuilder(e.getPlayer().getInventory().getItemInMainHand());
                if (handItem == null) return;
                LootTable storedTable = LootTableRegistry.getLootTable(handItem.getMeta());
                if (storedTable == null || !storedTable.getKey().equals(table.getKey())) return;

                List<ItemStack> rewards = new ArrayList<>();
                for (LootEntry entry : selection.keySet()){
                    int times = selection.get(entry);
                    for (int i = 0; i < times; i++){
                        ItemBuilder reward = new ItemBuilder(entry.getDrop());
                        DynamicItemModifier.modify(reward, e.getPlayer(), entry.getModifiers(), false, true, true);
                        if (!CustomFlag.hasFlag(reward.getMeta(), CustomFlag.UNCRAFTABLE)) {
                            int quantityMin = Utils.randomAverage(entry.getBaseQuantityMin() + (Math.max(0, context.getLootingModifier()) * entry.getQuantityMinFortuneBase()));
                            int quantityMax = Utils.randomAverage(entry.getBaseQuantityMax() + (Math.max(0, context.getLootingModifier()) * entry.getQuantityMaxFortuneBase()));
                            if (quantityMax < quantityMin) quantityMax = quantityMin;
                            int quantity = Utils.getRandom().nextInt(Math.max(1, quantityMax - quantityMin)) + quantityMin;

                            int trueQuantity = entry.getDrop().getAmount() * quantity;
                            List<ItemStack> loot;
                            if (trueQuantity > 0 ) {
                                loot = new ArrayList<>(ItemUtils.decompressStacks(Map.of(reward.get(), trueQuantity)));
                            } else continue;

                            rewards.addAll(loot);
                        }
                    }
                }
                if (sound != null) e.getPlayer().playSound(e.getPlayer(), sound, 1F, 1F);
                rewards.forEach(i -> ItemUtils.addItem(e.getPlayer(), i, true));

                ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
                if (hand.getAmount() <= 1) e.getPlayer().getInventory().setItemInMainHand(null);
                else hand.setAmount(hand.getAmount() - 1);
            }).open();
        } else {
            List<ItemStack> loot = LootTableRegistry.getLoot(table, context, LootTable.LootType.CONTAINER);
            ValhallaLootPopulateEvent loottableEvent = new ValhallaLootPopulateEvent(table, context, loot);
            ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(loottableEvent);
            if (!loottableEvent.isCancelled()){
                loottableEvent.getDrops().forEach(i -> ItemUtils.addItem(e.getPlayer(), i, true));

                if (clickedItem.getAmount() <= 1) e.getPlayer().getInventory().setItemInMainHand(null);
                else clickedItem.setAmount(clickedItem.getAmount() - 1);
            }
            if (sound != null) e.getPlayer().playSound(e.getPlayer(), sound, 1F, 1F);
        }
    }

    private final Collection<Material> itemDuplicationWhitelist = new HashSet<>(ItemUtils.getMaterialSet(ValhallaMMO.getPluginConfig().getStringList("item_duplication_whitelist")));

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDrops(EntityDeathEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || EntityClassification.matchesClassification(e.getEntityType(), EntityClassification.UNALIVE)) return;
        LivingEntity entity = e.getEntity();
        Entity killer = entity.getKiller();
        EntityDamageEvent lastDamageSource = entity.getLastDamageCause();
        if (killer == null && lastDamageSource instanceof EntityDamageByEntityEvent event){
            if (event.getDamager() instanceof LivingEntity realKiller) killer = realKiller;
        }

        Collection<Material> droppedHandTypes = new HashSet<>();
        EntityEquipment equipment = entity.getEquipment();
        if (equipment != null){
            if (!ItemUtils.isEmpty(equipment.getItemInMainHand())) droppedHandTypes.add(equipment.getItemInMainHand().getType());
            if (!ItemUtils.isEmpty(equipment.getItemInOffHand())) droppedHandTypes.add(equipment.getItemInOffHand().getType());
            if (!ItemUtils.isEmpty(equipment.getHelmet())) droppedHandTypes.add(equipment.getItemInOffHand().getType());
            if (!ItemUtils.isEmpty(equipment.getChestplate())) droppedHandTypes.add(equipment.getItemInOffHand().getType());
            if (!ItemUtils.isEmpty(equipment.getLeggings())) droppedHandTypes.add(equipment.getItemInOffHand().getType());
            if (!ItemUtils.isEmpty(equipment.getBoots())) droppedHandTypes.add(equipment.getItemInOffHand().getType());
        }
        double dropMultiplier = killer == null ? 0 : AccumulativeStatManager.getCachedStats("ENTITY_DROPS", killer, 10000, true);
        ItemUtils.multiplyItems(e.getDrops(), 1 + dropMultiplier, false, i -> !i.hasItemMeta() && itemDuplicationWhitelist.contains(i.getType()) && !droppedHandTypes.contains(i.getType()));

        LootTable table = LootTableRegistry.getLootTable(entity);
        if (table == null) return;
        int looting = 0;
        double luck = 0;
        if (killer != null){
            AttributeInstance luckInstance = entity.getKiller() == null ? null : entity.getKiller().getAttribute(Attribute.GENERIC_LUCK);
            luck = luckInstance == null ? 0 : luckInstance.getValue();
            luck += AccumulativeStatManager.getCachedStats("ENTITY_DROP_LUCK", killer, 10000, true);

            if (killer instanceof HumanEntity h){
                looting = ItemUtils.isEmpty(h.getInventory().getItemInMainHand()) ? 0 :
                        h.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
            }
        }

        if (killer != null) realKiller.put(entity.getUniqueId(), killer.getUniqueId());
        LootContext context = new LootContext.Builder(entity.getLocation()).killer(null).lootedEntity(entity).lootingModifier(looting).luck((float) luck).build();
        List<ItemStack> loot = LootTableRegistry.getLoot(table, context, LootTable.LootType.KILL);
        realKiller.remove(entity.getUniqueId());
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

    private static final Map<UUID, UUID> realKiller = new HashMap<>();
    public static Entity getRealKiller(Entity victim){
        UUID killer = realKiller.get(victim.getUniqueId());
        return killer == null ? null : ValhallaMMO.getInstance().getServer().getEntity(killer);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDrops(BlockDropItemEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled() || !transferToInventory.containsKey(e.getBlock().getLocation())){
            clear(e.getBlock());
            return;
        }
        boolean transfer = transferToInventory.containsKey(e.getBlock().getLocation()) && transferToInventory.get(e.getBlock().getLocation()).equals(e.getPlayer().getUniqueId());
        for (ItemStack item : preparedBlockDrops.getOrDefault(e.getBlock().getLocation(), new ArrayList<>())){
            if (transfer) ItemUtils.addItem(e.getPlayer(), item, false);
            else e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), item);
        }
        preparedBlockDrops.remove(e.getBlock().getLocation());
        if (transfer){
            for (Item i : e.getItems()) ItemUtils.addItem(e.getPlayer(), i.getItemStack(), false);
            e.getItems().clear();
        }
        clear(e.getBlock());
    }

    public static void clear(Block b){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            transferToInventory.remove(b.getLocation());
            preparedBlockDrops.remove(b.getLocation());
            preparedLuckBuffs.remove(b.getLocation());
            explodedBlocks.remove(b.getLocation());
            exploded.remove(b);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFishFinal(PlayerFishEvent e){
        preparedFishingLuckBuffs.remove(e.getPlayer().getUniqueId());
        if (e.isCancelled() || e.getState() != PlayerFishEvent.State.CAUGHT_FISH ) return;
        preparedFishingDrops.getOrDefault(e.getPlayer().getUniqueId(), new ArrayList<>()).forEach(d -> {
            Item item = e.getHook().getWorld().dropItem(e.getHook().getLocation(), d);
            item.setVelocity(fishingItemVelocity(e.getHook().getLocation(), e.getPlayer().getLocation().add(0, e.getPlayer().getBoundingBox().getHeight() / 2, 0)));
        });
        preparedFishingDrops.remove(e.getPlayer().getUniqueId());
    }

    private Vector fishingItemVelocity(Location hook, Location player){
        double d = player.getX() - hook.getX();
        double e = player.getY() - hook.getY();
        double f = player.getZ() - hook.getZ();
        return new Vector(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
    }

    private static final Map<UUID, List<ItemStack>> preparedFishingDrops = new HashMap<>();

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

    public static void addPreparedLuck(Block b, double luck){
        double existing = preparedLuckBuffs.getOrDefault(b.getLocation(), 0D);
        preparedLuckBuffs.put(b.getLocation(), existing + luck);
    }

    public static void addPreparedLuck(Player p, double luck){
        double existing = preparedFishingLuckBuffs.getOrDefault(p.getUniqueId(), 0D);
        preparedFishingLuckBuffs.put(p.getUniqueId(), existing + luck);
    }

    public static double getPreparedLuck(Player p){
        return preparedFishingLuckBuffs.getOrDefault(p.getUniqueId(), 0D);
    }

    public static double getPreparedLuck(Block b){
        return preparedLuckBuffs.getOrDefault(b.getLocation(), 0D);
    }

    /**
     * Returns the extra drops that have been prepared for the given block, to provide some way for other plugins
     * to see what a block will drop in case that's needed. Spigot does not
     * allow items to be added to a block's drops directly, and so they need to be prepared instead.
     * @param b the block to check its prepared drops for
     * @return the list of extra items that will drop from the block, or an empty list if none.
     */
    public static List<ItemStack> getPreparedExtraDrops(Block b){
        return preparedBlockDrops.getOrDefault(b.getLocation(), new ArrayList<>());
    }

    /**
     * Returns the extra drops that have been prepared for the player during fishing
     * @param p the player to get their extra fishing drops from
     * @return the list of extra items that will drop from the fishing catch, or an empty list if none.
     */
    public static List<ItemStack> getPreparedExtraDrops(Player p){
        return preparedFishingDrops.getOrDefault(p.getUniqueId(), new ArrayList<>());
    }

    public static void setInstantPickup(Block b, Player who){
        transferToInventory.put(b.getLocation(), who.getUniqueId());
    }

    public static void prepareFishingDrops(UUID fisher, ItemStack... items){
        prepareFishingDrops(fisher, List.of(items));
    }

    public static void prepareFishingDrops(UUID fisher, List<ItemStack> items){
        List<ItemStack> preparedDrops = preparedFishingDrops.getOrDefault(fisher, new ArrayList<>());
        preparedDrops.addAll(items);
        preparedFishingDrops.put(fisher, preparedDrops);
    }

    public static void prepareBlockDrops(Block b, ItemStack... items){
        prepareBlockDrops(b, List.of(items));
    }

    public static void prepareBlockDrops(Block b, List<ItemStack> items){
        List<ItemStack> preparedDrops = preparedBlockDrops.getOrDefault(b.getLocation(), new ArrayList<>());
        preparedDrops.addAll(items);
        preparedBlockDrops.put(b.getLocation(), preparedDrops);
    }

    public static Map<UUID, List<ItemStack>> getPreparedFishingDrops() {
        return preparedFishingDrops;
    }

    public static Map<Location, List<ItemStack>> getPreparedBlockDrops() {
        return preparedBlockDrops;
    }
}
