package me.athlaeos.valhallammo.skills.perkresourcecost.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOptionRegistry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpense;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.regex.Pattern;

public class ItemExpense implements ResourceExpense {
    private final Map<String, ItemCost> valhallaItemCost = new HashMap<>();
    private final Map<ItemStack, Integer> vanillaItemCost = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public void initExpense(Object value) {
        if (value instanceof List<?> list) {
            for (String s : (Collection<? extends String>) list){
                String[] split = s.split(Pattern.quote("|"));
                int amount = Catch.catchOrElse(() -> Integer.parseInt(split[1]), 1);
                Material material = Catch.catchOrElse(() -> Material.valueOf(split[0]), null);

                if (material == null) {
                    RecipeOption matcher = RecipeOptionRegistry.createOption(Catch.catchOrElse(() -> split[2], "CHOICE_MATERIAL_DATA"));
                    valhallaItemCost.put(split[0], new ItemCost(amount, matcher));
                } else vanillaItemCost.put(new ItemStack(material), amount);
            }
        }
    }

    @Override
    public boolean canPurchase(Player p) {
        List<ItemStack> inventory = Arrays.asList(p.getInventory().getStorageContents());
        if (ItemUtils.timesContained(inventory, vanillaItemCost, new MaterialChoice()) <= 0) return false;
        for (String id : valhallaItemCost.keySet()){
            ItemStack item = CustomItemRegistry.getProcessedItem(id, p);
            if (item == null) ValhallaMMO.logWarning("Perk item cost " + id + " was not a valid vanilla OR valhalla item, please fix");
            else {
                ItemCost cost = valhallaItemCost.get(id);
                if (ItemUtils.timesContained(inventory, Map.of(item, cost.quantity), cost.matcher) <= 0) return false;
            }
        }
        return true;
    }

    @Override
    public void purchase(Player p, boolean initialPurchase) {
        if (!initialPurchase) return;

        if (!vanillaItemCost.isEmpty())
            ItemUtils.removeItems(p.getInventory(), vanillaItemCost, 1, new MaterialChoice());
        if (!valhallaItemCost.isEmpty()) {
            for (String id : valhallaItemCost.keySet()){
                ItemStack item = CustomItemRegistry.getProcessedItem(id, p);
                if (item == null) ValhallaMMO.logWarning("Perk item cost " + id + " was not a valid vanilla OR valhalla item, please fix");
                else {
                    ItemCost cost = valhallaItemCost.get(id);
                    ItemUtils.removeItems(p.getInventory(), Map.of(item, cost.quantity), 1, cost.matcher);
                }
            }
        }
    }

    @Override
    public void refund(Player p) {
        if (!vanillaItemCost.isEmpty()){
            List<ItemStack> decompressed = ItemUtils.decompressStacks(vanillaItemCost);
            decompressed.forEach(i -> ItemUtils.addItem(p, i, true));
        }

        if (!valhallaItemCost.isEmpty()){
            Map<ItemStack, Integer> items = new HashMap<>();
            for (String item : valhallaItemCost.keySet()){
                ItemStack processed = CustomItemRegistry.getProcessedItem(item, p);
                if (processed == null) continue;
                ItemCost cost = valhallaItemCost.get(item);
                items.put(processed, cost.quantity);
            }
            List<ItemStack> decompressed = ItemUtils.decompressStacks(items);
            decompressed.forEach(i -> ItemUtils.addItem(p, i, true));
        }
    }

    private final boolean refundable = ConfigManager.getConfig("config.yml").reload().get().getBoolean("forgettable_perks_refund_items", true);
    @Override
    public boolean isRefundable() {
        return refundable;
    }

    @Override
    public ResourceExpense createInstance() {
        return new ItemExpense();
    }

    @Override
    public String getInsufficientFundsMessage() {
        return TranslationManager.getTranslation("warning_insufficient_items");
    }

    @Override
    public String getCostPlaceholder() {
        return "%cost_items%";
    }

    @Override
    public String getInsufficientCostPlaceholder() {
        return "%warning_cost_items%";
    }

    @Override
    public String getCostMessage() {
        String format = TranslationManager.getTranslation("status_items_cost");
        String entry = TranslationManager.getTranslation("status_items_cost_format");
        StringBuilder costBuilder = new StringBuilder();
        for (ItemStack i : vanillaItemCost.keySet()){
            costBuilder.append(entry
                    .replace("%item%", ItemUtils.getItemName(ItemUtils.getItemMeta(i)))
                    .replace("%quantity%", String.valueOf(vanillaItemCost.get(i)))
            );
        }
        for (String i : valhallaItemCost.keySet()){
            ItemStack processed = CustomItemRegistry.getProcessedItem(i);
            if (processed == null) continue;
            ItemCost cost = valhallaItemCost.get(i);
            costBuilder.append(entry
                    .replace("%item%", cost.matcher.ingredientDescription(processed))
                    .replace("%quantity%", String.valueOf(cost.quantity))
            );
        }
        return format.replace("%items%", costBuilder.toString());
    }

    private record ItemCost(int quantity, IngredientChoice matcher){}
}
