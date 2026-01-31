package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.event.PlayerBlocksDropItemsEvent;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.listeners.CustomBreakSpeedListener;
import me.athlaeos.valhallammo.listeners.LootListener;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.WoodcuttingProfile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.*;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;

public class WoodcuttingSkill extends Skill implements Listener {
    private final Map<String, Double> dropsExpValues = new HashMap<>();
    private final Map<String, Double> stripExpValues = new HashMap<>();
    private final Collection<String> treeCapitatorPreventionBlocks = new HashSet<>();
    private final Collection<String> additionalLogDefinition = new HashSet<>();
    private final Collection<String> additionalLeafDefinition = new HashSet<>();

    private int treeCapitatorLimit = 128;
    private boolean treeCapitatorInstant = true;
    private int treeCapitatorLeavesLimit = 256;

    private int treeScanLimit = 256;

    private boolean forgivingDropMultipliers = true; // if false, depending on drop multiplier, drops may be reduced to 0. If true, this will be at least 1

    public WoodcuttingSkill(String type) {
        super(type);
    }

    @Override
    public void loadConfiguration() {
        ValhallaMMO.getInstance().save("skills/woodcutting_progression.yml");
        ValhallaMMO.getInstance().save("skills/woodcutting.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/woodcutting.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/woodcutting_progression.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);

        treeCapitatorLimit = skillConfig.getInt("break_limit_tree_capitator");
        treeCapitatorInstant = skillConfig.getBoolean("tree_capitator_instant");
        treeCapitatorLeavesLimit = skillConfig.getInt("leaf_decay_limit_tree_capitator");
        forgivingDropMultipliers = skillConfig.getBoolean("forgiving_multipliers");
        treeScanLimit = skillConfig.getInt("tree_scan_limit");

        treeCapitatorPreventionBlocks.addAll(skillConfig.getStringList("tree_capitator_prevention_blocks"));
        additionalLogDefinition.addAll(skillConfig.getStringList("additional_log_definition"));
        additionalLeafDefinition.addAll(skillConfig.getStringList("additional_leaves_definition"));

        ConfigurationSection blockBreakSection = progressionConfig.getConfigurationSection("experience.woodcutting_break");
        if (blockBreakSection != null){
            for (String key : blockBreakSection.getKeys(false)){
                double reward = progressionConfig.getDouble("experience.woodcutting_break." + key);
                dropsExpValues.put(key, reward);
            }
        }

        ConfigurationSection blockStripSection = progressionConfig.getConfigurationSection("experience.woodcutting_strip");
        if (blockStripSection != null){
            for (String key : blockStripSection.getKeys(false)){
                double reward = progressionConfig.getDouble("experience.woodcutting_strip." + key);
                stripExpValues.put(key, reward);
            }
        }

        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    private final int[][] treeCapitatorScanArea = MathUtils.getOffsetsBetweenPoints(new int[]{-1, 0, -1}, new int[]{1, 1, 1});
    private final int[][] treeCapitatorLeavesScanArea = new int[][]{
            new int[]{-1, 0, 0}, new int[]{1, 0, 0},
            new int[]{0, -1, 0}, new int[]{0, 1, 0},
            new int[]{0, 0, -1}, new int[]{0, 0, 1},
            new int[]{0, -2, 0}, new int[]{0, -3, 0}
    };

    private final Collection<UUID> treeCapitatingPlayers = new HashSet<>();
    private final Map<Block, UUID> decayCausedBy = new HashMap<>();

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (ValhallaMMO.isWorldBlacklisted(player.getWorld().getName()) || player.getGameMode() == GameMode.CREATIVE) return;

        Block block = event.getBlock();
        String type = BlockUtils.getBlockType(block);
        if (!isLog(block, BlockUtils.getBlockType(block)) || !dropsExpValues.containsKey(type) || !hasPermissionAccess(player) || WorldGuardHook.inDisabledRegion(block.getLocation(), player, WorldGuardHook.VMMO_SKILL_WOODCUTTING)) {
            return;
        }

        WoodcuttingProfile profile = ProfileCache.getOrCache(player, WoodcuttingProfile.class);
        double woodCuttingLuck = AccumulativeStatManager.getCachedStats("WOODCUTTING_LUCK", player, 10000, true);
        if (BlockUtils.canReward(block)) {
            event.setExpToDrop(event.getExpToDrop() + Utils.randomAverage(profile.getBlockExperienceRate()));
            LootListener.addPreparedLuck(block, woodCuttingLuck);
        }

        if (treeCapitatingPlayers.contains(player.getUniqueId()) || !player.isSneaking() || !hasAxe(player)
                || !profile.isTreeCapitatorUnlocked() || !profile.getTreeCapitatorValidBlocks().contains(type)
                || WorldGuardHook.inDisabledRegion(player.getLocation(), player, WorldGuardHook.VMMO_ABILITIES_TREECAPITATOR)) {
            return;
        } else if (!Timer.isCooldownPassed(player.getUniqueId(), "woodcutting_tree_capitator")) {
            Timer.sendIfNotPassed(player, "woodcutting_tree_capitator", TranslationManager.getTranslation("ability_tree_capitator"));
            return;
        } else if (!isTree(block)) {
            return;
        }

        Collection<Block> vein = BlockUtils.getBlockVein(block, Math.min(treeCapitatorLimit, profile.getTreeCapitatorLimit() + 1), b -> treeCapitatorPreventionBlocks.contains(b.getType().toString()) || isLog(b, BlockUtils.getBlockType(b)), treeCapitatorScanArea);
        if (vein.size() > profile.getTreeCapitatorLimit()) {
            return;
        }
        if (vein.stream().anyMatch(b -> treeCapitatorPreventionBlocks.contains(BlockUtils.getBlockType(b)))) return;
        treeCapitatingPlayers.add(player.getUniqueId());
        event.setCancelled(true);

        Block leafOrigin = getTreeLeafOrigin(block);
        Consumer<Player> onFinish = (p) -> {
            endEXPBatch(player);
            treeCapitatingPlayers.remove(player.getUniqueId());
            if (leafOrigin == null) {
                return;
            }

            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                List<Block> leaves = new ArrayList<>(BlockUtils.getBlockVein(leafOrigin, treeCapitatorLeavesLimit,
                        bl -> isLeaves(bl, BlockUtils.getBlockType(bl)) && (!(bl.getBlockData() instanceof Leaves l) || l.getDistance() > 3), treeCapitatorLeavesScanArea));
                Collections.shuffle(leaves);
                BlockUtils.processBlocksDelayed(player, leaves, $ -> true, leaf -> {
                    decayCausedBy.put(leaf, player.getUniqueId());
                    BlockUtils.decayBlock(leaf);
                    decayCausedBy.remove(leaf);
                }, null);
            }, 20L);
        };

