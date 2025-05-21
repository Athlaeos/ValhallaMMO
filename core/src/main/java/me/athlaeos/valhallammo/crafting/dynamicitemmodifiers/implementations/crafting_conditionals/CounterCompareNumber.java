package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.crafting_conditionals;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import me.athlaeos.valhallammo.localization.TranslationManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CounterCompareNumber extends DynamicItemModifier {
    private int amount = 1;

    public CounterCompareNumber(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (!context.shouldValidate()) return;
        if (SmithingItemPropertyManager.getCounter(context.getItem().getMeta()) > amount){
            failedRecipe(context.getItem(), TranslationManager.getTranslation("modifier_warning_counter_exceeded"));
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12)
            amount = Math.max(0, amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
        }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&eAfter which number should recipe cancel?")
                        .lore(String.format("&fSet to &e%d", amount),
                                "&fRecipe cancels if counter > " + amount,
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 10")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.CLOCK).get();
    }

    @Override
    public String getDisplayName() {
        return "&cCancel if counter exceeded (NUMBER)";
    }

    @Override
    public String getDescription() {
        return "&fIf the item's counter exceeds the given value, recipe is cancelled";
    }

    @Override
    public String getActiveDescription() {
        return String.format("&fIf the item's counter exceeds %d, recipe is cancelled", amount);
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CRAFTING_CONDITIONALS.id());
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public DynamicItemModifier copy() {
        CounterCompareNumber m = new CounterCompareNumber(getName());
        m.setAmount(this.amount);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: an integer";
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored){
            return "One argument is expected: an integer. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<amount>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
