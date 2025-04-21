package me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations;

import com.nexomc.nexo.api.NexoItems;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class NexoChoice extends RecipeOption {
    @Override
    public String getName() {
        return "CHOICE_NEXO_ITEM";
    }

    @Override
    public String getActiveDescription() {
        return "This ingredient will need to match a specific Nexo item";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemBuilder(Material.KNOWLEDGE_BOOK).name("&7Nexo Item Requirement")
                .lore(
                        "&aRequire this ingredient to be a specific",
                        "&aNexo item, identified by its Nexo ID.",
                        "",
                        "&7The name of the ingredient will be",
                        "&7used to communicate item requirement",
                        "&7to the player.").get();
    }

    @Override
    public boolean isCompatible(ItemStack i) {
        // Check if the item is a Nexo item
        return NexoItems.idFromItem(i) != null;
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

                // Get the reference Nexo ID
                String referenceNexoId = NexoItems.idFromItem(i);
                if (referenceNexoId == null) return false;

                // Check if the tested item has the same Nexo ID
                String testedNexoId = NexoItems.idFromItem(itemStack);
                return referenceNexoId.equals(testedNexoId);
            }
        };
    }

    @Override
    public boolean matches(ItemStack i1, ItemStack i2) {
        // Get Nexo IDs from both items
        String nexoId1 = NexoItems.idFromItem(i1);
        String nexoId2 = NexoItems.idFromItem(i2);

        // If both items don't have Nexo IDs, they don't match the requirement
        if (nexoId1 == null && nexoId2 == null) return false;

        // If only one has a Nexo ID, they don't match
        if (nexoId1 == null || nexoId2 == null) return false;

        // Check if both items have the same Nexo ID
        return nexoId1.equals(nexoId2);
    }

    @Override
    public String ingredientDescription(ItemStack base) {
        // Return the item's display name for the recipe description
        ItemMeta meta = base.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }
        // If no display name, try to get the Nexo ID as a fallback
        String nexoId = NexoItems.idFromItem(base);
        if (nexoId != null) {
            return "Nexo Item: " + nexoId;
        }
        // Item name fallback
        return ItemUtils.getItemName(ItemUtils.getItemMeta(base));
    }

    @Override
    public RecipeOption getNew() {
        return new NexoChoice();
    }

    @Override
    public boolean isCompatibleWithInputItem(boolean isInput) {
        return true;
    }
}