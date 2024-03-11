package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.skills.skills.implementations.FishingSkill;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BaitValueSet extends DynamicItemModifier {
    private double bait = 1;

    public BaitValueSet(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        if (bait == 0) FishingSkill.setBaitPower(outputItem.getMeta(), null);
        else FishingSkill.setBaitPower(outputItem.getMeta(), bait);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12)
            bait += (e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1 : 0.1);
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&eHow much luck should this bait give?")
                        .lore("&fBait power set to " + bait,
                                "&6Click to add/subtract 0.1",
                                "&6Shift-Click to add/subtract 1")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.FISHING_ROD).get();
    }

    @Override
    public String getDisplayName() {
        return "&eBait Power";
    }

    @Override
    public String getDescription() {
        return "&fSets the item's bait power value. Bait power increases fishing luck. /n(be sure to check skills/fishing.yml for valid items you can use for this property)";
    }

    @Override
    public String getActiveDescription() {
        return "&fSets the item's bait power value to &e" + bait + "&f. Bait power increases fishing luck. /n(be sure to check skills/fishing.yml for valid items you can use for this property)";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setBait(double bait) {
        this.bait = bait;
    }

    @Override
    public DynamicItemModifier copy() {
        BaitValueSet m = new BaitValueSet(getName());
        m.setBait(this.bait);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: a double";
        try {
            bait = StringUtils.parseDouble(args[0]);
        } catch (NumberFormatException ignored){
            return "One argument is expected: a double. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<bait_power>");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
