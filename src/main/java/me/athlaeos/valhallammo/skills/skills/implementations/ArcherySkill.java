package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.animations.AnimationRegistry;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.CombatType;
import me.athlaeos.valhallammo.event.EntityCriticallyHitEvent;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.listeners.EntityAttackListener;
import me.athlaeos.valhallammo.listeners.EntityDamagedListener;
import me.athlaeos.valhallammo.listeners.EntitySpawnListener;
import me.athlaeos.valhallammo.listeners.ProjectileListener;
import me.athlaeos.valhallammo.particle.implementations.GenericParticle;
import me.athlaeos.valhallammo.particle.implementations.RedstoneParticle;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.ArcheryProfile;
import me.athlaeos.valhallammo.potioneffects.implementations.Stun;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.Bleeder;
import me.athlaeos.valhallammo.utility.*;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ArcherySkill extends Skill implements Listener {
    private final double expBaseBow;
    private final double expBaseCrossbow;
    private final double expDamageBonus;
    private final double expDistanceMultiplierBase;
    private final double expDistanceMultiplierBonus;
    private final int expDistanceLimit;
    private final int damageDistanceLimit;
    private final double expInfinityMultiplier;
    private final double expSpawnerMultiplier;
    private final double damageDiminishingReturnsLimit;
    private final double damageDiminishingReturnsMultiplier;
    private Animation chargedShotActivationAnimation;
    private Animation chargedShotFireAnimation;
    private Animation chargedShotSonicBoomAnimation;
    private Animation chargedShotAmmoAnimation;
    private final Map<EntityType, Double> entityExpMultipliers = new HashMap<>();

    private final Particle trail;
    private final Particle.DustOptions trailOptions;
    private final double sonicBoomRequiredVelocity;

    public ArcherySkill(String type) {
        super(type);
        ValhallaMMO.getInstance().save("skills/archery_progression.yml");
        ValhallaMMO.getInstance().save("skills/archery.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/archery.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/archery_progression.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);

        damageDistanceLimit = skillConfig.getInt("distance_limit");
        damageDiminishingReturnsLimit = skillConfig.getDouble("diminishing_returns_limit");
        damageDiminishingReturnsMultiplier = skillConfig.getDouble("diminishing_returns_multiplier");

        expBaseBow = progressionConfig.getDouble("experience.bow_exp_base");
        expBaseCrossbow = progressionConfig.getDouble("experience.crossbow_exp_base");
        expDamageBonus = progressionConfig.getDouble("experience.damage_exp_bonus");
        expDistanceMultiplierBase = progressionConfig.getDouble("experience.distance_exp_multiplier_base");
        expDistanceMultiplierBonus = progressionConfig.getDouble("experience.distance_exp_multiplier");
        expDistanceLimit = progressionConfig.getInt("experience.distance_limit");
        expInfinityMultiplier = progressionConfig.getDouble("experience.infinity_multiplier");
        expSpawnerMultiplier = progressionConfig.getDouble("experience.spawner_spawned_multiplier");

        ConfigurationSection entitySection = progressionConfig.getConfigurationSection("experience.entity_exp_multipliers");
        if (entitySection != null){
            entitySection.getKeys(false).forEach(s -> {
                EntityType e = Catch.catchOrElse(() -> EntityType.valueOf(s), null, "Invalid entity type given in skills/archery_progression.yml experience.entity_exp_multipliers." + s);
                if (e == null) return;
                double multiplier = progressionConfig.getDouble("experience.entity_exp_multipliers." + s);
                entityExpMultipliers.put(e, multiplier);
            });
        }

        trail = Catch.catchOrElse(() -> Particle.valueOf(skillConfig.getString("charged_shot_trail_particle")), null, "Invalid charged shot fire sound given in skills/archery.yml charged_shot_fire_sound");
        trailOptions = new Particle.DustOptions(Utils.hexToRgb(skillConfig.getString("charged_shot_trail_rgb", "#ffffff")), 0.5f);
        sonicBoomRequiredVelocity = MathUtils.pow(skillConfig.getDouble("charged_shot_sonic_boom_required_velocity"), 2);

        chargedShotSonicBoomAnimation = AnimationRegistry.CHARGED_SHOT_SONIC_BOOM;
        chargedShotAmmoAnimation = AnimationRegistry.CHARGED_SHOT_AMMO;
        chargedShotActivationAnimation = AnimationRegistry.CHARGED_SHOT_ACTIVATION;
        chargedShotFireAnimation = AnimationRegistry.CHARGED_SHOT_FIRE;

        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCrossbowUnload(InventoryClickEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getWhoClicked().getWorld().getName()) || e.isCancelled() ||
                !Timer.isCooldownPassed(e.getWhoClicked().getUniqueId(), "delay_crossbow_unload_attempts") ||
                WorldGuardHook.inDisabledRegion(e.getWhoClicked().getLocation(), (Player) e.getWhoClicked(), WorldGuardHook.VMMO_SKILL_ARCHERY)) return;
        if ((!(e.getClickedInventory() instanceof PlayerInventory)) || !e.isRightClick()) return; // player inventory must be right-clicked
        Timer.setCooldown(e.getWhoClicked().getUniqueId(), 500, "delay_crossbow_unload_attempts"); // setting cooldown between attempts so this can't be spammed with some macro
        if (ItemUtils.isEmpty(e.getCurrentItem()) || e.getCurrentItem().getType() != Material.CROSSBOW) return; // neither items must be empty
        ItemBuilder clicked = new ItemBuilder(e.getCurrentItem());
        if (clicked.getMeta() instanceof CrossbowMeta m){
            List<ItemStack> projectiles = m.getChargedProjectiles();
            if (projectiles.isEmpty()) return;
            projectiles.forEach(i -> ItemUtils.addItem((Player) e.getWhoClicked(), i, true));
            e.getWhoClicked().getWorld().playSound(e.getWhoClicked().getLocation(), Sound.ITEM_CROSSBOW_LOADING_END, 1F, 1F);
            m.setChargedProjectiles(new ArrayList<>());
            e.setCurrentItem(clicked.get());
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.getHand() == EquipmentSlot.OFF_HAND ||
                e.useItemInHand() == Event.Result.DENY || !Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "delay_charged_shot_attempts") ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_SKILL_ARCHERY) ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_COMBAT_CHARGEDSHOT)) return;
        Timer.setCooldown(e.getPlayer().getUniqueId(), 500, "delay_charged_shot_attempts");
        if (!e.getPlayer().isSneaking() && e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        ItemStack mainHand = e.getPlayer().getInventory().getItemInMainHand();
        if (ItemUtils.isEmpty(mainHand) || !e.getPlayer().isSneaking()) return;
        if (mainHand.getType() != Material.BOW && mainHand.getType() != Material.CROSSBOW) return;
        ArcheryProfile profile = ProfileCache.getOrCache(e.getPlayer(), ArcheryProfile.class);
        if (!profile.isChargedShotUnlocked() || profile.getChargedShotCharges() <= 0 || !Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "cooldown_charged_shot")) return;
        chargedShotUsers.put(e.getPlayer().getUniqueId(), new ChargedShotUser(
                profile.getChargedShotCharges(), profile.getChargedShotVelocityBonus(),
                profile.getChargedShotDamageMultiplier(), profile.getChargedShotKnockback(),
                profile.getChargedShotPiercing(), profile.getChargedShotAccuracy(),
                profile.doChargedShotsFireAtFullVelocity(), profile.isChargedShotAntiGravity(),
                profile.doChargedShotCrossbowsInstantlyReload()
        ));
        Timer.setCooldownIgnoreIfPermission(e.getPlayer(), profile.getChargedShotCooldown() * 50, "cooldown_charged_shot");
        if (chargedShotActivationAnimation != null) chargedShotActivationAnimation.animate(e.getPlayer(), e.getPlayer().getEyeLocation(), e.getPlayer().getEyeLocation().getDirection(), 0);
        if (chargedShotAmmoAnimation != null) chargedShotAmmoAnimation.animate(e.getPlayer(), e.getPlayer().getEyeLocation(), e.getPlayer().getEyeLocation().getDirection(), 0);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onArrowHit(EntityDamageByEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        if (!(e.getDamager() instanceof AbstractArrow a) || !(a.getShooter() instanceof Player p) || a instanceof Trident || !(e.getEntity() instanceof LivingEntity v)) return;
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_ARCHERY)) return;

        ArcheryProfile profile = ProfileCache.getOrCache(p, ArcheryProfile.class);
        ItemBuilder bow = ProjectileListener.getBow(a);
        if (bow == null) return;
        boolean hasInfinity = bow.getItem().containsEnchantment(Enchantment.ARROW_INFINITE);
        int distance = (int) p.getLocation().distance(v.getLocation());
        int damageDistance = (int) (Math.min(damageDistanceLimit, distance) / 10D);
        int expDistance = (int) (Math.min(expDistanceLimit, distance) / 10D);

        double damage = e.getDamage();
        double damageDistanceMultiplier = (1 + profile.getDistanceDamageBase()) + (damageDistance * profile.getDistanceDamageBonus());
        damage *= damageDistanceMultiplier;
        double baseExp = bow.getItem().getType() == Material.BOW ? expBaseBow : expBaseCrossbow;
        if (hasInfinity) damage *= (1 + profile.getInfinityDamageMultiplier());
        if (profile.doesCritOnBleed() && Bleeder.getBleedingEntities().containsKey(v.getUniqueId())) EntityAttackListener.critNextAttack(p);
        else if (profile.doesCritOnStun() && Stun.isStunned(v)) EntityAttackListener.critNextAttack(p);
        else if (profile.doesCritOnStealth()) {
            boolean facing = EntityUtils.isEntityFacing(v, a.getLocation(), EntityAttackListener.getFacingAngleCos());
            if (!facing && p.isSneaking() && !EntityAttackListener.isInCombat(p)) EntityAttackListener.critNextAttack(p);
        }
        if (damage > damageDiminishingReturnsLimit){
            double excess = damage - damageDistanceMultiplier;
            damage = damageDiminishingReturnsLimit + (excess * damageDiminishingReturnsMultiplier);
        }
        e.setDamage(damage);

        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            double entityExpMultiplier = entityExpMultipliers.getOrDefault(v.getType(), 1D);
            double exp = ((expDistanceMultiplierBase * baseExp) + (baseExp * expDistanceMultiplierBonus * expDistance)) * entityExpMultiplier;
            if (hasInfinity) exp *= expInfinityMultiplier;
            addEXP(p,
                    (1 + (expDamageBonus * EntityDamagedListener.getLastDamageTaken(v.getUniqueId()))) * exp *
                            entityExpMultiplier *
                            (EntitySpawnListener.getSpawnReason(v) == CreatureSpawnEvent.SpawnReason.SPAWNER ? expSpawnerMultiplier : 1),
                    false,
                    PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
        }, 2L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCrit(EntityCriticallyHitEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        if (e.getCritter() instanceof AbstractArrow a && a.getShooter() instanceof Player p && e.getEntity() instanceof LivingEntity l){
            if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_ARCHERY)) return;
            ArcheryProfile profile = ProfileCache.getOrCache(p, ArcheryProfile.class);
            if (profile.doesBleedOnCrit()) Bleeder.inflictBleed(l, p, CombatType.RANGED);
            if (profile.doesStunOnCrit()) Stun.attemptStun(l, p);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArrowShot(EntityShootBowEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        ItemStack bow = e.getBow();
        ItemStack consumable = e.getConsumable();
        if (!(e.getProjectile() instanceof AbstractArrow a) || a instanceof Trident || ItemUtils.isEmpty(consumable) || ItemUtils.isEmpty(bow)) return;
        if (!(a.getShooter() instanceof Player p) || ProjectileListener.isShotFromMultishot(a)) return;
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_ARCHERY)) return;

        ChargedShotUser user = chargedShotUsers.get(p.getUniqueId());
        if (user == null) return;
        if (user.spendCharge()){
            a.setKnockbackStrength(a.getKnockbackStrength() + user.knockbackBuff);
            a.setPierceLevel(a.getPierceLevel() + user.piercingBuff);
            a.setDamage(a.getDamage() * (1 + user.damageBuff));
            if (user.noGravity) {
                a.setGravity(false);
                // make charged shot disappear after 10 seconds to prevent arrows staying in the air forever
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        e.getProjectile().remove();
                    }
                }.runTaskLater(ValhallaMMO.getInstance(), 200);
            }

            if (chargedShotFireAnimation != null) chargedShotFireAnimation.animate(p, p.getEyeLocation(), p.getEyeLocation().getDirection(), 0);
            if (chargedShotSonicBoomAnimation != null && a.getVelocity().lengthSquared() >= sonicBoomRequiredVelocity) chargedShotSonicBoomAnimation.animate(p, p.getEyeLocation(), p.getEyeLocation().getDirection(), 0);
            if (trail != null) AnimationUtils.trailProjectile(a, trail == Particle.REDSTONE ? new RedstoneParticle(trailOptions) : new GenericParticle(trail), 60);
            if (user.crossbowInstantReload && bow.getType() == Material.CROSSBOW){
                ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                    boolean mainHand = !ItemUtils.isEmpty(p.getInventory().getItemInMainHand()) && p.getInventory().getItemInMainHand().getType() == Material.CROSSBOW;
                    ItemStack crossbow = mainHand ? p.getInventory().getItemInMainHand() : p.getInventory().getItemInOffHand();
                    if (ItemUtils.isEmpty(crossbow) || !(crossbow.getItemMeta() instanceof CrossbowMeta crossbowMeta)) return;
                    crossbowMeta.addChargedProjectile(consumable);
                    crossbow.setItemMeta(crossbowMeta);
                }, 1L);
            }
        } else {
            chargedShotUsers.remove(p.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void arrowShotSpeedMinimalizer(EntityShootBowEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        if (!(e.getProjectile() instanceof AbstractArrow a) || a instanceof Trident || ItemUtils.isEmpty(e.getConsumable()) || ItemUtils.isEmpty(e.getBow())) return;
        if (!(a.getShooter() instanceof Player p)) return;
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_ARCHERY)) return;

        ChargedShotUser user = chargedShotUsers.get(p.getUniqueId());
        if (user != null && user.maxVelocity && user.charges > 0) a.setVelocity(a.getVelocity().normalize().multiply(3));
    }

    public static double getChargedShotDamageBuff(UUID uuid){
        ChargedShotUser user = chargedShotUsers.get(uuid);
        if (user == null) return 0;
        return user.damageBuff;
    }

    public static int getChargedShotCharges(UUID uuid){
        ChargedShotUser user = chargedShotUsers.get(uuid);
        if (user == null) return 0;
        return user.charges;
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return ArcheryProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 55;
    }

    @Override
    public boolean isExperienceScaling() {
        return true;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (WorldGuardHook.inDisabledRegion(p.getLocation(), p, WorldGuardHook.VMMO_SKILL_ARCHERY)) return;
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= (1 + AccumulativeStatManager.getStats("ARCHERY_EXP_GAIN", p, true));
        }
        super.addEXP(p, amount, silent, reason);
    }

    public void setChargedShotActivationAnimation(Animation chargedShotActivationAnimation) {
        this.chargedShotActivationAnimation = chargedShotActivationAnimation;
    }

    public void setChargedShotSonicBoomAnimation(Animation chargedShotSonicBoomAnimation) {
        this.chargedShotSonicBoomAnimation = chargedShotSonicBoomAnimation;
    }

    public void setChargedShotAmmoAnimation(Animation chargedShotAmmoAnimation) {
        this.chargedShotAmmoAnimation = chargedShotAmmoAnimation;
    }

    public void setChargedShotFireAnimation(Animation chargedShotFireAnimation) {
        this.chargedShotFireAnimation = chargedShotFireAnimation;
    }

    public static Map<UUID, ChargedShotUser> chargedShotUsers = new HashMap<>();
    public static Map<UUID, ChargedShotUser> getChargedShotUsers() {
        return chargedShotUsers;
    }

    public static class ChargedShotUser{
        private int charges;
        private final float velocityBuff;
        private final float damageBuff;
        private final int knockbackBuff;
        private final int piercingBuff;
        private final float accuracyBuff;
        private final boolean maxVelocity;
        private final boolean crossbowInstantReload;
        private final boolean noGravity;

        public ChargedShotUser(int charges, float velocity, float damage, int knockback, int piercing, float accuracyBuff, boolean maxVelocity, boolean noGravity, boolean crossbowInstantReload){
            this.charges = charges;
            this.velocityBuff = velocity;
            this.damageBuff = damage;
            this.knockbackBuff = knockback;
            this.piercingBuff = piercing;
            this.accuracyBuff = accuracyBuff;
            this.maxVelocity = maxVelocity;
            this.noGravity = noGravity;
            this.crossbowInstantReload = crossbowInstantReload;
        }

        public boolean spendCharge(){
            charges--;
            return charges >= 0;
        }

        public int getCharges() { return charges; }
        public float getVelocityBuff() { return velocityBuff; }
        public float getDamageBuff() { return damageBuff; }
        public int getKnockbackBuff() { return knockbackBuff; }
        public int getPiercingBuff() { return piercingBuff; }
        public float getAccuracyBuff() { return accuracyBuff; }
        public boolean isMaxVelocity() { return maxVelocity; }
        public boolean isNoGravity() { return noGravity; }
        public boolean isCrossbowInstantReload() { return crossbowInstantReload; }
    }
}
