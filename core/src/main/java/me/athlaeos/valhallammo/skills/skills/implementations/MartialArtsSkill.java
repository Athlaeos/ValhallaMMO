package me.athlaeos.valhallammo.skills.skills.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.CombatType;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.entities.EntityClassification;
import me.athlaeos.valhallammo.event.EntityCriticallyHitEvent;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.listeners.EntityAttackListener;
import me.athlaeos.valhallammo.listeners.EntityDamagedListener;
import me.athlaeos.valhallammo.listeners.EntitySpawnListener;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MartialArtsProfile;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.potioneffects.implementations.Stun;
import me.athlaeos.valhallammo.skills.ChunkEXPNerf;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.*;
import me.athlaeos.valhallammo.version.AttributeMappings;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

import static me.athlaeos.valhallammo.utility.Utils.oldOrNew;

public class MartialArtsSkill extends Skill implements Listener {
    private final Map<EntityType, Double> entityExpMultipliers = new HashMap<>();
    private double expPerDamage = 0;
    private double spawnerMultiplier = 0;
    private String grappleTooStrongMessage;
    private String meditationPromptQuestion;
    private String meditationInvalidAnswer;
    private String meditationCooldownType;
    private String disarmingCooldownType;
    private boolean playerDisarming;
    private boolean playerDisarmedItemOwnership;
    private boolean mobDisarming;
    private final Map<String, String> meditationPromptAnswer = new HashMap<>();
    private boolean maxHealthLimitation = false;

    public MartialArtsSkill(String type) {
        super(type);
    }