        startEXPBatch(player, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION, false);
        if (treeCapitatorInstant) {
            BlockUtils.processBlocks(player, vein, WoodcuttingSkill::hasAxe, b -> {
                if (profile.isTreeCapitatorInstantPickup()) LootListener.setInstantPickup(b, player);
                CustomBreakSpeedListener.markInstantBreak(b);
                player.breakBlock(b);
            }, onFinish);
        } else {
            BlockUtils.processBlocksPulse(player, block, vein, WoodcuttingSkill::hasAxe, b -> {
                if (profile.isTreeCapitatorInstantPickup()) LootListener.setInstantPickup(b, player);
                CustomBreakSpeedListener.markInstantBreak(b);
                player.breakBlock(b);
            }, $ -> {
                startEXPBatch(player, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION, false);
            }, onFinish);
        }
        Timer.setCooldownIgnoreIfPermission(player, profile.getTreeCapitatorCooldown() * 50, "woodcutting_tree_capitator");
    }

    public static boolean hasAxe(Player p) {
        EntityProperties properties = EntityCache.getAndCacheProperties(p);
        return properties.getMainHand() != null && EquipmentClass.getMatchingClass(properties.getMainHand().getMeta()) == EquipmentClass.AXE;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockDecay(LeavesDecayEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName())) return;
        Player p = null;
        List<Player> nearby = EntityUtils.getNearbyPlayers(e.getBlock().getLocation(), 100);
        if (!nearby.isEmpty()) p = nearby.get(0);
        if (p == null) return;
        double woodCuttingLuck = AccumulativeStatManager.getCachedStats("WOODCUTTING_LUCK", p, 10000, true);
        LootListener.addPreparedLuck(e.getBlock(), woodCuttingLuck);
        LootListener.setResponsibleBreaker(e.getBlock(), p);
    }

    private final int[][] treeScanArea = MathUtils.getOffsetsBetweenPoints(new int[]{-1, 1, -1}, new int[]{1, 1, 1});

    /**
     * Determines if the player is holding an axe from cached entity properties.
     * @param p Player
     */
    private boolean playerHasAxe(Player p) {
        EntityProperties properties = EntityCache.getAndCacheProperties(p);
        return properties.getMainHand() != null && EquipmentClass.getMatchingClass(properties.getMainHand().getMeta()) == EquipmentClass.AXE;
    }

    /**
     * A tree is defined as any log with any leaves connected somewhere above it.
     */
    private boolean isTree(Block b){
        Collection<Block> treeBlocks = BlockUtils.getBlockVein(b, treeScanLimit, l -> !BlockStore.isPlaced(l) && (isLog(l, BlockUtils.getBlockType(l)) || isLeaves(l, BlockUtils.getBlockType(l))), treeScanArea);
        return treeBlocks.stream().anyMatch(l -> isLog(l, BlockUtils.getBlockType(l)) && treeBlocks.stream().anyMatch(le -> isLeaves(le, BlockUtils.getBlockType(le))));
    }

    // checks up to 48 blocks above the log mined for a leaf block which might be used as origin
    private Block getTreeLeafOrigin(Block b){
        for (int i = 1; i < 48; i++){
            if (b.getLocation().getY() + i >= b.getWorld().getMaxHeight()) break;
            Block blockAt = b.getLocation().add(0, i, 0).getBlock();
            String type = BlockUtils.getBlockType(blockAt);
            if (isLeaves(blockAt, type)) return blockAt;
            if (!blockAt.getType().isAir() && !isLog(blockAt, type)) return null;
        }
        return null;
    }

    private boolean isLeaves(Block block, String potentialCustomType){
        if (BlockStore.isPlaced(block)) return false;
        if (additionalLeafDefinition.contains(potentialCustomType)) return true;
        if (Tag.LEAVES.isTagged(block.getType()) && block.getBlockData() instanceof Leaves) return true;
        return block.getType() == Material.NETHER_WART_BLOCK || block.getType() == Material.WARPED_WART_BLOCK || block.getType() == Material.SHROOMLIGHT;
    }

    private boolean isLog(Block block, String potentialCustomType){
        if (additionalLogDefinition.contains(potentialCustomType)) return true;
        if (Tag.LOGS.isTagged(block.getType())) return true;
        return block.getType().toString().equals("MANGROVE_ROOTS");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void lootTableDrops(BlockBreakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || !isLog(e.getBlock(), type) ||
                !BlockUtils.canReward(e.getBlock()) || WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_WOODCUTTING) ||
                e.getBlock().getState() instanceof Container) return;
        double dropMultiplier = AccumulativeStatManager.getCachedStats("WOODCUTTING_DROP_MULTIPLIER", e.getPlayer(), 10000, true);
        // multiply any applicable prepared drops and grant exp for them. After the extra drops from a BlockBreakEvent the drops are cleared
        ItemUtils.multiplyItems(LootListener.getPreparedExtraDrops(e.getBlock()), 1 + dropMultiplier, forgivingDropMultipliers, (i) -> dropsExpValues.containsKey(type));

        double expQuantity = 0;
        for (ItemStack i : LootListener.getPreparedExtraDrops(e.getBlock())){
            if (ItemUtils.isEmpty(i)) continue;
            String type = ItemUtils.getItemType(i);
            expQuantity += dropsExpValues.getOrDefault(type, 0D) * i.getAmount();
        }
        addEXP(e.getPlayer(), expQuantity, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemsDropped(BlockDropItemEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlockState().getWorld().getName()) || !BlockUtils.canReward(e.getBlockState()) ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_WOODCUTTING) ||
                e.getBlockState() instanceof Container) return;
        double dropMultiplier = AccumulativeStatManager.getCachedStats("WOODCUTTING_DROP_MULTIPLIER", e.getPlayer(), 10000, true);
        // multiply the item drops from the event itself and grant exp for the initial items and extra drops
        List<ItemStack> extraDrops = ItemUtils.multiplyDrops(e.getItems(), 1 + dropMultiplier, forgivingDropMultipliers, (i) -> dropsExpValues.containsKey(ItemUtils.getItemType(i.getItemStack())));
        if (!extraDrops.isEmpty()) LootListener.prepareBlockDrops(e.getBlock(), extraDrops);

        double expQuantity = 0;
        for (Item i : e.getItems()){
            if (ItemUtils.isEmpty(i.getItemStack())) continue;
            String type = ItemUtils.getItemType(i.getItemStack());
            expQuantity += dropsExpValues.getOrDefault(type, 0D) * i.getItemStack().getAmount();
        }
        for (ItemStack i : extraDrops){
            if (ItemUtils.isEmpty(i)) continue;
            expQuantity += dropsExpValues.getOrDefault(ItemUtils.getItemType(i), 0D) * i.getAmount();
        }
        addEXP(e.getPlayer(), expQuantity, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlaced(BlockPlaceEvent e) {
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_WOODCUTTING)) return;
        Block b = e.getBlock();
        if (stripExpValues.containsKey(b.getType().toString()) && dropsExpValues.containsKey(e.getBlockReplacedState().getType().toString())) {
            double amount = stripExpValues.get(b.getType().toString());
            if (amount > 0) addEXP(e.getPlayer(), amount, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
        } else if (Tag.SAPLINGS.isTagged(b.getType()) && b.getBlockData() instanceof Sapling s){
            WoodcuttingProfile profile = ProfileCache.getOrCache(e.getPlayer(), WoodcuttingProfile.class);
            s.setStage(Math.min(Math.max(0, Utils.randomAverage(profile.getInstantGrowthRate())), s.getMaximumStage() - 1));
            e.getBlock().setBlockData(s);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onOtherBlockDrops(PlayerBlocksDropItemsEvent e){
        double exp = 0;
        for (Block b : e.getBlocksAndItems().keySet()){
            for (ItemStack i : e.getBlocksAndItems().getOrDefault(b, new ArrayList<>())){
                if (ItemUtils.isEmpty(i)) continue;
                String type = ItemUtils.getItemType(i);
                exp += dropsExpValues.getOrDefault(type, 0D) * i.getAmount();
            }
        }
        addEXP(e.getPlayer(), exp, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return WoodcuttingProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 25;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_WOODCUTTING)) return;
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= (1 + AccumulativeStatManager.getCachedStats("WOODCUTTING_EXP_GAIN", p, 10000, true));
        }
        super.addEXP(p, amount, silent, reason);
    }

    public Map<String, Double> getDropsExpValues() {
        return dropsExpValues;
    }
}
