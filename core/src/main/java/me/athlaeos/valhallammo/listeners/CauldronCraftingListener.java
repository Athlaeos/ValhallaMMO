package me.athlaeos.valhallammo.listeners;

import com.jeff_media.customblockdata.CustomBlockData;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.animations.AnimationRegistry;
import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCauldronRecipe;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.event.CauldronAbsorbItemEvent;
import me.athlaeos.valhallammo.event.CauldronCompleteRecipeEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CauldronCraftingListener implements Listener {
    private static final NamespacedKey CAULDRON_STORAGE = new NamespacedKey(ValhallaMMO.getInstance(), "cauldron_storage");
    private static final Map<UUID, CauldronInputTick> entityThrowItemLimiter = new HashMap<>();
    private static final Map<Location, CauldronInputTick> blockThrowItemLimiter = new HashMap<>();
    private static final Map<Location, CauldronCookingTask> activeCauldrons = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemThrow(PlayerDropItemEvent e){
        Player thrower = e.getPlayer();
        if (ValhallaMMO.isWorldBlacklisted(thrower.getWorld().getName())) return;
        if (e.isCancelled()) return;
        if (!entityThrowItemLimiter.containsKey(thrower.getUniqueId())){
            CauldronInputTick runnable = new CauldronInputTick(thrower, e.getItemDrop());
            entityThrowItemLimiter.put(thrower.getUniqueId(), runnable);
            runnable.runTaskTimer(ValhallaMMO.getInstance(), 0L, 1L);
        } else {
            entityThrowItemLimiter.get(thrower.getUniqueId()).resetTicks();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDispense(BlockDispenseEvent e){
        Block b = e.getBlock();
        if (ValhallaMMO.isWorldBlacklisted(b.getWorld().getName())) return;
        if (e.isCancelled() || !(e.getBlock().getBlockData() instanceof Directional d)) return;
        if (!blockThrowItemLimiter.containsKey(b.getLocation())){
            Collection<Entity> entitiesBefore = b.getWorld().getNearbyEntities(b.getRelative(d.getFacing()).getLocation().add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5, (i) -> i instanceof Item);
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
                Collection<Entity> newEntities = b.getWorld().getNearbyEntities(b.getRelative(d.getFacing()).getLocation().add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5, (i) -> i instanceof Item && !entitiesBefore.contains(i));
                Item i = (Item) newEntities.stream().findAny().orElse(null);
                if (i == null) return;
                CauldronInputTick runnable = new CauldronInputTick(b, i);
                blockThrowItemLimiter.put(b.getLocation(), runnable);
                runnable.runTaskTimer(ValhallaMMO.getInstance(), 0L, 1L);
            }, 1L);
        } else {
            blockThrowItemLimiter.get(b.getLocation()).resetTicks();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCauldronClick(PlayerInteractEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName())) return;
        if (e.getClickedBlock() == null || e.getHand() != EquipmentSlot.HAND || e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock().getType().toString().contains("CAULDRON")) {
            Block b = e.getClickedBlock();
            if (!(b.getBlockData() instanceof Levelled l) || l.getLevel() <= 0) return;
            if (e.getPlayer().isSneaking()){
                dumpCauldronContents(b);
            } else {
                onCauldronClickedItem(e.getPlayer(), b);
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPistonExtend(BlockPistonExtendEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName())) return;
        if (!e.isCancelled()) {
            e.getBlocks().stream().filter(CauldronCraftingListener::isCustomCauldron).forEach(CauldronCraftingListener::dumpCauldronContents);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPistonExtend(EntityExplodeEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName())) return;
        if (!e.isCancelled()) e.blockList().stream().filter(CauldronCraftingListener::isCustomCauldron).forEach(CauldronCraftingListener::dumpCauldronContents);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPistonExtend(BlockExplodeEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName())) return;
        if (!e.isCancelled()) e.blockList().stream().filter(CauldronCraftingListener::isCustomCauldron).forEach(CauldronCraftingListener::dumpCauldronContents);
    }

    @EventHandler(priority =EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent e){
        if (!e.isCancelled()){
            if (CauldronCraftingListener.isCustomCauldron(e.getBlock())){
                CauldronCraftingListener.dumpCauldronContents(e.getBlock());
            }
        }
    }

    /**
     * Drops all of a cauldron's items
     * @param cauldron the cauldron to drop all its contents of
     */
    public static void dumpCauldronContents(Block cauldron){
        if (activeCauldrons.containsKey(cauldron.getLocation())) activeCauldrons.get(cauldron.getLocation()).stop();
        List<ItemStack> items = getCauldronContents(cauldron);
        if (items.isEmpty()) return;
        for (ItemStack i : items){
            cauldron.getWorld().dropItem(cauldron.getLocation().add(0.5, 1, 0.5), i);
        }
        cauldron.getWorld().playSound(cauldron.getLocation().add(0.5, 1, 0.5), Sound.ITEM_BUCKET_EMPTY, 0.3F, 1F);
        setCauldronContents(cauldron, null);
    }

    /**
     * Updates the cauldron and triggers a potential recipe. The recipe triggered is also returned, or null if none were
     * be found.
     * @param responsible the player responsible for the recipe. Can be null
     * @param cauldron the cauldron in which the recipe is made
     * @param catalyst an optional catalyst with which the recipe is triggered
     * @param contents any predetermined contents of the cauldron
     * @return a pair with the recipe if one was triggered along with the integer amount the recipe will be crafted, or null if no recipe was found
     */
    public static Pair<DynamicCauldronRecipe, Integer> updateCauldronRecipes(@Nullable Player responsible, Block cauldron, @Nullable ItemStack catalyst, List<ItemStack> contents){
        Pair<DynamicCauldronRecipe, Integer> r = getCauldronRecipe(responsible, contents, cauldron, catalyst);
        if (r == null) return null;
        // recipe is null after clicked item, meaning it's not a catalyst.
        // try getting the recipes from the cauldron again because the item might trigger the cauldron starting to cook

        DynamicCauldronRecipe recipe = r.getOne();
        int count = r.getTwo();
        if (recipe.isTimedRecipe()){
            CauldronCookingTask task = new CauldronCookingTask(responsible, cauldron, recipe, count);
            activeCauldrons.put(cauldron.getLocation(), task);
            task.runTaskTimer(ValhallaMMO.getInstance(), 0L, 1L);
        } else if (!ItemUtils.isEmpty(catalyst)){
            ItemBuilder result = new ItemBuilder(recipe.tinkerCatalyst() ? catalyst : recipe.getResult());
            if (recipe.requiresValhallaTools() && !SmithingItemPropertyManager.hasSmithingQuality(ItemUtils.getItemMeta(catalyst))) return null;
            if (ItemUtils.removeItems(contents, recipe.getIngredients(), count, recipe.getMetaRequirement().getChoice())){
                // catalyst-triggered recipes are crafted instantly and so "use" can be true. Timed recipes should execute on completion
                DynamicItemModifier.modify(result, responsible, recipe.getModifiers(), false, true, true, count);
                if (ItemUtils.isEmpty(result.getItem()) || CustomFlag.hasFlag(result.getMeta(), CustomFlag.UNCRAFTABLE)) return null;
                setCauldronContents(cauldron, contents);

                CauldronCompleteRecipeEvent completionEvent = new CauldronCompleteRecipeEvent(cauldron, recipe, responsible, result.get());
                ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(completionEvent);

                for (int i = 0; i < count; i++)
                    cauldron.getWorld().dropItem(cauldron.getLocation().add(0.5, 1, 0.5), completionEvent.getResult());
                cauldron.getWorld().playEffect(cauldron.getLocation().add(0.5, 0.2, 0.5), Effect.EXTINGUISH, 0);
                cauldron.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, cauldron.getLocation().add(0.5, 0.5, 0.5), 20);

                recipe.getValidations().forEach(v -> {
                    Validation validation = ValidationRegistry.getValidation(v);
                    if (validation != null) validation.execute(cauldron);
                });
            }
        }

        return r;
    }

    /**
     * Updates the cauldron and triggers a potential recipe. The recipe triggered is also returned, or null if none were
     * be found.
     * @param responsible the player responsible for the recipe. Can be null
     * @param cauldron the cauldron in which the recipe is made
     * @param catalyst an optional catalyst with which the recipe is triggered
     * @return a pair with the recipe if one was triggered along with the integer amount the recipe will be crafted, or null if no recipe was found
     */
    public static Pair<DynamicCauldronRecipe, Integer> updateCauldronRecipes(@Nullable Player responsible, Block cauldron, @Nullable ItemStack catalyst){
        return updateCauldronRecipes(responsible, cauldron, catalyst, getCauldronContents(cauldron));
    }

    private static void onCauldronAbsorbItem(Player thrower, Block cauldron, Item item){
        if (activeCauldrons.containsKey(cauldron.getLocation())) return;
        List<ItemStack> contents = getCauldronContents(cauldron);
        Pair<DynamicCauldronRecipe, Integer> r = updateCauldronRecipes(thrower, cauldron, item.getItemStack(), contents);
        if (r == null) { // recipe is null after thrown item, meaning it's not a catalyst. Add the thrown item to the cauldron
            // if possible, and update the cauldron again afterwards in case a cooking recipe was triggered from it
            contents = addItem(cauldron, item.getItemStack(), thrower);
            if (contents != null){
                item.remove();
            } else return;

            updateCauldronRecipes(thrower, cauldron, null, contents);
        } else {
            DynamicCauldronRecipe recipe = r.getOne();
            int count = r.getTwo();
            if (item.getItemStack().getAmount() <= recipe.getCatalyst().getItem().getAmount() * count){
                item.remove();
            } else {
                ItemStack i = item.getItemStack().clone();
                i.setAmount(i.getAmount() - (recipe.getCatalyst().getItem().getAmount() * count));
                item.setItemStack(i);
            }
        }
    }

    private static void onCauldronClickedItem(Player clicker, Block cauldron){
        if (activeCauldrons.containsKey(cauldron.getLocation())) return;
        ItemStack item = clicker.getInventory().getItemInMainHand();
        if (ItemUtils.isEmpty(item)) return;
        List<ItemStack> contents = getCauldronContents(cauldron);
        Pair<DynamicCauldronRecipe, Integer> r = getCauldronRecipe(clicker, contents, cauldron, item);
        if (r == null) { // recipe is null after thrown item, meaning it's not a catalyst. Add the thrown item to the cauldron
            // if possible, and update the cauldron again afterwards in case a cooking recipe was triggered from it
            contents = addItem(cauldron, item, clicker);
            if (contents != null){
                clicker.getInventory().setItemInMainHand(null);
            } else return;

            updateCauldronRecipes(clicker, cauldron, null, contents);
        } else {
            DynamicCauldronRecipe recipe = r.getOne();
            int count = r.getTwo();
            if (item.getAmount() <= recipe.getCatalyst().getItem().getAmount() * count){
                item = null;
            } else {
                item.setAmount(item.getAmount() - (recipe.getCatalyst().getItem().getAmount() * count));
            }
            clicker.getInventory().setItemInMainHand(item);
        }
    }

    private static Pair<DynamicCauldronRecipe, Integer> getCauldronRecipe(Player crafter, List<ItemStack> contents, Block cauldron, ItemStack catalyst){
        if (!cauldron.getType().toString().contains("CAULDRON")) return null;
        if (!(cauldron.getBlockData() instanceof Levelled l) || l.getLevel() <= 0) return null;
        for (DynamicCauldronRecipe r : CustomRecipeRegistry.getCauldronRecipes().values()){
            if (r.getIngredients().size() > contents.size()) continue; // recipe requires items, but cauldron doesn't have enough. continue
            // if the recipe has modifiers requiring a player and there is no player,
            // the world is blacklisted, the block is in a disabled region, or if any of the
            // recipe's validations failed then skip recipe
            if ((r.getModifiers().stream().anyMatch(DynamicItemModifier::requiresPlayer) && crafter == null) ||
                    (ValhallaMMO.isWorldBlacklisted(cauldron.getWorld().getName())) ||
                    (WorldGuardHook.inDisabledRegion(cauldron.getLocation(), WorldGuardHook.VMMO_CRAFTING_CAULDRON)) ||
                    r.getValidations().stream().anyMatch(v -> {
                        Validation validation = ValidationRegistry.getValidation(v);
                        if (validation != null) return !validation.validate(cauldron);
                        return false;
                    })
            ) continue;
            int count = 1;
            if (!r.isTimedRecipe()){
                // if the recipe is a catalyst-triggered recipe, skip if the catalyst is empty or if the catalyst
                // doesn't match the required catalyst, or if there isn't enough of the catalyst
                if (ItemUtils.isEmpty(catalyst)) continue;
                if (!r.getCatalyst().getOption().matches(r.getCatalyst().getItem(), catalyst)) continue;
                if (r.getCatalyst().getItem().getAmount() > catalyst.getAmount()) continue;

                count = (int) Math.floor((double) catalyst.getAmount() / (double) r.getCatalyst().getItem().getAmount());
                if (r.getIngredients().isEmpty()) return new Pair<>(r, count); // catalyst recipes may have no ingredients, timed recipes MUST have ingredients
                else count = Math.min(count, ItemUtils.timesContained(contents, r.getIngredients(), r.getMetaRequirement().getChoice()));
            } else {
                // check if all ingredients are present in cauldron
                count = ItemUtils.timesContained(contents, r.getIngredients(), r.getMetaRequirement().getChoice());
            }
            if (count > 0){
                return new Pair<>(r, count);
            }
        }
        return null;
    }

    /**
     * Adds an item to the cauldron. Respects configured max storable items.
     * @param cauldron the cauldron to add an item into
     * @param i the item to add into
     * @param adder the player who adds the item to the cauldron. May be null, in which case the plugin assumes the item was added by non-player means
     * @return a list of the new cauldron contents if the item was added to it, or null if the item couldn't be added or if the event was cancelled
     */
    public static List<ItemStack> addItem(Block cauldron, ItemStack i, Player adder){
        if (!cauldron.getType().toString().contains("CAULDRON")) return null;
        if (!(cauldron.getBlockData() instanceof Levelled l) || l.getLevel() <= 0) return null;
        List<ItemStack> contents = getCauldronContents(cauldron);
        contents.add(i);

        Map<ItemStack, Integer> compactedContents = new HashMap<>();
        // this first compacts and then separates ItemStacks. For example, 60 ender pearls would be separated into 3 stacks of 16 plus 12
        for (ItemStack item : contents){
            if (ItemUtils.isEmpty(item)) continue;
            item = item.clone();
            int itemAmount = item.getAmount();
            item.setAmount(1);
            if (compactedContents.containsKey(item)){
                compactedContents.put(item, compactedContents.get(item) + itemAmount);
            } else {
                compactedContents.put(item, itemAmount);
            }
        }
        List<ItemStack> newContents = ItemUtils.decompressStacks(compactedContents);
        if (newContents.size() > ValhallaMMO.getPluginConfig().getInt("cauldron_max_capacity", 3)) return null;

        CauldronAbsorbItemEvent event = new CauldronAbsorbItemEvent(cauldron, i, adder);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return null;

        cauldron.getWorld().playSound(cauldron.getLocation().add(0.5, 0.5, 0.5), Sound.ITEM_BUCKET_FILL, 1F, 1F);
        cauldron.getWorld().playEffect(cauldron.getLocation().add(0.5, 0.2, 0.5), Effect.EXTINGUISH, 0);
        cauldron.getWorld().spawnParticle(Particle.WATER_SPLASH, cauldron.getLocation().add(0.5, 0.8, 0.5), 15);
        setCauldronContents(cauldron, newContents);
        return newContents;
    }

    /**
     * Returns the contents of the cauldron
     * @param cauldron the cauldron to return the contents from
     * @return the contents of the cauldron, or an empty list if it has none
     */
    public static List<ItemStack> getCauldronContents(Block cauldron){
        List<ItemStack> inventory = new ArrayList<>();
        if (!cauldron.getType().toString().contains("CAULDRON")) return inventory;
        if (!CustomBlockData.hasCustomBlockData(cauldron, ValhallaMMO.getInstance())) return inventory;

        PersistentDataContainer customBlockData = new CustomBlockData(cauldron, ValhallaMMO.getInstance());
        String rawContents = customBlockData.getOrDefault(CAULDRON_STORAGE, PersistentDataType.STRING, "");
        if (StringUtils.isEmpty(rawContents)) return inventory;

        String[] items = rawContents.split("<itemsplitter>");

        for (String itemSlot : items){
            ItemStack item = ItemUtils.deserialize(itemSlot);
            if (ItemUtils.isEmpty(item)) continue;
            inventory.add(item);
        }

        return inventory;
    }

    /**
     * Sets the contents of the cauldron. Contents may exceed the config max storable items
     * @param cauldron the cauldron to store items into
     * @param contents the list of items to store into the cauldron
     */
    public static void setCauldronContents(Block cauldron, List<ItemStack> contents){
        if (!cauldron.getType().toString().contains("CAULDRON")) return;

        PersistentDataContainer customBlockData = new CustomBlockData(cauldron, ValhallaMMO.getInstance());
        if (contents == null || contents.isEmpty()){
            customBlockData.remove(CAULDRON_STORAGE);
        } else {
            customBlockData.set(CAULDRON_STORAGE, PersistentDataType.STRING, contents.stream().map(ItemUtils::serialize).collect(Collectors.joining("<itemsplitter>")));
        }
    }

    /**
     * @param cauldron the cauldron to check if it has custom contents
     * @return true if the cauldron has custom stored contents, false otherwise
     */
    public static boolean isCustomCauldron(Block cauldron){
        if (!cauldron.getType().toString().contains("CAULDRON")) return false;
        if (!CustomBlockData.hasCustomBlockData(cauldron, ValhallaMMO.getInstance())) return false;
        PersistentDataContainer customBlockData = new CustomBlockData(cauldron, ValhallaMMO.getInstance());
        return customBlockData.has(CAULDRON_STORAGE, PersistentDataType.STRING) && !getCauldronContents(cauldron).isEmpty();
    }

    private static class CauldronCookingTask extends BukkitRunnable{
        private int duration;
        private final UUID cooker;
        private final Location cauldron;
        private final DynamicCauldronRecipe recipe;
        private final int quantity;

        public CauldronCookingTask(@Nullable Player cooker, @NotNull Block cauldron, @NotNull DynamicCauldronRecipe recipe, int quantity){
            this.cooker = cooker == null ? null : cooker.getUniqueId();
            this.cauldron = cauldron.getLocation();
            this.recipe = recipe;
            this.quantity = quantity;
            this.duration = recipe.getCookTime();
        }

        @Override
        public void run() {
            Player p = cooker == null ? null : ValhallaMMO.getInstance().getServer().getPlayer(cooker);
            if (duration > 0){
                Animation animation = AnimationRegistry.getAnimation(AnimationRegistry.BLOCK_BUBBLES.id());
                if (animation != null && p != null) animation.animate(p, cauldron, null, duration);
            } else {
                Block b = cauldron.getBlock();
                ItemBuilder result = new ItemBuilder(recipe.getResult());
                List<ItemStack> contents = getCauldronContents(b);
                if (ItemUtils.removeItems(contents, recipe.getIngredients(), quantity, recipe.getMetaRequirement().getChoice())){
                    // catalyst-triggered recipes are crafted instantly and so "use" can be true. Timed recipes should execute on completion
                    DynamicItemModifier.modify(result, p, recipe.getModifiers(), false, true, true, quantity);
                    if (ItemUtils.isEmpty(result.getItem()) || CustomFlag.hasFlag(result.getMeta(), CustomFlag.UNCRAFTABLE)) {
                        b.getWorld().playEffect(cauldron.add(0.5, 0.2, 0.5), Effect.EXTINGUISH, 0);
                        dumpCauldronContents(b);
                        stop();
                        return;
                    }
                    setCauldronContents(b, contents);

                    CauldronCompleteRecipeEvent completionEvent = new CauldronCompleteRecipeEvent(b, recipe, p, result.get());
                    ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(completionEvent);
                    for (int i = 0; i < quantity; i++)
                        b.getWorld().dropItem(cauldron.add(0.5, 0.5, 0.5), completionEvent.getResult());
                    Animation animation = AnimationRegistry.getAnimation(AnimationRegistry.BLOCK_SPARKS_EXTINGUISH.id());
                    animation.animate(p, cauldron, null, duration);

                    recipe.getValidations().forEach(v -> {
                        Validation validation = ValidationRegistry.getValidation(v);
                        if (validation != null) validation.execute(b);
                    });
                }
                stop();
            }
            duration--;
        }

        private void stop(){
            activeCauldrons.remove(cauldron);
            cancel();
        }
    }

    private static class CauldronInputTick extends BukkitRunnable{
        private int ticks = ValhallaMMO.getPluginConfig().getInt("cauldron_item_duration");
        private final UUID thrower;
        private final Location block;
        private final UUID item;

        public CauldronInputTick(Player thrower, Item item){
            this.thrower = thrower.getUniqueId();
            this.item = item.getUniqueId();
            this.block = null;
        }

        public CauldronInputTick(Block block, Item item){
            this.thrower = null;
            this.item = item.getUniqueId();
            this.block = block.getLocation();
        }

        @Override
        public void run() {
            Item i = (ValhallaMMO.getInstance().getServer().getEntity(item) instanceof Item it) ? it : null;
            Player p = thrower == null ? null : ValhallaMMO.getInstance().getServer().getPlayer(thrower);
            if (ticks > 0 && i != null && i.isValid() && p != null){
                Block b = i.getLocation().getBlock();
                if (!b.getType().toString().contains("CAULDRON")) b = i.getLocation().add(0, -0.9, 0).getBlock();
                if (b.getType().toString().contains("CAULDRON")){
                    if (!(b.getBlockData() instanceof Levelled l) || l.getLevel() <= 0){
                        remove(); // if it's not a cauldron with some liquid, cancel item drop detection
                        cancel();
                        return;
                    }

                    onCauldronAbsorbItem(p, b, i);
                    remove();
                    cancel();
                }
            } else {
                remove();
                cancel();
            }
            ticks--;
        }

        private void remove(){
            if (thrower != null) entityThrowItemLimiter.remove(thrower);
            else blockThrowItemLimiter.remove(block);
        }

        public void resetTicks(){
            ticks = ValhallaMMO.getPluginConfig().getInt("cauldron_item_duration");
        }
    }
}


