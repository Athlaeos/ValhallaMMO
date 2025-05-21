package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.event.PlayerBlocksDropItemsEvent;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.listeners.LootListener;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.DiggingProfile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.*;
import me.athlaeos.valhallammo.version.DiggingArchaeologyExtension;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DiggingSkill extends Skill implements Listener {
    private final Map<Material, Double> dropsExpValues = new HashMap<>();

    private boolean forgivingDropMultipliers = true; // if false, depending on drop multiplier, drops may be reduced to 0. If true, this will be at least 1

    public DiggingSkill(String type) {
        super(type);
    }

    @Override
    public void loadConfiguration() {
        ValhallaMMO.getInstance().save("skills/digging_progression.yml");
        ValhallaMMO.getInstance().save("skills/digging.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/digging.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/digging_progression.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);

        forgivingDropMultipliers = skillConfig.getBoolean("forgiving_multipliers");

        Collection<String> invalidMaterials = new HashSet<>();
        ConfigurationSection blockBreakSection = progressionConfig.getConfigurationSection("experience.digging_break");
        if (blockBreakSection != null){
            for (String key : blockBreakSection.getKeys(false)){
                try {
                    Material block = Material.valueOf(key);
                    double reward = progressionConfig.getDouble("experience.digging_break." + key);
                    dropsExpValues.put(block, reward);
                } catch (IllegalArgumentException ignored){
                    invalidMaterials.add(key);
                }
            }
        }
        if (!invalidMaterials.isEmpty()) {
            ValhallaMMO.logWarning("The following materials in skills/digging_progression.yml do not exist, no exp values set (ignore warning if your version does not have these materials)");
            ValhallaMMO.logWarning(String.join(", ", invalidMaterials));
        }

        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20))
            ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new DiggingArchaeologyExtension(this), ValhallaMMO.getInstance());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) ||
                !dropsExpValues.containsKey(e.getBlock().getType()) || !BlockUtils.canReward(e.getBlock()) ||
                WorldGuardHook.inDisabledRegion(e.getBlock().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_DIGGING)) return;

        if (!hasPermissionAccess(e.getPlayer())) return;
        DiggingProfile profile = ProfileCache.getOrCache(e.getPlayer(), DiggingProfile.class);
        e.setExpToDrop(e.getExpToDrop() + Utils.randomAverage(profile.getBlockExperienceRate()));
        LootListener.addPreparedLuck(e.getBlock(), AccumulativeStatManager.getCachedStats("DIGGING_LUCK", e.getPlayer(), 10000, true));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void lootTableDrops(BlockBreakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) ||
                !dropsExpValues.containsKey(e.getBlock().getType()) || !BlockUtils.canReward(e.getBlock()) ||
                WorldGuardHook.inDisabledRegion(e.getBlock().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_DIGGING) ||
                e.getBlock().getState() instanceof Container) return;
        double dropMultiplier = AccumulativeStatManager.getCachedStats("DIGGING_DROP_MULTIPLIER", e.getPlayer(), 10000, true);
        // multiply any applicable prepared drops and grant exp for them. After the extra drops from a BlockBreakEvent the drops are cleared
        ItemUtils.multiplyItems(LootListener.getPreparedExtraDrops(e.getBlock()), 1 + dropMultiplier, forgivingDropMultipliers, (i) -> dropsExpValues.containsKey(i.getType()));

        double expQuantity = 0;
        for (ItemStack i : LootListener.getPreparedExtraDrops(e.getBlock())){
            if (ItemUtils.isEmpty(i)) continue;
            expQuantity += dropsExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
        }
        addEXP(e.getPlayer(), expQuantity, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemsDropped(BlockDropItemEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlockState().getWorld().getName()) || !BlockUtils.canReward(e.getBlockState()) ||
                WorldGuardHook.inDisabledRegion(e.getBlock().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_DIGGING) ||
                !dropsExpValues.containsKey(e.getBlockState().getType()) ||
                e.getBlockState() instanceof Container) return;
        double dropMultiplier = AccumulativeStatManager.getCachedStats("DIGGING_DROP_MULTIPLIER", e.getPlayer(), 10000, true);
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

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return DiggingProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 30;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_DIGGING)) return;
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= (1 + AccumulativeStatManager.getCachedStats("DIGGING_EXP_GAIN", p, 10000, true));
        }
        super.addEXP(p, amount, silent, reason);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onOtherBlockDrops(PlayerBlocksDropItemsEvent e){
        double exp = 0;
        for (Block b : e.getBlocksAndItems().keySet()){
            for (ItemStack i : e.getBlocksAndItems().getOrDefault(b, new ArrayList<>())){
                if (ItemUtils.isEmpty(i)) continue;
                exp += dropsExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
            }
        }
        addEXP(e.getPlayer(), exp, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    public Map<Material, Double> getDropsExpValues() {
        return dropsExpValues;
    }
}
