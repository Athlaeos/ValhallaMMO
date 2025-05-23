package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ResultChangingModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.ImmersiveCraftingRecipe;
import me.athlaeos.valhallammo.dom.Comparator;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.listeners.ImmersiveRecipeListener;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class ImmersiveRecipeSelectionMenu extends Menu {
    private static final Collection<UUID> playersWhoReceivedFirstTimeMessage = new HashSet<>();
    private static final NamespacedKey BUTTON_ACTION_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "button_action");
    private static final NamespacedKey BUTTON_RECIPE_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "button_recipe");
    private static final int filter = 53;
    private static final int predictedResult = 43;
    private static final List<Integer> recipeIndexes = List.of(
            0,  1,  2,  3,  4,
            9,  10, 11, 12, 13,
            18, 19, 20, 21, 22,
            27, 28, 29, 30, 31,
            36, 37, 38, 39, 40,
            45, 46, 47, 48, 49
    );
    private static final List<Integer> ingredientIndexes = List.of(
            6,  7,  8,
            15, 16, 17,
            24, 25, 26
    );
    private static final ItemStack pageUp = new ItemBuilder(getButtonData("recipeselection_immersive_pageup", Material.ARROW))
            .stringTag(BUTTON_ACTION_KEY, "pageUp")
            .name(TranslationManager.getTranslation("translation_page_up")).get();
    private static final ItemStack pageDown = new ItemBuilder(getButtonData("recipeselection_immersive_pagedown", Material.ARROW))
            .stringTag(BUTTON_ACTION_KEY, "pageDown")
            .name(TranslationManager.getTranslation("translation_page_down")).get();
    private static final ItemStack favouritesButton = new ItemBuilder(getButtonData("recipeselection_immersive_favourites", Material.NETHER_STAR))
            .name(TranslationManager.getTranslation("selectionmenu_recipe_immersive_favourites"))
            .stringTag(BUTTON_ACTION_KEY, "favouritesButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack craftOnlyButton = new ItemBuilder(getButtonData("recipeselection_immersive_craftonly", Material.CRAFTING_TABLE))
            .name(TranslationManager.getTranslation("selectionmenu_recipe_immersive_craftonly"))
            .stringTag(BUTTON_ACTION_KEY, "craftOnlyButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack tinkerOnlyButton = new ItemBuilder(getButtonData("recipeselection_immersive_tinkeronly", Material.ANVIL))
            .name(TranslationManager.getTranslation("selectionmenu_recipe_immersive_tinkeronly"))
            .stringTag(BUTTON_ACTION_KEY, "tinkerOnlyButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();

    private View view;
    private final Block clicked;
    private ImmersiveCraftingRecipe selectedRecipe = null;
    private final Collection<ImmersiveCraftingRecipe> recipes = new HashSet<>();
    private ItemStack recipeFilter = null;
    private int page = 1;
    private ItemBuilder predictedItem = null;

    public ImmersiveRecipeSelectionMenu(PlayerMenuUtility playerMenuUtility, Block clicked, boolean craft) {
        super(playerMenuUtility);
        this.clicked = clicked;
        this.view = craft ? View.CRAFTING : View.TINKERING;
        Player p = playerMenuUtility.getOwner();
        PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
        if (profile == null) return;
        boolean allAllowed = p.hasPermission("valhalla.allrecipes");
        CustomRecipeRegistry.getImmersiveRecipesByBlock().getOrDefault(clicked.getType(), new HashSet<>()).forEach(r -> {
            if (r.isUnlockedForEveryone() || allAllowed || profile.getUnlockedRecipes().contains(r.getName())
                    || p.hasPermission("valhalla.recipe." + r.getName())){
                recipes.add(r);
            }
        });
        this.predictedItem = predictResult();
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF002\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("selectionmenu_recipe_immersive"));
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (e.getClickedInventory() instanceof PlayerInventory){
            if (ItemUtils.isEmpty(clicked)){
                recipeFilter = null;
            } else {
                recipeFilter = clicked.clone();
                recipeFilter.setAmount(1);
            }
        } else if (ItemUtils.isEmpty(clicked)) return;
        String action = ItemUtils.getPDCString(BUTTON_ACTION_KEY, clicked, null);
        if (!StringUtils.isEmpty(action)){
            switch (action){
                case "pageUp" -> page--;
                case "pageDown" -> page++;
                case "favouritesButton" -> view = View.FAVOURITES;
                case "craftOnlyButton" -> view = View.CRAFTING;
                case "tinkerOnlyButton" -> view = View.TINKERING;
            }
        } else if (e.getRawSlot() == filter) recipeFilter = null;
        else if (e.getRawSlot() == predictedResult || recipeIndexes.contains(e.getRawSlot())){
            String r = ItemUtils.getPDCString(BUTTON_RECIPE_KEY, clicked, null);
            if (!StringUtils.isEmpty(r)){
                ImmersiveCraftingRecipe recipe = CustomRecipeRegistry.getImmersiveRecipes().get(r);
                if (recipe != null){
                    if (e.isShiftClick()) {
                        if (isFavourited(playerMenuUtility.getOwner(), r)) removeFavourite(playerMenuUtility.getOwner(), r);
                        else addFavourite(playerMenuUtility.getOwner(), r);
                    } else if (selectedRecipe != null && this.predictedItem != null && !ItemUtils.isEmpty(this.predictedItem.getItem()) && recipe.getName().equals(selectedRecipe.getName())){
                        // check if clicked recipe is the same as the selected recipe, in which case proceed with crafting
                        // check if craftable and if validations check out
                        if (CustomFlag.hasFlag(this.predictedItem.getMeta(), CustomFlag.UNCRAFTABLE)){
                            Utils.sendMessage(e.getWhoClicked(), ItemUtils.getPDCString(DynamicItemModifier.ERROR_MESSAGE, this.predictedItem.getMeta(), ""));
                        } else {
                            if (recipe.tinker()){
                                playerMenuUtility.getOwner().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                                Utils.chat((playersWhoReceivedFirstTimeMessage.contains(e.getWhoClicked().getUniqueId()) ?
                                                        TranslationManager.getTranslation("status_tinkering_recipe_selected") :
                                                        TranslationManager.getTranslation("status_onetime_tinkering_recipe_selected")))
                                                        .replace("%recipe%", recipe.getDisplayName() == null ? ItemUtils.getItemName(ItemUtils.getItemMeta(clicked)) : recipe.getDisplayName())
                                                        .replace("%item%", ItemUtils.getItemName(ItemUtils.getItemMeta(recipe.getResult())))
                                        )
                                );
                            } else {
                                playerMenuUtility.getOwner().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                                Utils.chat((playersWhoReceivedFirstTimeMessage.contains(e.getWhoClicked().getUniqueId()) ?
                                                        TranslationManager.getTranslation("status_crafting_recipe_selected") :
                                                        TranslationManager.getTranslation("status_onetime_crafting_recipe_selected")))
                                                        .replace("%recipe%", recipe.getDisplayName() == null ? ItemUtils.getItemName(ItemUtils.getItemMeta(clicked)) : recipe.getDisplayName())
                                                        .replace("%item%", ItemUtils.getItemName(ItemUtils.getItemMeta(recipe.getResult())))
                                        )
                                );
                            }
                            playersWhoReceivedFirstTimeMessage.add(e.getWhoClicked().getUniqueId());
                            ImmersiveRecipeListener.getSelectedImmersiveRecipe().put(e.getWhoClicked().getUniqueId(), recipe);
                            e.getWhoClicked().closeInventory();
                        }
                    } else {
                        this.selectedRecipe = recipe;
                        this.predictedItem = predictResult();
                    }
                }
            }
        }
        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void setMenuItems() {
        inventory.clear();

        inventory.setItem(filter, recipeFilter);
        inventory.setItem(predictedResult, predictedItem == null ? null : predictedItem.get());
        inventory.setItem(5, favouritesButton);
        inventory.setItem(14, craftOnlyButton);
        inventory.setItem(23, tinkerOnlyButton);

        buildMenuItems((buttons) -> {
            buttons.sort(java.util.Comparator.comparing(this::isIconFavourited, java.util.Comparator.reverseOrder()).thenComparing(ItemStack::getType).thenComparing(item -> ChatColor.stripColor(ItemUtils.getItemName(ItemUtils.getItemMeta(item)))));

            List<ItemStack> p;
            int pageCount = 1;
            if (buttons.size() >= recipeIndexes.size()){
                Map<Integer, List<ItemStack>> pages = Utils.paginate(recipeIndexes.size(), buttons);
                pageCount = pages.size();
                if (page > pages.size()) page = pages.size();
                else if (page < 1) page = 1;
                p = pages.get(page - 1);
            } else {
                p = buttons;
            }
            for (int i = 0; i < p.size(); i++){
                inventory.setItem(recipeIndexes.get(i), p.get(i));
            }

            if (page < pageCount){
                inventory.setItem(50, pageDown);
            }
            if (page > 1){
                inventory.setItem(41, pageUp);
            }

            if (selectedRecipe != null){
                List<ItemStack> separatedIngredients = ItemUtils.decompressStacks(selectedRecipe.getIngredients()).stream().limit(9).toList();
                for (int i = 0; i < separatedIngredients.size(); i++){
                    inventory.setItem(ingredientIndexes.get(i), separatedIngredients.get(i));
                }
            }
        });
    }

    private void buildMenuItems(final ItemBuilderCallback callback){
        List<String> defaultFormat = TranslationManager.getListTranslation("immersive_recipe_button_format");
        String ingredientFormat = TranslationManager.getTranslation("recipe_ingredient_format");
        double craftingTimeReduction = AccumulativeStatManager.getCachedStats("CRAFTING_TIME_REDUCTION", playerMenuUtility.getOwner(), 10000, true);
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
            List<ItemStack> icons = new ArrayList<>();
            for (ImmersiveCraftingRecipe recipe : recipes){
                if (!view.shouldShow(playerMenuUtility.getOwner(), recipe)) continue;

                if (ValhallaMMO.isWorldBlacklisted(clicked.getWorld().getName()) ||
                        (WorldGuardHook.inDisabledRegion(clicked.getLocation(), WorldGuardHook.VMMO_CRAFTING_IMMERSIVE)) ||
                        (recipe.getValidations().stream().anyMatch(v -> {
                            Validation validation = ValidationRegistry.getValidation(v);
                            if (validation != null) return !validation.validate(clicked);
                            return false;
                        }))) continue;
                if (!ItemUtils.isEmpty(recipeFilter)){
                    boolean filterMatch = false;
                    if (recipe.tinker() && recipe.getTinkerInput().getOption().matches(recipe.getTinkerInput().getItem(), recipeFilter))
                        filterMatch = true;
                    else if (recipe.getIngredients().keySet().stream().anyMatch(i -> recipe.getMetaRequirement().getChoice().matches(i, recipeFilter)))
                        filterMatch = true;
                    if (!filterMatch) continue;
                }

                if (selectedRecipe != null && selectedRecipe.getName().equals(recipe.getName()) && !ItemUtils.isEmpty(predictedItem.getItem())){
                    icons.add(predictedItem.name("&r&f" + ItemUtils.getItemName(predictedItem.getMeta())).get());
                    continue;
                }

                ItemStack hand = playerMenuUtility.getOwner().getInventory().getItemInMainHand();
                // take hand item if it's a tinkering recipe and the hand item matches the tinkering input,
                // if no match take the recipe input item instead, or if not tinkering the recipe result
                ItemStack button =
                        (recipe.tinker() ?
                        (!ItemUtils.isEmpty(hand) && recipe.getTinkerInput().getOption().matches(recipe.getTinkerInput().getItem(), hand) ?
                                hand :
                                recipe.getTinkerInput().getItem()) :
                        recipe.getResult()).clone();
                ResultChangingModifier changingModifier = (ResultChangingModifier) recipe.getModifiers().stream().filter(m -> m instanceof ResultChangingModifier).findFirst().orElse(null);
                if (changingModifier != null) button = changingModifier.getNewResult(ModifierContext.builder(new ItemBuilder(button)).crafter(playerMenuUtility.getOwner()).get());
                List<String> lore = new ArrayList<>(recipe.getDescription() == null ?
                        defaultFormat :
                        Arrays.asList(recipe.getDescription().split("/n"))
                );
                lore = ItemUtils.setListPlaceholder(lore, "%ingredients%",
                        recipe.getIngredients().keySet().stream().map(i -> {
                            int amount = recipe.getIngredients().get(i);
                            return ingredientFormat
                                    .replace("%amount%", String.valueOf(amount))
                                    .replace("%item%", recipe.getMetaRequirement().getChoice().ingredientDescription(i));
                        }).collect(Collectors.toList())
                );
                String time = String.format("%.1f", (Math.max(0, recipe.getTimeToCraft() * (1 - craftingTimeReduction))/20D));
                lore = lore.stream().map(l -> l.replace("%crafting_time%", time)).collect(Collectors.toList());

                String displayName = recipe.getDisplayName() == null ? ItemUtils.getItemName(ItemUtils.getItemMeta(button)) : recipe.getDisplayName();
                String favouritePrefix = TranslationManager.getTranslation("recipe_favourited_prefix");
                String favouriteSuffix = TranslationManager.getTranslation("recipe_favourited_suffix");
                icons.add(new ItemBuilder(button).name("&r" +
                        (isFavourited(playerMenuUtility.getOwner(), recipe.getName()) ?
                                favouritePrefix + displayName + favouriteSuffix :
                                displayName)).lore(lore).stringTag(BUTTON_RECIPE_KEY, recipe.getName()).translate().get());
            }

            ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> callback.onItemsBuilt(icons));
        });

    }

    private ItemBuilder predictResult(){
        if (selectedRecipe == null) return null;
        ItemStack hand = playerMenuUtility.getOwner().getInventory().getItemInMainHand();
        ItemStack button =
                (selectedRecipe.tinker() ?
                        (!ItemUtils.isEmpty(hand) && selectedRecipe.getTinkerInput().getOption().matches(selectedRecipe.getTinkerInput().getItem(), hand) ?
                                hand :
                                selectedRecipe.getTinkerInput().getItem()) :
                        selectedRecipe.getResult()).clone();
        for (String v : selectedRecipe.getValidations()){
            Validation validation = ValidationRegistry.getValidation(v);
            if (validation != null && !validation.validate(clicked)){
                return new ItemBuilder(button).flag(CustomFlag.UNCRAFTABLE).lore(validation.validationError());
            }
        }
        ItemBuilder result = new ItemBuilder(button);
        DynamicItemModifier.modify(ModifierContext.builder(result).crafter(playerMenuUtility.getOwner()).validate().get(), selectedRecipe.getModifiers());
        if (ItemUtils.isEmpty(result.getItem())) return null;
        return result.stringTag(BUTTON_RECIPE_KEY, selectedRecipe.getName());
    }

    private enum View{
        CRAFTING((p, r) -> !r.tinker()),
        TINKERING((p, r) -> r.tinker()),
        FAVOURITES((p, r) -> isFavourited(p, r.getName()));
        private final Comparator<Player, ImmersiveCraftingRecipe> viewChecker;
        View(Comparator<Player, ImmersiveCraftingRecipe> viewChecker){
            this.viewChecker = viewChecker;
        }
        public boolean shouldShow(Player p, ImmersiveCraftingRecipe recipe){
            return viewChecker.compare(p, recipe);
        }
    }

    private interface ItemBuilderCallback{
        void onItemsBuilt(List<ItemStack> items);
    }

    private static final NamespacedKey KEY_FAVOURITES = new NamespacedKey(ValhallaMMO.getInstance(), "favourited_recipes");

    public static boolean isFavourited(Player p, String recipe){
        return getFavourites(p).contains(recipe);
    }

    public static List<String> getFavourites(Player p){
        List<String> favourites = new ArrayList<>();
        if (p.getPersistentDataContainer().has(KEY_FAVOURITES, PersistentDataType.STRING)){
            String value = p.getPersistentDataContainer().get(KEY_FAVOURITES, PersistentDataType.STRING);
            if (value == null) return favourites;
            favourites.addAll(Arrays.asList(value.split("<splitter>")));
        }
        return favourites;
    }

    public static void setFavourites(Player p, List<String> favourites){
        p.getPersistentDataContainer().set(KEY_FAVOURITES, PersistentDataType.STRING, String.join("<splitter>", favourites));
    }

    public static void addFavourite(Player p, String recipe){
        List<String> favourites = getFavourites(p);
        favourites.add(recipe);
        setFavourites(p, favourites);
    }

    public static void removeFavourite(Player p, String favourite){
        List<String> favourites = getFavourites(p);
        favourites.remove(favourite);
        setFavourites(p, favourites);
    }

    private boolean isIconFavourited(ItemStack i){
        String recipe = ItemUtils.getPDCString(BUTTON_RECIPE_KEY, i, "");
        return isFavourited(playerMenuUtility.getOwner(), recipe);
    }
}
