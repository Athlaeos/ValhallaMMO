package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.SweepStatus;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PreventSwordSweeping extends DynamicItemModifier {

    public PreventSwordSweeping(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        SweepStatus.setSweepable(context.getItem().getMeta(), false);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) { }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.DIAMOND_SWORD).get();
    }

    @Override
    public String getDisplayName() {
        return "&6Remove Sweeping";
    }

    @Override
    public String getDescription() {
        return "&fPrevents swords from being able to sweep attack (only works on swords)";
    }

    @Override
    public String getActiveDescription() {
        return "&fPrevents swords from being able to sweep attack (only works on swords)";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    @Override
    public DynamicItemModifier copy() {
        PreventSwordSweeping m = new PreventSwordSweeping(getName());
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
