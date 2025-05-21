package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.AlchemyItemPropertyManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AlchemyQualityAdd extends DynamicItemModifier {
    private int quality = 0;

    public AlchemyQualityAdd(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        int quality = AlchemyItemPropertyManager.getQuality(context.getItem().getMeta());
        int newQuality = Math.max(0, quality + this.quality);
        AlchemyItemPropertyManager.setQuality(context.getItem().getMeta(), newQuality);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) quality = quality + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(11,
                new ItemBuilder(Material.PAPER)
                        .name("&eHow much quality to add?")
                        .lore("&fAdds &e" + quality + "&f quality to the item",
                                "",
                                "&fFor example, an item with 100",
                                "&fquality is converted to &e" + (100 + quality),
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
        return "&dAlchemy Quality (ADD)";
    }

    @Override
    public String getDescription() {
        return "&fAdds a set amount of quality to the item";
    }

    @Override
    public String getActiveDescription() {
        return "&fAdds &e" + quality + "&f quality to the item's existing quality";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.POTION_MISC.id());
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    @Override
    public DynamicItemModifier copy() {
        AlchemyQualityAdd m = new AlchemyQualityAdd(getName());
        m.setQuality(this.quality);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One numbers is expected: an integer";
        try {
            quality = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored){
            return "One numbers is expected: an integer. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<quality_to_add>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
