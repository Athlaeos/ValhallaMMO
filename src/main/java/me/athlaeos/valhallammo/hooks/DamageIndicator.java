package me.athlaeos.valhallammo.hooks;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.CustomDamageType;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DamageIndicator extends PluginHook implements Listener {
    private static final Collection<UUID> critIndicator = new HashSet<>();
    private static final Map<UUID, Map<CustomDamageType, DPSInstance>> damageIndicatorMap = new HashMap<>();
    private static final Map<UUID, DPSInstance> expiredInstances = new HashMap<>();

    private static final String format = ValhallaMMO.getPluginConfig().getString("damage_indicator_format", "");
    private static final String crit = ValhallaMMO.getPluginConfig().getString("damage_indicator_crit", "");
    private static final boolean dummiesOnly = ValhallaMMO.getPluginConfig().getBoolean("dummies_only");

    public DamageIndicator() {
        super("DecentHolograms");
    }

    @Override
    public void whenPresent() {
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    /**
     * Creates a damage indicator. If dummies_only is enabled, only dummies will show damage indicators.
     * The damage indicator will not just show the damage dealt, but the damage per second. Within 1 second of the initial damage instance,
     * any following damage is added to that damage. If a damage instance is dealt outside of the 1 second delay, it creates a new indicator.
     * @param damaged the damaged entity
     * @param damageType the damage type the entity was damaged with
     * @param damage the damage the entity was damaged with
     * @return true if the damage dealt should be nullified as a result of hitting a dummy, false if damage should simply be dealt
     */
    public static boolean update(LivingEntity damaged, CustomDamageType damageType, double damage){
        if (damageType == null || format == null || (dummiesOnly && !isDummy(damaged))) return false;
        Map<CustomDamageType, DPSInstance> instances = damageIndicatorMap.getOrDefault(damaged.getUniqueId(), new TreeMap<>());
        if (instances.containsKey(damageType) && !expiredInstances.containsKey(instances.get(damageType).id)) {
            instances.get(damageType).update(damage);
        } else {
            DPSInstance instance = new DPSInstance(damaged, damageType, damage);
            instances.put(damageType, instance);
            damageIndicatorMap.put(damaged.getUniqueId(), instances);

            int index = 0;
            for (CustomDamageType type : damageIndicatorMap.get(damaged.getUniqueId()).keySet()){
                damageIndicatorMap.get(damaged.getUniqueId()).get(type).priority = index;
                index++;
            }
            instance.runTaskTimer(ValhallaMMO.getInstance(), 0L, 1L);
        }
        return isDummy(damaged);
    }

    private static class DPSInstance extends BukkitRunnable {
        private final UUID id;
        private final LivingEntity damaged;
        private final CustomDamageType type;
        private double damage;
        private Hologram hologram;
        private int priority;
        private boolean isCrit = false;

        public DPSInstance(LivingEntity damaged, CustomDamageType type, double damage){
            this.damaged = damaged;
            this.type = type;
            this.damage = damage;
            this.id = UUID.randomUUID();
            this.priority = damageIndicatorMap.get(damaged.getUniqueId()) == null ? 0 : damageIndicatorMap.get(damaged.getUniqueId()).size();
            if (format != null){
                isCrit = critIndicator.remove(damaged.getUniqueId());
                List<String> lines = Utils.chat(List.of("&l" + format
                         .replace("%icon%", ValhallaMMO.isResourcePackConfigForced() ? type.getHardCodedIndicatorIcon() : type.getIndicatorIcon() == null ? "" : type.getIndicatorIcon())
                         .replace("%dps%", String.format("%,.1f", damage))
                         .replace("%crit%", !isCrit || crit == null ? "" : crit)));
                 this.hologram = DHAPI.createHologram(id.toString(), damaged.getEyeLocation().add(0, 0.5 + (priority * 0.2435), 0), lines);
                 this.hologram.setSaveToFile(false);
            }
        }

        public void update(double damage){
            if (remaining > 0){
                this.damage += damage;
                bold = BOLD_TIME;
                linger = LINGER_TIME;
                isCrit = critIndicator.remove(damaged.getUniqueId());
            } else {
                Map<CustomDamageType, DPSInstance> instances = damageIndicatorMap.getOrDefault(damaged.getUniqueId(), new TreeMap<>());
                DPSInstance existingInstance = instances.get(type);
                expiredInstances.put(existingInstance.id, existingInstance);

                DPSInstance newInstance = new DPSInstance(damaged, type, damage);
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
            if (format == null){
                damageIndicatorMap.remove(damaged.getUniqueId());
                cancel();
                return;
            }

            List<String> lines = Utils.chat(List.of((bold > 0 ? "&l" : "") + format
                    .replace("%icon%", ValhallaMMO.isResourcePackConfigForced() ? type.getHardCodedIndicatorIcon() : type.getIndicatorIcon() == null ? "" : type.getIndicatorIcon())
                    .replace("%dps%", String.format("%s%,.1f", (bold > 0 ? "&l" : ""), damage))
                    .replace("%crit%", !isCrit || crit == null ? "" : crit)));
            if (bold > 0){
                DHAPI.setHologramLines(hologram, lines);
                hologram.setLocation(damaged.getEyeLocation().add(0, 0.5 + (priority * 0.2435), 0));
                bold--;
            } else if (linger > 0) {
                critIndicator.remove(damaged.getUniqueId());
                DHAPI.setHologramLines(hologram, lines);
                hologram.setLocation(damaged.getEyeLocation().add(0, 0.5 + (priority * 0.2435) + ((LINGER_TIME - linger) * RISE_SPEED), 0));
                linger--;
            } else {
                DHAPI.removeHologram(hologram.getName());
                hologram.delete();
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

    private static final NamespacedKey DUMMY_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "dummy_head");

    public static boolean isDummyItem(ItemMeta meta){
        return meta.getPersistentDataContainer().has(DUMMY_KEY, PersistentDataType.BYTE);
    }

    public static void setDummyItem(ItemMeta meta, boolean dummy){
        if (dummy) meta.getPersistentDataContainer().set(DUMMY_KEY, PersistentDataType.BYTE, (byte) 1);
        else meta.getPersistentDataContainer().remove(DUMMY_KEY);
    }

    public static boolean isDummy(LivingEntity stand){
        if (stand.getEquipment() == null) return false;
        EntityProperties equipment = EntityCache.getAndCacheProperties(stand);
        if (equipment.getHelmet() == null) return false;
        return equipment.getHelmet().getMeta().getPersistentDataContainer().has(DUMMY_KEY, PersistentDataType.BYTE);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDummyCreation(PlayerInteractAtEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getRightClicked().getWorld().getName()) || e.isCancelled() ||
                !(e.getRightClicked() instanceof ArmorStand a) || a.getEquipment() == null) return;
        ItemStack interactedWith = e.getPlayer().getInventory().getItemInMainHand();
        if (ItemUtils.isEmpty(interactedWith)) return;
        ItemMeta meta = ItemUtils.getItemMeta(interactedWith);
        if (meta == null || !isDummyItem(meta)) return;
        e.setCancelled(true);
        a.getEquipment().setHelmet(interactedWith.clone());
        if (interactedWith.getAmount() <= 1) e.getPlayer().getInventory().setItemInMainHand(null);
        else interactedWith.setAmount(interactedWith.getAmount() - 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDummyItemPlacement(BlockPlaceEvent e){
        if (ItemUtils.isEmpty(e.getItemInHand()) || !e.getItemInHand().getType().isBlock()) return;
        ItemMeta meta = ItemUtils.getItemMeta(e.getItemInHand());
        if (meta == null) return;
        if (isDummyItem(meta)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowHitDummy(ProjectileHitEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled() || e.getHitEntity() == null ||
                e.getHitBlock() != null || !(e.getEntity() instanceof AbstractArrow a) || !(e.getHitEntity() instanceof LivingEntity l) ||
                !isDummy(l)) return;
        ItemBuilder stored = ItemUtils.getStoredItem(a);
        if (stored == null) return;
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            if (a.isValid()) {
                if (a.getPickupStatus() == AbstractArrow.PickupStatus.ALLOWED) a.getWorld().dropItem(a.getLocation(), stored.get());
                a.remove();
            }
        }, 2L);
    }

    public static void markCrit(Entity crit){
        if (ValhallaMMO.isHookFunctional(DamageIndicator.class)) critIndicator.add(crit.getUniqueId());
    }
}
