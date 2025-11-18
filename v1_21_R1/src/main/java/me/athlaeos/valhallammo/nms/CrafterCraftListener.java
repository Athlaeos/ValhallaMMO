package me.athlaeos.valhallammo.nms;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.ToolRequirementType;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicGridRecipe;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Keyed;
import org.bukkit.block.Block;
import org.bukkit.block.Crafter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.inventory.CrafterInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class CrafterCraftListener implements Listener {
    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraft(CrafterCraftEvent e){
        if (e.getRecipe() instanceof ShapedRecipe || e.getRecipe() instanceof ShapelessRecipe) {
            DynamicGridRecipe recipe = CustomRecipeRegistry.getGridRecipesByKey().get(((Keyed) e.getRecipe()).getKey());
            if (recipe == null) return; // vanilla recipe, do nothing
            if ((ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) ||
                    WorldGuardHook.inDisabledRegion(e.getBlock().getLocation(), WorldGuardHook.VMMO_CRAFTING_CRAFTINGTABLE))) {
                e.setCancelled(true);
                return;
            }
            if (recipe.getModifiers().stream().anyMatch(DynamicItemModifier::requiresPlayer)) e.setCancelled(true);
            else {
                Block crafter = e.getBlock();
                if (!(crafter.getState() instanceof Crafter c) || !(c.getInventory() instanceof CrafterInventory ci)) return;
                ItemStack result = verifyIngredients(recipe, ci.getContents());
                if (ItemUtils.isEmpty(result)){
                    Pair<DynamicGridRecipe, ItemStack> corrected = correctRecipe(e.getBlock(), recipe, ci.getContents());
                    if (corrected == null){
                        e.setCancelled(true);
                        return;
                    }
                    recipe = corrected.getOne();
                    result = corrected.getTwo().clone();
                }

                ItemBuilder prepareResultBuilder = new ItemBuilder(result);
                DynamicItemModifier.modify(ModifierContext.builder(prepareResultBuilder).items(ci.getContents()).validate().get(), recipe.getModifiers());
                if (CustomFlag.hasFlag(prepareResultBuilder.getMeta(), CustomFlag.UNCRAFTABLE)) {
                    e.setCancelled(true);
                    return;
                }
                e.setResult(prepareResultBuilder.get());
            }
        }
    }
    private ItemStack verifyIngredients(DynamicGridRecipe recipe, ItemStack[] matrix){
        ItemBuilder result = new ItemBuilder(recipe.getResult().clone());
        if (!recipe.isUnlockedForEveryone()) return null;
        if (recipe.tinker()){
            SlotEntry tinkerEntry = recipe.getGridTinkerEquipment();
            for (ItemStack slot : matrix) {
                // finding item to tinker
                if (ItemUtils.isEmpty(slot)) continue;
                ItemBuilder builder = new ItemBuilder(slot);

                // If the recipe requires valhalla tools, the item is a tool, but has no
                // custom smithing quality, return null
                if (recipe.requireValhallaTools() &&
                        EquipmentClass.getMatchingClass(builder.getMeta()) != null &&
                        !SmithingItemPropertyManager.hasSmithingQuality(builder.getMeta())) return null;

                if (defaultChoice(tinkerEntry).matches(tinkerEntry.getItem(), slot)) {
                    result = new ItemBuilder(slot.clone());
                    break;
                }
            }
        }

        List<SlotEntry> allIngredients = new ArrayList<>(recipe.getItems().values());
        SlotEntry toolEntry = recipe.getItems().get(recipe.getToolIndex());
        for (ItemStack slot : matrix) {
            // finding item to tinker
            if (ItemUtils.isEmpty(slot)) continue;
            ItemBuilder builder = new ItemBuilder(slot);

            for (SlotEntry entry : new ArrayList<>(allIngredients)) {
                // If the item is either a matching tool
                if (toolEntry != null) {
                    if (recipe.getToolRequirement().getToolRequirementType() == ToolRequirementType.NONE_MANDATORY) {
                        // It is mandatory no tool is present. If there is, return null
                        if (ToolRequirementType.getToolID(builder.getMeta()) >= 0) return null;
                    } else if (recipe.getToolRequirement().getToolRequirementType() != ToolRequirementType.NOT_REQUIRED &&
                            recipe.getToolRequirement().getRequiredToolID() >= 0) {
                        // A tool is required, so if the item matches the tool requirement remove it from the list as well
                        if (recipe.getToolRequirement().canCraft(ToolRequirementType.getToolID(builder.getMeta()))) {
                            allIngredients.removeIf(e -> e.isSimilar(entry));
                            continue;
                        }
                    }
                }
                IngredientChoice choice = defaultChoice(entry);
                if (choice.matches(entry.getItem(), slot)) {
                    // The ingredient was found in the matrix items, remove it from the list
                    if (entry.isSimilar(entry) && allIngredients.remove(entry)) break;
                }
            }
        }

        // If allIngredients at this point is not empty that means not all ingredients
        // were verified to exist in the matrix, and so null should be returned
        if (!allIngredients.isEmpty()) return null;
        return result.get();
    }

    private IngredientChoice defaultChoice(SlotEntry entry){
        return Objects.requireNonNullElse(entry.getOption(), new MaterialChoice());
    }

    private Pair<DynamicGridRecipe, ItemStack> correctRecipe(Block crafter, DynamicGridRecipe original, ItemStack[] grid){
        int ingredients = original.getItems().size();
        ItemStack originalCorrect = verifyIngredients(original, grid);
        if (!ItemUtils.isEmpty(originalCorrect)) return new Pair<>(original, originalCorrect);
        for (DynamicGridRecipe recipe : CustomRecipeRegistry.getGridRecipesByIngredientQuantity().getOrDefault(ingredients, new HashSet<>())){
            if (recipe.getName().equals(original.getName()) || (!original.isShapeless() && !recipe.isShapeless() && !matchesShape(original, recipe))) continue;
            ItemStack result = verifyIngredients(recipe, grid);
            if (!ItemUtils.isEmpty(result)) return new Pair<>(recipe, result);
        }
        return null;
    }

    private boolean matchesShape(DynamicGridRecipe d1, DynamicGridRecipe d2){
        if (d1.isShapeless() || d2.isShapeless()) return false; // cannot work with shapeless recipes
        String[] shape1 = d1.getRecipeShapeStrings().getShape();
        String[] shape2 = d2.getRecipeShapeStrings().getShape();
        if (shape1.length != shape2.length) return false; // shapes do not match in length
        if (shape1.length == 0) return false; // shape is empty
        boolean inverse = false;
        int index = 0;
        lines:
        for (int i = 0; i < shape1.length * 2 && index < shape1.length; i++){
            String s1 = shape1[index];
            String s2 = inverse ? new StringBuilder(shape2[index]).reverse().toString() : shape2[index];
            if (s1.length() != s2.length()) return false; // shapes contain strings that do not match in length with eachother
            char[] s1Chars = s1.toCharArray();
            char[] s2Chars = s2.toCharArray();

            for (int c = 0; c < s1Chars.length; c++){
                if ((s1Chars[c] == ' ' && s2Chars[c] == ' ') || (s1Chars[c] != ' ' && s2Chars[c] != ' ')) continue;
                if (!inverse){
                    inverse = true;
                    index = 0;
                    continue lines;
                } else return false;
            }
            index++;
        }
        return true;
    }
}
