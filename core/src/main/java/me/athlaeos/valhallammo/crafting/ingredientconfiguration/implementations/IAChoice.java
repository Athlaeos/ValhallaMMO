package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.hooks.IAHook;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class IAChoice extends RecipeOption {
    @Override
    public String getName() {
        return "CHOICE_ITEMSADDER_ITEM";
    }

    @Override
    public String getActiveDescription() {
        return "This ingredient will need to match a specific ItemsAdder item";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.KNOWLEDGE_BOOK).name("&7ItemsAdder Item Requirement")
                .lore(
                        "&aRequire this ingredient to be a specific",
                        "&aItemsAdder item, identified by its ItemsAdder ID.",
                        "",
                        "&7The name of the ingredient will be",
                        "&7used to communicate item requirement",
                        "&7to the player.").get();
    }

    @Override
    public boolean isCompatible(ItemStack i) {
        if (!ValhallaMMO.isHookFunctional(IAHook.class)) return false;

        // Check if the item is a ItemsAdder item
        return IAHook.getItemsAdderItemID(i) != null;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public RecipeChoice getChoice(ItemStack i) {
        return new RecipeChoice.ExactChoice(i) {
            @Override
            public boolean test(@NotNull ItemStack itemStack) {
                if (!ValhallaMMO.isHookFunctional(IAHook.class)) return false;

                // Get the reference ItemsAdder ID
                String referenceItemsAdderId = IAHook.getItemsAdderItemID(i);
                if (referenceItemsAdderId == null) return false;

                // Check if the tested item has the same ItemsAdder ID
                String testedItemsAdderId = IAHook.getItemsAdderItemID(itemStack);
                return referenceItemsAdderId.equals(testedItemsAdderId);
            }
        };
    }

    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        if (!ValhallaMMO.isHookFunctional(IAHook.class)) return false;

        // Get ItemsAdder IDs from both items
        String itemsAdderId1 = IAHook.getItemsAdderItemID(i1);
        String itemsAdderId2 = IAHook.getItemsAdderItemID(i2);

        // If both items don't have ItemsAdder IDs, they don't match the requirement
        if (itemsAdderId1 == null && itemsAdderId2 == null) return false;

        // If only one has a ItemsAdder ID, they don't match
        if (itemsAdderId1 == null || itemsAdderId2 == null) return false;

        // Check if both items have the same ItemsAdder ID
        return itemsAdderId1.equals(itemsAdderId2);
    }

    @Override
    public String ingredientDescription(ItemStack base) {
        if (!ValhallaMMO.isHookFunctional(IAHook.class)) {
            // If IAHook is not functional, return item's material name
            return ItemUtils.getItemName(new ItemBuilder(base));
        }

        // Return the item's display name for the recipe description
        ItemMeta meta = base.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }

        // If no display name, try to get the ItemsAdder ID as a fallback
        String itemsAdderId = IAHook.getItemsAdderItemID(base);
        if (itemsAdderId != null) {
            return "ItemsAdder Item: " + itemsAdderId;
        }

        // Item name fallback
        return ItemUtils.getItemName(new ItemBuilder(base));
    }

    @Override
    public RecipeOption getNew() {
        return new IAChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }
}