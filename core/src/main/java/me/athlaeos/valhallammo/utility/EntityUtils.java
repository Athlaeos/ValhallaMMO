package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.item.*;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.dom.BiFetcher;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.listeners.EntityDamagedListener;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class EntityUtils {

    public static int getTotalExperience(Player player) {
        return Math.round(player.getExp() * player.getExpToLevel()) + getTotalExperience(player.getLevel());
    }

    private static final double inaccuracyConstant = ValhallaMMO.getPluginConfig().getDouble("inaccuracy_constant", 0.015);
    public static void applyInaccuracy(Entity projectile, Vector primaryDirection, double inaccuracy){
        Vector projectileVelocity = projectile.getVelocity().clone();
        double strength = projectileVelocity.length(); // record initial speed of the projectile
        projectileVelocity = projectileVelocity.normalize(); // reduce vector lengths to 1
        primaryDirection = primaryDirection.clone().normalize();
        projectileVelocity.setX(primaryDirection.getX()); // set direction of projectile equal to direction of shooter
        projectileVelocity.setY(primaryDirection.getY());
        projectileVelocity.setZ(primaryDirection.getZ());
        projectileVelocity.multiply(strength); // restore the initial speed to the projectile

        inaccuracy = Math.max(0, inaccuracy);
        projectileVelocity.setX(projectileVelocity.getX() + Utils.getRandom().nextGaussian() * inaccuracyConstant * inaccuracy);
        projectileVelocity.setY(projectileVelocity.getY() + Utils.getRandom().nextGaussian() * inaccuracyConstant * inaccuracy);
        projectileVelocity.setZ(projectileVelocity.getZ() + Utils.getRandom().nextGaussian() * inaccuracyConstant * inaccuracy);

        projectile.setVelocity(projectileVelocity);
    }

    public static double getMaxHP(LivingEntity l){
        AttributeInstance instance = l.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        return instance == null ? -1 : instance.getValue();
    }

    public static BlockFace getSelectedBlockFace(Player player) {
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 100);
        if (lastTwoTargetBlocks.size() != 2 || !lastTwoTargetBlocks.get(1).getType().isOccluding()) return null;
        Block targetBlock = lastTwoTargetBlocks.get(1);
        Block adjacentBlock = lastTwoTargetBlocks.get(0);
        return targetBlock.getFace(adjacentBlock);
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

    private static Method isOnGroundMethod = null;
    /**
     * Suboptimal solution, but it'll be like this until I figure out a better more reliable method of checking if a player is on the ground
     * Method by mfnalex (jeff media)
     */
    public static boolean isOnGround(Entity e){
        try {
            // Use reflection to get the isOnGround method from the Entity class
            Method method = isOnGroundMethod;
            if (method == null) {
                isOnGroundMethod = Entity.class.getDeclaredMethod("isOnGround");
                method = isOnGroundMethod;
            }

            // Invoke the method on the Player object
            return (boolean) method.invoke(e);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private static boolean downwardsRaytraceBlockSegment(Location l, Vector direction, double length){
        if (l.getWorld() == null) return false;
        RayTraceResult result = l.getWorld().rayTraceBlocks(l, direction, length, FluidCollisionMode.NEVER, true);
        return result != null && result.getHitBlock() != null && !result.getHitBlock().getType().isAir();
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

    public static void addUniqueAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type, double amount, AttributeModifier.Operation operation){
        ValhallaMMO.getNms().addUniqueAttribute(e, uuid, identifier, type, amount, operation);
    }

    public static boolean hasUniqueAttribute(LivingEntity e, UUID uuid, String identifier, Attribute type){
        return ValhallaMMO.getNms().hasUniqueAttribute(e, uuid, identifier, type);
    }

    public static double getUniqueAttributeValue(LivingEntity e, UUID uuid, String identifier, Attribute type){
        return ValhallaMMO.getNms().getUniqueAttributeValue(e, uuid, identifier, type);
    }

    public static void removeUniqueAttribute(LivingEntity e, String identifier, Attribute type){
        ValhallaMMO.getNms().removeUniqueAttribute(e, identifier, type);
    }

    public static boolean addExperience(Player player, int amount){
        if (amount > 0) {
            player.giveExp(amount);
            return true;
        } else {
            int exp = getTotalExperience(player);
            if (exp >= amount) {
                setTotalExperience(player, exp + amount);
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
            total += getValue(entity, equipmentPenalty, properties.getHelmet().getMeta(), properties.getHelmetAttributes(), attribute, null);
        if (properties.getChestplate() != null && (weightFilter == null || WeightClass.getWeightClass(properties.getChestplate().getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, properties.getChestplate().getMeta(), properties.getChestPlateAttributes(), attribute, null);
        if (properties.getLeggings() != null && (weightFilter == null || WeightClass.getWeightClass(properties.getLeggings().getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, properties.getLeggings().getMeta(), properties.getLeggingsAttributes(), attribute, null);
        if (properties.getBoots() != null && (weightFilter == null || WeightClass.getWeightClass(properties.getBoots().getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, properties.getBoots().getMeta(), properties.getBootsAttributes(), attribute, null);

        if (properties.getMainHand() != null && ItemUtils.usedMainHand(properties.getMainHand(), properties.getOffHand()))
            total += getValue(entity, equipmentPenalty, properties.getMainHand().getMeta(), properties.getMainHandAttributes(), attribute, null);
        else if (!mainHandOnly && properties.getOffHand() != null) total += getValue(entity, equipmentPenalty, properties.getOffHand().getMeta(), properties.getOffHandAttributes(), attribute, null);

        for (ItemBuilder extra : properties.getMiscEquipment()){
            total += getValue(entity, equipmentPenalty, extra.getMeta(), properties.getMiscEquipmentAttributes().get(extra), attribute, null);
        }
        return total;
    }

    public static double combinedAttackerAttributeValue(LivingEntity entity, String attribute, WeightClass weightFilter, String equipmentPenalty, boolean mainHand){
        double total = 0;
        EntityProperties properties = EntityCache.getAndCacheProperties(entity);
        if (properties.getHelmet() != null && (weightFilter == null || WeightClass.getWeightClass(properties.getHelmet().getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, properties.getHelmet().getMeta(), properties.getHelmetAttributes(), attribute, null);
        if (properties.getChestplate() != null && (weightFilter == null || WeightClass.getWeightClass(properties.getChestplate().getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, properties.getChestplate().getMeta(), properties.getChestPlateAttributes(), attribute, null);
        if (properties.getLeggings() != null && (weightFilter == null || WeightClass.getWeightClass(properties.getLeggings().getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, properties.getLeggings().getMeta(), properties.getLeggingsAttributes(), attribute, null);
        if (properties.getBoots() != null && (weightFilter == null || WeightClass.getWeightClass(properties.getBoots().getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, properties.getBoots().getMeta(), properties.getBootsAttributes(), attribute, null);

        if (mainHand && properties.getMainHand() != null)
            total += getValue(entity, equipmentPenalty, properties.getMainHand().getMeta(), properties.getMainHandAttributes(), attribute, null);
        if (!mainHand && properties.getOffHand() != null)
            total += getValue(entity, equipmentPenalty, properties.getOffHand().getMeta(), properties.getOffHandAttributes(), attribute, null);

        for (ItemBuilder extra : properties.getMiscEquipment()){
            total += getValue(entity, equipmentPenalty, extra.getMeta(), properties.getMiscEquipmentAttributes().get(extra), attribute, null);
        }
        return total;
    }

    private static final Attribute attackReachAttribute = Catch.catchOrElse(() -> Attribute.valueOf("PLAYER_ENTITY_INTERACTION_RANGE"), null);
    public static double getPlayerReach(Player p){
        if (attackReachAttribute == null) return 3.0;
        AttributeInstance reach = p.getAttribute(attackReachAttribute);
        if (reach != null) return reach.getBaseValue();
        return 3.0;
    }

    private static final Attribute miningSpeedAttribute = Catch.catchOrElse(() -> Attribute.valueOf("PLAYER_BLOCK_BREAK_SPEED"), null);
    public static double getPlayerMiningSpeed(Player p){
        if (miningSpeedAttribute == null) return 1.0;
        AttributeInstance speed = p.getAttribute(miningSpeedAttribute);
        if (speed != null) return speed.getValue();
        return 1.0;
    }

    public static double combinedAttributeValue(LivingEntity entity, Attribute attribute, AttributeModifier.Operation operation, WeightClass weightFilter, String equipmentPenalty, boolean mainHandOnly){
        double total = 0;
        EntityProperties properties = EntityCache.getAndCacheProperties(entity);
        if (properties.getHelmet() != null && (weightFilter == null || WeightClass.getWeightClass(properties.getHelmet().getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, properties.getHelmet().getMeta(), properties.getHelmetAttributes(), attribute.toString(), operation);
        if (properties.getChestplate() != null && (weightFilter == null || WeightClass.getWeightClass(properties.getChestplate().getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, properties.getChestplate().getMeta(), properties.getChestPlateAttributes(), attribute.toString(), operation);
        if (properties.getLeggings() != null && (weightFilter == null || WeightClass.getWeightClass(properties.getLeggings().getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, properties.getLeggings().getMeta(), properties.getLeggingsAttributes(), attribute.toString(), operation);
        if (properties.getBoots() != null && (weightFilter == null || WeightClass.getWeightClass(properties.getBoots().getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, properties.getBoots().getMeta(), properties.getBootsAttributes(), attribute.toString(), operation);

        if (properties.getMainHand() != null && ItemUtils.usedMainHand(properties.getMainHand(), properties.getOffHand()))
            total += getValue(entity, equipmentPenalty, properties.getMainHand().getMeta(), properties.getMainHandAttributes(), attribute.toString(), operation);
        else if (!mainHandOnly && properties.getOffHand() != null) total += getValue(entity, equipmentPenalty, properties.getOffHand().getMeta(), properties.getOffHandAttributes(), attribute.toString(), operation);

        for (ItemBuilder extra : properties.getMiscEquipment()){
            if (WeightClass.getWeightClass(extra.getMeta()) != weightFilter) continue;
            total += getValue(entity, equipmentPenalty, extra.getMeta(), properties.getMiscEquipmentAttributes().get(extra), attribute.toString(), operation);
        }
        return total;
    }

    private static double getValue(LivingEntity entity, String statPenalty, ItemMeta item, Map<String, AttributeWrapper> wrappers, String attribute, AttributeModifier.Operation operation){
        AttributeWrapper attributeWrapper = wrappers.get(attribute);
        if (attributeWrapper == null || (operation != null && attributeWrapper.isVanilla() && attributeWrapper.getOperation() != operation)) return 0;
        double value = attributeWrapper.getValue();
        if (statPenalty != null && entity instanceof Player p) value *= (1 + ItemSkillRequirements.getPenalty(p, item, statPenalty));
        return value;
    }

    public static EntityProperties updateProperties(EntityProperties properties, LivingEntity e, boolean equipment, boolean hands, boolean getPotionEffects){
        if (e.getEquipment() != null) {
            properties.getPermanentPotionEffects().clear();
            List<List<PotionEffect>> permanentEffects = new ArrayList<>();
            if (equipment){
                if (e instanceof Player p) {
                    PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
                    permanentEffects.add(PermanentPotionEffects.fromString(String.join(";", profile.getPermanentPotionEffects())));
                }

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

                if (properties.getHelmet() != null) {
                    properties.setHelmetAttributes(ItemAttributesRegistry.getStats(properties.getHelmet().getMeta(), false));
                    permanentEffects.add(PermanentPotionEffects.getPermanentPotionEffects(properties.getHelmet().getMeta()));
                }
                if (properties.getChestplate() != null) {
                    properties.setChestPlateAttributes(ItemAttributesRegistry.getStats(properties.getChestplate().getMeta(), false));
                    permanentEffects.add(PermanentPotionEffects.getPermanentPotionEffects(properties.getChestplate().getMeta()));
                }
                if (properties.getLeggings() != null) {
                    properties.setLeggingsAttributes(ItemAttributesRegistry.getStats(properties.getLeggings().getMeta(), false));
                    permanentEffects.add(PermanentPotionEffects.getPermanentPotionEffects(properties.getLeggings().getMeta()));
                }
                if (properties.getBoots() != null) {
                    properties.setBootsAttributes(ItemAttributesRegistry.getStats(properties.getBoots().getMeta(), false));
                    permanentEffects.add(PermanentPotionEffects.getPermanentPotionEffects(properties.getBoots().getMeta()));
                }
                for (BiFetcher<List<ItemStack>, LivingEntity> fetcher : otherEquipmentFetchers){
                    List<ItemBuilder> otherEquipment = fetcher.get(e).stream().map(ItemBuilder::new).toList();
                    properties.getMiscEquipment().addAll(otherEquipment);
                    otherEquipment.forEach(i -> {
                        properties.getMiscEquipmentAttributes().put(i, ItemAttributesRegistry.getStats(i.getMeta(), false));
                        properties.addCombinedEnchantments(i);
                        permanentEffects.add(PermanentPotionEffects.getPermanentPotionEffects(i.getMeta()));
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
                ) {
                    properties.setMainHandAttributes(ItemAttributesRegistry.getStats(properties.getMainHand().getMeta(), false));
                    permanentEffects.add(PermanentPotionEffects.getPermanentPotionEffects(properties.getMainHand().getMeta()));
                }

                if (properties.getOffHand() != null &&
                        !EquipmentClass.isArmor(properties.getOffHand().getMeta()) &&
                        EquipmentClass.getMatchingClass(properties.getOffHand().getMeta()) != EquipmentClass.TRINKET
                ) {
                    properties.setOffHandAttributes(ItemAttributesRegistry.getStats(properties.getOffHand().getMeta(), false));
                    permanentEffects.add(PermanentPotionEffects.getPermanentPotionEffects(properties.getOffHand().getMeta()));
                }
            }
            List<PotionEffect> combinedPermanentEffects = PermanentPotionEffects.getCombinedEffects(permanentEffects);
            if (!combinedPermanentEffects.isEmpty()) PermanentPotionEffects.setHasPermanentEffects(e);
            else PermanentPotionEffects.setHasNoPermanentEffects(e);
            properties.getPermanentPotionEffects().addAll(combinedPermanentEffects);
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
    public static double cooldownDamageMultiplier(float cooldown){
        return Math.min(1, 0.2 + MathUtils.pow((cooldown + 0.05F), 2) * 0.8);
    }

    public static void damage(LivingEntity entity, double amount, String type){
        damage(entity, null, amount, type);
    }

    public static void damage(LivingEntity entity, Entity by, double amount, String type){
        if (entity.isDead()) return;
        int immunityBefore = entity.getNoDamageTicks();
        entity.setNoDamageTicks(0);
        EntityDamagedListener.setCustomDamageCause(entity.getUniqueId(), type);
        if (by != null) EntityDamagedListener.setDamager(entity, by);
        entity.damage(amount);
        entity.setNoDamageTicks(immunityBefore);
    }

    public static Entity getTrueDamager(EntityDamageByEntityEvent e){
        if (e.getDamager() instanceof EnderPearl p && p.getShooter() instanceof Entity) return p;
        if (e.getDamager() instanceof Projectile p && p.getShooter() instanceof Entity t) return t;
        return e.getDamager();
    }

    public static boolean isEntityFacing(LivingEntity who, Location at, double cos_angle){
        Vector dir = at.toVector().subtract(who.getEyeLocation().toVector()).normalize();
        double dot = dir.dot(who.getEyeLocation().getDirection());
        return dot >= cos_angle;
    }
}
