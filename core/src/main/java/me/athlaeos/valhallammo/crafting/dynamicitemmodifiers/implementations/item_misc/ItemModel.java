package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemModel extends DynamicItemModifier {
    private String model = null;

    public ItemModel(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        ValhallaMMO.getNms().setItemModel(context.getItem().getMeta(), model);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12){
            ItemStack cursor = e.getCursor();
            if (ItemUtils.isEmpty(cursor)) model = null;
            else {
                ItemMeta cursorMeta = cursor.getItemMeta();
                String model = ValhallaMMO.getNms().getItemModel(cursorMeta);
                if (model == null) e.getWhoClicked().sendMessage(Utils.chat("&cItem has no model"));
                else this.model = model;
            }
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.BARRIER)
                        .name("&fWhich model should the item have?")
                        .lore("&fCurrently set to &e" + (model == null ? "&cnothing" : model),
                                "&fDetermines the visual appearance",
                                "&fof the item",
                                "&6Click with another item to",
                                "&6copy the model of the item over")
                        .get()).map(Set.of());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.PAINTING).get();
    }

    @Override
    public String getDisplayName() {
        return "&aSet Item Model";
    }

    @Override
    public String getDescription() {
        return "&fChanges the texture or model of the item";
    }

    @Override
    public String getActiveDescription() {
        return model == null ? "&cRemoves item model" : "&fSets item model to " + model;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public DynamicItemModifier copy() {
        ItemModel m = new ItemModel(getName());
        m.setModel(this.model);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1)
            return "You must enter the model you want the item to have, or 'reset' if you want to remove it";
        model = args[0].equalsIgnoreCase("reset") ? null : args[0];
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<model>", "minecraft:");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
