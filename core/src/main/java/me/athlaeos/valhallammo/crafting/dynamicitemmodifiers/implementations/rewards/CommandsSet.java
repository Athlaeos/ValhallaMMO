package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CommandsSet extends DynamicItemModifier {
    private List<String> commands = new ArrayList<>();

    public CommandsSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (!context.shouldExecuteUsageMechanics()) return;
        for (int i = 0; i < context.getTimesExecuted(); i++){
            for (String command : commands){
                ValhallaMMO.getInstance().getServer().dispatchCommand(
                        ValhallaMMO.getInstance().getServer().getConsoleSender(),
                        command
                                .replace("%player%", context.getCrafter() == null ? "" : context.getCrafter().getName())
                                .replace("%x%", context.getCrafter() == null ? "" : String.valueOf(context.getCrafter().getLocation().getX()))
                                .replace("%y%", context.getCrafter() == null ? "" : String.valueOf(context.getCrafter().getLocation().getY()))
                                .replace("%z%", context.getCrafter() == null ? "" : String.valueOf(context.getCrafter().getLocation().getZ()))
                );
            }
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            if (e.isShiftClick()) commands.clear();
            else {
                ItemStack cursor = e.getCursor();
                if (!ItemUtils.isEmpty(cursor)) {
                    ItemMeta meta = cursor.getItemMeta();
                    if (meta != null && meta.hasLore() && meta.getLore() != null) commands = new ArrayList<>(meta.getLore());
                    else commands.clear();
                }
            }
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.INK_SAC)
                        .name("&eWhat should the commands be?")
                        .appendLore(commands.isEmpty() ? List.of("&cCommand are clearaed") : commands)
                        .appendLore(
                                "&8&m                                 ",
                                "&6Click with another item",
                                "&6to copy its commands over.",
                                "&fEach line of lore defined a command",
                                "&6Shift-click to reset",
                                "&6the commands back to nothing."
                        )
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.WRITABLE_BOOK).get();
    }

    @Override
    public String getDisplayName() {
        return "&dCommand Execution";
    }

    @Override
    public String getDescription() {
        return "&fCauses the creation of the item to execute a number of commands";
    }

    @Override
    public String getActiveDescription() {
        return "&fThe creation of this item triggers the following commands: " + (commands.isEmpty() ? "&cNone" : "/n&e" + String.join("/n&e", commands));
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public DynamicItemModifier copy() {
        CommandsSet m = new CommandsSet(getName());
        m.setCommands(this.commands);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "You must indicate the commands of the item";
        commands.addAll(Arrays.stream(args[0].split("/n")).map(l -> l.replace("/_", " ")).toList());
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<commands>", "use-/n-for-new-commands", "use-/_-for-spaces", "use-%player%-for-player", "use-%x%-%y%-or-%z%-for-coords");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
