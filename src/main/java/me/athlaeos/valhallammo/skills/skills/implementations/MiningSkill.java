package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.animations.AnimationRegistry;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.listeners.*;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MiningProfile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.*;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MiningSkill extends Skill implements Listener {
    private final Map<Material, Double> dropsExpValues = new HashMap<>();
    private final double miningExpMultiplier;
    private final double blastingExpMultiplier;

    private final int veinMiningLimit;
    private final boolean veinMiningInstantPickup;

    private final boolean forgivingDropMultipliers; // if false, depending on drop multiplier, drops may be reduced to 0. If true, this will be at least 1
    private final boolean tntPreventChaining;

    private Animation drillingAnimation = AnimationRegistry.DRILLING_ACTIVE;
    private final String drillingOn;
    private final Sound drillingActivationSound;

    public void setDrillingAnimation(Animation drillingAnimation) {
        this.drillingAnimation = drillingAnimation;
    }

    public MiningSkill(String type) {
        super(type);
        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/mining.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/mining_progression.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);

        miningExpMultiplier = progressionConfig.getDouble("experience.exp_multiplier_mine");
        blastingExpMultiplier = progressionConfig.getDouble("experience.exp_multiplier_blast");
        veinMiningLimit = skillConfig.getInt("break_limit_vein_mining");
        veinMiningInstantPickup = skillConfig.getBoolean("instant_pickup_vein_mining");
        forgivingDropMultipliers = skillConfig.getBoolean("forgiving_multipliers");
        tntPreventChaining = skillConfig.getBoolean("remove_tnt_chaining");
        drillingOn = TranslationManager.translatePlaceholders(skillConfig.getString("drilling_toggle_on"));
        drillingActivationSound = Catch.catchOrElse(() -> Sound.valueOf(skillConfig.getString("drilling_enable_sound")), null, "Invalid drilling activation sound given in skills/mining.yml drilling_enable_sound");

        ConfigurationSection blockBreakSection = progressionConfig.getConfigurationSection("experience.mining_break");
        if (blockBreakSection != null){
            for (String key : blockBreakSection.getKeys(false)){
                try {
                    Material block = Material.valueOf(key);
                    double reward = progressionConfig.getDouble("experience.mining_break." + key);
                    dropsExpValues.put(block, reward);
                } catch (IllegalArgumentException ignored){
                    ValhallaMMO.logWarning("invalid block type given:" + key + " for the block break rewards in skills/mining_progression.yml, no reward set for this type until corrected.");
                }
            }
        }

        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    private final int[][] veinMiningScanArea = new int[][]{
            {-1, -1, -1}, {-1, -1, 0}, {-1, -1, 1},
            {-1, 0, -1}, {-1, 0, 0}, {-1, 0, 1},
            {-1, 1, -1}, {-1, 1, 0}, {-1, 1, 1},
            {0, -1, -1}, {0, -1, 0}, {0, -1, 1},
            {0, 0, -1}, /*{0, 0, 0}, */{0, 0, 1},
            {0, 1, -1}, {0, 1, 0}, {0, 1, 1},
            {1, -1, -1}, {1, -1, 0}, {1, -1, 1},
            {1, 0, -1}, {1, 0, 0}, {1, 0, 1},
            {1, 1, -1}, {1, 1, 0}, {1, 1, 1}
    };

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled()) return;
        MiningProfile profile = ProfileCache.getOrCache(e.getPlayer(), MiningProfile.class);
        if (profile.getUnbreakableBlocks().contains(e.getBlock().getType().toString())) {
            e.setCancelled(true);
            return;
        }
        if (!dropsExpValues.containsKey(e.getBlock().getType())) return;
        int experience = e.getExpToDrop() + Utils.randomAverage(profile.getBlockExperienceRate());
        experience = Utils.randomAverage(experience * (1D + profile.getBlockExperienceMultiplier()));
        e.setExpToDrop(experience);

        if (profile.isVeinMiningUnlocked() && profile.getVeinMinerValidBlocks().contains(e.getBlock().getType().toString()) && Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "mining_vein_miner")){
            List<Block> vein = BlockUtils.getBlockVein(e.getBlock(), veinMiningLimit, b -> b.getType() == e.getBlock().getType(), veinMiningScanArea);
            e.setCancelled(true);
            if (profile.isVeinMiningInstantPickup())
                BlockUtils.processBlocks(e.getPlayer(), vein, p -> {
                    EntityProperties properties = EntityCache.getAndCacheProperties(p);
                    return properties.getMainHand() != null && EquipmentClass.getMatchingClass(properties.getMainHand().getMeta()) == EquipmentClass.PICKAXE;
                    }, b -> e.getPlayer().breakBlock(b), null);
            else
                BlockUtils.processBlocksPulse(e.getPlayer(), e.getBlock(), vein, p -> {
                    EntityProperties properties = EntityCache.getAndCacheProperties(p);
                    return properties.getMainHand() != null && EquipmentClass.getMatchingClass(properties.getMainHand().getMeta()) == EquipmentClass.PICKAXE;
                }, b -> {
                    if (veinMiningInstantPickup) LootListener.setInstantPickup(b, e.getPlayer());
                    e.getPlayer().breakBlock(b);
                }, null);
            Timer.setCooldownIgnoreIfPermission(e.getPlayer(), profile.getVeinMiningCooldown(), "mining_vein_miner");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemsDropped(BlockDropItemEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled() || BlockStore.isPlaced(e.getBlock())) return;
        double dropMultiplier = AccumulativeStatManager.getCachedStats("MINING_DROP_MULTIPLIER", e.getPlayer(), 10000, true);
        List<ItemStack> extraDrops = ItemUtils.multiplyDrops(e.getItems(), 1 + dropMultiplier, forgivingDropMultipliers, (i) -> dropsExpValues.containsKey(i.getItemStack().getType()));
        LootListener.prepareBlockDrops(e.getBlock(), extraDrops);

        double expQuantity = 0;
        for (Item i : e.getItems()){
            if (ItemUtils.isEmpty(i.getItemStack())) continue;
            if (!dropsExpValues.containsKey(i.getItemStack().getType())) continue;
            expQuantity += dropsExpValues.get(i.getItemStack().getType());
        }
        for (ItemStack i : extraDrops){
            if (ItemUtils.isEmpty(i)) continue;
            if (!dropsExpValues.containsKey(i.getType())) continue;
            expQuantity += dropsExpValues.get(i.getType());
        }
        addEXP(e.getPlayer(), expQuantity * miningExpMultiplier, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) ||
                !e.getPlayer().isSneaking() || e.getHand() == EquipmentSlot.OFF_HAND ||
                e.useItemInHand() == Event.Result.DENY || !Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "mining_drilling_cooldown") ||
                !Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "mining_drilling_duration") ||
                (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK)) return;
        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        if (ItemUtils.isEmpty(hand) || !hand.getType().toString().endsWith("_PICKAXE")) return;
        MiningProfile profile = ProfileCache.getOrCache(e.getPlayer(), MiningProfile.class);
        if (!profile.isDrillingUnlocked() || profile.getDrillingDuration() < 0) return;
        Timer.setCooldownIgnoreIfPermission(e.getPlayer(), profile.getDrillingCooldown() * 50, "mining_drilling_cooldown");
        Timer.setCooldown(e.getPlayer().getUniqueId(), profile.getDrillingDuration() * 50, "mining_drilling_duration");
        Utils.sendActionBar(e.getPlayer(), drillingOn);
        if (drillingAnimation != null) drillingAnimation.animate(e.getPlayer(), e.getPlayer().getLocation(), e.getPlayer().getEyeLocation().getDirection(), 0);
        if (drillingActivationSound != null) e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), drillingActivationSound, 1F, 1F);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled() || !(e.getEntity() instanceof TNTPrimed tnt) || tnt.getSource() == null) return;
        Player responsible = null;
        if (tnt.getSource() instanceof Player p) responsible = p;
        else if (tnt.getSource() instanceof AbstractArrow a && a.getShooter() instanceof Player p) responsible = p;
        if (responsible == null) return;

        MiningProfile profile = ProfileCache.getOrCache(responsible, MiningProfile.class);
        ItemStack normalPickaxe = new ItemStack(Material.IRON_PICKAXE);
        if (profile.getBlastFortuneLevel() > 0) normalPickaxe.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, profile.getBlastFortuneLevel());
        else if (profile.getBlastFortuneLevel() < 0) normalPickaxe.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
        double blastingDropMultiplier = AccumulativeStatManager.getCachedStats("BLASTING_DROP_MULTIPLIER", responsible, 10000, true);

        double exp = 0;
        for (Block b : new HashSet<>(e.blockList())){
            if (b.getType().isAir() || (!tntPreventChaining && b.getType() == Material.TNT) || BlockStore.isPlaced(b)) continue;
            e.blockList().remove(b);

            List<ItemStack> predictedDrops = new ArrayList<>(b.getDrops(normalPickaxe));
            List<ItemStack> newDrops = ItemUtils.multiplyItems(predictedDrops, 1 + blastingDropMultiplier, forgivingDropMultipliers,  (i) -> dropsExpValues.containsKey(i.getType()));
            LootListener.markExploded(b);
            LootListener.setFortuneLevel(e.getEntity(), profile.getBlastFortuneLevel());
            LootListener.prepareBlockDrops(b, newDrops);
            LootListener.setEntityOwner(e.getEntity(), responsible);
            if (profile.isBlastingInstantPickup()) LootListener.setInstantPickup(b, responsible);
            b.setType(Material.AIR);
            for (ItemStack i : newDrops){
                if (ItemUtils.isEmpty(i)) continue;
                if (!dropsExpValues.containsKey(i.getType())) continue;
                exp += dropsExpValues.get(i.getType());
            }
        }
        addEXP(responsible, exp * blastingExpMultiplier, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler
    public void onTNTDamage(EntityDamageByEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled() || !(e.getEntity() instanceof Player p) || !(e.getDamager() instanceof TNTPrimed)) return;
        MiningProfile profile = ProfileCache.getOrCache(p, MiningProfile.class);
        e.setDamage(e.getDamage() * (1 - profile.getTntDamageReduction()));
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return MiningProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 20;
    }

    @Override
    public boolean isExperienceScaling() {
        return true;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= (1 + AccumulativeStatManager.getStats("MINING_EXP_GAIN", p, true));
        }
        super.addEXP(p, amount, silent, reason);
    }

    public Map<Material, Double> getDropsExpValues() {
        return dropsExpValues;
    }
}
