package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public record ModifierCategory(String id, int order, ItemStack icon) {
    private static final NamespacedKey CATEGORY_KEY = ValhallaMMO.key("modifier_category");

    public ModifierCategory(String id, int order, ItemStack icon) {
        this.id = id;
        this.order = order;
        this.icon = new ItemBuilder(icon).stringTag(CATEGORY_KEY, id).get();
    }
}
