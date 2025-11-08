package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.hooks.CEHook;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class CEChoice extends RecipeOption {
    @Override
    public String getName() {
        return "CHOICE_CRAFTENGINE_ITEM";
    }

    @Override
    public String getActiveDescription() {
        return "This ingredient will need to match a specific CraftEngine item";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.KNOWLEDGE_BOOK).name("&7CraftEngine Item Requirement")
                .lore(
                        "&aRequire this ingredient to be a specific",
                        "&aCraftEngine item, identified by its CraftEngine ID.",
                        "",
                        "&7The name of the ingredient will be",
                        "&7used to communicate item requirement",
                        "&7to the player.").get();
    }

    @Override
    public boolean isCompatible(ItemStack i) {
        if (!ValhallaMMO.isHookFunctional(CEHook.class)) return false;

        // Check if the item is a CraftEngine item
        return CEHook.getCraftEngineItemID(i) != null;
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
                if (!ValhallaMMO.isHookFunctional(CEHook.class)) return false;

                // Get the reference CraftEngine ID
                String referenceCraftEngineId = CEHook.getCraftEngineItemID(i);
                if (referenceCraftEngineId == null) return false;

                // Check if the tested item has the same CraftEngine ID
                String testedCraftEngineId = CEHook.getCraftEngineItemID(itemStack);
                return referenceCraftEngineId.equals(testedCraftEngineId);
            }
        };
    }

    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        if (!ValhallaMMO.isHookFunctional(CEHook.class)) return false;

        // Get CraftEngine IDs from both items
        String craftEngineId1 = CEHook.getCraftEngineItemID(i1);
        String craftEngineId2 = CEHook.getCraftEngineItemID(i2);

        // If both items don't have CraftEngine IDs, they don't match the requirement
        if (craftEngineId1 == null && craftEngineId2 == null) return false;

        // If only one has a CraftEngine ID, they don't match
        if (craftEngineId1 == null || craftEngineId2 == null) return false;

        // Check if both items have the same CraftEngine ID
        return craftEngineId1.equals(craftEngineId2);
    }

    @Override
    public String ingredientDescription(ItemStack base) {
        if (!ValhallaMMO.isHookFunctional(CEHook.class)) {
            // If CEHook is not functional, return item's material name
            return ItemUtils.getItemName(new ItemBuilder(base));
        }

        // Return the item's display name for the recipe description
        ItemMeta meta = base.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }

        // If no display name, try to get the CraftEngine ID as a fallback
        String craftEngineId = CEHook.getCraftEngineItemID(base);
        if (craftEngineId != null) {
            return "CraftEngine Item: " + craftEngineId;
        }

        // Item name fallback
        return ItemUtils.getItemName(new ItemBuilder(base));
    }

    @Override
    public RecipeOption getNew() {
        return new CEChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }
}