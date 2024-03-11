package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.crafting_conditionals;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.hooks.VaultHook;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.hooks.VaultTransaction;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CostMoney extends DynamicItemModifier {
    private double amount = 300;

    public CostMoney(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        if (!ValhallaMMO.isHookFunctional(VaultHook.class)) return;
        if ((validate && VaultTransaction.getBalance(crafter) < amount * timesExecuted) ||
                (use && !VaultTransaction.withdrawBalance(crafter, amount * timesExecuted))){
            String warning = TranslationManager.getTranslation("modifier_warning_insufficient_funds");
            failedRecipe(outputItem,
                    warning.replace("%quantity%", String.valueOf(timesExecuted))
                            .replace("%cost%", String.format("%,.2f", amount))
            );
        }
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 10)
            amount = Math.max(0, amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1D : 0.01D)));
        else if (button == 11)
            amount = Math.max(0, amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10D : 1D)));
        else if (button == 12)
            amount = Math.max(0, amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1000D : 100D)));
        else if (button == 13)
            amount = Math.max(0, amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 100000D : 10000D)));
        else if (button == 14)
            amount = Math.max(0, amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10000000D : 1000000D)));
        else if (button == 15)
            amount = Math.max(0, amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1000000000D : 100000000D)));
        else if (button == 16)
            amount = Math.max(0, amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 100000000000D : 10000000000D)));
        else if (button == 17)
            amount = Math.max(0, amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10000000000000D : 1000000000000D)));
        else if (button == 18)
            amount = Math.max(0, amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1000000000000000D : 100000000000000D)));
        else if (button == 19)
            amount = Math.max(0, amount + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 100000000000000000D : 10000000000000000D)));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(10,
                new ItemBuilder(Material.IRON_NUGGET)
                        .name("&eHow much money should be required?")
                        .lore(String.format("&fSet to &e%,.2f", amount),
                                "&6Click to add/subtract 0.01$",
                                "&6Shift-Click to add/subtract 0.1$")
                        .get()).map(Set.of(
                new Pair<>(11,
                        new ItemBuilder(Material.IRON_INGOT)
                                .name("&eHow much money should be required?")
                                .lore(String.format("&fSet to &e%,.2f", amount),
                                        "&6Click to add/subtract 1$",
                                        "&6Shift-Click to add/subtract 10$")
                                .get()),
                new Pair<>(12,
                        new ItemBuilder(Material.IRON_BARS)
                                .name("&eHow much money should be required?")
                                .lore(String.format("&fSet to &e%,.2f", amount),
                                        "&6Click to add/subtract 100$",
                                        "&6Shift-Click to add/subtract 1K$")
                                .get()),
                new Pair<>(13,
                        new ItemBuilder(Material.GOLD_NUGGET)
                                .name("&eHow much money should be required?")
                                .lore(String.format("&fSet to &e%,.2f", amount),
                                        "&6Click to add/subtract 10K$",
                                        "&6Shift-Click to add/subtract 100K$")
                                .get()),
                new Pair<>(14,
                        new ItemBuilder(Material.GOLD_INGOT)
                                .name("&eHow much money should be required?")
                                .lore(String.format("&fSet to &e%,.2f", amount),
                                        "&6Click to add/subtract 1M$",
                                        "&6Shift-Click to add/subtract 10M$")
                                .get()),
                new Pair<>(15,
                        new ItemBuilder(Material.GOLD_BLOCK)
                                .name("&eHow much money should be required?")
                                .lore(String.format("&fSet to &e%,.2f", amount),
                                        "&6Click to add/subtract 100M$",
                                        "&6Shift-Click to add/subtract 1B$")
                                .get()),
                new Pair<>(16,
                        new ItemBuilder(Material.DIAMOND)
                                .name("&eHow much money should be required?")
                                .lore(String.format("&fSet to &e%,.2f", amount),
                                        "&6Click to add/subtract 10B$",
                                        "&6Shift-Click to add/subtract 100B$")
                                .get()),
                new Pair<>(17,
                        new ItemBuilder(Material.DIAMOND_BLOCK)
                                .name("&eHow much money should be required?")
                                .lore(String.format("&fSet to &e%,.2f", amount),
                                        "&6Click to add/subtract 1T$",
                                        "&6Shift-Click to add/subtract 10T$")
                                .get()),
                new Pair<>(18,
                        new ItemBuilder(Material.NETHERITE_INGOT)
                                .name("&eHow much money should be required?")
                                .lore(String.format("&fSet to &e%,.2f", amount),
                                        "&6Click to add/subtract 100T$",
                                        "&6Shift-Click to add/subtract 1Q$")
                                .get()),
                new Pair<>(19,
                        new ItemBuilder(Material.NETHERITE_BLOCK)
                                .name("&eHow much money should be required?")
                                .lore(String.format("&fSet to &e%,.2f", amount),
                                        "&6Click to add/subtract 10Q$",
                                        "&6Shift-Click to add/subtract 100Q$")
                                .get())
        ));
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.GOLD_NUGGET).get();
    }

    @Override
    public String getDisplayName() {
        return "&2Costs Money";
    }

    @Override
    public String getDescription() {
        return "&fRequires the player to have and spend an amount of money";
    }

    @Override
    public String getActiveDescription() {
        return String.format("&fGives the player %,.2f$", amount);
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CRAFTING_CONDITIONALS.id());
    }

    @Override
    public DynamicItemModifier copy() {
        CostMoney m = new CostMoney(getName());
        m.setAmount(this.amount);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: a double";
        try {
            amount = StringUtils.parseDouble(args[0]);
        } catch (NumberFormatException ignored){
            return "One argument is expected: a double. It was not a number";
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
