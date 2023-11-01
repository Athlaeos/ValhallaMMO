package me.athlaeos.valhallammo.item.item_attributes.implementations;

import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AttributeHiddenWrapper extends AttributeWrapper {
    private final Collection<Material> compatibleWith = new HashSet<>();

    /**
     * Hidden attribute wrappers simply add the stat to the item, without displaying it. By default just used for custom max durability because
     * custom max durability is already displayed in {@link me.athlaeos.valhallammo.item.CustomDurabilityManager}
     */
    public AttributeHiddenWrapper(String attribute, StatFormat format) {
        super(attribute, format);
    }

    public AttributeHiddenWrapper(String attribute, StatFormat format, Material... compatibleWith) {
        super(attribute, format);
        this.compatibleWith.addAll(Set.of(compatibleWith));
    }

    @Override
    public boolean isCompatible(ItemStack i) {
        return compatibleWith.isEmpty() || compatibleWith.contains(i.getType());
    }

    @Override
    public void onApply(ItemMeta i) {
        // do nothing
    }

    @Override
    public void onRemove(ItemMeta i) {
        // do nothing
    }

    @Override
    public AttributeWrapper copy() {
        return new AttributeHiddenWrapper(attribute, format, compatibleWith.toArray(new Material[0]));
    }

    @Override
    public String toString(){
        return format == null ? StatFormat.FLOAT_P2.format(value) : format.format(value);
    }
}
