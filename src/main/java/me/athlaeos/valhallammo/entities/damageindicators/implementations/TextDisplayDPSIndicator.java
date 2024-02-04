package me.athlaeos.valhallammo.entities.damageindicators.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.CustomDamageType;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.entities.Dummy;
import me.athlaeos.valhallammo.entities.damageindicators.DamageIndicatorStrategy;
import me.athlaeos.valhallammo.listeners.EntityDamagedListener;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.EntityEffect;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TextDisplayDPSIndicator implements DamageIndicatorStrategy {
    private static final boolean dummiesOnly = ValhallaMMO.getPluginConfig().getBoolean("dummies_only");
    private static final String actionBarFormat = TranslationManager.translatePlaceholders(ValhallaMMO.getPluginConfig().getString("damage_indicator_actionbar_format"));

    @Override
    public boolean sendDamage(LivingEntity l, CustomDamageType damageType, double damage, double mitigated) {
        return update(l, damageType, damage, mitigated, false);
    }

    @Override
    public boolean sendCriticalDamage(LivingEntity l, CustomDamageType damageType, double damage, double mitigated) {
        if (Dummy.isDummy(l)) l.playEffect(EntityEffect.ARMOR_STAND_HIT);
        return update(l, damageType, damage, mitigated, true);
    }

    @Override
    public boolean use(LivingEntity l) {
        boolean dummy = Dummy.isDummy(l);
        if (MinecraftVersion.currentVersionOlderThan(MinecraftVersion.MINECRAFT_1_19) || (dummiesOnly && !dummy)) return false;
        if (dummy) l.playEffect(EntityEffect.ARMOR_STAND_HIT);
        return true;
    }

    private static final Map<UUID, Map<CustomDamageType, DPSInstance>> damageIndicatorMap = new HashMap<>();
    private static final Map<UUID, DPSInstance> expiredInstances = new HashMap<>();

    private static final String format = TranslationManager.translatePlaceholders(ValhallaMMO.getPluginConfig().getString("damage_indicator_format", ""));
    private static final String critFormat = TranslationManager.translatePlaceholders(ValhallaMMO.getPluginConfig().getString("damage_indicator_crit", ""));

    /**
     * Creates a damage indicator. 
     * The damage indicator will not just show the damage dealt, but the damage per second. Within 1 second of the initial damage instance,
     * any following damage is added to that damage. If a damage instance is dealt outside of the 1 second delay, it creates a new indicator.
     * @param damaged the damaged entity
     * @param damageType the damage type the entity was damaged with
     * @param damage the damage the entity was damaged with
     * @return true if the damage dealt should be nullified as a result of hitting a dummy, false if damage should simply be dealt
     */
    public static boolean update(LivingEntity damaged, CustomDamageType damageType, double damage, double mitigated, boolean crit){
        if (damageType == null || format == null) return false;
        Map<CustomDamageType, DPSInstance> instances = damageIndicatorMap.getOrDefault(damaged.getUniqueId(), new TreeMap<>());
        if (instances.containsKey(damageType) && !expiredInstances.containsKey(instances.get(damageType).id)) {
            instances.get(damageType).update(damage, mitigated, crit);
        } else {
            DPSInstance instance = new DPSInstance(damaged, damageType, damage, mitigated, crit);
            instances.put(damageType, instance);
            damageIndicatorMap.put(damaged.getUniqueId(), instances);

            int index = 0;
            for (CustomDamageType type : damageIndicatorMap.get(damaged.getUniqueId()).keySet()){
                damageIndicatorMap.get(damaged.getUniqueId()).get(type).priority = index;
                index++;
            }
            instance.runTaskTimer(ValhallaMMO.getInstance(), 0L, 1L);
        }
        return Dummy.isDummy(damaged);
    }

    private static class DPSInstance extends BukkitRunnable {
        private final UUID id;
        private final LivingEntity damaged;
        private final CustomDamageType type;
        private double damage;
        private double mitigated;
        private TextDisplay display;
        private int priority;
        private boolean isCrit = false;

        public DPSInstance(LivingEntity damaged, CustomDamageType type, double damage, double mitigated, boolean crit){
            this.damaged = damaged;
            this.type = type;
            this.damage = damage;
            this.mitigated = mitigated;
            this.id = UUID.randomUUID();
            this.priority = damageIndicatorMap.get(damaged.getUniqueId()) == null ? 0 : damageIndicatorMap.get(damaged.getUniqueId()).size();
            if (format != null){
                isCrit = crit;
                String mitigationString = mitigated < 0.05 || mitigated > 0.05 ? String.format("(%,.1f)", mitigated) : "";
                List<String> lines = Utils.chat(List.of("&l" + format
                        .replace("%icon%", ValhallaMMO.isResourcePackConfigForced() ? type.getHardCodedIndicatorIcon() : type.getIndicatorIcon() == null ? "" : type.getIndicatorIcon())
                        .replace("%dps%", String.format("%,.1f", damage))
                        .replace("%mitigated%", mitigationString)
                        .replace("%crit%", !isCrit || critFormat == null ? "" : critFormat)));
                this.display = damaged.getWorld().spawn(damaged.getEyeLocation().add(0, 0.5 + (priority * 0.2435), 0), TextDisplay.class);
                this.display.setBillboard(Display.Billboard.CENTER);
                this.display.setText(String.join("\n", lines));
                this.display.setAlignment(TextDisplay.TextAlignment.LEFT);
                this.display.setPersistent(false);
                this.display.setSeeThrough(true);
                this.display.setInterpolationDuration(10);

                Entity lastDamager = EntityDamagedListener.getLastDamager(damaged);
                if (lastDamager instanceof Player p && actionBarFormat != null) Utils.sendActionBar(p, "&l" + actionBarFormat
                        .replace("%icon%", ValhallaMMO.isResourcePackConfigForced() ? type.getHardCodedIndicatorIcon() : type.getIndicatorIcon() == null ? "" : type.getIndicatorIcon())
                        .replace("%dps%", String.format("%,.1f", damage))
                        .replace("%mitigated%", mitigationString)
                        .replace("%crit%", !isCrit || critFormat == null ? "" : critFormat));

            }
        }

        public void update(double damage, double mitigated, boolean crit){
            if (remaining > 0){
                this.damage += damage;
                this.mitigated += mitigated;
                bold = BOLD_TIME;
                linger = LINGER_TIME;
                isCrit = crit;

                Entity lastDamager = EntityDamagedListener.getLastDamager(damaged);
                if (lastDamager instanceof Player p && actionBarFormat != null)
                    Utils.sendActionBar(p, "&l" + actionBarFormat
                            .replace("%icon%", ValhallaMMO.isResourcePackConfigForced() ? type.getHardCodedIndicatorIcon() : type.getIndicatorIcon() == null ? "" : type.getIndicatorIcon())
                            .replace("%dps%", String.format("%,.1f", this.damage))
                            .replace("%mitigated%", this.mitigated < 0.05 || this.mitigated > 0.05 ? String.format("(%,.1f)", this.mitigated) : "")
                            .replace("%crit%", !isCrit || critFormat == null ? "" : critFormat));
            } else {
                Map<CustomDamageType, DPSInstance> instances = damageIndicatorMap.getOrDefault(damaged.getUniqueId(), new TreeMap<>());
                DPSInstance existingInstance = instances.get(type);
                expiredInstances.put(existingInstance.id, existingInstance);

                DPSInstance newInstance = new DPSInstance(damaged, type, damage, mitigated, crit);
                instances.put(type, newInstance);
                damageIndicatorMap.put(damaged.getUniqueId(), instances);
                newInstance.runTaskTimer(ValhallaMMO.getInstance(), 0L, 1L);

                int index = 0;
                for (CustomDamageType type : damageIndicatorMap.get(damaged.getUniqueId()).keySet()){
                    damageIndicatorMap.get(damaged.getUniqueId()).get(type).priority = index;
                    index++;
                }
            }
        }

        private static final int BOLD_TIME = 5; // time hologram will be bold after hit
        private static final int LINGER_TIME = 20; // time hologram will stay, rising, after last hit
        private static final double RISE_SPEED = 0.03; // speed at which hologram rises per tick

        private int remaining = 20;
        private int bold = BOLD_TIME;
        private int linger = LINGER_TIME;

        @Override
        public void run() {
            if (format == null || !damaged.isValid() || damaged.isDead()){
                damageIndicatorMap.remove(damaged.getUniqueId());
                cancel();
                return;
            }

            List<String> lines = Utils.chat(List.of((bold > 0 ? "&l" : "") + format
                    .replace("%icon%", ValhallaMMO.isResourcePackConfigForced() ? type.getHardCodedIndicatorIcon() : type.getIndicatorIcon() == null ? "" : type.getIndicatorIcon())
                    .replace("%dps%", String.format("%s%,.1f", (bold > 0 ? "&l" : ""), damage))
                    .replace("%mitigated%", mitigated < 0.05 || mitigated > 0.05 ? String.format("(%s%,.1f)", (bold > 0 ? "&l" : ""), mitigated) : "")
                    .replace("%crit%", !isCrit || critFormat == null ? "" : critFormat)));
            if (bold > 0){
                display.setText(String.join("\n", lines));
                display.teleport(damaged.getEyeLocation().add(0, 0.5 + (priority * 0.2435), 0), PlayerTeleportEvent.TeleportCause.PLUGIN);
                bold--;
            } else if (linger > 0) {
                isCrit = false;
                display.setText(String.join("\n", lines));
                display.teleport(damaged.getEyeLocation().add(0, 0.5 + (priority * 0.2435) + ((LINGER_TIME - linger) * RISE_SPEED), 0), PlayerTeleportEvent.TeleportCause.PLUGIN);
                linger--;
            } else {
                display.remove();
                cancel();
                expiredInstances.remove(id);
                damageIndicatorMap.get(damaged.getUniqueId()).remove(type, this);

                int index = 0;
                for (CustomDamageType type : damageIndicatorMap.get(damaged.getUniqueId()).keySet()){
                    damageIndicatorMap.get(damaged.getUniqueId()).get(type).priority = index;
                    index++;
                }
                return;
            }
            if (remaining <= 0 && !expiredInstances.containsKey(id)){ // time ran out for this instance and it's not yet marked as expired
                expiredInstances.put(id, this);
                damageIndicatorMap.get(damaged.getUniqueId()).remove(type, this);
            }
            remaining--;
        }
    }
}
