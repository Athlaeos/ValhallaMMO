package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.ValhallaMMO;
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

public class FireResistant extends DynamicItemModifier {
    private Boolean fireResistant = true;

    public FireResistant(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        ValhallaMMO.getNms().setFireResistant(context.getItem().getMeta(), fireResistant);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            if (e.isShiftClick()) fireResistant = null;
            else fireResistant = fireResistant == null || !fireResistant;
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.BOOK)
                        .name("&eShould the item be fire resistant?")
                        .lore("&fSet to &e" + (fireResistant == null ? "reset" : (fireResistant ? "yes" : "no")),
                                "&fIf yes, the item cannot be destroyed",
                                "&fby lava or fire.",
                                "&fIf no, the item will be destructible",
                                "&fby lava or fire.",
                                "&fIf reset, it will behave as normal.",
                                "&6Click to toggle",
                                "&6Shift-Click to reset")
                        .get()).map(Set.of());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.LAVA_BUCKET).get();
    }

    @Override
    public String getDisplayName() {
        return "&aSet Fire Resistant";
    }

    @Override
    public String getDescription() {
        return "&fMakes the item immune/vulnerable to lava/fire";
    }

    @Override
    public String getActiveDescription() {
        return "&fMakes the item " + (fireResistant == null ? "normally fire resistant." : (fireResistant ? "immune to lava or fire" : "vulnerable to lava or fire"));
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setFireResistant(Boolean fireResistant) {
        this.fireResistant = fireResistant;
    }

    @Override
    public DynamicItemModifier copy() {
        FireResistant m = new FireResistant(getName());
        m.setFireResistant(this.fireResistant);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1)
            return "You must indicate if the item should be immune to lava/fire damage or not";
        try {
            fireResistant = args[0].equalsIgnoreCase("reset") ? null : Boolean.parseBoolean(args[0]);
        } catch (IllegalArgumentException ignored) {
            return "Invalid option";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<fire_resistant>", "true", "false", "reset");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
