package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.animations.AnimationRegistry;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
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
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MiningProfile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class MiningSkill extends Skill implements Listener {
    private final Map<Material, Double> dropsExpValues = new HashMap<>();
    private double miningExpMultiplier = 1;
    private double blastingExpMultiplier = 1;

    private int veinMiningLimit = 64;
    private boolean veinMiningInstant = true;

    private boolean forgivingDropMultipliers = true; // if false, depending on drop multiplier, drops may be reduced to 0. If true, this will be at least 1
    private boolean tntPreventChaining = true;

    private Animation drillingAnimation = AnimationRegistry.DRILLING_ACTIVE;
    private String drillingOn = null;
    private Sound drillingActivationSound = null;

    public void setDrillingAnimation(Animation drillingAnimation) {
        this.drillingAnimation = drillingAnimation;
    }

    public MiningSkill(String type) {
        super(type);
    }

    @Override
    public void loadConfiguration() {
        ValhallaMMO.getInstance().save("skills/mining_progression.yml");
        ValhallaMMO.getInstance().save("skills/mining.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/mining.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/mining_progression.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);

        miningExpMultiplier = progressionConfig.getDouble("experience.exp_multiplier_mine");
        blastingExpMultiplier = progressionConfig.getDouble("experience.exp_multiplier_blast");
        veinMiningLimit = skillConfig.getInt("break_limit_vein_mining");
        veinMiningInstant = skillConfig.getBoolean("vein_mining_instant");
        forgivingDropMultipliers = skillConfig.getBoolean("forgiving_multipliers");
        tntPreventChaining = skillConfig.getBoolean("remove_tnt_chaining");
        drillingOn = TranslationManager.translatePlaceholders(skillConfig.getString("drilling_toggle_on"));
        drillingActivationSound = Catch.catchOrElse(() -> Sound.valueOf(skillConfig.getString("drilling_enable_sound")), null, "Invalid drilling activation sound given in skills/mining.yml drilling_enable_sound");

        Collection<String> invalidMaterials = new HashSet<>();
        ConfigurationSection blockBreakSection = progressionConfig.getConfigurationSection("experience.mining_break");
        if (blockBreakSection != null){
            for (String key : blockBreakSection.getKeys(false)){
                try {
                    Material block = Material.valueOf(key);
                    double reward = progressionConfig.getDouble("experience.mining_break." + key);
                    dropsExpValues.put(block, reward);
                } catch (IllegalArgumentException ignored){
                    invalidMaterials.add(key);
                }
            }
        }
        if (!invalidMaterials.isEmpty()) {
            ValhallaMMO.logWarning("The following materials in skills/mining_progression.yml do not exist, no exp values set (ignore warning if your version does not have these materials)");
            ValhallaMMO.logWarning(String.join(", ", invalidMaterials));
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

    private final Collection<UUID> veinMiningPlayers = new HashSet<>();

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) ||
                WorldGuardHook.inDisabledRegion(e.getBlock().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_MINING) ||
                !dropsExpValues.containsKey(e.getBlock().getType()) || e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        MiningProfile profile = ProfileCache.getOrCache(e.getPlayer(), MiningProfile.class);
        if (profile.getUnbreakableBlocks().contains(e.getBlock().getType().toString())) {
            e.setCancelled(true);
            return;
        }
        if (!hasPermissionAccess(e.getPlayer())) return;
        if (BlockUtils.canReward(e.getBlock())) {
            int experience = e.getExpToDrop() + Utils.randomAverage(profile.getBlockExperienceRate());
            experience = Utils.randomAverage(experience * (1D + profile.getBlockExperienceMultiplier()));
            e.setExpToDrop(experience);
        }
        LootListener.addPreparedLuck(e.getBlock(), AccumulativeStatManager.getCachedStats("MINING_LUCK", e.getPlayer(), 10000, true));

        if (e.getPlayer().isSneaking() && !veinMiningPlayers.contains(e.getPlayer().getUniqueId()) &&
                profile.isVeinMiningUnlocked() && profile.getVeinMinerValidBlocks().contains(e.getBlock().getType().toString()) &&
                Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "mining_vein_miner") &&
                !WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_ABILITIES_VEINMINER)){
            Collection<Block> vein = BlockUtils.getBlockVein(e.getBlock(), veinMiningLimit, b -> b.getType() == e.getBlock().getType(), veinMiningScanArea);
            veinMiningPlayers.add(e.getPlayer().getUniqueId());
            e.setCancelled(true);

            if (veinMiningInstant)
                BlockUtils.processBlocks(e.getPlayer(), vein, p -> {
                    EntityProperties properties = EntityCache.getAndCacheProperties(p);
                    return properties.getMainHand() != null && EquipmentClass.getMatchingClass(properties.getMainHand().getMeta()) == EquipmentClass.PICKAXE;
                    }, b -> {
                    if (profile.isVeinMiningInstantPickup()) LootListener.setInstantPickup(b, e.getPlayer());
                    CustomBreakSpeedListener.markInstantBreak(b);
                    e.getPlayer().breakBlock(b);
                }, (b) -> veinMiningPlayers.remove(b.getUniqueId()));
            else
                BlockUtils.processBlocksPulse(e.getPlayer(), e.getBlock(), vein, p -> {
                    EntityProperties properties = EntityCache.getAndCacheProperties(p);
                    return properties.getMainHand() != null && EquipmentClass.getMatchingClass(properties.getMainHand().getMeta()) == EquipmentClass.PICKAXE;
                }, b -> {
                    if (profile.isVeinMiningInstantPickup()) LootListener.setInstantPickup(b, e.getPlayer());
                    CustomBreakSpeedListener.markInstantBreak(b);
                    e.getPlayer().breakBlock(b);
                }, (b) -> veinMiningPlayers.remove(b.getUniqueId()));
            Timer.setCooldownIgnoreIfPermission(e.getPlayer(), profile.getVeinMiningCooldown() * 50, "mining_vein_miner");
        } else {
            if (!Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "mining_vein_miner")) Timer.sendCooldownStatus(e.getPlayer(), "mining_vein_miner", TranslationManager.getTranslation("ability_vein_miner"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void lootTableDrops(BlockBreakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || !BlockUtils.canReward(e.getBlock()) ||
                WorldGuardHook.inDisabledRegion(e.getBlock().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_MINING) ||
                !dropsExpValues.containsKey(e.getBlock().getType()) ||
                e.getBlock().getState() instanceof Container) return;
        double dropMultiplier = AccumulativeStatManager.getCachedStats("MINING_DROP_MULTIPLIER", e.getPlayer(), 10000, true);
        // multiply any applicable prepared drops and grant exp for them. After the extra drops from a BlockBreakEvent the drops are cleared
        ItemUtils.multiplyItems(LootListener.getPreparedExtraDrops(e.getBlock()), 1 + dropMultiplier, forgivingDropMultipliers, (i) -> dropsExpValues.containsKey(i.getType()));

        double expQuantity = 0;
        for (ItemStack i : LootListener.getPreparedExtraDrops(e.getBlock())){
            if (ItemUtils.isEmpty(i)) continue;
            expQuantity += dropsExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
        }
        addEXP(e.getPlayer(), expQuantity * miningExpMultiplier, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemsDropped(BlockDropItemEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlockState().getWorld().getName()) || !BlockUtils.canReward(e.getBlockState()) ||
                WorldGuardHook.inDisabledRegion(e.getBlock().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_MINING) ||
                !dropsExpValues.containsKey(e.getBlockState().getType()) ||
                e.getBlockState() instanceof Container) return;
        double dropMultiplier = AccumulativeStatManager.getCachedStats("MINING_DROP_MULTIPLIER", e.getPlayer(), 10000, true);
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
        addEXP(e.getPlayer(), expQuantity * miningExpMultiplier, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) ||
                !e.getPlayer().isSneaking() || e.getHand() == EquipmentSlot.OFF_HAND ||
                e.useItemInHand() == Event.Result.DENY || !Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "mining_drilling_cooldown") ||
                !Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "mining_drilling_duration") ||
                (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_MINING) ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_ABILITIES_DRILLING)) {
            if (!Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "mining_drilling_cooldown")) Timer.sendCooldownStatus(e.getPlayer(), "mining_drilling_cooldown", TranslationManager.getTranslation("ability_drilling"));
            return;
        }
        if (!hasPermissionAccess(e.getPlayer())) return;
        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        if (ItemUtils.isEmpty(hand) || !hand.getType().toString().endsWith("_PICKAXE")) return;
        MiningProfile profile = ProfileCache.getOrCache(e.getPlayer(), MiningProfile.class);
        if (!profile.isDrillingUnlocked() || profile.getDrillingDuration() < 0) return;
        Timer.setCooldownIgnoreIfPermission(e.getPlayer(), profile.getDrillingCooldown() * 50, "mining_drilling_cooldown");
        Timer.setCooldown(e.getPlayer().getUniqueId(), profile.getDrillingDuration() * 50, "mining_drilling_duration");
        Utils.sendActionBar(e.getPlayer(), drillingOn);
        if (drillingAnimation != null) drillingAnimation.animate(e.getPlayer(), e.getPlayer().getLocation(), e.getPlayer().getEyeLocation().getDirection(), 0);
        if (drillingActivationSound != null) e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), drillingActivationSound, 1F, 1F);

        if (!ValhallaMMO.isCustomMiningEnabled() && !MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5)){
            int amplifier = (int) Math.round(profile.getDrillingSpeedBonus() / 0.2) - 1;
            if (amplifier < 0) return;
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, profile.getDrillingDuration(), amplifier, true, false, false));
        }
    }

    private final Collection<UUID> recursionPrevention = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || recursionPrevention.contains(e.getEntity().getUniqueId()) ||
                !(e.getEntity() instanceof TNTPrimed tnt) || tnt.getSource() == null) return;
        Player responsible = null;
        if (tnt.getSource() instanceof Player p && p.isOnline()) responsible = p;
        else if (tnt.getSource() instanceof AbstractArrow a && a.getShooter() instanceof Player p && p.isOnline()) responsible = p;
        if (responsible == null) return;
        if (WorldGuardHook.inDisabledRegion(responsible.getLocation(), responsible, WorldGuardHook.VMMO_SKILL_MINING)) return;

        MiningProfile profile = ProfileCache.getOrCache(responsible, MiningProfile.class);
        ItemStack normalPickaxe = new ItemStack(Material.IRON_PICKAXE);
        if (profile.getBlastFortuneLevel() > 0) normalPickaxe.addUnsafeEnchantment(EnchantmentMappings.FORTUNE.getEnchantment(), profile.getBlastFortuneLevel());
        else if (profile.getBlastFortuneLevel() < 0) normalPickaxe.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
        double blastingDropMultiplier = AccumulativeStatManager.getCachedStats("BLASTING_DROP_MULTIPLIER", responsible, 10000, true);
        double blastingLuck = AccumulativeStatManager.getCachedStats("BLASTING_LUCK", responsible, 10000, true);

        double exp = 0;
        List<Block> ourBlockList = new ArrayList<>();
        for (Block block : e.blockList()){
            if (block.getState() instanceof Container) continue;
            if (block.getType().isAir() || (!tntPreventChaining && block.getType() == Material.TNT) || !BlockUtils.canReward(block)) continue;
            ourBlockList.add(block);
        }
        e.blockList().removeAll(ourBlockList);

        recursionPrevention.add(e.getEntity().getUniqueId());
        EntityExplodeEvent explodeEvent = ValhallaMMO.getNms().getExplosionEvent(e.getEntity(), e.getLocation(), ourBlockList, e.getYield(), 1);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(explodeEvent);
        recursionPrevention.remove(e.getEntity().getUniqueId());

        if (explodeEvent.isCancelled()) {
            e.setCancelled(true);
            return;
        }

        Map<Block, List<ItemStack>> blocksAndItems = new HashMap<>();
        for (Block b : ourBlockList) {
            LootListener.addPreparedLuck(b, blastingLuck);
            List<ItemStack> predictedDrops = new ArrayList<>(b.getDrops(normalPickaxe));
            ItemUtils.multiplyItems(predictedDrops, 1 + blastingDropMultiplier, forgivingDropMultipliers, (i) -> dropsExpValues.containsKey(i.getType()));
            LootListener.markExploded(b);
            LootListener.setFortuneLevel(e.getEntity(), profile.getBlastFortuneLevel());
            LootListener.setEntityOwner(e.getEntity(), responsible);
            if (profile.isBlastingInstantPickup()) LootListener.setInstantPickup(b, responsible);
            b.setType(Material.AIR);

            blocksAndItems.put(b, predictedDrops);
        }

        PlayerBlocksDropItemsEvent event = new PlayerBlocksDropItemsEvent(responsible, blocksAndItems);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        for (Block b : event.getBlocksAndItems().keySet()){
            List<ItemStack> drops = event.getBlocksAndItems().getOrDefault(b, new ArrayList<>());
            LootListener.prepareBlockDrops(b, drops);
            for (ItemStack i : drops){
                if (ItemUtils.isEmpty(i)) continue;
                exp += dropsExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
            }
        }

        addEXP(responsible, exp * blastingExpMultiplier, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTNTDamage(EntityDamageByEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getDamager() instanceof TNTPrimed tnt) ||
                !(e.getEntity() instanceof Item || e.getEntity() instanceof Player)) return;
        if (WorldGuardHook.inDisabledRegion(e.getEntity().getLocation(), WorldGuardHook.VMMO_SKILL_MINING)) return;
        if (e.getEntity() instanceof Player p) {
            Player responsible = null;
            if (tnt.getSource() instanceof Player pl && p.isOnline()) responsible = pl;
            else if (tnt.getSource() instanceof AbstractArrow a && a.getShooter() instanceof Player pl && p.isOnline()) responsible = pl;
            if (responsible == null || !p.getUniqueId().equals(responsible.getUniqueId())) return;
            MiningProfile profile = ProfileCache.getOrCache(p, MiningProfile.class);
            e.setDamage(e.getDamage() * (1 - profile.getTntDamageReduction()));
        } else if (e.getEntity() instanceof Item) {
            Player responsible = null;
            if (tnt.getSource() instanceof Player p && p.isOnline()) responsible = p;
            else if (tnt.getSource() instanceof AbstractArrow a && a.getShooter() instanceof Player p && p.isOnline()) responsible = p;
            if (responsible == null) return;
            MiningProfile profile = ProfileCache.getOrCache(responsible, MiningProfile.class);
            if (profile.isBlastingItemImmunity()) e.setCancelled(true);
        }
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
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_MINING)) return;
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= (1 + AccumulativeStatManager.getCachedStats("MINING_EXP_GAIN", p, 10000, true));
        }
        super.addEXP(p, amount, silent, reason);
    }

    public Map<Material, Double> getDropsExpValues() {
        return dropsExpValues;
    }
}
