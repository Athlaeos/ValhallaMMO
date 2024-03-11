package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_misc;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.item.AlchemyItemPropertyManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AlchemyQualityMultiply extends DynamicItemModifier {
    private double multiplyBy = 1;

    public AlchemyQualityMultiply(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        int quality = AlchemyItemPropertyManager.getQuality(outputItem.getMeta());
        int newQuality = Math.max(0, (int) Math.floor(quality * multiplyBy));
        AlchemyItemPropertyManager.setQuality(outputItem.getMeta(), newQuality);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12)
            multiplyBy = Math.max(0, multiplyBy + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01)));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&eHow much to multiply quality with?")
                        .lore("&fMultiplies quality with &e" + StatFormat.FLOAT_P2.format(multiplyBy),
                                "",
                                "&fFor example, 300 alchemy quality",
                                "&fis converted to &e" + (int) (Math.floor(multiplyBy * 300)) + " &fquality",
                                "&6Click to add/subtract 0.01",
                                "&6Shift-Click to add/subtract 0.1")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.NETHER_STAR).get();
    }

    @Override
    public String getDisplayName() {
        return "&dAlchemy Quality (MULTIPLY)";
    }

    @Override
    public String getDescription() {
        return "&fMultiplies an item's quality by a given multiplier.";
    }

    @Override
    public String getActiveDescription() {
        return "&fMultiplies the item's quality by &e" + StatFormat.FLOAT_P2.format(multiplyBy);
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.POTION_MISC.id());
    }

    public void setMultiplyBy(double multiplyBy) {
        this.multiplyBy = multiplyBy;
    }

    @Override
    public DynamicItemModifier copy() {
        AlchemyQualityMultiply m = new AlchemyQualityMultiply(getName());
        m.setMultiplyBy(this.multiplyBy);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: an integer";
        try {
            multiplyBy = StringUtils.parseDouble(args[0]);
        } catch (NumberFormatException ignored){
            return "One argument is expected: an integer. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<multiplier>");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
