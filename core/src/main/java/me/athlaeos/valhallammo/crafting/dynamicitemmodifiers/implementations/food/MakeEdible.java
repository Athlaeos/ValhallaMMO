package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.food;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MakeEdible extends DynamicItemModifier {
    private boolean edible = true;
    private boolean canAlwaysEat = false;
    private float eatTimeSeconds = 3F;

    public MakeEdible(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        ValhallaMMO.getNms().setEdible(context.getItem(), edible, canAlwaysEat, eatTimeSeconds);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 7) edible = !edible;
        if (button == 11) canAlwaysEat = !canAlwaysEat;
        if (button == 13) eatTimeSeconds = Math.max(0, eatTimeSeconds + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1F : 0.1F)));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(7,
                new ItemBuilder(Material.REDSTONE_TORCH)
                        .name("&eShould this item become edible?")
                        .lore("&fSet to &e" + (edible ? "yes" : "no"),
                                "&6Click to toggle")
                        .get()).map(edible ? Set.of(
                new Pair<>(11,
                        new ItemBuilder(Material.GOLDEN_APPLE)
                                .name("&eShould it always be edible?")
                                .lore("&fSet to &e" + (canAlwaysEat ? "yes" : "no"),
                                        "&fDetermines if the item can always",
                                        "&fbe eaten, even if the player has",
                                        "&ffull hunger points.",
                                        "&6Click to toggle")
                                .get()),
                new Pair<>(13,
                        new ItemBuilder(Material.CLOCK)
                                .name("&eHow long should it take to eat?")
                                .lore("&fSet to &e" + String.format("%.1fs", eatTimeSeconds),
                                        "&fDetermines how long it takes",
                                        "&fto eat this item.",
                                        "&fNormal items typically take",
                                        "&fabout 3 seconds to eat.",
                                        "&6Click to add/subtract 0.1 seconds",
                                        "&6Shift-Click to add/subtract 1 second")
                                .get())
        ) : new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.COOKED_BEEF).get();
    }

    @Override
    public String getDisplayName() {
        return "&dFoodify";
    }

    @Override
    public String getDescription() {
        return "&fMakes it so the item becomes edible! Allows editing consumption time as well";
    }

    @Override
    public String getActiveDescription() {
        return "&fMakes the item " + (edible ? "edible! " + (canAlwaysEat ? "Can always be eaten and t" : "T") + "akes " + eatTimeSeconds + " to eat" : "inedible!");
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.FOOD.id());
    }

    @Override
    public DynamicItemModifier copy() {
        MakeEdible m = new MakeEdible(getName());
        m.setPriority(this.getPriority());
        m.setEdible(this.isEdible());
        m.setCanAlwaysEat(this.canAlwaysEat());
        m.setEatTimeSeconds(this.getEatTimeSeconds());
        return m;
    }

    public void setCanAlwaysEat(boolean canAlwaysEat) {
        this.canAlwaysEat = canAlwaysEat;
    }

    public void setEatTimeSeconds(float eatTimeSeconds) {
        this.eatTimeSeconds = eatTimeSeconds;
    }

    public void setEdible(boolean edible) {
        this.edible = edible;
    }

    public boolean isEdible() {
        return edible;
    }

    public boolean canAlwaysEat() {
        return canAlwaysEat;
    }

    public float getEatTimeSeconds() {
        return eatTimeSeconds;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 3) return "Three arguments are expected: two booleans and a number";
        try {
            edible = Boolean.parseBoolean(args[0]);
            canAlwaysEat = Boolean.parseBoolean(args[1]);
            eatTimeSeconds = Float.parseFloat(args[2]);
        } catch (NumberFormatException ignored){
            return "Three arguments are expected: two booleans and a number. At least one was improperly given";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<edible>", "true", "false");
        if (currentArg == 1) return List.of("<can_always_eat>", "true", "false");
        if (currentArg == 2) return List.of("<eat_time_seconds>", "3", "2", "1");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 3;
    }
}
