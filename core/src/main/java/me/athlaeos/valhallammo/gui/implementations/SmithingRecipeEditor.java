package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicSmithingRecipe;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.IngredientChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.gui.*;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Scheduling;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.EnchantmentMappings;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static me.athlaeos.valhallammo.gui.implementations.RecipeOptionMenu.KEY_OPTION_ID;

public class SmithingRecipeEditor extends Menu implements SetModifiersMenu, SetRecipeOptionMenu, SetValidationsMenu {
    private static final NamespacedKey BUTTON_ACTION_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "button_action");
    private static final int templateIndex = 20;
    private static final int baseIndex = 21;
    private static final int additionIndex = 22;
    private static final int resultIndex = 24;
    private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").get();

    private final DynamicSmithingRecipe recipe;

    private RecipeOption selectedChoice = null;

    private SlotEntry template;
    private SlotEntry base;
    private SlotEntry addition;
    private ItemStack result;
    private boolean requireValhallaTools;
    private boolean tinker;
    private boolean consumeAddition;
    private List<DynamicItemModifier> additionModifiers;
    private List<DynamicItemModifier> resultModifiers;
    private boolean unlockedForEveryone;
    private Collection<String> validations;
    private String displayName;
    private String description;

    private static final ItemStack toggleValhallaToolRequirementButton = new ItemBuilder(getButtonData("editor_recipe_smithing_valhallatoolrequirement", Material.DIAMOND_PICKAXE))
            .name("&eValhalla Tools")
            .stringTag(BUTTON_ACTION_KEY, "toggleValhallaToolRequirementButton")
            .lore("&7If enabled, the armors or tools",
                    "&7in the recipe need to have ValhallaMMO ",
                    "&7custom attributes. If disabled, vanilla ",
                    "&7equipment can be used too.",
                    "&eClick to toggle on/off")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack toggleTinkerButton = new ItemBuilder(getButtonData("editor_recipe_smithing_toggletinker", Material.ANVIL))
            .name("&eTinker Tools")
            .stringTag(BUTTON_ACTION_KEY, "toggleTinkerButton")
            .lore("&7If enabled, the recipe will",
                    "&7improve the input item of the recipe.",
                    "&7Otherwise, it will simply produce the",
                    "&7item specified as the result.",
                    "&eClick to toggle on/off")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack toggleConsumeAdditionButton = new ItemBuilder(getButtonData("editor_recipe_smithing_toggleconsumeaddition", Material.GOLDEN_APPLE))
            .name("&eConsume Addition")
            .stringTag(BUTTON_ACTION_KEY, "toggleConsumeAdditionButton")
            .lore("&7If enabled, the recipe will",
                    "&7consume 1 of the addition item like usual.",
                    "&7Otherwise, the addition will be left",
                    "&7in the smithing table GUI which also",
                    "&7allows it to be tinkered.",
                    "&eClick to toggle on/off")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack selectValidationButton = new ItemBuilder(getButtonData("editor_recipe_smithing_selectvalidation", Material.BARRIER))
            .name("&eBlock condition")
            .stringTag(BUTTON_ACTION_KEY, "selectValidationButton")
            .lore("&7Block conditions are checks",
                    "&7to allow/disallow recipes based on",
                    "&7block environment.",
                    "&7These conditions may also change",
                    "&7the environment after crafting.",
                    "&eClick to open the menu",
                    "&8&m                <>                ",
                    "%description%").get();
    private static final ItemStack additionModifierButton = new ItemBuilder(getButtonData("editor_recipe_smithing_additionmodifiers", Material.WRITABLE_BOOK))
            .name("&dAddition Item Modifiers")
            .stringTag(BUTTON_ACTION_KEY, "additionModifierButton")
            .lore("&7Modifiers are usually functions to edit",
                    "&7the output item based on player",
                    "&7stats or to apply crafting conditions.",
                    "&aThese modifiers specifically run on",
                    "&athe addition item if 'consume addition'",
                    "&ais disabled.",
                    "&eClick to open the menu",
                    "&8&m                <>                ",
                    "%modifiers%").get();
    private static final ItemStack resultModifierButton = new ItemBuilder(getButtonData("editor_recipe_smithing_resultmodifiers", Material.WRITABLE_BOOK))
            .name("&dResult Item Modifiers")
            .stringTag(BUTTON_ACTION_KEY, "resultModifierButton")
            .lore("&7Modifiers are functions to edit",
                    "&7the output item based on player",
                    "&7stats or to apply crafting conditions.",
                    "&eClick to open the menu",
                    "&8&m                <>                ",
                    "%modifiers%").get();
    private static final ItemStack recipeOptionsButton = new ItemBuilder(getButtonData("editor_recipe_smithing_recipeoptions", Material.WRITABLE_BOOK))
            .name("&bIngredient Options")
            .stringTag(BUTTON_ACTION_KEY, "recipeOptionsButton")
            .lore("&7Ingredient options are ingredient",
                    "&7flags you can put on an ingredient",
                    "&7to change its behavior during crafting.",
                    "&eClick to open the menu").get();
    private static final ItemStack toggleUnlockedForEveryoneButton = new ItemBuilder(getButtonData("editor_recipe_smithing_unlockedforeveryone", Material.PAPER))
            .name("&eUnlocked for Everyone")
            .stringTag(BUTTON_ACTION_KEY, "toggleUnlockedForEveryoneButton")
            .lore("&7If enabled, the recipe will",
                    "&7available to everyone regardless",
                    "&7if unlocked or not. Otherwise it",
                    "&7will have to be unlocked through",
                    "&7perk rewards.",
                    "&eClick to toggle on/off").get();
    private static final ItemStack setDisplayNameButton = new ItemBuilder(getButtonData("editor_recipe_smithing_setdisplayname", Material.NAME_TAG))
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
    private static final ItemStack setDescriptionButton = new ItemBuilder(getButtonData("editor_recipe_smithing_setdescription", Material.WRITABLE_BOOK))
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
            .enchant(EnchantmentMappings.UNBREAKING.getEnchantment(), 1)
            .lore("&aRight-click &7to confirm recipe deletion")
            .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS).get();
    private static final ItemStack backToMenuButton = new ItemBuilder(getButtonData("editor_backtomenu", Material.BOOK))
            .stringTag(BUTTON_ACTION_KEY, "backToMenuButton")
            .name("&fBack to Menu").get();

    public SmithingRecipeEditor(PlayerMenuUtility playerMenuUtility, DynamicSmithingRecipe recipe) {
        super(playerMenuUtility);
        this.recipe = recipe;

        this.template = recipe.getTemplate();
        this.base = recipe.getBase();
        this.addition = recipe.getAddition();
        this.result = recipe.getResult();
        this.requireValhallaTools = recipe.requireValhallaTools();
        this.tinker = recipe.tinkerBase();
        this.consumeAddition = recipe.consumeAddition();
        this.resultModifiers = recipe.getResultModifiers();
        this.additionModifiers = recipe.getAdditionModifiers();
        this.unlockedForEveryone = recipe.isUnlockedForEveryone();
        this.validations = recipe.getValidations();
        this.displayName = recipe.getDisplayName();
        this.description = recipe.getDescription();
    }

    public SmithingRecipeEditor(PlayerMenuUtility playerMenuUtility, DynamicSmithingRecipe recipe, String newName) {
        super(playerMenuUtility);
        this.recipe = new DynamicSmithingRecipe(newName);

        this.template = new SlotEntry(recipe.getTemplate().getItem().clone(), recipe.getTemplate().getOption());
        this.base = new SlotEntry(recipe.getBase().getItem().clone(), recipe.getBase().getOption());
        this.addition = new SlotEntry(recipe.getAddition().getItem().clone(), recipe.getAddition().getOption());
        this.result = recipe.getResult().clone();
        this.requireValhallaTools = recipe.requireValhallaTools();
        this.tinker = recipe.tinkerBase();
        this.consumeAddition = recipe.consumeAddition();
        this.resultModifiers = new ArrayList<>(recipe.getResultModifiers().stream().map(DynamicItemModifier::copy).toList());
        this.additionModifiers = new ArrayList<>(recipe.getAdditionModifiers().stream().map(DynamicItemModifier::copy).toList());
        this.unlockedForEveryone = recipe.isUnlockedForEveryone();
        this.validations = new HashSet<>(recipe.getValidations());
        this.displayName = recipe.getDisplayName();
        this.description = recipe.getDescription();
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF203\uF80C\uF80A\uF808\uF802&8%recipe%" : TranslationManager.getTranslation("editormenu_smithingrecipes")).replace("%recipe%", recipe.getName());
    }

    @Override
    public int getSlots() {
        return 54;
    }

    private String lastAction = null;

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(!(e.getClickedInventory() instanceof PlayerInventory));

        ItemStack cursor = e.getCursor();
        ItemStack clicked = e.getCurrentItem();

        String action = ItemUtils.getPDCString(BUTTON_ACTION_KEY, clicked, "");
        if (!StringUtils.isEmpty(action)){
            lastAction = action;
            switch (action){
                case "backToMenuButton" -> {
                    new RecipeOverviewMenu(playerMenuUtility, "smithing").open();
                    return;
                }
                case "resultModifierButton", "additionModifierButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new DynamicModifierMenu(playerMenuUtility, this, true).open();
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
                        new RecipeOverviewMenu(playerMenuUtility, RecipeOverviewMenu.SMITHING.getId()).open();
                        return;
                    }
                }
                case "selectValidationButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new ValidationSelectionMenu(playerMenuUtility, this, Material.SMITHING_TABLE).open();
                    return;
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
                        Utils.sendMessage(e.getWhoClicked(), "          &2%template% &afor the template item");
                        Utils.sendMessage(e.getWhoClicked(), "          &2%base% &afor the base item");
                        Utils.sendMessage(e.getWhoClicked(), "          &2%addition% &afor the added item");
                        Utils.sendMessage(e.getWhoClicked(), "          &2%result% &afor a preformatted result");
                        Utils.sendMessage(e.getWhoClicked(), "          &2%tinker% &afor the raw name of the tinkered result");
                    } else description = null;
                }
                case "confirmButton" -> {
                    if (base == null || ItemUtils.isEmpty(base.getItem()) || addition == null || ItemUtils.isEmpty(addition.getItem()) || ItemUtils.isEmpty(result)) {
                        Utils.sendMessage(e.getWhoClicked(), "&cA base and addition are required!");
                        setMenuItems();
                        return;
                    }
                    recipe.setTemplate(template);
                    recipe.setBase(base);
                    recipe.setAddition(addition);
                    recipe.setResult(result);
                    recipe.setRequireValhallaTools(requireValhallaTools);
                    recipe.setTinkerBase(tinker);
                    recipe.setConsumeAddition(consumeAddition);
                    recipe.setResultModifiers(resultModifiers);
                    recipe.setAdditionModifiers(additionModifiers);
                    recipe.setUnlockedForEveryone(unlockedForEveryone);
                    recipe.setValidations(validations);
                    recipe.setDescription(description);
                    recipe.setDisplayName(displayName);
                    CustomRecipeRegistry.register(recipe, true);
                    CustomRecipeRegistry.setChangesMade();
                    new RecipeOverviewMenu(playerMenuUtility, "smithing").open();
                    return;
                }
                case "toggleValhallaToolRequirementButton" -> requireValhallaTools = !requireValhallaTools;
                case "toggleTinkerButton" -> tinker = !tinker;
                case "toggleUnlockedForEveryoneButton" -> unlockedForEveryone = !unlockedForEveryone;
                case "toggleConsumeAdditionButton" -> consumeAddition = !consumeAddition;
            }
        }

        if (templateIndex == e.getRawSlot() || baseIndex == e.getRawSlot() || additionIndex == e.getRawSlot() || resultIndex == e.getRawSlot()){
            if (e.getRawSlot() == resultIndex){
                if (!tinker && !ItemUtils.isEmpty(cursor)) result = cursor.clone();
            } else {
                // clicked non-result slot
                if (!ItemUtils.isEmpty(cursor)){
                    String option = ItemUtils.getPDCString(KEY_OPTION_ID, cursor, null);
                    if (option == null){
                        if (e.getRawSlot() == templateIndex)
                            template = new SlotEntry(new ItemBuilder(cursor.clone()).amount(1).get(), new MaterialChoice());
                        else if (e.getRawSlot() == baseIndex)
                            base = new SlotEntry(new ItemBuilder(cursor.clone()).amount(1).get(), new MaterialChoice());
                        else if (e.getRawSlot() == additionIndex)
                            addition = new SlotEntry(new ItemBuilder(cursor.clone()).amount(1).get(), new MaterialChoice());
                    } else {
                        if (selectedChoice != null) {
                            if (e.getRawSlot() == templateIndex){
                                if (selectedChoice.isCompatible(template.getItem()) && selectedChoice.isCompatibleWithInputItem(false)){
                                    template.setOption(selectedChoice);
                                    e.getWhoClicked().setItemOnCursor(null);
                                } else Utils.sendMessage(e.getWhoClicked(), "&cNot compatible with this item");
                            } else if (e.getRawSlot() == baseIndex){
                                if (selectedChoice.isCompatible(base.getItem()) && selectedChoice.isCompatibleWithInputItem(false)){
                                    base.setOption(selectedChoice);
                                    e.getWhoClicked().setItemOnCursor(null);
                                } else Utils.sendMessage(e.getWhoClicked(), "&cNot compatible with this item");
                            } else if (e.getRawSlot() == additionIndex){
                                if (selectedChoice.isCompatible(addition.getItem()) && selectedChoice.isCompatibleWithInputItem(true)){
                                    addition.setOption(selectedChoice);
                                    e.getWhoClicked().setItemOnCursor(null);
                                } else Utils.sendMessage(e.getWhoClicked(), "&cNot compatible with this item");
                            }
                        }
                    }
                } else {
                    if (e.getRawSlot() == templateIndex){
                        template = null;
                    } else if (e.getRawSlot() == baseIndex){
                        if (base != null){
                            base.setOption(null);
                        }
                    } else if (e.getRawSlot() == additionIndex){
                        if (addition != null){
                            addition.setOption(null);
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
        inventory.setItem(templateIndex, null);
        inventory.setItem(baseIndex, null);
        inventory.setItem(additionIndex, null);
        if (!tinker) inventory.setItem(resultIndex, null);
        else inventory.setItem(resultIndex, new ItemBuilder(base.getItem()).name("&dTinkered Base").get());

        List<String> templateLore = SlotEntry.getOptionLore(template);
        List<String> baseLore = SlotEntry.getOptionLore(base);
        List<String> additionLore = SlotEntry.getOptionLore(addition);

        ItemStack templateIcon = template == null ?
                new ItemBuilder(Material.BARRIER).name("&fNo template required!").lore("&7Click with an item to require", "&7it as template.").get() :
                new ItemBuilder(template.getItem().clone()).appendLore(templateLore).get();
        ItemStack baseIcon = new ItemBuilder(base.getItem().clone()).appendLore(baseLore).get();
        ItemStack additionIcon = new ItemBuilder(addition.getItem().clone()).appendLore(additionLore).get();
        inventory.setItem(templateIndex, templateIcon);
        inventory.setItem(baseIndex, baseIcon);
        inventory.setItem(additionIndex, additionIcon);
        if (!tinker) inventory.setItem(resultIndex, result.clone());


        List<String> validationLore = new ArrayList<>();
        if (!validations.isEmpty()){
            for (String v : validations){
                Validation validation = ValidationRegistry.getValidation(v);
                validationLore.add(validation.activeDescription());
            }
        } else validationLore.add("&aNo special conditions required");

        List<String> additionModifierLore = new ArrayList<>();
        additionModifiers.forEach(m -> additionModifierLore.addAll(StringUtils.separateStringIntoLines("&d> " + m.getActiveDescription(), 40)));
        List<String> resultModifierLore = new ArrayList<>();
        resultModifiers.forEach(m -> resultModifierLore.addAll(StringUtils.separateStringIntoLines("&d> " + m.getActiveDescription(), 40)));

        ItemMeta resultMeta = ItemUtils.getItemMeta(result);

        String tinkerFormat = TranslationManager.getTranslation("tinker_result_format");
        List<String> description = Arrays.asList(this.description == null ? "&eDefault".split("/n") :
                this.description
                        .replace("%template%", SlotEntry.toString(template))
                        .replace("%base%", SlotEntry.toString(base))
                        .replace("%tinker%", tinker ? SlotEntry.toString(base) : ItemUtils.getItemName(resultMeta))
                        .replace("%result%", tinker ? tinkerFormat.replace("%item%", SlotEntry.toString(base)) : ItemUtils.getItemName(resultMeta))
                        .replace("%addition%", SlotEntry.toString(addition))
                        .split("/n")
        );

        inventory.setItem(0, new ItemBuilder(setDisplayNameButton).lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(setDisplayNameButton), "%display_name%", List.of(displayName == null ? "&eDefault" : displayName))).get());
        inventory.setItem(1, new ItemBuilder(setDescriptionButton).lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(setDescriptionButton), "%description%", description)).get());
        inventory.setItem(5, new ItemBuilder(toggleUnlockedForEveryoneButton).name("&eUnlocked for Everyone " + (unlockedForEveryone ? "&aYes" : "&fNo")).get());
        inventory.setItem(10, new ItemBuilder(selectValidationButton).lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(selectValidationButton), "%description%", validationLore)).get());
        inventory.setItem(12, recipeOptionsButton);
        inventory.setItem(26, new ItemBuilder(toggleValhallaToolRequirementButton).name("&eValhalla Tools: " + (requireValhallaTools ? "&aYes" : "&fNo")).get());
        inventory.setItem(30, new ItemBuilder(toggleTinkerButton).name("&eTinker: " + (tinker ? "&aYes" : "&fNo")).get());
        inventory.setItem(13, new ItemBuilder(toggleConsumeAdditionButton).name("&eConsume Addition: " + (consumeAddition ? "&aYes" : "&fNo")).get());
        inventory.setItem(33, new ItemBuilder(resultModifierButton).lore(resultModifierLore).get());
        if (!consumeAddition) inventory.setItem(31, new ItemBuilder(additionModifierButton).lore(additionModifierLore).get());
        inventory.setItem(45, confirmDeletion ? deleteConfirmButton : deleteButton);
        inventory.setItem(49, backToMenuButton);
        inventory.setItem(53, confirmButton);
    }

    @Override
    public void setResultModifiers(List<DynamicItemModifier> resultModifiers) {
        if (lastAction.equals("resultModifierButton")) this.resultModifiers = resultModifiers;
        else if (lastAction.equals("additionModifierButton")) this.additionModifiers = resultModifiers;
    }

    @Override
    public List<DynamicItemModifier> getResultModifiers() {
        if (lastAction.equals("resultModifierButton")) return resultModifiers;
        else if (lastAction.equals("additionModifierButton")) return additionModifiers;
        return new ArrayList<>();
    }

    @Override
    public void setRecipeOption(RecipeOption option) {
        this.selectedChoice = option;
        if (option == null) return;
        Scheduling.runTaskLater(ValhallaMMO.getInstance(), 1L, () ->
                playerMenuUtility.getOwner().setItemOnCursor(new ItemBuilder(option.getIcon()).stringTag(KEY_OPTION_ID, option.getName()).get())
        );
    }

    @Override
    public void setValidations(Collection<String> validations) {
        this.validations = validations;
    }

    @Override
    public Collection<String> getValidations() {
        return validations;
    }
}
