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

public class HideToolTip extends DynamicItemModifier {
    private boolean hideToolTip = true;

    public HideToolTip(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        ValhallaMMO.getNms().setHideTooltip(context.getItem().getMeta(), hideToolTip);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) hideToolTip = !hideToolTip;
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.BOOK)
                        .name("&eShould the item not have a tooltip?")
                        .lore("7fSet to &e" + (hideToolTip ? "yes" : "no"),
                                "&fThe tooltip is the textbox you get",
                                "&fwhen hovering over an item in an",
                                "&finventory. Without tooltip, you can't",
                                "&fsee stats, its name, lore, etc.",
                                "&fIf yes, the item will not have a",
                                "&ftooltip at all.",
                                "&fIf no, the item will show its tooltip",
                                "&flike normal.",
                                "&6Click to toggle")
                        .get()).map(Set.of());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.WRITABLE_BOOK).get();
    }

    @Override
    public String getDisplayName() {
        return "&aHide Tooltip";
    }

    @Override
    public String getDescription() {
        return "&fRemoves/adds an item's tooltip";
    }

    @Override
    public String getActiveDescription() {
        return hideToolTip ? "&fHides an item's tooltip" : "&fRe-adds an item's tooltip";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setHideToolTip(Boolean hideToolTip) {
        this.hideToolTip = hideToolTip;
    }

    @Override
    public DynamicItemModifier copy() {
        HideToolTip m = new HideToolTip(getName());
        m.setHideToolTip(this.hideToolTip);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1)
            return "You must indicate if the item should hide its tooltip or not";
        try {
            hideToolTip = Boolean.parseBoolean(args[0]);
        } catch (IllegalArgumentException ignored) {
            return "Invalid option";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<hide_tooltip>", "true", "false");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
