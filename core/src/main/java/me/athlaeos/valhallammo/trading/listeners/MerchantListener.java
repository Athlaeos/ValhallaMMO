package me.athlaeos.valhallammo.trading.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.TradingProfile;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.skills.skills.implementations.TradingSkill;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.*;
import me.athlaeos.valhallammo.trading.happiness.HappinessSourceRegistry;
import me.athlaeos.valhallammo.trading.menu.ServiceMenu;
import me.athlaeos.valhallammo.trading.merchants.VirtualMerchant;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectTypeWrapper;
import org.bukkit.util.Vector;

import java.util.*;

public class MerchantListener implements Listener {
    private static final NamespacedKey KEY_PROFESSION_DELAY = new NamespacedKey(ValhallaMMO.getInstance(), "time_career_change");
    private static final Map<UUID, VirtualMerchant> activeTradingMenus = new HashMap<>();
    private static final Collection<UUID> tradingMerchants = new HashSet<>();

    private final boolean convertAllVillagers = CustomMerchantManager.getTradingConfig().getBoolean("customize_all_villagers");
    private final double demandDecayRate = CustomMerchantManager.getTradingConfig().getDouble("demand_decay_per_day", 0.5);
    private final int forgivePunchDelay = CustomMerchantManager.getTradingConfig().getInt("time_forgive_accidental_punch", 1200);

