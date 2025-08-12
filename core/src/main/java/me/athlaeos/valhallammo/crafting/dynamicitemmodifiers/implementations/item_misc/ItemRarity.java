package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.ItemRarityWrapper;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ItemRarity extends DynamicItemModifier {
    private ItemRarityWrapper rarity = ItemRarityWrapper.COMMON;

    public ItemRarity(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        ValhallaMMO.getNms().setItemRarity(context.getItem().getMeta(), rarity);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            List<ItemRarityWrapper> rarities = Arrays.asList(ItemRarityWrapper.values());
            int currentClass = rarities.indexOf(rarity);
            if (e.isLeftClick()) {
                if (currentClass + 1 >= rarities.size()) currentClass = 0;
                else currentClass++;
            } else {
                if (currentClass - 1 < 0) currentClass = rarities.size() - 1;
                else currentClass--;
            }
            rarity = rarities.get(currentClass);
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&eWhich rarity should it be?")
                        .lore("&fRarity set to " + rarity.getColor() + rarity,
                                "&fHas no effect on gameplay, unless",
                                "&fother plugins implement such effects.",
                                "&fMerely changes the default name color",
                                "&fof the item")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.ENCHANTED_GOLDEN_APPLE).get();
    }

    @Override
    public String getDisplayName() {
        return "&dRarity";
    }

    @Override
    public String getDescription() {
        return "&fChanges the rarity of an item.";
    }

    @Override
    public String getActiveDescription() {
        return "&fRarity set to " + rarity.getColor() + rarity;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setRarity(ItemRarityWrapper rarity) {
        this.rarity = rarity;
    }

    @Override
    public DynamicItemModifier copy() {
        ItemRarity m = new ItemRarity(getName());
        m.setRarity(this.rarity);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "You must indicate the rarity to set the item to";
        try {
            rarity = ItemRarityWrapper.valueOf(args[0]);
        } catch (IllegalArgumentException ignored){
            return "Invalid rarity, valid classes are: " + Arrays.stream(ItemRarityWrapper.values()).map(r -> r.getColor() + r).collect(Collectors.joining(", "));
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return Arrays.stream(ItemRarityWrapper.values()).map(ItemRarityWrapper::toString).toList();
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
