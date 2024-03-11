package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.food;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.FoodPropertyManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SaturationValueSet extends DynamicItemModifier {
    private float saturation = 4;

    public SaturationValueSet(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        FoodPropertyManager.setSaturationValue(outputItem.getMeta(), saturation);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12)
            saturation = Math.min(20, Math.max(0, saturation + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 5 : 1))));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&eHow much saturation should it replenish?")
                        .lore("&fSaturation set to " + saturation,
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 5")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.GOLDEN_CARROT).get();
    }

    @Override
    public String getDisplayName() {
        return "&eSaturation Value (SET)";
    }

    @Override
    public String getDescription() {
        return "&fSets the item's saturation value.";
    }

    @Override
    public String getActiveDescription() {
        return "&fSets the item's saturation value to &e" + saturation;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.FOOD.id());
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    @Override
    public DynamicItemModifier copy() {
        SaturationValueSet m = new SaturationValueSet(getName());
        m.setSaturation(this.saturation);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: a double";
        try {
            saturation = StringUtils.parseFloat(args[0]);
        } catch (NumberFormatException ignored){
            return "One argument is expected: an integer. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<saturation>");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
