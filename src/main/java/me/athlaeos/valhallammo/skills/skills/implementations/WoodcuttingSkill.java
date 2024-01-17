package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.listeners.CustomBreakSpeedListener;
import me.athlaeos.valhallammo.listeners.LootListener;
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
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class WoodcuttingSkill extends Skill implements Listener {
    private final Map<Material, Double> dropsExpValues = new HashMap<>();
    private final Map<Material, Double> stripExpValues = new HashMap<>();

    private final int treeCapitatorLimit;
    private final boolean treeCapitatorInstant;
    private final int treeCapitatorLeavesLimit;

    private final int treeScanLimit;

    private final boolean forgivingDropMultipliers; // if false, depending on drop multiplier, drops may be reduced to 0. If true, this will be at least 1

    public WoodcuttingSkill(String type) {
        super(type);
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

        Collection<String> invalidMaterials = new HashSet<>();
        ConfigurationSection blockBreakSection = progressionConfig.getConfigurationSection("experience.woodcutting_break");
        if (blockBreakSection != null){
            for (String key : blockBreakSection.getKeys(false)){
                try {
                    Material block = Material.valueOf(key);
                    double reward = progressionConfig.getDouble("experience.woodcutting_break." + key);
                    dropsExpValues.put(block, reward);
                } catch (IllegalArgumentException ignored){
                    invalidMaterials.add(key);
                }
            }
        }

        ConfigurationSection blockStripSection = progressionConfig.getConfigurationSection("experience.woodcutting_strip");
        if (blockStripSection != null){
            for (String key : blockStripSection.getKeys(false)){
                try {
                    Material block = Material.valueOf(key);
                    double reward = progressionConfig.getDouble("experience.woodcutting_strip." + key);
                    stripExpValues.put(block, reward);
                } catch (IllegalArgumentException ignored){
                    invalidMaterials.add(key);
                }
            }
        }
        if (!invalidMaterials.isEmpty()) {
            ValhallaMMO.logWarning("The following materials in skills/woodcutting_progression.yml do not exist, no exp values set (ignore warning if your version does not have these materials)");
            ValhallaMMO.logWarning(String.join(", ", invalidMaterials));
        }

        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    private final int[][] treeCapitatorScanArea = MathUtils.getOffsetsBetweenPoints(new int[]{-1, 0, -1}, new int[]{1, 1, 1});
    private final int[][] treeCapitatorLeavesScanArea = MathUtils.getOffsetsBetweenPoints(new int[]{-1, -1, -1}, new int[]{1, 1, 1});

    private final Collection<UUID> treeCapitatingPlayers = new HashSet<>();

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled() || !Tag.LOGS.isTagged(e.getBlock().getType()) ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_WOODCUTTING) ||
                !dropsExpValues.containsKey(e.getBlock().getType()) || e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        WoodcuttingProfile profile = ProfileCache.getOrCache(e.getPlayer(), WoodcuttingProfile.class);

        double woodCuttingLuck = AccumulativeStatManager.getCachedStats("WOODCUTTING_LUCK", e.getPlayer(), 10000, true);
        if (BlockUtils.canReward(e.getBlock())){
            e.setExpToDrop(e.getExpToDrop() + Utils.randomAverage(profile.getBlockExperienceRate()));
            LootListener.addPreparedLuck(e.getBlock(), woodCuttingLuck);
        }

        if (!treeCapitatingPlayers.contains(e.getPlayer().getUniqueId()) &&
                profile.isTreeCapitatorUnlocked() &&
                profile.getTreeCapitatorValidBlocks().contains(e.getBlock().getType().toString()) &&
                Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "woodcutting_tree_capitator") &&
                isTree(e.getBlock()) &&
                !WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_ABILITIES_TREECAPITATOR)){
            Collection<Block> vein = BlockUtils.getBlockVein(e.getBlock(), treeCapitatorLimit, b -> Tag.LOGS.isTagged(b.getType()), treeCapitatorScanArea);
            if (vein.size() > profile.getTreeCapitatorLimit()) {
                Timer.setCooldownIgnoreIfPermission(e.getPlayer(), profile.getTreeCapitatorCooldown(), "woodcutting_tree_capitator");
                return;
            }
            treeCapitatingPlayers.add(e.getPlayer().getUniqueId());
            e.setCancelled(true);

            Block leafOrigin = getTreeLeafOrigin(e.getBlock());
            Collection<Block> leaves = leafOrigin == null ? new HashSet<>() : BlockUtils.getBlockVein(leafOrigin, treeCapitatorLeavesLimit, b -> Tag.LEAVES.isTagged(b.getType()), treeCapitatorLeavesScanArea);

            if (treeCapitatorInstant)
                BlockUtils.processBlocks(e.getPlayer(), vein, p -> {
                    EntityProperties properties = EntityCache.getAndCacheProperties(p);
                    return properties.getMainHand() != null && EquipmentClass.getMatchingClass(properties.getMainHand().getMeta()) == EquipmentClass.AXE;
                    }, b -> {
                    if (profile.isTreeCapitatorInstantPickup()) LootListener.setInstantPickup(b, e.getPlayer());
                    CustomBreakSpeedListener.markInstantBreak(b);
                    e.getPlayer().breakBlock(b);
                }, (b) -> {
                    treeCapitatingPlayers.remove(e.getPlayer().getUniqueId());
                    BlockUtils.processBlocksDelayed(e.getPlayer(), leaves, (p) -> true, bl -> {
                        LootListener.addPreparedLuck(bl, woodCuttingLuck);
                        LootListener.setResponsibleBreaker(bl, e.getPlayer());
                        BlockUtils.decayBlock(bl);
                    }, null);
                });
            else
                BlockUtils.processBlocksPulse(e.getPlayer(), e.getBlock(), vein, p -> {
                    EntityProperties properties = EntityCache.getAndCacheProperties(p);
                    return properties.getMainHand() != null && EquipmentClass.getMatchingClass(properties.getMainHand().getMeta()) == EquipmentClass.AXE;
                }, b -> {
                    if (profile.isTreeCapitatorInstantPickup()) LootListener.setInstantPickup(b, e.getPlayer());
                    CustomBreakSpeedListener.markInstantBreak(b);
                    e.getPlayer().breakBlock(b);
                }, (b) -> {
                    treeCapitatingPlayers.remove(e.getPlayer().getUniqueId());
                    BlockUtils.processBlocksDelayed(e.getPlayer(), leaves, (p) -> true, bl -> {
                        LootListener.addPreparedLuck(bl, woodCuttingLuck);
                        LootListener.setResponsibleBreaker(bl, e.getPlayer());
                        BlockUtils.decayBlock(bl);
                    }, null);
                });
            Timer.setCooldownIgnoreIfPermission(e.getPlayer(), profile.getTreeCapitatorCooldown(), "woodcutting_tree_capitator");
        }
    }

    private final int[][] treeScanArea = MathUtils.getOffsetsBetweenPoints(new int[]{-1, 1, -1}, new int[]{1, 1, 1});

    /**
     * A tree is defined as any log with any leaves connected somewhere above it.
     */
    private boolean isTree(Block b){
        Collection<Block> treeBlocks = BlockUtils.getBlockVein(b, treeScanLimit, l -> Tag.LOGS.isTagged(l.getType()) || Tag.LEAVES.isTagged(l.getType()), treeScanArea);
        return treeBlocks.stream().anyMatch(l -> Tag.LOGS.isTagged(l.getType())) && treeBlocks.stream().anyMatch(l -> Tag.LEAVES.isTagged(l.getType()));
    }

    // checks up to 48 blocks above the log mined for a leaf block which might be used as origin
    private Block getTreeLeafOrigin(Block b){
        for (int i = 1; i < 48; i++){
            if (b.getLocation().getY() + i >= b.getWorld().getMaxHeight()) break;
            Block blockAt = b.getLocation().add(0, i, 0).getBlock();
            if (Tag.LEAVES.isTagged(blockAt.getType())) return blockAt;
            if (!blockAt.getType().isAir() && !Tag.LOGS.isTagged(blockAt.getType())) return null;
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void lootTableDrops(BlockBreakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled() || !Tag.LOGS.isTagged(e.getBlock().getType()) ||
                !BlockUtils.canReward(e.getBlock()) || WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_WOODCUTTING)) return;
        double dropMultiplier = AccumulativeStatManager.getCachedStats("WOODCUTTING_DROP_MULTIPLIER", e.getPlayer(), 10000, true);
        // multiply any applicable prepared drops and grant exp for them. After the extra drops from a BlockBreakEvent the drops are cleared
        ItemUtils.multiplyItems(LootListener.getPreparedExtraDrops(e.getBlock()), 1 + dropMultiplier, forgivingDropMultipliers, (i) -> dropsExpValues.containsKey(i.getType()));

        double expQuantity = 0;
        for (ItemStack i : LootListener.getPreparedExtraDrops(e.getBlock())){
            if (ItemUtils.isEmpty(i)) continue;
            expQuantity += dropsExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
        }
        addEXP(e.getPlayer(), expQuantity, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemsDropped(BlockDropItemEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlockState().getWorld().getName()) || e.isCancelled() || !BlockUtils.canReward(e.getBlockState()) ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_WOODCUTTING)) return;
        double dropMultiplier = AccumulativeStatManager.getCachedStats("WOODCUTTING_DROP_MULTIPLIER", e.getPlayer(), 10000, true);
        // multiply the item drops from the event itself and grant exp for the initial items and extra drops
        List<ItemStack> extraDrops = ItemUtils.multiplyDrops(e.getItems(), 1 + dropMultiplier, forgivingDropMultipliers, (i) -> dropsExpValues.containsKey(i.getItemStack().getType()));
        if (!extraDrops.isEmpty()) LootListener.prepareBlockDrops(e.getBlock(), extraDrops);

        double expQuantity = 0;
        for (Item i : e.getItems()){
            if (ItemUtils.isEmpty(i.getItemStack())) continue;
            expQuantity += dropsExpValues.getOrDefault(i.getItemStack().getType(), 0D) * i.getItemStack().getAmount();
        }
        for (ItemStack i : extraDrops){
            if (ItemUtils.isEmpty(i)) continue;
            expQuantity += dropsExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
        }
        addEXP(e.getPlayer(), expQuantity, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlaced(BlockPlaceEvent e) {
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled() ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_WOODCUTTING)) return;
        Block b = e.getBlock();
        if (stripExpValues.containsKey(b.getType()) && dropsExpValues.containsKey(e.getBlockReplacedState().getType())) {
            double amount = stripExpValues.get(b.getType());
            if (amount > 0) addEXP(e.getPlayer(), amount, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
        } else if (Tag.SAPLINGS.isTagged(b.getType()) && b.getBlockData() instanceof Sapling s){
            WoodcuttingProfile profile = ProfileCache.getOrCache(e.getPlayer(), WoodcuttingProfile.class);
            s.setStage(Math.min(Math.max(0, Utils.randomAverage(profile.getInstantGrowthRate())), s.getMaximumStage() - 1));
            e.getBlock().setBlockData(s);
        }
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
    public boolean isExperienceScaling() {
        return true;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_WOODCUTTING)) return;
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= (1 + AccumulativeStatManager.getStats("WOODCUTTING_EXP_GAIN", p, true));
        }
        super.addEXP(p, amount, silent, reason);
    }

    public Map<Material, Double> getDropsExpValues() {
        return dropsExpValues;
    }
}
