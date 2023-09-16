package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.CustomDurabilityManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.*;

public class AmountRandomized extends DynamicItemModifier {
    private int lowerBound = 0;
    private int upperBound = 64;

    public AmountRandomized(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        if (!use) return;
        if (lowerBound > upperBound) {
            int temp = upperBound;
            upperBound = lowerBound;
            lowerBound = temp;
        }
        int newAmount = Utils.getRandom().nextInt(upperBound + 1) + lowerBound;
        outputItem.amount(Math.min(Math.max(1, newAmount), outputItem.get().getType().getMaxStackSize()));
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

    @Override
    public DynamicItemModifier createNew() {
        return new AmountRandomized(getName());
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
