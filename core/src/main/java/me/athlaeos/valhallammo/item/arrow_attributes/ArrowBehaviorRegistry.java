package me.athlaeos.valhallammo.item.arrow_attributes;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.item_attributes.ArrowBehavior;
import me.athlaeos.valhallammo.item.arrow_attributes.implementations.*;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ArrowBehaviorRegistry {
    private static final NamespacedKey BEHAVIOR = new NamespacedKey(ValhallaMMO.getInstance(), "arrow_behavior");
    private static final Map<String, ArrowBehavior> registeredBehaviors = new HashMap<>();
    public static final ArrowBehavior EXPLODING = new ExplodingArrow("explosive_arrow");
    public static final ArrowBehavior EXPLODING_REPEATEDLY = new VeryExplosiveArrow("very_explosive_arrow");
    public static final ArrowBehavior INCENDIARY = new IncendiaryArrow("incendiary_arrow");
    public static final ArrowBehavior TELEPORTING = new TeleportingArrow("ender_arrow");
    public static final ArrowBehavior IMMUNITY_REMOVAL = new VoidImmunityArrow("no_iframes_arrow");
    public static final ArrowBehavior LIGHTNING = new LightningArrow("lightning_arrow");
    public static final ArrowBehavior FIREBALL_SMALL = new SmallFireballArrow("small_fireball_arrow");
    public static final ArrowBehavior FIREBALL_LARGE = new LargeFireballArrow("large_fireball_arrow");
    public static final ArrowBehavior FIREBALL_DRAGON = new DragonFireballArrow("dragon_fireball_arrow");
    public static final ArrowBehavior ANTIGRAVITY = new NoGravityArrow("gravityless_arrow");

    static {
        registerBehavior(EXPLODING);
        registerBehavior(INCENDIARY);
        registerBehavior(TELEPORTING);
        registerBehavior(IMMUNITY_REMOVAL);
        registerBehavior(LIGHTNING);
        registerBehavior(FIREBALL_SMALL);
        registerBehavior(FIREBALL_LARGE);
        registerBehavior(FIREBALL_DRAGON);
        registerBehavior(ANTIGRAVITY);
        registerBehavior(EXPLODING_REPEATEDLY);
    }

    public static void registerBehavior(ArrowBehavior behavior){
        registeredBehaviors.put(behavior.getName(), behavior);
    }

    public static ArrowBehavior getBehavior(String behavior){
        if (!registeredBehaviors.containsKey(behavior)) throw new IllegalArgumentException("Arrow behavior " + behavior + " does not exist");
        return registeredBehaviors.get(behavior);
    }

    public static void addBehavior(ItemMeta arrow, String behavior, double... args){
        if (!registeredBehaviors.containsKey(behavior)) return;

        Map<String, BehaviorDetails> attributes = getBehaviors(arrow);
        attributes.put(behavior, new BehaviorDetails(behavior, args));
        setBehaviors(arrow, attributes);
    }

    public static void setBehaviors(ItemMeta meta, Map<String, BehaviorDetails> attributes){
        if (meta == null) return;
        if (attributes.isEmpty()){
            meta.getPersistentDataContainer().remove(BEHAVIOR);
        } else {
            Collection<String> attributeStrings = new HashSet<>();
            for (BehaviorDetails a : attributes.values()){
                StringBuilder s = new StringBuilder(a.getName());
                for (double i : a.getArgs()){
                    s.append(":").append(i);
                }
                attributeStrings.add(s.toString());
            }
            meta.getPersistentDataContainer().set(BEHAVIOR, PersistentDataType.STRING, String.join(";", attributeStrings));
        }
    }

    public static Map<String, BehaviorDetails> getBehaviors(ItemMeta arrow){
        Map<String, BehaviorDetails> attributes = new HashMap<>();
        String value = ItemUtils.getPDCString(BEHAVIOR, arrow, null);
        if (StringUtils.isEmpty(value)) return attributes;
        for (String s : value.split(";")){
            String[] args = s.split(":");
            if (args.length > 1){
                String name = args[0];
                if (!registeredBehaviors.containsKey(name)) continue;
                double[] doubleArgs = new double[args.length - 1];
                for (int i = 0; i < args.length - 1; i++){
                    try {
                        doubleArgs[i] = StringUtils.parseDouble(args[i + 1]);
                    } catch (NumberFormatException ignored){}
                }
                attributes.put(args[0], new BehaviorDetails(args[0], doubleArgs));
            } else attributes.put(s, new BehaviorDetails(s, new double[0]));
        }
        return attributes;
    }

    public static void execute(EntityDamageByEntityEvent e, Collection<BehaviorDetails> details){
        details.forEach(b -> getBehavior(b.name).onDamage(e, b.args));
    }

    public static void execute(PlayerPickupArrowEvent e, Collection<BehaviorDetails> details){
        details.forEach(b -> getBehavior(b.name).onPickup(e, b.args));
    }

    public static void execute(ProjectileLaunchEvent e, Collection<BehaviorDetails> details){
        details.forEach(b -> getBehavior(b.name).onLaunch(e, b.args));
    }

    public static void execute(ProjectileHitEvent e, Collection<BehaviorDetails> details){
        details.forEach(b -> getBehavior(b.name).onHit(e, b.args));
    }

    public static void execute(EntityShootBowEvent e, Collection<BehaviorDetails> details){
        details.forEach(b -> getBehavior(b.name).onShoot(e, b.args));
    }

    private record BehaviorDetails(String name, double[] args) {
        public String getName() {
            return name;
        }

        public double[] getArgs() {
            return args;
        }
    }
}
