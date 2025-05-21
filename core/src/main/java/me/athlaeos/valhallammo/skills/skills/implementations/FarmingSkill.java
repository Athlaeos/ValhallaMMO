package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.entities.EntityClassification;
import me.athlaeos.valhallammo.event.PlayerBlocksDropItemsEvent;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.listeners.CustomBreakSpeedListener;
import me.athlaeos.valhallammo.listeners.LootListener;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.FarmingProfile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.*;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.List;

import static me.athlaeos.valhallammo.utility.Utils.oldOrNew;

public class FarmingSkill extends Skill implements Listener {
    private final Map<Material, Double> blockDropExpValues = new HashMap<>();
    private final Map<Material, Double> blockInteractExpValues = new HashMap<>();
    private final Map<Material, Double> entityDropExpValues = new HashMap<>();
    private final Map<EntityType, Double> entityBreedExpValues = new HashMap<>();
    private final Map<EntityType, Double> entityShearExpValues = new HashMap<>();

    private int fieldHarvestLimit = 256;
    private boolean fieldHarvestInstant = false;

    private boolean forgivingDropMultipliers; // if false, depending on drop multiplier, drops may be reduced to 0. If true, this will be at least 1

    private Animation fieldHarvestAnimation = null;

    public void setFieldHarvestAnimation(Animation fieldHarvestAnimation) {
        this.fieldHarvestAnimation = fieldHarvestAnimation;
    }

    public FarmingSkill(String type) {
        super(type);
    }

