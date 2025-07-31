package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ResultChangingModifier;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemRandomize extends DynamicItemModifier implements ResultChangingModifier {
    private final ItemStack previewItem = new ItemBuilder(Material.PAPER).name("&fdivine tome of randomization :3").lore("&cshould not generally be visible", "&cbut someone used a randomizer", "&cwithout knowing what it does").get();
    private final Map<ItemStack, Float> items = new HashMap<>();
    private ItemStack currentItem = null;
    private float currentWeight = 0F;

    public ItemRandomize(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        ItemStack selected = Utils.weightedSelection(items, 1).getFirst();
        if (ItemUtils.isEmpty(selected)) {
            failedRecipe(context.getItem(), "&cImproperly configured randomization modifier!");
            return;
        }
        context.getItem().setItem(selected);
        context.getItem().setMeta(ItemUtils.getItemMeta(selected));
    }

    @Override
    public ItemStack getNewResult(ModifierContext context) {
        return previewItem;
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 6){
            if (!ItemUtils.isEmpty(e.getCursor())) currentItem = e.getCursor().clone();
        }
        if (button == 7) currentWeight = Math.max(0, currentWeight + ((e.isShiftClick() ? 25 : 1) * (e.isLeftClick() ? 0.1F : -0.1F)));
        if (button == 8) {
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
            List<Pair<ItemStack, Float>> sortedByWeight = new ArrayList<>();
            for (ItemStack itemStack : items.keySet()) sortedByWeight.add(new Pair<>(itemStack, items.get(itemStack)));
            sortedByWeight.sort(Comparator.comparingDouble(Pair::getTwo));
            for (Pair<ItemStack, Float> pair : sortedByWeight){
                float chance = Math.max(0, Math.min(1, pair.getTwo() / totalWeight));
                ItemBuilder asBuilder = new ItemBuilder(pair.getOne());
                String format = String.format("&7>> &e%.1f%% %s &8(%.1f)", chance * 100, ItemUtils.getItemName(asBuilder), pair.getTwo());
                lore.add(format);
            }
        }
        ItemBuilder currentAsBuilder = currentItem == null ? null : new ItemBuilder(currentItem);
        float chanceIfAdded = currentWeight / (totalWeight + currentWeight);
        if (currentAsBuilder != null){
            lore.add("");
            lore.add("&6Click to add " + ItemUtils.getItemName(currentAsBuilder) + "&6,");
            lore.add(String.format("&6which has a pick chance of %.1f%%", chanceIfAdded));
        }

        return new Pair<>(6,
                new ItemBuilder(currentItem == null ? new ItemStack(Material.BARRIER) : currentItem)
                        .name("&fWhich item?")
                        .lore("&fItem set to &e" + (currentAsBuilder == null ? "nothing" : ""),
                                "&fIf this item were added, it would have",
                                String.format("&7a &e%.1f%%&f chance of occurring", chanceIfAdded * 100),
                                "&6Click with item in cursor to set")
                        .get()).map(Set.of(
                new Pair<>(7,
                        new ItemBuilder(Material.NETHER_STAR)
                                .name("&fWhat should the chance (weight) of it be?")
                                .lore("&fWeight set to &e" + currentWeight + ",",
                                        String.format("&fwhich equates to &e%.1f%% &fif added", chanceIfAdded),
                                        "&6Click to increase/decrease by 0.1",
                                        "&6Shift-Click to do so by 2.5")
                                .get()),
                new Pair<>(8,
                        new ItemBuilder(Material.EMERALD)
                                .name("&fAdd Selection")
                                .lore("&fReplaces randomly to one of the following:")
                                .appendLore(lore)
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.DISPENSER).get();
    }

    @Override
    public String getDisplayName() {
        return "&dReplace By Random Item";
    }

    @Override
    public String getDescription() {
        return "&fReplaces the item by one of the given items at random, from a weighted picking system";
    }

    @Override
    public String getActiveDescription() {
        return "&fReplaces the item by one of " + items.size() + " at random, from a weighted picking system";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setItems(Map<ItemStack, Float> items) {
        this.items.clear();
        this.items.putAll(items);
    }

    public Map<ItemStack, Float> getItems() {
        return items;
    }

    @Override
    public DynamicItemModifier copy() {
        ItemRandomize m = new ItemRandomize(getName());
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
