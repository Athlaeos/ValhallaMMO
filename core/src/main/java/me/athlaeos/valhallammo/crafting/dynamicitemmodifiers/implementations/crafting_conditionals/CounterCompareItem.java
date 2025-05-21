package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.crafting_conditionals;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import me.athlaeos.valhallammo.localization.TranslationManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CounterCompareItem extends DynamicItemModifier {
    public CounterCompareItem(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (!context.shouldValidate()) return;
        if (SmithingItemPropertyManager.getCounter(context.getItem().getMeta()) > SmithingItemPropertyManager.getCounterLimit(context.getItem().getMeta())){
            failedRecipe(context.getItem(), TranslationManager.getTranslation("modifier_warning_counter_exceeded_item_cap"));
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.CLOCK).get();
    }

    @Override
    public String getDisplayName() {
        return "&cCancel if counter exceeded (ITEM)";
    }

    @Override
    public String getDescription() {
        return "&fIf the item's counter exceeds the limit value assigned to the item, recipe is cancelled";
    }

    @Override
    public String getActiveDescription() {
        return "&fIf the item's counter exceeds the item's limit, recipe is cancelled";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CRAFTING_CONDITIONALS.id());
    }

    @Override
    public DynamicItemModifier copy() {
        CounterCompareItem m = new CounterCompareItem(getName());
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 0;
    }
}
