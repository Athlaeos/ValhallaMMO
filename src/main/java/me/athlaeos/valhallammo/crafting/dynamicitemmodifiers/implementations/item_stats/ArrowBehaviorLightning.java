package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.item.arrow_attributes.ArrowBehaviorRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ArrowBehaviorLightning extends DynamicItemModifier {
    private boolean requiresRain = false;

    public ArrowBehaviorLightning(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        ArrowBehaviorRegistry.addBehavior(outputItem.getMeta(), ArrowBehaviorRegistry.LIGHTNING.getName(), requiresRain ? 1 : 0);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) requiresRain = !requiresRain;
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.WATER_BUCKET)
                        .name("&9Requires Rain")
                        .lore(String.format("&fRequires rain for lightning: &e%s", requiresRain ? "Yes" : "No"),
                                "&6Click to toggle on/off")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.ICE).get();
    }

    @Override
    public String getDisplayName() {
        return "&9Arrow Behavior: Lightning";
    }

    @Override
    public String getDescription() {
        return "&fStrikes lightning where the arrow lands";
    }

    @Override
    public String getActiveDescription() {
        return String.format("&fStrikes lightning where the arrow lands %s", requiresRain ? "&bonly if it rains/storms" : "");
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CUSTOM_ATTRIBUTES.id());
    }

    @Override
    public DynamicItemModifier createNew() {
        return new ArrowBehaviorLightning(getName());
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: a yes/no answer";
        requiresRain = args[0].equalsIgnoreCase("yes");
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<requires_rain>", "yes", "no");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
