package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.food;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.item.FoodPropertyManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EffectNullification extends DynamicItemModifier {
    public EffectNullification(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        FoodPropertyManager.setCancelPotionEffects(context.getItem().getMeta(), true);
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
        return new ItemBuilder(Material.MILK_BUCKET).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Nullify Potion Effects";
    }

    @Override
    public String getDescription() {
        return "&fMakes it so vanilla items that would grant potion effects stop doing that.";
    }

    @Override
    public String getActiveDescription() {
        return "&fMakes it so vanilla items that would grant potion effects stop doing that.";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.FOOD.id());
    }

    @Override
    public DynamicItemModifier copy() {
        EffectNullification m = new EffectNullification(getName());
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