    private final double renownPenaltyHomicide = CustomMerchantManager.getTradingConfig().getDouble("renown_kill_villager", 0);
    private final double renownPenaltyInfanticide = CustomMerchantManager.getTradingConfig().getDouble("renown_kill_baby_villager", 0);
    private final double renownPenaltyTreason = CustomMerchantManager.getTradingConfig().getDouble("renown_kill_iron_golem", 0);
    private final double renownAssault = CustomMerchantManager.getTradingConfig().getDouble("renown_hurt_villager", 0);
    private final double renownChildAssault = CustomMerchantManager.getTradingConfig().getDouble("renown_hurt_baby_villager", 0);
    private final double renownGolemAssault = CustomMerchantManager.getTradingConfig().getDouble("renown_hurt_iron_golem", 0);
    private final double renownVillagerDeath = CustomMerchantManager.getTradingConfig().getDouble("renown_death_villager", 0);
    private final double renownChildDeath = CustomMerchantManager.getTradingConfig().getDouble("renown_death_baby_villager", 0);
    private final double renownGolemDeath = CustomMerchantManager.getTradingConfig().getDouble("renown_death_iron_golem", 0);
    private final double reputationPenaltyHomicide = CustomMerchantManager.getTradingConfig().getDouble("reputation_kill_villager", 0);
    private final double reputationPenaltyInfanticide = CustomMerchantManager.getTradingConfig().getDouble("reputation_kill_baby_villager", 0);
    private final double reputationPenaltyTreason = CustomMerchantManager.getTradingConfig().getDouble("reputation_kill_iron_golem", 0);
    private final double reputationAssault = CustomMerchantManager.getTradingConfig().getDouble("reputation_hurt_villager", 0);
    private final double reputationChildAssault = CustomMerchantManager.getTradingConfig().getDouble("reputation_hurt_baby_villager", 0);
    private final double reputationGolemAssault = CustomMerchantManager.getTradingConfig().getDouble("reputation_hurt_iron_golem", 0);
    private final double reputationVillagerDeath = CustomMerchantManager.getTradingConfig().getDouble("reputation_death_villager", 0);
    private final double reputationChildDeath = CustomMerchantManager.getTradingConfig().getDouble("reputation_death_baby_villager", 0);
    private final double reputationGolemDeath = CustomMerchantManager.getTradingConfig().getDouble("reputation_death_iron_golem", 0);
    private final double renownHero = CustomMerchantManager.getTradingConfig().getDouble("renown_hero", 0);
    private final double renownHappiness = CustomMerchantManager.getTradingConfig().getDouble("renown_happiness", 0);
    private final double happinessDenyTrading = CustomMerchantManager.getTradingConfig().getDouble("happiness_deny_trading", -3);
    private final double renownDenyTrading = CustomMerchantManager.getTradingConfig().getDouble("renown_deny_trading", -75);

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent e){
        if (e.getInventory().getType() != InventoryType.MERCHANT) return;
        virtualMerchantClose((Player) e.getPlayer(), getCurrentActiveVirtualMerchant((Player) e.getPlayer()));
    }

    public static void virtualMerchantClose(Player p, VirtualMerchant virtualMerchant){
        if (virtualMerchant == null) return;
        setActiveTradingMenu(p, null);
        virtualMerchant.onClose();
        if (virtualMerchant.getMerchantID() != null) tradingMerchants.remove(virtualMerchant.getMerchantID());
        UUID villager = virtualMerchant.getMerchantID();
        if (villager == null || !(ValhallaMMO.getInstance().getServer().getEntity(villager) instanceof AbstractVillager v)) return;
        MerchantType type = CustomMerchantManager.getMerchantType(virtualMerchant.getData().getType());
        MerchantData data = virtualMerchant.getData();
        if (type == null) return;
        int expToGrant = (int) ((1 + AccumulativeStatManager.getCachedStats("TRADING_MERCHANT_EXPERIENCE_MULTIPLIER", p, 10000, true)) * virtualMerchant.getExpToGrant());
        data.setExp(Math.min(type.getExpRequirement(MerchantLevel.MASTER), data.getExp() + expToGrant));
        MerchantLevel level = CustomMerchantManager.getLevel(data);
        MerchantLevel nextLevel = level == null ? null : MerchantLevel.getNextLevel(level);
        if (v instanceof Villager vi){
            int villagerLevel = vi.getVillagerLevel();
            if (nextLevel == null) {
                vi.setVillagerLevel(MerchantLevel.MASTER.getLevel());
                vi.setVillagerExperience(MerchantLevel.MASTER.getDefaultExpRequirement()); // already max level
            } else {
                int expToCurrentLevel = type.getExpRequirement(level);
                int expToNextLevel = type.getExpRequirement(nextLevel);

                float progressToNextLevel = ((float) data.getExp() - expToCurrentLevel) / (expToNextLevel - expToCurrentLevel);
                vi.setVillagerExperience(level.getDefaultExpRequirement() + (int) Math.floor((nextLevel.getDefaultExpRequirement() - level.getDefaultExpRequirement()) * progressToNextLevel));
                vi.setVillagerLevel(MerchantLevel.getLevel(vi.getVillagerExperience()).getLevel());
            }
            if (vi.getVillagerLevel() != villagerLevel) vi.addPotionEffect(PotionEffectTypeWrapper.REGENERATION.createEffect(200, 0));
        }
        // Ensures that at least one of the villager's trades is not fully restocked, prompting
        // the villager to want to restock
        MerchantRecipe recipe = Catch.catchOrElse(() -> v.getRecipe(0), null);
        if (recipe != null) {
            recipe.setUses(1);
            v.setRecipe(0, recipe);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPrepareTrade(TradeSelectEvent e){
        if (e.getMerchant().getTrader() == null || e.isCancelled()) return;
        VirtualMerchant merchantInterface = activeTradingMenus.get(e.getMerchant().getTrader().getUniqueId());
        if (merchantInterface == null) return;
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            ItemStack result = e.getInventory().getItem(2);
            if (ItemUtils.isEmpty(result)) return;
            ItemMeta meta = result.getItemMeta();
            if (meta == null) return;
            CustomMerchantManager.removeTradeKey(meta);
            result.setItemMeta(meta);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTrade(InventoryClickEvent e){
        if (!(e.getClickedInventory() instanceof MerchantInventory m) || e.isCancelled() || m.getSelectedRecipe() == null || e.getRawSlot() != 2 || ItemUtils.isEmpty(m.getItem(2))) return;
        VirtualMerchant merchantInterface = activeTradingMenus.get(e.getWhoClicked().getUniqueId());
        if (merchantInterface == null || merchantInterface.getMerchantID() == null) return;
        UUID merchantID = merchantInterface.getMerchantID();
        MerchantRecipe recipe = m.getSelectedRecipe();
        ItemStack result = recipe.getResult();
        ItemMeta meta = ItemUtils.isEmpty(result) ? null : result.getItemMeta();
        if (meta == null) return;
        MerchantTrade trade = CustomMerchantManager.tradeFromKeyedMeta(meta);
        if (trade == null) return;
        CustomMerchantManager.removeTradeKey(meta);
        result.setItemMeta(meta);

        ItemStack item1 = m.getItem(0);
        ItemStack item2 = m.getItem(1);
        ItemStack cost1 = recipe.getAdjustedIngredient1();
        ItemStack cost2 = recipe.getIngredients().size() > 1 ? recipe.getIngredients().get(1) : null;

        ClickType clickType = e.getClick();
        int timesTraded = 1;
        switch (clickType){
            case DROP, CONTROL_DROP -> {
                if (!ItemUtils.isEmpty(e.getCursor())){
                    e.setCancelled(true);
                    return;
                }
            } // do nothing special because these actions do not require empty inventory space
            case LEFT -> {
                if (!ItemUtils.isEmpty(e.getCursor()) && !ItemUtils.isEmpty(result) &&
                        (!e.getCursor().isSimilar(result) ||
                                (e.getCursor().getAmount() + result.getAmount() > ValhallaMMO.getNms().getMaxStackSize(e.getCursor().getItemMeta(), e.getCursor().getType()))) ||
                        recipe.getMaxUses() - recipe.getUses() <= 0){
                    // cursor cannot stack with result item, or the merchant recipe has been exhausted. do not proceed
                    e.setCancelled(true);
                    return;
                }
            }
            case SHIFT_LEFT, SHIFT_RIGHT -> {
                // calculate how many items can be traded
                // the max amount of items the player could trade if they have enough inventory space,
                int maxTradeable = 99;

                int available = ItemUtils.maxInventoryFit((Player) e.getWhoClicked(), result); // max items available to fit in inventory
                timesTraded = Math.min(available, maxTradeable);

                if (!ItemUtils.isEmpty(cost1)){
                    // if the recipe has a primary cost, here we calculate how often the player can make this trade based on the item in slot 0 and the cost of the recipe
                    if (!ItemUtils.isEmpty(item1)) timesTraded = Math.min(timesTraded, (int) Math.floor(item1.getAmount() / (double) cost1.getAmount()));
                    else timesTraded = 0; // the ingredient has a cost, but no items are present in slot 1. No trade mad
                } // if not, assuming the recipe has no secondary cost that isn't met, the trade can be made indefinitely
                if (!ItemUtils.isEmpty(cost2)) {
                    // we do the same with the secondary cost
                    if (!ItemUtils.isEmpty(item2)) timesTraded = Math.min(timesTraded, (int) Math.floor(item2.getAmount() / (double) cost2.getAmount()));
                    else timesTraded = 0;
                }
                timesTraded = Math.min(timesTraded, recipe.getMaxUses() - recipe.getUses());
                if (timesTraded <= 0) {
                    e.setCancelled(true);
                    return;
                }
            }
            default -> {
                e.setCancelled(true);
                return;
            }
        }

        int finalTimesTraded = timesTraded;
        CustomMerchantManager.getMerchantData(merchantID, data -> {
            ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
                PlayerTradeItemEvent event = new PlayerTradeItemEvent((Player) e.getWhoClicked(), merchantID, data, m.getMerchant(), recipe, trade, result, finalTimesTraded);
                ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
                if (event.isCancelled() || event.getTimesTraded() <= 0){
                    e.setCancelled(true);
                    return;
                }
                MerchantData.TradeData tradeData = event.getMerchantData().getTrades().get(event.getCustomTrade().getID());
                if (tradeData == null) return; // should never really happen, but here as a precaution
                MerchantType type = CustomMerchantManager.getMerchantType(event.getMerchantData().getType());
                if (type == null) return; // should also never really happen unless a type is deleted during trading
                tradeData.setLastTraded(System.currentTimeMillis());
                tradeData.setDemand(tradeData.getDemand() + finalTimesTraded);
                float perTradeWeight = trade.getPerTradeWeight((Player) e.getWhoClicked(), tradeData);
                tradeData.setRemainingUses((Player) e.getWhoClicked(), tradeData.getRemainingUses((Player) e.getWhoClicked(), type.isPerPlayerStock()) - (finalTimesTraded * perTradeWeight), type.isPerPlayerStock());
                int experiencePointsToReward = Utils.randomAverage(trade.getEnchantingExperience() * finalTimesTraded);
                int orbSize = orbSize(experiencePointsToReward);
                int orbCount = (int) Math.ceil((double) experiencePointsToReward / orbSize);
                Location spawnLocation = ValhallaMMO.getInstance().getServer().getEntity(event.getMerchantData().getVillagerUUID()) instanceof AbstractVillager a ? a.getLocation() : e.getWhoClicked().getLocation();
                for (int i = 0; i < orbCount; i++){
                    ExperienceOrb orb = e.getWhoClicked().getWorld().spawn(spawnLocation, ExperienceOrb.class);
                    orb.setExperience(Math.min(experiencePointsToReward, orbSize));
                    experiencePointsToReward -= orbSize;
                }

                int price = ItemUtils.isEmpty(recipe.getAdjustedIngredient1()) ? recipe.getIngredients().getFirst().getAmount() + recipe.getSpecialPrice() : recipe.getAdjustedIngredient1().getAmount();
                double ratio = 1 - ((double) price / Math.max(1, tradeData.getBasePrice()));

                TradingSkill skill = (TradingSkill) SkillRegistry.getSkill(TradingSkill.class);
                if (skill != null) skill.addEXP((Player) e.getWhoClicked(), trade.getSkillExp() * finalTimesTraded, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION, CustomMerchantManager.getLevel(data), ratio);

                AbstractVillager villager = event.getMerchantData().getVillager();
                MerchantData.MerchantPlayerMemory memory = event.getMerchantData().getPlayerMemory(e.getWhoClicked().getUniqueId());

                if (villager != null && memory.getLastTimeTraded() < CustomMerchantManager.time() && !Timer.isCooldownPassed(e.getWhoClicked().getUniqueId(), "cooldown_villager_gift")) {
                    double giftChance = AccumulativeStatManager.getCachedRelationalStats("TRADING_GIFT_CHANCE", e.getWhoClicked(), data.getVillager(), 10000, true);
                    if (giftChance > 0 && Utils.proc(giftChance, finalTimesTraded, false)) {
                        Collection<MerchantTrade> possibleGifts = new HashSet<>();
                        MerchantLevel merchantLevel = CustomMerchantManager.getLevel(event.getMerchantData());
                        if (merchantLevel != null){
                            float luck = CustomMerchantManager.getTradingLuck((Player) e.getWhoClicked());

                            TradingProfile profile = ProfileCache.getOrCache((Player) e.getWhoClicked(), TradingProfile.class);
                            LootContext context = new LootContext.Builder(e.getWhoClicked().getLocation()).killer(e.getWhoClicked()).lootedEntity(ValhallaMMO.getInstance().getServer().getEntity(merchantID) instanceof Villager v ? v : null).lootingModifier(0).luck(luck).build();
                            for (MerchantLevel level : type.getTrades().keySet()){
                                if (merchantLevel.getLevel() > level.getLevel()) continue;
                                MerchantType.MerchantLevelTrades trades = type.getTrades(level);
                                for (String tradeName : trades.getTrades()){
                                    MerchantTrade merchantTrade = CustomMerchantManager.getTrade(tradeName);
                                    if (merchantTrade == null || !memory.isGiftable(tradeName) || merchantTrade.getGiftWeight() == 0 ||
                                            (merchantTrade.isExclusive() && !profile.getExclusiveTrades().contains(tradeName)) ||
                                            trade.failsPredicates(trade.getPredicateSelection(), context)) continue;
                                    possibleGifts.add(merchantTrade);
                                }
                            }

                            MerchantTrade selectedGift = giftSelection(possibleGifts);
                            if (selectedGift != null) {
                                ItemBuilder gift = new ItemBuilder(selectedGift.getResult());
                                DynamicItemModifier.modify(ModifierContext.builder(gift).crafter((Player) e.getWhoClicked()).executeUsageMechanics().entity(villager).validate().get(), selectedGift.getModifiers());
                                if (!CustomFlag.hasFlag(gift.getMeta(), CustomFlag.UNCRAFTABLE)) {
                                    long cooldown = (long) AccumulativeStatManager.getCachedStats("TRADING_GIFT_COOLDOWN", e.getWhoClicked(), 10000, true);
                                    ItemStack finalGift = gift.get();
                                    Item drop = villager.getWorld().dropItem(villager.getEyeLocation(), finalGift);

                                    double relX = e.getWhoClicked().getLocation().getX() - drop.getLocation().getX();
                                    double relY = e.getWhoClicked().getLocation().getY() - drop.getLocation().getY();
                                    double relZ = e.getWhoClicked().getLocation().getZ() - drop.getLocation().getZ();
                                    drop.setVelocity(new Vector(relX * 0.1, relY * 0.1 + Math.sqrt(Math.sqrt(relX * relX + relY * relY + relZ * relZ)) * 0.08, relZ * 0.1));

                                    villager.getWorld().playSound(villager.getEyeLocation(), Sound.ENTITY_VILLAGER_YES, 1F, 1F);

                                    if (selectedGift.getGiftWeight() < 0) memory.setCooldown(selectedGift.getID(), -1);
                                    else memory.setCooldown(selectedGift.getID(), cooldown);
                                }
                            }
                        }
                    }
                }

                m.setItem(2, event.getResult());
                if (merchantID != null){
                    int reputationQuantity = event.getTimesTraded();
                    memory.setTradingReputation(memory.getTradingReputation() + reputationQuantity);
                    int exp = Utils.randomAverage(event.getTimesTraded() * event.getCustomTrade().getVillagerExperience());
                    merchantInterface.setExpToGrant(merchantInterface.getExpToGrant() + exp);
                }
            });
        });
    }

    private MerchantTrade giftSelection(Collection<MerchantTrade> entries){
        // weighted selection
        double totalWeight = 0;
        List<MerchantTrade> selectedEntries = new ArrayList<>();
        if (entries.isEmpty()) return null;
        List<Pair<MerchantTrade, Double>> totalEntries = new ArrayList<>();
        for (MerchantTrade entry : entries){
            totalWeight += Math.abs(entry.getGiftWeight());
            totalEntries.add(new Pair<>(entry, totalWeight));
        }

        double random = Utils.getRandom().nextDouble() * totalWeight;
        for (Pair<MerchantTrade, Double> pair : totalEntries){
            if (pair.getTwo() >= random) {
                selectedEntries.add(pair.getOne());
                break;
            }
        }
        return selectedEntries.isEmpty() ? null : selectedEntries.get(0);
    }

    private int orbSize(int exp){
        return exp <= 8 ? 1 : exp <= 16 ? 2 : exp <= 32 ? 4 : exp <= 64 ? 8 : exp <= 128 ? 16 :
                exp <= 256 ? 32 : exp <= 512 ? 64 : exp <= 1024 ? 128 : exp <= 2048 ? 256 :
                        exp <= 4096 ? 512 : exp <= 8192 ? 1024 : exp <= 16384 ? 2048 :
                                exp <= 32768 ? 4096 : 8192;
    }

    private static final Collection<UUID> cancelMerchantInventory = new HashSet<>();
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVillagerInteract(PlayerInteractAtEntityEvent e){
        if (!(e.getRightClicked() instanceof AbstractVillager v) || (!convertAllVillagers && !CustomMerchantManager.isCustomMerchant(v))) return;
        if (tradingMerchants.contains(v.getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        if (HappinessSourceRegistry.getHappiness(e.getPlayer(), v) <= happinessDenyTrading) {
            e.setCancelled(true);
            if (v instanceof Villager villager) villager.shakeHead();
            e.getPlayer().spawnParticle(Particle.VILLAGER_ANGRY, v.getEyeLocation(), 5, 0.5, 0.5, 0.5);
            Utils.sendMessage(e.getPlayer(), "&fYou expect me to work in conditions like these?"); // TODO data driven
            return;
        }
        if (!e.getPlayer().hasPermission("valhalla.bypasstradedelay") && v.getPersistentDataContainer().getOrDefault(KEY_PROFESSION_DELAY, PersistentDataType.LONG, 0L) + CustomMerchantManager.getDelayUntilWorking() > CustomMerchantManager.time()){
            e.setCancelled(true);
            if (v instanceof Villager villager) villager.shakeHead();
            Utils.sendMessage(e.getPlayer(), "&fAllow me a moment to prepare"); // TODO data driven
            return;
        }
        e.setCancelled(true);
        cancelMerchantInventory.add(v.getUniqueId());
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
            CustomMerchantManager.getMerchantData(v, data -> {
                ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
                    MerchantData d = data;
                    MerchantType type = d == null ? null : CustomMerchantManager.getMerchantType(d.getType());
                    if (d != null && type != null && d.getTypeVersion() != type.getVersion()) {
                        Map<UUID, MerchantData.MerchantPlayerMemory> memory = new HashMap<>(d.getPlayerMemory());
                        d = CustomMerchantManager.createMerchant(v.getUniqueId(), type, e.getPlayer());
                        d.getPlayerMemory().putAll(memory);
                    } else if (d != null && type != null && d.getDay() != CustomMerchantManager.today()) {
                        Map<String, MerchantData.TradeData> newData = new HashMap<>();
                        Map<String, MerchantData.TradeData> currentData = d.getTrades();
                        if (type.resetsTradesDaily()){
                            for (MerchantData.TradeData trade : CustomMerchantManager.generateRandomTrades(v, type, e.getPlayer())){
                                MerchantData.TradeData entry = currentData.get(trade.getTrade());
                                if (entry == null) {
                                    newData.put(trade.getTrade(), trade);
                                    continue;
                                }
                                trade.setDemand(entry.getDemand());
                                trade.setLastRestocked(entry.getLastRestocked());
                                trade.setLastTraded(entry.getLastTraded());
                                newData.put(trade.getTrade(), trade);
                            }
                        } else {
                            for (MerchantData.TradeData trade : currentData.values()){
                                MerchantTrade t = CustomMerchantManager.getTrade(trade.getTrade());
                                if (!t.refreshes()) {
                                    newData.put(trade.getTrade(), trade);
                                    continue;
                                }
                                ItemBuilder newResult = CustomMerchantManager.prepareTradeResult(v, t, e.getPlayer());
                                if (newResult == null) {
                                    newData.put(trade.getTrade(), trade);
                                    continue;
                                }
                                trade.setItem(newResult.getItem());
                                newData.put(trade.getTrade(), trade);
                            }
                        }
                        d.getTrades().clear();
                        d.getTrades().putAll(newData);
                    }
                    if ((d == null && convertAllVillagers) || type == null) {
                        d = CustomMerchantManager.convertToRandomMerchant(v, e.getPlayer());
                    }
                    if (d == null) {
                        cancelMerchantInventory.remove(v.getUniqueId());
                        e.getPlayer().openMerchant(v, true);
                        return;
                    }
                    int today = CustomMerchantManager.today();
                    if (d.getDay() < today){
                        // different day, decay demand
                        for (MerchantData.TradeData tradeData : d.getTrades().values()) tradeData.setDemand((int) Math.floor(tradeData.getDemand() * Math.pow(demandDecayRate, 1 + today - d.getDay())));
                        d.setDay(today);
                    } else if (d.getDay() != today) {
                        // time has gone backwards...? reset demand
                        for (MerchantData.TradeData tradeData : d.getTrades().values()) tradeData.setDemand(0);
                    }
                    MerchantData.MerchantPlayerMemory reputation = d.getPlayerMemory(e.getPlayer().getUniqueId());
                    if (reputation.getRenownReputation() <= renownDenyTrading) {
                        if (v instanceof Villager villager) villager.shakeHead();
                        e.getPlayer().spawnParticle(Particle.VILLAGER_ANGRY, v.getEyeLocation(), 5, 0.5, 0.5, 0.5);
                        Utils.sendMessage(e.getPlayer(), "&fI don't deal with the likes of you."); // TODO data driven
                        return;
                    }

                    ServiceMenu menu = new ServiceMenu(PlayerMenuUtilManager.getPlayerMenuUtility(e.getPlayer()), d);
                    if (menu.getServices().size() > 1) menu.open();
                    else if (!menu.getServices().isEmpty()) menu.getServices().get(0).getServiceType().onServiceSelect(null, menu, menu.getServices().get(0), d);
                    else if (v instanceof Villager villager) villager.shakeHead();
                });
            });
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMerchantInterfaceOpen(InventoryOpenEvent e) {
        if (!(e.getInventory() instanceof MerchantInventory m)) return;
        if (!(m.getHolder() instanceof AbstractVillager v)) return;
        if (cancelMerchantInventory.contains(v.getUniqueId())) {
            e.setCancelled(true);
            cancelMerchantInventory.remove(v.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMerchantProfessionChange(VillagerCareerChangeEvent e){
        if (e.isCancelled()) return;
        Villager.Profession newProfession = e.getProfession();
        e.getEntity().getPersistentDataContainer().set(KEY_PROFESSION_DELAY, PersistentDataType.LONG, CustomMerchantManager.time());

        CustomMerchantManager.getMerchantData(e.getEntity(), data -> {
            if (data == null) {
                e.getEntity().setProfession(newProfession);
                return;
            }
            MerchantType type = CustomMerchantManager.getMerchantType(data.getType());
            if (!type.canLoseProfession() || data.getExp() > 0) {
                e.setCancelled(true);
                return; // Can't lose profession, so the villager stays as they are
            }
            CustomMerchantManager.getMerchantDataPersistence().setData(e.getEntity().getUniqueId(), null);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMerchantRestock(VillagerReplenishTradeEvent e){
        if (e.isCancelled() || !Timer.isCooldownPassed(e.getEntity().getUniqueId(), "delay_restock_trades")) return;
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
            CustomMerchantManager.getMerchantData(e.getEntity(), data -> {
                ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
                    if (data == null) return;
                    MerchantType type = CustomMerchantManager.getMerchantType(data.getType());
                    long time = CustomMerchantManager.time();
                    for (MerchantData.TradeData tradeData : data.getTrades().values()){
                        MerchantTrade trade = CustomMerchantManager.getTrade(tradeData.getTrade());
                        if (trade == null || trade.getRestockDelay() < 0) continue;
                        if (time >= tradeData.getLastRestocked() + trade.getRestockDelay()){
                            tradeData.resetRemainingUses(type != null && type.isPerPlayerStock());
                            tradeData.setLastRestocked(time);
                        }
                    }
                });
            });
        });
        Timer.setCooldown(e.getEntity().getUniqueId(), 5000, "delay_restock_trades");
        // All trades are being restocked, though this event fires for each MerchantRecipe restocked.
        // Since that is unnecessary, and we purely use the event to detect when a villager restocks normally,
        // we apply a cooldown of a couple seconds to make sure this event doesn't fire repeatedly needlessly
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVillagerDeath(EntityDeathEvent e){
        if (!(e.getEntity() instanceof AbstractVillager) && !(e.getEntity() instanceof IronGolem)) return;
        LivingEntity v = e.getEntity();
        if (e.getEntity() instanceof IronGolem i && i.isPlayerCreated()) return; // Player-created golems are not penalized for getting killed
        boolean wasVillagerKilled = v instanceof AbstractVillager;
        Player killer = v.getKiller();
        Collection<Player> responsiblePlayers = killer != null ? new HashSet<>(Set.of(killer)) : EntityUtils.getNearbyPlayers(v.getLocation(), 128);
        boolean directResponsibility = killer != null;

        AbstractVillager victim = wasVillagerKilled ? (AbstractVillager) v : null;
        if (victim != null) CustomMerchantManager.getMerchantDataPersistence().setData(victim.getUniqueId(), null);
        for (Entity villagerInRange : v.getWorld().getNearbyEntities(v.getLocation(), 128, 128, 128, en -> en instanceof AbstractVillager)){
            AbstractVillager villager = (AbstractVillager) villagerInRange;
            if (villager.equals(v)) continue;

            ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
                CustomMerchantManager.getMerchantData(villager, data ->
                        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
                            if (data == null) return;

                            if (wasVillagerKilled){
                                responsiblePlayers.forEach(p -> {
                                    float renown;
                                    if (directResponsibility) renown = (float) (victim.isAdult() ? renownPenaltyHomicide : renownPenaltyInfanticide);
                                    else renown = (float) (victim.isAdult() ? renownVillagerDeath : renownChildDeath);
                                    CustomMerchantManager.modifyRenownReputation(data, p, renown);

                                    float reputation;
                                    if (directResponsibility) reputation = (float) (victim.isAdult() ? reputationPenaltyHomicide : reputationPenaltyInfanticide);
                                    else reputation = (float) (victim.isAdult() ? reputationVillagerDeath : reputationChildDeath);
                                    CustomMerchantManager.modifyTradingReputation(data, p, reputation);
                                });
                            } else {
                                responsiblePlayers.forEach(p -> {
                                    CustomMerchantManager.modifyRenownReputation(data, p, (float) (directResponsibility ? renownPenaltyTreason : renownGolemDeath));
                                    CustomMerchantManager.modifyTradingReputation(data, p, (float) (directResponsibility ? reputationPenaltyTreason : reputationGolemDeath));
                                });
                            }
                        })
                );
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVillagerAssault(EntityDamageByEntityEvent e){
        if (!(e.getDamager() instanceof Player p) || !(e.getEntity() instanceof AbstractVillager) && !(e.getEntity() instanceof IronGolem) ||
                !Timer.isCooldownPassed(e.getEntity().getUniqueId(), "delay_forgive_punch")) return;
        LivingEntity v = (LivingEntity) e.getEntity();
        if (e.getEntity() instanceof IronGolem i && i.isPlayerCreated()) return; // Player-created golems are not penalized for getting assaulted
        boolean wasVillagerHurt = v instanceof AbstractVillager;
        Timer.setCooldown(v.getUniqueId(), forgivePunchDelay * 20, "delay_forgive_punch");

        AbstractVillager victim = wasVillagerHurt ? (AbstractVillager) v : null;
        for (Entity villagerInRange : v.getWorld().getNearbyEntities(v.getLocation(), 128, 128, 128, en -> en instanceof AbstractVillager)){
            AbstractVillager villager = (AbstractVillager) villagerInRange;

            ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
                CustomMerchantManager.getMerchantData(villager, data ->
                        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
                            if (data == null) return;

                            if (wasVillagerHurt){
                                CustomMerchantManager.modifyRenownReputation(data, p, (float) (victim.isAdult() ? renownAssault : renownChildAssault));
                                CustomMerchantManager.modifyTradingReputation(data, p, (float) (victim.isAdult() ? reputationAssault : reputationChildAssault));
                            } else {
                                CustomMerchantManager.modifyRenownReputation(data, p, (float) (renownGolemAssault));
                                CustomMerchantManager.modifyTradingReputation(data, p, (float) (reputationGolemAssault));
                            }
                        })
                );
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRaidEnd(RaidFinishEvent e){
        if (e.getRaid().getStatus() != Raid.RaidStatus.VICTORY || e.getRaid().getLocation().getWorld() == null) return;

        for (Entity villagerInRange : e.getRaid().getLocation().getWorld().getNearbyEntities(e.getRaid().getLocation(), 128, 128, 128, en -> en instanceof AbstractVillager)){
            AbstractVillager villager = (AbstractVillager) villagerInRange;

            ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
                CustomMerchantManager.getMerchantData(villager, data ->
                        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
                            if (data == null) return;

                            e.getRaid().getHeroes().forEach(p -> {
                                Player player = ValhallaMMO.getInstance().getServer().getPlayer(p);
                                if (player == null) return;
                                CustomMerchantManager.modifyRenownReputation(data, player, (float) renownHero);
                            });
                        })
                );
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemThrow(ItemSpawnEvent e){
        if (e.isCancelled() || e.getEntity().getThrower() == null || !(ValhallaMMO.getInstance().getServer().getEntity(e.getEntity().getThrower()) instanceof AbstractVillager a)) return;
        System.out.println(e.getEntity().getItemStack().getType() + " thrown by villager!");
    }

    public static VirtualMerchant getCurrentActiveVirtualMerchant(Player player){
        return activeTradingMenus.get(player.getUniqueId());
    }

    public static void setActiveTradingMenu(Player player, VirtualMerchant inventory){
        activeTradingMenus.put(player.getUniqueId(), inventory);
    }

    public static Collection<UUID> getTradingMerchants() {
        return tradingMerchants;
    }
}
