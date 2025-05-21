package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Template extends DynamicItemModifier {
    public Template(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {

    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            double placeholder = Math.max(0, 0 + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01)));
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&fName of button")
                        .lore("&eDescription of button")
                        .get()).map(
                                Set.of(
                                        new Pair<>(12,
                                                new ItemBuilder(Material.PAPER)
                                                        .name("&fName of button")
                                                        .lore("&eDescription of button")
                                                        .get()
                                        ),
                                        new Pair<>(12,
                                                new ItemBuilder(Material.PAPER)
                                                        .name("&fName of button")
                                                        .lore("&eDescription of button")
                                                        .get()
                                        )
                                )
        );
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.WRITTEN_BOOK).get();
    }

    @Override
    public String getDisplayName() {
        return "&bDisplayname";
    }

    @Override
    public String getDescription() {
        return "&fAdds a player signature to the item's lore.";
    }

    @Override
    public String getActiveDescription() {
        return "&factive description";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    @Override
    public DynamicItemModifier copy() {
        Template m = new Template(getName());
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
