package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.EntityClassification;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.attributes.AttributeWrapper;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.skills.skills.implementations.archery.ArrowBehaviorRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.util.Vector;

import java.util.*;

public class ProjectileListener implements Listener {
    private final Map<Block, ItemStack> dispensedItems = new HashMap<>();
    private final double inaccuracyConstant = ValhallaMMO.getPluginConfig().getDouble("inaccuracy_constant", 0.015);

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLaunch(ProjectileLaunchEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        ItemStack projectile;
        if (e.getEntity() instanceof ThrowableProjectile t) {
            projectile = t.getItem().clone();
            ItemUtils.storeItem(e.getEntity(), projectile);
        } else if (e.getEntity().getShooter() instanceof BlockProjectileSource b){
            projectile = dispensedItems.get(b.getBlock());
            ItemUtils.storeItem(e.getEntity(), projectile);
        } else projectile = ItemUtils.getStoredItem(e.getEntity());

        if (ItemUtils.isEmpty(projectile)) return;
        ItemMeta meta = ItemUtils.getItemMeta(projectile);

        if (e.getEntity() instanceof AbstractArrow a && !(a instanceof Trident)) {
            // inaccuracy mechanics only applied to arrows
            Vector direction;
            double inaccuracy = 0;
            if (e.getEntity().getShooter() instanceof BlockProjectileSource b && b.getBlock().getBlockData() instanceof Directional d){
                inaccuracy = ValhallaMMO.getPluginConfig().getDouble("dispenser_inaccuracy", 7);
                direction = d.getFacing().getDirection();
            } else if (e.getEntity() instanceof LivingEntity l) {
                inaccuracy = AccumulativeStatManager.getCachedStats("ARCHERY_INACCURACY", l, 10000, true);
                direction = l.getEyeLocation().getDirection();
            } else direction = e.getEntity().getVelocity();

            AttributeWrapper accuracy = ItemAttributesRegistry.getAttribute(meta, "ARROW_ACCURACY", false);
            if (accuracy != null) inaccuracy = Math.max(0, inaccuracy - accuracy.getValue());

            Vector aV = e.getEntity().getVelocity().clone();
            double strength = aV.length(); // record initial speed of the arrow
            aV = aV.normalize(); // reduce vector lengths to 1
            direction = direction.normalize();
            aV.setX(direction.getX()); // set direction of arrow equal to direction of shooter
            aV.setY(direction.getY());
            aV.setZ(direction.getZ());
            aV.multiply(strength); // restore the initial speed to the arrow

            inaccuracy = Math.max(0, inaccuracy);
            aV.setX(aV.getX() + Utils.getRandom().nextGaussian() * inaccuracyConstant * inaccuracy);
            aV.setY(aV.getY() + Utils.getRandom().nextGaussian() * inaccuracyConstant * inaccuracy);
            aV.setZ(aV.getZ() + Utils.getRandom().nextGaussian() * inaccuracyConstant * inaccuracy);

            e.getEntity().setVelocity(aV);

            setProjectileProperties(e.getEntity(), meta);
        }
        ArrowBehaviorRegistry.execute(e, ArrowBehaviorRegistry.getBehaviors(meta).values());
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShoot(EntityShootBowEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        Entity projectile = e.getProjectile();
        LivingEntity shooter = e.getEntity();
        double speedMultiplier = 1;// + AccumulativeStatManager.getCachedStats("BOW_SHOT_VELOCITY", shooter, 10000, true);

        ItemStack consumable = e.getConsumable();
        ItemStack bow = e.getBow();
        if (!ItemUtils.isEmpty(consumable) && !ItemUtils.isEmpty(bow)){
            ItemMeta consumableMeta = ItemUtils.getItemMeta(consumable);
            AttributeWrapper speedWrapper = ItemAttributesRegistry.getAttribute(consumableMeta, "ARROW_SPEED", false);
            if (speedWrapper != null) speedMultiplier += speedWrapper.getValue();
            speedMultiplier = Math.max(0, speedMultiplier);
            e.getProjectile().setVelocity(e.getProjectile().getVelocity().multiply(speedMultiplier));

            boolean infinityExploitable = CustomFlag.hasFlag(consumableMeta, CustomFlag.INFINITY_EXPLOITABLE);
            boolean hasInfinity = bow.containsEnchantment(Enchantment.ARROW_INFINITE);
            // arrows may be preserved with infinity if they resemble a vanilla arrow, or if they have the infinityExploitable flag
            boolean shouldSave = hasInfinity && (consumable.isSimilar(new ItemStack(Material.ARROW)) || infinityExploitable);
            e.setConsumeItem(!shouldSave);
            if (e.getProjectile() instanceof AbstractArrow a && !(a instanceof Trident)){
                a.setPickupStatus(AbstractArrow.PickupStatus.ALLOWED);
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
        AttributeWrapper speedWrapper = ItemAttributesRegistry.getAttribute(i, "ARROW_SPEED", false);
        if (speedWrapper != null) p.setVelocity(p.getVelocity().multiply(1 + speedWrapper.getValue()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(ProjectileHitEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;

        ItemStack stored = ItemUtils.getStoredItem(e.getEntity());
        if (ItemUtils.isEmpty(stored)) return;
        ArrowBehaviorRegistry.execute(e, ArrowBehaviorRegistry.getBehaviors(ItemUtils.getItemMeta(stored)).values());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickup(PlayerPickupArrowEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.isCancelled()) return;
        if (!(e.getArrow() instanceof Trident)){
            Item i = e.getItem();
            ItemStack stored = ItemUtils.getStoredItem(e.getArrow());
            if (stored == null) return;
            ArrowBehaviorRegistry.execute(e, ArrowBehaviorRegistry.getBehaviors(ItemUtils.getItemMeta(stored)).values());
            i.setItemStack(stored);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.isCancelled()) return;
        if (e.getDamager() instanceof Projectile p && e.getEntity() instanceof LivingEntity l && !EntityClassification.matchesClassification(e.getEntity().getType(), EntityClassification.UNALIVE)){
            ItemStack stored = ItemUtils.getStoredItem(p);
            if (ItemUtils.isEmpty(stored)) return;
            ItemMeta storedMeta = ItemUtils.getItemMeta(stored);

            // apply potion effects
            if (p instanceof AbstractArrow && !(p instanceof Trident)){
                cancelNextArrowEffects.add(e.getEntity().getUniqueId());
                for (PotionEffectWrapper wrapper : PotionEffectRegistry.getStoredEffects(storedMeta, false).values()){
                    int duration = (int) Math.floor(wrapper.getDuration() / 8D); // the duration displayed on the item will not
                    // match the actual effect applied with vanilla potion effects. Arrows have their duration reduced to 1/8th
                    if (wrapper.isVanilla()) l.addPotionEffect(new PotionEffect(wrapper.getVanillaEffect(), duration, (int) wrapper.getAmplifier(), false));
                    else PotionEffectRegistry.addEffect(l, new CustomPotionEffect(wrapper, duration, wrapper.getAmplifier()), false, EntityPotionEffectEvent.Cause.ARROW);
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