    @Override
    public void loadConfiguration() {
        ValhallaMMO.getInstance().save("skills/martial_arts_progression.yml");
        ValhallaMMO.getInstance().save("skills/martial_arts.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/martial_arts.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/martial_arts_progression.yml").get();

        loadCommonConfig(skillConfig, progressionConfig);

        grappleTooStrongMessage = TranslationManager.translatePlaceholders(skillConfig.getString("grapple_too_strong_message"));
        meditationPromptQuestion = TranslationManager.translatePlaceholders(skillConfig.getString("meditation_question"));
        meditationInvalidAnswer = TranslationManager.translatePlaceholders(skillConfig.getString("meditation_incorrect_answer"));
        meditationCooldownType = TranslationManager.translatePlaceholders(skillConfig.getString("meditation_cooldown_type"));
        disarmingCooldownType = TranslationManager.translatePlaceholders(skillConfig.getString("disarming_cooldown_type"));
        mobDisarming = skillConfig.getBoolean("mob_disarming");
        playerDisarming = skillConfig.getBoolean("player_disarming");
        playerDisarmedItemOwnership = skillConfig.getBoolean("player_disarming_item_ownership");
        maxHealthLimitation = progressionConfig.getBoolean("experience.max_health_limitation");
        ConfigurationSection answerSection = skillConfig.getConfigurationSection("meditation_answer");
        if (answerSection != null){
            for (String answer : answerSection.getKeys(false)){
                meditationPromptAnswer.put(answer, TranslationManager.translatePlaceholders(skillConfig.getString("meditation_answer." + answer, "")));
            }
        }

        ConfigurationSection entitySection = progressionConfig.getConfigurationSection("experience.exp_enemies_nerfed");
        if (entitySection != null){
            entitySection.getKeys(false).forEach(s -> {
                EntityType e = Catch.catchOrElse(() -> EntityType.valueOf(s), null, "Invalid entity type given in martial_arts_progression.yml experience.entity_exp_multipliers." + s);
                if (e == null) return;
                double multiplier = progressionConfig.getDouble("experience.entity_exp_multipliers." + s);
                entityExpMultipliers.put(e, multiplier);
            });
        }

        expPerDamage = progressionConfig.getDouble("experience.exp_per_damage");
        spawnerMultiplier = progressionConfig.getDouble("experience.spawner_spawned_multiplier");

        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    private static final Map<UUID, GrappleDetails> grappleDetails = new HashMap<>();
    private static final Attribute interactionReachAttribute = AttributeMappings.ENTITY_INTERACTION_RANGE.getAttribute();

    @EventHandler(priority = EventPriority.LOW)
    public void onAttemptedAttack(PlayerInteractEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || !EntityUtils.isUnarmed(e.getPlayer())) return;
        if (e.getAction().toString().contains("LEFT_")) {
            // left click
            MartialArtsProfile profile = ProfileCache.getOrCache(e.getPlayer(), MartialArtsProfile.class);
            AttributeInstance reachAttribute = interactionReachAttribute == null ? null : e.getPlayer().getAttribute(interactionReachAttribute);
            if (reachAttribute == null) return;
            double reach = reachAttribute.getValue();

            RayTraceResult result = e.getPlayer().getWorld().rayTraceEntities(e.getPlayer().getEyeLocation(), e.getPlayer().getEyeLocation().getDirection(),
                    reach, 0.1);
            if (result == null) return;

            if (result.getHitEntity() == null){
                // missed, punish
                Timer.setCooldown(e.getPlayer().getUniqueId(), profile.getGrapplingAttackCooldown() * 50, "grappling_attack_cooldown");
                Timer.startTimer(e.getPlayer().getUniqueId(), "time_since_punch");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onGrapple(PlayerInteractEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.isCancelled() ||
                EntityClassification.matchesClassification(e.getRightClicked().getType(), EntityClassification.UNALIVE) ||
                !(e.getRightClicked() instanceof LivingEntity l) ||
                !Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "grappling_attempt_cooldown") ||
                !EntityUtils.isUnarmed(e.getPlayer())) return;
        if (!Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "grappling_attack_cooldown")) return;
        if (!Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "cooldown_disarming")) {
            Timer.sendCooldownStatus(e.getPlayer(), "cooldown_disarming", disarmingCooldownType);
            return;
        }
        MartialArtsProfile grapplerProfile = ProfileCache.getOrCache(e.getPlayer(), MartialArtsProfile.class);
        if (!grapplerProfile.isGrapplingUnlocked()) return;
        boolean canGrapple = true;
        GrappleDetails details = grappleDetails.get(l.getUniqueId());
        if (l instanceof Player grappledPlayer){
            if (details == null || details.isGrappled() || Timer.isCooldownPassed(grappledPlayer.getUniqueId(), "grappling_attack_cooldown")) {
                // If the player is already grappled, or has a lower grappling power than the grappler, they should be grappled
                MartialArtsProfile grappledProfile = ProfileCache.getOrCache(grappledPlayer, MartialArtsProfile.class);
                canGrapple = (details != null && details.isGrappled()) || grappledProfile.getGrapplingPower() < grapplerProfile.getGrapplingPower();
            }
        }
        if (!canGrapple){
            Utils.sendMessage(e.getPlayer(), grappleTooStrongMessage);
            Timer.setCooldown(e.getPlayer().getUniqueId(), 500, "grappling_attempt_cooldown");
            return;
        }
        // can and will grapple
        if (details == null) {
            details = new GrappleDetails(l.getUniqueId(), e.getPlayer().getUniqueId(), grapplerProfile.getStackDuration());
            grappleDetails.put(l.getUniqueId(), details);
        }

