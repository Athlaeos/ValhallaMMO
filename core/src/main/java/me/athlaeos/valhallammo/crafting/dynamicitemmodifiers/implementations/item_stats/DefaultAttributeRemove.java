package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DefaultAttributeRemove extends DynamicItemModifier {
    private final String attribute;
    private final Material icon;

    public DefaultAttributeRemove(String name, String attribute, Material icon) {
        super(name);
        this.attribute = attribute;
        this.icon = icon;
    }

    @Override
    public void processItem(ModifierContext context) {
        AttributeWrapper attribute = ItemAttributesRegistry.getCopy(this.attribute);
        ItemAttributesRegistry.removeStat(context.getItem().getMeta(), attribute);
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
        return new ItemBuilder(icon).get();
    }

    @Override
    public String getDisplayName() {
        AttributeWrapper attribute = ItemAttributesRegistry.getCopy(this.attribute);
        return (attribute.isVanilla() ? "&bVanilla" : "&dCustom") + " Attribute: " + attribute.getAttribute().toLowerCase(java.util.Locale.US).replace("_", " ") + " (REMOVE)";
    }

    @Override
    public String getDescription() {
        AttributeWrapper attribute = ItemAttributesRegistry.getCopy(this.attribute);
        return "&fRemoves " + attribute.getAttribute().toLowerCase(java.util.Locale.US).replace("_", " ") + " from the item";
    }

    @Override
    public String getActiveDescription() {
        AttributeWrapper attribute = ItemAttributesRegistry.getCopy(this.attribute);
        return "&fRemoves " + attribute.getAttribute().toLowerCase(java.util.Locale.US).replace("_", " ") + " from the item";
    }

    @Override
    public Collection<String> getCategories() {
        AttributeWrapper attribute = ItemAttributesRegistry.getCopy(this.attribute);
        return attribute.isVanilla() ? Set.of(ModifierCategoryRegistry.VANILLA_ATTRIBUTES.id()) : Set.of(ModifierCategoryRegistry.CUSTOM_ATTRIBUTES.id());
    }

    @Override
    public DynamicItemModifier copy() {
        DefaultAttributeRemove m = new DefaultAttributeRemove(getName(), attribute, icon);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 0;
    }
}
