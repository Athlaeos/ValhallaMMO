package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.crafting_conditionals;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SmithingTagsRemoveAll extends DynamicItemModifier {
    public SmithingTagsRemoveAll(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        SmithingItemPropertyManager.setTags(context.getItem().getMeta(), null);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {

    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.PAPER).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Item Tags (CLEAR)";
    }

    @Override
    public String getDescription() {
        return "&fRemoves all item tags from the item.";
    }

    @Override
    public String getActiveDescription() {
        return "&fRemoves all item tags from the item.";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CRAFTING_CONDITIONALS.id());
    }

    @Override
    public DynamicItemModifier copy() {
        SmithingTagsRemoveAll m = new SmithingTagsRemoveAll(getName());
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
