package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ResultChangingModifier;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.CustomItem;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemReplaceByIndexed extends DynamicItemModifier implements ResultChangingModifier {
    private String item = null;

    public ItemReplaceByIndexed(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        ItemStack customItem = CustomItemRegistry.getProcessedItem(item, context.getCrafter());
        if (ItemUtils.isEmpty(customItem)) return;
        context.getItem().setItem(customItem);
        context.getItem().setMeta(ItemUtils.getItemMeta(customItem));
    }

    @Override
    public ItemStack getNewResult(ModifierContext context) {
        return item == null ? null : CustomItemRegistry.getProcessedItem(item, context.getCrafter());
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        List<CustomItem> items = new ArrayList<>(CustomItemRegistry.getItems().values());
        if (items.isEmpty()) return;
        if (button == 12){
            CustomItem i = item == null ? null : CustomItemRegistry.getItems().get(item);
            int currentItem = i == null ? -1 : items.indexOf(i);
            if (e.isLeftClick()) item = items.get(Math.max(0, Math.min(items.size() - 1, currentItem + 1))).getId();
            else item = items.get(Math.max(0, Math.min(items.size() - 1, currentItem - 1))).getId();
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.CHEST)
                        .name("&fWhich item?")
                        .lore("&fItem set to &e" + item,
                                "&6Click to cycle")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.ENDER_CHEST).get();
    }

    @Override
    public String getDisplayName() {
        return "&dReplace Item by Custom Item";
    }

    @Override
    public String getDescription() {
        return "&fReplaces the item by one of the item in the custom item registry (/val items)";
    }

    @Override
    public String getActiveDescription() {
        return "&fReplaces the item by " + item + " in the custom item registry (/val items)";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setItem(String item) {
        this.item = item;
    }

    @Override
    public DynamicItemModifier copy() {
        ItemReplaceByIndexed m = new ItemReplaceByIndexed(getName());
        m.setItem(this.item);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length < 1) return "One argument is expected: the name of the item";
        item = args[0];
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return new ArrayList<>(CustomItemRegistry.getItems().keySet());
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
