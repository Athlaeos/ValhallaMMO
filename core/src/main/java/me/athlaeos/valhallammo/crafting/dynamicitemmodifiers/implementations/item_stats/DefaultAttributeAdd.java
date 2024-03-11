package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultAttributeAdd extends DynamicItemModifier {
    private final String attribute;
    private double value = 0;
    private boolean hidden = false;
    private boolean add = false;
    private AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_NUMBER;
    private final double smallIncrement;
    private final double bigIncrement;
    private final Material icon;

    public DefaultAttributeAdd(String name, String attribute, double smallIncrement, double bigIncrement, Material icon) {
        super(name);
        this.attribute = attribute;
        this.smallIncrement = smallIncrement;
        this.bigIncrement = bigIncrement;
        this.icon = icon;
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        AttributeWrapper attribute = ItemAttributesRegistry.getCopy(this.attribute);

        double extraValue = 0;
        if (add){
            AttributeWrapper existingAttribute = ItemAttributesRegistry.getStats(outputItem.getMeta(), true).get(this.attribute);
            if (existingAttribute != null && (!existingAttribute.isVanilla() || existingAttribute.getOperation() == operation)){
                extraValue = existingAttribute.getValue();
            }
        }

        attribute.setValue(value + extraValue);
        attribute.setOperation(operation);
        attribute.setHidden(hidden);
        ItemAttributesRegistry.addDefaultStat(outputItem.getMeta(), attribute);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        AttributeWrapper attribute = ItemAttributesRegistry.getCopy(this.attribute);
        if (button == 7) {
            add = !add;
        } else if (button == 11) {
            value = Math.min(attribute.getMax(), Math.max(attribute.getMin(), value + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? bigIncrement : smallIncrement))));
        } else if (button == (attribute.isVanilla() ? 12 : 13)) {
            value = Math.min(attribute.getMax(), Math.max(attribute.getMin(), value + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? bigIncrement * 10 : smallIncrement * 5))));
        } else if (button == 13 && attribute.isVanilla()){
            int currentOperation = Arrays.asList(AttributeModifier.Operation.values()).indexOf(operation);
            if (e.isLeftClick()) {
                if (currentOperation + 1 >= AttributeModifier.Operation.values().length) currentOperation = 0;
                else currentOperation++;
            } else {
                if (currentOperation - 1 < 0) currentOperation = AttributeModifier.Operation.values().length - 1;
                else currentOperation--;
            }
            operation = AttributeModifier.Operation.values()[currentOperation];
        } else if (button == 17) hidden = !hidden;
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        AttributeWrapper attribute = ItemAttributesRegistry.getCopy(this.attribute);
        Set<Pair<Integer, ItemStack>> extraButtons = new HashSet<>();
        extraButtons.add(new Pair<>(7,
                new ItemBuilder(Material.REDSTONE_TORCH)
                        .name("&dShould this stat be added to existing stats?")
                        .lore("&fKeep in mind this also sets the actualized",
                                "&fstat on the item, so any scalings should be",
                                "&fapplied again.",
                                "&e" + (add ? "yes" : "no"),
                                "&6Click to toggle")
                        .get()
        ));
        extraButtons.add(new Pair<>(attribute.isVanilla() ? 12 : 13,
                new ItemBuilder(Material.PAPER)
                        .name("&dHow strong should this base stat be?")
                        .lore("&f" + attribute.getAttribute().toLowerCase().replace("_", " ") + " &e" + attribute.getFormat().format(value),
                                "&6Click to add/subtract " + (5 * smallIncrement),
                                "&6Shift-Click to add/subtract " + (10 * bigIncrement))
                        .get()
        ));
        if (attribute.isVanilla()){
            extraButtons.add(new Pair<>(13,
                    new ItemBuilder(Material.PAPER)
                            .name("&dWhat should the attribute's operation be?")
                            .lore("&f" + StringUtils.toPascalCase(operation.toString().replace("_", " ")),
                                    "&6Click to cycle")
                            .get()
            ));
        }
        extraButtons.add(new Pair<>(17,
                new ItemBuilder(Material.PAPER)
                        .name("&dShould this stat be hidden?")
                        .lore("&f" + hidden,
                                "&fVanilla attributes cannot be",
                                "&fhidden unless the HIDE_ATTRIBUTES",
                                "&fand DISPLAY_ATTRIBUTES flags are",
                                "&fon the item as well",
                                "&6Click to toggle ")
                        .get()
        ));
        return new Pair<>(11,
                new ItemBuilder(Material.PAPER)
                        .name("&dHow strong should this base stat be?")
                        .lore("&f" + attribute.getAttribute().toLowerCase().replace("_", " ") + " &e" + attribute.getFormat().format(value),
                                "&6Click to add/subtract " + smallIncrement,
                                "&6Shift-Click to add/subtract " + bigIncrement)
                        .get()).map(extraButtons);
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(icon).get();
    }

    @Override
    public String getDisplayName() {
        AttributeWrapper attribute = ItemAttributesRegistry.getCopy(this.attribute);
        return (attribute.isVanilla() ? "&bVanilla" : "&dCustom") + " Attribute: " + attribute.getAttribute().toLowerCase().replace("_", " ") + " (ADD)";
    }

    @Override
    public String getDescription() {
        AttributeWrapper attribute = ItemAttributesRegistry.getCopy(this.attribute);
        return "&fAdds " + attribute.getAttribute().toLowerCase().replace("_", " ") + " as default stat to the item. If the item has vanilla stats by default, they are applied first";
    }

    @Override
    public String getActiveDescription() {
        AttributeWrapper attribute = ItemAttributesRegistry.getCopy(this.attribute);
        return "&fAdds " + attribute.getAttribute().toLowerCase().replace("_", " ") + " &e" + attribute.getFormat().format(value) + " as default stat to the item. ";
    }

    @Override
    public Collection<String> getCategories() {
        AttributeWrapper attribute = ItemAttributesRegistry.getCopy(this.attribute);
        return attribute.isVanilla() ? Set.of(ModifierCategoryRegistry.VANILLA_ATTRIBUTES.id()) : Set.of(ModifierCategoryRegistry.CUSTOM_ATTRIBUTES.id());
    }

    public void setOperation(AttributeModifier.Operation operation) {
        this.operation = operation;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public DynamicItemModifier copy() {
        DefaultAttributeAdd m = new DefaultAttributeAdd(getName(), attribute, smallIncrement, bigIncrement, icon);
        m.setOperation(this.operation);
        m.setValue(this.value);
        m.setHidden(this.hidden);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 3) return "Three arguments expected: a number, an operation, and a boolean";
        try {
            value = StringUtils.parseDouble(args[0]);
            operation = AttributeModifier.Operation.valueOf(args[1]);
            hidden = Boolean.parseBoolean(args[2]);
        } catch (NumberFormatException ignored){
            return "Three arguments expected: a number, an operation, and a boolean. Invalid number";
        } catch (IllegalArgumentException ignored){
            return "Three arguments expected: a number, an operation, and a boolean. Invalid operation or booleangiven";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<value>");
        if (currentArg == 1) return Arrays.stream(AttributeModifier.Operation.values()).map(Object::toString).collect(Collectors.toList());
        if (currentArg == 2) return List.of("<hidden>", "true", "false");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 3;
    }
}
