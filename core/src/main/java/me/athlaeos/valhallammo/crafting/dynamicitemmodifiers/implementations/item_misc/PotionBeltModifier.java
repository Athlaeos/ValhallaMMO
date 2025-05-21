package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.PotionBelt;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PotionBeltModifier extends DynamicItemModifier{
    private int capacity = 8;

    public PotionBeltModifier(String id) {
        super(id);
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.ENCHANTED_BOOK).get();
    }

    @Override
    public String getDisplayName() {
        return "&dCreate Potion Belt";
    }

    @Override
    public String getDescription() {
        return "&fTurns the item into a potion belt, allowing it to hold several potions";
    }

    @Override
    public String getActiveDescription() {
        return "&fTurns the item into a potion belt, allowing it to hold &d" + capacity + "&f potions";
    }

    @Override
    public Collection<String> getCategories() {
        return List.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    @Override
    public DynamicItemModifier copy() {
        PotionBeltModifier m1 = new PotionBeltModifier(getName());
        m1.setCapacity(this.capacity);
        m1.setPriority(this.getPriority());
        return m1;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public String parseCommand(CommandSender commandSender, String[] args) {
        if (args.length != 1) return "One number is expected: an integer";
        try {
            capacity = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored){
            return "One number is expected: an integer. It was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender commandSender, int i) {
        if (i == 0) return List.of("<capacity>");
        return List.of();
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&fWhich capacity?")
                        .lore("&f" + capacity + " set",
                                "&6Click to add/subtract 1",
                                "&6Shift-Click to add/subtract 10")
                        .get()).map(Set.of(
        ));
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int i) {
        if (i == 12)
            capacity = Math.max(1, capacity + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10 : 1)));
    }

    @Override
    public void processItem(ModifierContext context) {
        PotionBelt.setPotions(context.getItem().getMeta(), new ArrayList<>());
        PotionBelt.setCapacity(context.getItem().getMeta(), capacity);
        PotionBelt.setStoredBelt(context.getItem().getMeta(), context.getItem().get());
        ItemStack belt = context.getItem().get();
        PotionBelt.setStoredBelt(context.getItem().getMeta(), belt);
    }
}
