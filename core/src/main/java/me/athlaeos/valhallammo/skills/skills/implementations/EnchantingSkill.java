package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.animations.AnimationRegistry;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.EnchantmentClassification;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.item.*;
import me.athlaeos.valhallammo.listeners.EntityDamagedListener;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.EnchantingProfile;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.*;

public class EnchantingSkill extends Skill implements Listener {
    private static final Map<String, Animation> elementalHitAnimation = new HashMap<>();
    private final Map<Enchantment, Double> enchantmentBaseValues = new HashMap<>();
    private final Map<Integer, Double> enchantmentLevelMultipliers = new HashMap<>();

    private double experienceSpentConversion = 0;
    private double diminishingReturnsMultiplier = 0;
    private int diminishingReturnsCount = 0;
    private boolean anvilDowngrading = false;
    private final Collection<EntityType> diminishingReturnsEntities = new HashSet<>();
    private final Map<EntityType, Double> entityEXPMultipliers = new HashMap<>();
    private final Map<UUID, Integer> diminishingReturnTallyCounter = new HashMap<>();

    private Animation elementalBladeActivationAnimation = AnimationRegistry.ELEMENTAL_BLADE_ACTIVATION;
    private Animation elementaBladeExpirationAnimation = AnimationRegistry.ELEMENTAL_BLADE_EXPIRATION;

    public void setElementalBladeExpirationAnimation(Animation elementaBladeExpirationAnimation) {
        this.elementaBladeExpirationAnimation = elementaBladeExpirationAnimation;
    }

    public void setElementalBladeActivationAnimation(Animation elementalBladeActivationAnimation) {
        this.elementalBladeActivationAnimation = elementalBladeActivationAnimation;
    }

    public EnchantingSkill(String type) {
        super(type);
    }

