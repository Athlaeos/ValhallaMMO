package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.dom.BiFetcher;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.ItemSkillRequirements;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.listeners.EntityDamagedListener;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class EntityUtils {

    public static int getTotalExperience(Player player) {
        return Math.round(player.getExp() * player.getExpToLevel()) + getTotalExperience(player.getLevel());
    }

    private static final Map<Integer, Integer> levelExperienceCache = new HashMap<>();
    public static int getTotalExperience(int level) {
        if (levelExperienceCache.containsKey(level)) return levelExperienceCache.get(level);
        int xp = 0;

        if (level >= 0 && level <= 15) {
            xp = (int) Math.round(Math.pow(level, 2) + 6 * level);
        } else if (level > 15 && level <= 30) {
            xp = (int) Math.round((2.5 * Math.pow(level, 2) - 40.5 * level + 360));
        } else if (level > 30) {
            xp = (int) Math.round(((4.5 * Math.pow(level, 2) - 162.5 * level + 2220)));
        }
        levelExperienceCache.put(level, xp);
        return xp;
    }

    public static void setTotalExperience(Player player, int amount) {
        int level;
        int xp;
        float a = 0;
        float b = 0;
        float c = -amount;

        if (amount > getTotalExperience(0) && amount <= getTotalExperience(15)) {
            a = 1;
            b = 6;
        } else if (amount > getTotalExperience(15) && amount <= getTotalExperience(30)) {
            a = 2.5f;
            b = -40.5f;
            c += 360;
        } else if (amount > getTotalExperience(30)) {
            a = 4.5f;
            b = -162.5f;
            c += 2220;
        }
        level = (int) Math.floor((-b + Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a));
        xp = amount - getTotalExperience(level);
        player.setLevel(level);
        player.setExp(0);
        player.giveExp(xp);
    }


    public static void addUniqueAttribute(LivingEntity e, String identifier, Attribute type, double amount, AttributeModifier.Operation operation){
        AttributeInstance instance = e.getAttribute(type);
        if (instance != null){
            instance.getModifiers().stream().filter(m -> m.getName().equals(identifier)).forEach(instance::removeModifier);
            if (amount != 0) instance.addModifier(new AttributeModifier(identifier, amount, operation));
        }
    }

    public static void removeUniqueAttribute(LivingEntity e, String identifier, Attribute type){
        AttributeInstance instance = e.getAttribute(type);
        if (instance != null) instance.getModifiers().stream().filter(m -> m.getName().equals(identifier)).forEach(instance::removeModifier);
    }

    public static boolean addExperience(Player player, int amount){
        if (amount > 0) {
            player.giveExp(amount);
            return true;
        } else {
            int exp = getTotalExperience(player);
            if (exp >= amount) {
                setTotalExperience(player, exp - amount);
                return true;
            } else return false;
        }
    }

    private static final Collection<BiFetcher<List<ItemStack>, LivingEntity>> otherEquipmentFetchers = new HashSet<>();
    public static void registerEquipmentFetcher(BiFetcher<List<ItemStack>, LivingEntity> fetcher){
        otherEquipmentFetchers.add(fetcher);
    }

    public static EntityProperties getEntityProperties(LivingEntity e, boolean getEquipment, boolean getHands, boolean getPotionEffects){
        EntityProperties equipment = new EntityProperties();
        if (e == null) return equipment;
        return updateProperties(equipment, e, getEquipment, getHands, getPotionEffects);
    }

    public static double combinedAttributeValue(LivingEntity entity, String attribute, WeightClass weightFilter, String equipmentPenalty, boolean mainHandOnly){
        double total = 0;
        EntityProperties properties = EntityCache.getAndCacheProperties(entity);
        if (properties.getHelmet() != null && (weightFilter == null || WeightClass.getWeightClass(properties.getHelmet().getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, properties.getHelmet().getMeta(), properties.getHelmetAttributes(), attribute);
        if (properties.getChestplate() != null && (weightFilter == null || WeightClass.getWeightClass(properties.getChestplate().getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, properties.getChestplate().getMeta(), properties.getChestPlateAttributes(), attribute);
        if (properties.getLeggings() != null && (weightFilter == null || WeightClass.getWeightClass(properties.getLeggings().getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, properties.getLeggings().getMeta(), properties.getLeggingsAttributes(), attribute);
        if (properties.getBoots() != null && (weightFilter == null || WeightClass.getWeightClass(properties.getBoots().getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, properties.getBoots().getMeta(), properties.getBootsAttributes(), attribute);

        if (properties.getMainHand() != null && ItemUtils.usedMainHand(properties.getMainHand(), properties.getOffHand()))
            total += getValue(entity, equipmentPenalty, properties.getMainHand().getMeta(), properties.getMainHandAttributes(), attribute);
        else if (!mainHandOnly && properties.getOffHand() != null) total += getValue(entity, equipmentPenalty, properties.getOffHand().getMeta(), properties.getOffHandAttributes(), attribute);

        for (ItemBuilder extra : properties.getMiscEquipment()){
            total += getValue(entity, equipmentPenalty, extra.getMeta(), properties.getMiscEquipmentAttributes().get(extra), attribute);
        }
        return total;
    }

    private static double getValue(LivingEntity entity, String statPenalty, ItemMeta item, Map<String, AttributeWrapper> wrappers, String attribute){
        AttributeWrapper attributeWrapper = wrappers.get(attribute);
        if (attributeWrapper == null) return 0;
        double value = attributeWrapper.getValue();
        if (statPenalty != null && entity instanceof Player p) value *= (1 + ItemSkillRequirements.getPenalty(p, item, statPenalty));
        return value;
    }

    public static EntityProperties updateProperties(EntityProperties properties, LivingEntity e, boolean equipment, boolean hands, boolean getPotionEffects){
        if (e.getEquipment() != null) {
            if (equipment){
                properties.getCombinedEnchantments().clear();
                properties.setHelmet(e.getEquipment().getHelmet());
                properties.setChestplate(e.getEquipment().getChestplate());
                properties.setLeggings(e.getEquipment().getLeggings());
                properties.setBoots(e.getEquipment().getBoots());
                properties.addCombinedEnchantments(properties.getHelmet());
                properties.addCombinedEnchantments(properties.getChestplate());
                properties.addCombinedEnchantments(properties.getLeggings());
                properties.addCombinedEnchantments(properties.getBoots());

                properties.setHeavyArmorCount(WeightClass.getArmorWeightClassCount(e, WeightClass.HEAVY));
                properties.setLightArmorCount(WeightClass.getArmorWeightClassCount(e, WeightClass.LIGHT));
                properties.setWeightlessArmorCount(WeightClass.getArmorWeightClassCount(e, WeightClass.WEIGHTLESS));

                if (properties.getHelmet() != null) properties.setHelmetAttributes(ItemAttributesRegistry.getStats(properties.getHelmet().getMeta(), true));
                if (properties.getChestplate() != null) properties.setChestPlateAttributes(ItemAttributesRegistry.getStats(properties.getChestplate().getMeta(), true));
                if (properties.getLeggings() != null) properties.setLeggingsAttributes(ItemAttributesRegistry.getStats(properties.getLeggings().getMeta(), true));
                if (properties.getBoots() != null) properties.setBootsAttributes(ItemAttributesRegistry.getStats(properties.getBoots().getMeta(), true));
                for (BiFetcher<List<ItemStack>, LivingEntity> fetcher : otherEquipmentFetchers){
                    List<ItemBuilder> otherEquipment = fetcher.get(e).stream().map(ItemBuilder::new).collect(Collectors.toList());
                    properties.getMiscEquipment().addAll(otherEquipment);
                    otherEquipment.forEach(i -> {
                        properties.getMiscEquipmentAttributes().put(i, ItemAttributesRegistry.getStats(i.getMeta(), false));
                        properties.addCombinedEnchantments(i);
                    });
                }
            }
            if (hands){
                properties.setMainHand(e.getEquipment().getItemInMainHand());
                properties.setOffHand(e.getEquipment().getItemInOffHand());
                properties.addCombinedEnchantments(properties.getMainHand());
                properties.addCombinedEnchantments(properties.getOffHand());
                if (properties.getMainHand() != null &&
                        !EquipmentClass.isArmor(properties.getMainHand().getMeta()) &&
                        EquipmentClass.getMatchingClass(properties.getMainHand().getMeta()) != EquipmentClass.TRINKET
                ) properties.setMainHandAttributes(ItemAttributesRegistry.getStats(properties.getMainHand().getMeta(), true));

                if (properties.getOffHand() != null &&
                        !EquipmentClass.isArmor(properties.getOffHand().getMeta()) &&
                        EquipmentClass.getMatchingClass(properties.getOffHand().getMeta()) != EquipmentClass.TRINKET
                ) properties.setOffHandAttributes(ItemAttributesRegistry.getStats(properties.getOffHand().getMeta(), true));
            }
        }
        if (getPotionEffects) {
            properties.getActivePotionEffects().clear();
            properties.getActivePotionEffects().putAll(PotionEffectRegistry.getActiveEffects(e));
        }

        return properties;
    }

    /**
     * returns approximately the fraction of damage an attack should do given an attack cooldown
     * this method is not exactly like vanilla, but it comes pretty close to vanilla
     * the original formula involves attack speed, but this formula is intended to work with Player#getAttackCooldown()
     * @param cooldown the attack cooldown, a float between 0 and 1
     * @return the approximate damage multiplier of the attack
     */
    public static float cooldownDamageMultiplier(float cooldown){
        return Math.min(1F, (((4.525F * cooldown * (4.525F * cooldown + 1)) / 2) * 6.4F + 20F) / 100F);
    }

    public static void damage(LivingEntity entity, double amount, String type){
        damage(entity, null, amount, type);
    }

    public static void damage(LivingEntity entity, Entity by, double amount, String type){
        int immunityBefore = entity.getNoDamageTicks();
        EntityDamagedListener.setCustomDamageCause(entity.getUniqueId(), type);
        if (by != null) EntityDamagedListener.setDamager(entity, by);
        entity.damage(amount);
        entity.setNoDamageTicks(immunityBefore);
    }

    public static Entity getTrueDamager(EntityDamageByEntityEvent e){
        if (e.getDamager() instanceof Projectile p && p.getShooter() instanceof Entity t) return t;
        return e.getDamager();
    }

    public static boolean isEntityFacing(LivingEntity who, Location at, double cos_angle){
        Vector dir = at.toVector().subtract(who.getEyeLocation().toVector()).normalize();
        double dot = dir.dot(who.getEyeLocation().getDirection());
        return dot >= cos_angle;
    }
}
