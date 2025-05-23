package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ResultChangingModifier;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ItemReplaceKeepingAmount extends DynamicItemModifier implements ResultChangingModifier {
    private ItemStack replaceBy = new ItemStack(Material.DIAMOND);

    public ItemReplaceKeepingAmount(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        int amount = context.getItem().getItem().getAmount();
        context.getItem().setItem(replaceBy);
        context.getItem().setMeta(ItemUtils.getItemMeta(replaceBy));
        context.getItem().amount(amount);
    }

    @Override
    public ItemStack getNewResult(ModifierContext context) {
        return replaceBy;
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            ItemStack cursor = e.getCursor();
            if (!ItemUtils.isEmpty(cursor)) {
                replaceBy = cursor.clone();
            }
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(replaceBy)
                        .name("&eWhat should the new item be?")
                        .lore("&6Click with another item to",
                                "&6copy it over.",
                                "&fSet to " + ItemUtils.getItemName(ItemUtils.getItemMeta(replaceBy)))
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.LAPIS_LAZULI).get();
    }

    @Override
    public String getDisplayName() {
        return "&dReplace Item, keeping same quantity";
    }

    @Override
    public String getDescription() {
        return "&fReplaces the item with another one, but the original amount of the item stays the same.";
    }

    @Override
    public String getActiveDescription() {
        return "&fItem will be replaced by " + ItemUtils.getItemName(ItemUtils.getItemMeta(replaceBy)) + ", but the original amount of the item stays the same.";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setReplaceBy(ItemStack replaceBy) {
        this.replaceBy = replaceBy;
    }

    @Override
    public DynamicItemModifier copy() {
        ItemReplaceKeepingAmount m = new ItemReplaceKeepingAmount(getName());
        m.setReplaceBy(this.replaceBy);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "You must indicate the item type for replacement, or 'hand' for your current held item";
        if (args[0].equalsIgnoreCase("hand")) {
            if (!(executor instanceof Player p)) return "This argument requires you to be a player for usage";
            ItemStack held = p.getInventory().getItemInMainHand();
            if (ItemUtils.isEmpty(held)) return "The replace-by item cannot be nothing";
            replaceBy = held.clone();
        } else {
            try {
                ItemStack item = new ItemStack(Material.valueOf(args[0]));
                if (ItemUtils.isEmpty(item)) return "The replace-by item cannot be nothing";
                replaceBy = new ItemStack(item);
            } catch (IllegalArgumentException ignored){
                return "A material is required. Invalid material";
            }
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return Arrays.stream(Material.values()).map(Object::toString).collect(Collectors.toList());
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
