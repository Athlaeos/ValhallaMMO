package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.CustomDamageType;
import me.athlaeos.valhallammo.entities.damageindicators.DamageIndicatorRegistry;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
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
    private static final Collection<String> entityDamageCauses = new HashSet<>(Set.of("THORNS", "ENTITY_ATTACK", "ENTITY_SWEEP_ATTACK", "PROJECTILE", "ENTITY_EXPLOSION", "SONIC_BOOM"));
    private static final Collection<String> trueDamage = new HashSet<>(Set.of("VOID", "SONIC_BOOM", "STARVATION", "SUICIDE", "WORLD_BORDER"));

    private static final Map<UUID, String> customDamageCauses = new HashMap<>();
    private static final Map<UUID, UUID> lastDamagedByMap = new HashMap<>();
    private static final Map<UUID, Double> lastDamageTakenMap = new HashMap<>();

    private static final Map<String, Double> physicalDamageTypes = new HashMap<>();

    public EntityDamagedListener(){
        YamlConfiguration c = ValhallaMMO.getPluginConfig();
        for (String type : c.getStringList("armor_effective_types")){
            String[] args = type.split(":");
            physicalDamageTypes.put(args[0], args.length > 1 ? Catch.catchOrElse(() -> StringUtils.parseDouble(args[1]), 1D) : 1D);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamageTaken(EntityDamageEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        if (e instanceof EntityDamageByEntityEvent eve) EntityDamagedListener.setDamager(e.getEntity(), eve.getDamager());
        if (e.getEntity() instanceof LivingEntity l){
            String damageCause = customDamageCauses.getOrDefault(e.getEntity().getUniqueId(), e.getCause().toString());
            CustomDamageType type = CustomDamageType.getCustomType(damageCause);
            double originalDamage = e.getDamage();
            double customDamage = !customDamageEnabled ? e.getDamage() : type == null || type.isFatal() ? calculateCustomDamage(e) : Math.min(l.getHealth() - 1, calculateCustomDamage(e)); // poison damage may never kill the victim
            double damageAfterImmunity = !customDamageEnabled ? e.getDamage() : overrideImmunityFrames(customDamage, l);
            if (damageAfterImmunity <= 0 && type != null && e.getEntityType() != EntityType.ARMOR_STAND) {
                e.setCancelled(true);
                return; // entity is immune, and so damage doesn't need to be calculated further
            }
            lastDamageTakenMap.put(l.getUniqueId(), customDamage);
            boolean applyImmunity = l.getHealth() - customDamage > 0;

            if (DamageIndicatorRegistry.sendDamageIndicator(l, type, customDamage, customDamage - originalDamage)) {
                customDamage = 0;
                e.setDamage(0);
                applyImmunity = true;
            }
            if (e instanceof EntityDamageByEntityEvent d && e.getFinalDamage() == 0 && l instanceof Player p && p.isBlocking() &&
                    EntityUtils.isEntityFacing(p, d.getDamager().getLocation(), EntityAttackListener.getFacingAngleCos())) return; // blocking with shield damage reduction
            final double damage = customDamage;
            if (applyImmunity){
                // custom damage did not kill entity
                Entity lastDamager = e instanceof EntityDamageByEntityEvent eve ? eve.getDamager() : null;

                double iFrameMultiplier = 1 + AccumulativeStatManager.getCachedRelationalStats("IMMUNITY_FRAME_MULTIPLIER", l, lastDamager, 10000, true);
                int iFrameBonus = (int) AccumulativeStatManager.getCachedRelationalStats("IMMUNITY_FRAME_BONUS", l, lastDamager, 10000, true);
                int iFrames = (int) Math.max(0, iFrameMultiplier * (Math.max(0, 10 + iFrameBonus)));

                double currentHealth = l.getHealth();
                if ((type == null || type.isImmuneable()) && customDamage <= 0) e.setCancelled(true);
                if (!customDamageEnabled && l.getHealth() - e.getFinalDamage() <= 0) e.setDamage(0.00001);
                ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                    l.setNoDamageTicks(iFrames);
                    if (customDamageEnabled){
                        AttributeInstance health = l.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                        double maxHealth = health != null ? health.getValue() : -1;
                        if (l.getHealth() > 0) l.setHealth(Math.max(damageCause.equals("POISON") ? 1 : 0, Math.min(maxHealth, currentHealth - damage)));
                    }
                    customDamageCauses.remove(l.getUniqueId());
                }, 1L);
            } else if (customDamageEnabled) {
                // custom damage killed entity
                l.setLastDamageCause(e);
                if (customDamage > 0) DamageIndicatorRegistry.sendDamageIndicator(l, type, customDamage, customDamage - originalDamage);
                if (l.getHealth() > 0) l.setHealth(0.0001); // attempt to ensure that this attack will kill
                if (e.getFinalDamage() == 0) { // if player wouldn't have died even with this little health, force a death (e.g. with resistance V)
                    ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                        l.setLastDamageCause(e);
                        l.setHealth(0);
                    }, 1L);
                } else {
                    ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> customDamageCauses.remove(l.getUniqueId()), 1L);
                }
            }
        }
    }

    public static double calculateCustomDamage(EntityDamageEvent e){
        String damageCause = customDamageCauses.getOrDefault(e.getEntity().getUniqueId(), e.getCause().toString());
        UUID lastDamagerUUID = entityDamageCauses.contains(e.getCause().toString()) ? lastDamagedByMap.get(e.getEntity().getUniqueId()) : null;
        Entity lastDamager = lastDamagerUUID == null ? null : ValhallaMMO.getInstance().getServer().getEntity(lastDamagerUUID);

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

    private double overrideImmunityFrames(double customDamage, LivingEntity l){
        if (l.getNoDamageTicks() > 0) {
            // immunity frames should be overwritten if, during them, the entity takes a hit of damage greater than
            // the last. In this case the entity takes the difference between the last damage and current damage.
            EntityDamageEvent previousEvent = l.getLastDamageCause();
            double lastDamage = lastDamageTakenMap.getOrDefault(l.getUniqueId(), previousEvent == null ? 0 : previousEvent.getDamage());
            if (customDamage > lastDamage) {
                return customDamage - lastDamage;
            }
            return 0; // entity remains immune, return false
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
}