    @Override
    public void loadConfiguration() {
        ValhallaMMO.getInstance().save("skills/enchanting_progression.yml");
        ValhallaMMO.getInstance().save("skills/enchanting.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/enchanting.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/enchanting_progression.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);

        this.experienceSpentConversion = progressionConfig.getDouble("experience.exp_gain.experience_spent_conversion", 2);
        this.anvilDowngrading = skillConfig.getBoolean("anvil_downgrading");
        this.diminishingReturnsMultiplier = progressionConfig.getDouble("experience.diminishing_returns.multiplier");
        this.diminishingReturnsCount = progressionConfig.getInt("experience.diminishing_returns.amount");
        progressionConfig.getStringList("experience.diminishing_returns.mobs").forEach(s -> {
            EntityType e = Catch.catchOrElse(() -> EntityType.valueOf(s), null, "Invalid entity type given in skills/enchanting_progression.yml experience.diminishing_returns.on." + s);
            if (e == null) return;
            this.diminishingReturnsEntities.add(e);
        });

        ConfigurationSection expReducedEntitySection = progressionConfig.getConfigurationSection("experience.diminishing_returns.mob_experience");
        if (expReducedEntitySection != null){
            expReducedEntitySection.getKeys(false).forEach(k -> {
                EntityType e = Catch.catchOrElse(() -> EntityType.valueOf(k), null, "Invalid entity type given at skills/enchanting_progression.yml experience.diminishing_returns.mob_experience." + k);
                if (e == null) return;
                double value = progressionConfig.getDouble("experience.diminishing_returns.mob_experience." + k);
                entityEXPMultipliers.put(e, value);
            });
        }

        ConfigurationSection baseEnchantmentValueSection = progressionConfig.getConfigurationSection("experience.exp_gain.enchantment_base");
        if (baseEnchantmentValueSection != null){
            baseEnchantmentValueSection.getKeys(false).forEach((s) -> {
                Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(s.toLowerCase()));
                if (e != null) enchantmentBaseValues.put(e, progressionConfig.getDouble("experience.exp_gain.enchantment_base." + s));
                else ValhallaMMO.logWarning("Invalid enchantment type given at progression_enchanting.yml experience.exp_gain.enchantment_base." + s);
            });
        }

        ConfigurationSection levelMultiplierSection = progressionConfig.getConfigurationSection("experience.exp_gain.enchantment_level_multiplier");
        if (levelMultiplierSection != null){
            levelMultiplierSection.getKeys(false).forEach((s) -> {
                try {
                    enchantmentLevelMultipliers.put(Integer.parseInt(s), progressionConfig.getDouble("experience.exp_gain.enchantment_level_multiplier." + s));
                } catch (NumberFormatException ignored){
                    ValhallaMMO.logWarning("Invalid enchantment level given at skills/enchanting.yml experience.exp_gain.enchantment_level_multiplier." + s);
                }
            });
        }

        elementalHitAnimation.put("LIGHTNING", AnimationRegistry.HIT_ELECTRIC);
        elementalHitAnimation.put("ENTITY_EXPLOSION", AnimationRegistry.HIT_EXPLOSION);
        elementalHitAnimation.put("FIRE", AnimationRegistry.HIT_FIRE);
        elementalHitAnimation.put("FREEZE", AnimationRegistry.HIT_FREEZING);
        elementalHitAnimation.put("MAGIC", AnimationRegistry.HIT_MAGIC);
        elementalHitAnimation.put("NECROTIC", AnimationRegistry.HIT_NECROTIC);
        elementalHitAnimation.put("POISON", AnimationRegistry.HIT_POISON);
        elementalHitAnimation.put("RADIANT", AnimationRegistry.HIT_RADIANT);

        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    public static void setElementalHit(String damageType, Animation animation){
        elementalHitAnimation.put(damageType, animation);
    }

    public static void removeElementalHit(String damageType){
        elementalHitAnimation.remove(damageType);
    }

    @EventHandler
    public void onGrindstoneUsage(InventoryClickEvent e){
        if (WorldGuardHook.inDisabledRegion(e.getWhoClicked().getLocation(), (Player) e.getWhoClicked(), WorldGuardHook.VMMO_SKILL_ENCHANTING)) return;
        if (e.getClickedInventory() instanceof GrindstoneInventory && !e.isCancelled()){
            Timer.setCooldown(e.getWhoClicked().getUniqueId(), 5000, "cancel_essence_multiplication");
        }
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return EnchantingProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 10;
    }

    @Override
    public boolean isExperienceScaling() {
        return true;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_ENCHANTING)) return;
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= (1 + AccumulativeStatManager.getStats("ENCHANTING_EXP_GAIN", p, true));
        }
        super.addEXP(p, amount, silent, reason);
    }

    private static final Map<UUID, Map<Material, Map<Integer, EnchantmentOffer[]>>> storedEnchantmentOffers = new HashMap<>();
    private static final Map<UUID, Integer> enchantmentOfferSkillLevels = new HashMap<>();
    private static final Map<UUID, Map<Enchantment, Integer>> anvilMaxLevelCache = new HashMap<>(); // max anvil levels are cached because up to 3 events are fired in a row when
    // interacting with an anvil, and each would be calculating the max levels which is unnecessary.

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareEnchant(PrepareItemEnchantEvent e) {
        if (ValhallaMMO.isWorldBlacklisted(e.getEnchanter().getWorld().getName()) || e.isCancelled() ||
                WorldGuardHook.inDisabledRegion(e.getEnchanter().getLocation(), e.getEnchanter(), WorldGuardHook.VMMO_SKILL_ENCHANTING)) return;
        Map<Material, Map<Integer, EnchantmentOffer[]>> existingMaterialOffers = storedEnchantmentOffers.getOrDefault(e.getEnchanter().getUniqueId(), new HashMap<>());
        Map<Integer, EnchantmentOffer[]> existingLevelOffers = existingMaterialOffers.getOrDefault(e.getItem().getType(), new HashMap<>());
        Player enchanter = e.getEnchanter();
        ItemBuilder item = new ItemBuilder(e.getItem());
        if (CustomFlag.hasFlag(item.getMeta(), CustomFlag.UNENCHANTABLE)) {
            e.setCancelled(true);
            return;
        }
        if (!existingLevelOffers.containsKey(e.getEnchantmentBonus())){
            int skill = (int) AccumulativeStatManager.getCachedStats("ENCHANTING_QUALITY", enchanter, 10000, true);
            skill = (int) (skill * (1 + AccumulativeStatManager.getCachedStats("ENCHANTING_FRACTION_QUALITY", enchanter, 10000, true)));
            double chance = AccumulativeStatManager.getStats("ENCHANTING_AMPLIFY_CHANCE", e.getEnchanter(), true);

            EnchantingItemPropertyManager.scaleEnchantmentOffers(skill, e.getOffers(), chance);
            EnchantingProfile profile = ProfileCache.getOrCache(enchanter, EnchantingProfile.class);
            for (EnchantmentOffer offer : e.getOffers()){
                if (offer == null) continue;
                int enchantmentBonus = profile.getEnchantmentBonus(offer.getEnchantment()) + profile.getEnchantmentBonus(EnchantmentClassification.getClassification(offer.getEnchantment()));
                offer.setEnchantmentLevel(offer.getEnchantmentLevel() + enchantmentBonus);
            }

            existingLevelOffers.put(e.getEnchantmentBonus(), e.getOffers());
            existingMaterialOffers.put(e.getItem().getType(), existingLevelOffers);
            enchantmentOfferSkillLevels.put(e.getEnchanter().getUniqueId(), skill);
            storedEnchantmentOffers.put(e.getEnchanter().getUniqueId(), existingMaterialOffers);
        } else {
            EnchantmentOffer[] storedOffers = existingLevelOffers.get(e.getEnchantmentBonus());
            for (int i = 0; i < storedOffers.length && i < e.getOffers().length; i++){
                e.getOffers()[i] = storedOffers[i];
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnchantItem(EnchantItemEvent e) {
        if (ValhallaMMO.isWorldBlacklisted(e.getEnchanter().getWorld().getName()) || e.isCancelled() ||
                WorldGuardHook.inDisabledRegion(e.getEnchanter().getLocation(), e.getEnchanter(), WorldGuardHook.VMMO_SKILL_ENCHANTING)) return;
        Player enchanter = e.getEnchanter();
        ItemBuilder item = new ItemBuilder(e.getItem());
        if (CustomFlag.hasFlag(item.getMeta(), CustomFlag.UNENCHANTABLE)) {
            e.setCancelled(true);
            return;
        }

        int skill = (int) AccumulativeStatManager.getCachedStats("ENCHANTING_QUALITY", enchanter, 10000, true);
        skill = (int) (skill * (1 + AccumulativeStatManager.getCachedStats("ENCHANTING_FRACTION_QUALITY", enchanter, 10000, true)));
        double chance = AccumulativeStatManager.getCachedStats("ENCHANTING_AMPLIFY_CHANCE", enchanter, 10000, true);

        EnchantingProfile profile = ProfileCache.getOrCache(enchanter, EnchantingProfile.class);
        for (Enchantment en : e.getEnchantsToAdd().keySet()){
            int enchantmentBonus = profile.getEnchantmentBonus(en) + profile.getEnchantmentBonus(EnchantmentClassification.getClassification(en));
            if (Utils.proc(chance, 0, false)) {
                e.getEnchantsToAdd().put(en, Math.max(1, EnchantingItemPropertyManager.getScaledLevel(en, skill, e.getEnchantsToAdd().get(en)) + enchantmentBonus));
            } else e.getEnchantsToAdd().put(en, Math.max(1, e.getEnchantsToAdd().get(en) + enchantmentBonus));
        }

        Map<Integer, EnchantmentOffer[]> cachedOffers = storedEnchantmentOffers.getOrDefault(enchanter.getUniqueId(), new HashMap<>()).getOrDefault(e.getItem().getType(), new HashMap<>());
        if (enchantmentOfferSkillLevels.getOrDefault(enchanter.getUniqueId(), 0) == skill) {
            offersLoop: for (EnchantmentOffer[] offers : cachedOffers.values()){
                for (EnchantmentOffer offer : offers){
                    if (offer == null) continue;
                    if (offer.getCost() == e.getExpLevelCost() && e.getEnchantsToAdd().containsKey(offer.getEnchantment())) {
                        e.getEnchantsToAdd().put(offer.getEnchantment(), offer.getEnchantmentLevel());
                        break offersLoop;
                    }
                }
            }
        }

        int expSpent = EntityUtils.getTotalExperience(enchanter.getLevel()) - EntityUtils.getTotalExperience(enchanter.getLevel() - (e.whichButton() + 1));
        double exp = EXPForEnchantments(enchanter, e.getEnchantsToAdd()) + (experienceSpentConversion * expSpent);
        if (doDiminishingReturnsApply(enchanter)){
            exp *= diminishingReturnsMultiplier;
            reduceTallyCounter(enchanter);
        }
        addEXP(enchanter, exp, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);

        storedEnchantmentOffers.remove(enchanter.getUniqueId());
        enchantmentOfferSkillLevels.remove(enchanter.getUniqueId());
    }

    private static final Collection<UUID> activeElementalBlade = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onAttack(EntityDamageByEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled() ||
                !EntityDamagedListener.getEntityDamageCauses().contains(e.getCause().toString())) return;
        Entity trueDamager = EntityUtils.getTrueDamager(e);
        if (!(trueDamager instanceof Player p) || !(e.getEntity() instanceof LivingEntity v)) return;
        if (WorldGuardHook.inDisabledRegion(e.getDamager().getLocation(), p, WorldGuardHook.VMMO_SKILL_ENCHANTING)) return;
        EnchantingProfile profile = ProfileCache.getOrCache(p, EnchantingProfile.class);
        if (profile.getElementalDamageTypes().isEmpty()) return;

        boolean enhanced = false;
        if (activeElementalBlade.contains(p.getUniqueId())){
            float expToSpend = profile.getEssenceCostPerHit();
            double refundChance = AccumulativeStatManager.getCachedStats("ENCHANTING_REFUND_CHANCE", p, 10000, true);
            if (Utils.proc(p, refundChance, false)){
                double refundAmount = Math.max(0, Math.min(AccumulativeStatManager.getCachedStats("ENCHANTING_REFUND_AMOUNT", p, 10000, true), 1D));

                expToSpend *= (1 - refundAmount); // exp cost is lowered by the amount "refunded"
            }
            int finalCost = Utils.randomAverage(expToSpend);
            int totalEXP = EntityUtils.getTotalExperience(p);
            if (totalEXP < finalCost) {
                activeElementalBlade.remove(p.getUniqueId());
                Utils.sendActionBar(p, TranslationManager.getTranslation("skill-enchanting-elemental-blade-expiration-actionbar"));
                Utils.sendMessage(p, TranslationManager.getTranslation("skill-enchanting-elemental-blade-expiration-chat"));
                if (elementaBladeExpirationAnimation != null) elementaBladeExpirationAnimation.animate(p, p.getLocation(), p.getEyeLocation().getDirection(), 0);
            } else {
                enhanced = true; // only perform enhanced attacks if player has successfully spent exp
                EntityUtils.setTotalExperience(p, totalEXP - finalCost);
            }
        }

        double conversion = profile.getPassiveElementalDamageConversion();
        double damagePerType;
        if (enhanced) {
            conversion += profile.getActiveElementalDamageConversion();
            damagePerType = ((e.getDamage() * conversion) / profile.getElementalDamageTypes().size()) * (1 + profile.getActiveElementalDamageMultiplier());
            // damage is further amplified by multiplier
        } else {
            damagePerType = (e.getDamage() * conversion) / profile.getElementalDamageTypes().size();
        }
        e.setDamage(e.getDamage() * (1 - conversion)); // damage is reduced by the fraction of it that is converted

        for (String damageType : profile.getElementalDamageTypes()){
            EntityUtils.damage(v, p, damagePerType, damageType);
            if (enhanced) {
                Animation a = elementalHitAnimation.get(damageType);
                if (a != null) a.animate(v, e.getDamager() instanceof Projectile pr ? pr.getLocation() : v.getEyeLocation(), p.getEyeLocation().getDirection(), 0);
            }
        }
    }

    @SuppressWarnings("all")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onHandSwap(PlayerSwapHandItemsEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.isCancelled()) return;
        if (WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_ENCHANTING)) return;
        if (!Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "delay_hand_swap")) return; // to prevent spam, a cooldown of 0.5 seconds is applied
        if (EquipmentClass.isHandHeld(ItemUtils.getItemMeta(e.getMainHandItem()))) return; // if the item has no attack damage attribute, it is not considered a handheld weapo
        EnchantingProfile profile = ProfileCache.getOrCache(e.getPlayer(), EnchantingProfile.class);
        if (profile.getActiveElementalDamageMultiplier() == 0 && profile.getActiveElementalDamageConversion() == 0) return;
        // if the player gains no benefit from enhanced attacks, it is considered not unlocked
        if (activeElementalBlade.contains(e.getPlayer().getUniqueId())) {
            activeElementalBlade.remove(e.getPlayer().getUniqueId());
            Utils.sendActionBar(e.getPlayer(), TranslationManager.getTranslation("skill-enchanting-elemental-blade-expiration-actionbar"));
            Utils.sendMessage(e.getPlayer(), TranslationManager.getTranslation("skill-enchanting-elemental-blade-expiration-chat"));
            if (elementaBladeExpirationAnimation != null) elementaBladeExpirationAnimation.animate(e.getPlayer(), e.getPlayer().getLocation(), e.getPlayer().getEyeLocation().getDirection(), 0);
        } else {
            activeElementalBlade.add(e.getPlayer().getUniqueId());
            Utils.sendActionBar(e.getPlayer(), TranslationManager.getTranslation("skill-enchanting-elemental-blade-activation-actionbar"));
            Utils.sendMessage(e.getPlayer(), TranslationManager.getTranslation("skill-enchanting-elemental-blade-activation-chat"));
            if (elementalBladeActivationAnimation != null) elementalBladeActivationAnimation.animate(e.getPlayer(), e.getPlayer().getLocation(), e.getPlayer().getEyeLocation().getDirection(), 0);
        }
        e.setCancelled(true);
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> e.getPlayer().updateInventory(), 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityKilled(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;
        if (diminishingReturnsEntities.contains(e.getEntityType())) incrementMobTally(e.getEntity().getKiller(), e.getEntityType());

        e.setDroppedExp(Utils.randomAverage(e.getDroppedExp() * entityEXPMultipliers.getOrDefault(e.getEntityType(), 1D)));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExpAbsorb(PlayerExpChangeEvent e){
        if (WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_ENCHANTING)) return;
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || !Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "cancel_essence_multiplication")) return;
        double multiplier = 1 + AccumulativeStatManager.getCachedStats("ENCHANTING_VANILLA_EXP_GAIN", e.getPlayer(), 10000, true);
        e.setAmount(Utils.randomAverage(e.getAmount() * multiplier));
    }

    private double EXPForEnchantments(Player p, Map<Enchantment, Integer> enchantments){
        if (p == null) return 0;
        if (enchantments == null || enchantments.isEmpty()) return 0;
        double amount = 0D;

        for (Enchantment e : enchantments.keySet()){
            double levelMultiplier = enchantmentLevelMultipliers.getOrDefault(enchantments.get(e), 0D);
            double baseAmount = enchantmentBaseValues.getOrDefault(e, 0D);

            amount += baseAmount * levelMultiplier;
        }

        if (doDiminishingReturnsApply(p)){
            amount *= diminishingReturnsMultiplier;
            reduceTallyCounter(p);
        }
        return amount;
    }

    @SuppressWarnings("all")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvilUsage(PrepareAnvilEvent e) {
        Player combiner = (Player) e.getView().getPlayer();
        if (WorldGuardHook.inDisabledRegion(combiner.getLocation(), combiner, WorldGuardHook.VMMO_SKILL_ENCHANTING)) return;
        ItemStack i1 = e.getInventory().getItem(0);
        ItemStack i2 = e.getInventory().getItem(1);
        ItemStack r = e.getResult();
        if (!ItemUtils.isEmpty(i1) && !ItemUtils.isEmpty(i2) && !ItemUtils.isEmpty(r)) {
            ItemBuilder item1 = new ItemBuilder(i1);
            ItemBuilder item2 = new ItemBuilder(i2);
            ItemBuilder result = new ItemBuilder(r);

            Map<Enchantment, Integer> i1Enchantments = item1.getMeta() instanceof EnchantmentStorageMeta m ? m.getStoredEnchants() : item1.getItem().getEnchantments();
            Map<Enchantment, Integer> i2Enchantments = item2.getMeta() instanceof EnchantmentStorageMeta m ? m.getStoredEnchants() : item2.getItem().getEnchantments();
            Map<Enchantment, Integer> resultEnchantments = result.getMeta() instanceof EnchantmentStorageMeta m ? m.getStoredEnchants() : result.getItem().getEnchantments();
            // If item 1 or item 2 have the UNENCHANTABLE tag, and the result has different enchantments from item1 it implies
            // the player attempted to combine items that are unenchantable and should therefore not be combined
            // if the result ends up having the same enchantments as item 1 it can be assumed no enchantments are added to
            // the item, and so this event should not be interfered with. if any of the items in the anvil are empty,
            // nothing is being combined(successfully) and so nothing is wrong
            if (CustomFlag.hasFlag(item1.getMeta(), CustomFlag.UNENCHANTABLE) || CustomFlag.hasFlag(item2.getMeta(), CustomFlag.UNENCHANTABLE)){
                boolean matches = r.getEnchantments().size() == i1.getEnchantments().size();
                if (matches){
                    for (Enchantment en : i1Enchantments.keySet()){
                        if (resultEnchantments.getOrDefault(en, -1).intValue() != i1Enchantments.get(en).intValue()){
                            e.setResult(null);
                            return;
                        }
                    }
                }
            }

            if (item2.getMeta() instanceof EnchantmentStorageMeta m) {
                if (m.getStoredEnchants().isEmpty()) return;
            } else if (item2.getItem().getEnchantments().isEmpty()) return;

            Map<Enchantment, Integer> maxLevels = new HashMap<>();
            if (!anvilMaxLevelCache.containsKey(combiner.getUniqueId())){
                boolean creative = combiner.getGameMode() == GameMode.CREATIVE;
                int skill = (int) AccumulativeStatManager.getCachedStats("ENCHANTING_QUALITY_ANVIL", combiner, 10000, true);
                skill = (int) (skill * (1 + AccumulativeStatManager.getCachedStats("ENCHANTING_FRACTION_QUALITY_ANVIL", combiner, 10000, true)));

                EnchantingProfile profile = ProfileCache.getOrCache(combiner, EnchantingProfile.class);
                for (Enchantment en : Enchantment.values()) {
                    int enchantmentBonus = profile.getEnchantmentBonus(en) + profile.getEnchantmentBonus(EnchantmentClassification.getClassification(en));
                    maxLevels.put(en, en.getMaxLevel() == 1 ? 1 : creative ? Integer.MAX_VALUE : (EnchantingItemPropertyManager.getScaledAnvilLevel(en, skill) + enchantmentBonus));
                }
                anvilMaxLevelCache.put(combiner.getUniqueId(), maxLevels);
            } else {
                maxLevels = anvilMaxLevelCache.get(combiner.getUniqueId());
                if (Timer.isCooldownPassed(combiner.getUniqueId(), "delay_anvil_cache_reset")){
                    ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(
                            ValhallaMMO.getInstance(),
                            () -> anvilMaxLevelCache.remove(combiner.getUniqueId()),
                            5L);
                    Timer.setCooldown(combiner.getUniqueId(), 250, "delay_anvil_cache_reset");
                }
            }

            e.getInventory().setMaximumRepairCost(Integer.MAX_VALUE); // i don't even remember why this is here
            combiner.updateInventory();

            Map<Enchantment, Integer> newEnchantments = combineEnchantments(i1Enchantments, i2Enchantments, maxLevels);
            result.disEnchant();
            for (Enchantment en : resultEnchantments.keySet()){
                if (newEnchantments.containsKey(en)) {
                    result.enchant(en, newEnchantments.get(en));
                }
            }

            e.setResult(result.get());

            if (e.getInventory().getRepairCost() >= 40){
                e.getInventory().setRepairCost(39);
            }

            combiner.updateInventory();
        }
    }

    private Map<Enchantment, Integer> combineEnchantments(Map<Enchantment, Integer> item1Enchantments, Map<Enchantment, Integer> item2Enchantments, Map<Enchantment, Integer> maxAllowed){
        Map<Enchantment, Integer> newEnchantments = new HashMap<>();

        for (Enchantment e : item1Enchantments.keySet()){
            int level = item1Enchantments.get(e);
            int maxLevel = maxAllowed.getOrDefault(e, e.getMaxLevel());
            if (item2Enchantments.containsKey(e)){
                int compareLevel = item2Enchantments.get(e);
                if (level == compareLevel){
                    newEnchantments.put(e, (!anvilDowngrading && maxLevel <= level ? level : Math.min(maxLevel, level + 1)));
                } else {
                    newEnchantments.put(e, (!anvilDowngrading && maxLevel <= level ? level : Math.min(maxLevel, Math.max(level, compareLevel))));
                }
            } else {
                newEnchantments.put(e, (!anvilDowngrading && maxLevel <= level ? level : Math.min(maxLevel, level)));
            }
        }

        for (Enchantment e : item2Enchantments.keySet()){
            int level = item2Enchantments.get(e);
            int maxLevel = maxAllowed.getOrDefault(e, e.getMaxLevel());
            if (!item1Enchantments.containsKey(e)){
                newEnchantments.put(e, (!anvilDowngrading && maxLevel <= level ? level : Math.min(maxLevel, level)));
            }
        }

        return newEnchantments;
    }

    private void incrementMobTally(Player p, EntityType type){
        if (p.hasPermission("valhalla.ignorediminishingreturns")) return;
        if (!diminishingReturnsEntities.contains(type)) return;
        diminishingReturnTallyCounter.put(p.getUniqueId(), diminishingReturnTallyCounter.getOrDefault(p.getUniqueId(), 0) + 1);
    }

    private void reduceTallyCounter(Player p){
        int count = diminishingReturnTallyCounter.getOrDefault(p.getUniqueId(), 0);
        if (count < diminishingReturnsCount) return;
        count -= diminishingReturnsCount;
        diminishingReturnTallyCounter.put(p.getUniqueId(), count);
    }

    private boolean doDiminishingReturnsApply(Player p){
        if (p.hasPermission("valhalla.ignorediminishingreturns")) return false;
        return diminishingReturnTallyCounter.getOrDefault(p.getUniqueId(), 0) >= diminishingReturnsCount;
    }
}
