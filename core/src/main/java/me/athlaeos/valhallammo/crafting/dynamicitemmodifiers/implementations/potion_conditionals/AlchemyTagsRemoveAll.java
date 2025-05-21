package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_conditionals;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.item.AlchemyItemPropertyManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AlchemyTagsRemoveAll extends DynamicItemModifier {
    public AlchemyTagsRemoveAll(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        AlchemyItemPropertyManager.setTags(context.getItem().getMeta(), new HashSet<>());
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
        return "&7Potion Tags (CLEAR)";
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
        return Set.of(ModifierCategoryRegistry.POTION_CONDITIONALS.id());
    }

    @Override
    public DynamicItemModifier copy() {
        AlchemyTagsRemoveAll m = new AlchemyTagsRemoveAll(getName());
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
