package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.MetaRequirement;
import me.athlaeos.valhallammo.crafting.ToolRequirement;
import me.athlaeos.valhallammo.crafting.ToolRequirementType;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.ImmersiveCraftingRecipe;
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

public class ImmersiveRecipeEditor extends Menu implements SetModifiersMenu, SetRecipeOptionMenu, SetIngredientsMenu, SetValidationsMenu {
    private static final NamespacedKey BUTTON_ACTION_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "button_action");
    private static final int resultIndex = 24;
    private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").get();

    private final ImmersiveCraftingRecipe recipe;

    private Map<ItemStack, Integer> ingredients;
    private SlotEntry tinkerInput;
    private ItemStack result;
    private Material block;
    private int timeToCraft;
    private boolean destroyStation;
    private MetaRequirement metaRequirement;
    private boolean tinker;
    private boolean requireValhallaTools;
    private List<DynamicItemModifier> modifiers;
    private boolean unlockedForEveryone;
    private final ToolRequirement toolRequirement;
    private Collection<String> validations;
    private int consecutiveCrafts;
    private String displayName;
    private String description;

    private static final ItemStack toggleValhallaToolRequirementButton = new ItemBuilder(getButtonData("editor_recipe_immersive_valhallatoolrequirement", Material.DIAMOND_PICKAXE))
            .name("&eValhalla Tools")
            .stringTag(BUTTON_ACTION_KEY, "toggleValhallaToolRequirementButton")
            .lore("&7If enabled, armors and tools used",
                    "&7in tinkering recipes will need to possess ",
                    "&7custom attributes. If disabled, vanilla ",
                    "&7equipment can be used too.",
                    "&eClick to toggle on/off")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack toggleTinkerButton = new ItemBuilder(getButtonData("editor_recipe_immersive_toggletinker", Material.ANVIL))
            .name("&eTinker Held item")
            .stringTag(BUTTON_ACTION_KEY, "toggleTinkerButton")
            .lore("&7If enabled, the recipe will",
                    "&7modify the held item.",
                    "&7Otherwise, it will simply produce the",
                    "&7item specified as the result.",
                    "&eClick to toggle on/off")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack setCraftingBlockButton = new ItemBuilder(getButtonData("editor_recipe_immersive_setcraftingblock", Material.CRAFTING_TABLE))
            .name("&eCrafting Station")
            .stringTag(BUTTON_ACTION_KEY, "setCraftingBlockButton")
            .lore("&7The recipe may be crafted on",
                    "&7whatever the block type of this button",
                    "&7is.",
                    "&eClick with the block type of your",
                    "&echoosing to change the type.")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack craftTimeButton = new ItemBuilder(getButtonData("editor_recipe_immersive_crafttime", Material.CLOCK))
            .name("&eCrafting Time")
            .stringTag(BUTTON_ACTION_KEY, "craftTimeButton")
            .lore("&7How long it takes to craft this item.",
                    "&eClick to increase/decrease by 0.1 seconds",
                    "&eShift-click to increase/decrease by 1 second")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack repeatCraftsButton = new ItemBuilder(getButtonData("editor_recipe_immersive_repeatcrafts", Material.REPEATER))
            .name("&eRepeatable")
            .stringTag(BUTTON_ACTION_KEY, "repeatCraftsButton")
            .lore("&7How often the player can craft",
                    "&7this recipe before having to re-select",
                    "&7it. This is to prevent afk-crafting",
                    "&eClick to increase/decrease by 1",
                    "&eShift-click to increase/decrease by 16")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack toggleDestroyStationButton = new ItemBuilder(getButtonData("editor_recipe_immersive_toggledestroystation", Material.LAVA_BUCKET))
            .name("&fDestroy Block")
            .stringTag(BUTTON_ACTION_KEY, "toggleDestroyStationButton")
            .lore("&7If enabled, the block will",
                    "&7be destroyed upon recipe completion.",
                    "&7This is useful for \"harvesting\" type",
                    "&7recipes, such as gathering plant string",
                    "&7from grass.",
                    "&7The block will be destroyed without",
                    "&7dropping items.",
                    "&eClick to toggle on/off")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack metaRequirementButton = new ItemBuilder(getButtonData("editor_recipe_immersive_toggleexactmeta", Material.PAPER))
            .name("&dMeta Requirement")
            .stringTag(BUTTON_ACTION_KEY, "metaRequirementButton")
            .lore("&7Determines the manner in which ",
                    "&7ingredients will need to match player",
                    "&7items.",
                    "&cFor material-only, if the player's ",
                    "&citems have a custom name OR lore, ",
                    "&cthey will be considered custom items ",
                    "&cand will not be used in crafting.",
                    "&eClick to select next option",
                    "&8&m                <>                ")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack selectValidationButton = new ItemBuilder(getButtonData("editor_recipe_immersive_selectvalidation", Material.BARRIER))
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
    private static final ItemStack ingredientsButton = new ItemBuilder(getButtonData("editor_recipe_immersive_ingredients", Material.WRITABLE_BOOK))
            .name("&7Ingredients")
            .stringTag(BUTTON_ACTION_KEY, "ingredientsButton")
            .lore("&7The player must have all the listed",
                    "&7ingredients in their inventory to",
                    "&7craft this recipe.",
                    "&eClick to open the menu and insert your",
                    "&eingredients there.",
                    "&8&m                <>                ",
                    "%ingredients%").get();
    private static final ItemStack modifierButton = new ItemBuilder(getButtonData("editor_recipe_immersive_modifiers", Material.WRITABLE_BOOK))
            .name("&dDynamic Item Modifiers")
            .stringTag(BUTTON_ACTION_KEY, "modifierButton")
            .lore("&7Modifiers are functions to edit",
                    "&7the output item based on player",
                    "&7stats or to apply crafting conditions.",
                    "&eClick to open the menu",
                    "&8&m                <>                ",
                    "%modifiers%").get();
    private static final ItemStack recipeOptionsButton = new ItemBuilder(getButtonData("editor_recipe_immersive_recipeoptions", Material.WRITABLE_BOOK))
            .name("&bIngredient Options")
            .stringTag(BUTTON_ACTION_KEY, "recipeOptionsButton")
            .lore("&7Ingredient options are ingredient",
                    "&7flags you can put on an ingredient",
                    "&7to change its behavior during crafting.",
                    "&eClick to open the menu").get();
    private static final ItemStack toggleUnlockedForEveryoneButton = new ItemBuilder(getButtonData("editor_recipe_immersive_unlockedforeveryone", Material.PAPER))
            .name("&eUnlocked for Everyone")
            .stringTag(BUTTON_ACTION_KEY, "toggleUnlockedForEveryoneButton")
            .lore("&7If enabled, the recipe will",
                    "&7available to everyone regardless",
                    "&7if unlocked or not. Otherwise it",
                    "&7will have to be unlocked through",
                    "&7perk rewards.",
                    "&eClick to toggle on/off").get();
    private static final ItemStack setToolRequirementButton = new ItemBuilder(getButtonData("editor_recipe_immersive_settoolrequirement", Material.CRAFTING_TABLE))
            .name("&eTool Required")
            .stringTag(BUTTON_ACTION_KEY, "setToolRequirementButton")
            .lore("%description%",
                    "&8&m                <>                ",
                    "&7Determines whether this recipe requires",
                    "&7a tool to craft. The tool must be held",
                    "&7during crafting.",
                    "&7For tinkering recipes the tool must exist",
                    "&7in the player's inventory instead.",
                    "&7A tool is determined by its \"Tool ID\"",
                    "&7which can be applied with a modifier.",
                    "&eRight/Left click to change the",
                    "&eID required, hold Shift during clicks",
                    "&eto change the requirement type").get();
    private static final ItemStack setDisplayNameButton = new ItemBuilder(getButtonData("editor_recipe_immersive_setdisplayname", Material.NAME_TAG))
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
    private static final ItemStack setDescriptionButton = new ItemBuilder(getButtonData("editor_recipe_immersive_setdescription", Material.WRITABLE_BOOK))
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

    public ImmersiveRecipeEditor(PlayerMenuUtility playerMenuUtility, ImmersiveCraftingRecipe recipe) {
        super(playerMenuUtility);
        this.recipe = recipe;

        this.ingredients = recipe.getIngredients();
        this.metaRequirement = recipe.getMetaRequirement();
        this.block = recipe.getBlock();
        this.timeToCraft = recipe.getTimeToCraft();
        this.destroyStation = recipe.destroysStation();
        this.modifiers = recipe.getModifiers();
        this.tinkerInput = recipe.getTinkerInput();
        this.result = recipe.getResult();
        this.tinker = recipe.tinker();
        this.requireValhallaTools = recipe.requiresValhallaTools();
        this.validations = recipe.getValidations();
        this.toolRequirement = recipe.getToolRequirement();
        this.consecutiveCrafts = recipe.getConsecutiveCrafts();
        this.unlockedForEveryone = recipe.isUnlockedForEveryone();
        this.displayName = recipe.getDisplayName();
        this.description = recipe.getDescription();
    }

    public ImmersiveRecipeEditor(PlayerMenuUtility playerMenuUtility, ImmersiveCraftingRecipe recipe, String newName) {
        super(playerMenuUtility);
        this.recipe = new ImmersiveCraftingRecipe(newName);

        this.ingredients = recipe.getIngredients();
        this.metaRequirement = recipe.getMetaRequirement();
        this.block = recipe.getBlock();
        this.timeToCraft = recipe.getTimeToCraft();
        this.destroyStation = recipe.destroysStation();
        this.modifiers = recipe.getModifiers();
        this.tinkerInput = recipe.getTinkerInput();
        this.result = recipe.getResult();
        this.tinker = recipe.tinker();
        this.requireValhallaTools = recipe.requiresValhallaTools();
        this.validations = recipe.getValidations();
        this.toolRequirement = recipe.getToolRequirement();
        this.consecutiveCrafts = recipe.getConsecutiveCrafts();
        this.unlockedForEveryone = recipe.isUnlockedForEveryone();
        this.displayName = recipe.getDisplayName();
        this.description = recipe.getDescription();
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF205\uF80C\uF80A\uF808\uF802&8%recipe%" : TranslationManager.getTranslation("editormenu_gridrecipes")).replace("%recipe%", recipe.getName());
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
                    new RecipeOverviewMenu(playerMenuUtility, RecipeOverviewMenu.IMMERSIVE.getId()).open();
                    return;
                }
                case "modifierButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new DynamicModifierMenu(playerMenuUtility, this).open();
                    return;
                }
                case "ingredientsButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new IngredientSelectionMenu(playerMenuUtility, this).open();
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
                        new RecipeOverviewMenu(playerMenuUtility, RecipeOverviewMenu.IMMERSIVE.getId()).open();
                        return;
                    }
                }
                case "setCraftingBlockButton" -> {
                    if (!ItemUtils.isEmpty(cursor) && cursor.getType().isBlock()){
                        block = ItemUtils.getBaseMaterial(cursor.getType());
                        // remove incompatible validations
                        validations.removeIf(v -> {
                            Validation validation = ValidationRegistry.getValidation(v);
                            if (validation != null) return !validation.isCompatible(block);
                            return false;
                        });
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
                        Utils.sendMessage(e.getWhoClicked(), "          &2%ingredients% &afor the ingredients");
                        Utils.sendMessage(e.getWhoClicked(), "          &2%held% &afor the held tinkering item");
                        Utils.sendMessage(e.getWhoClicked(), "          &2%result% &afor a preformatted result");
                        Utils.sendMessage(e.getWhoClicked(), "          &2%tinker% &afor the raw name of the tinkered result");
                    } else description = null;
                }
                case "confirmButton" -> {
                    if (ItemUtils.isEmpty(result) || tinkerInput == null || ItemUtils.isEmpty(tinkerInput.getItem())) {
                        Utils.sendMessage(e.getWhoClicked(), "&cPlease add a result!");
                        setMenuItems();
                        return;
                    }
                    recipe.setIngredients(ingredients);
                    recipe.setRequireExactMeta(metaRequirement);
                    recipe.setBlock(block);
                    recipe.setTimeToCraft(timeToCraft);
                    recipe.setDestroyStation(destroyStation);
                    recipe.setModifiers(modifiers);
                    recipe.setTinkerInput(tinkerInput);
                    recipe.setResult(result);
                    recipe.setTinker(tinker);
                    recipe.setRequiresValhallaTools(requireValhallaTools);
                    recipe.setValidations(validations);
                    recipe.setToolRequirement(toolRequirement);
                    recipe.setConsecutiveCrafts(consecutiveCrafts);
                    recipe.setUnlockedForEveryone(unlockedForEveryone);
                    recipe.setDescription(description);
                    recipe.setDisplayName(displayName);

                    CustomRecipeRegistry.register(recipe, true);
                    CustomRecipeRegistry.setChangesMade();
                    new RecipeOverviewMenu(playerMenuUtility, RecipeOverviewMenu.IMMERSIVE.getId()).open();
                    return;
                }
                case "selectValidationButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new ValidationSelectionMenu(playerMenuUtility, this, block).open();
                    return;
                }
                case "setToolRequirementButton" -> {
                    if (e.getClick().isShiftClick()){
                        int toolRequirement = Arrays.asList(ToolRequirementType.values()).indexOf(this.toolRequirement.getToolRequirementType());
                        if (e.getClick().isLeftClick()){
                            if (toolRequirement + 1 >= ToolRequirementType.values().length) toolRequirement = 0;
                            else toolRequirement += 1;
                        } else {
                            if (toolRequirement - 1 < 0) toolRequirement = ToolRequirementType.values().length - 1;
                            else toolRequirement -= 1;
                        }
                        this.toolRequirement.setToolRequirementType(ToolRequirementType.values()[toolRequirement]);
                    } else {
                        this.toolRequirement.setRequiredToolID(e.getClick().isLeftClick() ?
                                this.toolRequirement.getRequiredToolID() + 1 :
                                Math.max(-1, this.toolRequirement.getRequiredToolID() - 1));
                    }
                }
                case "toggleValhallaToolRequirementButton" -> requireValhallaTools = !requireValhallaTools;
                case "toggleTinkerButton" -> tinker = !tinker;
                case "craftTimeButton" -> timeToCraft = Math.max(0, timeToCraft + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 20 : 2)));
                case "repeatCraftsButton" -> consecutiveCrafts = Math.max(-1, consecutiveCrafts + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 16 : 1)));
                case "toggleDestroyStationButton" -> destroyStation = !destroyStation;
                case "metaRequirementButton" -> {
                    int currentRequirement = Arrays.asList(MetaRequirement.values()).indexOf(metaRequirement);
                    if (e.isLeftClick()) {
                        if (currentRequirement + 1 >= MetaRequirement.values().length) currentRequirement = 0;
                        else currentRequirement++;
                    } else {
                        if (currentRequirement - 1 < 0) currentRequirement = MetaRequirement.values().length - 1;
                        else currentRequirement--;
                    }
                    metaRequirement = MetaRequirement.values()[currentRequirement];
                }
                case "toggleUnlockedForEveryoneButton" -> unlockedForEveryone = !unlockedForEveryone;
            }
        }

        if (resultIndex == e.getRawSlot()){
            if (!tinker && !ItemUtils.isEmpty(cursor)) {
                result = cursor.clone();
            } else {
                if (!ItemUtils.isEmpty(cursor)){
                    tinkerInput = new SlotEntry(new ItemBuilder(cursor.clone()).amount(1).get(), new MaterialChoice());
                } else {
                    tinkerInput = new SlotEntry(new ItemBuilder(tinkerInput.getItem()).amount(1).get(), new MaterialChoice());
                }
            }
        }

        confirmDeletion = false;
        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        if (e.getRawSlots().size() == 1){
            e.setCancelled(true);
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
        inventory.setItem(resultIndex, null);

        if (!tinker) inventory.setItem(resultIndex, result.clone());
        else {
            inventory.setItem(resultIndex, new ItemBuilder(tinkerInput.getItem().getType()).name("&f" + SlotEntry.toString(tinkerInput))
                    .lore(SlotEntry.getOptionLore(tinkerInput)).get());
        }

        List<String> modifierLore = new ArrayList<>();
        modifiers.forEach(m -> modifierLore.addAll(StringUtils.separateStringIntoLines("&d> " + m.getActiveDescription(), 40)));

        boolean toolRequired = toolRequirement.getRequiredToolID() >= 0 &&
                (toolRequirement.getToolRequirementType() != ToolRequirementType.NOT_REQUIRED &&
                        toolRequirement.getToolRequirementType() != ToolRequirementType.NONE_MANDATORY);
        String toolSettings = "&7ID: &6" + toolRequirement.getRequiredToolID() + "&7, type: &6" + StringUtils.toPascalCase(toolRequirement.getToolRequirementType().toString().replace("_", " "));
        List<String> toolDescription = toolRequired ? switch (this.toolRequirement.getToolRequirementType()){
            case EQUAL -> List.of(toolSettings, Utils.chat("&eTool with ID " + toolRequirement.getRequiredToolID() + " required"));
            case NOT_REQUIRED -> List.of(toolSettings, Utils.chat("&eNo tool required"));
            case NONE_MANDATORY -> List.of(toolSettings, Utils.chat("&eNo tool is allowed to be used"));
            case EQUAL_OR_LESSER -> List.of(toolSettings, Utils.chat("&eTool with ID of or lesser than " + toolRequirement.getRequiredToolID() + " required"));
            case EQUAL_OR_GREATER -> List.of(toolSettings, Utils.chat("&eTool with ID of or greater than " + toolRequirement.getRequiredToolID() + " required"));
        } : List.of(toolSettings, Utils.chat("&eNo tool required"));
        ItemStack toolRequirementButton = setToolRequirementButton.clone();
        toolRequirementButton = new ItemBuilder(toolRequirementButton)
                .lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(toolRequirementButton), "%description%", toolDescription))
                .get();

        List<String> validationLore = new ArrayList<>();
        if (!validations.isEmpty()){
            for (String v : validations){
                Validation validation = ValidationRegistry.getValidation(v);
                validationLore.add(validation.activeDescription());
            }
        } else validationLore.add("&aNo special conditions required");

        List<String> lore = new ArrayList<>();
        if (!ingredients.isEmpty()){
            String format = TranslationManager.getTranslation("ingredient_format_shapeless");
            for (ItemStack entry : ingredients.keySet()){
                int amount = ingredients.get(entry);
                lore.add(format.replace("%amount%", String.valueOf(amount)).replace("%ingredient%", metaRequirement.getChoice().ingredientDescription(entry)));
            }
        } else lore.add("&eRecipe is crafted for free");

        ItemMeta resultMeta = ItemUtils.getItemMeta(result);

        String tinkerFormat = TranslationManager.getTranslation("tinker_result_format");
        List<String> description = ItemUtils.setListPlaceholder(Arrays.asList(this.description == null ? "&eDefault".split("/n") :
                this.description
                        .replace("%held%", SlotEntry.toString(tinkerInput))
                        .replace("%tinker%", tinker ? SlotEntry.toString(tinkerInput) : ItemUtils.getItemName(resultMeta))
                        .replace("%result%", tinker ? tinkerFormat.replace("%item%", SlotEntry.toString(tinkerInput)) : ItemUtils.getItemName(resultMeta))
                        .split("/n")), "%ingredients%", lore);

        inventory.setItem(0, new ItemBuilder(setDisplayNameButton).lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(setDisplayNameButton), "%display_name%", List.of(displayName == null ? "&eDefault" : displayName))).get());
        inventory.setItem(1, new ItemBuilder(setDescriptionButton).lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(setDescriptionButton), "%description%", description)).get());
        inventory.setItem(5, new ItemBuilder(toggleUnlockedForEveryoneButton).name("&eUnlocked for Everyone " + (unlockedForEveryone ? "&aYes" : "&fNo")).get());
        inventory.setItem(17, new ItemBuilder(toggleValhallaToolRequirementButton).name("&eValhalla Tools: " + (requireValhallaTools ? "&aYes" : "&fNo")).get());
        if (tinker) inventory.setItem(15, recipeOptionsButton);
        inventory.setItem(19, new ItemBuilder(metaRequirementButton).name("&fMeta Requirement: &e" + metaRequirement).appendLore(metaRequirement.getDescription()).get());
        inventory.setItem(20, new ItemBuilder(ingredientsButton).lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(ingredientsButton), "%ingredients%", lore)).get());
        inventory.setItem(21, new ItemBuilder(toolRequirementButton).name("&eTool Required: " + (toolRequired ? "&aYes" : "&fNo")).get());
        inventory.setItem(22, new ItemBuilder(setCraftingBlockButton).data(-1).type(block).get());
        inventory.setItem(25, new ItemBuilder(modifierButton).lore(modifierLore).get());
        inventory.setItem(31, new ItemBuilder(craftTimeButton).name(String.format("&fTime to craft: &e%d, %ss", timeToCraft, StringUtils.toTimeStamp2(timeToCraft, 20))).get());
        inventory.setItem(35, new ItemBuilder(toggleTinkerButton).name("&eTinker: " + (tinker ? "&aYes" : "&fNo")).get());
        inventory.setItem(39, new ItemBuilder(selectValidationButton).lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(selectValidationButton), "%description%", validationLore)).get());
        inventory.setItem(40, new ItemBuilder(repeatCraftsButton).name(String.format("&fRepeatable &e%s &ftimes", consecutiveCrafts < 0 ? "∞" : String.valueOf(consecutiveCrafts))).get());
        inventory.setItem(41, new ItemBuilder(toggleDestroyStationButton).name("&cDestroy After Crafting: " + (destroyStation ? "&aYes" : "&fNo")).get());
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
        if (!option.isCompatible(tinkerInput.getItem()) || !option.isCompatibleWithInputItem(true)) {
            Utils.sendMessage(playerMenuUtility.getOwner(), "&cNot compatible with this item");
        } else {
            if (option instanceof IngredientChoice c) tinkerInput.setOption(c);
        }
    }

    @Override
    public void setIngredients(Map<ItemStack, Integer> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public Map<ItemStack, Integer> getIngredients() {
        return ingredients;
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
