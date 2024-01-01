package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.entities.EntityClassification;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.item.arrow_attributes.ArrowBehaviorRegistry;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.util.Vector;

import java.util.*;

public class ProjectileListener implements Listener {
    private final Map<Block, ItemStack> dispensedItems = new HashMap<>();
    private final double infinityAmmoConsumption = ValhallaMMO.getPluginConfig().getDouble("infinity_ammo_consumption_reduction");

    private static final Map<UUID, ItemBuilder> projectileShotByMap = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLaunch(ProjectileLaunchEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        ItemBuilder projectile;
        if (e.getEntity() instanceof ThrowableProjectile t) {
            projectile = new ItemBuilder(t.getItem().clone());
            ItemUtils.storeItem(e.getEntity(), projectile.getItem());
        } else if (e.getEntity().getShooter() instanceof BlockProjectileSource b){
            projectile = new ItemBuilder(dispensedItems.get(b.getBlock()));
            ItemUtils.storeItem(e.getEntity(), projectile.getItem());
        } else projectile = ItemUtils.getStoredItem(e.getEntity());

        if (projectile == null) return;
        ItemMeta meta = projectile.getMeta();

        if (e.getEntity() instanceof AbstractArrow a && !(a instanceof Trident)) {
            // inaccuracy mechanics only applied to arrows
            Vector direction;
            double inaccuracy = 0;
            if (a.getShooter() instanceof BlockProjectileSource b && b.getBlock().getBlockData() instanceof Directional d){
                inaccuracy = ValhallaMMO.getPluginConfig().getDouble("dispenser_inaccuracy", 7);
                direction = d.getFacing().getDirection();
            } else if (a.getShooter() instanceof LivingEntity l) {
                inaccuracy = AccumulativeStatManager.getCachedStats("RANGED_INACCURACY", l, 10000, true);
                direction = l.getEyeLocation().getDirection();
            } else direction = a.getVelocity();

            if (!isShotFromMultishot(a)){
                AttributeWrapper accuracy = ItemAttributesRegistry.getAttribute(meta, "ARROW_ACCURACY", false);
                if (accuracy != null) inaccuracy = Math.max(0, inaccuracy - accuracy.getValue());

                EntityUtils.applyInaccuracy(a, direction, inaccuracy);
            }

            setProjectileProperties(e.getEntity(), meta);
        }
        ArrowBehaviorRegistry.execute(e, ArrowBehaviorRegistry.getBehaviors(meta).values());


        if (e.getEntity().getShooter() instanceof LivingEntity l){
            // thrown object speed mechanic
            if (!(e.getEntity() instanceof AbstractArrow)){
                double speedMultiplier = 1 + AccumulativeStatManager.getCachedStats("THROW_VELOCITY_BONUS", l, 10000, true);
                e.getEntity().setVelocity(e.getEntity().getVelocity().multiply(speedMultiplier));
            }

            // potion saving mechanic
            if (e.getEntity() instanceof ThrownPotion t){
                if (l instanceof Player p && p.getGameMode() != GameMode.CREATIVE){
                    if (Utils.proc(p, AccumulativeStatManager.getCachedStats("POTION_SAVE_CHANCE", p, 10000, true), false)){
                        p.getInventory().addItem(t.getItem());
                    }
                }
            }
        }
    }

    public static boolean isShotFromMultishot(AbstractArrow a){
        return !a.getMetadata("duplicate_multishot").isEmpty();
    }

