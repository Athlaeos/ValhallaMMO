package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.ToolRequirementType;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicGridRecipe;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.CustomDurabilityManager;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.EntityEffect;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.*;

public class CraftingTableListener implements Listener {
    private static final Map<UUID, Map<Integer, ItemMeta>> matrixMetaCache = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraft(CraftItemEvent e){
        if (e.getWhoClicked() instanceof Player crafter && (e.getRecipe() instanceof ShapedRecipe || e.getRecipe() instanceof ShapelessRecipe)){
            DynamicGridRecipe recipe = CustomRecipeRegistry.getGridRecipesByKey().get(((Keyed) e.getRecipe()).getKey());
            if (recipe == null) return; // not a valhalla recipe, don't do anything
            CraftingInventory inventory = e.getInventory();
            if ((inventory.getLocation() != null && inventory.getLocation().getWorld() != null && ValhallaMMO.isWorldBlacklisted(inventory.getLocation().getWorld().getName())) ||
                    (WorldGuardHook.inDisabledRegion(inventory.getLocation(), WorldGuardHook.VMMO_CRAFTING_CRAFTINGTABLE))) {
                // If the recipe or the player's profile are null, the player hasn't unlocked
                // the recipe, the world is blacklisted, or the location is in a region which blocks custom recipes,
                // nullify result
                e.setCancelled(true);
                return;
            }
            PowerProfile profile = ProfileCache.getOrCache(crafter, PowerProfile.class);
            ItemStack result = verifyIngredients(crafter, profile, recipe, inventory.getMatrix());
            if (ItemUtils.isEmpty(result)){
                Pair<DynamicGridRecipe, ItemStack> corrected = correctRecipe(crafter, profile, recipe, inventory.getMatrix());
                if (corrected == null){
                    e.setCancelled(true);
                    return;
                }
                recipe = corrected.getOne();
                result = corrected.getTwo().clone();
            }

            ItemBuilder prepareResultBuilder = new ItemBuilder(result);
            DynamicItemModifier.modify(prepareResultBuilder, (Player) e.getWhoClicked(), recipe.getModifiers(), false, false, true, 1);
            if (CustomFlag.hasFlag(prepareResultBuilder.getMeta(), CustomFlag.UNCRAFTABLE)) {
                e.setCancelled(true);
                return;
            }
            inventory.setResult(prepareResultBuilder.get());

            PlayerInventory playerInventory = crafter.getInventory();
            ClickType clickType = e.getClick();
            int amountCrafted = 1;
            boolean toolRequired = recipe.getToolRequirement().getToolRequirementType() != ToolRequirementType.NOT_REQUIRED &&
                    recipe.getToolRequirement().getRequiredToolID() >= 0;

            boolean verifyClicks = false;
            switch (clickType){
                case DROP, CONTROL_DROP -> {
                    if (!ItemUtils.isEmpty(e.getCursor())){
                        e.setCancelled(true);
                        return; // TODO hotfix, dont yet know why crafting with the drop key while holding an item on the cursor doesnt actually spit out an item and doesnt consume ingredients while still rewarding exp
                    }
                } // do nothing special because these actions do not require empty inventory space
                case LEFT -> {
                    if (!ItemUtils.isEmpty(e.getCursor()) && !ItemUtils.isEmpty(inventory.getResult()) &&
                            (!e.getCursor().isSimilar(inventory.getResult()) ||
                                    (e.getCursor().getAmount() + inventory.getResult().getAmount() > e.getCursor().getType().getMaxStackSize()))){
                        // cursor cannot stack with result item, do not proceed
                        e.setCancelled(true);
                        return;
                    }
                    verifyClicks = true;
                }
                case SHIFT_LEFT, SHIFT_RIGHT -> {
                    // calculate how many items can be crafted

                    // the max amount of items the player could craft if they have enough inventory space,
                    int maxCraftable = 64;
                    Map<Integer, ItemMeta> matrixMeta = matrixMetaCache.getOrDefault(e.getWhoClicked().getUniqueId(), new HashMap<>());
                    for (int i = 0; i < inventory.getMatrix().length; i++){
                        ItemStack slot = inventory.getMatrix()[i];
                        if (ItemUtils.isEmpty(slot) || toolRequired &&
                                ToolRequirementType.getToolID(matrixMeta.get(i)) >= 0) continue;
                        if (maxCraftable > slot.getAmount()) maxCraftable = slot.getAmount();
                    }

                    int available = ItemUtils.maxInventoryFit(crafter, result); // max items available to fit in inventory
                    amountCrafted = Math.min(available, maxCraftable);
                    if (amountCrafted == 0) {
                        e.setCancelled(true);
                        return;
                    }
                }
                default -> {
                    e.setCancelled(true);
                    return;
                }
            }

            if (toolRequired){
                // A tool is required, so the matching item should be damaged and removed if broken
                if (recipe.getToolIndex() >= 0){
                    // the tool is required to be part of the recipe itself
                    int toolIndex = -1;
                    List<ItemStack> matrix = Arrays.asList(inventory.getMatrix());
                    for (ItemStack i : inventory.getMatrix()){
                        if (!ItemUtils.isEmpty(i) &&
                                recipe.getToolRequirement().canCraft(ToolRequirementType.getToolID(ItemUtils.getItemMeta(i)))){
                            toolIndex = matrix.indexOf(i);
                            break;
                        }
                    }
                    if (toolIndex < 0) {
                        // no usable tool found
                        e.setCancelled(true);
                        return;
                    } else {
                        ItemStack tool = ItemUtils.damageItem(crafter, inventory.getMatrix()[toolIndex], amountCrafted, EntityEffect.BREAK_EQUIPMENT_MAIN_HAND) ?
                                null : inventory.getMatrix()[toolIndex].clone();
                        inventory.getMatrix()[toolIndex].setAmount(amountCrafted);
                        int finalToolIndex = toolIndex;
                        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                            ItemStack[] m = inventory.getMatrix();
                            m[finalToolIndex] = tool;
                            inventory.setMatrix(m);
                            }, 1L
                        );
                    }
                } else {
                    // the tool is required to be in the player's inventory instead
                    int matchingIndex = -1;
                    for (int i = 0; i < playerInventory.getContents().length; i++){
                        ItemStack item = playerInventory.getContents()[i];
                        if (!ItemUtils.isEmpty(item) &&
                                recipe.getToolRequirement().canCraft(ToolRequirementType.getToolID(ItemUtils.getItemMeta(item)))){
                            matchingIndex = i;
                            break;
                        }
                    }
                    if (matchingIndex < 0){
                        e.setCancelled(true);
                        return;
                    }
                    ItemStack foundItem = crafter.getInventory().getItem(matchingIndex);
                    if (!ItemUtils.isEmpty(foundItem) && ItemUtils.damageItem(crafter, foundItem, amountCrafted, EntityEffect.BREAK_EQUIPMENT_MAIN_HAND, true)){
                        crafter.getInventory().setItem(matchingIndex, null);
                    }
                }
            }
            ItemBuilder resultBuilder = new ItemBuilder(result);
            DynamicItemModifier.modify(resultBuilder, (Player) e.getWhoClicked(), recipe.getModifiers(), false, true, true, amountCrafted);
            if (CustomFlag.hasFlag(resultBuilder.getMeta(), CustomFlag.UNCRAFTABLE)) {
                e.setCancelled(true);
                return;
            }
            ItemStack finalResult = resultBuilder.get();
            if (verifyClicks){
                // check if cursor can accept the result item
                ItemStack cursor = e.getCursor();
                if (!ItemUtils.isEmpty(cursor)){
                    // cancel crafting if the result no longer fits in the cursor
                    if (!cursor.isSimilar(finalResult) || cursor.getAmount() + finalResult.getAmount() > cursor.getType().getMaxStackSize()){
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            inventory.setResult(finalResult);
            if (!ItemUtils.isEmpty(result)){
                recipe.getValidations().forEach(v -> {
                    Validation validation = ValidationRegistry.getValidation(v);
                    if (inventory.getLocation() == null || validation == null) return;
                    validation.execute(inventory.getLocation().getBlock());
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(PrepareItemCraftEvent e){
        Recipe crafted = e.getRecipe();
        CraftingInventory inventory = e.getInventory();
        if (crafted == null || (crafted instanceof Keyed k && CustomRecipeRegistry.getDisabledRecipes().contains(k.getKey()))){
            // if the recipe is null, the recipe is disabled, or if the recipe is a repair recipe using custom items,
            // nullify result
            inventory.setResult(null);
            return;
        }
        Player crafter = (Player) e.getViewers().get(0);

        Map<Integer, ItemMeta> matrixMeta = new HashMap<>();
        // mapping meta to matrix items, so they dont need to be fetched several times per event
        for (int i = 0; i < inventory.getMatrix().length; i++){
            ItemStack slot = inventory.getMatrix()[i];
            if (ItemUtils.isEmpty(slot)) continue;
            matrixMeta.put(i, ItemUtils.getItemMeta(slot));
        }
        matrixMetaCache.put(crafter.getUniqueId(), matrixMeta);

        boolean isRepair = e.isRepair() || Arrays.stream(e.getInventory().getMatrix()).filter(i -> !ItemUtils.isEmpty(i) && i.getType().getMaxDurability() > 0).count() >= 2;
        if (isRepair){
            boolean anyCustom = false;
            boolean anyNotCustom = true;
            for (int i = 0; i < inventory.getMatrix().length; i++){
                ItemStack slot = inventory.getMatrix()[i];
                if (ItemUtils.isEmpty(slot)) continue;
                ItemMeta cachedMeta = matrixMeta.get(i);
                if (cachedMeta == null) continue;
                if (!anyCustom && SmithingItemPropertyManager.hasSmithingQuality(cachedMeta)) anyCustom = true;
                if (anyNotCustom && SmithingItemPropertyManager.hasSmithingQuality(cachedMeta)) anyNotCustom = false;
            }
            boolean incompatible = anyCustom && anyNotCustom; // if any of them aren't custom yet also some ARE custom, tools arent combinable

            // very ugly implementation, I know, but either ALL tools or NONE of the tools are allowed to be custom. anything in-between is invalid
            if (!incompatible){
                // is a repair recipe where either all of the items are custom or none of them are
                if (ItemUtils.isEmpty(inventory.getResult())) return; // result for whatever reason is null
                if (!ItemUtils.allMatchInTypeAndData(Arrays.asList(inventory.getMatrix()))) {
                    // items used in matrix do not all match in same material and custom model data, meaning they are considered different items
                    inventory.setResult(null);
                    return;
                }
                int combinedDurability = 0;
                int firstItemMaxDurability = -1;
                ItemStack firstItem = null;
                ItemMeta firstMeta = null;
                for (int i = 0; i < inventory.getMatrix().length; i++){
                    ItemStack item = inventory.getMatrix()[i];
                    if (!ItemUtils.isEmpty(item)){
                        ItemMeta cachedMeta = matrixMeta.get(i);
                        if (cachedMeta == null) continue;
                        if (firstItemMaxDurability < 0) {
                            firstItemMaxDurability = CustomDurabilityManager.getDurability(cachedMeta, true);
                            firstItem = item.clone();
                            firstMeta = cachedMeta;
                        }
                        combinedDurability += CustomDurabilityManager.getDurability(cachedMeta, false);
                    }
                }
                // first non-empty item will be used as result
                if (!ItemUtils.isEmpty(firstItem)){
                    firstItem.getEnchantments().keySet().forEach(firstItem::removeEnchantment);
                    int newDurability = Math.min(combinedDurability + (int) Math.floor(0.05 * firstItemMaxDurability), firstItemMaxDurability);
                    CustomDurabilityManager.setDurability(firstMeta, newDurability, firstItemMaxDurability);
                    ItemUtils.setItemMeta(firstItem, firstMeta);
                    inventory.setResult(firstItem);
                }
            } else {
                // is a repair recipe where only some of the items have custom quality, should be nullified
                inventory.setResult(null);
            }
            return;
        }

        if (crafted instanceof Keyed k && !CustomRecipeRegistry.getGridRecipesByKey().containsKey(k.getKey())) return;

        if ((crafted instanceof ShapedRecipe || crafted instanceof ShapelessRecipe) && !e.getViewers().isEmpty()){
            DynamicGridRecipe recipe = CustomRecipeRegistry.getGridRecipesByKey().get(((Keyed) crafted).getKey());
            if (recipe == null) {
                return; // not a valhalla recipe, don't do anything
            }
            PowerProfile profile = ProfileCache.getOrCache(crafter, PowerProfile.class);

            ItemStack result = verifyIngredients(crafter, profile, recipe, inventory.getMatrix());
            if (ItemUtils.isEmpty(result)){
                Pair<DynamicGridRecipe, ItemStack> corrected = correctRecipe(crafter, profile, recipe, inventory.getMatrix());
                if (corrected == null){
                    inventory.setResult(null);
                    return;
                }
                recipe = corrected.getOne();
                result = corrected.getTwo();
            }
            if (recipe.getValidations().stream().anyMatch(v -> {
                        Validation validation = ValidationRegistry.getValidation(v);
                        if (inventory.getLocation() == null) return false;
                        if (validation != null) {
                            boolean invalid = !validation.validate(inventory.getLocation().getBlock());
                            if (invalid) Utils.sendMessage(crafter, validation.validationError());
                            return invalid;
                        }
                        return false;
                    }) ||
                    (inventory.getLocation() != null &&
                            inventory.getLocation().getWorld() != null &&
                            ValhallaMMO.isWorldBlacklisted(inventory.getLocation().getWorld().getName())) ||
                    (WorldGuardHook.inDisabledRegion(inventory.getLocation(), WorldGuardHook.VMMO_CRAFTING_CRAFTINGTABLE)) ||
                    (!crafter.hasPermission("valhalla.allrecipes") && !recipe.isUnlockedForEveryone() && !profile.getUnlockedRecipes().contains(recipe.getName()))) {
                // If the recipe or the player's profile are null, the player hasn't unlocked the recipe,
                // the world is blacklisted, any of the validations failed, or the location is in a region
                // which blocks custom recipes, nullify result
                inventory.setResult(null);
                return;
            }

            ItemBuilder resultBuilder = new ItemBuilder(result);
            DynamicItemModifier.modify(resultBuilder, crafter, recipe.getModifiers(), false, false, true);
            inventory.setResult(resultBuilder.get());
        } else if (crafted instanceof ComplexRecipe r){
            if (r.getKey().getKey().equals("tipped_arrow") && !ItemUtils.isEmpty(inventory.getResult()) && Arrays.stream(inventory.getMatrix()).noneMatch(ItemUtils::isEmpty)){
                ItemStack[] m = inventory.getMatrix();
                if (m[0].isSimilar(m[1]) && m[0].isSimilar(m[2]) && m[0].isSimilar(m[3])
                        && m[0].isSimilar(m[5]) && m[0].isSimilar(m[6]) && m[0].isSimilar(m[7])
                        && m[0].isSimilar(m[8])){
                    // arrows are similar to eachother, proceed
                    ItemStack arrow = m[0].clone();
                    arrow.setAmount(8);
                    arrow.setType(Material.TIPPED_ARROW);
                    PotionMeta arrowMeta = (PotionMeta) ItemUtils.getItemMeta(arrow);
                    ItemStack potion = m[4].clone();
                    PotionMeta potionMeta = (PotionMeta) ItemUtils.getItemMeta(potion);
                    Map<String, PotionEffectWrapper> defaultEffects = PotionEffectRegistry.getStoredEffects(potionMeta, true);
                    Map<String, PotionEffectWrapper> actualEffects = PotionEffectRegistry.getStoredEffects(potionMeta, false);
                    if (defaultEffects.isEmpty()) return; // potion is vanilla
                    PotionEffectRegistry.setDefaultStoredEffects(arrowMeta, defaultEffects);
                    PotionEffectRegistry.setActualStoredEffects(arrowMeta, actualEffects);
                    PotionEffectRegistry.updateItemName(arrowMeta, true, false);

                    if (arrowMeta != null && potionMeta != null){
                        arrowMeta.setColor(potionMeta.getColor());
                        arrowMeta.addItemFlags(potionMeta.getItemFlags().toArray(new org.bukkit.inventory.ItemFlag[0]));
                        ItemUtils.setItemMeta(arrow, arrowMeta);
                    }

                    inventory.setResult(arrow);
                } else {
                    // arrows are not similar to each other, nullify result
                    inventory.setResult(null);
                }
            }
        }
    }

    private ItemStack verifyIngredients(Player clicker, PowerProfile profile, DynamicGridRecipe recipe, ItemStack[] matrix){
        ItemStack result = recipe.getResult().clone();
        if (!clicker.hasPermission("valhalla.allrecipes") && !recipe.isUnlockedForEveryone() && !profile.getUnlockedRecipes().contains(recipe.getName())) return null;
        Map<Integer, ItemMeta> matrixMeta = matrixMetaCache.getOrDefault(clicker.getUniqueId(), new HashMap<>());
        if (recipe.tinker()){
            SlotEntry tinkerEntry = recipe.getGridTinkerEquipment();
            for (int i = 0; i < matrix.length; i++){
                ItemStack slot = matrix[i];
                // finding item to tinker
                if (ItemUtils.isEmpty(slot)) continue;
                ItemMeta meta = matrixMeta.get(i);
                if (meta == null) continue;

                // If the recipe requires valhalla tools, the item is a tool, but has no
                // custom smithing quality, return null
                if (recipe.requireValhallaTools() &&
                        EquipmentClass.getMatchingClass(meta) != null &&
                        !SmithingItemPropertyManager.hasSmithingQuality(meta)) return null;

                if (defaultChoice(tinkerEntry).matches(tinkerEntry.getItem(), slot)){
                    result = slot.clone();
                    break;
                }
            }
        }

        List<SlotEntry> allIngredients = new ArrayList<>(recipe.getItems().values());
        SlotEntry toolEntry = recipe.getItems().get(recipe.getToolIndex());
        for (int i = 0; i < matrix.length; i++){
            ItemStack slot = matrix[i];
            // finding item to tinker
            if (ItemUtils.isEmpty(slot)) continue;
            ItemMeta meta = matrixMeta.get(i);
            if (meta == null) continue;

            for (SlotEntry entry : new ArrayList<>(allIngredients)){
                // If the item is either a matching tool
                if (toolEntry != null){
                    if (recipe.getToolRequirement().getToolRequirementType() == ToolRequirementType.NONE_MANDATORY){
                        // It is mandatory no tool is present. If there is, return null
                        if (ToolRequirementType.getToolID(meta) >= 0) return null;
                    } else if (recipe.getToolRequirement().getToolRequirementType() != ToolRequirementType.NOT_REQUIRED &&
                            recipe.getToolRequirement().getRequiredToolID() >= 0){
                        // A tool is required, so if the item matches the tool requirement remove it from the list as well
                        if (recipe.getToolRequirement().canCraft(ToolRequirementType.getToolID(meta))){
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
        return result;
    }

    private IngredientChoice defaultChoice(SlotEntry entry){
        return Objects.requireNonNullElse(entry.getOption(), new MaterialChoice());
    }

    private Pair<DynamicGridRecipe, ItemStack> correctRecipe(Player crafter, PowerProfile profile, DynamicGridRecipe original, ItemStack[] grid){
        int ingredients = original.getItems().size();
        ItemStack originalCorrect = verifyIngredients(crafter, profile, original, grid);
        if (!ItemUtils.isEmpty(originalCorrect)) return new Pair<>(original, originalCorrect);
        for (DynamicGridRecipe recipe : CustomRecipeRegistry.getGridRecipesByIngredientQuantity().getOrDefault(ingredients, new HashSet<>())){
            if (recipe.getName().equals(original.getName()) || (!original.isShapeless() && !recipe.isShapeless() && !matchesShape(original, recipe))) continue;
            ItemStack result = verifyIngredients(crafter, profile, recipe, grid);
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
