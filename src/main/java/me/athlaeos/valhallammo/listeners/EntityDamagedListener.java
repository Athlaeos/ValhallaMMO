package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;

public class EntityDamagedListener implements Listener {
    private static final Map<String, String> resistanceStatSources = new HashMap<>();
    private static final Collection<String> entityDamageCauses = new HashSet<>(Set.of("THORNS", "ENTITY_ATTACK", "ENTITY_SWEEP_ATTACK", "PROJECTILE", "ENTITY_EXPLOSION"));
    private static final Collection<String> trueDamage = new HashSet<>(Set.of("VOID", "SONIC_BOOM", "STARVATION", "SUICIDE", "WORLD_BORDER"));

    static {
        registerResistanceEntries("FIRE_RESISTANCE", "FIRE", "LAVA", "MELTING", "FIRE_TICK", "HOT_FLOOR", "DRYOUT");
        registerResistanceEntries("MAGIC_RESISTANCE", "MAGIC", "THORNS", "DRAGON_BREATH");
        registerResistanceEntries("PROJECTILE_RESISTANCE", "PROJECTILE");
        registerResistanceEntries("EXPLOSION_RESISTANCE", "ENTITY_EXPLOSION", "BLOCK_EXPLOSION");
        registerResistanceEntries("POISON_RESISTANCE", "POISON");
        registerResistanceEntries("FALLING_RESISTANCE", "FALL", "FLY_INTO_WALL");
        registerResistanceEntries("MELEE_RESISTANCE", "CONTACT", "ENTITY_ATTACK", "ENTITY_SWEEP_ATTACK");
        registerResistanceEntries("BLEED_RESISTANCE", "BLEED");
        registerResistanceEntries("BLUDGEONING_RESISTANCE", "FALLING_BLOCK", "BLUDGEONING");
        registerResistanceEntries("LIGHTNING_RESISTANCE", "LIGHTNING");
        registerResistanceEntries("FREEZING_RESISTANCE", "FREEZE");
        registerResistanceEntries("RADIANT_RESISTANCE", "RADIANT");
        registerResistanceEntries("NECROTIC_RESISTANCE", "NECROTIC", "WITHER");
    }
    private static final Map<UUID, String> customDamageCauses = new HashMap<>();
    private static final Map<UUID, UUID> lastDamagedByMap = new HashMap<>();
    private static final Map<UUID, Double> lastDamageTakenMap = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamageTaken(EntityDamageEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        if (e.getEntity() instanceof LivingEntity l){
            boolean overrideImmunity = overrideImmunityFrames(e, l);
            if (!overrideImmunity) return; // entity is immune, and so damage doesn't need to be calculated further

            String damageCause = customDamageCauses.getOrDefault(e.getEntity().getUniqueId(), e.getCause().toString());
            double customDamage = Math.max(damageCause.equals("POISON") ? l.getHealth() - 1 : 0, calculateCustomDamage(e)); // poison damage may never kill the victim
            if (l.getHealth() - customDamage > 0){
                // custom damage did not kill entity
                Entity lastDamager = getLastDamager(l);

                double iFrameMultiplier = 1 + AccumulativeStatManager.getCachedRelationalStats("IMMUNITY_FRAME_MULTIPLIER", l, lastDamager, 10000, true);
                int iFrameBonus = (int) AccumulativeStatManager.getCachedRelationalStats("IMMUNITY_FRAME_BONUS", l, lastDamager, 10000, true);
                int iFrames = (int) Math.max(0, iFrameMultiplier * (Math.max(0, 10 + iFrameBonus)));

                double currentHealth = l.getHealth();
                if (customDamage <= 0) e.setCancelled(true);
                if (l.getHealth() - e.getFinalDamage() <= 0) e.setDamage(0.00001);
                ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                    l.setNoDamageTicks(iFrames);
                    AttributeInstance health = l.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                    double maxHealth = health != null ? health.getValue() : -1;
                    if (l.getHealth() > 0) l.setHealth(Math.max(damageCause.equals("POISON") ? 1 : 0, Math.min(maxHealth, currentHealth - customDamage)));
                    lastDamageTakenMap.put(l.getUniqueId(), customDamage);
                    customDamageCauses.remove(l.getUniqueId());
                }, 1L);
            } else {
                // custom damage killed entity
                l.setLastDamageCause(e);
                if (l.getHealth() > 0) l.setHealth(0.0001); // attempt to ensure that this attack will kill
                if (e.getFinalDamage() == 0) { // if player wouldn't have died even with this little health, force a death (e.g. with resistance V)
                    ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                        l.setLastDamageCause(e);
                        l.setHealth(0);
                    }, 1L);
                } else {
                    ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                        lastDamageTakenMap.put(l.getUniqueId(), customDamage);
                        customDamageCauses.remove(l.getUniqueId());
                    }, 1L);
                }
            }
        }
    }

    public static double calculateCustomDamage(EntityDamageEvent e){
        String damageCause = customDamageCauses.getOrDefault(e.getEntity().getUniqueId(), e.getCause().toString());
        UUID lastDamagerUUID = entityDamageCauses.contains(e.getCause().toString()) ? lastDamagedByMap.get(e.getEntity().getUniqueId()) : null;
        Entity lastDamager = lastDamagerUUID == null ? null : ValhallaMMO.getInstance().getServer().getEntity(lastDamagerUUID);
        if (lastDamager == null) lastDamagedByMap.remove(e.getEntity().getUniqueId());

        if (EntityAttackListener.getCustomDamageMultipliers().containsKey(damageCause)) {
            double elementalMultiplier = AccumulativeStatManager.getCachedAttackerRelationalStats(EntityAttackListener.getCustomDamageMultipliers().get(damageCause), e.getEntity(), lastDamager, 10000, true);
            e.setDamage(e.getDamage() * (1 + elementalMultiplier));
        }

        if (e.getEntity() instanceof LivingEntity l){
            double resistance = 0;
            String resistanceStatSource = resistanceStatSources.get(damageCause);
            if (resistanceStatSource != null) resistance = AccumulativeStatManager.getCachedRelationalStats(resistanceStatSource, l, lastDamager, 10000, true);
            double originalDamage = e.getDamage();
            e.setDamage(Math.max(0, originalDamage * (1 - resistance)));

            if (!trueDamage.contains(damageCause)){
                double generalResistance = AccumulativeStatManager.getCachedRelationalStats("DAMAGE_RESISTANCE", l, lastDamager, 10000, true);
                e.setDamage(Math.max(0, e.getDamage() * (1 - generalResistance)));
            }
        } else return e.getDamage();

        double resistedDamage = e.getDamage();
        YamlConfiguration c = ValhallaMMO.getPluginConfig();
        if (c.getStringList("armor_effective_types").contains(damageCause)){
            double totalArmor = AccumulativeStatManager.getCachedRelationalStats("ARMOR_TOTAL", e.getEntity(), lastDamager, 10000, true);
            double toughness = Math.max(0, AccumulativeStatManager.getCachedRelationalStats("TOUGHNESS", e.getEntity(), lastDamager, 2000, true));

            String scaling = c.getString("damage_formula_physical", "%damage% * (10 / (10 + %armor%)) - (%damage%^2 * 0.00005 * %toughness%)");
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

    private boolean overrideImmunityFrames(EntityDamageEvent e, LivingEntity l){
        if (l.getNoDamageTicks() > 0) {
            // immunity frames should be overwritten if, during them, the entity takes a hit of damage greater than
            // the last. In this case the entity takes the difference between the last damage and current damage.
            EntityDamageEvent previousEvent = l.getLastDamageCause();
            double lastDamage = lastDamageTakenMap.getOrDefault(l.getUniqueId(), previousEvent == null ? 0 : previousEvent.getDamage());
            if (e.getDamage() > lastDamage) {
                double difference = e.getDamage() - lastDamage;
                e.setDamage(difference);
                return true;
            }
            return false; // entity remains immune, return false
        }
        return true;
    }

    public static double getLastDamageTaken(UUID entity){
        return lastDamageTakenMap.getOrDefault(entity, 0D);
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

    /**
     * Registers the given damage causes (custom ones allowed too) under a resistance stat. The resistance stat should match
     * one of the stats in {@link AccumulativeStatManager} and then the given damage types are reduced (or increased) by this
     * stat.
     * @param resistance the resistance stat the given damage causes should be affected by
     * @param causes the damage causes that should be affected by the resistance stat
     */
    public static void registerResistanceEntries(String resistance, String... causes){
        for (String cause : causes) resistanceStatSources.put(cause, resistance);
    }

    public static Collection<String> getEntityDamageCauses() {
        return entityDamageCauses;
    }
}
