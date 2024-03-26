package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Fetcher;
import me.athlaeos.valhallammo.dom.Weighted;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.event.ValhallaLootPopulateEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.listeners.LootListener;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.FishingProfile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class FishingSkill extends Skill implements Listener {
    private final Map<Material, Double> dropsExpValues = new HashMap<>();
    private final Collection<String> baitMaterials = new HashSet<>();

    private boolean forgivingDropMultipliers = true; // if false, depending on drop multiplier, drops may be reduced to 0. If true, this will be at least 1

    public FishingSkill(String type) {
        super(type);
    }

    @Override
    public void loadConfiguration() {
        ValhallaMMO.getInstance().save("skills/fishing_progression.yml");
        ValhallaMMO.getInstance().save("skills/fishing.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/fishing.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/fishing_progression.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);

        forgivingDropMultipliers = skillConfig.getBoolean("forgiving_multipliers");
        baitMaterials.addAll(skillConfig.getStringList("fishing_bait_materials"));

        Collection<String> invalidMaterials = new HashSet<>();
        ConfigurationSection blockBreakSection = progressionConfig.getConfigurationSection("experience.fishing_catch");
        if (blockBreakSection != null){
            for (String key : blockBreakSection.getKeys(false)){
                try {
                    Material block = Material.valueOf(key);
                    double reward = progressionConfig.getDouble("experience.fishing_catch." + key);
                    dropsExpValues.put(block, reward);
                } catch (IllegalArgumentException ignored){
                    invalidMaterials.add(key);
                }
            }
        }
        if (!invalidMaterials.isEmpty()) {
            ValhallaMMO.logWarning("The following materials in skills/fishing_progression.yml do not exist, no exp values set (ignore warning if your version does not have these materials)");
            ValhallaMMO.logWarning(String.join(", ", invalidMaterials));
        }

        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void baitChecker(PlayerFishEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.isCancelled() ||
                e.getState() != PlayerFishEvent.State.CAUGHT_FISH ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_FISHING)) return;

        for (int i = 0; i < e.getPlayer().getInventory().getContents().length; i++){
            ItemStack at = e.getPlayer().getInventory().getItem(i);
            if (ItemUtils.isEmpty(at) || !at.hasItemMeta() || !baitMaterials.contains(at.getType().toString())) continue;
            double baitPower = getBaitPower(ItemUtils.getItemMeta(at));
            if (baitPower > 0) {
                LootListener.addPreparedLuck(e.getPlayer(), baitPower);
                preparedBaitInfo.put(e.getPlayer().getUniqueId(), i);
                break;
            }
        }
    }

    private final Map<UUID, Integer> preparedBaitInfo = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onCatch(PlayerFishEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.isCancelled() ||
                !(e.getState() == PlayerFishEvent.State.FISHING || e.getState() == PlayerFishEvent.State.CAUGHT_FISH) ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_FISHING)) return;
        FishingProfile profile = ProfileCache.getOrCache(e.getPlayer(), FishingProfile.class);
        if (e.getState() == PlayerFishEvent.State.FISHING) {
            double multiplier = (1 / (1 + Math.max(-0.999, profile.getFishingSpeedBonus())));
//            ItemStack rod = e.getPlayer().getInventory().getItem(e.getHand() == null ? EquipmentSlot.HAND : e.getHand());
//            if (ItemUtils.isEmpty(rod)) return;
//            int lureLevel = rod.getEnchantmentLevel(Enchantment.LURE);
//            int minWaitTime = Math.max(1, e.getHook().getMinWaitTime() - (lureLevel * 100));
//            int maxWaitTime = Math.max(1, e.getHook().getMaxWaitTime() - (lureLevel * 100));
            e.getHook().setWaitTime(Math.max(1, Utils.randomAverage(e.getHook().getMinWaitTime() * multiplier)), Math.max(1, Utils.randomAverage(e.getHook().getMaxWaitTime() * multiplier)));

        } else if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            int extraCatches = Utils.randomAverage(profile.getFishingDrops());
            if (!forgivingDropMultipliers && extraCatches < 0) {
                e.setCancelled(true);
                return;
            }
            for (int i = 0; i < extraCatches; i++) simulateFishingEvent(e.getPlayer());

            if (preparedBaitInfo.containsKey(e.getPlayer().getUniqueId()) && !Utils.proc(e.getPlayer(), profile.getBaitSaveChance(), false)) {
                int index = preparedBaitInfo.get(e.getPlayer().getUniqueId());
                ItemStack at = e.getPlayer().getInventory().getItem(index);
                if (!ItemUtils.isEmpty(at)){
                    if (at.getAmount() <= 1) e.getPlayer().getInventory().setItem(index, null);
                    else at.setAmount(at.getAmount() - 1);
                }
            }
            e.setExpToDrop(Utils.randomAverage(e.getExpToDrop() * (1 + extraCatches) * (1 + profile.getFishingEssenceMultiplier())));

            if (!(e.getCaught() instanceof Item item) || ItemUtils.isEmpty(item.getItemStack())) return;
            double exp = dropsExpValues.getOrDefault(item.getItemStack().getType(), 0D) * item.getItemStack().getAmount();
            for (ItemStack i : LootListener.getPreparedExtraDrops(e.getPlayer())){
                if (ItemUtils.isEmpty(i)) return;
                exp += dropsExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
            }
            if (exp > 0) addEXP(e.getPlayer(), exp, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
        }
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return FishingProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 50;
    }

    @Override
    public boolean isExperienceScaling() {
        return true;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_FISHING)) return;
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= (1 + AccumulativeStatManager.getStats("FISHING_EXP_GAIN", p, true));
        }
        super.addEXP(p, amount, silent, reason);
    }

    public Map<Material, Double> getDropsExpValues() {
        return dropsExpValues;
    }

    private static final NamespacedKey BAIT_POWER_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "bait_power");
    public static void setBaitPower(ItemMeta meta, Double baitPower){
        if (baitPower == null) meta.getPersistentDataContainer().remove(BAIT_POWER_KEY);
        else meta.getPersistentDataContainer().set(BAIT_POWER_KEY, PersistentDataType.DOUBLE, baitPower);
    }
    public static double getBaitPower(ItemMeta meta){
        if (meta == null) return 0;
        return meta.getPersistentDataContainer().getOrDefault(BAIT_POWER_KEY, PersistentDataType.DOUBLE, 0D);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFish(PlayerFishEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.isCancelled() ||
                e.getState() != PlayerFishEvent.State.CAUGHT_FISH || !(e.getCaught() instanceof Item i)) return;
        Player p = e.getPlayer();
        AttributeInstance luckAttribute = p.getAttribute(Attribute.GENERIC_LUCK);
        double luck = AccumulativeStatManager.getCachedStats("FISHING_LUCK", p, 10000, true) + LootListener.getPreparedLuck(p);
        if (luckAttribute != null) luck += luckAttribute.getValue();

        FishingTableEntry pickedEntry = Utils.weightedSelection(fishingTables, 1, luck).stream().findFirst().orElse(null);
        if (pickedEntry == null) return; // somehow, no entry. bail out
        if (pickedEntry.valhallaTable.get() != null && pickedEntry.valhallaTable.get().getPools().isEmpty()) return;
        LootContext context = new LootContext.Builder(p.getLocation()).luck((float) luck).lootingModifier(0).killer(p).lootedEntity(p).build();

        List<ItemStack> vanillaLoot = new ArrayList<>(pickedEntry.vanillaTable.getLootTable().populateLoot(Utils.getRandom(), context));
        if (!vanillaLoot.isEmpty()) { // re-setting new vanilla loot to hook
            i.setItemStack(vanillaLoot.get(0));
            vanillaLoot.remove(0);
        }
        if (pickedEntry.valhallaTable.get() != null){
            LootTable table = pickedEntry.valhallaTable.get();
            List<ItemStack> loot = LootTableRegistry.getLoot(table, context, LootTable.LootType.FISH);

            ValhallaLootPopulateEvent loottableEvent = new ValhallaLootPopulateEvent(table, context, loot);
            ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(loottableEvent);
            if (!loottableEvent.isCancelled()){
                LootListener.prepareFishingDrops(p.getUniqueId(), loottableEvent.getDrops());
                boolean clearVanilla = switch (loottableEvent.getPreservationType()){
                    case CLEAR -> true;
                    case CLEAR_UNLESS_EMPTY -> !loottableEvent.getDrops().isEmpty();
                    case KEEP -> false;
                };
                if (clearVanilla) {
                    if (loottableEvent.getDrops().isEmpty()) {
                        e.setCancelled(true); // custom table returned no drops and is configured to clear vanilla loot, so event should be cancelled
                        return;
                    } else {
                        i.setItemStack(loottableEvent.getDrops().get(0)); // overwriting vanilla drop with custom one
                        loottableEvent.getDrops().remove(0);
                    }
                } else LootListener.prepareFishingDrops(p.getUniqueId(), vanillaLoot);
                LootListener.prepareFishingDrops(p.getUniqueId(), loottableEvent.getDrops());
            }
        }
    }

    private static final Collection<FishingTableEntry> fishingTables = Set.of(
            new FishingTableEntry(LootTables.FISHING_FISH, LootTableRegistry::getFishingFishLootTable, 1700, -3),
            new FishingTableEntry(LootTables.FISHING_JUNK, LootTableRegistry::getFishingJunkLootTable, 200, -39),
            new FishingTableEntry(LootTables.FISHING_TREASURE, LootTableRegistry::getFishingTreasureLootTable, 100, 42)
    );

    private record FishingTableEntry(LootTables vanillaTable, Fetcher<LootTable> valhallaTable, double baseWeight, double bonusWeightPerLuck) implements Weighted {
        @Override public double getWeight() { return baseWeight; }
        @Override public double getWeight(double luck) { return Math.max(0, baseWeight + (luck * bonusWeightPerLuck)); }
    }

    public static void simulateFishingEvent(Player p){
        AttributeInstance luckAttribute = p.getAttribute(Attribute.GENERIC_LUCK);
        double luck = AccumulativeStatManager.getCachedStats("FISHING_LUCK", p, 10000, true) + LootListener.getPreparedLuck(p);
        if (luckAttribute != null) luck += luckAttribute.getValue();

        FishingTableEntry pickedEntry = Utils.weightedSelection(fishingTables, 1, luck).stream().findFirst().orElse(null);
        if (pickedEntry == null) return; // somehow, no entry. bail out
        LootContext context = new LootContext.Builder(p.getLocation()).luck((float) luck).lootingModifier(0).killer(p).lootedEntity(p).build();

        List<ItemStack> vanillaLoot = new ArrayList<>(pickedEntry.vanillaTable.getLootTable().populateLoot(Utils.getRandom(), context));
        if (pickedEntry.valhallaTable.get() != null){
            LootTable table = pickedEntry.valhallaTable.get();
            List<ItemStack> loot = LootTableRegistry.getLoot(table, context, LootTable.LootType.FISH);

            ValhallaLootPopulateEvent loottableEvent = new ValhallaLootPopulateEvent(table, context, loot);
            ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(loottableEvent);
            if (!loottableEvent.isCancelled()){
                LootListener.prepareFishingDrops(p.getUniqueId(), loottableEvent.getDrops());
                boolean clearVanilla = switch (loottableEvent.getPreservationType()){
                    case CLEAR -> true;
                    case CLEAR_UNLESS_EMPTY -> !loottableEvent.getDrops().isEmpty();
                    case KEEP -> false;
                };
                if (!clearVanilla) LootListener.prepareFishingDrops(p.getUniqueId(), vanillaLoot);
                LootListener.prepareFishingDrops(p.getUniqueId(), loottableEvent.getDrops());
            }
        } else LootListener.prepareFishingDrops(p.getUniqueId(), vanillaLoot);
    }
}
