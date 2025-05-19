package me.athlaeos.valhallammo.playerstats;

import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityProperties {
    private ItemBuilder helmet = null;
    private Map<String, AttributeWrapper> helmetAttributes = new HashMap<>();
    private ItemBuilder chestplate = null;
    private Map<String, AttributeWrapper> chestplateAttributes = new HashMap<>();
    private ItemBuilder leggings = null;
    private Map<String, AttributeWrapper> leggingsAttributes = new HashMap<>();
    private ItemBuilder boots = null;
    private Map<String, AttributeWrapper> bootsAttributes = new HashMap<>();
    private ItemBuilder mainHand = null;
    private Map<String, AttributeWrapper> mainHandAttributes = new HashMap<>();
    private ItemBuilder offHand = null;
    private Map<String, AttributeWrapper> offHandAttributes = new HashMap<>();
    private final List<ItemBuilder> miscEquipment = new ArrayList<>();
    private final Map<ItemBuilder, Map<String, AttributeWrapper>> miscEquipmentAttributes = new HashMap<>();
    private int heavyArmorCount = 0;
    private int lightArmorCount = 0;
    private int weightlessArmorCount = 0;
    private final Map<String, CustomPotionEffect> activePotionEffects = new HashMap<>();
    private final Map<Enchantment, Integer> combinedEnchantments = new HashMap<>();
    private final List<PotionEffect> permanentPotionEffects = new ArrayList<>();

    public EntityProperties(){}

    public EntityProperties(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, ItemStack mainHand, ItemStack offHand){
        this.helmet = ItemUtils.isEmpty(helmet) ? null : new ItemBuilder(helmet);
        this.chestplate = ItemUtils.isEmpty(chestplate) ? null : new ItemBuilder(chestplate);
        this.leggings = ItemUtils.isEmpty(leggings) ? null : new ItemBuilder(leggings);
        this.boots = ItemUtils.isEmpty(boots) ? null : new ItemBuilder(boots);
        this.mainHand = ItemUtils.isEmpty(mainHand) ? null : new ItemBuilder(mainHand);
        this.offHand = ItemUtils.isEmpty(offHand) ? null : new ItemBuilder(offHand);
    }

    public void addCombinedEnchantments(ItemBuilder builder){
        if (builder == null) return;
        for (Map.Entry<Enchantment, Integer> entry : builder.getItem().getEnchantments().entrySet()){
            int existingLevel = combinedEnchantments.getOrDefault(entry.getKey(), 0);
            combinedEnchantments.put(entry.getKey(), existingLevel + entry.getValue());
        }
    }

    public Map<String, CustomPotionEffect> getActivePotionEffects() {
        return activePotionEffects;
    }
    public int getHeavyArmorCount() { return heavyArmorCount; }
    public void setHeavyArmorCount(int heavyArmorCount) { this.heavyArmorCount = heavyArmorCount; }
    public int getLightArmorCount() { return lightArmorCount; }
    public void setLightArmorCount(int lightArmorCount) { this.lightArmorCount = lightArmorCount; }
    public int getWeightlessArmorCount() { return weightlessArmorCount; }
    public void setWeightlessArmorCount(int weightlessArmorCount) { this.weightlessArmorCount = weightlessArmorCount; }
    public List<ItemBuilder> getMiscEquipment() { return miscEquipment; }
    public ItemBuilder getHelmet() { return helmet; }
    public void setHelmet(ItemStack helmet) { this.helmet = ItemUtils.isEmpty(helmet) ? null : new ItemBuilder(helmet); }
    public ItemBuilder getChestplate() { return chestplate; }
    public void setChestplate(ItemStack chestplate) { this.chestplate = ItemUtils.isEmpty(chestplate) ? null : new ItemBuilder(chestplate); }
    public ItemBuilder getBoots() { return boots; }
    public void setBoots(ItemStack boots) { this.boots = ItemUtils.isEmpty(boots) ? null : new ItemBuilder(boots); }
    public ItemBuilder getLeggings() { return leggings; }
    public void setLeggings(ItemStack leggings) { this.leggings = ItemUtils.isEmpty(leggings) ? null : new ItemBuilder(leggings); }
    public ItemBuilder getMainHand() { return mainHand; }
    public void setMainHand(ItemStack mainHand) { this.mainHand = ItemUtils.isEmpty(mainHand) ? null : new ItemBuilder(mainHand); }
    public ItemBuilder getOffHand() { return offHand; }
    public void setOffHand(ItemStack offHand) { this.offHand = ItemUtils.isEmpty(offHand) ? null : new ItemBuilder(offHand); }
    public Map<Enchantment, Integer> getCombinedEnchantments() { return combinedEnchantments; }
    public List<PotionEffect> getPermanentPotionEffects() { return permanentPotionEffects; }

    /**
     * Returns a list containing all of the entity's equipment.
     * @param includeHands whether the hand items should be included also
     * @param hand true if only main hand should be returned, false if only offhand, null if both
     * @return a list containing all of the entity's equipment
     */
    public List<ItemBuilder> getIterable(boolean includeHands, Boolean hand){
        List<ItemBuilder> iterable = getArmor();
        if (!miscEquipmentAttributes.isEmpty()) iterable.addAll(miscEquipment);
        if (includeHands){
            if ((hand == null || hand) && mainHand != null) iterable.add(mainHand);
            if ((hand == null || !hand) && offHand != null) iterable.add(offHand);
        }
        return iterable;
    }

    public List<ItemBuilder> getArmor(){
        List<ItemBuilder> armor = new ArrayList<>();
        if (helmet != null) armor.add(helmet);
        if (chestplate != null) armor.add(chestplate);
        if (leggings != null) armor.add(leggings);
        if (boots != null) armor.add(boots);
        return armor;
    }

    /**
     * Returns a list containing all attributes of the entity's equipment.
     * @param includeHands whether the hand items should be included also
     * @param hand true if only main hand should be returned, false if only offhand, null if both
     * @return a list containing all attributes of the entity's equipment
     */
    public List<Map<String, AttributeWrapper>> attributeIterable(boolean includeHands, Boolean hand){
        List<Map<String, AttributeWrapper>> iterable = new ArrayList<>();
        if (helmetAttributes != null && !helmetAttributes.isEmpty()) iterable.add(helmetAttributes);
        if (chestplateAttributes != null && !chestplateAttributes.isEmpty()) iterable.add(chestplateAttributes);
        if (leggingsAttributes != null && !leggingsAttributes.isEmpty()) iterable.add(leggingsAttributes);
        if (bootsAttributes != null && !bootsAttributes.isEmpty()) iterable.add(bootsAttributes);
        if (!miscEquipment.isEmpty()) iterable.addAll(miscEquipmentAttributes.values());
        if (includeHands){
            if ((hand == null || hand) && mainHandAttributes != null && !mainHandAttributes.isEmpty()) iterable.add(mainHandAttributes);
            if ((hand == null || !hand)  && offHandAttributes != null && !offHandAttributes.isEmpty()) iterable.add(offHandAttributes);
        }
        return iterable;
    }

    public List<ItemBuilder> getHands(){
        List<ItemBuilder> iterable = new ArrayList<>();
        if (mainHand != null) iterable.add(mainHand);
        if (offHand != null) iterable.add(offHand);
        return iterable;
    }

    public Map<String, AttributeWrapper> getBootsAttributes() { return bootsAttributes; }
    public Map<String, AttributeWrapper> getChestPlateAttributes() { return chestplateAttributes; }
    public Map<String, AttributeWrapper> getHelmetAttributes() { return helmetAttributes; }
    public Map<String, AttributeWrapper> getLeggingsAttributes() { return leggingsAttributes; }
    public Map<ItemBuilder, Map<String, AttributeWrapper>> getMiscEquipmentAttributes() { return miscEquipmentAttributes; }
    public Map<String, AttributeWrapper> getMainHandAttributes() { return mainHandAttributes; }
    public Map<String, AttributeWrapper> getOffHandAttributes() { return offHandAttributes; }
    public void setBootsAttributes(Map<String, AttributeWrapper> bootsAttributes) { this.bootsAttributes = bootsAttributes; }
    public void setChestPlateAttributes(Map<String, AttributeWrapper> chestplateAttributes) { this.chestplateAttributes = chestplateAttributes; }
    public void setHelmetAttributes(Map<String, AttributeWrapper> helmetAttributes) { this.helmetAttributes = helmetAttributes; }
    public void setLeggingsAttributes(Map<String, AttributeWrapper> leggingsAttributes) { this.leggingsAttributes = leggingsAttributes; }
    public void setMainHandAttributes(Map<String, AttributeWrapper> mainHandAttributes) { this.mainHandAttributes = mainHandAttributes; }
    public void setOffHandAttributes(Map<String, AttributeWrapper> offHandAttributes) { this.offHandAttributes = offHandAttributes; }
}