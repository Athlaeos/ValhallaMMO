package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MaxStackSizeSet extends DynamicItemModifier {
    private int maxStackSize = 64;

    public MaxStackSizeSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        ValhallaMMO.getNms().setMaxStackSize(context.getItem().getMeta(), maxStackSize);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            maxStackSize = Math.min(99, Math.max(1, maxStackSize + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 16 : 1))));
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.BOOK)
                        .name("&eWhat should max stack size be?")
                        .lore("&fCurrently &e" + maxStackSize,
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 16")
                        .get()).map(Set.of());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.BUNDLE).get();
    }

    @Override
    public String getDisplayName() {
        return "&aSet Max Stack Size";
    }

    @Override
    public String getDescription() {
        return "&fSets a new max stack size to the item";
    }

    @Override
    public String getActiveDescription() {
        return "&fSets the max stack size of the item to " + maxStackSize;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setMaxStackSize(Integer maxStackSize) {
        this.maxStackSize = maxStackSize;
    }

    @Override
    public DynamicItemModifier copy() {
        MaxStackSizeSet m = new MaxStackSizeSet(getName());
        m.setMaxStackSize(this.maxStackSize);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1)
            return "You must indicate the max stack size of the item";
        try {
            maxStackSize = Integer.parseInt(args[0]);
        } catch (IllegalArgumentException ignored) {
            return "Invalid number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<stack_size>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
