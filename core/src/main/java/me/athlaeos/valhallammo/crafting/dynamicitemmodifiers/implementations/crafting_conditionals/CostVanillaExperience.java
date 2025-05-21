package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.crafting_conditionals;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CostVanillaExperience extends DynamicItemModifier {
    private int amount = 5;

    public CostVanillaExperience(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if ((context.shouldValidate() && EntityUtils.getTotalExperience(context.getCrafter()) < amount * context.getTimesExecuted()) ||
                (context.shouldExecuteUsageMechanics() && !EntityUtils.addExperience(context.getCrafter(), -amount * context.getTimesExecuted()))){
            String warning = TranslationManager.getTranslation("modifier_warning_insufficient_exp");
            failedRecipe(context.getItem(),
                    warning.replace("%quantity%", String.valueOf(context.getTimesExecuted()))
                            .replace("%cost%", String.format("%,d", amount))
            );
        }
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
                        .name("&eHow much vanilla EXP should it cost?")
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
        return "&eCost Vanilla EXP";
    }

    @Override
    public String getDescription() {
        return "&fRequires the player to have and spend an amount of &evanilla EXP";
    }

    @Override
    public String getActiveDescription() {
        return String.format("&fRequires the player to have and spend %d vanilla &eEXP", amount);
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CRAFTING_CONDITIONALS.id());
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public DynamicItemModifier copy() {
        CostVanillaExperience m = new CostVanillaExperience(getName());
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
