package me.athlaeos.valhallammo.item.throwable_weapon_animations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.throwable_weapon_animations.implementations.VerticalSpin;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class ThrowableWeaponAnimationRegistry {
    private static final Map<String, ThrowableWeaponAnimation> animations = new HashMap<>();
    private static final NamespacedKey THROWING_ANIMATION = new NamespacedKey(ValhallaMMO.getInstance(), "throwable");

    static {
        register(new VerticalSpin("vertical_spin"));
    }

    public static void register(ThrowableWeaponAnimation animation){
        animations.put(animation.getName(), animation);
    }

    public static ThrowableWeaponAnimation getRegisteredAnimation(String animation){
        return animations.get(animation);
    }

    public static Map<String, ThrowableWeaponAnimation> getAnimations() {
        return new HashMap<>(animations);
    }

    public static void setItemStats(ItemMeta meta, ThrowableItemStats stats){
        setStats(meta.getPersistentDataContainer(), stats);
    }

    public static void setStats(PersistentDataContainer container, ThrowableItemStats stats){
        if (stats == null) container.remove(THROWING_ANIMATION);
        else container.set(THROWING_ANIMATION, PersistentDataType.STRING,
                String.format("%s:%.1f:%.1f:%.1f:%.1f:%s:%s:%d", stats.getAnimationType(), stats.getGravityStrength(), stats.getVelocityDamageMultiplier(),
                        stats.getDefaultVelocity(), stats.getDamageMultiplier(), stats.isInfinity(), stats.returnsNaturally(), stats.getCooldown())
        );
    }

    public static ThrowableItemStats getItemStats(ItemMeta meta){
        if (meta == null) return null;
        return getStats(meta.getPersistentDataContainer());
    }

    public static ThrowableItemStats getStats(PersistentDataContainer container){
        if (container == null || !container.has(THROWING_ANIMATION, PersistentDataType.STRING)) return null;
        String value = container.getOrDefault(THROWING_ANIMATION, PersistentDataType.STRING, "");
        String[] args = value.split(":");
        if (args.length != 8) return null;
        String type = args[0];
        if (!animations.containsKey(type)) return null;
        try {
            double gravityMultiplier = StringUtils.parseDouble(args[1]);
            double velocityDamageMultiplier = StringUtils.parseDouble(args[2]);
            double defaultVelocity = StringUtils.parseDouble(args[3]);
            double damageMultiplier = StringUtils.parseDouble(args[4]);
            boolean infinity = Boolean.parseBoolean(args[5]);
            boolean returnsNaturally = Boolean.parseBoolean(args[6]);
            int cooldown = Integer.parseInt(args[7]);
            return new ThrowableItemStats(type, cooldown, gravityMultiplier, velocityDamageMultiplier, defaultVelocity, damageMultiplier, infinity, returnsNaturally);
        } catch (IllegalArgumentException ignored){ }
        return null;
    }
}
