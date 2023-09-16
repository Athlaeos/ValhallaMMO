package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.dom.EntityProperties;
import me.athlaeos.valhallammo.dom.Fetcher;
import me.athlaeos.valhallammo.dom.WeightClass;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import org.bukkit.Tag;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

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

    private static final Collection<Fetcher<List<ItemStack>, LivingEntity>> otherEquipmentFetchers = new HashSet<>();
    public static void registerEquipmentFetcher(Fetcher<List<ItemStack>, LivingEntity> fetcher){
        otherEquipmentFetchers.add(fetcher);
    }

    public static EntityProperties getEntityProperties(LivingEntity e, boolean getEquipment, boolean getHands, boolean getPotionEffects){
        EntityProperties equipment = new EntityProperties();
        if (e == null) return equipment;
        return updateProperties(equipment, e, getEquipment, getHands, getPotionEffects);
    }

    public static EntityProperties updateProperties(EntityProperties properties, LivingEntity e, boolean equipment, boolean hands, boolean getPotionEffects){
        if (e.getEquipment() != null) {
            if (equipment){
                properties.setHelmet(e.getEquipment().getHelmet());
                properties.setChestplate(e.getEquipment().getChestplate());
                properties.setLeggings(e.getEquipment().getLeggings());
                properties.setBoots(e.getEquipment().getBoots());

                properties.setHeavyArmorCount(WeightClass.getArmorWeightClassCount(e, WeightClass.HEAVY));
                properties.setLightArmorCount(WeightClass.getArmorWeightClassCount(e, WeightClass.HEAVY));
                properties.setWeightlessArmorCount(WeightClass.getArmorWeightClassCount(e, WeightClass.HEAVY));

                if (properties.getHelmet() != null) properties.setHelmetAttributes(ItemAttributesRegistry.getStats(properties.getHelmet().getMeta(), true));
                if (properties.getChestplate() != null) properties.setChestPlateAttributes(ItemAttributesRegistry.getStats(properties.getHelmet().getMeta(), true));
                if (properties.getLeggings() != null) properties.setLeggingsAttributes(ItemAttributesRegistry.getStats(properties.getHelmet().getMeta(), true));
                if (properties.getBoots() != null) properties.setBootsAttributes(ItemAttributesRegistry.getStats(properties.getHelmet().getMeta(), true));
                for (Fetcher<List<ItemStack>, LivingEntity> fetcher : otherEquipmentFetchers){
                    List<ItemBuilder> otherEquipment = fetcher.get(e).stream().map(ItemBuilder::new).collect(Collectors.toList());
                    properties.getMiscEquipment().addAll(otherEquipment);
                    otherEquipment.forEach(i -> properties.getMiscEquipmentAttributes().put(i, ItemAttributesRegistry.getStats(i.getMeta(), false)));
                }
            }
            if (hands){
                properties.setMainHand(e.getEquipment().getItemInMainHand());
                properties.setOffHand(e.getEquipment().getItemInOffHand());
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
        if (getPotionEffects) properties.getActivePotionEffects().putAll(PotionEffectRegistry.getActiveEffects(e));

        return properties;
    }

    public static Entity getTrueDamager(EntityDamageByEntityEvent e){
        if (e.getDamager() instanceof Projectile p && p.getShooter() instanceof Entity t) return t;
        return e.getDamager();
    }
}
