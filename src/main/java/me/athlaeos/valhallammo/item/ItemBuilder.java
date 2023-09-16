package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ItemBuilder {
    private ItemStack item;
    private ItemMeta meta;

    public ItemBuilder(Material m){
        this.item = new ItemStack(m);
        this.meta = ItemUtils.getItemMeta(item);
    }

    public ItemBuilder(ItemStack i){
        this.item = i.clone();
        this.meta = ItemUtils.getItemMeta(item);
    }

    public ItemBuilder type(Material type){
        item.setType(type);
        ItemUtils.updateStoredType(meta, type);
        return this;
    }

    public ItemBuilder translate(){
        TranslationManager.translateItemMeta(meta);
        return this;
    }

    public ItemBuilder data(int data){
        if (data > 0) meta.setCustomModelData(data);
        else meta.setCustomModelData(null);
        return this;
    }

    public ItemBuilder intTag(NamespacedKey key, int tag){
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, tag);
        return this;
    }

    public ItemBuilder stringTag(NamespacedKey key, String tag){
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, tag);
        return this;
    }

    public ItemBuilder name(String name){
        meta.setDisplayName(Utils.chat(name));
        return this;
    }

    public ItemBuilder lore(List<String> lore){
        if (lore == null) return this;
        meta.setLore(Utils.chat(lore));
        return this;
    }

    public ItemBuilder lore(String... lore){
        if (lore == null) return this;
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder appendLore(List<String> lore){
        if (lore == null) return this;
        List<String> l = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        l.addAll(Utils.chat(lore));
        meta.setLore(l);
        return this;
    }

    public ItemBuilder appendLore(String... lore){
        if (lore == null) return this;
        return appendLore(Arrays.asList(lore));
    }

    public ItemBuilder amount(int amount){
        item.setAmount(amount);
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
        return this;
    }

    public ItemBuilder enchant(Enchantment e, int level){
        if (meta instanceof EnchantmentStorageMeta m)
            m.addStoredEnchant(e, level, true);
        else
            item.addUnsafeEnchantment(e, level);
        return this;
    }

    public ItemStack get(){
        if (ItemUtils.isEmpty(this.item)) return null;
        ItemUtils.setItemMeta(this.item, meta);
        return item;
    }

    public ItemStack getItem() { return item; }
    public ItemMeta getMeta() { return meta; }
    public void setItem(ItemStack item) { this.item = item; }
    public void setMeta(ItemMeta meta) { this.meta = meta; }
}
