package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.commands.valhallasubcommands.Debugger;
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
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.*;
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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
        String type = BlockUtils.getBlockType(e.getBlock());
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || !isLog(e.getBlock(), type) ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_WOODCUTTING) ||
                !dropsExpValues.containsKey(type) || e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        WoodcuttingProfile profile = ProfileCache.getOrCache(e.getPlayer(), WoodcuttingProfile.class);

        if (!hasPermissionAccess(e.getPlayer())) {
            Debugger.send(e.getPlayer(), "tree_capitator", "&cDoes not have permission access to Woodcutting");
            return;
        }
        double woodCuttingLuck = AccumulativeStatManager.getCachedStats("WOODCUTTING_LUCK", e.getPlayer(), 10000, true);
        if (BlockUtils.canReward(e.getBlock())){
            e.setExpToDrop(e.getExpToDrop() + Utils.randomAverage(profile.getBlockExperienceRate()));
            LootListener.addPreparedLuck(e.getBlock(), woodCuttingLuck);
        }

        if (ItemUtils.isEmpty(e.getPlayer().getInventory().getItemInMainHand()) || !e.getPlayer().getInventory().getItemInMainHand().getType().toString().contains("_AXE")) return;
        if (e.getPlayer().isSneaking() && !treeCapitatingPlayers.contains(e.getPlayer().getUniqueId()) &&
                profile.isTreeCapitatorUnlocked() &&
                profile.getTreeCapitatorValidBlocks().contains(type) &&
                Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "woodcutting_tree_capitator") &&
                isTree(e.getBlock()) &&
                !WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_ABILITIES_TREECAPITATOR)){
            Collection<Block> vein = BlockUtils.getBlockVein(e.getBlock(), treeCapitatorLimit, b -> isLog(b, BlockUtils.getBlockType(b)) || treeCapitatorPreventionBlocks.contains(type), treeCapitatorScanArea);
            if (vein.size() > profile.getTreeCapitatorLimit()) {
                Debugger.send(e.getPlayer(), "tree_capitator", "&cTree size " + vein.size() + " is above limit " + profile.getTreeCapitatorLimit());
                return;
            }
            if (vein.stream().anyMatch(b -> treeCapitatorPreventionBlocks.contains(type))) {
                Debugger.send(e.getPlayer(), "tree_capitator", "&cTree contains one of the following blocks: " + String.join(String.join(", ", treeCapitatorPreventionBlocks) + ", which are tree capitator protection blocks"));
                return;
            }
            treeCapitatingPlayers.add(e.getPlayer().getUniqueId());
            e.setCancelled(true);

            Block leafOrigin = getTreeLeafOrigin(e.getBlock());

            if (treeCapitatorInstant) {
                if (!playerHasAxe(e.getPlayer())) {
                    Debugger.send(e.getPlayer(), "tree_capitator", "&cCached player properties indicates player does not have an axe.");
                    treeCapitatingPlayers.remove(e.getPlayer().getUniqueId());
                } else {
                    BlockUtils.processBlocks(e.getPlayer(), vein, this::playerHasAxe, b -> {
                        if (profile.isTreeCapitatorInstantPickup()) LootListener.setInstantPickup(b, e.getPlayer());
                        CustomBreakSpeedListener.markInstantBreak(b);
                        e.getPlayer().breakBlock(b);
                    }, (b) -> {
                        treeCapitatingPlayers.remove(e.getPlayer().getUniqueId());
                        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                            List<Block> leaves = leafOrigin == null ? new ArrayList<>() : new ArrayList<>(BlockUtils.getBlockVein(leafOrigin, treeCapitatorLeavesLimit, bl -> isLeaves(bl, BlockUtils.getBlockType(bl)) && (!(bl.getBlockData() instanceof Leaves l) || l.getDistance() > 3), treeCapitatorLeavesScanArea));
                            Collections.shuffle(leaves);
                            BlockUtils.processBlocksDelayed(e.getPlayer(), leaves, (p) -> true, BlockUtils::decayBlock, null);
                        }, 20L);
                    });
                }
            }
            else
                BlockUtils.processBlocksPulse(e.getPlayer(), e.getBlock(), vein, this::playerHasAxe, b -> {
                    if (profile.isTreeCapitatorInstantPickup()) LootListener.setInstantPickup(b, e.getPlayer());
                    CustomBreakSpeedListener.markInstantBreak(b);
                    e.getPlayer().breakBlock(b);
                }, (b) -> {
                    treeCapitatingPlayers.remove(e.getPlayer().getUniqueId());
                    ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                        List<Block> leaves = leafOrigin == null ? new ArrayList<>() : new ArrayList<>(BlockUtils.getBlockVein(leafOrigin, treeCapitatorLeavesLimit, bl -> isLeaves(bl, BlockUtils.getBlockType(bl)) && (!(bl.getBlockData() instanceof Leaves l) || l.getDistance() > 3), treeCapitatorLeavesScanArea));
                        Collections.shuffle(leaves);
                        BlockUtils.processBlocksDelayed(e.getPlayer(), leaves, (p) -> true, BlockUtils::decayBlock, null);
                    }, 20L);
                });
            Timer.setCooldownIgnoreIfPermission(e.getPlayer(), profile.getTreeCapitatorCooldown() * 50, "woodcutting_tree_capitator");
        } else {
            if (Debugger.isDebuggerEnabled(e.getPlayer(), "tree_capitator")) {
                if (!e.getPlayer().isSneaking()) Debugger.send(e.getPlayer(), "tree_capitator", "&cPlayer is not sneaking");
                if (treeCapitatingPlayers.contains(e.getPlayer().getUniqueId())) Debugger.send(e.getPlayer(), "tree_capitator", "&cPlayer is already processing a tree (This is only of concern if this message is sent once. If it is spammed, it is expected tree capitator worked here)");
                if (!profile.isTreeCapitatorUnlocked()) Debugger.send(e.getPlayer(), "tree_capitator", "&cPlayer doesn't have tree capitator unlocked");
                if (!profile.getTreeCapitatorValidBlocks().contains(e.getBlock().getType().toString())) Debugger.send(e.getPlayer(), "tree_capitator", "&cPlayer cannot tree capitate " + e.getBlock().getType());
                if (!Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "woodcutting_tree_capitator")) Debugger.send(e.getPlayer(), "tree_capitator", "&cTree capitator is on cooldown");
                if (!isTree(e.getBlock())) Debugger.send(e.getPlayer(), "tree_capitator", "&cThis block (" + e.getBlock().getType() + ") is not considered part of a tree");
                if (WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_ABILITIES_TREECAPITATOR)) Debugger.send(e.getPlayer(), "tree_capitator", "&cAttempted tree capitation in worldguard blocked area");
            }

            if (!Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "woodcutting_tree_capitator")) Timer.sendCooldownStatus(e.getPlayer(), "woodcutting_tree_capitator", TranslationManager.getTranslation("ability_tree_capitator"));
        }
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
        String type = BlockUtils.getBlockType(e.getBlock());
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || !isLog(e.getBlock(), type) ||
                !BlockUtils.canReward(e.getBlock()) || WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_WOODCUTTING) ||
                e.getBlock().getState() instanceof Container) return;
        double dropMultiplier = AccumulativeStatManager.getCachedStats("WOODCUTTING_DROP_MULTIPLIER", e.getPlayer(), 10000, true);
        // multiply any applicable prepared drops and grant exp for them. After the extra drops from a BlockBreakEvent the drops are cleared
        ItemUtils.multiplyItems(LootListener.getPreparedExtraDrops(e.getBlock()), 1 + dropMultiplier, forgivingDropMultipliers, (i) -> dropsExpValues.containsKey(type));

        double expQuantity = 0;
        for (ItemStack i : LootListener.getPreparedExtraDrops(e.getBlock())){
            if (ItemUtils.isEmpty(i)) continue;
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
            expQuantity += dropsExpValues.getOrDefault(ItemUtils.getItemType(i.getItemStack()), 0D) * i.getItemStack().getAmount();
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
