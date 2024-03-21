package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.ToolRequirementType;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.animations.AnimationRegistry;
import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.ImmersiveCraftingRecipe;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.ImmersiveRecipeSelectionMenu;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.item.SmithingItemPropertyManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ImmersiveRecipeListener implements Listener {
    private final Collection<Material> immersiveBlocks = ItemUtils.getMaterialSet(ValhallaMMO.getPluginConfig().getStringList("immersive_on_regular_click"));
    private static final Map<UUID, ImmersiveCraftingRecipe> selectedImmersiveRecipe = new HashMap<>();
    public static Map<UUID, ImmersiveCraftingRecipe> getSelectedImmersiveRecipe() { return selectedImmersiveRecipe; }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e){
        Block clicked = e.getClickedBlock();
        if (clicked == null) return;
        boolean disableVanillaMenu = ItemUtils.getMaterialSet(ValhallaMMO.getPluginConfig().getStringList("disable_vanilla_menu")).contains(clicked.getType());
        // cancel block gui opening if disabled, or if the player still has an active cooldown on such interactions
        if (clicked.getType().isInteractable() && (disableVanillaMenu || !Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "cancel_block_interactions"))) e.setCancelled(true);
        Player p = e.getPlayer();
        if (!Timer.isCooldownPassed(p.getUniqueId(), "delay_crafting_attempts")) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) selectedImmersiveRecipe.remove(p.getUniqueId());
        if (e.useItemInHand() == Event.Result.DENY || e.getHand() != EquipmentSlot.HAND || e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!CustomRecipeRegistry.getImmersiveRecipesByBlock().containsKey(ItemUtils.getBaseMaterial(clicked.getType()))) return;
        if (Timer.getTimerResult(p.getUniqueId(), "time_held_immersive_interact") == 0) Timer.startTimer(p.getUniqueId(), "time_held_immersive_interact"); // start timer if none was started yet
        if (Timer.getTimerResult(p.getUniqueId(), "time_since_immersive_interact") == 0) Timer.startTimer(p.getUniqueId(), "time_since_immersive_interact"); // start timer if none was started yet

        boolean openImmersiveMenu = !p.isSneaking() == immersiveBlocks.contains(clicked.getType());
        ItemStack heldItem = p.getInventory().getItemInMainHand();
        ImmersiveCraftingRecipe recipe = selectedImmersiveRecipe.get(p.getUniqueId());
        if (!ItemUtils.isEmpty(heldItem) && recipe == null && !openImmersiveMenu && Timer.isCooldownPassed(p.getUniqueId(), "delay_tinkering_attempts")){
            // attempt a tinkering recipe
            Collection<ImmersiveCraftingRecipe> tinkerRecipes = getTinkerRecipes(p, clicked.getType());
            if (tinkerRecipes.size() == 1) recipe = tinkerRecipes.stream().findFirst().orElse(null);
            else Timer.setCooldown(p.getUniqueId(), 1000, "delay_tinkering_attempts");
            if (recipe != null && !recipe.getIngredients().isEmpty()){
                if (ItemUtils.timesContained(Arrays.asList(p.getInventory().getStorageContents()), recipe.getIngredients(), recipe.getMetaRequirement().getChoice()) <= 0){
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Utils.chat(TranslationManager.getTranslation("error_crafting_no_ingredients"))));
                    Timer.setCooldown(p.getUniqueId(), 500, "delay_crafting_attempts");
                    return;
                }
            }
        }
        if (recipe == null && openImmersiveMenu){
            new ImmersiveRecipeSelectionMenu(PlayerMenuUtilManager.getPlayerMenuUtility(p), clicked, ItemUtils.isEmpty(heldItem)).open();
            e.setCancelled(true);
            return;
        } else if (recipe != null){
            e.setCancelled(true);
            // if the recipe is a tinker recipe, the held item should match the tinker input and optional valhalla tool requirements
            // if not, the held item should only be a tool if one is required
            if (Timer.getTimerResult(p.getUniqueId(), "time_since_immersive_interact") > 500){
                // if the player hasn't interacted for more than 0.5 seconds, reset "button held" timer
                Timer.startTimer(p.getUniqueId(), "time_held_immersive_interact");
            }
            ItemBuilder held = ItemUtils.isEmpty(heldItem) ? null : new ItemBuilder(heldItem);
            // if the player isn't holding the right item any more, unselect recipe entirely and do nothing more
            boolean properItemHeld = recipe.tinker() ?
                    (held != null && recipe.getTinkerInput().getOption().matches(recipe.getTinkerInput().getItem(), heldItem) &&
                            (!recipe.requiresValhallaTools() || SmithingItemPropertyManager.hasSmithingQuality(held.getMeta()))) :
                    recipe.getToolRequirement().canCraft(held == null ? -1 : ToolRequirementType.getToolID(held.getMeta()));
            if (!ItemUtils.isSimilarMaterial(recipe.getBlock(), clicked.getType()) || !canPlayerCraft(e, recipe) || !properItemHeld) {
                selectedImmersiveRecipe.remove(p.getUniqueId());
                return;
            }
            long interactedFor = Timer.getTimerResult(p.getUniqueId(), "time_held_immersive_interact");
            if (interactedFor < 100 || interactedFor >= recipe.getTimeToCraft() * 50L){
                // only check crafting conditions on start and finish of crafting process
                PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
                if (profile == null ||
                        (!p.hasPermission("valhalla.allrecipes") && !recipe.isUnlockedForEveryone() && !profile.getUnlockedRecipes().contains(recipe.getName())) ||
                        ValhallaMMO.isWorldBlacklisted(p.getWorld().getName()) ||
                        (WorldGuardHook.inDisabledRegion(clicked.getLocation(), WorldGuardHook.VMMO_CRAFTING_IMMERSIVE)) ||
                        (recipe.getValidations().stream().anyMatch(v -> {
                            Validation validation = ValidationRegistry.getValidation(v);
                            if (validation != null) {
                                boolean invalid = !validation.validate(clicked);
                                if (invalid) Utils.sendActionBar(p, validation.validationError());
                                return invalid;
                            }
                            return false;
                        }))) {
                    // attempted craft in invalid place, cancel.
                    resetFrequency(p);
                    selectedImmersiveRecipe.remove(p.getUniqueId());
                    Timer.startTimer(p.getUniqueId(), "time_held_immersive_interact");
                    Timer.setCooldown(p.getUniqueId(), 500, "delay_crafting_attempts");
                    return;
                }

                if (interactedFor >= recipe.getTimeToCraft() * 50L){
                    // finished crafting, output recipe
                    if (ItemUtils.timesContained(Arrays.asList(p.getInventory().getStorageContents()), recipe.getIngredients(), recipe.getMetaRequirement().getChoice()) > 0){
                        if (ItemUtils.removeItems(p.getInventory(), recipe.getIngredients(), 1, recipe.getMetaRequirement().getChoice())){
                            ItemBuilder result = recipe.tinker() ? held : new ItemBuilder(recipe.getResult());
                            DynamicItemModifier.modify(result, p, recipe.getModifiers(), false, true, true);
                            if (ItemUtils.isEmpty(result.getItem()) || CustomFlag.hasFlag(result.getMeta(), CustomFlag.UNCRAFTABLE)){
                                Utils.sendMessage(p, ItemUtils.getPDCString(DynamicItemModifier.ERROR_MESSAGE, heldItem, ""));
                                selectedImmersiveRecipe.remove(p.getUniqueId());
                            } else {
                                Animation animation = AnimationRegistry.getAnimation(AnimationRegistry.BLOCK_SPARKS_CRAFTSOUND.id());
                                if (animation != null) animation.animate(p, clicked.getLocation(), p.getEyeLocation().getDirection(), 0);
                                incrementPlayerCraftFrequency(e.getPlayer(), recipe);
                                if (recipe.tinker())
                                    p.getInventory().setItemInMainHand(result.get());
                                else {
                                    if (ValhallaMMO.getPluginConfig().getBoolean("craft_item_drop", true)){
                                        Item itemDrop = e.getPlayer().getWorld().dropItem(clicked.getLocation().add(0.5, 1.2, 0.5), result.get());
                                        itemDrop.setPickupDelay(0);
                                        itemDrop.setOwner(e.getPlayer().getUniqueId());
                                        itemDrop.setThrower(e.getPlayer().getUniqueId());
                                    } else {
                                        e.getPlayer().getInventory().addItem(result.get());
                                    }
                                }

                                recipe.getValidations().forEach(v -> {
                                    Validation validation = ValidationRegistry.getValidation(v);
                                    if (validation != null) validation.execute(clicked);
                                });
                            }
                        }
                    } else {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Utils.chat(TranslationManager.getTranslation("error_crafting_no_ingredients"))));
                    }
                    Timer.startTimer(p.getUniqueId(), "time_held_immersive_interact");
                }
            } else {
                // inbetween clicks, just for aesthetic effects
                if (Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "sound_craft")){
                    Animation animation = AnimationRegistry.getAnimation(AnimationRegistry.BLOCK_PARTICLE_PUFF.id());
                    if (animation != null) animation.animate(p, clicked.getLocation(), p.getEyeLocation().getDirection(), 0);
                    Timer.setCooldown(e.getPlayer().getUniqueId(), 200, "sound_craft");
                }
            }
        }

        Timer.startTimer(p.getUniqueId(), "time_since_immersive_interact");
    }

    private final Map<Material, Collection<ImmersiveCraftingRecipe>> immersiveRecipeCache = new HashMap<>();

    private Collection<ImmersiveCraftingRecipe> getTinkerRecipes(Player crafter, Material block){
        ItemStack mainHand = crafter.getInventory().getItemInMainHand();
        if (ItemUtils.isEmpty(mainHand)) return new ArrayList<>();
        Collection<ImmersiveCraftingRecipe> heldTypeRecipes = immersiveRecipeCache.getOrDefault(mainHand.getType(), new HashSet<>());

        Material base = ItemUtils.getBaseMaterial(block);
        for (ImmersiveCraftingRecipe r : CustomRecipeRegistry.getImmersiveRecipesByBlock().get(base)) {
            if (!r.tinker() || heldTypeRecipes.contains(r)) continue;
            heldTypeRecipes.add(r);
        }
        immersiveRecipeCache.put(mainHand.getType(), heldTypeRecipes);

        return heldTypeRecipes.stream().filter(r -> r.getTinkerInput().getOption().matches(r.getTinkerInput().getItem(), mainHand)).collect(Collectors.toSet());
    }

    private final Map<UUID, RecipeFrequencyDO> recipeFrequency = new HashMap<>();

    private void resetFrequency(Player p){
        recipeFrequency.remove(p.getUniqueId());
    }

    private void incrementPlayerCraftFrequency(Player p, ImmersiveCraftingRecipe recipe){
        if (recipe == null) return;
        if (recipe.getConsecutiveCrafts() == -1) return;
        RecipeFrequencyDO frequencyDO = recipeFrequency.getOrDefault(p.getUniqueId(), new RecipeFrequencyDO(0, recipe));
        if (!frequencyDO.recipe.getName().equals(recipe.getName())) frequencyDO = new RecipeFrequencyDO(0, recipe);
        frequencyDO.setFrequency(frequencyDO.getFrequency() + 1);
        recipeFrequency.put(p.getUniqueId(), frequencyDO);
    }

    private boolean canPlayerCraft(PlayerInteractEvent e, ImmersiveCraftingRecipe recipe){
        if (recipe == null) return true;
        if (recipe.getConsecutiveCrafts() == -1) return true;
        Player p = e.getPlayer();
        RecipeFrequencyDO frequencyDO = recipeFrequency.getOrDefault(p.getUniqueId(), new RecipeFrequencyDO(0, recipe));
        if (!frequencyDO.getRecipe().getName().equals(recipe.getName())){
            // player is trying to craft something else
            recipeFrequency.put(p.getUniqueId(), new RecipeFrequencyDO(0, recipe));
            frequencyDO = recipeFrequency.get(p.getUniqueId());
        }
        return frequencyDO.getFrequency() < recipe.getConsecutiveCrafts();
    }

    private static class RecipeFrequencyDO{
        private int frequency;
        private ImmersiveCraftingRecipe recipe;

        public RecipeFrequencyDO(int frequency, ImmersiveCraftingRecipe recipe) {
            this.frequency = frequency;
            this.recipe = recipe;
        }

        public ImmersiveCraftingRecipe getRecipe() {
            return recipe;
        }

        public int getFrequency() {
            return frequency;
        }

        public void setRecipe(ImmersiveCraftingRecipe recipe) {
            this.recipe = recipe;
        }

        public void setFrequency(int frequency) {
            this.frequency = frequency;
        }
    }
}
