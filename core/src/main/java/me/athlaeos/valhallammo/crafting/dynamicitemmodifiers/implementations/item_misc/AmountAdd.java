package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AmountAdd extends DynamicItemModifier {
    private int amount = 1;

    public AmountAdd(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        context.getItem().amount(Math.min(Math.max(1, context.getItem().getItem().getAmount() + amount), 64));
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12)
            amount = Math.min(64, Math.max(1, amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1))));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&fWhich amount?")
                        .lore("&f" + amount + " added to amount",
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 10")
                        .get()).map(Set.of(
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.STICK).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Amount (ADD)";
    }

    @Override
    public String getDescription() {
        return "&fIncreases the quantity of an item by the given amount.";
    }

    @Override
    public String getActiveDescription() {
        return "&fIncreases the quantity of the item by " + amount;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public DynamicItemModifier copy() {
        AmountAdd m = new AmountAdd(getName());
        m.setAmount(this.amount);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One number is expected: an integer";
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored){
            return "One number is expected: an integer. It was not a number";
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
