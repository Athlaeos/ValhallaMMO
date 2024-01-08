package me.athlaeos.valhallammo.item;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CustomItem {
    private final String id;
    private final ItemStack item;
    private List<DynamicItemModifier> modifiers = new ArrayList<>();

    public CustomItem(String id, ItemStack item){
        this.id = id;
        this.item = item;
    }

    public String getId() { return id; }
    public ItemStack getItem() { return item.clone(); }
    public List<DynamicItemModifier> getModifiers() { return modifiers; }
    public void setModifiers(List<DynamicItemModifier> modifiers) {
        this.modifiers = modifiers;
        DynamicItemModifier.sortModifiers(this.modifiers);
    }
}
