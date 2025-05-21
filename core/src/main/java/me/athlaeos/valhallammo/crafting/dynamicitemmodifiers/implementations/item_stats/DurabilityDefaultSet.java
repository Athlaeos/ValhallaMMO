package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.CustomDurabilityManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DurabilityDefaultSet extends DynamicItemModifier {
    private int value = 500;

    public DurabilityDefaultSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        double fraction = (double) CustomDurabilityManager.getDurability(context.getItem().getMeta(), false) / CustomDurabilityManager.getDurability(context.getItem().getMeta(), true);
        CustomDurabilityManager.setDurability(context.getItem().getMeta(), (int) (value * fraction), value);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) value = Math.max(1, value + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 25 : 1)));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&dHow much max durability should it have?")
                        .lore("&f" + value,
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 25")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.DIAMOND).get();
    }

    @Override
    public String getDisplayName() {
        return "&dCustom Max Durability";
    }

    @Override
    public String getDescription() {
        return "&fSets a new custom max durability to the item";
    }

    @Override
    public String getActiveDescription() {
        return "&fSets a new custom max durability of " + value + " to the item";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public DynamicItemModifier copy() {
        DurabilityDefaultSet m = new DurabilityDefaultSet(getName());
        m.setValue(this.value);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument expected: a number";
        try {
            value = Integer.parseInt(args[0]);
        } catch (IllegalArgumentException ignored){
            return "One argument expected: a number. Invalid number given";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<durability>");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
