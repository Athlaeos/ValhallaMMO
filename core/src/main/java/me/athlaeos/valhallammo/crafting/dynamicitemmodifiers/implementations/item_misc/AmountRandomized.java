package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AmountRandomized extends DynamicItemModifier {
    private int lowerBound = 0;
    private int upperBound = 64;

    public AmountRandomized(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (!context.shouldExecuteUsageMechanics()) return;
        if (lowerBound > upperBound) {
            int temp = upperBound;
            upperBound = lowerBound;
            lowerBound = temp;
        }
        int newAmount = Utils.getRandom().nextInt(upperBound + 1) + lowerBound;
        context.getItem().amount(Math.min(Math.max(1, newAmount), context.getItem().get().getType().getMaxStackSize()));
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 11)
            lowerBound = Math.min(Math.min(64, upperBound), Math.max(0, lowerBound + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1))));
        if (button == 13)
            upperBound = Math.min(64, Math.max(0, upperBound + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1))));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(11,
                new ItemBuilder(Material.PAPER)
                        .name("&fWhich amounts? (lower bound)")
                        .lore(String.format("&fAmount randomized between &e %d-%d", lowerBound, upperBound),
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 25")
                        .get()).map(Set.of(
                new Pair<>(13,
                        new ItemBuilder(Material.PAPER)
                                .name("&fWhich amounts? (upper bound)")
                                .lore(String.format("&fAmount randomized between &e %d-%d", lowerBound, upperBound),
                                        "&6Click to add/subtract 1",
                                        "&6Shift-Click to add/subtract 25")
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.STICK).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Amount (RANDOMIZED)";
    }

    @Override
    public String getDescription() {
        return "&fSets the amount of the item to a random number between the given bounds.";
    }

    @Override
    public String getActiveDescription() {
        return "&fSetting the amount to a random value between " + lowerBound + " and " + upperBound;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setLowerBound(int lowerBound) {
        this.lowerBound = lowerBound;
    }

    public void setUpperBound(int upperBound) {
        this.upperBound = upperBound;
    }

    @Override
    public DynamicItemModifier copy() {
        AmountRandomized m = new AmountRandomized(getName());
        m.setLowerBound(this.lowerBound);
        m.setUpperBound(this.upperBound);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 2) return "Two numbers are expected: both integers.";
        try {
            lowerBound = Integer.parseInt(args[0]);
            upperBound = Integer.parseInt(args[1]);
        } catch (NumberFormatException ignored){
            return "Two numbers are expected: both integers. At least one was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<lower_bound>");
        if (currentArg == 1) return List.of("<upper_bound>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 2;
    }
}
