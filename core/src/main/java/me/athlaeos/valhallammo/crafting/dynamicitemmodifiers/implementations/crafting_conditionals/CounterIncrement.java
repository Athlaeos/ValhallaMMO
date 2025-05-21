package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.crafting_conditionals;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CounterIncrement extends DynamicItemModifier {
    private int by = 1;

    public CounterIncrement(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        SmithingItemPropertyManager.setCounter(context.getItem().getMeta(), SmithingItemPropertyManager.getCounter(context.getItem().getMeta()) + by);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12)
            by = Math.max(1, by + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
        }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&eHow much should the counter be increased?")
                        .lore(String.format("&fSet to &e%d", by),
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 10")
                        .get()).map(new HashSet<>());
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.CLOCK).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Increment Counter";
    }

    @Override
    public String getDescription() {
        return "&fIncreases a counter stat on the item";
    }

    @Override
    public String getActiveDescription() {
        return String.format("&fIncrements the counter by %d", by);
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CRAFTING_CONDITIONALS.id());
    }

    public void setBy(int by) {
        this.by = by;
    }

    @Override
    public DynamicItemModifier copy() {
        CounterIncrement m = new CounterIncrement(getName());
        m.setBy(this.by);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: an integer";
        try {
            by = Integer.parseInt(args[0]);
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
