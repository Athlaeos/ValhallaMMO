package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public abstract class DynamicItemModifier {
    public static final NamespacedKey ERROR_MESSAGE = new NamespacedKey(ValhallaMMO.getInstance(), "contained_error_message");

    private final String name;
    private ModifierPriority priority = ModifierPriority.NEUTRAL;
    public DynamicItemModifier(String name){
        this.name = name;
    }

    public abstract ItemStack getModifierIcon();
    public abstract String getDisplayName();
    public abstract String getDescription();
    public abstract String getActiveDescription();
    public abstract Collection<String> getCategories();
    public abstract DynamicItemModifier copy();

    /**
     * Should return an error message if incorrectly parsed, null if execution is fine
     */
    public abstract String parseCommand(CommandSender executor, String[] args);

    /**
     * Should return a list of command suggestions given the current arg
     */
    public abstract List<String> commandSuggestions(CommandSender executor, int currentArg);

    /**
     * Should return the amount of args required to execute properly, which should coincide with commandSuggestions and parseCommand
     */
    public abstract int commandArgsRequired();

    /**
     * Up to 25 buttons are allowed to be registered. These buttons should configure the modifier using onButtonPress.<br>
     * The key should represent the position of the button in a 5x5 grid
     * @return a map with buttons
     */
    public abstract Map<Integer, ItemStack> getButtons();
    public abstract void onButtonPress(InventoryClickEvent e, int button);

    public ModifierPriority getPriority() {
        return priority;
    }

    public void setPriority(ModifierPriority priority) {
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public void processItem(Player crafter, ItemBuilder i, boolean use, boolean validate){
        processItem(crafter, i, use, validate, 1);
    }
    public abstract void processItem(Player crafter, ItemBuilder i, boolean use, boolean validate, int timesExecuted);

    public void failedRecipe(ItemBuilder i, String message){
        i.flag(CustomFlag.UNCRAFTABLE).stringTag(ERROR_MESSAGE, message).lore(StringUtils.separateStringIntoLines(message, 40));
    }

    public boolean requiresPlayer(){
        return false;
    }

    /**
     * Modifies an item based on the given dynamic item modifiers. <br>
     * If an item is flagged with {@link CustomFlag#UNCRAFTABLE} by one of the modifiers it is assumed
     * the recipe failed for one reason or another. UNCRAFTABLE items cannot be removed from the crafting inventory or be produced at all,
     * in which case lore may be added to the item describing why the recipe failed.
     * @param i the item to modify
     * @param p the player to use in modification (can be null if no player-required modifiers are used)
     * @param modifiers the modifiers to execute on the item
     * @param sort if the modifiers need to be sorted based on priority first (might help performance if modifiers are sorted prior)
     * @param use communicates to the modifier if the recipe is being crafted rather than pre-crafted. things like exp shouldn't be given if the recipe is only pre-crafted
     * @param validate if crafting validation should happen. for example, if a result of a recipe is being pre-generated,
     *                 you typically don't want the recipe to be validated based on conditions otherwise the result will look off
     * @param count how many times the recipe is being crafted. mainly applicable for the crafting grid recipes where the player can craft several items instantly
     */
    public static void modify(ItemBuilder i, Player p, List<DynamicItemModifier> modifiers, boolean sort, boolean use, boolean validate, int count){
        if (sort) sortModifiers(modifiers);
        for (DynamicItemModifier modifier : modifiers){
            if (modifier instanceof RelationalItemModifier) continue;
            modifier.processItem(p, i, use, validate, count);
            if (ItemUtils.isEmpty(i.getItem()) || CustomFlag.hasFlag(i.getMeta(), CustomFlag.UNCRAFTABLE)) break;
        }
        TranslationManager.translateItemMeta(i.getMeta());
    }

    /**
     * Does what {@link DynamicItemModifier#modify(ItemBuilder, Player, List, boolean, boolean, boolean, int)} does, except with a
     * <b>count</b> of 1.
     */
    public static void modify(ItemBuilder i, Player p, List<DynamicItemModifier> modifiers, boolean sort, boolean use, boolean validate){
        modify(i, p, modifiers, sort, use, validate, 1);
    }

    /**
     * Modifiers <b>two</b> items based on the given modifiers. Because two items are considered, {@link RelationalItemModifier}s may be used to alter items
     * based on the player's stats AND another item.
     * @param i1 the first item to modify
     * @param i2 the second item to modify
     * @param p the player to use in modification (can be null if no player-required modifiers are used)
     * @param modifiers the modifiers to execute on the item
     * @param sort if the modifiers need to be sorted based on priority first (might help performance if modifiers are sorted prior)
     * @param use communicates to the modifier if the recipe is being crafted rather than pre-crafted. things like exp shouldn't be given if the recipe is only pre-crafted
     * @param validate if crafting validation should happen. for example, if a result of a recipe is being pre-generated,
     *                 you typically don't want the recipe to be validated based on conditions otherwise the result will look off
     * @param count how many times the recipe is being crafted. mainly applicable for the crafting grid recipes where the player can craft several items instantly
     */
    public static void modify(ItemBuilder i1, ItemBuilder i2, Player p, List<DynamicItemModifier> modifiers, boolean sort, boolean use, boolean validate, int count){
        RelationalItemModifier.RelationalResult result = new RelationalItemModifier.RelationalResult(i1, i2);
        if (sort) sortModifiers(modifiers);
        for (DynamicItemModifier modifier : modifiers){
            if (modifier instanceof RelationalItemModifier relationalItemModifier)
                relationalItemModifier.processItem(p, result.i1(), result.i2(), use, validate, count);
            else {
                modifier.processItem(p, i1, use, validate, count);
                result = new RelationalItemModifier.RelationalResult(i1, i2);
                if (ItemUtils.isEmpty(result.i1().getItem()) || CustomFlag.hasFlag(result.i1().getMeta(), CustomFlag.UNCRAFTABLE) ||
                        ItemUtils.isEmpty(result.i2().getItem()) || CustomFlag.hasFlag(result.i2().getMeta(), CustomFlag.UNCRAFTABLE)) break;
            }
            if (ItemUtils.isEmpty(i1.getItem()) || CustomFlag.hasFlag(i1.getMeta(), CustomFlag.UNCRAFTABLE) ||
                    ItemUtils.isEmpty(i2.getItem()) || CustomFlag.hasFlag(i2.getMeta(), CustomFlag.UNCRAFTABLE)) break;
        }
        TranslationManager.translateItemMeta(i1.getMeta());
        TranslationManager.translateItemMeta(i2.getMeta());
    }

    /**
     * Does what {@link DynamicItemModifier#modify(ItemBuilder, ItemBuilder, Player, List, boolean, boolean, boolean, int)} does, except with a
     * <b>count</b> of 1.
     */
    public static void modify(ItemBuilder i1, ItemBuilder i2, Player p, List<DynamicItemModifier> modifiers, boolean sort, boolean use, boolean validate){
        modify(i1, i2, p, modifiers, sort, use, validate, 1);
    }

    /**
     * Sorts the given modifiers based on their priority.
     * @param modifiers the modifiers to sort
     */
    public static void sortModifiers(List<DynamicItemModifier> modifiers){
        modifiers.sort(Comparator.comparingInt((DynamicItemModifier a) -> a.getPriority().getPriorityRating()));
    }
}
