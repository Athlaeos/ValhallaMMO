package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;

public abstract class DynamicItemModifier {
    private final String name;
    private final ModifierPriority priority = ModifierPriority.NEUTRAL;
    public DynamicItemModifier(String name){
        this.name = name;
    }


    public abstract ItemStack getCategoryIcon();
    public abstract ItemStack getModifierIcon();
    public abstract String getDisplayName();
    public abstract String getDescription();
    public abstract String getErrorMessage();
    public abstract String getExtraRequirementMessage();

    public abstract ItemStack[] getButtons();
    public abstract void buttonPress(InventoryClickEvent e, int button);

    public ModifierPriority getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public ItemStack processItem(Player crafter, ItemStack outputItem, boolean use, boolean validate){
        return processItem(crafter, outputItem, use, validate, 1);
    }
    public abstract ItemStack processItem(Player crafter, ItemStack outputItem, boolean use, boolean validate, int timesExecuted);

    public static ItemStack modify(ItemStack i, Player p, List<DynamicItemModifier> modifiers, boolean sort, boolean use, boolean validate, int count){
        ItemStack item = i.clone();
        if (sort) sortModifiers(modifiers);
        for (DynamicItemModifier modifier : modifiers){
            if (modifier instanceof RelationalItemModifier) continue;
            item = modifier.processItem(p, item, use, validate, count);
            if (ItemUtils.isEmpty(item)) {
                if (use || validate) Utils.sendMessage(p, modifier.getErrorMessage());
                break;
            }
        }

        return item;
    }

    public static ItemStack modify(ItemStack i, Player p, List<DynamicItemModifier> modifiers, boolean sort, boolean use, boolean validate){
        return modify(i, p, modifiers, sort, use, validate, 1);
    }

    public static RelationalItemModifier.RelationalResult modify(ItemStack i1, ItemStack i2, Player p, List<DynamicItemModifier> modifiers, boolean sort, boolean use, boolean validate, int count){
        RelationalItemModifier.RelationalResult result = new RelationalItemModifier.RelationalResult(i1.clone(), i2.clone());
        if (sort) sortModifiers(modifiers);
        for (DynamicItemModifier modifier : modifiers){
            if (modifier instanceof RelationalItemModifier relationalItemModifier)
                result = relationalItemModifier.processItem(p, result.i1(), result.i2(), use, validate, count);
            else {
                ItemStack item1 = result.i1();
                ItemStack item2 = result.i2();
                item1 = modifier.processItem(p, item1, use, validate, count);
                result = new RelationalItemModifier.RelationalResult(item1, item2);
                if (ItemUtils.isEmpty(result.i1()) || ItemUtils.isEmpty(result.i2())) {
                    if (use || validate) Utils.sendMessage(p, modifier.getErrorMessage());
                    break;
                }
            }
        }

        return result;
    }

    public static RelationalItemModifier.RelationalResult modify(ItemStack i1, ItemStack i2, Player p, List<DynamicItemModifier> modifiers, boolean sort, boolean use, boolean validate){
        return modify(i1, i2, p, modifiers, sort, use, validate, 1);
    }

    public static void sortModifiers(List<DynamicItemModifier> modifiers){
        modifiers.sort(Comparator.comparingInt((DynamicItemModifier a) -> a.getPriority().getPriorityRating()));
    }
}
