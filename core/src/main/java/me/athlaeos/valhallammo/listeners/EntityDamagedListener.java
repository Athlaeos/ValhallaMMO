package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.CustomDamageType;
import me.athlaeos.valhallammo.entities.EntityClassification;
import me.athlaeos.valhallammo.entities.damageindicators.DamageIndicatorRegistry;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.potioneffects.EffectResponsibility;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;

public class EntityDamagedListener implements Listener {
    private static final boolean customDamageEnabled = ValhallaMMO.getPluginConfig().getBoolean("custom_damage_system", true);
    private static final Collection<String> entityDamageCauses = new HashSet<>(Set.of("CUSTOM", "THORNS", "ENTITY_ATTACK", "ENTITY_SWEEP_ATTACK", "PROJECTILE", "ENTITY_EXPLOSION", "SONIC_BOOM"));
    private static final Collection<String> trueDamage = new HashSet<>(Set.of("VOID", "SONIC_BOOM", "STARVATION", "DROWNING", "SUICIDE", "WORLD_BORDER", "KILL", "GENERIC_KILL"));

    private static final Map<UUID, String> customDamageCauses = new HashMap<>();
    private static final Map<UUID, UUID> lastDamagedByMap = new HashMap<>();
    private static final Map<UUID, Double> lastDamageTakenMap = new HashMap<>();

    private static final Map<String, Double> physicalDamageTypes = new HashMap<>();

    private static final Map<UUID, Collection<String>> noImmunityNextDamage = new HashMap<>();
    private static final Map<UUID, Map<String, Double>> preparedDamageInstances = new HashMap<>();

    private final boolean pvpOneShotProtection;
    private final boolean pveOneShotProtection;
    private final boolean environmentalOneShotProtection;
    private final double oneShotProtectionCap;
    private final Sound oneShotProtectionSound;

