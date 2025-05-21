package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Enchanter {
    private static final Map<Enchantment, E> enchantmentRanges = new HashMap<>();
    private static final Map<Material, Integer> enchantabilityMap = new HashMap<>();

    static {
        enchantmentRanges.put(EnchantmentMappings.POWER.getEnchantment(), new E(10).range(1, 1, 16).range(2, 11, 26).range(3, 21, 36).range(4, 31, 46).range(5, 41, 56));
        enchantmentRanges.put(EnchantmentMappings.FLAME.getEnchantment(), new E(2).range(1, 20, 50));
        enchantmentRanges.put(EnchantmentMappings.INFINITY.getEnchantment(), new E(1).range(1, 20, 50));
        enchantmentRanges.put(EnchantmentMappings.PUNCH.getEnchantment(), new E(2).range(1, 12, 37).range(2, 32, 57));
        enchantmentRanges.put(EnchantmentMappings.CURSE_OF_BINDING.getEnchantment(), new E(1).range(1, 25, 50));
        enchantmentRanges.put(EnchantmentMappings.CHANNELING.getEnchantment(), new E(1).range(1, 25, 50));
        enchantmentRanges.put(EnchantmentMappings.SHARPNESS.getEnchantment(), new E(10).range(1, 1, 21).range(2, 12, 32).range(3, 23, 43).range(4, 34, 54).range(5, 45, 65));
        enchantmentRanges.put(EnchantmentMappings.BANE_OF_ARTHROPODS.getEnchantment(), new E(5).range(1, 5, 25).range(2, 13, 33).range(3, 21, 41).range(4, 29, 49).range(5, 37, 57));
        enchantmentRanges.put(EnchantmentMappings.SMITE.getEnchantment(), new E(5).range(1, 5, 25).range(2, 13, 33).range(3, 21, 41).range(4, 29, 49).range(5, 37, 57));
        enchantmentRanges.put(EnchantmentMappings.DEPTH_STRIDER.getEnchantment(), new E(2).range(1, 10, 25).range(2, 20, 35).range(3, 30, 45));
        enchantmentRanges.put(EnchantmentMappings.EFFICIENCY.getEnchantment(), new E(10).range(1, 1, 51).range(2, 11, 61).range(3, 21, 71).range(4, 31, 81).range(5, 41, 91));
        enchantmentRanges.put(EnchantmentMappings.UNBREAKING.getEnchantment(), new E(5).range(1, 5, 55).range(2, 13, 63).range(3, 21, 71));
        enchantmentRanges.put(EnchantmentMappings.FIRE_ASPECT.getEnchantment(), new E(2).range(1, 10, 60).range(2, 30, 80));
        enchantmentRanges.put(EnchantmentMappings.FROST_WALKER.getEnchantment(), new E(2).range(1, 10, 25).range(2, 20, 35));
        enchantmentRanges.put(EnchantmentMappings.IMPALING.getEnchantment(), new E(2).range(1, 1, 21).range(2, 9, 29).range(3, 17, 37).range(4, 25, 45).range(5, 33, 53));
        enchantmentRanges.put(EnchantmentMappings.KNOCKBACK.getEnchantment(), new E(5).range(1, 5, 55).range(2, 25, 75));
        enchantmentRanges.put(EnchantmentMappings.FORTUNE.getEnchantment(), new E(2).range(1, 15, 65).range(2, 24, 33).range(3, 33, 83));
        enchantmentRanges.put(EnchantmentMappings.LOOTING.getEnchantment(), new E(2).range(1, 15, 24).range(2, 24, 74).range(3, 33, 83));
        enchantmentRanges.put(EnchantmentMappings.LOYALTY.getEnchantment(), new E(5).range(1, 17, 50).range(2, 24, 50).range(3, 31, 50));
        enchantmentRanges.put(EnchantmentMappings.LUCK_OF_THE_SEA.getEnchantment(), new E(2).range(1, 15, 65).range(2, 24, 74).range(3, 33, 83));
        enchantmentRanges.put(EnchantmentMappings.LURE.getEnchantment(), new E(2).range(1, 15, 65).range(2, 24, 74).range(3, 33, 83));
        enchantmentRanges.put(EnchantmentMappings.MENDING.getEnchantment(), new E(2).range(1, 25, 75));
        enchantmentRanges.put(EnchantmentMappings.MULTISHOT.getEnchantment(), new E(2).range(1, 20, 50));
        enchantmentRanges.put(EnchantmentMappings.RESPIRATION.getEnchantment(), new E(2).range(1, 10, 40).range(2, 20, 50).range(3, 30, 60));
        enchantmentRanges.put(EnchantmentMappings.PIERCING.getEnchantment(), new E(10).range(1, 1, 50).range(2, 11, 50).range(3, 21, 50).range(4, 31, 50));
        enchantmentRanges.put(EnchantmentMappings.PROTECTION.getEnchantment(), new E(10).range(1, 1, 12).range(2, 12, 23).range(3, 23, 34).range(4, 34, 45));
        enchantmentRanges.put(EnchantmentMappings.BLAST_PROTECTION.getEnchantment(), new E(2).range(1, 5, 13).range(2, 13, 21).range(3, 21, 29).range(4, 29, 37));
        enchantmentRanges.put(EnchantmentMappings.FEATHER_FALLING.getEnchantment(), new E(5).range(1, 5, 11).range(2, 11, 17).range(3, 17, 23).range(4, 34, 42));
        enchantmentRanges.put(EnchantmentMappings.FIRE_PROTECTION.getEnchantment(), new E(5).range(1, 10, 18).range(2, 18, 26).range(3, 26, 34).range(4, 34, 42));
        enchantmentRanges.put(EnchantmentMappings.PROJECTILE_PROTECTION.getEnchantment(), new E(5).range(1, 3, 9).range(2, 9, 15).range(3, 15, 21).range(4, 21, 27));
        enchantmentRanges.put(EnchantmentMappings.QUICK_CHARGE.getEnchantment(), new E(5).range(1, 1, 50).range(2, 11, 50).range(3, 21, 50).range(4, 31, 50));
        enchantmentRanges.put(EnchantmentMappings.RIPTIDE.getEnchantment(), new E(2).range(1, 17, 50).range(2, 24, 50).range(3, 31, 50));
        enchantmentRanges.put(EnchantmentMappings.SILK_TOUCH.getEnchantment(), new E(1).range(1, 15, 65));
        enchantmentRanges.put(EnchantmentMappings.SOUL_SPEED.getEnchantment(), new E(1).range(1, 10, 25).range(2, 20, 35).range(3, 30, 45));
        enchantmentRanges.put(EnchantmentMappings.SWEEPING_EDGE.getEnchantment(), new E(2).range(1, 5, 20).range(2, 14, 29).range(3, 23, 38));
        enchantmentRanges.put(EnchantmentMappings.THORNS.getEnchantment(), new E(1).range(1, 10, 60).range(2, 30, 70).range(3, 50, 80));
        enchantmentRanges.put(EnchantmentMappings.CURSE_OF_VANISHING.getEnchantment(), new E(1).range(1, 25, 50));
        enchantmentRanges.put(EnchantmentMappings.AQUA_AFFINITY.getEnchantment(), new E(2).range(1, 1, 41));

        registerItem(15, Material.WOODEN_AXE, Material.WOODEN_PICKAXE, Material.WOODEN_SWORD, Material.WOODEN_HOE, Material.WOODEN_SHOVEL, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
        registerItem(5, Material.STONE_AXE, Material.STICKY_PISTON, Material.STONE_SWORD, Material.STONE_HOE, Material.STONE_SHOVEL);
        registerItem(12, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS);
        registerItem(9, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS);
        registerItem(14, Material.IRON_AXE, Material.IRON_PICKAXE, Material.IRON_SWORD, Material.IRON_HOE, Material.IRON_SHOVEL);
        registerItem(25, Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS);
        registerItem(22, Material.GOLDEN_AXE, Material.GOLDEN_PICKAXE, Material.GOLDEN_SWORD, Material.GOLDEN_HOE, Material.GOLDEN_SHOVEL);
        registerItem(10, Material.DIAMOND_AXE, Material.DIAMOND_PICKAXE, Material.DIAMOND_SWORD, Material.DIAMOND_HOE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS);
        registerItem(9, Material.TURTLE_HELMET);
        registerItem(15, Material.NETHERITE_AXE, Material.NETHERITE_PICKAXE, Material.NETHERITE_SWORD, Material.NETHERITE_HOE, Material.NETHERITE_SHOVEL, Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS);
    }

    public static Map<Enchantment, Integer> getRandomEnchantments(ItemStack item, ItemMeta meta, int level, boolean treasure){
        boolean isBook = item.getType() == Material.ENCHANTED_BOOK || item.getType() == Material.BOOK;
        int modifiedEnchantmentLevel = getModifiedEnchantmentLevel(item, meta, level);

        Map<Enchantment, Integer> possibleEnchantments = new HashMap<>();
        Map<Enchantment, Integer> chosenEnchantments = new HashMap<>();
        for (Enchantment e : enchantmentRanges.keySet()){
            if (!treasure && e.isTreasure()) {
                continue;
            }
            if (isBook || e.canEnchantItem(item)){
                int maxLevel = enchantmentRanges.get(e).getAmplifierForLevel(modifiedEnchantmentLevel);
                if (maxLevel > 0){
                    possibleEnchantments.put(e, maxLevel);
                }
            }
        }
        // list of possible enchantments now determined
        Map<Enchantment, E> availableEnchantments = new HashMap<>(enchantmentRanges);
        for (Enchantment e : enchantmentRanges.keySet()){
            if (!possibleEnchantments.containsKey(e)) {
                availableEnchantments.remove(e);
            }
        }
        Enchantment firstEnchantmentRoll = pickRandomEnchantment(availableEnchantments);
        if (firstEnchantmentRoll != null) {
            availableEnchantments.remove(firstEnchantmentRoll);
            int lv = possibleEnchantments.get(firstEnchantmentRoll);
            if (lv > 0){
                chosenEnchantments.put(firstEnchantmentRoll, lv);
            }
        }

        int limit = 10;
        while (Utils.getRandom().nextDouble() <= (modifiedEnchantmentLevel + 1) / 50D){
            if (limit <= 0) break;
            Enchantment additionalEnchantmentRoll = pickRandomEnchantment(availableEnchantments);
            if (additionalEnchantmentRoll == null) return new HashMap<>();
            availableEnchantments.remove(additionalEnchantmentRoll);
            int additionalEnchantmentRollLv = possibleEnchantments.get(additionalEnchantmentRoll);
            if (additionalEnchantmentRollLv > 0){
                chosenEnchantments.put(additionalEnchantmentRoll, additionalEnchantmentRollLv);
            }

            modifiedEnchantmentLevel = (int) Math.floor(modifiedEnchantmentLevel / 2D);
            limit--;
        }

        return chosenEnchantments;
    }

    public static void enchantItem(ItemStack i, ItemMeta meta, int level, boolean treasure){
        if (i == null || level == 0) return;

        Map<Enchantment, Integer> chosenEnchantments = getRandomEnchantments(i, meta, level, treasure);
        if (i.getType() == Material.BOOK) i.setType(Material.ENCHANTED_BOOK);

        if (meta instanceof EnchantmentStorageMeta eMeta){
            for (Enchantment e : chosenEnchantments.keySet()){
                eMeta.addStoredEnchant(e, chosenEnchantments.get(e), false);
            }
            ItemUtils.setMetaNoClone(i, eMeta);
        } else {
            i.addEnchantments(chosenEnchantments);
        }
    }

    private static Enchantment pickRandomEnchantment(Map<Enchantment, E> availableEnchantments){
        int combinedWeight = 0;
        for (E e : availableEnchantments.values()){
            combinedWeight += e.getWeight();
        }
        if (combinedWeight == 0) return null;
        int randInt = Utils.getRandom().nextInt(combinedWeight);
        for (Enchantment e : availableEnchantments.keySet()){
            randInt -= availableEnchantments.get(e).getWeight();
            if (randInt < 0) return e;
        }
        return null;
    }

    private static int getModifiedEnchantmentLevel(ItemStack i, ItemMeta meta, int b){
        int e = getEnchantability(i, meta);

        int r1 = (e >= 4) ? Utils.getRandom().nextInt((int) Math.floor(e / 4D)) : 1;
        int r2 = (e >= 4) ? Utils.getRandom().nextInt((int) Math.floor(e / 4D)) : 1;
        int enchantment_level = b + r1 + r2 + 1;

        double random = 1 + (Utils.getRandom().nextFloat() + Utils.getRandom().nextFloat() - 1) * 0.15;
        return Math.max(1, (int) Math.round(enchantment_level * random));
    }

    private static final NamespacedKey ENCHANTABILITY = new NamespacedKey(ValhallaMMO.getInstance(), "enchantability");
    public static int getEnchantability(ItemStack i, ItemMeta meta){
        return ItemUtils.getPDCInt(ENCHANTABILITY, meta, enchantabilityMap.getOrDefault(i.getType(), 1));
    }

    private static void registerItem(int enchant_ability, Material... materials){
        for (Material m : materials){
            enchantabilityMap.put(m, enchant_ability);
        }
    }

    private record Range(int min, int max) {
        public boolean isInRange(int i) {
            return i <= max && i >= min;
        }
    }

    private static class E {
        private final int weight;
        private final TreeMap<Integer, Range> ranges = new TreeMap<>();

        public E(int weight){
            this.weight = weight;
        }

        public E range(int level, int min, int max){
            this.ranges.put(level, new Range(min, max));
            return this;
        }

        public int getWeight() {
            return weight;
        }

        public int getAmplifierForLevel(int score){
            int maxLevel = 0;
            for (int i : ranges.keySet()){
                if (ranges.get(i).isInRange(score)){
                    maxLevel = i;
                }
            }

            return maxLevel;
        }
    }
}
