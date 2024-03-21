package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicSmithingRecipe;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.CustomDurabilityManager;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;

import java.util.*;

public class SmithingTableListener implements Listener {

    private final Map<UUID, SmithingAdditionInfo> smithingAdditionInfoMap = new HashMap<>();

    @EventHandler
    public void onSmithingInteract(InventoryClickEvent e){
        boolean isTemplateCompatible = MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20);
        if (e.getView().getTopInventory() instanceof SmithingInventory s){
            Player p = (Player) e.getWhoClicked();
            int templateIndex = isTemplateCompatible ? 0 : -1;
            int baseIndex = templateIndex + 1;
            int additionIndex = baseIndex + 1;
            int resultIndex = additionIndex + 1;
            if (e.getRawSlot() != resultIndex) return;
            ItemStack template = isTemplateCompatible ? s.getItem(templateIndex) : null;
            ItemStack rawBase = s.getItem(baseIndex);
            ItemBuilder base = ItemUtils.isEmpty(rawBase) ? null : new ItemBuilder(rawBase);
            ItemStack rawAddition = s.getItem(additionIndex);
            ItemBuilder addition = ItemUtils.isEmpty(rawAddition) ? null : new ItemBuilder(rawAddition);
            if (base == null || addition == null) return;
            if (!ItemUtils.isEmpty(template)) template = template.clone();

            Pair<SmithingRecipe, DynamicSmithingRecipe> recipes = getSmithingRecipe(template, base, addition);
            if (recipes == null){
                e.setCancelled(true);
                s.setResult(null);
                return;
            }
            if (recipes.getOne() == null) return; // no recipe was found at all, so do nothing
            if (recipes.getTwo() == null) {
                if (CustomRecipeRegistry.getDisabledRecipes().contains(recipes.getOne().getKey())) s.setResult(null);
                return;// vanilla recipe found, cancel if recipe is disabled
            }
            DynamicSmithingRecipe r = recipes.getTwo();
            int availableSpace = ItemUtils.maxInventoryFit(p, r.getResult());
            int crafted = 1;
            if (e.isShiftClick()){
                crafted = 64;
                // get the smallest amount in the inventory, this will calculate the amount crafted if the player shift clicks
                if (r.consumeAddition() && addition.getItem().getAmount() < crafted) crafted = addition.getItem().getAmount();
                if (base.getItem().getAmount() < crafted) crafted = base.getItem().getAmount();
                if (!ItemUtils.isEmpty(template) && template.getAmount() < crafted) crafted = template.getAmount();
                crafted = Math.min(crafted, availableSpace);
            }

            ItemBuilder result = new ItemBuilder(r.tinkerBase() ? base.getItem() : r.getResult());
            DynamicItemModifier.modify(result, addition, p, r.getResultModifiers(), false, true, true, e.isShiftClick() ? crafted : 1);
            if (ItemUtils.isEmpty(result.getItem()) || ItemUtils.isEmpty(addition.getItem())) {
                s.setResult(null);
                return;
            }

            if (!r.consumeAddition()){
                SmithingAdditionInfo additionPrediction = smithingAdditionInfoMap.get(e.getWhoClicked().getUniqueId());
                if (additionPrediction != null){
                    if (!r.getName().equals(additionPrediction.recipe().getName()) ||
                            !(additionPrediction.base().isSimilar(base.getItem()) &&
                                    additionPrediction.originalAddition().isSimilar(addition.getItem()))){
                        // if the recipe that was used when the addition item was cached doesn't
                        // match the current recipe, or the items don't match what they used to,
                        // cancel recipe
                        s.setResult(null);
                        smithingAdditionInfoMap.remove(e.getWhoClicked().getUniqueId());
                        return;
                    }
                    DynamicItemModifier.modify(addition, result, p, r.getAdditionModifiers(), false, true, true, crafted);
                    if (ItemUtils.isEmpty(result.getItem()) || ItemUtils.isEmpty(addition.getItem())) {
                        s.setResult(null);
                        return;
                    }
                    ItemStack slotItem = s.getItem(additionIndex);
                    if (!ItemUtils.isEmpty(slotItem)) slotItem.setAmount(crafted);
                    s.setResult(result.get());
                }
                ItemStack finalAddition = addition.get();
                smithingAdditionInfoMap.remove(e.getWhoClicked().getUniqueId());
                if (ItemUtils.isEmpty(finalAddition) || CustomFlag.hasFlag(addition.getMeta(), CustomFlag.UNCRAFTABLE) || ItemUtils.isEmpty(result.getItem()) || CustomFlag.hasFlag(result.getMeta(), CustomFlag.UNCRAFTABLE)){
                    e.setCancelled(true);
                } else {
                    ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () ->
                            e.getInventory().setItem(additionIndex, finalAddition), 1L
                    );
                }
            } else {
                if (ItemUtils.isEmpty(result.getItem()) || CustomFlag.hasFlag(result.getMeta(), CustomFlag.UNCRAFTABLE)) e.setCancelled(true);
                s.setResult(result.get());
            }

