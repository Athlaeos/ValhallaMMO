package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.*;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.dom.BiFetcher;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.listeners.EntityDamagedListener;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.version.AttributeMappings;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class EntityUtils {
    private static final String DAMAGE_CAUSE_KEY = "custom_damage_cause";

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
     */
    public static boolean isOnGround(Entity e){
        return e.isOnGround();
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
        if (properties.getHelmet() != null && WeightClass.getWeightClass(properties.getHelmet().getMeta()) == weightFilter)
            total += getValue(entity, equipmentPenalty, properties.getHelmet().getMeta(), properties.getHelmetAttributes(), attribute, null);
        if (properties.getChestplate() != null && WeightClass.getWeightClass(properties.getChestplate().getMeta()) == weightFilter)
            total += getValue(entity, equipmentPenalty, properties.getChestplate().getMeta(), properties.getChestPlateAttributes(), attribute, null);
        if (properties.getLeggings() != null && WeightClass.getWeightClass(properties.getLeggings().getMeta()) == weightFilter)
            total += getValue(entity, equipmentPenalty, properties.getLeggings().getMeta(), properties.getLeggingsAttributes(), attribute, null);
        if (properties.getBoots() != null && WeightClass.getWeightClass(properties.getBoots().getMeta()) == weightFilter)
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

    public static List<Player> getNearbyPlayers(Location from, double radius){
        return getNearbyPlayers(from, radius, true);
    }

    public static List<Player> getNearbyPlayers(Location from, double radius, boolean sorted){
        double squared = radius * radius;
        List<Player> nearby = new ArrayList<>();
        if (from.getWorld() == null) return nearby;
        for (Player p : from.getWorld().getPlayers()){
            if (from.distanceSquared(p.getLocation()) <= squared) nearby.add(p);
        }
        if (sorted) {
            nearby.sort(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(from)));
        }
        return nearby;
    }

    private static final Attribute attackReachAttribute = AttributeMappings.ENTITY_INTERACTION_RANGE.getAttribute();
    public static double getPlayerReach(Player p){
        if (attackReachAttribute == null) return 3.0;
        AttributeInstance reach = p.getAttribute(attackReachAttribute);
        if (reach != null) return reach.getBaseValue();
        return 3.0;
    }

    private static final Attribute miningSpeedAttribute = AttributeMappings.BLOCK_BREAK_SPEED.getAttribute();
    public static double getPlayerMiningSpeed(Player p){
        if (miningSpeedAttribute == null) return 1.0;
        AttributeInstance speed = p.getAttribute(miningSpeedAttribute);
        if (speed != null) return speed.getValue();
        return 1.0;
    }

    public static double combinedAttributeValue(LivingEntity entity, String attribute, AttributeModifier.Operation operation, WeightClass weightFilter, String equipmentPenalty, boolean mainHandOnly){
        double total = 0;
        EntityProperties properties = EntityCache.getAndCacheProperties(entity);
        ItemBuilder helmet = properties.getHelmet();
        if (helmet != null && (weightFilter == null || WeightClass.getWeightClass(helmet.getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, helmet.getMeta(), properties.getHelmetAttributes(), attribute, operation);
        ItemBuilder chestPlate = properties.getChestplate();
        if (chestPlate != null && (weightFilter == null || WeightClass.getWeightClass(chestPlate.getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, chestPlate.getMeta(), properties.getChestPlateAttributes(), attribute, operation);
        ItemBuilder leggings = properties.getLeggings();
        if (leggings != null && (weightFilter == null || WeightClass.getWeightClass(leggings.getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, leggings.getMeta(), properties.getLeggingsAttributes(), attribute, operation);
        ItemBuilder boots = properties.getBoots();
        if (boots != null && (weightFilter == null || WeightClass.getWeightClass(boots.getMeta()) == weightFilter))
            total += getValue(entity, equipmentPenalty, boots.getMeta(), properties.getBootsAttributes(), attribute, operation);

        if (properties.getMainHand() != null && ItemUtils.usedMainHand(properties.getMainHand(), properties.getOffHand()))
            total += getValue(entity, equipmentPenalty, properties.getMainHand().getMeta(), properties.getMainHandAttributes(), attribute, operation);
        else if (!mainHandOnly && properties.getOffHand() != null) total += getValue(entity, equipmentPenalty, properties.getOffHand().getMeta(), properties.getOffHandAttributes(), attribute, operation);

        for (ItemBuilder extra : properties.getMiscEquipment()){
            if (WeightClass.getWeightClass(extra.getMeta()) != weightFilter) continue;
            total += getValue(entity, equipmentPenalty, extra.getMeta(), properties.getMiscEquipmentAttributes().get(extra), attribute, operation);
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
        EntityEquipment eEquipment = e.getEquipment();
        if (eEquipment != null) {
            properties.getPermanentPotionEffects().clear();
            List<List<PotionEffect>> permanentEffects = new ArrayList<>();
            if (e instanceof Player p) {
                PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
                permanentEffects.add(PermanentPotionEffects.fromString(String.join(";", profile.getPermanentPotionEffects())));
            }
            if (equipment){
                properties.getCombinedEnchantments().clear();
                properties.setHelmet(eEquipment.getHelmet());
                properties.setChestplate(eEquipment.getChestplate());
                properties.setLeggings(eEquipment.getLeggings());
                properties.setBoots(eEquipment.getBoots());

                int[] armorWeights = WeightClass.getWeightClasses(properties);
                properties.setHeavyArmorCount(armorWeights[2]);
                properties.setLightArmorCount(armorWeights[1]);
                properties.setWeightlessArmorCount(armorWeights[0]);

                ItemBuilder helmet = properties.getHelmet();
                if (helmet != null) {
                    properties.addCombinedEnchantments(helmet);
                    properties.setHelmetAttributes(ItemAttributesRegistry.getStats(helmet.getMeta(), false));
                    permanentEffects.add(PermanentPotionEffects.getPermanentPotionEffects(helmet.getMeta()));
                }
                ItemBuilder chestPlate = properties.getChestplate();
                if (chestPlate != null) {
                    properties.addCombinedEnchantments(chestPlate);
                    properties.setChestPlateAttributes(ItemAttributesRegistry.getStats(chestPlate.getMeta(), false));
                    permanentEffects.add(PermanentPotionEffects.getPermanentPotionEffects(chestPlate.getMeta()));
                }
                ItemBuilder leggings = properties.getLeggings();
                if (leggings != null) {
                    properties.addCombinedEnchantments(leggings);
                    properties.setLeggingsAttributes(ItemAttributesRegistry.getStats(leggings.getMeta(), false));
                    permanentEffects.add(PermanentPotionEffects.getPermanentPotionEffects(leggings.getMeta()));
                }
                ItemBuilder boots = properties.getBoots();
                if (boots != null) {
                    properties.addCombinedEnchantments(boots);
                    properties.setBootsAttributes(ItemAttributesRegistry.getStats(boots.getMeta(), false));
                    permanentEffects.add(PermanentPotionEffects.getPermanentPotionEffects(boots.getMeta()));
                }
                for (BiFetcher<List<ItemStack>, LivingEntity> fetcher : otherEquipmentFetchers){
                    for (ItemStack otherEquipment : fetcher.get(e)) {
                        ItemBuilder builder = new ItemBuilder(otherEquipment);
                        properties.getMiscEquipment().add(builder);
                        properties.getMiscEquipmentAttributes().put(builder, ItemAttributesRegistry.getStats(builder.getMeta(), false));
                        permanentEffects.add(PermanentPotionEffects.getPermanentPotionEffects(builder.getMeta()));
                    }
                }
            }
            if (hands){
                properties.setMainHand(e.getEquipment().getItemInMainHand());
                properties.setOffHand(e.getEquipment().getItemInOffHand());

                ItemBuilder mainHand = properties.getMainHand();
                properties.addCombinedEnchantments(mainHand);
                if (mainHand != null && EquipmentClass.isHandHeld(mainHand.getMeta()) && EquipmentClass.getMatchingClass(mainHand.getMeta()) != EquipmentClass.TRINKET) {
                    properties.setMainHandAttributes(ItemAttributesRegistry.getStats(mainHand.getMeta(), false));
                    permanentEffects.add(PermanentPotionEffects.getPermanentPotionEffects(mainHand.getMeta()));
                }

                ItemBuilder offHand = properties.getOffHand();
                properties.addCombinedEnchantments(offHand);
                if (offHand != null && EquipmentClass.isHandHeld(offHand.getMeta()) && EquipmentClass.getMatchingClass(offHand.getMeta()) != EquipmentClass.TRINKET) {
                    properties.setOffHandAttributes(ItemAttributesRegistry.getStats(offHand.getMeta(), false));
                    permanentEffects.add(PermanentPotionEffects.getPermanentPotionEffects(offHand.getMeta()));
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

    public static void damage(LivingEntity entity, double amount, String type, boolean ignoreImmunity){
        damage(entity, null, amount, type, ignoreImmunity);
    }

    private static final Collection<UUID> activeDamageProcesses = new HashSet<>();

    public static boolean hasActiveDamageProcess(Entity damaged){
        return activeDamageProcesses.contains(damaged.getUniqueId());
    }

    public static void damage(LivingEntity entity, Entity by, double amount, String type, boolean ignoreImmunity){
        if (entity.isDead() || activeDamageProcesses.contains(entity.getUniqueId())) return;
        int immunityBefore = entity.getNoDamageTicks();
        if (ignoreImmunity) entity.setNoDamageTicks(0);
        EntityDamagedListener.setCustomDamageCause(entity.getUniqueId(), type);
        entity.setMetadata(DAMAGE_CAUSE_KEY, new FixedMetadataValue(ValhallaMMO.getInstance(), type));
        activeDamageProcesses.add(entity.getUniqueId());
        if (by != null) {
            EntityDamagedListener.setDamager(entity, by);
            entity.damage(amount, by);
        } else entity.damage(amount);
        activeDamageProcesses.remove(entity.getUniqueId());
        entity.removeMetadata(DAMAGE_CAUSE_KEY, ValhallaMMO.getInstance());
        entity.setNoDamageTicks(immunityBefore);
    }

    public static String getCustomDamageType(LivingEntity entity){
        if (!entity.hasMetadata(DAMAGE_CAUSE_KEY)) return null;
        return entity.getMetadata(DAMAGE_CAUSE_KEY).getFirst().asString();
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

    public static boolean isUnarmed(LivingEntity player){
        EntityProperties properties = EntityCache.getAndCacheProperties(player);

        // player is holding something that's not weightless, not unarmed!
        if (properties.getMainHand() != null && WeightClass.getWeightClass(properties.getMainHand().getMeta()) != WeightClass.WEIGHTLESS) return false;

        // player is not holding anything, unarmed!
        if (properties.getMainHand() == null) return true;
        AttributeWrapper damageAttribute = ItemAttributesRegistry.getAnyAttribute(properties.getMainHand().getMeta(), "GENERIC_ATTACK_DAMAGE");
        // Has a damage attribute, but not a defined weight class. That means that it must be a damaging item not explicitly marked weightless
        return damageAttribute == null || WeightClass.hasDefinedWeightClass(properties.getMainHand().getMeta());
    }
}