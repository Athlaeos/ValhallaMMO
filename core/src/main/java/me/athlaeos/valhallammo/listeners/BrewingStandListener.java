package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicBrewingRecipe;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.event.PlayerCustomBrewEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.BrewingPreventionListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BrewingStandListener implements Listener {
    private static final Map<Location, ActiveBrewingStand> activeStands = new HashMap<>();
    private static final int fuel = 4;
    private static final int ingredient = 3;
    private static final Collection<Integer> potions = Set.of(0, 1, 2);
    private final Collection<InventoryAction> doNotInteractActions = Set.of(
            InventoryAction.DROP_ALL_CURSOR, InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_CURSOR, InventoryAction.DROP_ONE_SLOT,
            InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF
    );

    public BrewingStandListener(){
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20))
            ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(new BrewingPreventionListener(), ValhallaMMO.getInstance());
    }

    @EventHandler
    public void onBrewingInventoryInteract(InventoryClickEvent e){
        if (e.isCancelled() || WorldGuardHook.inDisabledRegion(e.getWhoClicked().getLocation(), WorldGuardHook.VMMO_CRAFTING_BREWING) ||
                ValhallaMMO.isWorldBlacklisted(e.getWhoClicked().getWorld().getName())) return;

        if (e.getView().getTopInventory() instanceof BrewerInventory b){
            Player p = (Player) e.getWhoClicked();
            e.setCancelled(true);

            if (doNotInteractActions.contains(e.getAction()) || e.getClick() == ClickType.DOUBLE_CLICK) e.setCancelled(false);
            else {
                if (e.getClickedInventory() instanceof BrewerInventory){
                    // brewing inventory clicked
                    if (potions.contains(e.getRawSlot())) ItemUtils.calculateClickEvent(e, 1, 0, 1, 2);
                    else ItemUtils.calculateClickEvent(e, 64, ingredient, fuel);
                } else {
                    // player inventory clicked
                    ItemStack clickedItem = e.getCurrentItem();
                    if (!ItemUtils.isEmpty(clickedItem)){
                        // order of integers determines priority. Blaze powder should have priority into the fuel slot
                        switch (clickedItem.getType()) {
                            case BLAZE_POWDER -> ItemUtils.calculateClickEvent(e, 64, fuel, ingredient);
                            case GLASS_BOTTLE, POTION, LINGERING_POTION, SPLASH_POTION -> ItemUtils.calculateClickEvent(e, 1, 0, 1, 2);
                            default -> {
                                if (ItemUtils.isEmpty(b.getIngredient())) ItemUtils.calculateClickEvent(e, 64, ingredient);
                                else if (potions.stream().anyMatch(i -> ItemUtils.isEmpty(b.getItem(i)))) ItemUtils.calculateClickEvent(e, 1, 0, 1, 2);
                                else ItemUtils.calculateClickEvent(e, 64, fuel);
                            }
                        }
                    } else ItemUtils.calculateClickEvent(e, 64, ingredient, fuel);
                }
            }

            if (b.getLocation() != null) BlockUtils.setOwner(b.getLocation().getBlock(), p.getUniqueId());
            updateStand(b, p);
        }
    }

    @EventHandler
    public void onBrewingInventoryDrag(InventoryDragEvent e){
        if (e.isCancelled() || WorldGuardHook.inDisabledRegion(e.getWhoClicked().getLocation(), WorldGuardHook.VMMO_CRAFTING_BREWING) ||
                ValhallaMMO.isWorldBlacklisted(e.getWhoClicked().getWorld().getName())) return;
        if (e.getView().getTopInventory() instanceof BrewerInventory b){
            Player p = (Player) e.getWhoClicked();
            ItemUtils.calculateDragEvent(e, 1, 0, 1, 2);

            if (b.getLocation() != null) BlockUtils.setOwner(b.getLocation().getBlock(), p.getUniqueId());
            updateStand(b, p);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStandPlace(BlockPlaceEvent e){
        if (e.isCancelled() || e.getBlock().getType() != Material.BREWING_STAND) return;
        BlockUtils.setOwner(e.getBlock(), e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onBrewingInventoryHopperFeed(InventoryMoveItemEvent e){
        if (WorldGuardHook.inDisabledRegion(e.getDestination().getLocation(), WorldGuardHook.VMMO_CRAFTING_BREWING) ||
                e.getDestination().getLocation() == null ||
                e.getDestination().getLocation().getWorld() == null ||
                ValhallaMMO.isWorldBlacklisted(e.getDestination().getLocation().getWorld().getName())) return;
        if (e.getDestination() instanceof BrewerInventory b){
            if(b.getLocation() == null) {
                e.setCancelled(true);
                return;
            }
            Player owner = BlockUtils.getOwner(b.getLocation().getBlock());
            updateStand(b, owner);
        }
    }

    @EventHandler(priority= EventPriority.HIGHEST)
    public void onBrew(BrewEvent e){
        if (e.isCancelled() || WorldGuardHook.inDisabledRegion(e.getContents().getLocation(), WorldGuardHook.VMMO_CRAFTING_BREWING) ||
                ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName())) return;
        e.setCancelled(true);
    }

    private void updateStand(BrewerInventory inventory, Player p){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            ItemStack i = inventory.getItem(ingredient);
            ActiveBrewingStand activeStand = activeStands.get(inventory.getLocation());
            if (ItemUtils.isEmpty(i)) {
                // cancel brewing
                if (activeStand != null) activeStand.cancel();
                activeStands.remove(inventory.getLocation());
                return;
            }
            Map<Integer, DynamicBrewingRecipe> recipes = WorldGuardHook.inDisabledRegion(inventory.getLocation(), WorldGuardHook.VMMO_CRAFTING_BREWING) ? new HashMap<>() : getBrewingRecipes(inventory, p);
            BrewingStand brewingStand = inventory.getHolder();
            if (brewingStand == null) return;
            if (recipes.isEmpty() || (activeStand != null && activeStand.visualProgress <= 0)){
                // expired
                if (activeStand != null) activeStand.cancel();
                brewingStand.setBrewingTime(0);
                brewingStand.update();
                activeStands.remove(inventory.getLocation());
                return;
            }
            if (activeStand != null){
                activeStand.recipes = recipes;
            } else {
                brewingStand.setBrewingTime(400);
                double baseDuration = recipes.values().stream().map(DynamicBrewingRecipe::getBrewTime).mapToDouble(a -> a).average().orElse(400);

                double speedMultiplier = 1 + (p == null ? 0 : AccumulativeStatManager.getCachedStats("BREWING_SPEED_BONUS", p, 10000, true));
                int duration = (int) (speedMultiplier <= 0 ? -1 : baseDuration / speedMultiplier); // negative speed = no brewing
                ActiveBrewingStand newStand = new ActiveBrewingStand(p, inventory, recipes, duration);
                newStand.runTaskTimer(ValhallaMMO.getInstance(), 0, 1);
                activeStands.put(brewingStand.getLocation(), newStand);
            }
        }, 1L);
    }

    private static class ActiveBrewingStand extends BukkitRunnable {
        private final boolean disabled;
        private final Player p;
        private final BrewerInventory inventory;
        private Map<Integer, DynamicBrewingRecipe> recipes;
        private int visualProgress = 400;
        private double actualProgress = 400;
        private final double tickStep;

        public ActiveBrewingStand(Player p, BrewerInventory inventory, Map<Integer, DynamicBrewingRecipe> recipes, int duration){
            this.p = p;
            this.inventory = inventory;
            this.recipes = recipes;
            disabled = duration < 0;
            this.tickStep = 400D / duration;
        }

        @Override
        public void run() {
            Location l = inventory.getLocation();
            BrewingStand stand = inventory.getHolder();
            if (l == null || l.getBlock().getType() != Material.BREWING_STAND || stand == null || stand.getFuelLevel() <= 0 || ItemUtils.isEmpty(inventory.getIngredient())){
                cancel();
                if (l != null) activeStands.remove(l);
                return;
            }
            if (!activeStands.containsKey(l)) cancel();
            if (visualProgress > 0){
                if (disabled || recipes.isEmpty()){
                    visualProgress = 10;
                } else {
                    actualProgress -= tickStep;
                    visualProgress = Math.max(0, (int) actualProgress);
                }
                stand.setBrewingTime(visualProgress);
                stand.update();
                return;
            }
            // stand is finished brewing
            stand.setFuelLevel(stand.getFuelLevel() - 1);
            if (stand.getFuelLevel() <= 0 && !ItemUtils.isEmpty(inventory.getFuel()) && inventory.getFuel().getType() == Material.BLAZE_POWDER){
                ItemStack powder = inventory.getFuel();
                if (powder.getAmount() <= 1) inventory.setFuel(null);
                else powder.setAmount(powder.getAmount() - 1);
                stand.setFuelLevel(20);
            }
            stand.update();

            ItemStack[] results = new ItemStack[] { null, null, null };
            for (int slot = 0; slot < 3; slot++){
                DynamicBrewingRecipe recipe = recipes.get(slot);
                ItemStack slotItem = inventory.getItem(slot);
                if (ItemUtils.isEmpty(slotItem) || recipe == null) continue;

                ItemBuilder result = (recipe.tinker() ? new ItemBuilder(slotItem) : new ItemBuilder(recipe.getResult()));
                DynamicItemModifier.modify(result, p, recipe.getModifiers(), false, true, true);
                PlayerCustomBrewEvent event = new PlayerCustomBrewEvent(p, recipe, result.get(), stand, ItemUtils.isEmpty(result.getItem()) && !CustomFlag.hasFlag(result.getMeta(), CustomFlag.UNCRAFTABLE));
                ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()) results[slot] = event.getResult();
            }

            if (!ItemUtils.isEmpty(results[0]) || !ItemUtils.isEmpty(results[1]) || !ItemUtils.isEmpty(results[2])){
                if (!ItemUtils.isEmpty(results[0])) inventory.setItem(0, results[0]);
                if (!ItemUtils.isEmpty(results[1])) inventory.setItem(1, results[1]);
                if (!ItemUtils.isEmpty(results[2])) inventory.setItem(2, results[2]);

                ItemStack ingredient = inventory.getIngredient().clone();
                // one of the recipes was successful, so consider operation a success
                boolean saveIngredient = false;
                if (p != null) saveIngredient = Utils.proc(p, AccumulativeStatManager.getCachedStats("BREWING_INGREDIENT_SAVE_CHANCE", p, 10000, true), false);

                if (saveIngredient){
                    ingredient.setAmount(1);
                    stand.getWorld().dropItem(stand.getLocation().add(0.5, 0.8, 0.5), ingredient);
                }

                if (inventory.getIngredient().getAmount() <= 1){
                    if (!saveIngredient && ItemUtils.getSimilarMaterials(Material.WATER_BUCKET).contains(ingredient.getType())){
                        inventory.setIngredient(new ItemStack(Material.BUCKET));
                    } else {
                        inventory.setIngredient(null);
                    }
                } else {
                    inventory.getIngredient().setAmount(inventory.getIngredient().getAmount() - 1);
                }
            }
            activeStands.remove(stand.getLocation());
            cancel();
        }
    }

    private Map<Integer, DynamicBrewingRecipe> getBrewingRecipes(BrewerInventory inventory, Player brewer) {
        PowerProfile p = brewer == null ? null : ProfileCache.getOrCache(brewer, PowerProfile.class);
        boolean allowedAllRecipes = brewer != null && brewer.hasPermission("valhalla.allrecipes");
        Map<Integer, DynamicBrewingRecipe> recipes = new HashMap<>();

        if (inventory.getLocation() != null && inventory.getLocation().getWorld() != null && (ValhallaMMO.isWorldBlacklisted(inventory.getLocation().getWorld().getName()))) {
            return recipes; // no recipes returned if profile is null, world is disabled, or in blocking region
        }
        ItemStack ingredient = inventory.getIngredient();
        if (ItemUtils.isEmpty(ingredient)) return recipes;
        ingredient = ingredient.clone();
        ingredient.setAmount(1);

        for (DynamicBrewingRecipe r : CustomRecipeRegistry.getBrewingRecipesByIngredient().getOrDefault(ingredient.getType(), new HashSet<>())) {
            if (validateAndInsertRecipe(r, ingredient, brewer, p, allowedAllRecipes, inventory, recipes) == RecipeStatus.FINISHED) break;
        }
        return recipes;
    }

    private RecipeStatus validateAndInsertRecipe(DynamicBrewingRecipe r, ItemStack ingredient, Player brewer, PowerProfile p, boolean allowedAllRecipes, BrewerInventory inventory, Map<Integer, DynamicBrewingRecipe> recipes){
        // profile is null, so recipe is tried in brewing stand without online owner. if the recipe isn't default unlocked or any of its modifiers require a player, skip it.
        if (brewer == null && r.getModifiers().stream().anyMatch(DynamicItemModifier::requiresPlayer)) return RecipeStatus.SKIP;
        if (p != null && !allowedAllRecipes &&
                !r.isUnlockedForEveryone() &&
                !p.getUnlockedRecipes().contains(r.getName())) return RecipeStatus.SKIP;

        for (int i = 0; i < 3; i++) {
            if (recipes.size() == 3) {
                return RecipeStatus.FINISHED; // If all recipes are already determined, we're done. Can cancel!
            }
            if (recipes.containsKey(i)) {
                continue; // If this recipe slot is already occupied, skip to next
            }

            ItemStack slotItem = inventory.getItem(i);
            if (ItemUtils.isEmpty(slotItem)) {
                continue; // If the slot is empty, might as well continue to next slot
            }
            slotItem = slotItem.clone();

            if (!r.getApplyOn().getOption().matches(r.getApplyOn().getItem(), slotItem)) continue;
            if (!r.getIngredient().getOption().matches(r.getIngredient().getItem(), ingredient)) continue;

            // If the slot item does not match the required type, skip to next
            ItemBuilder result = (r.tinker() ? new ItemBuilder(slotItem) : new ItemBuilder(r.getResult()));
            DynamicItemModifier.modify(result, brewer, r.getModifiers(), false, false, true);

            if (!ItemUtils.isEmpty(result.getItem()) && !CustomFlag.hasFlag(result.getMeta(), CustomFlag.UNCRAFTABLE)) {
                recipes.put(i, r); // If the item is not null by the end of processing, recipes is added.
            }
        }
        return RecipeStatus.SKIP;
    }

    private enum RecipeStatus{
        SKIP,
        FINISHED
    }
}