            r.getValidations().forEach(v -> {
                Validation validation = ValidationRegistry.getValidation(v);
                if (validation != null && s.getLocation() != null) validation.execute(s.getLocation().getBlock());
            });
        }
    }

    @SuppressWarnings("all")
    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent e){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            boolean isTemplateCompatible = MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20);

            int baseIndex = isTemplateCompatible ? 1 : 0;
            int templateIndex = baseIndex - 1;
            int additionIndex = baseIndex + 1;
            SmithingInventory s = e.getInventory();
            ItemStack template = isTemplateCompatible ? s.getItem(templateIndex) : null;
            ItemStack rawBase = s.getItem(baseIndex);
            ItemBuilder base = ItemUtils.isEmpty(rawBase) ? null : new ItemBuilder(rawBase);
            ItemStack rawAddition = s.getItem(additionIndex);
            ItemBuilder addition = ItemUtils.isEmpty(rawAddition) ? null : new ItemBuilder(rawAddition);
            if (base == null || addition == null) return;
            Pair<SmithingRecipe, DynamicSmithingRecipe> recipes = getSmithingRecipe(template, base, addition);
            if (recipes == null){
                e.setResult(null);
                return;
            }
            if (recipes.getOne() == null) {
                e.setResult(null);
                return; // no recipe was found at all, so do nothing
            }
            if (recipes.getTwo() == null) {
                if (CustomRecipeRegistry.getDisabledRecipes().contains(recipes.getOne().getKey())) {
                    e.setResult(null);
                }
                if (e.getResult() != null && SmithingItemPropertyManager.hasSmithingQuality(base.getMeta()) &&
                        base.getItem().getType() != e.getResult().getType()) e.setResult(null);
                return;// vanilla recipe found, cancel if recipe is disabled or if resulting item is custom and different from result item
            }
            DynamicSmithingRecipe recipe = recipes.getTwo();
            ItemStack originalAddition = rawAddition.clone();
            Player p = (Player) e.getView().getPlayer();
            PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
            if (profile == null ||
                    (recipe.getValidations().stream().anyMatch(v -> {
                        Validation validation = ValidationRegistry.getValidation(v);
                        if (validation != null && s.getLocation() != null) {
                            boolean invalid = !validation.validate(s.getLocation().getBlock());
                            if (invalid) Utils.sendActionBar(p, validation.validationError());
                            return invalid;
                        }
                        return false;
                    })) ||
                    (ValhallaMMO.isWorldBlacklisted(p.getWorld().getName())) ||
                    (WorldGuardHook.inDisabledRegion(p.getLocation(), WorldGuardHook.VMMO_CRAFTING_SMITHING)) ||
                    (!p.hasPermission("valhalla.allrecipes") && !recipe.isUnlockedForEveryone() && !profile.getUnlockedRecipes().contains(recipe.getName()))) {
                // If the the player's profile is null, the player hasn't unlocked the recipe,
                // the world is blacklisted, any of the validations failed, or the location is in a region
                // which blocks custom recipes, cancel campfire interaction
                e.setResult(null);
                return;
            }

            ItemBuilder result = new ItemBuilder(recipe.tinkerBase() ? base.getItem() : recipe.getResult());
            DynamicItemModifier.modify(result, addition, p, recipe.getResultModifiers(), false, false, true);
            if (ItemUtils.isEmpty(result.getItem()) || ItemUtils.isEmpty(addition.getItem())) {
                s.setResult(null);
                return;
            }
            if (!recipe.consumeAddition()){
                // the addition may be modified, but we can't modify the addition directly as this would allow
                // modification without crafting. The addition must therefore be cached along with the other
                // recipe specifications, we then later check if during the recipe execution the specifications
                // match or otherwise we cancel
                DynamicItemModifier.modify(addition, result, p, recipe.getAdditionModifiers(), false, false, true);
                if (ItemUtils.isEmpty(result.getItem()) || ItemUtils.isEmpty(addition.getItem())) {
                    s.setResult(null);
                    return;
                }
            }
            if (CustomDurabilityManager.getDurability(result.getMeta(), true) > 0 && CustomDurabilityManager.getDurability(result.getMeta(), false) <= 0) result = null;
            if (CustomDurabilityManager.getDurability(addition.getMeta(), true) > 0 && CustomDurabilityManager.getDurability(addition.getMeta(), false) <= 0) result = null;
            smithingAdditionInfoMap.put(p.getUniqueId(), new SmithingAdditionInfo(recipe, base.get(), originalAddition, addition.get()));
            s.setResult(result == null ? null : result.get());
        }, 1L);
    }

    public static final Map<String, Pair<SmithingRecipe, DynamicSmithingRecipe>> smithingRecipeCache = new HashMap<>();

    private Pair<SmithingRecipe, DynamicSmithingRecipe> getSmithingRecipe(ItemStack template, ItemBuilder base, ItemBuilder addition){
        if (base == null || addition == null) return new Pair<>(null, null);
        String key = key(template, base.getItem(), addition.getItem());
        if (smithingRecipeCache.containsKey(key)) return smithingRecipeCache.get(key);
        boolean isTemplateCompatible = MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20);
        Iterator<Recipe> iterator = ValhallaMMO.getInstance().getServer().recipeIterator();
        SmithingRecipe found = null;
        while (iterator.hasNext()){
            if (iterator.next() instanceof SmithingRecipe s && s.getBase().test(base.getItem()) && s.getAddition().test(addition.getItem())){
                found = s;
                DynamicSmithingRecipe dynamicRecipe = CustomRecipeRegistry.getSmithingRecipesByKey().get(s.getKey());
                if (dynamicRecipe != null && dynamicRecipe.getBase().getOption().matches(dynamicRecipe.getBase().getItem(), base.getItem()) &&
                        dynamicRecipe.getAddition().getOption().matches(dynamicRecipe.getAddition().getItem(), addition.getItem())) {
                    if (isTemplateCompatible && ItemUtils.isEmpty(template)) continue; // 1.20+ recipes need to be template compatible, and so templates cannot be null
                    // templates are considered matching if templates aren't in the game yet, if the dynamic recipe template is null,
                    // or if the dynamic template matches the template item
                    if (dynamicRecipe.requireValhallaTools() && (EquipmentClass.getMatchingClass(base.getMeta()) != null && !SmithingItemPropertyManager.hasSmithingQuality(base.getMeta()) ||
                            EquipmentClass.getMatchingClass(addition.getMeta()) != null && !SmithingItemPropertyManager.hasSmithingQuality(addition.getMeta()))){
                        continue;
                    }
                    boolean templatesMatch = !isTemplateCompatible || ((dynamicRecipe.getTemplate() == null || dynamicRecipe.getTemplate().getOption() == null || ItemUtils.isEmpty(dynamicRecipe.getTemplate().getItem())) ?
                            ItemUtils.isEmpty(template) :
                            dynamicRecipe.getTemplate().getOption().matches(dynamicRecipe.getTemplate().getItem(), template));
                    Pair<SmithingRecipe, DynamicSmithingRecipe> match = new Pair<>(found, dynamicRecipe);
                    if (templatesMatch) {
                        smithingRecipeCache.put(key, match);
                        return match;
                    }
                }
            }
        }
        // if a valhalla recipe is found but its items do not match, return null. null should mean the recipe should be cancelled
        if (found != null && CustomRecipeRegistry.getSmithingRecipesByKey().containsKey(found.getKey())) return null;
        return new Pair<>(found, null);
    }

    private String key(ItemStack template, ItemStack base, ItemStack addition){
        ItemStack tClone = ItemUtils.isEmpty(template) ? null : template.clone();
        if (tClone != null) tClone.setAmount(1);
        ItemStack bClone = base.clone();
        bClone.setAmount(1);
        ItemStack aClone = addition.clone();
        aClone.setAmount(1);
        return String.format("%s:%s:%s", ItemUtils.isEmpty(tClone) ? "" : tClone, bClone, aClone);
    }

    private record SmithingAdditionInfo(DynamicSmithingRecipe recipe, ItemStack base, ItemStack originalAddition, ItemStack newAddition){}
}
