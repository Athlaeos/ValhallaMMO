package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.food;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.FoodPropertyManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class FoodValueSet extends DynamicItemModifier {
    private int food = 4;

    public FoodValueSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        FoodPropertyManager.setFoodValue(context.getItem().getMeta(), food);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12)
            food = Math.min(20, Math.max(0, food + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 5 : 1))));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&eHow much food should it replenish?")
                        .lore("&fFood set to " + food,
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 5")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.COOKED_BEEF).get();
    }

    @Override
    public String getDisplayName() {
        return "&eFood Value (SET)";
    }

    @Override
    public String getDescription() {
        return "&fSets the item's food value.";
    }

    @Override
    public String getActiveDescription() {
        return "&fSets the item's food value to &e" + food;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.FOOD.id());
    }

    public void setFood(int food) {
        this.food = food;
    }

    @Override
    public DynamicItemModifier copy() {
        FoodValueSet m = new FoodValueSet(getName());
        m.setFood(this.food);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: an integer";
        try {
            food = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored){
            return "One argument is expected: an integer. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<food>");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
