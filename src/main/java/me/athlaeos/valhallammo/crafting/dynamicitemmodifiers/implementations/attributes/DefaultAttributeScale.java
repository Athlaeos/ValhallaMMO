package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.attributes;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemAttributesRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.item.attributes.AttributeWrapper;
import me.athlaeos.valhallammo.skills.skills.implementations.smithing.SmithingItemPropertyManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultAttributeScale extends DynamicItemModifier {
    private double skillEfficiency = 0.5;
    private double minimumValue = 0;
    private final String attribute;
    private final Material icon;

    public DefaultAttributeScale(String name, String attribute, Material icon) {
        super(name);
        this.icon = icon;
        this.attribute = attribute;
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        int quality = SmithingItemPropertyManager.getQuality(outputItem.getMeta());
        int finalQuality = (int) Math.round(skillEfficiency * quality);
        SmithingItemPropertyManager.applyAttributeScaling(outputItem.getItem(), outputItem.getMeta(), finalQuality, attribute, minimumValue);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 11) skillEfficiency = Math.max(0, skillEfficiency + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01)));
        else if (button == 13) minimumValue = Math.max(0, minimumValue + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01)));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(11,
                new ItemBuilder(Material.NETHER_STAR)
                        .name("&fSkill Efficiency")
                        .lore(String.format("&fSkill efficiency: %.0f%%", skillEfficiency * 100),
                                String.format("&fMinimum fraction of stat: &e%.2fx", minimumValue),
                                "&fSkill efficiency determines how much",
                                "&fof the player's skill is used in the",
                                "&fformula.",
                                "&6Click to add/subtract 1%",
                                "&6Shift-Click to add/subtract 10%")
                        .get()).map(
                                Set.of(
                                        new Pair<>(13,
                                                new ItemBuilder(Material.PAPER)
                                                        .name("&fMinimum fraction")
                                                        .lore(String.format("&fSkill efficiency: %.0f%%", skillEfficiency * 100),
                                                                String.format("&fMinimum fraction of stat: &e%.2fx", minimumValue),
                                                                "&fThe resulting stat will always be at",
                                                                "&fleast the given fraction of its default",
                                                                "&fvalue, rounded up",
                                                                "&6Click to add/subtract 0.01",
                                                                "&6Shift-Click to add/subtract 0.1")
                                                        .get()
                                        )
                                )
        );
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(icon).get();
    }

    @Override
    public String getDisplayName() {
        AttributeWrapper attribute = ItemAttributesRegistry.getCopy(this.attribute);
        return (attribute.isVanilla() ? "&b" : "&d") + "Scale Attribute: " + attribute.getAttribute().toLowerCase().replace("_", " ");
    }

    @Override
    public String getDescription() {
        return "&fChanges the item's amount based on the player's smithing skill";
    }

    @Override
    public String getActiveDescription() {
        return String.format("&fChanges the item's amount based on &e%.0f%%&f the player's smithing skill, to at least &e%.2fx&f the stat's default amount", skillEfficiency * 100, minimumValue);
    }

    @Override
    public Collection<String> getCategories() {
        AttributeWrapper attribute = ItemAttributesRegistry.getCopy(this.attribute);
        return attribute.isVanilla() ? Set.of(ModifierCategoryRegistry.VANILLA_ATTRIBUTES.id()) : Set.of(ModifierCategoryRegistry.CUSTOM_ATTRIBUTES.id());
    }

    @Override
    public DynamicItemModifier createNew() {
        AttributeWrapper attribute = ItemAttributesRegistry.getCopy(this.attribute);
        return new DefaultAttributeScale(getName(), attribute.getAttribute(), icon);
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 2) return "Two arguments are expected: both doubles";
        try {
            skillEfficiency = Double.parseDouble(args[0]);
            minimumValue = Double.parseDouble(args[1]);
        } catch (NumberFormatException ignored){
            return "Two arguments are expected: both doubles. At least one was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<skill_efficiency>");
        if (currentArg == 1) return List.of("<minimum_fraction>");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 2;
    }
}
