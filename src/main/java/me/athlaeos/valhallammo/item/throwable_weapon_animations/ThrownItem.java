package me.athlaeos.valhallammo.item.throwable_weapon_animations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.MathUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class ThrownItem {
    private static final NamespacedKey THROWN_WEAPON = new NamespacedKey(ValhallaMMO.getInstance(), "thrown_weapon");
    public static UUID getThrownBy(ArmorStand a){
        String value = a.getPersistentDataContainer().get(THROWN_WEAPON, PersistentDataType.STRING);
        if (value == null) return null;
        return UUID.fromString(value);
    }

    private final ArmorStand stand;
    private final BukkitRunnable runnable;

    private final ItemBuilder item;
    private final ThrowableItemStats stats;
    private final EquipmentSlot hand;
    private final Player thrower;

    private Vector direction;
    private Vector gravity;
    private final Location location;
    private int piercingLeft;
    private final Collection<UUID> hitEntities = new HashSet<>();
    private int tick = 0;
    private boolean returning = false;

    private final ItemStack rawThrown;

    private static final double GRAVITY = 9.81;

    public ThrownItem(Player thrower, EquipmentSlot hand, ItemBuilder item, ThrowableWeaponAnimation animation){
        this.thrower = thrower;
        this.stats = ThrowableWeaponAnimationRegistry.getItemStats(item.getMeta());
        this.hand = hand;
        this.item = item.amount(1);
        rawThrown = item.get();

        gravity = new Vector(0, -GRAVITY * stats.getGravityStrength(), 0);
        location = thrower.getLocation().add(MathUtils.getHandOffset(thrower, hand == EquipmentSlot.HAND));
        location.setPitch(0);
        location.setYaw(thrower.getEyeLocation().getYaw() + 90);

        direction = thrower.getEyeLocation().getDirection().multiply(stats.getDefaultVelocity()); // TODO thrown weapon speed

        AttributeWrapper piercing = ItemAttributesRegistry.getAttribute(item.getMeta(), "ARROW_PIERCING", false);
        if (piercing != null) piercingLeft = Math.max(0, ((int) piercing.getValue()));
        AttributeWrapper speedWrapper = ItemAttributesRegistry.getAttribute(item.getMeta(), "ARROW_VELOCITY", false);
        if (speedWrapper != null) direction = direction.multiply(1 + speedWrapper.getValue());

        stand = thrower.getWorld().spawn(location, ArmorStand.class);
        if (stand.getEquipment() == null){
            stand.remove();
            runnable = null;
            return;
        }
        ThrowableWeaponAnimationRegistry.setStats(stand.getPersistentDataContainer(), stats);
        stand.getPersistentDataContainer().set(THROWN_WEAPON, PersistentDataType.STRING, thrower.getUniqueId().toString());
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setInvisible(true);
        stand.setCollidable(false);
        stand.setMarker(false);
        stand.setAI(false);
        stand.getEquipment().setHelmet(item.get());
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            stand.addEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING);
            stand.addEquipmentLock(slot, ArmorStand.LockType.ADDING);
        }

        final ThrownItem instance = this;
        runnable = new BukkitRunnable(){
            @Override
            public void run() {
                animation.tick(instance);
            }
        };
        runnable.runTaskTimer(ValhallaMMO.getInstance(), 0, 1L);
    }

    public void incrementTick(){ tick++; }
    public int getTick() { return tick; }
    public ArmorStand getStand() { return stand; }
    public Collection<UUID> getHitEntities() { return hitEntities; }
    public EquipmentSlot getHand() { return hand; }
    public int getPiercingLeft() { return piercingLeft; }
    public ItemBuilder getItem() { return item; }
    public ItemStack getRawThrown() { return rawThrown; }
    public Location getLocation() { return location; }
    public Player getThrower() { return thrower; }
    public ThrowableItemStats getStats() { return stats; }
    public Vector getDirection() { return direction; }
    public Vector getGravity() { return gravity; }
    public boolean isReturning() { return returning; }

    public void setDirection(Vector direction) { this.direction = direction; }
    public void setGravity(Vector gravity) { this.gravity = gravity; }
    public void setPiercingLeft(int piercingLeft) { this.piercingLeft = piercingLeft; }
    public void setReturning(boolean returning) { this.returning = returning; }

    public void stop(){
        runnable.cancel();
        stand.remove();
    }

    public void drop(){
        if (stand.getEquipment() == null || ItemUtils.isEmpty(stand.getEquipment().getHelmet())) return;
        if (!stats.isInfinity()) stand.getWorld().dropItem(stand.getEyeLocation(), stand.getEquipment().getHelmet());
        stop();
    }
}
