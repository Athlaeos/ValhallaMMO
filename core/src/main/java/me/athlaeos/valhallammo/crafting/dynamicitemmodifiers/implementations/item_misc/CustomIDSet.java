package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.CustomID;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustomIDSet extends DynamicItemModifier {
    private Integer customID = 1000000;

    public CustomIDSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        CustomID.setID(context.getItem().getMeta(), customID);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 11 || button == 12 || button == 13) {
            ItemStack cursor = e.getCursor();
            if (!ItemUtils.isEmpty(cursor)) {
                ItemMeta meta = cursor.getItemMeta();
                if (meta != null) customID = CustomID.getID(meta);
            } else {
                if (button == 11)
                    customID = Math.min(9999999, Math.max(0, customID + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1000000 : 100000))));
                else if (button == 12)
                    customID = Math.min(9999999, Math.max(0, customID + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10000 : 1000))));
                else
                    customID = Math.min(9999999, Math.max(0, customID + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 25 : 1))));
            }
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(11,
                new ItemBuilder(Material.RED_DYE)
                        .name("&eWhat should custom id be?")
                        .lore("&6Click with another item to copy",
                                "&6its custom id over.",
                                "&fSet to " + (customID == null || customID == 0 ? "removal" : customID),
                                "&6Click to add/subtract 1000000",
                                "&6Shift-Click to add/subtract 100000")
                        .get()).map(Set.of(
                new Pair<>(12,
                        new ItemBuilder(Material.GREEN_DYE)
                                .name("&eWhat should custom id be?")
                                .lore("&6Click with another item to copy",
                                        "&6its custom id over.",
                                        "&fSet to " + (customID == null || customID == 0 ? "removal" : customID),
                                        "&6Click to add/subtract 10000",
                                        "&6Shift-Click to add/subtract 1000")
                                .get()),
                new Pair<>(13,
                        new ItemBuilder(Material.BLUE_DYE)
                                .name("&eWhat should custom model data be?")
                                .lore("&6Click with another item to copy",
                                        "&6its custom id over.",
                                        "&fSet to " + (customID == null || customID == 0 ? "removal" : customID),
                                        "&6Click to add/subtract 25",
                                        "&6Shift-Click to add/subtract 1")
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.NAME_TAG).get();
    }

    @Override
    public String getDisplayName() {
        return "&aSet Custom ID";
    }

    @Override
    public String getDescription() {
        return "&fSets a custom ID to the item. This ID can be used for conditions or ingredient requirements";
    }

    @Override
    public String getActiveDescription() {
        if (customID == null || customID == 0) return "&fRemoves the item's custom ID";
        else return "&fSets the custom ID of the item to " + customID;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setCustomID(Integer customID) {
        this.customID = customID;
    }

    @Override
    public DynamicItemModifier copy() {
        CustomIDSet m = new CustomIDSet(getName());
        m.setCustomID(this.customID);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1)
            return "You must indicate the custom ID of the item";
        try {
            customID = Integer.parseInt(args[0]);
        } catch (IllegalArgumentException ignored) {
            customID = null;
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<custom_id>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
