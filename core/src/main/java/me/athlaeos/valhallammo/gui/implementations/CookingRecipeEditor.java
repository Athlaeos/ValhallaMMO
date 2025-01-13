package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCookingRecipe;
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

public class CookingRecipeEditor extends Menu implements SetModifiersMenu, SetRecipeOptionMenu, SetValidationsMenu {
    private static final NamespacedKey BUTTON_ACTION_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "button_action");
    private static final int inputIndex = 21;
    private static final int resultIndex = 23;
    private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").get();

    private final DynamicCookingRecipe recipe;

    private final DynamicCookingRecipe.CookingRecipeType type;
    private SlotEntry input;
    private ItemStack result;
    private boolean requireValhallaTools;
    private boolean tinker;
    private List<DynamicItemModifier> modifiers;
    private boolean unlockedForEveryone;
    private boolean hidden;
    private int cookTime;
    private float experience;
    private Collection<String> validations;
    private String displayName;
    private String description;

    private static final ItemStack toggleValhallaToolRequirementButton = new ItemBuilder(getButtonData("editor_recipe_cooking_valhallatoolrequirement", Material.DIAMOND_PICKAXE))
            .name("&eValhalla Tools")
            .stringTag(BUTTON_ACTION_KEY, "toggleValhallaToolRequirementButton")
            .lore("&7If enabled, the armors or tools",
                    "&7in the recipe need to have ValhallaMMO ",
                    "&7custom attributes. If disabled, vanilla ",
                    "&7equipment can be used too.",
                    "&eClick to toggle on/off")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack toggleTinkerButton = new ItemBuilder(getButtonData("editor_recipe_cooking_toggletinker", Material.ANVIL))
            .name("&eTinker Tools")
            .stringTag(BUTTON_ACTION_KEY, "toggleTinkerButton")
            .lore("&7If enabled, the recipe will",
                    "&7improve the input item of the recipe.",
                    "&7Otherwise, it will simply produce the",
                    "&7item specified as the result.",
                    "&eClick to toggle on/off")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack toggleHiddenButton = new ItemBuilder(getButtonData("editor_recipe_cooking_togglehidden", Material.ANVIL))
            .name("&eHidden from Recipe Book")
            .stringTag(BUTTON_ACTION_KEY, "toggleHiddenButton")
            .lore("&7If enabled, the recipe will",
                    "&7not be visible in the recipe book.",
                    "&eClick to toggle on/off")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack selectValidationButton = new ItemBuilder(getButtonData("editor_recipe_cooking_selectvalidation", Material.BARRIER))
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
    private static final ItemStack cookTimeButton = new ItemBuilder(getButtonData("editor_recipe_cooking_cooktime", Material.CLOCK))
            .name("&eCooking Time")
            .stringTag(BUTTON_ACTION_KEY, "cookTimeButton")
            .lore("&7How long it takes to cook this item.",
                    "&7Typical campfire time: &a600 ticks",
                    "&7Typical furnace time: &a200 ticks",
                    "&7Typical blasting/smoking time: &a100 ticks",
                    "&eClick to increase/decrease by 1 tick",
                    "&eShift-click to increase/decrease by 20 ticks")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack experienceButton = new ItemBuilder(getButtonData("editor_recipe_cooking_experience", Material.EXPERIENCE_BOTTLE))
            .name("&aExperience")
            .stringTag(BUTTON_ACTION_KEY, "experienceButton")
            .lore("&7How much EXP is rewarded on cooking.",
                    "&7Typically cooking recipes provide",
                    "&a1 experience &7per cook.",
                    "&eClick to increase/decrease by 0.1",
                    "&eShift-click to increase/decrease by 1")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack modifierButton = new ItemBuilder(getButtonData("editor_recipe_cooking_modifiers", Material.WRITABLE_BOOK))
            .name("&dDynamic Item Modifiers")
            .stringTag(BUTTON_ACTION_KEY, "modifierButton")
            .lore("&7Modifiers are functions to edit",
                    "&7the output item based on player",
                    "&7stats or to apply crafting conditions.",
                    "&eClick to open the menu",
                    "&8&m                <>                ",
                    "%modifiers%").get();
    private static final ItemStack recipeOptionsButton = new ItemBuilder(getButtonData("editor_recipe_cooking_recipeoptions", Material.WRITABLE_BOOK))
            .name("&bIngredient Options")
            .stringTag(BUTTON_ACTION_KEY, "recipeOptionsButton")
            .lore("&7Ingredient options are ingredient",
                    "&7flags you can put on an ingredient",
                    "&7to change its behavior during crafting.",
                    "&eClick to open the menu").get();
    private static final ItemStack toggleUnlockedForEveryoneButton = new ItemBuilder(getButtonData("editor_recipe_cooking_unlockedforeveryone", Material.PAPER))
            .name("&eUnlocked for Everyone")
            .stringTag(BUTTON_ACTION_KEY, "toggleUnlockedForEveryoneButton")
            .lore("&7If enabled, the recipe will",
                    "&7available to everyone regardless",
                    "&7if unlocked or not. Otherwise it",
                    "&7will have to be unlocked through",
                    "&7perk rewards.",
                    "&eClick to toggle on/off").get();
    private static final ItemStack setDisplayNameButton = new ItemBuilder(getButtonData("editor_recipe_cooking_setdisplayname", Material.NAME_TAG))
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
    private static final ItemStack setDescriptionButton = new ItemBuilder(getButtonData("editor_recipe_cooking_setdescription", Material.WRITABLE_BOOK))
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

    public CookingRecipeEditor(PlayerMenuUtility playerMenuUtility, DynamicCookingRecipe recipe) {
        super(playerMenuUtility);
        this.type = recipe.getType();
        this.recipe = recipe;

        this.input = recipe.getInput();
        this.result = recipe.getResult();
        this.requireValhallaTools = recipe.requireValhallaTools();
        this.tinker = recipe.tinker();
        this.cookTime = recipe.getCookTime();
        this.experience = recipe.getExperience();
        this.modifiers = recipe.getModifiers();
        this.unlockedForEveryone = recipe.isUnlockedForEveryone();
        this.validations = recipe.getValidations();
        this.displayName = recipe.getDisplayName();
        this.description = recipe.getDescription();
        this.hidden = recipe.isHiddenFromBook();
    }

    public CookingRecipeEditor(PlayerMenuUtility playerMenuUtility, DynamicCookingRecipe recipe, String newName) {
        super(playerMenuUtility);
        this.type = recipe.getType();
        this.recipe = new DynamicCookingRecipe(newName, recipe.getType());

        this.input = new SlotEntry(recipe.getInput().getItem().clone(), recipe.getInput().getOption());
        this.result = recipe.getResult().clone();
        this.requireValhallaTools = recipe.requireValhallaTools();
        this.tinker = recipe.tinker();
        this.cookTime = recipe.getCookTime();
        this.experience = recipe.getExperience();
        this.modifiers = new ArrayList<>(recipe.getModifiers().stream().map(DynamicItemModifier::copy).toList());
        this.unlockedForEveryone = recipe.isUnlockedForEveryone();
        this.validations = new HashSet<>(recipe.getValidations());
        this.displayName = recipe.getDisplayName();
        this.description = recipe.getDescription();
        this.hidden = recipe.isHiddenFromBook();
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF202\uF80C\uF80A\uF808\uF802&8%recipe%" : TranslationManager.getTranslation("editormenu_cookingrecipes")).replace("%recipe%", recipe.getName());
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
                    new RecipeOverviewMenu(playerMenuUtility, type.getCategory()).open();
                    return;
                }
                case "modifierButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new DynamicModifierMenu(playerMenuUtility, this).open();
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
                        new RecipeOverviewMenu(playerMenuUtility, type.getCategory()).open();
                        return;
                    }
                }
                case "selectValidationButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new ValidationSelectionMenu(playerMenuUtility, this, switch(type) {
                        case CAMPFIRE -> Material.CAMPFIRE;
                        case BLAST_FURNACE -> Material.BLAST_FURNACE;
                        case SMOKER -> Material.SMOKER;
                        case FURNACE -> Material.FURNACE;
                    }).open();
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
                        Utils.sendMessage(e.getWhoClicked(), "          &2%input% &afor the input item");
                        Utils.sendMessage(e.getWhoClicked(), "          &2%result% &afor a preformatted result");
                        Utils.sendMessage(e.getWhoClicked(), "          &2%tinker% &afor the raw name of the tinkered result");
                    } else description = null;
                }
                case "confirmButton" -> {
                    if (input == null || ItemUtils.isEmpty(input.getItem()) || ItemUtils.isEmpty(result)) {
                        Utils.sendMessage(e.getWhoClicked(), "&cAn input and output are required!");
                        setMenuItems();
                        return;
                    }
                    recipe.setInput(input);
                    recipe.setResult(result);
                    recipe.setModifiers(modifiers);
                    recipe.setTinker(tinker);
                    recipe.setRequireValhallaTools(requireValhallaTools);
                    recipe.setUnlockedForEveryone(unlockedForEveryone);
                    recipe.setCookTime(cookTime);
                    recipe.setExperience(experience);
                    recipe.setValidations(validations);
                    recipe.setDescription(description);
                    recipe.setDisplayName(displayName);
                    recipe.setHiddenFromBook(hidden);

                    CustomRecipeRegistry.register(recipe, true);
                    CustomRecipeRegistry.setChangesMade();
                    new RecipeOverviewMenu(playerMenuUtility, type.getCategory()).open();
                    return;
                }
                case "cookTimeButton" -> cookTime = Math.max(0, cookTime + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 20 : 1)));
                case "toggleHiddenButton" -> hidden = !hidden;
                case "experienceButton" -> experience = Math.max(0, experience + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1F : 0.1F)));
                case "toggleValhallaToolRequirementButton" -> requireValhallaTools = !requireValhallaTools;
                case "toggleTinkerButton" -> tinker = !tinker;
                case "toggleUnlockedForEveryoneButton" -> unlockedForEveryone = !unlockedForEveryone;
            }
        }

        if (resultIndex == e.getRawSlot() || inputIndex == e.getRawSlot()){
            if (e.getRawSlot() == resultIndex && !tinker && !ItemUtils.isEmpty(cursor)) {
                result = cursor.clone();
            } else if (inputIndex == e.getRawSlot()){
                // clicked input slot
                if (!ItemUtils.isEmpty(cursor)){
                    input = new SlotEntry(new ItemBuilder(cursor.clone()).amount(1).get(), new MaterialChoice());
                } else {
                    if (input != null){
                        input.setOption(null);
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

    @SuppressWarnings("all")
    @Override
    public void setMenuItems() {
        inventory.clear();
        if (!ValhallaMMO.getPluginConfig().getBoolean("admin_gui_filler_removal")) for (int i = 0; i < 54; i++) inventory.setItem(i, filler);
        inventory.setItem(inputIndex, null);

        ItemStack icon = new ItemBuilder(input.getItem().clone()).appendLore(SlotEntry.getOptionLore(input)).get();
        inventory.setItem(inputIndex, icon);
        if (!tinker) inventory.setItem(resultIndex, result.clone());
        else inventory.setItem(resultIndex, new ItemBuilder(input.getItem()).name("&dTinkered Input").get());

        List<String> validationLore = new ArrayList<>();
        if (!validations.isEmpty()){
            for (String v : validations){
                Validation validation = ValidationRegistry.getValidation(v);
                validationLore.add(validation.activeDescription());
            }
        } else validationLore.add("&aNo special conditions required");

        List<String> modifierLore = new ArrayList<>();
        modifiers.forEach(m -> modifierLore.addAll(StringUtils.separateStringIntoLines("&d> " + m.getActiveDescription(), 40)));

        ItemMeta resultMeta = ItemUtils.getItemMeta(result);

        String tinkerFormat = TranslationManager.getTranslation("tinker_result_format");
        List<String> description = Arrays.asList(this.description == null ? "&eDefault".split("/n") :
                this.description
                        .replace("%input%", SlotEntry.toString(input))
                        .replace("%tinker%", tinker ? SlotEntry.toString(input) : ItemUtils.getItemName(resultMeta))
                        .replace("%result%", tinker ? tinkerFormat.replace("%item%", SlotEntry.toString(input)) : ItemUtils.getItemName(resultMeta))
                        .split("/n")
        );

        inventory.setItem(0, new ItemBuilder(setDisplayNameButton).lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(setDisplayNameButton), "%display_name%", List.of(displayName == null ? "&eDefault" : displayName))).get());
        inventory.setItem(1, new ItemBuilder(setDescriptionButton).lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(setDescriptionButton), "%description%", description)).get());
        inventory.setItem(5, new ItemBuilder(toggleUnlockedForEveryoneButton).name("&eUnlocked for Everyone " + (unlockedForEveryone ? "&aYes" : "&fNo")).get());
        inventory.setItem(8, new ItemBuilder(toggleHiddenButton).name("&eHidden from Recipe Book: " + (hidden ? "&aYes" : "&fNo")).get());
        inventory.setItem(10, new ItemBuilder(selectValidationButton).lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(selectValidationButton), "%description%", validationLore)).get());
        inventory.setItem(17, new ItemBuilder(toggleValhallaToolRequirementButton).name("&eValhalla Tools: " + (requireValhallaTools ? "&aYes" : "&fNo")).get());
        inventory.setItem(20, recipeOptionsButton);
        inventory.setItem(24, new ItemBuilder(modifierButton).lore(modifierLore).get());
        inventory.setItem(30, new ItemBuilder(cookTimeButton).name(String.format("&fTime to cook: &e%d, %ss", cookTime, StringUtils.toTimeStamp2(cookTime, 20))).get());
        inventory.setItem(32, new ItemBuilder(experienceButton).name(String.format("&fExperience: &e%.1f", experience)).get());
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
        return this.modifiers;
    }

    @Override
    public void setRecipeOption(RecipeOption option) {
        if (option == null) return;
        if (!option.isCompatible(input.getItem()) || !option.isCompatibleWithInputItem(true)) {
            Utils.sendMessage(playerMenuUtility.getOwner(), "&cNot compatible with this item");
        } else {
            if (option instanceof IngredientChoice c) input.setOption(c);
        }
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
