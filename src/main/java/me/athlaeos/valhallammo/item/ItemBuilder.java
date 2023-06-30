package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material m){
        this.item = new ItemStack(m);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack i){
        this.item = i;
        this.meta = item.getItemMeta();
    }

    public ItemBuilder data(int data){
        if (data > 0) meta.setCustomModelData(data);
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
        meta.setLore(Utils.chat(lore));
        return this;
    }

    public ItemBuilder lore(String... lore){
        meta.setLore(Utils.chat(Arrays.stream(lore).toList()));
        return this;
    }

    public ItemBuilder amount(int amount){
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder flag(ItemFlag... flags){
        meta.addItemFlags(flags);
        return this;
    }

    public ItemStack get(){
        this.item.setItemMeta(meta);
        return item;
    }
}
