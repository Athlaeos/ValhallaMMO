package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ResultChangingModifier;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.gui.implementations.CustomItemSelectionMenu;
import me.athlaeos.valhallammo.gui.implementations.DynamicModifierMenu;
import me.athlaeos.valhallammo.item.CustomItem;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemReplaceByIndexed extends DynamicItemModifier implements ResultChangingModifier {
    private String item = null;
    private boolean onlyExecuteModifiers = false;

    public ItemReplaceByIndexed(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (onlyExecuteModifiers) {
            CustomItem customItem = CustomItemRegistry.getItem(item);
            DynamicItemModifier.modify(context, customItem.getModifiers());
        } else {
            ItemStack customItem = CustomItemRegistry.getProcessedItem(item, context);
            if (ItemUtils.isEmpty(customItem)) return;
            context.getItem().setItem(customItem);
            context.getItem().setMeta(ItemUtils.getItemMeta(customItem));
        }
    }

    @Override
    public boolean requiresPlayer() {
        CustomItem customItem = CustomItemRegistry.getItem(item);
        if (customItem == null) return false;
        return customItem.getModifiers().stream().anyMatch(DynamicItemModifier::requiresPlayer);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {

    }

    @Override
    public void onButtonPress(InventoryClickEvent e, DynamicModifierMenu menu, int button) {
        if (button == 7) onlyExecuteModifiers = !onlyExecuteModifiers;
        if (button == 12){
            new CustomItemSelectionMenu(menu.getPlayerMenuUtility(), (item) -> {
                this.item = item.getId();
                menu.open();
            }).open();
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.CHEST)
                        .name("&fWhich item?")
                        .lore("&fItem set to &e" + item,
                                "&6Click to select")
                        .get()).map(Set.of(
                new Pair<>(7,
                        new ItemBuilder(Material.REDSTONE_TORCH)
                                .name("&fShould only its modifiers be executed?")
                                .lore("&fSet to &e" + onlyExecuteModifiers,
                                        "&7If enabled, the item will not be",
                                        "&7replaced. Instead, the modifiers",
                                        "&7of the index item are executed",
                                        "&6Click to toggle")
                                .get())
        ));
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

    public void setOnlyExecuteModifiers(boolean onlyExecuteModifiers) {
        this.onlyExecuteModifiers = onlyExecuteModifiers;
    }

    @Override
    public DynamicItemModifier copy() {
        ItemReplaceByIndexed m = new ItemReplaceByIndexed(getName());
        m.setItem(this.item);
        m.setPriority(this.getPriority());
        m.setOnlyExecuteModifiers(this.onlyExecuteModifiers);
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
