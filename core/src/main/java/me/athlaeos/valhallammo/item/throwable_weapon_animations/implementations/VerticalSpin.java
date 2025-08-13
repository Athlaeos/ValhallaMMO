package me.athlaeos.valhallammo.item.throwable_weapon_animations.implementations;

import me.athlaeos.valhallammo.entities.EntityClassification;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.item.throwable_weapon_animations.ThrowableWeaponAnimation;
import me.athlaeos.valhallammo.item.throwable_weapon_animations.ThrownItem;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.MathUtils;
import org.bukkit.EntityEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class VerticalSpin extends ThrowableWeaponAnimation {
    public VerticalSpin(String name) {
        super(name);
    }

    private static final double SPINNING_CONSTANT = 0.5d;

    private double currentAngle(ThrownItem item){
        return SPINNING_CONSTANT * item.getTick();
    }

    private EulerAngle currentEulerAngle(ThrownItem item){
        return new EulerAngle(0, currentAngle(item), MathUtils.pitchRadians(item.getDirection()));
    }

    private static final double tick = 0.05;

    @Override
    public void tick(ThrownItem item) {
        ArmorStand a = item.getStand();
        if (!a.isValid()){
            item.stop();
            return;
        }
        if (!item.getThrower().isOnline() || !item.getThrower().getWorld().equals(a.getWorld())){
            item.drop();
            return;
        }

        a.setHeadPose(new EulerAngle(0, 0, currentAngle(item)));
        item.getDirection().add(item.getGravity().clone().multiply(tick));
        if (item.isReturning() || item.getItem().getItem().getEnchantments().containsKey(Enchantment.LOYALTY)){
            item.setDirection(item.getThrower().getEyeLocation().subtract(a.getEyeLocation()).toVector().normalize().multiply(30));
            if (item.getThrower().getEyeLocation().distanceSquared(item.getStand().getEyeLocation()) < 4){
                boolean mainEmpty = ItemUtils.isEmpty(item.getThrower().getInventory().getItemInMainHand());
                boolean offEmpty = ItemUtils.isEmpty(item.getThrower().getInventory().getItemInOffHand());
                if (mainEmpty && item.getHand() == EquipmentSlot.HAND) item.getThrower().getInventory().setItemInMainHand(item.getRawThrown());
                else if (offEmpty && item.getHand() == EquipmentSlot.OFF_HAND) item.getThrower().getInventory().setItemInOffHand(item.getRawThrown());
                else if (mainEmpty) item.getThrower().getInventory().setItemInMainHand(item.getRawThrown());
                else if (offEmpty) item.getThrower().getInventory().setItemInOffHand(item.getRawThrown());
                else ItemUtils.addItem(item.getThrower(), item.getRawThrown(), true);
                item.stop();
                return;
            }
        }

        Vector dx = item.getDirection().clone();
        Vector normal = item.getDirection().clone().normalize();
        RayTraceResult result = item.isReturning() ? null : a.getWorld()
                .rayTrace(a.getEyeLocation(), normal, dx.length() * 1.5, FluidCollisionMode.NEVER,
                        false, 0.2, e -> e instanceof LivingEntity && !e.equals(item.getStand()) &&
                                !e.equals(item.getThrower()) && !item.getHitEntities().contains(e.getUniqueId()) &&
                                !EntityClassification.matchesClassification(e, EntityClassification.UNALIVE)
                );
        if (result != null && result.getHitEntity() != null){
            AttributeWrapper damageWrapper = ItemAttributesRegistry.getAnyAttribute(item.getItem().getMeta(), "GENERIC_ATTACK_DAMAGE");
            item.getHitEntities().add(result.getHitEntity().getUniqueId());
            double damage = damageWrapper == null ? 1 : damageWrapper.getValue();
            EntityUtils.damage((LivingEntity) result.getHitEntity(), item.getThrower(), damage, "PROJECTILE", false); // TODO damage modification and/or extra hits
            item.setPiercingLeft(item.getPiercingLeft() - 1);
        }

        if (result != null && ((result.getHitEntity() != null && item.getPiercingLeft() <= 0) || (result.getHitBlock() != null && !result.getHitBlock().getType().isAir()))){
            if (item.getStats().isInfinity()){
                item.stop();
                return;
            } else if (item.getStats().returnsNaturally()){
                item.setReturning(true);
                item.getDirection().setY(10);
            } else {
                item.drop();
                return;
            }
        }

        if (result != null && (result.getHitBlock() != null || result.getHitEntity() != null) && ItemUtils.damageItem(item.getThrower(), item.getRawThrown(), 1, EntityEffect.BREAK_EQUIPMENT_OFF_HAND)){
            item.stop();
            return;
        }

        item.getLocation().add(dx);
        a.teleport(item.getLocation());
        item.incrementTick();
        if (item.getTick() > 100) {
            if (item.getStats().isInfinity()){
                item.stop();
            } else if (item.getStats().returnsNaturally()){
                item.setReturning(true);
            } else {
                item.drop();
            }
        }
    }

    @Override
    public ThrownItem throwItem(Player thrower, ItemBuilder thrownItem, EquipmentSlot fromHand) {
        return new ThrownItem(thrower, fromHand, thrownItem, this);
    }
}
