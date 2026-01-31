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

public class VanillaItemRequirement extends DynamicItemModifier {
    public VanillaItemRequirement(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (!context.shouldValidate()) return;
        if (SmithingItemPropertyManager.hasSmithingQuality(context.getItem().getMeta())) return;
        failedRecipe(context.getItem(), TranslationManager.getTranslation("warning_vanilla_item_required"));
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
        return new ItemBuilder(Material.ANVIL).get();
    }

    @Override
    public String getDisplayName() {
        return "&cCancel if item is custom";
    }

    @Override
    public String getDescription() {
        return "&fIf the item has a smithing quality value, the recipe is cancelled";
    }

    @Override
    public String getActiveDescription() {
        return "&fIf the item has a smithing quality value, the recipe is cancelled";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CRAFTING_CONDITIONALS.id());
    }

    @Override
    public DynamicItemModifier copy() {
        VanillaItemRequirement m = new VanillaItemRequirement(getName());
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
