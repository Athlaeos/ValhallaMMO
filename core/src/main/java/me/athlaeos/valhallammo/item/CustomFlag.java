package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum CustomFlag {
    HIDE_TAGS,
    HIDE_QUALITY,
    DISPLAY_ATTRIBUTES, // tells an item to display its attributes anyway through lore, even if the vanilla HIDE_ATTRIBUTES flag is applied
    HIDE_DURABILITY,
    ATTRIBUTE_FOR_BOTH_HANDS,
    ATTRIBUTE_FOR_HELMET,
    INFINITY_EXPLOITABLE,
    UNCRAFTABLE,
    TEMPORARY_POTION_DISPLAY,
    UNENCHANTABLE,
    UNMENDABLE;

    private static final NamespacedKey FLAG = new NamespacedKey(ValhallaMMO.getInstance(), "item_flags");

    public static Collection<CustomFlag> getItemFlags(ItemMeta meta){
        Collection<CustomFlag> flags = new HashSet<>();
        String value = ItemUtils.getPDCString(FLAG, meta, null);
        if (StringUtils.isEmpty(value)) return flags;
        String[] stringFlags = value.split(";");
        for (String s : stringFlags){
            try {
                flags.add(CustomFlag.valueOf(s));
            } catch (IllegalArgumentException ignored){}
        }
        return flags;
    }

    public static void setItemFlags(ItemMeta meta, Collection<CustomFlag> flags){
        if (meta == null) return;
        if (flags == null || flags.isEmpty()){
            meta.getPersistentDataContainer().remove(FLAG);
        } else {
            meta.getPersistentDataContainer().set(FLAG, PersistentDataType.STRING, String.join(";", flags.stream().map(CustomFlag::toString).collect(Collectors.toSet())));
        }
    }

    public static void setItemFlags(ItemMeta meta, CustomFlag... flags){
        setItemFlags(meta, Set.of(flags));
    }

    public static boolean hasFlag(ItemMeta meta, CustomFlag flag){
        return getItemFlags(meta).contains(flag);
    }
    
    public static void addItemFlag(ItemMeta meta, CustomFlag... flag){
        Collection<CustomFlag> flags = getItemFlags(meta);
        flags.addAll(Set.of(flag));
        setItemFlags(meta, flags);
    }

    public static void removeItemFlag(ItemMeta meta, CustomFlag flag){
        Collection<CustomFlag> flags = getItemFlags(meta);
        flags.remove(flag);
        setItemFlags(meta, flags);
    }
}