    public static void setMultishotArrow(AbstractArrow a){
        a.setMetadata("duplicate_multishot", new FixedMetadataValue(ValhallaMMO.getInstance(), true));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDispense(BlockDispenseEvent e){
        Block b = e.getBlock();
        if (ValhallaMMO.isWorldBlacklisted(b.getWorld().getName()) || ItemUtils.isEmpty(e.getItem()) || e.isCancelled()) return;
        ItemStack storedItem = e.getItem().clone();
        storedItem.setAmount(1);
        dispensedItems.put(e.getBlock(), storedItem);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFinalShoot(EntityShootBowEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled() || ItemUtils.isEmpty(e.getConsumable())) return;

        ArrowBehaviorRegistry.execute(e, ArrowBehaviorRegistry.getBehaviors(ItemUtils.getItemMeta(e.getConsumable())).values());
    }

    public static ItemBuilder getBow(AbstractArrow arrow){
        return projectileShotByMap.get(arrow.getUniqueId());
    }

    private final Map<UUID, Integer> crossbowReloads = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShoot(EntityShootBowEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        ItemStack bow = e.getBow();
        if (e.getProjectile() instanceof AbstractArrow a && a.isShotFromCrossbow() && a.getPickupStatus() != AbstractArrow.PickupStatus.ALLOWED && !ItemUtils.isEmpty(bow) && bow.containsEnchantment(Enchantment.MULTISHOT)){
            setMultishotArrow(a); // it's safe to assume these are secondary multishot arrows
        }

        Entity projectile = e.getProjectile();
        LivingEntity shooter = e.getEntity();
        double speedMultiplier = Math.max(0, AccumulativeStatManager.getCachedStats("RANGED_VELOCITY_BONUS", shooter, 10000, true));

        ItemStack consumable = e.getConsumable();
        if (!ItemUtils.isEmpty(consumable) && !ItemUtils.isEmpty(bow)){
            ItemBuilder builderBow = new ItemBuilder(bow);
            projectileShotByMap.put(e.getProjectile().getUniqueId(), builderBow);

            ItemMeta consumableMeta = ItemUtils.getItemMeta(consumable);
            e.getProjectile().setVelocity(e.getProjectile().getVelocity().multiply(speedMultiplier));

            boolean infinityExploitable = CustomFlag.hasFlag(consumableMeta, CustomFlag.INFINITY_EXPLOITABLE);
            boolean hasInfinity = bow.containsEnchantment(Enchantment.ARROW_INFINITE);
            boolean shouldSave = hasInfinity && (consumable.isSimilar(new ItemStack(Material.ARROW)) || infinityExploitable);

            if (shooter instanceof Player p && (e.getProjectile() instanceof AbstractArrow || e.getProjectile() instanceof Firework)){
                double ammoSaveChance = AccumulativeStatManager.getCachedStats("AMMO_SAVE_CHANCE", shooter, 10000, true);
                if (hasInfinity) ammoSaveChance += infinityAmmoConsumption;
                if (Utils.proc(shooter, ammoSaveChance, false)){
                    if (bow.getType() == Material.CROSSBOW){
                        int allowedReloads = 1 + (int) Math.round(AccumulativeStatManager.getCachedStats("CROSSBOW_MAGAZINE", p, 10000, true));
                        int currentReloads = crossbowReloads.getOrDefault(p.getUniqueId(), 0); // reload, increment magazine
                        if (currentReloads + 1 <= allowedReloads){
                            crossbowReloads.put(p.getUniqueId(), currentReloads + 1);
                            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                                boolean mainHand = !ItemUtils.isEmpty(p.getInventory().getItemInMainHand()) && p.getInventory().getItemInMainHand().getType() == Material.CROSSBOW;
                                ItemStack crossbow = mainHand ? p.getInventory().getItemInMainHand() : p.getInventory().getItemInOffHand();
                                if (ItemUtils.isEmpty(crossbow) || !(crossbow.getItemMeta() instanceof CrossbowMeta crossbowMeta)) return;
                                crossbowMeta.addChargedProjectile(consumable);
                                crossbow.setItemMeta(crossbowMeta);
                            }, 1L);
                            if (e.getProjectile() instanceof AbstractArrow a) a.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
                        } else {
                            crossbowReloads.remove(p.getUniqueId()); // do not reload, but reset magazine
                            p.playSound(p, Sound.ITEM_CROSSBOW_LOADING_END, 1F, 1F);
                        }
                        return;
                    } else if (e.getProjectile() instanceof AbstractArrow a && !(a instanceof Trident)){
                        // arrows may be preserved with infinity if they resemble a vanilla arrow, or if they have the infinityExploitable flag
                        e.setConsumeItem(!shouldSave);
                        if (e.shouldConsumeItem()) a.setPickupStatus(AbstractArrow.PickupStatus.ALLOWED);
                        else a.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
                    }
                }
            }

            ItemStack storedItem = consumable.clone();
            storedItem.setAmount(1);
            ItemUtils.storeItem(projectile, storedItem);
        }
    }

    private void setProjectileProperties(Projectile p, ItemMeta i){
        if (p instanceof AbstractArrow a && !(a instanceof Trident)) {
            a.setCritical(false);
            AttributeWrapper damage = ItemAttributesRegistry.getAttribute(i, "ARROW_DAMAGE", false);
            if (damage != null) a.setDamage(Math.max(0, a.getDamage() + damage.getValue()));
            AttributeWrapper piercing = ItemAttributesRegistry.getAttribute(i, "ARROW_PIERCING", false);
            if (piercing != null) a.setPierceLevel(Math.max(0, a.getPierceLevel() + ((int) piercing.getValue())));
        }
        AttributeWrapper speedWrapper = ItemAttributesRegistry.getAttribute(i, "ARROW_VELOCITY", false);
        if (speedWrapper != null) p.setVelocity(p.getVelocity().multiply(1 + speedWrapper.getValue()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(ProjectileHitEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;

        if (e.getHitEntity() instanceof LivingEntity l && e.getEntity() instanceof AbstractArrow a && isShotFromMultishot(a) && ValhallaMMO.getPluginConfig().getBoolean("multishot_all_hit")) {
            int originalDamageTicks = l.getNoDamageTicks();
            a.setDamage(a.getDamage() * ValhallaMMO.getPluginConfig().getDouble("multishot_damage_reduction", 0.5));
            l.setNoDamageTicks(0);
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> l.setNoDamageTicks(originalDamageTicks), 1L);
        }

        projectileShotByMap.remove(e.getEntity().getUniqueId()); // if an arrow hits anything, the bow it was shot from is forgotten

        ItemBuilder stored = ItemUtils.getStoredItem(e.getEntity());
        if (stored == null) return;
        ArrowBehaviorRegistry.execute(e, ArrowBehaviorRegistry.getBehaviors(stored.getMeta()).values());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickup(PlayerPickupArrowEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.isCancelled()) return;
        if (!(e.getArrow() instanceof Trident)){
            projectileShotByMap.remove(e.getArrow().getUniqueId());
            Item i = e.getItem();
            ItemBuilder stored = ItemUtils.getStoredItem(e.getArrow());
            if (stored == null) return;
            ArrowBehaviorRegistry.execute(e, ArrowBehaviorRegistry.getBehaviors(stored.getMeta()).values());
            i.setItemStack(stored.getItem());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        if (e.getDamager() instanceof Projectile p && e.getEntity() instanceof LivingEntity l && !EntityClassification.matchesClassification(e.getEntity().getType(), EntityClassification.UNALIVE)){
            ItemBuilder stored = ItemUtils.getStoredItem(p);
            if (stored == null) return;
            ItemMeta storedMeta = stored.getMeta();
            LivingEntity trueDamager = p.getShooter() instanceof LivingEntity d ? d : null;

            // apply potion effects
            if (!(p instanceof Trident)){
                cancelNextArrowEffects.add(e.getEntity().getUniqueId());
                for (PotionEffectWrapper wrapper : PotionEffectRegistry.getStoredEffects(storedMeta, false).values()){
                    int duration = (int) Math.floor(wrapper.getDuration() / 8D); // the duration displayed on the item will not
                    // match the actual effect applied with vanilla potion effects. Arrows have their duration reduced to 1/8th
                    if (wrapper.isVanilla()) l.addPotionEffect(new PotionEffect(wrapper.getVanillaEffect(), duration, (int) wrapper.getAmplifier(), false));
                    else PotionEffectRegistry.addEffect(l, trueDamager, new CustomPotionEffect(wrapper, duration, wrapper.getAmplifier()), false, 1, EntityPotionEffectEvent.Cause.ARROW);
                }
                ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () ->
                        cancelNextArrowEffects.remove(e.getEntity().getUniqueId()), 1L
                );
            }

            ArrowBehaviorRegistry.execute(e, ArrowBehaviorRegistry.getBehaviors(storedMeta).values());
        }
    }

    private final Collection<UUID> cancelNextArrowEffects = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowPotionEffect(EntityPotionEffectEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        if (e.getCause() == EntityPotionEffectEvent.Cause.ARROW && cancelNextArrowEffects.contains(e.getEntity().getUniqueId())) e.setCancelled(true);
    }
}
