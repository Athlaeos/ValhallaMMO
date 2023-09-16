package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.item.CustomDurabilityManager;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.*;

public class DurabilityRandomized extends DynamicItemModifier {

    public DurabilityRandomized(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        if (!use) return;
        if (!(outputItem.getMeta() instanceof Damageable) || outputItem.getItem().getType().getMaxDurability() <= 0) return;
        if (CustomDurabilityManager.hasCustomDurability(outputItem.getMeta())){
            int maxDurability = CustomDurabilityManager.getDurability(outputItem.getItem(), outputItem.getMeta(), true);
            int randomDurability = Utils.getRandom().nextInt(maxDurability) + 1;
            CustomDurabilityManager.setDurability(outputItem.getItem(), outputItem.getMeta(), randomDurability, maxDurability);
        } else {
            Damageable meta = (Damageable) outputItem.getMeta();
            int maxDurability = outputItem.getItem().getType().getMaxDurability();
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
    public DynamicItemModifier createNew() {
        return new DurabilityRandomized(getName());
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
