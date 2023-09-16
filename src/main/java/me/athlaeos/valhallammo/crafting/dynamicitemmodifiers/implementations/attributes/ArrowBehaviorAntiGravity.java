package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.attributes;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.skills.skills.implementations.archery.ArrowBehaviorRegistry;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ArrowBehaviorAntiGravity extends DynamicItemModifier {
    public ArrowBehaviorAntiGravity(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        ArrowBehaviorRegistry.addBehavior(outputItem.getMeta(), ArrowBehaviorRegistry.ANTIGRAVITY.getName());
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
        return new ItemBuilder(Material.FEATHER).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Arrow Behavior: Antigravity";
    }

    @Override
    public String getDescription() {
        return "&fArrow is not affected by gravity, and despawns after 10 seconds";
    }

    @Override
    public String getActiveDescription() {
        return "&fArrow is not affected by gravity, and despawns after 10 seconds";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CUSTOM_ATTRIBUTES.id());
    }

    @Override
    public DynamicItemModifier createNew() {
        return new ArrowBehaviorAntiGravity(getName());
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
