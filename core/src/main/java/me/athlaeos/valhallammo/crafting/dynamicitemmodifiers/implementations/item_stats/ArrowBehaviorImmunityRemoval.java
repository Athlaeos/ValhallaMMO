package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.arrow_attributes.ArrowBehaviorRegistry;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ArrowBehaviorImmunityRemoval extends DynamicItemModifier {
    public ArrowBehaviorImmunityRemoval(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        ArrowBehaviorRegistry.addBehavior(context.getItem().getMeta(), ArrowBehaviorRegistry.IMMUNITY_REMOVAL.getName());
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
        return new ItemBuilder(Material.WITHER_SKELETON_SKULL).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Arrow Behavior: Immunity Removal";
    }

    @Override
    public String getDescription() {
        return "&fArrow immediately removes a hit entity's immunity frames after being hit";
    }

    @Override
    public String getActiveDescription() {
        return "&fArrow immediately removes a hit entity's immunity frames after being hit";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CUSTOM_ATTRIBUTES.id());
    }

    @Override
    public DynamicItemModifier copy() {
        ArrowBehaviorImmunityRemoval m = new ArrowBehaviorImmunityRemoval(getName());
        m.setPriority(this.getPriority());
        return m;
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
