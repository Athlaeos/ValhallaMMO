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
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemRandomizeIndexed extends DynamicItemModifier implements ResultChangingModifier {
    private final Map<String, Float> items = new HashMap<>();
    private String currentItem = null;
    private float currentWeight = 0;
    private boolean onlyExecuteModifiers = false;

    public ItemRandomizeIndexed(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        String selected = Utils.weightedSelection(items, 1).getFirst();
        CustomItem selectedItem = selected == null ? null : CustomItemRegistry.getItem(selected);
        if (selectedItem == null) {
            failedRecipe(context.getItem(), "&cImproperly configured indexed randomization modifier!");
            return;
        }
        if (onlyExecuteModifiers) {
            DynamicItemModifier.modify(context, selectedItem.getModifiers());
        } else {
            ItemStack processed = CustomItemRegistry.getProcessedItem(selected, context);
            context.getItem().setItem(processed);
            context.getItem().setMeta(ItemUtils.getItemMeta(processed));
        }
    }

    @Override
    public boolean requiresPlayer() {
        for (String s : items.keySet()){
            CustomItem customItem = CustomItemRegistry.getItem(s);
            if (customItem == null) continue;
            if (customItem.getModifiers().stream().anyMatch(DynamicItemModifier::requiresPlayer)) return true;
        }
        return false;
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {

    }

    @Override
    public void onButtonPress(InventoryClickEvent e, DynamicModifierMenu menu, int button) {
        if (button == 7) onlyExecuteModifiers = !onlyExecuteModifiers;
        if (button == 11){
            new CustomItemSelectionMenu(menu.getPlayerMenuUtility(), (item) -> {
                this.currentItem = item.getId();
                menu.open();
            }).open();
        }
        if (button == 12) currentWeight = Math.max(0, currentWeight + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.1F : -0.1F)));
        if (button == 13) {
            if (e.isShiftClick()) this.items.clear();
            else if (currentItem != null) {
                this.items.put(currentItem, currentWeight);
                currentItem = null;
            }
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        float totalWeight = 0F;
        for (float f : items.values()) totalWeight += f;

        List<String> lore = new ArrayList<>();
        if (items.isEmpty()) lore.add("&cNo items");
        else {
            List<Pair<String, Float>> sortedByWeight = new ArrayList<>();
            for (String itemID : items.keySet()) sortedByWeight.add(new Pair<>(itemID, items.get(itemID)));
            sortedByWeight.sort(Comparator.comparingDouble(Pair::getTwo));
            for (Pair<String, Float> pair : sortedByWeight){
                float chance = Math.max(0, Math.min(1, pair.getTwo() / totalWeight));
                String format = String.format("&7>> &e%.1f%% %s &8(%.1f)", chance * 100, pair.getOne(), pair.getTwo());
                lore.add(format);
            }
        }
        float chanceIfAdded = currentWeight / (totalWeight + currentWeight);
        if (currentItem != null){
            lore.add("");
            lore.add("&6Click to add " + currentItem + "&6,");
            lore.add(String.format("&6which has a pick chance of %.1f%%", chanceIfAdded));
        }

        return new Pair<>(11,
                new ItemBuilder(currentItem == null || CustomItemRegistry.getItem(currentItem) == null ? new ItemStack(Material.BARRIER) : CustomItemRegistry.getItem(currentItem).getItem())
                        .name("&fWhich item?")
                        .lore("&fItem set to &e" + (currentItem == null ? "nothing" : currentItem),
                                "&fIf this item were added, it would have",
                                String.format("&7a &e%.1f%%&f chance of occurring", chanceIfAdded * 100),
                                "&6Click with item in cursor to set")
                        .get()).map(Set.of(
                new Pair<>(12,
                        new ItemBuilder(Material.NETHER_STAR)
                                .name("&fWhat should the chance (weight) of it be?")
                                .lore("&fWeight set to &e" + currentWeight + ",",
                                        String.format("&fwhich equates to &e%.1f%% &fif added", chanceIfAdded),
                                        "&6Click to increase/decrease by 0.1",
                                        "&6Shift-Click to do so by 2.5")
                                .get()),
                new Pair<>(13,
                        new ItemBuilder(Material.EMERALD)
                                .name("&fAdd Selection")
                                .lore("&fReplaces randomly to one of the following:")
                                .appendLore(lore)
                                .get()),
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
        return "&dReplace By Random Indexed Item";
    }

    @Override
    public String getDescription() {
        return "&fReplaces the item by one of the given indexed items at random, from a weighted picking system";
    }

    @Override
    public String getActiveDescription() {
        return "&fReplaces the item by one of " + items.size() + " at random, from a weighted picking system";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setItems(Map<String, Float> items) {
        this.items.clear();
        this.items.putAll(items);
    }

    public Map<String, Float> getItems() {
        return items;
    }

    @Override
    public DynamicItemModifier copy() {
        ItemRandomizeIndexed m = new ItemRandomizeIndexed(getName());
        m.setItems(this.items);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        return "Too complex for command usage";
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 0;
    }
}
