package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ArmorSet;
import me.athlaeos.valhallammo.item.ArmorSetRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ArmorSetSet extends DynamicItemModifier {
    private String set = null;

    public ArmorSetSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        ArmorSetRegistry.setArmorSet(context.getItem(), ArmorSetRegistry.getRegisteredSets().get(set));
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        List<ArmorSet> sets = new ArrayList<>(ArmorSetRegistry.getRegisteredSets().values());
        if (button == 12){
            if (e.isShiftClick()) set = null;
            else {
                ArmorSet set = ArmorSetRegistry.getRegisteredSets().get(this.set);
                int currentSet = sets.indexOf(set);
                if (e.isLeftClick()) this.set = sets.get(Math.max(0, Math.min(sets.size() - 1, currentSet + 1))).getId();
                else this.set = sets.get(Math.max(0, Math.min(sets.size() - 1, currentSet - 1))).getId();
            }
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.CHEST)
                        .name("&fWhich loot table?")
                        .lore("&fLoot table set to &e" + set,
                                "&6Click to cycle",
                                "&6Shift-Click to remove")
                        .get()).map(Set.of());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.DIAMOND_CHESTPLATE).get();
    }

    @Override
    public String getDisplayName() {
        return "&dArmor Set";
    }

    @Override
    public String getDescription() {
        return "&fSets the armor set type to the item, or removes it. The player will receive additional stats when a full set is worn. View /val armorsets for available armor sets";
    }

    @Override
    public String getActiveDescription() {
        return set == null ? "&fRemoving armor set from item" : "&fSetting armor set &e" + set + "&f to the item";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setSet(String set) {
        this.set = set;
    }

    @Override
    public DynamicItemModifier copy() {
        ArmorSetSet m = new ArmorSetSet(getName());
        m.setSet(this.set);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length < 1) return "One argument is expected: the name of the armor set";
        set = args[0];
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return new ArrayList<>(ArmorSetRegistry.getRegisteredSets().keySet());
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