        Timer.setCooldown(e.getPlayer().getUniqueId(), grapplerProfile.getGrapplingEffectInterval() * 50, "grappling_attempt_cooldown");
        if (details.grapple(grapplerProfile.getStacksUntilDisarming())){
            if ((playerDisarming && l instanceof Player p &&
                    (!ItemUtils.isEmpty(p.getInventory().getItemInMainHand()) ||
                            !ItemUtils.isEmpty(p.getInventory().getItemInOffHand()))) ||
                    (mobDisarming &&
                            EntityClassification.matchesClassification(l.getType(), EntityClassification.HOSTILE) &&
                            l.getEquipment() != null && !ItemUtils.isEmpty(l.getEquipment().getItemInMainHand())
                    )){
                if (l instanceof Player p && playerDisarming){
                    if (!ItemUtils.isEmpty(p.getInventory().getItemInMainHand())) {
                        p.setCooldown(p.getInventory().getItemInMainHand().getType(), grapplerProfile.getDisarmingDuration() * 50);
                        ItemStack toDrop = p.getInventory().getItemInMainHand().clone();
                        p.getInventory().setItemInMainHand(null);
                        Item item = l.getWorld().dropItem(l.getEyeLocation(), toDrop);
                        if (playerDisarmedItemOwnership) item.setOwner(l.getUniqueId());
                        item.setVelocity(e.getPlayer().getEyeLocation().getDirection().normalize().multiply(0.4));
                    } else {
                        p.setCooldown(p.getInventory().getItemInOffHand().getType(), grapplerProfile.getDisarmingDuration() * 50);
                        ItemStack toDrop = p.getInventory().getItemInOffHand().clone();
                        p.getInventory().setItemInOffHand(null);
                        Item item = l.getWorld().dropItem(l.getEyeLocation(), toDrop);
                        if (playerDisarmedItemOwnership) item.setOwner(l.getUniqueId());
                        item.setVelocity(e.getPlayer().getEyeLocation().getDirection().normalize().multiply(0.4));
                    }
                } else if (l.getEquipment() != null) {
                    ItemStack toDrop = l.getEquipment().getItemInMainHand().clone();
                    l.getEquipment().setItemInMainHand(null);
                    Item item = l.getWorld().dropItem(l.getEyeLocation(), toDrop);
                    item.setVelocity(e.getPlayer().getEyeLocation().getDirection().normalize().multiply(0.4));
                }
            }
            for (StackableEffect effect : StackableEffect.fromString(grapplerProfile.getDisarmingDebuffs())){
                effect.applyPotionEffect(l, e.getPlayer(), grapplerProfile.getStacksUntilDisarming());
            }
            for (StackableEffect effect : StackableEffect.fromString(grapplerProfile.getDisarmingBuffs())){
                effect.applyPotionEffect(e.getPlayer(), e.getPlayer(), grapplerProfile.getStacksUntilDisarming());
            }
            e.getPlayer().getWorld().playSound(l.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5F, 1F);
            e.getPlayer().getWorld().spawnParticle(Particle.valueOf(oldOrNew("BLOCK_DUST", "BLOCK")), l.getEyeLocation().add(0, -(l.getHeight()/2), 0),
                    50, 0.4, 0.1, 0.1, Material.EMERALD_BLOCK.createBlockData());
            Timer.setCooldownIgnoreIfPermission(e.getPlayer(), grapplerProfile.getDisarmingCooldown() * 50, "cooldown_disarming");
        } else {
            for (StackableEffect effect : StackableEffect.fromString(grapplerProfile.getGrapplingEnemyEffects())){
                effect.applyPotionEffect(l, e.getPlayer(), details.grappleStacks);
            }
            for (StackableEffect effect : StackableEffect.fromString(grapplerProfile.getGrapplingSelfEffects())){
                effect.applyPotionEffect(e.getPlayer(), e.getPlayer(), details.grappleStacks);
            }
            e.getPlayer().getWorld().playSound(l.getLocation(), Sound.ITEM_AXE_STRIP, 0.5F, 1F - (details.grappleStacks * 0.07F));
            e.getPlayer().getWorld().spawnParticle(Particle.valueOf(oldOrNew("BLOCK_DUST", "BLOCK")), l.getEyeLocation().add(0, -(l.getHeight()/2), 0),
                    2 * details.grappleStacks, 0.4, 0.1, 0.1, Material.ANVIL.createBlockData());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAttack(EntityDamageByEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        Entity trueDamager = EntityUtils.getTrueDamager(e);
        if (trueDamager instanceof Player p && !(e.getDamager() instanceof Projectile) && e.getEntity() instanceof LivingEntity l){
            if (!EntityUtils.isUnarmed(p)) return;
            endMeditation(p);

            ItemBuilder weapon = e.getDamager() instanceof Trident t && !ItemUtils.isEmpty(t.getItem()) ? new ItemBuilder(t.getItem()) : EntityCache.getAndCacheProperties(p).getMainHand();
            if (weapon != null && WeightClass.getWeightClass(weapon.getMeta()) != WeightClass.WEIGHTLESS) return;
            MartialArtsProfile profile = ProfileCache.getOrCache(p, MartialArtsProfile.class);
            if (profile.getDropKickDamageType().stream().findFirst().orElse("ENTITY_HIT").equals(EntityUtils.getCustomDamageType(l))) return;
            double bonusDamage = 0;
            long timeSinceLastPunch = Timer.getTimerResult(p.getUniqueId(), "time_since_punch");
            int ticks = (int) Math.floor(timeSinceLastPunch / 50D);
            bonusDamage += ticks >= profile.getDelayedPunchTimeMinimum() ? Math.min(profile.getDelayedPunchDamageCap(), ticks * profile.getDelayedPunchDamagePerTick()) : 0;
            e.setDamage(e.getDamage() + (bonusDamage * (p.getFallDistance() > 0 ?
                    1.5 + AccumulativeStatManager.getCachedAttackerRelationalStats("POWER_ATTACK_DAMAGE_MULTIPLIER", p, l, 10000, true) :
                    1)));

            if (p.isSneaking() && EntityUtils.isOnGround(l) && profile.isUppercutUnlocked() && (l instanceof Player ? Timer.isCooldownPassed(p.getUniqueId(), "unarmed_uppercut_pvp_cooldown") : Timer.isCooldownPassed(p.getUniqueId(), "unarmed_uppercut_pve_cooldown"))){
                e.setDamage(e.getDamage() + profile.getUppercutDamage());
                double knockUpMagnitude = profile.getUppercutKnockUpStrength();
                AttributeInstance knockBackResistance = l.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
                if (knockBackResistance != null) knockUpMagnitude *= Math.max(0, 1 - knockBackResistance.getValue());
                final double magnitude = knockUpMagnitude;
                ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () ->
                                l.setVelocity(l.getVelocity().add(new Vector(0, magnitude, 0)))
                        , 1L);

                Timer.setCooldownIgnoreIfPermission(p,
                        (l instanceof Player ? profile.getUppercutPVPCooldown() : profile.getUppercutPVECooldown()) * 50,
                        (l instanceof Player ? "cooldown_uppercut_pvp" : "cooldown_uppercut_pve")
                );
            } else if (!EntityUtils.isOnGround(l) && profile.isDropKickUnlocked()) {
                Vector direction = l.getEyeLocation().toVector().subtract(p.getEyeLocation().toVector()).normalize();
                e.setDamage(e.getDamage() + profile.getDropKickDamage());
                l.setVelocity(direction.multiply(profile.getDropKickKnockBackStrength()));
                final Vector finalDirection = direction;
                new BukkitRunnable(){
                    int duration = 20;
                    @Override
                    public void run() {
                        if (duration <= 0 || !l.isValid() || l.isDead() || EntityUtils.isOnGround(l) || !p.isOnline() || p.isDead()){
                            cancel();
                            return;
                        }
                        duration--;

                        double length = MathUtils.sqrt(l.getVelocity().lengthSquared());
                        RayTraceResult result = l.getWorld().rayTrace(l.getEyeLocation(), finalDirection, length, FluidCollisionMode.NEVER, true, 0.1, (e) -> e instanceof LivingEntity le && !le.equals(l) && !le.equals(p));
                        RayTraceResult result2 = l.getWorld().rayTrace(l.getLocation(), finalDirection, length, FluidCollisionMode.NEVER, true, 0.1, (e) -> e instanceof LivingEntity le && !le.equals(l) && !le.equals(p));
                        Entity hitEntity = (result != null && result.getHitEntity() != null ? result.getHitEntity() : (result2 != null && result2.getHitEntity() != null ? result2.getHitEntity() : null));
                        if (hitEntity != null){
                            double damage = profile.getDropKickWallHitDamage() + (length * profile.getDropKickWallHitDamagePerVelocity());
                            String damageType = profile.getDropKickDamageType().stream().findFirst().orElse("ENTITY_HIT");
                            if (Timer.isCooldownPassed(p.getUniqueId(), "cooldown_dropkick_sound")) {
                                l.getWorld().playSound(l.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.1F, 1F);
                                Timer.setCooldown(p.getUniqueId(), 1000, "cooldown_dropkick_sound");
                            }
                            l.setVelocity(new Vector(0, l.getVelocity().getY(), 0));
                            EntityUtils.damage(l, p, damage, damageType, true);
                            EntityUtils.damage((LivingEntity) hitEntity, p, damage, damageType, true);
                            cancel();
                            return;
                        }

                        Block hitBlock = (result != null && result.getHitBlock() != null ? result.getHitBlock() : (result2 != null && result2.getHitBlock() != null ? result2.getHitBlock() : null));
                        if (hitBlock == null || !hitBlock.getType().isSolid() || !hitBlock.getType().isOccluding()) return;
                        double damage = profile.getDropKickWallHitDamage() + (length * profile.getDropKickWallHitDamagePerVelocity());
                        String damageType = profile.getDropKickDamageType().stream().findFirst().orElse("ENTITY_HIT");
                        if (Timer.isCooldownPassed(p.getUniqueId(), "cooldown_dropkick_sound")) {
                            l.getWorld().playSound(l.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.1F, 1F);
                            Timer.setCooldown(p.getUniqueId(), 1000, "cooldown_dropkick_sound");
                        }
                        ValhallaMMO.getNms().blockParticleAnimation(hitBlock);
                        l.setVelocity(new Vector(0, l.getVelocity().getY(), 0));
                        EntityUtils.damage(l, p, damage, damageType, true);
                        cancel();
                    }
                }.runTaskTimer(ValhallaMMO.getInstance(), 1L, 1L);
            }

            if (profile.doesCritOnBleed() && Bleeder.getBleedingEntities().containsKey(l.getUniqueId())) EntityAttackListener.critNextAttack(p);
            else if (profile.doesCritOnStun() && Stun.isStunned(l)) EntityAttackListener.critNextAttack(p);
            else if (profile.doesCritOnStealth()) {
                boolean facing = EntityUtils.isEntityFacing(l, e.getDamager().getLocation(), EntityAttackListener.getFacingAngleCos());
                if (!facing && p.isSneaking() && !EntityAttackListener.isInCombat(p)) EntityAttackListener.critNextAttack(p);
            }

            Timer.setCooldown(p.getUniqueId(), profile.getGrapplingAttackCooldown() * 50, "grappling_attack_cooldown");
            Timer.startTimer(p.getUniqueId(), "time_since_punch");
        } else if (e.getEntity() instanceof Player p){
            endMeditation(p);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onExpAttack(EntityDamageEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled() ||
                EntityClassification.matchesClassification(e.getEntityType(), EntityClassification.UNALIVE) ||
                e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK || !(e.getEntity() instanceof LivingEntity l) ||
                EntityClassification.matchesClassification(l.getType(), EntityClassification.PASSIVE)) return;
        Entity damager = EntityDamagedListener.getLastDamager(l);
        Player p = damager instanceof Player pl ? pl : damager instanceof Trident t && t.getShooter() instanceof Player pl ? pl : null;
        if (p != null && EntityUtils.isUnarmed(p)){
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                if (e.isCancelled() || !p.isOnline()) return;
                double chunkNerf = EntitySpawnListener.isTrialSpawned(l) ? 1 : ChunkEXPNerf.getChunkEXPNerf(l.getLocation().getChunk(), p, "weapons");
                double entityExpMultiplier = entityExpMultipliers.getOrDefault(l.getType(), 1D);
                addEXP(p,
                        maxHealthLimitation ? (Math.min(EntityUtils.getMaxHP(l), e.getDamage())) : e.getDamage() *
                                expPerDamage *
                                entityExpMultiplier *
                                chunkNerf *
                                (EntitySpawnListener.getSpawnReason(l) == CreatureSpawnEvent.SpawnReason.SPAWNER ? spawnerMultiplier : 1),
                        false,
                        PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);
                if (!EntitySpawnListener.isTrialSpawned(l)) ChunkEXPNerf.increment(l.getLocation().getChunk(), p, "weapons");
            }, 2L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCrit(EntityCriticallyHitEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        if (e.getCritter() instanceof Player p && e.getEntity() instanceof LivingEntity l && EntityUtils.isUnarmed(l)){
            ItemBuilder weapon = EntityCache.getAndCacheProperties(p).getMainHand();
            if (weapon != null && WeightClass.getWeightClass(weapon.getMeta()) != WeightClass.WEIGHTLESS) return;
            MartialArtsProfile profile = ProfileCache.getOrCache(p, MartialArtsProfile.class);
            if (profile.doesBleedOnCrit()) Bleeder.inflictBleed(l, p, CombatType.MELEE_UNARMED);
            if (profile.doesStunOnCrit()) Stun.attemptStun(l, p);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMeditate(PlayerInteractEvent e){
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) ||
                e.getClickedBlock() == null || e.getBlockFace() != BlockFace.UP || getMeditationVehicle(e.getPlayer()) != null ||
                e.getHand() == EquipmentSlot.OFF_HAND || e.getPlayer().isSneaking() || !EntityUtils.isUnarmed(e.getPlayer())) return;
        if (!Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "cooldown_meditation")) {
            Timer.sendCooldownStatus(e.getPlayer(), "cooldown_meditation", meditationCooldownType);
            return;
        }
        MartialArtsProfile profile = ProfileCache.getOrCache(e.getPlayer(), MartialArtsProfile.class);
        if (!profile.isMeditationUnlocked()) return;
        Block rightClicked = e.getClickedBlock();
        if (!profile.getMeditationSittingMaterials().contains(rightClicked.getType().toString())) return;
        if (e.getPlayer().getLocation().getY() < profile.getMeditationElevationRequirement()) return;
        if (profile.isMeditationSkyLightRequirement() && e.getPlayer().getLocation().getBlock().getLightFromSky() < (byte) 15) return;

        Questionnaire questionnaire = new Questionnaire(e.getPlayer(), null, null,
                new Question(meditationPromptQuestion, s -> true, meditationInvalidAnswer)
        ) {
            @Override
            public me.athlaeos.valhallammo.dom.Action<Player> getOnFinish() {
                if (getQuestions().isEmpty()) return super.getOnFinish();
                Question question = getQuestions().getFirst();
                if (question.getAnswer() == null) return super.getOnFinish();
                return (p) -> {
                    String response = meditationPromptAnswer.keySet().stream().filter(question.getAnswer().toLowerCase(Locale.US)::contains).findFirst().orElse(null);
                    if (response == null) return;
                    Utils.sendMessage(p, meditationPromptAnswer.get(response));

                    Location sitLocation = rightClicked.getType().isOccluding() ? rightClicked.getLocation().add(0.5, 0, 0.5) : rightClicked.getLocation().add(0.5, -1, 0.5);

                    ArmorStand seatEntity = rightClicked.getWorld().spawn(sitLocation, ArmorStand.class);
                    seatEntity.setGravity(false);
                    seatEntity.setInvulnerable(true);
                    seatEntity.setSmall(true);
                    seatEntity.setInvisible(true);
                    seatEntity.setBasePlate(false);
                    seatEntity.getPersistentDataContainer().set(KEY_MEDITATION_VEHICLE, PersistentDataType.BYTE, (byte) 0);
                    seatEntity.addPassenger(e.getPlayer());

                    new BukkitRunnable(){
                        final int required = 400;
                        double yaw = e.getPlayer().getEyeLocation().getYaw();
                        double pitch = e.getPlayer().getEyeLocation().getPitch();
                        @Override
                        public void run() {
                            if (!e.getPlayer().isOnline() || e.getPlayer().isDead()) {
                                endMeditation(e.getPlayer());
                                return;
                            }
                            Location eye = e.getPlayer().getEyeLocation();
                            if (Math.abs(eye.getYaw() - yaw) > 0.1 || Math.abs(eye.getPitch() - pitch) > 0.1){
                                // moved eyes
                                resetMeditation(e.getPlayer());
                            }
                            yaw = eye.getYaw();
                            pitch = eye.getPitch();

                            int meditatingFor = meditationTimeTracker.getOrDefault(e.getPlayer().getUniqueId(), 0);

                            if (cancelMeditation.contains(e.getPlayer().getUniqueId())){
                                cancel();
                                cancelMeditation.remove(e.getPlayer().getUniqueId());
                                return;
                            }
                            if (getMeditationVehicle(p) == null) {
                                endMeditation(p);
                                cancel();
                                return;
                            }

                            if (meditatingFor >= required) {
                                ValhallaMMO.getNms().removeUniqueAttribute(p, "meditation_fov_changer", Attribute.GENERIC_MOVEMENT_SPEED);
                                Map<String, Collection<MeditationEffect>> effects = MeditationEffect.fromString(profile.getMeditationBuffs());
                                for (MeditationEffect effect : effects.getOrDefault(response, new HashSet<>())){
                                    effect.applyPotionEffect(e.getPlayer());
                                }
                                p.playSound(p, Sound.BLOCK_BELL_RESONATE, 1F, 1F);
                                Timer.setCooldownIgnoreIfPermission(e.getPlayer(), profile.getMeditationCooldown() * 50, "cooldown_meditation");
                                endMeditation(e.getPlayer());
                                cancel();
                            } else {
                                if (meditatingFor > 100 && meditatingFor <= 200){
                                    if (meditatingFor % 10 == 0) p.playSound(p, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1F, meditatingFor / 100F);
                                } else if (meditatingFor > 200 && meditatingFor <= 220){
                                    ValhallaMMO.getNms().addUniqueAttribute(p, MEDITATION_FOV_UUID, "meditation_fov_changer", Attribute.GENERIC_MOVEMENT_SPEED, Math.pow(1.05, (meditatingFor - 200)) - 1, AttributeModifier.Operation.ADD_SCALAR);
                                } else if (meditatingFor > 220 && meditatingFor <= 390){
                                    ValhallaMMO.getNms().addUniqueAttribute(p, MEDITATION_FOV_UUID, "meditation_fov_changer", Attribute.GENERIC_MOVEMENT_SPEED, -1, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
                                    p.stopAllSounds();
                                    if (meditatingFor == 221){
                                        p.addPotionEffect(new PotionEffect(PotionEffectMappings.DARKNESS.getPotionEffectType(), 180, 0, true, false, false));
                                        p.addPotionEffect(new PotionEffect(PotionEffectMappings.NAUSEA.getPotionEffectType(), 180, 0, true, false, false));
                                    }
                                }

                                meditatingFor++;
                                meditationTimeTracker.put(e.getPlayer().getUniqueId(), meditatingFor);
                            }
                        }
                    }.runTaskTimer(ValhallaMMO.getInstance(), 10L, 1L);
                };
            }
        };
        Questionnaire.startQuestionnaire(e.getPlayer(), questionnaire);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHandSwap(PlayerSwapHandItemsEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName())) return;
        resetMeditation(e.getPlayer());
    }

    private static final UUID MEDITATION_FOV_UUID = UUID.fromString("6bc4beec-6a6f-4c34-8ad3-a07dc5e81226");
    public void resetMeditation(Player player){
        meditationTimeTracker.remove(player.getUniqueId());
        ValhallaMMO.getNms().removeUniqueAttribute(player, "meditation_fov_changer", Attribute.GENERIC_MOVEMENT_SPEED);
    }

    public void endMeditation(Player player){
        resetMeditation(player);
        cancelMeditation.add(player.getUniqueId());
        ArmorStand vehicle = getMeditationVehicle(player);
        if (vehicle == null) return;
        player.leaveVehicle();
        vehicle.remove();
        player.teleport(player.getLocation().add(0, 1, 0));
        player.removePotionEffect(PotionEffectMappings.NAUSEA.getPotionEffectType());
        player.removePotionEffect(PotionEffectMappings.DARKNESS.getPotionEffectType());
    }

    private final Map<UUID, Integer> meditationTimeTracker = new HashMap<>();
    private final Collection<UUID> cancelMeditation = new HashSet<>();

    public static final NamespacedKey KEY_MEDITATION_VEHICLE = new NamespacedKey(ValhallaMMO.getInstance(), "meditation_vehicle");
    public ArmorStand getMeditationVehicle(Player player){
        return player.getVehicle() instanceof ArmorStand a && a.getPersistentDataContainer().has(KEY_MEDITATION_VEHICLE, PersistentDataType.BYTE) ? a : null;
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return MartialArtsProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return 46;
    }

    @Override
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) {
            amount *= (1 + AccumulativeStatManager.getStats("MARTIAL_ARTS_EXP_GAIN", p, true));
        }
        super.addEXP(p, amount, silent, reason);
    }

    private static class GrappleDetails{
        private final UUID grappled;
        private final UUID grappler;
        private final int grappleDuration;
        private long lastGrappledAt;
        private int grappleStacks;

        public GrappleDetails(UUID grappled, UUID grappler, int grappleDuration){
            this.grappled = grappled;
            this.grappler = grappler;
            this.grappleDuration = grappleDuration;
            this.lastGrappledAt = System.currentTimeMillis();
            this.grappleStacks = 0;
        }

        /**
         * If true, the max stacks of grapple have been reached as a result of this interaction.
         * If false, only a stack has been added
         */
        public boolean grapple(int maxStacks){
            if (!isGrappled()) grappleStacks = 0;
            lastGrappledAt = System.currentTimeMillis();
            if (grappleStacks < maxStacks) grappleStacks++;
            return grappleStacks >= maxStacks;
        }

        public boolean isGrappled(){
            return lastGrappledAt + (50L * grappleDuration) >= System.currentTimeMillis();
        }
    }

    private record MeditationEffect(String category, String effect, int duration, float amplifier){
        private void applyPotionEffect(LivingEntity on){
            PotionEffectWrapper potionEffect = PotionEffectRegistry.getEffect(effect);
            if (potionEffect == null) return;

            potionEffect.setAmplifier(amplifier);
            potionEffect.setDuration(duration);
            if (potionEffect.isVanilla()) on.addPotionEffect(new PotionEffect(potionEffect.getVanillaEffect(), duration, (int) Math.round(amplifier), true, false, true));
            else PotionEffectRegistry.addEffect(on, on, new CustomPotionEffect(potionEffect, duration, potionEffect.getAmplifier()), false, 1, EntityPotionEffectEvent.Cause.ATTACK);
        }

        private static final Map<String, MeditationEffect> cachedEffects = new HashMap<>();

        private static Map<String, Collection<MeditationEffect>> fromString(Collection<String> effects){
            Map<String, Collection<MeditationEffect>> meditationEffects = new HashMap<>();
            for (String s : effects){
                if (cachedEffects.containsKey(s)) {
                    MeditationEffect cached = cachedEffects.get(s);
                    Collection<MeditationEffect> currentEffects = meditationEffects.getOrDefault(cached.category, new HashSet<>());
                    currentEffects.add(cached);
                    meditationEffects.put(cached.category, currentEffects);
                    continue;
                }
                String[] args = s.split(":");
                if (args.length < 4) continue;

                String category = args[0];
                String effect = args[1];
                int duration = Catch.catchOrElse(() -> Integer.parseInt(args[2]), -1);
                Float amplifier = Catch.catchOrElse(() -> Float.parseFloat(args[3]), null);
                if (duration < 0 || amplifier == null) continue;

                MeditationEffect meditationEffect = new MeditationEffect(category, effect, duration, amplifier);

                cachedEffects.put(s, meditationEffect);
                Collection<MeditationEffect> currentEffects = meditationEffects.getOrDefault(meditationEffect.category, new HashSet<>());
                currentEffects.add(meditationEffect);
                meditationEffects.put(meditationEffect.category, currentEffects);
            }
            return meditationEffects;
        }
    }

    private record StackableEffect(String effect, int duration, String amplifierFormula, int maxStacks){
        private double getAmplifier(int stacks){
            return me.athlaeos.valhallammo.utility.Utils.eval(amplifierFormula.replace("%stacks%", String.valueOf(stacks)));
        }

        private void applyPotionEffect(LivingEntity on, LivingEntity causedBy, int stacks){
            PotionEffectWrapper potionEffect = PotionEffectRegistry.getEffect(effect);
            if (potionEffect == null) return;
            potionEffect.setAmplifier(getAmplifier(stacks));
            potionEffect.setDuration(duration);
            if (potionEffect.isVanilla()) on.addPotionEffect(new PotionEffect(potionEffect.getVanillaEffect(), duration, (int) Math.round(getAmplifier(stacks) - 1), true, false, true));
            else PotionEffectRegistry.addEffect(on, causedBy, new CustomPotionEffect(potionEffect, duration, potionEffect.getAmplifier()), false, 1, EntityPotionEffectEvent.Cause.ATTACK);
        }

        private static final Map<String, StackableEffect> cachedEffects = new HashMap<>();

        private static Collection<StackableEffect> fromString(Collection<String> effects){
            Map<String, StackableEffect> stackableEffects = new HashMap<>();
            for (String s : effects){
                if (cachedEffects.containsKey(s)) {
                    stackableEffects.put(s, cachedEffects.get(s));
                    continue;
                }
                String[] args = s.split(":");
                if (args.length < 3) continue;
                String effect = args[0];
                int duration = Catch.catchOrElse(() -> Integer.parseInt(args[1]), -1);
                String amplifier = args[2];
                int maxStacks = args.length > 3 ? Catch.catchOrElse(() -> Integer.parseInt(args[3]), -1) : -1;
                if (duration < 0) continue;
                StackableEffect stackableEffect = new StackableEffect(effect, duration, amplifier, maxStacks);
                cachedEffects.put(s, stackableEffect);
            }
            return stackableEffects.values();
        }
    }
}
