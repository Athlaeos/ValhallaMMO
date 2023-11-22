package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicBrewingRecipe;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.SetModifiersMenu;
import me.athlaeos.valhallammo.gui.SetRecipeOptionMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static me.athlaeos.valhallammo.gui.implementations.RecipeOptionMenu.KEY_OPTION_ID;

public class BrewingRecipeEditor extends Menu implements SetModifiersMenu, SetRecipeOptionMenu {
    private static final NamespacedKey BUTTON_ACTION_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "button_action");
    private static final int ingredientIndex = 12;
    private static final Collection<Integer> applyOnIndexes = Set.of(28, 30, 32);
    private static final int resultIndex = 24;
    private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").get();

    private final DynamicBrewingRecipe recipe;

    private RecipeOption selectedChoice = null;

    private SlotEntry ingredient;
    private SlotEntry applyOn;
    private ItemStack result;
    private boolean requireValhallaTools;
    private boolean tinker;
    private boolean consumeIngredient;
    private List<DynamicItemModifier> modifiers;
    private boolean unlockedForEveryone;
    private int brewTime;
    private String displayName;
    private String description;

    private static final ItemStack toggleValhallaToolRequirementButton = new ItemBuilder(getButtonData("editor_recipe_brewing_valhallatoolrequirement", Material.DIAMOND_PICKAXE))
            .name("&eValhalla Tools")
            .stringTag(BUTTON_ACTION_KEY, "toggleValhallaToolRequirementButton")
            .lore("&7If enabled, the armors or tools",
                    "&7in the recipe need to have ValhallaMMO ",
                    "&7custom attributes. If disabled, vanilla ",
                    "&7equipment can be used too.",
                    "&eClick to toggle on/off")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack toggleTinkerButton = new ItemBuilder(getButtonData("editor_recipe_brewing_toggletinker", Material.ANVIL))
            .name("&eTinker Tools")
            .stringTag(BUTTON_ACTION_KEY, "toggleTinkerButton")
            .lore("&7If enabled, the recipe will",
                    "&7improve the input item of the recipe.",
                    "&7Otherwise, it will simply produce the",
                    "&7item specified as the result.",
                    "&eClick to toggle on/off")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack toggleConsumeIngredientButton = new ItemBuilder(getButtonData("editor_recipe_brewing_toggleconsumeingredient", Material.GOLDEN_APPLE))
            .name("&eConsume Ingredient")
            .stringTag(BUTTON_ACTION_KEY, "toggleConsumeIngredientButton")
            .lore("&7If enabled, the recipe will",
                    "&7consume 1 of the ingredient item like usual.",
                    "&7Otherwise, the ingredient will be left",
                    "&7in the brewing stand GUI.",
                    "&eClick to toggle on/off")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack modifierButton = new ItemBuilder(getButtonData("editor_recipe_brewing_modifiers", Material.WRITABLE_BOOK))
            .name("&dItem Modifiers")
            .stringTag(BUTTON_ACTION_KEY, "modifierButton")
            .lore("&7Modifiers are usually functions to edit",
                    "&7the output item based on player",
                    "&7stats or to apply crafting conditions.",
                    "&aThese modifiers specifically run on",
                    "&athe addition item if 'consume addition'",
                    "&ais disabled.",
                    "&eClick to open the menu",
                    "&8&m                <>                ",
                    "%modifiers%").get();
    private static final ItemStack brewTimeButton = new ItemBuilder(getButtonData("editor_recipe_brewing_brewtime", Material.CLOCK))
            .name("&eBrewing Time")
            .stringTag(BUTTON_ACTION_KEY, "brewTimeButton")
            .lore("&7How long it takes to brew this recipe.",
                    "&7Typical brewing time: &a400 ticks",
                    "&eClick to increase/decrease by 1 tick",
                    "&eShift-click to increase/decrease by 20 ticks")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack recipeOptionsButton = new ItemBuilder(getButtonData("editor_recipe_brewing_recipeoptions", Material.WRITABLE_BOOK))
            .name("&bIngredient Options")
            .stringTag(BUTTON_ACTION_KEY, "recipeOptionsButton")
            .lore("&7Ingredient options are ingredient",
                    "&7flags you can put on an ingredient or",
                    "&7input to change its behavior during",
                    "&7brewing.",
                    "&eClick to open the menu").get();
    private static final ItemStack toggleUnlockedForEveryoneButton = new ItemBuilder(getButtonData("editor_recipe_brewing_unlockedforeveryone", Material.PAPER))
            .name("&eUnlocked for Everyone")
            .stringTag(BUTTON_ACTION_KEY, "toggleUnlockedForEveryoneButton")
            .lore("&7If enabled, the recipe will",
                    "&7available to everyone regardless",
                    "&7if unlocked or not. Otherwise it",
                    "&7will have to be unlocked through",
                    "&7perk rewards.",
                    "&eClick to toggle on/off").get();
    private static final ItemStack setDisplayNameButton = new ItemBuilder(getButtonData("editor_recipe_brewing_setdisplayname", Material.NAME_TAG))
            .name("&aSet Display Name")
            .stringTag(BUTTON_ACTION_KEY, "setDisplayNameButton")
            .lore("&7Sets the display name of the recipe.",
                    "&7The display name is visible in the",
                    "&7recipe book and is usually used",
                    "&7to roughly describe the purpose of",
                    "&7tinkering recipes since the output is",
                    "&7non-descriptive.",
                    "&eLeft-Click to set display name",
                    "&cRight-Click to nullify display name",
                    "&7A nullified display name defaults to",
                    "&7the name of the input/output",
                    "&8&m                <>                ",
                    "&7Currently set to: %display_name%").get();
    private static final ItemStack setDescriptionButton = new ItemBuilder(getButtonData("editor_recipe_brewing_setdescription", Material.WRITABLE_BOOK))
            .name("&aSet Description")
            .stringTag(BUTTON_ACTION_KEY, "setDescriptionButton")
            .lore("&7Sets the description of the recipe.",
                    "&7The description is visible in the",
                    "&7recipe book as lore.",
                    "&eLeft-Click to set description",
                    "&cRight-Click to nullify description",
                    "&7A nullified description defaults to",
                    "&7a default default recipe format in",
                    "&7the language file.",
                    "&8&m                <>                ",
                    "&7Currently set to: %description%").get();


    private static final ItemStack confirmButton = new ItemBuilder(getButtonData("editor_save", Material.STRUCTURE_VOID))
            .name("&aConfirm")
            .stringTag(BUTTON_ACTION_KEY, "confirmButton")
            .lore("&aRight-click &7to save changes").get();
    private boolean confirmDeletion = false;
    private static final ItemStack deleteButton = new ItemBuilder(getButtonData("editor_delete", Material.BARRIER))
            .stringTag(BUTTON_ACTION_KEY, "deleteButton")
            .name("&cDelete Recipe").get();
    private static final ItemStack deleteConfirmButton = new ItemBuilder(getButtonData("editor_deleteconfirm", Material.BARRIER))
            .name("&cDelete Recipe")
            .stringTag(BUTTON_ACTION_KEY, "deleteConfirmButton")
            .enchant(Enchantment.DURABILITY, 1)
            .lore("&aRight-click &7to confirm recipe deletion")
            .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS).get();
    private static final ItemStack backToMenuButton = new ItemBuilder(getButtonData("editor_backtomenu", Material.BOOK))
            .stringTag(BUTTON_ACTION_KEY, "backToMenuButton")
            .name("&fBack to Menu").get();

    public BrewingRecipeEditor(PlayerMenuUtility playerMenuUtility, DynamicBrewingRecipe recipe) {
        super(playerMenuUtility);
        this.recipe = recipe;

        this.ingredient = recipe.getIngredient();
        this.applyOn = recipe.getApplyOn();
        this.result = recipe.getResult();
        this.requireValhallaTools = recipe.requireValhallaTools();
        this.tinker = recipe.tinker();
        this.consumeIngredient = recipe.consumeIngredient();
        this.modifiers = recipe.getModifiers();
        this.unlockedForEveryone = recipe.isUnlockedForEveryone();
        this.brewTime = recipe.getBrewTime();
        this.displayName = recipe.getDisplayName();
        this.description = recipe.getDescription();
    }

    public BrewingRecipeEditor(PlayerMenuUtility playerMenuUtility, DynamicBrewingRecipe recipe, String newName) {
        super(playerMenuUtility);
        this.recipe = new DynamicBrewingRecipe(newName);

        this.ingredient = recipe.getIngredient();
        this.applyOn = recipe.getApplyOn();
        this.result = recipe.getResult();
        this.requireValhallaTools = recipe.requireValhallaTools();
        this.tinker = recipe.tinker();
        this.consumeIngredient = recipe.consumeIngredient();
        this.modifiers = recipe.getModifiers();
        this.unlockedForEveryone = recipe.isUnlockedForEveryone();
        this.brewTime = recipe.getBrewTime();
        this.displayName = recipe.getDisplayName();
        this.description = recipe.getDescription();
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF204\uF80C\uF80A\uF808\uF802&8%recipe%" : TranslationManager.getTranslation("editormenu_brewingrecipes")).replace("%recipe%", recipe.getName());
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(!(e.getClickedInventory() instanceof PlayerInventory));

        ItemStack cursor = e.getCursor();
        ItemStack clicked = e.getCurrentItem();

        String action = ItemUtils.getPDCString(BUTTON_ACTION_KEY, clicked, "");
        if (!StringUtils.isEmpty(action)){
            switch (action){
                case "backToMenuButton" -> {
                    new RecipeOverviewMenu(playerMenuUtility, "brewing").open();
                    return;
                }
                case "modifierButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new DynamicModifierMenu(playerMenuUtility, this, false).open();
                    return;
                }
                case "recipeOptionsButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new RecipeOptionMenu(playerMenuUtility, this).open();
                    return;
                }
                case "deleteButton" -> {
                    confirmDeletion = true;
                    Utils.sendMessage(e.getWhoClicked(), "&cAre you sure you want to delete this recipe?");
                    setMenuItems();
                    return;
                }
                case "deleteConfirmButton" -> {
                    if (e.isRightClick()){
                        CustomRecipeRegistry.unregister(recipe.getName());
                        new RecipeOverviewMenu(playerMenuUtility, RecipeOverviewMenu.BREWING.getId()).open();
                        return;
                    }
                }
                case "setDisplayNameButton" -> {
                    if (e.isLeftClick()) {
                        playerMenuUtility.setPreviousMenu(this); // fallback in case player doesn't wanna
                        e.getWhoClicked().closeInventory();
                        Questionnaire questionaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                                new Question("&aWhat should the display name be? (type 'cancel' to cancel)", null, "")
                        ) {
                            @Override
                            public Action<Player> getOnFinish() {
                                if (getQuestions().isEmpty()) return super.getOnFinish();
                                Question question = getQuestions().get(0);
                                if (question.getAnswer() == null) return super.getOnFinish();
                                return (p) -> {
                                    String answer = Utils.chat(question.getAnswer());
                                    if (!answer.contains("cancel")) displayName = answer;
                                    playerMenuUtility.getPreviousMenu().open();
                                };
                            }
                        };
                        Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionaire);
                    } else displayName = null;
                }
                case "setDescriptionButton" -> {
                    if (e.isLeftClick()) {
                        playerMenuUtility.setPreviousMenu(this); // fallback in case player doesn't wanna
                        e.getWhoClicked().closeInventory();
                        Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                                new Question("&aWhat should the description be? Use '/n' to separate lines. (type 'cancel' to cancel)", null, "")
                        ) {
                            @Override
                            public Action<Player> getOnFinish() {
                                if (getQuestions().isEmpty()) return super.getOnFinish();
                                Question question = getQuestions().get(0);
                                if (question.getAnswer() == null) return super.getOnFinish();
                                return (p) -> {
                                    String answer = Utils.chat(question.getAnswer());
                                    if (!answer.contains("cancel")) description = answer;
                                    playerMenuUtility.getPreviousMenu().open();
                                };
                            }
                        };
                        Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionnaire);
                        Utils.sendMessage(e.getWhoClicked(), "&aValid placeholders are:");
                        Utils.sendMessage(e.getWhoClicked(), "          &2%ingredient% &afor the input item");
                        Utils.sendMessage(e.getWhoClicked(), "          &2%applyon% &afor the 'applied on' or potion slot item");
                        Utils.sendMessage(e.getWhoClicked(), "          &2%result% &afor a preformatted result");
                        Utils.sendMessage(e.getWhoClicked(), "          &2%tinker% &afor the raw name of the tinkered result");
                    } else description = null;
                }
                case "confirmButton" -> {
                    if (applyOn == null || ItemUtils.isEmpty(applyOn.getItem()) || ingredient == null || ItemUtils.isEmpty(ingredient.getItem()) || ItemUtils.isEmpty(result)) {
                        Utils.sendMessage(e.getWhoClicked(), "&cAn input and ingredient are required!");
                        setMenuItems();
                        return;
                    }
                    recipe.setIngredient(ingredient);
                    recipe.setApplyOn(applyOn);
                    recipe.setResult(result);
                    recipe.setRequireValhallaTools(requireValhallaTools);
                    recipe.setTinker(tinker);
                    recipe.setConsumeIngredient(consumeIngredient);
                    recipe.setModifiers(modifiers);
                    recipe.setUnlockedForEveryone(unlockedForEveryone);
                    recipe.setBrewTime(brewTime);
                    recipe.setDescription(description);
                    recipe.setDisplayName(displayName);

                    CustomRecipeRegistry.register(recipe, true);
                    CustomRecipeRegistry.setChangesMade();
                    new RecipeOverviewMenu(playerMenuUtility, RecipeOverviewMenu.BREWING.getId()).open();
                    return;
                }
                case "brewTimeButton" -> brewTime = Math.max(0, brewTime + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 20 : 1)));
                case "toggleValhallaToolRequirementButton" -> requireValhallaTools = !requireValhallaTools;
                case "toggleTinkerButton" -> tinker = !tinker;
                case "toggleUnlockedForEveryoneButton" -> unlockedForEveryone = !unlockedForEveryone;
                case "toggleConsumeIngredientButton" -> consumeIngredient = !consumeIngredient;
            }
        }

        if (ingredientIndex == e.getRawSlot() || applyOnIndexes.contains(e.getRawSlot()) || resultIndex == e.getRawSlot()){
            if (e.getRawSlot() == resultIndex){
                if (!tinker && !ItemUtils.isEmpty(cursor)) result = cursor.clone();
            } else {
                // clicked non-result slot
                if (!ItemUtils.isEmpty(cursor)){
                    String option = ItemUtils.getPDCString(KEY_OPTION_ID, cursor, null);
                    if (option == null){
                        if (e.getRawSlot() == ingredientIndex)
                            ingredient = new SlotEntry(new ItemBuilder(cursor.clone()).amount(1).get(), new MaterialChoice());
                        else if (applyOnIndexes.contains(e.getRawSlot()))
                            applyOn = new SlotEntry(new ItemBuilder(cursor.clone()).amount(1).get(), new MaterialChoice());
                    } else {
                        if (selectedChoice != null) {
                            if (e.getRawSlot() == ingredientIndex){
                                if (selectedChoice.isCompatible(ingredient.getItem()) && selectedChoice.isCompatibleWithInputItem(false)){
                                    if (selectedChoice instanceof IngredientChoice c) ingredient.setOption(c);
                                    e.getWhoClicked().setItemOnCursor(null);
                                } else Utils.sendMessage(e.getWhoClicked(), "&cNot compatible with this item");
                            } else if (applyOnIndexes.contains(e.getRawSlot())){
                                if (selectedChoice.isCompatible(applyOn.getItem()) && selectedChoice.isCompatibleWithInputItem(true)){
                                    if (selectedChoice instanceof IngredientChoice c) applyOn.setOption(c);
                                    e.getWhoClicked().setItemOnCursor(null);
                                } else Utils.sendMessage(e.getWhoClicked(), "&cNot compatible with this item");
                            }
                        }
                    }
                } else {
                    if (e.getRawSlot() == ingredientIndex){
                        if (ingredient != null){
                            ingredient.setOption(null);
                        }
                    } else if (applyOnIndexes.contains(e.getRawSlot())){
                        if (applyOn != null){
                            applyOn.setOption(null);
                        }
                    }
                }
            }
        }

        confirmDeletion = false;
        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        e.setCancelled(true);
        if (e.getRawSlots().size() == 1){
            ClickType type = e.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT;
            InventoryAction action = e.getType() == DragType.EVEN ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
            handleMenu(new InventoryClickEvent(e.getView(), InventoryType.SlotType.CONTAINER, new ArrayList<>(e.getRawSlots()).get(0), type, action));
        }
    }

    @Override
    public void setMenuItems() {
        inventory.clear();
        if (!ValhallaMMO.getPluginConfig().getBoolean("admin_gui_filler_removal")) for (int i = 0; i < 54; i++) inventory.setItem(i, filler);
        inventory.setItem(ingredientIndex, null);
        for (Integer i : applyOnIndexes) inventory.setItem(i, null);
        if (!tinker) inventory.setItem(resultIndex, null);
        else inventory.setItem(resultIndex, new ItemBuilder(applyOn.getItem().getType()).name("&dTinkered Base").get());

        List<String> ingredientLore = SlotEntry.getOptionLore(ingredient);
        List<String> applyOnLore = SlotEntry.getOptionLore(applyOn);

        ItemStack ingredientIcon = new ItemBuilder(ingredient.getItem().clone()).appendLore(ingredientLore).get();
        ItemStack applyOnIcon = new ItemBuilder(applyOn.getItem().clone()).appendLore(applyOnLore).get();

        inventory.setItem(ingredientIndex, ingredientIcon);
        for (Integer i : applyOnIndexes) inventory.setItem(i, applyOnIcon);
        if (!tinker) inventory.setItem(resultIndex, result.clone());

        List<String> modifierLore = new ArrayList<>();
        modifiers.forEach(m -> modifierLore.addAll(StringUtils.separateStringIntoLines("&d> " + m.getActiveDescription(), 40)));

        ItemMeta resultMeta = ItemUtils.getItemMeta(result);
        String tinkerFormat = TranslationManager.getTranslation("tinker_result_format");
        List<String> description = Arrays.asList(this.description == null ? "&eDefault".split("/n") :
                this.description
                        .replace("%ingredient%", SlotEntry.toString(ingredient))
                        .replace("%applyon%", SlotEntry.toString(applyOn))
                        .replace("%tinker%", tinker ? SlotEntry.toString(applyOn) : ItemUtils.getItemName(resultMeta))
                        .replace("%result%", tinker ? tinkerFormat.replace("%item%", SlotEntry.toString(applyOn)) : ItemUtils.getItemName(resultMeta))
                        .split("/n"));

        inventory.setItem(0, new ItemBuilder(setDisplayNameButton).lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(setDisplayNameButton), "%display_name%", List.of(displayName == null ? "&eDefault" : displayName))).get());
        inventory.setItem(1, new ItemBuilder(setDescriptionButton).lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(setDescriptionButton), "%description%", description)).get());
        inventory.setItem(3, new ItemBuilder(toggleConsumeIngredientButton).name("&eConsume Ingredient: " + (consumeIngredient ? "&aYes" : "&fNo")).get());
        inventory.setItem(5, new ItemBuilder(toggleUnlockedForEveryoneButton).name("&eUnlocked for Everyone " + (unlockedForEveryone ? "&aYes" : "&fNo")).get());
        inventory.setItem(10, recipeOptionsButton);
        inventory.setItem(17, new ItemBuilder(toggleValhallaToolRequirementButton).name("&eValhalla Tools: " + (requireValhallaTools ? "&aYes" : "&fNo")).get());
        inventory.setItem(21, new ItemBuilder(brewTimeButton).name(String.format("&fTime to brew: &e%d, %ss", brewTime, StringUtils.toTimeStamp2(brewTime, 20))).get());
        inventory.setItem(25, new ItemBuilder(modifierButton).lore(modifierLore).get());
        inventory.setItem(35, new ItemBuilder(toggleTinkerButton).name("&eTinker: " + (tinker ? "&aYes" : "&fNo")).get());
        inventory.setItem(45, confirmDeletion ? deleteConfirmButton : deleteButton);
        inventory.setItem(49, backToMenuButton);
        inventory.setItem(53, confirmButton);
    }

    @Override
    public void setResultModifiers(List<DynamicItemModifier> resultModifiers) {
        this.modifiers = resultModifiers;
    }

    @Override
    public List<DynamicItemModifier> getResultModifiers() {
        return modifiers;
    }

    @Override
    public void setRecipeOption(RecipeOption option) {
        this.selectedChoice = option;
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () ->
                playerMenuUtility.getOwner().setItemOnCursor(new ItemBuilder(option.getIcon()).stringTag(KEY_OPTION_ID, option.getName()).get()), 1L);
    }
}
