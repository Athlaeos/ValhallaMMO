package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.CustomDurabilityManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.*;

public class DurabilityRepairNumeric extends DynamicItemModifier {
    private int repair = 100;

    public DurabilityRepairNumeric(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (!(context.getItem().getMeta() instanceof Damageable) || context.getItem().getItem().getType().getMaxDurability() <= 0) return;
        CustomDurabilityManager.damage(context.getItem().getMeta(), -repair);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12)
            repair = Math.max(0, repair + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 25 : 1)));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.ANVIL)
                        .name("&fHow much should the item be repaired by?")
                        .lore(String.format("&fRepair: &e%d &fdurability", repair),
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 25")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.ANVIL).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Repair (NUMERIC)";
    }

    @Override
    public String getDescription() {
        return "&fRepairs the item by a given amount.";
    }

    @Override
    public String getActiveDescription() {
        return "&fRepairing the item by &e" + repair + "&f durability";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setRepair(int repair) {
        this.repair = repair;
    }

    @Override
    public DynamicItemModifier copy() {
        DurabilityRepairNumeric m = new DurabilityRepairNumeric(getName());
        m.setRepair(this.repair);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One number is expected: an integer.";
        try {
            repair = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored){
            return "One number is expected: an integer. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<amount_to_repair>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
