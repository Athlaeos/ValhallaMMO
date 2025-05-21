package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.item.CustomDurabilityManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.*;

public class DurabilityRandomized extends DynamicItemModifier {

    public DurabilityRandomized(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (!context.shouldExecuteUsageMechanics()) return;
        if (!(context.getItem().getMeta() instanceof Damageable) || context.getItem().getItem().getType().getMaxDurability() <= 0) return;
        if (CustomDurabilityManager.hasCustomDurability(context.getItem().getMeta())){
            int maxDurability = CustomDurabilityManager.getDurability(context.getItem().getMeta(), true);
            int randomDurability = Utils.getRandom().nextInt(maxDurability) + 1;
            CustomDurabilityManager.setDurability(context.getItem().getMeta(), randomDurability, maxDurability);
        } else {
            Damageable meta = (Damageable) context.getItem().getMeta();
            int maxDurability = context.getItem().getItem().getType().getMaxDurability();
            int randomDurability = Utils.getRandom().nextInt(maxDurability) + 1;
            meta.setDamage(maxDurability - randomDurability);
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) { }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.WOODEN_PICKAXE).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Durability (RANDOMIZED)";
    }

    @Override
    public String getDescription() {
        return "&fRandomizes the durability of the item.";
    }

    @Override
    public String getActiveDescription() {
        return "&fRandomizes the durability of the item.";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    @Override
    public DynamicItemModifier copy() {
        DurabilityRandomized m = new DurabilityRandomized(getName());
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
