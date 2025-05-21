package me.athlaeos.valhallammo.item;

import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class MetaHandler {
    public abstract boolean hasDisplayName();
    public abstract String getDisplayName();
    public abstract void setDisplayName(String name);
    public abstract boolean hasLore();
    public abstract List<String> getLore();
    public abstract void setLore(List<String> lore);
    public abstract ItemMeta getMeta();

    public static MetaHandler of(ItemMeta meta) {
        return new ItemMetaHandler(meta);
    }

    public static MetaHandler of(ItemBuilder itemBuilder) {
        return new ItemBuilderMetaHandler(itemBuilder);
    }

    private static class ItemMetaHandler extends MetaHandler {
        private final ItemMeta meta;
        public ItemMetaHandler(ItemMeta meta) { this.meta = meta; }
        @Override public boolean hasDisplayName() { return meta.hasDisplayName(); }
        @Override public String getDisplayName() { return meta.getDisplayName(); }
        @Override public void setDisplayName(String name) {meta.setDisplayName(name); }
        @Override public boolean hasLore() { return meta.hasLore(); }
        @Override public List<String> getLore() { return meta.getLore(); }
        @Override public void setLore(List<String> lore) { meta.setLore(lore); }
        @Override public ItemMeta getMeta() { return meta; }
    }

    private static class ItemBuilderMetaHandler extends MetaHandler {
        private final ItemBuilder itemBuilder;
        public ItemBuilderMetaHandler(ItemBuilder itemBuilder) { this.itemBuilder = itemBuilder; }
        @Override public boolean hasDisplayName() { return getDisplayName() != null; }
        @Override public String getDisplayName() { return itemBuilder.getName(); }
        @Override public void setDisplayName(String name) { itemBuilder.name(name); }
        @Override public boolean hasLore() { return getLore() != null; }
        @Override public List<String> getLore() { return itemBuilder.getLore(); }
        @Override public void setLore(List<String> lore) { itemBuilder.lore(lore); }
        @Override public ItemMeta getMeta() { return itemBuilder.getMeta(); }
    }
}