package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ItemBuilder {
    private ItemStack item;
    private ItemMeta meta;
    private String name;
    private List<String> lore;
    private boolean translate = true;
    private final Collection<MiningSpeed.EmbeddedTool> embeddedTools;

    public ItemBuilder copy(){
        return new ItemBuilder(item.clone(), meta.clone());
    }

    public ItemBuilder(Material m){
        this.item = new ItemStack(m);
        this.meta = ItemUtils.getItemMeta(item);
        this.embeddedTools = MiningSpeed.getEmbeddedTools(meta);
    }

    public ItemBuilder(ItemStack i, ItemMeta m){
        this.item = i.clone();
        this.meta = m.clone();
        this.embeddedTools = MiningSpeed.getEmbeddedTools(meta);
    }

    public ItemBuilder(ItemStack i){
        this.item = i.clone();
        this.meta = ItemUtils.getItemMeta(item);
        this.embeddedTools = MiningSpeed.getEmbeddedTools(meta);
    }

    public ItemBuilder type(Material type){
        item.setType(type);
        ItemUtils.setMetaNoClone(item, meta);
        meta = ItemUtils.getItemMeta(item);
        return this;
    }

    public ItemBuilder translate(){
        this.translate = true;
        return this;
    }

    /**
     * This method is mainly here because grabbing embedded tools off of items is a relatively more expensive task than grabbing plain ol properties,
     * so embedded tools are grabbed when ItemBuilder is initialized. Adding or removing embedded tools to the returned set has no effect on the meta itself.
     * @return the embedded tools stored on this item
     */
    public Collection<MiningSpeed.EmbeddedTool> getEmbeddedTools() {
        return new HashSet<>(embeddedTools);
    }

    public ItemBuilder data(int data){
        if (data > 0) meta.setCustomModelData(data);
        else meta.setCustomModelData(null);
        return this;
    }

    /**
     * Attempts to parse the given model to an integer. If it parses successfully, it is assumed
     * the model is custom model data. If it doesn't parse, it is instead set as the item's
     * model namespacedkey (this only works on Minecraft 1.21.3 and newer).
     * @param model the model or custom model data
     */
    public ItemBuilder model(String model){
        int asInteger = Catch.catchOrElse(() -> Integer.parseInt(model), -1);
        if (asInteger < 0 || MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_21_4)) {
            ValhallaMMO.getNms().setItemModel(meta, model);
        } else {
            meta.setCustomModelData(asInteger);
        }
        return this;
    }

    public ItemBuilder intTag(NamespacedKey key, int tag){
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, tag);
        return this;
    }

    public ItemBuilder floatTag(NamespacedKey key, float tag){
        meta.getPersistentDataContainer().set(key, PersistentDataType.FLOAT, tag);
        return this;
    }

    public ItemBuilder doubleTag(NamespacedKey key, double tag){
        meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, tag);
        return this;
    }

    public ItemBuilder stringTag(NamespacedKey key, String tag){
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, tag);
        return this;
    }

    public ItemBuilder name(String name){
        this.name = name;
        return this;
    }

    public ItemBuilder lore(List<String> lore){
        if (lore == null) return this;
        this.lore = lore;
        return this;
    }

    public String getName(){
        return name;
    }

    public ItemBuilder lore(String... lore){
        if (lore == null) return this;
        return lore(new ArrayList<>(Arrays.asList(lore)));
    }

    public ItemBuilder appendLore(List<String> lore){
        if (lore == null) return this;
        List<String> l = this.lore == null ? new ArrayList<>() : this.lore;
        l.addAll(lore);
        this.lore = l;
        return this;
    }

    public ItemBuilder prependLore(List<String> lore){
        if (lore == null) return this;
        lore = new ArrayList<>(lore);
        if (this.lore != null) lore.addAll(this.lore);
        this.lore = lore;
        return this;
    }

    public ItemBuilder placeholderLore(String placeholder, List<String> replaceWith){
        return lore(ItemUtils.setListPlaceholder(lore != null ? lore : new ArrayList<>(), placeholder, replaceWith));
    }

    public ItemBuilder placeholderLore(String placeholder, String replaceWith){
        return lore(ItemUtils.setListPlaceholder(lore != null ? lore : new ArrayList<>(), placeholder, replaceWith));
    }

    public ItemBuilder appendLore(String... lore){
        if (lore == null) return this;
        return appendLore(new ArrayList<>(Arrays.asList(lore)));
    }

    public ItemBuilder prependLore(String... lore){
        if (lore == null) return this;
        return prependLore(new ArrayList<>(Arrays.asList(lore)));
    }

    public List<String> getLore(){
        return lore;
    }

    public ItemBuilder amount(int amount){
        item.setAmount(Math.min(amount, 64));
        return this;
    }

    public ItemBuilder flag(ItemFlag... flags){
        meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder flag(CustomFlag... flags){
        CustomFlag.addItemFlag(this.meta, flags);
        return this;
    }

    public ItemBuilder color(Color color){
        if (meta instanceof LeatherArmorMeta m){
            m.setColor(color);
        } else if (meta instanceof PotionMeta m){
            m.setColor(color);
        }
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_21)){
            if (meta instanceof ColorableArmorMeta c){
                c.setColor(color);
            }
        }
        return this;
    }

    public ItemBuilder disEnchant(){
        if (meta instanceof EnchantmentStorageMeta m) m.getStoredEnchants().keySet().forEach(m::removeStoredEnchant);
        else item.getEnchantments().keySet().forEach(item::removeEnchantment);
        return this;
    }

    public ItemBuilder disEnchant(Enchantment e){
        if (meta instanceof EnchantmentStorageMeta m) m.removeStoredEnchant(e);
        else meta.removeEnchant(e);
        return this;
    }

    public ItemBuilder enchant(Enchantment e, int level){
        if (meta instanceof EnchantmentStorageMeta m) m.addStoredEnchant(e, level, true);
        else meta.addEnchant(e, level, true);
        return this;
    }

    public ItemStack get(){
        if (ItemUtils.isEmpty(this.item)) return null;
        if (this.name != null) {
            meta.setDisplayName(Utils.chat(this.name));
        }
        if (this.lore != null) {
            meta.setLore(Utils.chat(this.lore));
        }
        if (translate) {
            TranslationManager.translateItemMeta(meta);
        }
        ItemUtils.setMetaNoClone(this.item, meta);
        ItemUtils.storeType(meta, this.item.getType());
        return item;
    }

    public ItemBuilder attribute(String attribute, double value, AttributeModifier.Operation operation){
        ItemAttributesRegistry.addDefaultStat(meta, ItemAttributesRegistry.getCopy(attribute).setOperation(operation).setValue(value));
        return this;
    }

    public ItemBuilder attribute(String attribute, double value){
        ItemAttributesRegistry.addDefaultStat(meta, ItemAttributesRegistry.getCopy(attribute).setOperation(AttributeModifier.Operation.ADD_NUMBER).setValue(value));
        return this;
    }

    public ItemBuilder wipeAttributes(){
        meta.setAttributeModifiers(null);
        return this;
    }

    public ItemStack getItem() { return item; }
    public ItemMeta getMeta() { return meta; }
    public ItemBuilder setItem(ItemStack item) { this.item = item; return this; }
    public ItemBuilder setMeta(ItemMeta meta) { this.meta = meta; return this; }
}
