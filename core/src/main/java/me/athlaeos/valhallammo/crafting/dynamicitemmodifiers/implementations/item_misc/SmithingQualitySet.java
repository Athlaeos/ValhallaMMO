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

import java.util.*;

public class SmithingQualitySet extends DynamicItemModifier {
    private int quality = 50;

    public SmithingQualitySet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        SmithingItemPropertyManager.setQuality(context.getItem().getMeta(), quality);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12)
            quality = Math.max(0, quality + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&eHow much quality should the item have?")
                        .lore("&fQuality set to " + quality,
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 10")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.NETHER_STAR).get();
    }

    @Override
    public String getDisplayName() {
        return "&eSmithing Quality (NUMERIC)";
    }

    @Override
    public String getDescription() {
        return "&fSets the item's smithing quality.";
    }

    @Override
    public String getActiveDescription() {
        return "&fSets the item's smithing quality to &e" + quality;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    @Override
    public DynamicItemModifier copy() {
        SmithingQualitySet m = new SmithingQualitySet(getName());
        m.setQuality(this.quality);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument is expected: an integer";
        try {
            quality = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored){
            return "One argument is expected: an integer. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<quality>");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
