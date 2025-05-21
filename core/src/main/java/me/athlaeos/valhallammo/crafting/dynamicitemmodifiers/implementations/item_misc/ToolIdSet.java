package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.ToolRequirementType;
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

public class ToolIdSet extends DynamicItemModifier {
    private int id = 1;

    public ToolIdSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        ToolRequirementType.setItemsToolID(context.getItem().getMeta(), id);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12)
            id = Math.min(64, Math.max(1, id + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1))));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&fWhich tool ID?")
                        .lore("&fTool ID set to &e" + id,
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 10")
                        .get()).map(Set.of(
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.IRON_HOE).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Tool ID (SET)";
    }

    @Override
    public String getDescription() {
        return "&fSets the tool ID of the item to the given number.";
    }

    @Override
    public String getActiveDescription() {
        return "&fSetting the tool ID to &e" + id;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public DynamicItemModifier copy() {
        ToolIdSet m = new ToolIdSet(getName());
        m.setId(this.id);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One number is expected: an integer";
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored){
            return "One number is expected: an integer. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<id>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
