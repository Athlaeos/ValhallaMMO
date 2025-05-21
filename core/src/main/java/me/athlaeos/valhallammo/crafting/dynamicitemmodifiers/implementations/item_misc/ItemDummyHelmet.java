package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.entities.Dummy;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemDummyHelmet extends DynamicItemModifier {

    public ItemDummyHelmet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        Dummy.setDummyItem(context.getItem().getMeta(), true);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) { }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.HAY_BLOCK).get();
    }

    @Override
    public String getDisplayName() {
        return "&6Dummy Head";
    }

    @Override
    public String getDescription() {
        return "&fMarks the item as a dummy head. Can be placed on armor stands to turn them into immortal dummies that show damage indicators (if Decent Holograms is installed). Dummies can be un-made by simply removing this head item";
    }

    @Override
    public String getActiveDescription() {
        return "&fMarks the item as a dummy head. Can be placed on armor stands to turn them into immortal dummies that show damage indicators (if Decent Holograms is installed). Dummies can be un-made by simply removing this head item";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    @Override
    public DynamicItemModifier copy() {
        ItemDummyHelmet m = new ItemDummyHelmet(getName());
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 0;
    }
}
