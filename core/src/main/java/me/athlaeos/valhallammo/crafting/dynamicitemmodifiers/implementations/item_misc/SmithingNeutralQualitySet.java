package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmithingNeutralQualitySet extends DynamicItemModifier {
    private int neutral = 50;

    public SmithingNeutralQualitySet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        SmithingItemPropertyManager.setNeutralQuality(context.getItem().getMeta(), neutral);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12)
            neutral = Math.max(0, neutral + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&eHow much quality is neutral for this item?")
                        .lore("&fSet to &e" + neutral,
                                " ",
                                "&eDetermines the tooltip added to the",
                                "&eitem. Neutral is typically the quality",
                                "&eat which an item is at vanilla strength.",
                                "&eNeutral quality is considered " + SmithingItemPropertyManager.getQualityLore(neutral, neutral),
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 10")
                        .get()).map(Set.of(
                new Pair<>(17,
                        new ItemBuilder(Material.PAPER)
                                .name("&9Neutral quality info")
                                .lore("&fSet to &e" + neutral,
                                        " ",
                                        "&bNeutral quality is an arbitrary",
                                        "&bquality level at which an item",
                                        "&bis considered 'good' by the plugin.",
                                        "",
                                        "&bQualities under this neutral level",
                                        "&bare going to result in a negative",
                                        "&btooltip, while qualities above this",
                                        "&bneutral level are going to have ",
                                        "&bpositive tooltips.")
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.NETHER_STAR).get();
    }

    @Override
    public String getDisplayName() {
        return "&dNeutral Quality (NUMERIC)";
    }

    @Override
    public String getDescription() {
        return "&fSets the item's neutral quality.";
    }

    @Override
    public String getActiveDescription() {
        return "&fSets the item's neutral quality to &e" + neutral;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setNeutral(int neutral) {
        this.neutral = neutral;
    }

    @Override
    public DynamicItemModifier copy() {
        SmithingNeutralQualitySet m = new SmithingNeutralQualitySet(getName());
        m.setNeutral(this.neutral);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: an integer";
        try {
            neutral = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored){
            return "One argument is expected: an integer. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<neutral_quality>");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