    @Override
    public void loadConfiguration() {
        ValhallaMMO.getInstance().save("skills/farming_progression.yml");
        ValhallaMMO.getInstance().save("skills/farming.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/farming.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/farming_progression.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);

        fieldHarvestLimit = skillConfig.getInt("field_harvest_limit");
        fieldHarvestInstant = skillConfig.getBoolean("field_harvest_instant");

        forgivingDropMultipliers = skillConfig.getBoolean("forgiving_multipliers");

        Collection<String> invalidMaterials = new HashSet<>();
        Collection<String> invalidEntities = new HashSet<>();
        ConfigurationSection blockBreakSection = progressionConfig.getConfigurationSection("experience.block_drops");
        if (blockBreakSection != null){
            for (String key : blockBreakSection.getKeys(false)){
                try {
                    Material drop = Material.valueOf(key);
                    double reward = progressionConfig.getDouble("experience.block_drops." + key);
                    blockDropExpValues.put(drop, reward);
                } catch (IllegalArgumentException ignored){
                    invalidMaterials.add(key);
                }
            }
        }

        ConfigurationSection blockInteractSection = progressionConfig.getConfigurationSection("experience.block_interact");
        if (blockInteractSection != null){
            for (String key : blockInteractSection.getKeys(false)){
                try {
                    Material block = Material.valueOf(key);
                    double reward = progressionConfig.getDouble("experience.block_interact." + key);
                    blockInteractExpValues.put(block, reward);
                } catch (IllegalArgumentException ignored){
                    invalidMaterials.add(key);
                }
            }
        }

        ConfigurationSection entityBreedSection = progressionConfig.getConfigurationSection("experience.entity_breed");
        if (entityBreedSection != null){
            for (String key : entityBreedSection.getKeys(false)){
                try {
                    EntityType entity = EntityType.valueOf(key);
                    double reward = progressionConfig.getDouble("experience.entity_breed." + key);
                    entityBreedExpValues.put(entity, reward);
                } catch (IllegalArgumentException ignored){
                    invalidEntities.add(key);
                }
            }
        }

        ConfigurationSection entityShearSection = progressionConfig.getConfigurationSection("experience.entity_shear");
        if (entityShearSection != null){
            for (String key : entityShearSection.getKeys(false)){
                try {
                    EntityType entity = EntityType.valueOf(key);
                    double reward = progressionConfig.getDouble("experience.entity_shear." + key);
                    entityShearExpValues.put(entity, reward);
                } catch (IllegalArgumentException ignored){
                    invalidEntities.add(key);
                }
            }
        }

        ConfigurationSection entityDropSection = progressionConfig.getConfigurationSection("experience.entity_drops");
        if (entityDropSection != null){
            for (String key : entityDropSection.getKeys(false)){
                try {
                    Material drop = Material.valueOf(key);
                    double reward = progressionConfig.getDouble("experience.entity_drops." + key);
                    entityDropExpValues.put(drop, reward);
                } catch (IllegalArgumentException ignored){
                    invalidMaterials.add(key);
                }
            }
        }
        if (!invalidMaterials.isEmpty()) {
            ValhallaMMO.logWarning("The following materials in skills/farming_progression.yml do not exist, no exp values set (ignore warning if your version does not have these materials)");
            ValhallaMMO.logWarning(String.join(", ", invalidMaterials));
        }
        if (!invalidEntities.isEmpty()) {
            ValhallaMMO.logWarning("The following entities in skills/farming_progression.yml do not exist, no exp values set (ignore warning if your version does not have these entities)");
            ValhallaMMO.logWarning(String.join(", ", invalidEntities));
        }

        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    private final Collection<UUID> fieldHarvestingPlayers = new HashSet<>();

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || !BlockUtils.canReward(e.getBlock()) ||
                WorldGuardHook.inDisabledRegion(e.getBlock().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_FARMING) ||
                !blockDropExpValues.containsKey(e.getBlock().getType()) || e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (!hasPermissionAccess(e.getPlayer())) return;

        FarmingProfile profile = ProfileCache.getOrCache(e.getPlayer(), FarmingProfile.class);
        int experience = e.getExpToDrop() + Utils.randomAverage(profile.getFarmingExperienceRate());
        e.setExpToDrop(experience);

        LootListener.addPreparedLuck(e.getBlock(), AccumulativeStatManager.getCachedStats("FARMING_LUCK", e.getPlayer(), 10000, true));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void lootTableDrops(BlockBreakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || !BlockUtils.canReward(e.getBlock()) ||
                WorldGuardHook.inDisabledRegion(e.getBlock().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_FARMING) ||
                !blockDropExpValues.containsKey(e.getBlock().getType()) || e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        double dropMultiplier = AccumulativeStatManager.getCachedStats("FARMING_DROP_MULTIPLIER", e.getPlayer(), 10000, true);
        // multiply any applicable prepared drops and grant exp for them. After the extra drops from a BlockBreakEvent the drops are cleared
        ItemUtils.multiplyItems(LootListener.getPreparedExtraDrops(e.getBlock()), 1 + dropMultiplier, forgivingDropMultipliers, (i) -> blockDropExpValues.containsKey(i.getType()));

        double expQuantity = 0;
        for (ItemStack i : LootListener.getPreparedExtraDrops(e.getBlock())){
            if (ItemUtils.isEmpty(i)) continue;
            expQuantity += blockDropExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
        }
        addEXP(e.getPlayer(), expQuantity, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    private final Collection<Material> harvestableCrops = ItemUtils.getMaterialSet(Arrays.asList(
            "WHEAT", "POTATOES", "CARROTS", "BEETROOTS", "COCOA", "NETHER_WART"
    ));

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent e){
        if (e.getClickedBlock() == null || ValhallaMMO.isWorldBlacklisted(e.getClickedBlock().getWorld().getName()) ||
                e.useInteractedBlock() == Event.Result.DENY || e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() == EquipmentSlot.OFF_HAND ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_FARMING)) return;

        if (!hasPermissionAccess(e.getPlayer())) return;
        Block clickedBlock = e.getClickedBlock();
        FarmingProfile profile = ProfileCache.getOrCache(e.getPlayer(), FarmingProfile.class);
        if (clickedBlock.getBlockData() instanceof Ageable a && a.getAge() >= a.getMaximumAge() && harvestableCrops.contains(clickedBlock.getType())) {
            if (profile.isFieldHarvestUnlocked() && e.getPlayer().isSneaking() &&
                    Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "farming_field_harvest") &&
                    !fieldHarvestingPlayers.contains(e.getPlayer().getUniqueId()) &&
                    !WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_ABILITIES_VEINFARMER)) {
                Collection<Block> vein = BlockUtils.getBlockVein(clickedBlock, fieldHarvestLimit, b ->
                        harvestableCrops.contains(b.getType()) && b.getBlockData() instanceof Ageable ag && ag.getAge() >= ag.getMaximumAge(),
                        fieldHarvestScanArea);
                fieldHarvestingPlayers.add(e.getPlayer().getUniqueId());
                e.setCancelled(true);
                if (fieldHarvestAnimation != null) fieldHarvestAnimation.animate(e.getPlayer(), clickedBlock.getLocation(), e.getPlayer().getEyeLocation().getDirection(), 0);
                if (fieldHarvestInstant)
                    BlockUtils.processBlocks(e.getPlayer(), vein, p -> true, b -> {
                        if (profile.isFieldHarvestInstantPickup()) LootListener.setInstantPickup(b, e.getPlayer());
                        instantHarvest(e.getPlayer(), b, profile);
                    }, (b) -> fieldHarvestingPlayers.remove(b.getUniqueId()));
                else
                    BlockUtils.processBlocksPulse(e.getPlayer(), clickedBlock, vein, p -> true, b -> {
                        if (profile.isFieldHarvestInstantPickup()) LootListener.setInstantPickup(b, e.getPlayer());
                        instantHarvest(e.getPlayer(), b, profile);
                    }, (b) -> fieldHarvestingPlayers.remove(b.getUniqueId()));
                Timer.setCooldownIgnoreIfPermission(e.getPlayer(), profile.getFieldHarvestCooldown() * 50, "farming_field_harvest");
            } else if (profile.isInstantHarvesting()) {
                if (!Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "farming_field_harvest")) Timer.sendCooldownStatus(e.getPlayer(), "farming_field_harvest", TranslationManager.getTranslation("ability_field_harvest"));

                instantHarvest(e.getPlayer(), clickedBlock, profile);
                e.getPlayer().swingMainHand();
            }
        }
        if (clickedBlock.getBlockData() instanceof Beehive b && b.getHoneyLevel() >= b.getMaximumHoneyLevel()) {
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                Beehive newHive = clickedBlock.getBlockData() instanceof Beehive bee ? bee : null;
                if (newHive == null) return;
                if (newHive.getHoneyLevel() < newHive.getMaximumHoneyLevel()){
                    // hive is empty after 5 ticks, so it can be assumed it was harvested
                    if (blockInteractExpValues.containsKey(clickedBlock.getType())) {
                        double amount = blockInteractExpValues.get(clickedBlock.getType());
                        addEXP(e.getPlayer(), amount, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);

                        int vanillaExpReward = Utils.randomAverage(profile.getFarmingExperienceRate());
                        if (vanillaExpReward > 0) {
                            ExperienceOrb orb = (ExperienceOrb) clickedBlock.getWorld().spawnEntity(clickedBlock.getLocation().add(0.5, 0.5, 0.5), EntityType.EXPERIENCE_ORB);
                            orb.setExperience(vanillaExpReward);
                        }
                    }

                    if (Utils.proc(e.getPlayer(), profile.getHiveHoneySaveChance(), false)) {
                        newHive.setHoneyLevel(newHive.getMaximumHoneyLevel());
                        clickedBlock.setBlockData(newHive);
                    }
                }
            }, 5L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || !harvestableCrops.contains(e.getBlock().getType()) ||
                WorldGuardHook.inDisabledRegion(e.getBlock().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_FARMING)) return;
        Block b = e.getBlock();
        growBlock(b, ProfileCache.getOrCache(e.getPlayer(), FarmingProfile.class));
    }

    private void growBlock(Block b, FarmingProfile p){
        if (b.getBlockData() instanceof Ageable a) {
            int stages = Utils.randomAverage(p.getInstantGrowthRate());
            if (stages <= 0) return;
            a.setAge(Math.min(a.getAge() + stages, a.getMaximumAge() - 1));
            b.setBlockData(a);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockHarvest(PlayerHarvestBlockEvent e) {
        if (ValhallaMMO.isWorldBlacklisted(e.getHarvestedBlock().getWorld().getName()) ||
                !blockInteractExpValues.containsKey(e.getHarvestedBlock().getType()) ||
                WorldGuardHook.inDisabledRegion(e.getHarvestedBlock().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_FARMING)) return;
        double dropMultiplier = AccumulativeStatManager.getCachedStats("FARMING_DROP_MULTIPLIER", e.getPlayer(), 10000, true);
        ItemUtils.multiplyItems(e.getItemsHarvested(), dropMultiplier, forgivingDropMultipliers, (i) -> blockInteractExpValues.containsKey(i.getType()));
        ItemUtils.multiplyItems(LootListener.getPreparedExtraDrops(e.getHarvestedBlock()), dropMultiplier, forgivingDropMultipliers, (i) -> blockInteractExpValues.containsKey(i.getType()));

        double amount = 0;
        for (ItemStack i : e.getItemsHarvested()){
            if (ItemUtils.isEmpty(i)) continue;
            amount += blockInteractExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
        }
        for (ItemStack i : LootListener.getPreparedExtraDrops(e.getHarvestedBlock())){
            if (ItemUtils.isEmpty(i)) continue;
            amount += blockInteractExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
        }
        addEXP(e.getPlayer(), amount, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);

        if (!hasPermissionAccess(e.getPlayer())) return;
        FarmingProfile profile = ProfileCache.getOrCache(e.getPlayer(), FarmingProfile.class);
        int exp = Utils.randomAverage(profile.getFarmingExperienceRate());
        if (exp > 0) {
            ExperienceOrb orb = (ExperienceOrb) e.getHarvestedBlock().getWorld().spawnEntity(e.getHarvestedBlock().getLocation().add(0.5, 0.5, 0.5), EntityType.EXPERIENCE_ORB);
            orb.setExperience(exp);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemsDropped(BlockDropItemEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlockState().getWorld().getName()) || !BlockUtils.canReward(e.getBlockState()) ||
                WorldGuardHook.inDisabledRegion(e.getBlock().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_FARMING) || e.getBlockState() instanceof Container) return;
        double dropMultiplier = AccumulativeStatManager.getCachedStats("FARMING_DROP_MULTIPLIER", e.getPlayer(), 10000, true);
        // multiply the item drops from the event itself and grant exp for the initial items and extra drops
        List<ItemStack> extraDrops = ItemUtils.multiplyDrops(e.getItems(), 1 + dropMultiplier, forgivingDropMultipliers, (i) -> blockDropExpValues.containsKey(i.getItemStack().getType()));
        LootListener.prepareBlockDrops(e.getBlock(), extraDrops);

        double expQuantity = 0;
        for (Item i : e.getItems()){
            if (ItemUtils.isEmpty(i.getItemStack())) continue;
            expQuantity += blockDropExpValues.getOrDefault(i.getItemStack().getType(), 0D) * i.getItemStack().getAmount();
        }
        for (ItemStack i : extraDrops){
            if (ItemUtils.isEmpty(i)) continue;
            expQuantity += blockDropExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
        }
        addEXP(e.getPlayer(), expQuantity, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onOtherBlockDrops(PlayerBlocksDropItemsEvent e){
        double exp = 0;
        for (Block b : e.getBlocksAndItems().keySet()){
            for (ItemStack i : e.getBlocksAndItems().getOrDefault(b, new ArrayList<>())){
                if (ItemUtils.isEmpty(i)) continue;
                exp += blockDropExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
            }
        }
        addEXP(e.getPlayer(), exp, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBeeAggro(EntityTargetLivingEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof Bee) ||
                !(e.getTarget() instanceof Player p) || e.getReason() != EntityTargetEvent.TargetReason.CLOSEST_PLAYER ||
                WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_FARMING)) return;

        if (!hasPermissionAccess(p)) return;
        FarmingProfile profile = ProfileCache.getOrCache(p, FarmingProfile.class);
        if (!profile.hasBeeAggroImmunity()) return;
        e.setCancelled(true);
    }

    private void instantHarvest(Player p, Block b, FarmingProfile profile) {
        if (!(b.getBlockData() instanceof Ageable a) || a.getAge() < a.getMaximumAge() || !blockDropExpValues.containsKey(b.getType())) return;
        a = (Ageable) a.clone();
        a.setAge(0);
        Material previousType = b.getType();
        CustomBreakSpeedListener.markInstantBreak(b);
        b.getWorld().spawnParticle(Particle.valueOf(oldOrNew("BLOCK_CRACK", "BLOCK")), b.getLocation().add(0.5, 0.5, 0.5), 100, 0.1, 0.1, 0.1, 4, b.getBlockData());
        b.getWorld().playSound(b.getLocation().add(0.4, 0.4, 0.4), Sound.BLOCK_CROP_BREAK, 0.3F, 1F);
        p.breakBlock(b);
        b.setType(previousType);
        b.setBlockData(a);
        growBlock(b, profile);
    }

    private final int[][] fieldHarvestScanArea = new int[][]{
            {-1, 0, -1}, {-1, 0, 0}, {-1, 0, 1},
            {0, 0, -1}, /*{0, 0, 0}, */{0, 0, 1},
            {1, 0, -1}, {1, 0, 0}, {1, 0, 1},
    };

    @EventHandler(priority = EventPriority.HIGH)
    public void onAnimalKill(EntityDeathEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !EntityClassification.matchesClassification(e.getEntityType(), EntityClassification.ANIMAL)) return;
        Player killer = e.getEntity().getKiller();
        if (killer == null) return;
        if (WorldGuardHook.inDisabledRegion(killer.getLocation(), killer, WorldGuardHook.VMMO_SKILL_FARMING)) return;
        double multiplier = AccumulativeStatManager.getCachedStats("BUTCHERY_DROP_MULTIPLIER", killer, 10000, true);
        ItemUtils.multiplyItems(e.getDrops(), 1 + multiplier, forgivingDropMultipliers, (i) -> entityDropExpValues.containsKey(i.getType()));

        if (!hasPermissionAccess(killer)) return;
        double exp = 0;
        for (ItemStack i : e.getDrops()){
            if (ItemUtils.isEmpty(i)) continue;
            exp += entityDropExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
        }
        addEXP(killer, exp, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAnimalBreed(EntityBreedEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) ||
                !EntityClassification.matchesClassification(e.getEntityType(), EntityClassification.ANIMAL)) return;
        if (!(e.getBreeder() instanceof Player p) || !(e.getEntity() instanceof org.bukkit.entity.Ageable a)) return;
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_FARMING)) return;

        if (!hasPermissionAccess(p)) return;
        FarmingProfile profile = ProfileCache.getOrCache(p, FarmingProfile.class);
        int newAge = (int) Math.ceil(a.getAge() * (1 / (Math.max(-0.999, profile.getGrowUpTimeMultiplier()) + 1)));
        a.setAge(newAge);

        e.setExperience(Utils.randomAverage(e.getExperience() * profile.getBreedingExpMultiplier()));

        double exp = entityBreedExpValues.getOrDefault(e.getEntityType(), 0D);
        if (exp > 0) addEXP(p, exp, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAnimalShear(PlayerShearEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) ||
                !EntityClassification.matchesClassification(e.getEntity().getType(), EntityClassification.ANIMAL) ||
                !entityShearExpValues.containsKey(e.getEntity().getType())) return;
        if (WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_FARMING)) return;

        double exp = entityShearExpValues.getOrDefault(e.getEntity().getType(), 0D);
        if (exp > 0) addEXP(e.getPlayer(), exp, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return FarmingProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 35;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_FARMING)) return;
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= (1 + AccumulativeStatManager.getCachedStats("FARMING_EXP_GAIN", p, 10000, true));
        }
        super.addEXP(p, amount, silent, reason);
    }

    public Map<Material, Double> getBlockDropExpValues() {
        return blockDropExpValues;
    }
}
