package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards;

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

public class VanillaExperience extends DynamicItemModifier {
    private int amount = 5;

    public VanillaExperience(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (!context.shouldExecuteUsageMechanics()) return;
        context.getCrafter().giveExp(amount * context.getTimesExecuted());
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12)
            amount = Math.max(1, amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                        .name("&eHow much vanilla EXP should be given?")
                        .lore("&fSet to &e" + amount,
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 10")
                        .get()).map(new HashSet<>());
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.EXPERIENCE_BOTTLE).get();
    }

    @Override
    public String getDisplayName() {
        return "&eGrant Vanilla EXP";
    }

    @Override
    public String getDescription() {
        return "&fGives the player an amount of &evanilla EXP";
    }

    @Override
    public String getActiveDescription() {
        return String.format("&fGives the player %d vanilla &eEXP", amount);
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.REWARDS.id());
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public DynamicItemModifier copy() {
        VanillaExperience m = new VanillaExperience(getName());
        m.setAmount(this.amount);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: an integer";
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored){
            return "One argument is expected: an integer. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<amount>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