    public EntityDamagedListener(){
        YamlConfiguration c = ValhallaMMO.getPluginConfig();
        for (String type : c.getStringList("armor_effective_types")){
            String[] args = type.split(":");
            physicalDamageTypes.put(args[0], args.length > 1 ? Catch.catchOrElse(() -> StringUtils.parseDouble(args[1]), 1D) : 1D);
        }

        pvpOneShotProtection = c.getBoolean("oneshot_protection_players");
        pveOneShotProtection = c.getBoolean("oneshot_protection_mobs");
        environmentalOneShotProtection = c.getBoolean("oneshot_protection_environment");
        oneShotProtectionCap = c.getDouble("oneshot_protection_limit");
        oneShotProtectionSound = Catch.catchOrElse(() -> Sound.valueOf(c.getString("oneshot_protection_sound")), Sound.ITEM_TOTEM_USE);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamageRecord(EntityDamageEvent e){
        if (e instanceof EntityDamageByEntityEvent eve) EntityDamagedListener.setDamager(e.getEntity(), eve.getDamager());
    }

    private final Map<UUID, Double> healthTracker = new HashMap<>();
    private final Map<UUID, Double> absorptionTracker = new HashMap<>();

    private static final Map<UUID, Runnable> damageProcesses = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageTaken(EntityDamageEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !customDamageEnabled) return;
        if (e.getEntity() instanceof LivingEntity l){
            String damageCause = customDamageCauses.getOrDefault(e.getEntity().getUniqueId(), e.getCause().toString());
            CustomDamageType type = CustomDamageType.getCustomType(damageCause);
            if (type != null) e.setDamage(e.getDamage() * (1 + EffectResponsibility.getResponsibleDamageBuff(e.getEntity(), type)));
            double originalDamage = e.getDamage();
            double customDamage = type == null || type.isFatal() ? calculateCustomDamage(e) : Math.min(l.getHealth() - 1, calculateCustomDamage(e)); // poison damage may never kill the victim

            // custom damage did not kill entity
            Entity lastDamager = lastDamager(e);
            if (e.getEntity() instanceof Player dP && lastDamager instanceof Player aP){
                // pvp damage bonus and resistance mechanic
                double bonus = AccumulativeStatManager.getCachedAttackerRelationalStats("PLAYER_DAMAGE_DEALT", dP, aP, 10000, true);
                customDamage *= 1 + bonus;

                double resistance = AccumulativeStatManager.getCachedRelationalStats("PVP_RESISTANCE", dP, aP, 10000, true);
                customDamage *= 1 - resistance;
            }
            if (!Timer.isCooldownPassed(l.getUniqueId(), "duration_oneshot_protection")){
                customDamage = 0;
            }

            double damageAfterImmunity = overrideImmunityFrames(customDamage, l);
            if (damageAfterImmunity < 0 && e.getEntityType() != EntityType.ARMOR_STAND) {
                e.setCancelled(true);
                return; // entity is immune, and so damage doesn't need to be calculated further
            }
            lastDamageTakenMap.put(l.getUniqueId(), customDamage);
            boolean applyImmunity = (l.getHealth() + l.getAbsorptionAmount()) - customDamage > 0;

            if (DamageIndicatorRegistry.sendDamageIndicator(l, type, customDamage, customDamage - originalDamage)) {
                customDamage = 0;
                e.setDamage(0);
                applyImmunity = true;
            }
            if (e instanceof EntityDamageByEntityEvent && e.getFinalDamage() == 0 && l instanceof Player p && p.isBlocking()) return; // blocking with shield damage reduction

            if (((lastDamager == null || EntityClassification.matchesClassification(lastDamager.getType(), EntityClassification.UNALIVE)) && environmentalOneShotProtection) || ((pvpOneShotProtection && lastDamager instanceof Player) || (pveOneShotProtection && lastDamager != null && !EntityClassification.matchesClassification(lastDamager.getType(), EntityClassification.UNALIVE)))) {
                double oneShotProtectionFraction = AccumulativeStatManager.getCachedRelationalStats("ONESHOT_PROTECTION_FRACTION", l, lastDamager, 10000, true);
                if (oneShotProtectionFraction > 0){
                    AttributeInstance healthAttribute = l.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                    if (healthAttribute != null){
                        double maxHealth = healthAttribute.getValue();
                        double damageUntilOSP = maxHealth * (1 - oneShotProtectionFraction);
                        if (maxHealth * oneShotProtectionCap >= customDamage && customDamage > damageUntilOSP && l.getHealth() > damageUntilOSP){
                            customDamage = damageUntilOSP;
                            applyImmunity = true;
                            Timer.setCooldown(l.getUniqueId(), 500, "duration_oneshot_protection");
                            if (l instanceof Player p){
                                PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
                                Timer.setCooldownIgnoreIfPermission(p, profile.getOneShotProtectionCooldown() * 50, "cooldown_oneshot_protection");
                                Timer.sendCooldownStatus(p, "cooldown_oneshot_protection", TranslationManager.getTranslation("ability_oneshot_protection"));
                                p.playSound(p, oneShotProtectionSound, 1F, 1F);
                            }
                        }
                    }
                }
            }

            final double damage = customDamage;
            if (applyImmunity){
                double iFrameMultiplier = 1 + AccumulativeStatManager.getCachedRelationalStats("IMMUNITY_FRAME_MULTIPLIER", l, lastDamager, 10000, true);
                int iFrameBonus = (int) AccumulativeStatManager.getCachedRelationalStats("IMMUNITY_FRAME_BONUS", l, lastDamager, 10000, true);
                int iFrames = isMarkedNoImmunityOnNextDamageInstance(l, damageCause) ? 0 : (int) Math.max(0, iFrameMultiplier * (Math.max(0, 10 + iFrameBonus)));
                unmarkNextDamageInstanceNoImmunity(l, damageCause);
                double predictedAbsorption = absorptionTracker.getOrDefault(l.getUniqueId(), l.getAbsorptionAmount()) - damage;
                double predictedHealth = healthTracker.getOrDefault(l.getUniqueId(), l.getHealth()) - (predictedAbsorption >= 0 ? 0 : -predictedAbsorption);
                if (predictedAbsorption > 0) absorptionTracker.put(l.getUniqueId(), predictedAbsorption);
                healthTracker.put(l.getUniqueId(), predictedHealth); // if two damage instances occur in rapid succession (such as with bonus damage types)
                // then the predicted health of the entity is recorded and used for additional damage instances. Without this, preceding damage instances
                // would be ignored because the entity's health would not have changed yet at this point and their health would be set assuming they've only
                // taken the last damage instance

                if ((type != null && type.isImmuneable()) && customDamage <= 0) e.setCancelled(true);
                if (l.getHealth() - e.getFinalDamage() <= 0) e.setDamage(0);
                double healthBefore = l.getHealth();
                damageProcesses.put(l.getUniqueId(), () -> {
                    l.setNoDamageTicks(iFrames);
                    AttributeInstance health = l.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                    double maxHealth = health != null ? health.getValue() : -1;
                    if (l.getHealth() > 0) {
                        l.setAbsorptionAmount(Math.max(0, predictedAbsorption));
                        l.setHealth(Math.max(damageCause.equals("POISON") ? 1 : 0, Math.min(maxHealth, predictedHealth)));
                    }
                    customDamageCauses.remove(l.getUniqueId());
                    healthTracker.remove(l.getUniqueId());
                    absorptionTracker.remove(l.getUniqueId());
                    damageProcesses.remove(l.getUniqueId());
                });
                ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                    Runnable r = damageProcesses.get(l.getUniqueId());
                    if (r != null) r.run();
                }, 1L);
            }
//            else if (customDamageEnabled) {
//                // custom damage killed entity
//                if (damageCause.equals("POISON")) {
//                    e.setDamage(0);
//                    l.setHealth(Math.min(l.getHealth(), 1));
//                    ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> customDamageCauses.remove(l.getUniqueId()), 1L);
//                    return;
//                }
//                double previousHealth = l.getHealth();
//                double previousAbsorption = l.getAbsorptionAmount();
//                if (customDamage > 0) DamageIndicatorRegistry.sendDamageIndicator(l, type, customDamage, customDamage - originalDamage);
//                if (l.getHealth() + l.getAbsorptionAmount() > 0) {
//                    l.setAbsorptionAmount(0);
//                    l.setHealth(0.0001); // attempt to ensure that this attack will kill
//                }
//                if (e.getFinalDamage() == 0) { // if player wouldn't have died even with this little health, force a death (e.g. with resistance V)
//                    ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
//                        // l.setLastDamageCause(e);
//                        if (l.getHealth() > 0) l.setHealth(0);
//                    }, 1L);
//                } else {
//                    ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
//                        if (e.isCancelled()) {
//                            l.setAbsorptionAmount(previousAbsorption);
//                            l.setHealth(previousHealth); // if the event was cancelled at this point, restore health to what it was previously
//                        }
//                        customDamageCauses.remove(l.getUniqueId());
//                    }, 1L);
//                }
//            }
        }
    }

    private static Entity lastDamager(EntityDamageEvent e){
        UUID lastDamagerUUID = e instanceof EntityDamageByEntityEvent eve ? eve.getDamager().getUniqueId() : lastDamagedByMap.get(e.getEntity().getUniqueId());
        return lastDamagerUUID == null ? null : ValhallaMMO.getInstance().getServer().getEntity(lastDamagerUUID);
    }

    public static double calculateCustomDamage(EntityDamageEvent e){
        String damageCause = customDamageCauses.getOrDefault(e.getEntity().getUniqueId(), e.getCause().toString());
        Entity lastDamager = lastDamager(e);

        CustomDamageType customDamageType = CustomDamageType.getCustomType(damageCause);

        if (customDamageType != null) {
            double elementalMultiplier = customDamageType.damageMultiplier() == null ? 0 : AccumulativeStatManager.getCachedAttackerRelationalStats(customDamageType.damageMultiplier(), e.getEntity(), lastDamager, 10000, true);
            e.setDamage(e.getDamage() * (1 + elementalMultiplier));

            if (e.getEntity() instanceof LivingEntity l){
                double resistance = customDamageType.resistance() == null ? 0 : AccumulativeStatManager.getCachedRelationalStats(customDamageType.resistance(), l, lastDamager, 10000, true);
                e.setDamage(Math.max(0, e.getDamage() * (1 - resistance)));
            }
        }

        if (e.getEntity() instanceof LivingEntity l){
            if (!trueDamage.contains(damageCause)){
                double generalResistance = AccumulativeStatManager.getCachedRelationalStats("DAMAGE_RESISTANCE", l, lastDamager, 10000, true);
                e.setDamage(Math.max(0, e.getDamage() * (1 - generalResistance)));
            }
        } else return e.getDamage();

        double resistedDamage = e.getDamage();
        YamlConfiguration c = ValhallaMMO.getPluginConfig();
        if (physicalDamageTypes.containsKey(damageCause)){
            double armorEffectiveness = physicalDamageTypes.get(damageCause);
            double totalArmor = AccumulativeStatManager.getCachedRelationalStats("ARMOR_TOTAL", e.getEntity(), lastDamager, 10000, true);
            double toughness = Math.max(0, AccumulativeStatManager.getCachedRelationalStats("TOUGHNESS", e.getEntity(), lastDamager, 2000, true));
            if (totalArmor < 0){
                double negativeArmorDamageDebuff = c.getDouble("negative_armor_damage_buff");
                resistedDamage *= (1 + (-totalArmor * negativeArmorDamageDebuff));
                totalArmor = 0;
            } else {
                totalArmor *= armorEffectiveness;
                toughness *= armorEffectiveness;
            }

            String scaling = c.getString("damage_formula_physical", "%damage% * (15 / (15 + %armor%)) - (%toughness% * 0.15)");
            boolean mode = c.getString("damage_formula_mode", "SET").equalsIgnoreCase("set");
            double minimumFraction = c.getDouble("damage_reduction_cap");
            double damageResult = Utils.eval(scaling
                    .replace("%damage%", String.format("%.4f", resistedDamage))
                    .replace("%armor%", String.format("%.4f", totalArmor))
                    .replace("%toughness%", String.format("%.4f", toughness)));

            if (mode) return Math.max(damageResult, minimumFraction * resistedDamage);
            else return resistedDamage * Math.max(minimumFraction, damageResult);
        } else return resistedDamage;
    }

    /**
     * Compares damage taken to entity immunity frames and their last damage taken to determine if the entity should take
     * damage equal to the difference of the current damage amount and previous damage amount.
     * <br>
     * If an entity is immune due to some damage taken, and they take a hit of damage greater than this previous amount, then
     * they will take the difference between those damage numbers regardless of immunity.
     * <br>
     * @param customDamage The damage taken through immunity
     * @param l the entity
     * @return -1 if damage is blocked from immunity, or the damage taken if not
     */
    private double overrideImmunityFrames(double customDamage, LivingEntity l){
        if (l.getNoDamageTicks() > 0) {
            // immunity frames should be overwritten if, during them, the entity takes a hit of damage greater than
            // the last. In this case the entity takes the difference between the last damage and current damage.
            EntityDamageEvent previousEvent = l.getLastDamageCause();
            double lastDamage = lastDamageTakenMap.getOrDefault(l.getUniqueId(), previousEvent == null ? 0 : previousEvent.getDamage());
            if (customDamage > lastDamage) return customDamage - lastDamage;
            return -1; // entity remains immune, return false
        }
        return customDamage;
    }

    public static double getLastDamageTaken(UUID entity){
        return lastDamageTakenMap.getOrDefault(entity, 0D);
    }

    public static double getLastDamageTaken(UUID entity, double def){
        return lastDamageTakenMap.getOrDefault(entity, def);
    }

    public static void setDamager(Entity attacked, Entity attacker){
        lastDamagedByMap.put(attacked.getUniqueId(), attacker.getUniqueId());
    }

    public static void setCustomDamageCause(UUID uuid, String customCause){
        customDamageCauses.put(uuid, customCause);
    }

    public static String getLastDamageCause(LivingEntity e){
        return customDamageCauses.getOrDefault(e.getUniqueId(), e.getLastDamageCause() == null ? null : e.getLastDamageCause().getCause().toString());
    }

    /**
     * Returns the entity which last damaged the given entity. If none were registered by the plugin, the last damage cause's damager will be returned if present.
     * @param e the entity to check which entity last damaged them
     * @return the last damager of the entity
     */
    public static Entity getLastDamager(LivingEntity e){
        UUID lastDamagerUUID = lastDamagedByMap.get(e.getUniqueId());
        return lastDamagerUUID == null ?
                (e.getLastDamageCause() instanceof EntityDamageByEntityEvent d ? d.getDamager() : null) :
                ValhallaMMO.getInstance().getServer().getEntity(lastDamagerUUID);
    }

    public static Collection<String> getEntityDamageCauses() {
        return entityDamageCauses;
    }

    public static void markNextDamageInstanceNoImmunity(Entity entity, String toDamageType){
        Collection<String> damageTypes = noImmunityNextDamage.getOrDefault(entity.getUniqueId(), new HashSet<>());
        damageTypes.add(toDamageType);
        noImmunityNextDamage.put(entity.getUniqueId(), damageTypes);
    }

    public static void unmarkNextDamageInstanceNoImmunity(Entity entity, String toDamageType){
        Collection<String> damageTypes = noImmunityNextDamage.getOrDefault(entity.getUniqueId(), new HashSet<>());
        damageTypes.remove(toDamageType);
        if (damageTypes.isEmpty()) noImmunityNextDamage.remove(entity.getUniqueId());
        else noImmunityNextDamage.put(entity.getUniqueId(), damageTypes);
    }

    public static Collection<String> getNoImmunityToNextDamageInstances(Entity entity){
        return noImmunityNextDamage.getOrDefault(entity.getUniqueId(), new HashSet<>());
    }

    public static boolean isMarkedNoImmunityOnNextDamageInstance(Entity entity, String toDamageType){
        return noImmunityNextDamage.getOrDefault(entity.getUniqueId(), new HashSet<>()).contains(toDamageType);
    }

    public static void prepareDamageInstance(Entity against, String type, double amount){
        Map<String, Double> preparedInstances = preparedDamageInstances.getOrDefault(against.getUniqueId(), new HashMap<>());
        preparedInstances.put(type, amount);
        preparedDamageInstances.put(against.getUniqueId(), preparedInstances);
    }

    public static void removeDamageInstance(Entity against, String type){
        Map<String, Double> preparedInstances = preparedDamageInstances.getOrDefault(against.getUniqueId(), new HashMap<>());
        preparedInstances.remove(type);
        if (preparedInstances.isEmpty()) preparedDamageInstances.remove(against.getUniqueId());
        else preparedDamageInstances.put(against.getUniqueId(), preparedInstances);
    }

    public static double getPreparedInstance(Entity against, String type){
        return preparedDamageInstances.getOrDefault(against.getUniqueId(), new HashMap<>()).getOrDefault(type, 0D);
    }

    public static Map<String, Double> getPreparedInstances(Entity against){
        return preparedDamageInstances.getOrDefault(against.getUniqueId(), new HashMap<>());
    }
}
