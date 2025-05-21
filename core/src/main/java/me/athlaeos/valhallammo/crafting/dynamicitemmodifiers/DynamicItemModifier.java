package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.event.ItemModificationEvent;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
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

    public abstract void processItem(ModifierContext context);

    public void failedRecipe(ItemBuilder i, String message){
        if (message == null) message = "";
        i.flag(CustomFlag.UNCRAFTABLE).stringTag(ERROR_MESSAGE, message).lore(StringUtils.separateStringIntoLines(message, 40));
    }

    public boolean requiresPlayer(){
        return false;
    }

    /**
     * Modifies a modification context based on the given dynamic item modifiers. <br>
     * If an item is flagged with {@link CustomFlag#UNCRAFTABLE} by one of the modifiers it is assumed
     * the recipe failed for one reason or another. UNCRAFTABLE items cannot be removed from the crafting inventory or be produced at all,
     * in which case lore may be added to the item describing why the recipe failed.
     * @param context the modification context which should include all the details needed to modify its item
     * @param modifiers the modifiers to execute on the context
     */
    public static void modify(ModifierContext context, List<DynamicItemModifier> modifiers){
        ItemModificationEvent event = new ItemModificationEvent(context, modifiers);
        if (Bukkit.isPrimaryThread()) ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.getContext().shouldSort()) sortModifiers(event.getModifiers());
        modifiers:
        for (DynamicItemModifier modifier : event.getModifiers()){
            if (!modifier.meetsRequirement(context)) {
                if (context.getCrafter() != null) Utils.sendMessage(context.getCrafter(), "&cWhatever you just created was improperly configured. Please notify admin. A modifier was used that doesn't function in this context");
                context.getItem().flag(CustomFlag.UNCRAFTABLE);
                return;
            }
            if (!modifier.meetsPlayerRequirement(context)){
                ValhallaMMO.logSevere("&cWhatever was just created was improperly configured. Please check your recipes. A modifier was used that requires a player, but no player exists in this context");
                context.getItem().flag(CustomFlag.UNCRAFTABLE);
                return;
            }
            modifier.processItem(context);
            if (ItemUtils.isEmpty(context.getItem().getItem()) || CustomFlag.hasFlag(context.getItem().getMeta(), CustomFlag.UNCRAFTABLE)) break;
            for (ItemBuilder otherItem : context.getOtherInvolvedItems())
                if (ItemUtils.isEmpty(otherItem.getItem()) || CustomFlag.hasFlag(otherItem.getMeta(), CustomFlag.UNCRAFTABLE)) break modifiers;
        }
        context.getItem().translate();
        for (ItemBuilder otherItem : context.getOtherInvolvedItems()) otherItem.translate();
    }

    public final boolean meetsPlayerRequirement(ModifierContext context){
        return !requiresPlayer() || context.getCrafter() != null;
    }

    public boolean meetsRequirement(ModifierContext context){
        return true;
    }

    /**
     * Sorts the given modifiers based on their priority.
     * @param modifiers the modifiers to sort
     */
    public static void sortModifiers(List<DynamicItemModifier> modifiers){
        modifiers.sort(Comparator.comparingInt((DynamicItemModifier a) -> a.getPriority().getPriorityRating()));
    }

    public static boolean requiresPlayer(Collection<DynamicItemModifier> modifiers) {
        for (DynamicItemModifier modifier : modifiers) {
            if (modifier.requiresPlayer()) return true;
        }
        return false;
    }
}
