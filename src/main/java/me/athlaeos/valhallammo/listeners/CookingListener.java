package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCookingRecipe;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.EquipmentClass;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.skills.skills.implementations.power.PowerProfile;
import me.athlaeos.valhallammo.skills.skills.implementations.smithing.SmithingItemPropertyManager;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Campfire;
import org.bukkit.block.Furnace;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;

import java.util.*;

public class CookingListener implements Listener {
    private final Map<Block, Map<Integer, DynamicCookingRecipe>> campfireRecipes = new HashMap<>();

    private static final Collection<InventoryType> furnaces = Set.of(InventoryType.FURNACE, InventoryType.BLAST_FURNACE, InventoryType.SMOKER);

    // owner is set to furnace on inventory click
    @EventHandler
    public void furnaceOwnerTracker(InventoryClickEvent e){
        if (furnaces.contains(e.getView().getTopInventory().getType())){
            Location l = e.getView().getTopInventory().getLocation();
            if (l != null) BlockUtils.setOwner(l.getBlock(), e.getWhoClicked().getUniqueId());
        }
    }

    @EventHandler
    public void onCampfireClick(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if (e.useItemInHand() != Event.Result.DENY && e.getClickedBlock() != null &&
                e.getAction() == Action.RIGHT_CLICK_BLOCK &&
                (e.getClickedBlock().getState() instanceof Campfire c)){
            if (!Timer.isCooldownPassed(p.getUniqueId(), "delay_campfire_interact_events")){
                e.setCancelled(true);
                return;
            }
            BlockUtils.setOwner(e.getClickedBlock(), p.getUniqueId());

            int firstEmpty = firstEmpty(c);

            if (firstEmpty >= 0){
                ItemBuilder handItem = new ItemBuilder(p.getInventory().getItemInMainHand());
                Pair<CampfireRecipe, DynamicCookingRecipe> recipes = getCampfireRecipe(handItem.getItem());
                // assertion: the DynamicCookingRecipe(two) is null if CampfireRecipe(one) is also null
                // and CampfireRecipe cannot be null if DynamicCookingRecipe also isn't null

                if (recipes.getOne() == null){ // if no campfire recipe with this item was found, try for the off-hand
                    handItem = new ItemBuilder(p.getInventory().getItemInOffHand());
                    recipes = getCampfireRecipe(handItem.getItem());
                }
                if (recipes.getOne() == null) {
                    e.setCancelled(true);
                    return; // no recipe was found at all, so do nothing
                }
                if (recipes.getTwo() == null) {
                    if (CustomRecipeRegistry.getDisabledRecipes().contains(recipes.getOne().getKey())) e.setCancelled(true);
                    return;// vanilla recipe found, cancel if recipe is disabled
                }
                DynamicCookingRecipe recipe = recipes.getTwo();
                PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
                if (profile == null ||
                        (recipe.getValidations().stream().anyMatch(v -> {
                            Validation validation = ValidationRegistry.getValidation(v);
                            if (validation != null) {
                                boolean invalid = !validation.validate(c.getLocation().getBlock());
                                if (invalid) Utils.sendActionBar(p, validation.validationError());
                                return invalid;
                            }
                            return false;
                        })) ||
                        (ValhallaMMO.isWorldBlacklisted(c.getWorld().getName())) ||
                        (WorldGuardHook.inDisabledRegion(c.getLocation(), WorldGuardHook.VMMO_CRAFTING_CAMPFIRE)) ||
                        (!p.hasPermission("valhalla.allrecipes") && !recipe.isUnlockedForEveryone() && !profile.getUnlockedRecipes().contains(recipe.getName()))) {
                    // If the the player's profile is null, the player hasn't unlocked the recipe,
                    // the world is blacklisted, any of the validations failed, or the location is in a region
                    // which blocks custom recipes, cancel campfire interaction
                    e.setCancelled(true);
                    return;
                }

                Map<Integer, DynamicCookingRecipe> campfireContents = campfireRecipes.getOrDefault(c.getBlock(), new HashMap<>());
                if (recipe.requireValhallaTools()){
                    if (EquipmentClass.getMatchingClass(handItem.getMeta()) != null && !SmithingItemPropertyManager.hasSmithingQuality(handItem.getMeta())){
                        // item needs to be custom, but isn't
                        e.setCancelled(true);
                        return;
                    }
                }

                ItemBuilder result = new ItemBuilder(recipe.tinker() ? handItem.getItem() : recipe.getResult());

                DynamicItemModifier.modify(result, p, recipe.getModifiers(), false, false, true);
                if (ItemUtils.isEmpty(result.getItem()) || CustomFlag.hasFlag(result.getMeta(), CustomFlag.UNCRAFTABLE)){
                    Timer.setCooldown(e.getPlayer().getUniqueId(), 500, "delay_dynamic_campfire_attempts");
                    e.setCancelled(true);
                    return;
                }

                campfireContents.put(firstEmpty, recipe);
                campfireRecipes.put(c.getBlock(), campfireContents);
            } else e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFurnaceBurn(FurnaceBurnEvent e){
        Block b = e.getBlock();
        if (b.getBlockData() instanceof Furnace f && !e.isCancelled()){
            Pair<CookingRecipe<?>, DynamicCookingRecipe> recipes = getFurnaceRecipe(f.getInventory().getSmelting());
            if (recipes.getOne() == null){
                e.setCancelled(true);
                return;
            }
            if (recipes.getTwo() == null) {
                if (CustomRecipeRegistry.getDisabledRecipes().contains(recipes.getOne().getKey())) e.setCancelled(true);
                return;// vanilla recipe found, cancel if recipe is disabled
            }
            DynamicCookingRecipe recipe = recipes.getTwo();
            if (!ItemUtils.isEmpty(f.getInventory().getSmelting())){
                ItemBuilder result = new ItemBuilder(recipe.tinker() ? f.getInventory().getSmelting() : recipe.getResult());

                Player owner = BlockUtils.getOwner(b);
                if (owner != null){
                    PowerProfile profile = ProfileCache.getOrCache(owner, PowerProfile.class);
                    if (profile == null ||
                            (!owner.hasPermission("valhalla.allrecipes") && !recipe.isUnlockedForEveryone() && !profile.getUnlockedRecipes().contains(recipe.getName()))) {
                        // If the the player's profile is null, the player hasn't unlocked the recipe,
                        // the world is blacklisted, any of the validations failed, or the location is in a region
                        // which blocks custom recipes, cancel campfire interaction
                        e.setCancelled(true);
                        return;
                    }
                }
                if ((recipe.getModifiers().stream().anyMatch(DynamicItemModifier::requiresPlayer) && owner == null) ||
                        (ValhallaMMO.isWorldBlacklisted(f.getWorld().getName())) ||
                        (WorldGuardHook.inDisabledRegion(f.getLocation(), WorldGuardHook.VMMO_CRAFTING_CAMPFIRE)) ||
                        (recipe.getValidations().stream().anyMatch(v -> {
                            Validation validation = ValidationRegistry.getValidation(v);
                            if (validation != null) {
                                boolean invalid = !validation.validate(f.getLocation().getBlock());
                                if (invalid && owner != null) {
                                    Utils.sendActionBar(owner, validation.validationError());
                                }
                                return invalid;
                            }
                            return false;
                        }))){
                    e.setCancelled(true);
                    f.getWorld().playEffect(f.getLocation(), Effect.EXTINGUISH, 0);
                    return;
                }
                DynamicItemModifier.modify(result, owner, recipe.getModifiers(), false, false, true);

                if (ItemUtils.isEmpty(result.getItem()) || CustomFlag.hasFlag(result.getMeta(), CustomFlag.UNCRAFTABLE)){
                    if (owner != null) Timer.setCooldown(owner.getUniqueId(), 500, "delay_furnace_attempts");
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFurnaceStart(FurnaceStartSmeltEvent e){
        if (e.getBlock().getState() instanceof Furnace){
            DynamicCookingRecipe recipe = CustomRecipeRegistry.getCookingRecipesByKey().get(e.getRecipe().getKey());
            if (recipe == null) {
                if (CustomRecipeRegistry.getDisabledRecipes().contains(e.getRecipe().getKey())) {
                    e.setTotalCookTime(Integer.MAX_VALUE);
                    return;
                }
            } else {
                Recipe r = ValhallaMMO.getInstance().getServer().getRecipe(recipe.getKey());
                if (!(r instanceof CookingRecipe<?>)) throw new IllegalStateException("Recipe linked to dynamic cooking recipe key is not a cooking recipe");
            }
            Player owner = BlockUtils.getOwner(e.getBlock());
            double cookTimeMultiplier = owner == null ? 1 : 2; // TODO AccumulativeStatManager.getCachedStats("COOKING_SPEED", owner, 10000, true);
            e.setTotalCookTime((int) (cookTimeMultiplier <= 0 ? Integer.MAX_VALUE : e.getRecipe().getCookingTime() / cookTimeMultiplier));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCook(BlockCookEvent e){
        if (e.isCancelled()) return;
        Block b = e.getBlock();
        Player owner = BlockUtils.getOwner(b);
        if (b.getState() instanceof Campfire c){
            int finishedSlot = getFinishedCampfireCook(c);
            if (finishedSlot >= 0){
                Map<Integer, DynamicCookingRecipe> campfireRecipes = this.campfireRecipes.getOrDefault(b, new HashMap<>());
                DynamicCookingRecipe recipe = campfireRecipes.get(finishedSlot);
                ItemStack finishedItem = c.getItem(finishedSlot);
                if (recipe == null || ItemUtils.isEmpty(finishedItem)) return; // vanilla or failed recipe
                ItemBuilder result = new ItemBuilder(recipe.tinker() ? finishedItem : recipe.getResult());
                if (ItemUtils.isEmpty(result.getItem()) ||
                        (recipe.getModifiers().stream().anyMatch(DynamicItemModifier::requiresPlayer) && owner == null) ||
                        (recipe.getValidations().stream().anyMatch(v -> {
                            Validation validation = ValidationRegistry.getValidation(v);
                            if (validation != null) {
                                boolean invalid = !validation.validate(b);
                                if (invalid && owner != null) Utils.sendActionBar(owner, validation.validationError());
                                return invalid;
                            }
                            return false;
                        }))){
                    // TODO eject items off of campfire, this should never occur though
                    c.getWorld().playEffect(c.getLocation(), Effect.EXTINGUISH, 0);
                    e.setCancelled(true);
                    return;
                }

                DynamicItemModifier.modify(result, owner, recipe.getModifiers(), false, true, true);
                TranslationManager.translateItemMeta(result.getMeta());
                if (ItemUtils.isEmpty(result.getItem()) || CustomFlag.hasFlag(result.getMeta(), CustomFlag.UNCRAFTABLE)){
                    // TODO eject items off campfire
                    c.getWorld().playEffect(c.getLocation(), Effect.EXTINGUISH, 0);
                    e.setCancelled(true);
                    return;
                } else {
                    int expReward = Utils.randomAverage(recipe.getExperience());
                    if (expReward > 0){
                        Location dropLocation = c.getLocation().add(0.5, 0.5, 0.5);
                        ExperienceOrb exp = c.getWorld().spawn(dropLocation, ExperienceOrb.class);
                        exp.setExperience(expReward);
                    }
                    e.setResult(result.get());

                    recipe.getValidations().forEach(v -> {
                        Validation validation = ValidationRegistry.getValidation(v);
                        if (validation != null) validation.execute(c.getBlock());
                    });
                }

                campfireRecipes.remove(finishedSlot);
                this.campfireRecipes.put(b, campfireRecipes);
            } else {
                e.setCancelled(true);
                // TODO eject items off of campfire
            }
        } else if (e.getBlock().getState() instanceof Furnace f){
            Pair<CookingRecipe<?>, DynamicCookingRecipe> recipes = getFurnaceRecipe(f.getInventory().getSmelting());
            if (recipes.getOne() == null){
                e.setCancelled(true);
                return;
            }
            if (recipes.getTwo() == null) {
                if (CustomRecipeRegistry.getDisabledRecipes().contains(recipes.getOne().getKey())) e.setCancelled(true);
                return;// vanilla recipe found, cancel if recipe is disabled
            }
            DynamicCookingRecipe recipe = recipes.getTwo();
            if (ItemUtils.isEmpty(f.getInventory().getSmelting())) return;
            ItemBuilder result = new ItemBuilder(recipe.tinker() ? f.getInventory().getSmelting() : recipe.getResult());
            if (recipe.tinker()){
                if (recipe.requireValhallaTools() && EquipmentClass.getMatchingClass(result.getMeta()) != null && !SmithingItemPropertyManager.hasSmithingQuality(result.getMeta())) return;
            }

            if (ItemUtils.isEmpty(result.getItem()) ||
                    (recipe.getModifiers().stream().anyMatch(DynamicItemModifier::requiresPlayer) && owner == null) ||
                    (recipe.getValidations().stream().anyMatch(v -> {
                        Validation validation = ValidationRegistry.getValidation(v);
                        if (validation != null) {
                            boolean invalid = !validation.validate(b);
                            if (invalid && owner != null) Utils.sendActionBar(owner, validation.validationError());
                            return invalid;
                        }
                        return false;
                    }))){
                // TODO eject items out of furnace, this should never occur though
                f.getWorld().playEffect(f.getLocation(), Effect.EXTINGUISH, 0);
                e.setCancelled(true);
                return;
            }

            DynamicItemModifier.modify(result, owner, recipe.getModifiers(), false, true, true);
            TranslationManager.translateItemMeta(result.getMeta());
            if (ItemUtils.isEmpty(result.getItem()) || CustomFlag.hasFlag(result.getMeta(), CustomFlag.UNCRAFTABLE)){
                // TODO eject items out of furnace
                f.getWorld().playEffect(f.getLocation(), Effect.EXTINGUISH, 0);
                e.setCancelled(true);
            } else {
                e.setResult(result.get());
                recipe.getValidations().forEach(v -> {
                    Validation validation = ValidationRegistry.getValidation(v);
                    if (validation != null) validation.execute(f.getBlock());
                });
            }
        }
    }

    private int firstEmpty(Campfire campfire){
        if (ItemUtils.isEmpty(campfire.getItem(0))) return 0;
        if (ItemUtils.isEmpty(campfire.getItem(1))) return 1;
        if (ItemUtils.isEmpty(campfire.getItem(2))) return 2;
        if (ItemUtils.isEmpty(campfire.getItem(3))) return 3;
        return -1;
    }

    private int getFinishedCampfireCook(Campfire campfire){
        for (int i = 0; i < 4; i++) if (!ItemUtils.isEmpty(campfire.getItem(i)) && campfire.getCookTime(i) > 0 && campfire.getCookTime(i) == campfire.getCookTimeTotal(i)) return i;
        return -1;
    }

    public static Map<String, Pair<CampfireRecipe, DynamicCookingRecipe>> campfireRecipeCache = new HashMap<>();
    public static Map<String, Pair<CookingRecipe<?>, DynamicCookingRecipe>> furnaceRecipeCache = new HashMap<>();

    private Pair<CampfireRecipe, DynamicCookingRecipe> getCampfireRecipe(ItemStack i){
        if (ItemUtils.isEmpty(i)) return new Pair<>(null, null);
        ItemStack clone = i.clone();
        clone.setAmount(1);
        if (campfireRecipeCache.containsKey(clone.toString())) return campfireRecipeCache.get(clone.toString());
        Iterator<Recipe> iterator = ValhallaMMO.getInstance().getServer().recipeIterator();
        CampfireRecipe found = null;
        while (iterator.hasNext()){
            if (iterator.next() instanceof CampfireRecipe c){
                if (c.getInputChoice().test(i)){
                    found = c;
                    DynamicCookingRecipe dynamicRecipe = CustomRecipeRegistry.getCookingRecipesByKey().get(c.getKey());
                    if (dynamicRecipe != null && dynamicRecipe.getType() == DynamicCookingRecipe.CookingRecipeType.CAMPFIRE) {
                        Pair<CampfireRecipe, DynamicCookingRecipe> match = new Pair<>(found, dynamicRecipe);
                        campfireRecipeCache.put(clone.toString(), match);
                        return match;
                    }
                }
            }
        }
        return new Pair<>(found, null);
    }

    private Pair<CookingRecipe<?>, DynamicCookingRecipe> getFurnaceRecipe(ItemStack i){
        if (ItemUtils.isEmpty(i)) return new Pair<>(null, null);
        ItemStack clone = i.clone();
        clone.setAmount(1);
        if (furnaceRecipeCache.containsKey(clone.toString())) return furnaceRecipeCache.get(clone.toString());
        Iterator<Recipe> iterator = ValhallaMMO.getInstance().getServer().recipeIterator();
        CookingRecipe<?> found = null;
        while (iterator.hasNext()){
            Recipe next = iterator.next();
            if (next instanceof FurnaceRecipe || next instanceof BlastingRecipe || next instanceof SmokingRecipe){
                CookingRecipe<?> r = (CookingRecipe<?>) next;
                if (r.getInputChoice().test(i)){
                    found = r;
                    DynamicCookingRecipe dynamicRecipe = CustomRecipeRegistry.getCookingRecipesByKey().get(r.getKey());
                    if (dynamicRecipe != null && dynamicRecipe.getType() != DynamicCookingRecipe.CookingRecipeType.CAMPFIRE) {
                        Pair<CookingRecipe<?>, DynamicCookingRecipe> match = new Pair<>(found, dynamicRecipe);
                        furnaceRecipeCache.put(clone.toString(), match);
                        return match;
                    }
                }
            }
        }
        return new Pair<>(found, null);
    }
}
