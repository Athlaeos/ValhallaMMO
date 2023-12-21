package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.ToolRequirement;
import me.athlaeos.valhallammo.crafting.ToolRequirementType;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicGridRecipe;
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

import static me.athlaeos.valhallammo.gui.implementations.RecipeOptionMenu.KEY_OPTION_ID;

public class GridRecipeEditor extends Menu implements SetModifiersMenu, SetRecipeOptionMenu, SetValidationsMenu {
    private static final NamespacedKey BUTTON_ACTION_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "button_action");
    private static final List<Integer> gridIndexes = Arrays.asList(11, 12, 13, 20, 21, 22, 29, 30, 31);
    private static final int resultIndex = 23;
    private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").get();

    private final DynamicGridRecipe recipe;

    private RecipeOption selectedOption = null;

    private final Map<Integer, SlotEntry> items;
    private ItemStack result;
    private boolean requireValhallaTools;
    private boolean tinker;
    private int tinkerGridIndex;
    private int toolIndex;
    private boolean shapeless;
    private boolean hidden;
    private List<DynamicItemModifier> modifiers;
    private boolean unlockedForEveryone;
    private final ToolRequirement toolRequirement;
    private Collection<String> validations;
    private String displayName;
    private String description;

    private static final ItemStack toggleValhallaToolRequirementButton = new ItemBuilder(getButtonData("editor_recipe_grid_valhallatoolrequirement", Material.DIAMOND_PICKAXE))
            .name("&eValhalla Tools")
            .stringTag(BUTTON_ACTION_KEY, "toggleValhallaToolRequirementButton")
            .lore("&7If enabled, any armors or tools",
                    "&7in the recipe need to have ValhallaMMO ",
                    "&7custom attributes. If disabled, vanilla ",
                    "&7equipment can be used too.",
                    "&eClick to toggle on/off")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack toggleTinkerButton = new ItemBuilder(getButtonData("editor_recipe_grid_toggletinker", Material.ANVIL))
            .name("&eTinker Tools")
            .stringTag(BUTTON_ACTION_KEY, "toggleTinkerButton")
            .lore("&7If enabled, the recipe will",
                    "&7improve the first tool or armor",
                    "&7found in the grid (or one specified",
                    "&7in the \"tinker this item\" option)",
                    "&7Otherwise, it will simply produce the",
                    "&7item specified as the result.",
                    "&eClick to toggle on/off")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack toggleHiddenButton = new ItemBuilder(getButtonData("editor_recipe_grid_togglehidden", Material.ANVIL))
            .name("&eHidden from Recipe Book")
            .stringTag(BUTTON_ACTION_KEY, "toggleHiddenButton")
            .lore("&7If enabled, the recipe will",
                    "&7not be visible in the recipe book.",
                    "&eClick to toggle on/off")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack selectTinkerItemButton = new ItemBuilder(getButtonData("editor_recipe_grid_selecttinkeritem", Material.ANVIL))
            .name("&eSelect which tool to tinker")
            .stringTag(BUTTON_ACTION_KEY, "selectTinkerItemButton")
            .lore("&eDrag-and-drop onto the",
                    "&eitem in the grid &7you want the",
                    "&7recipe to tinker.",
                    "&eDrag-and-drop it onto itself",
                    "&7to remove the previous selection.",
                    "&eIf none are selected, it will",
                    "&edefault to the first equipment found").get();
    private static final ItemStack selectValidationButton = new ItemBuilder(getButtonData("editor_recipe_grid_selectvalidation", Material.BARRIER))
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
    private static final ItemStack selectToolItemButton = new ItemBuilder(getButtonData("editor_recipe_grid_selecttoolitem", Material.IRON_HOE))
            .name("&eSelect which item is the tool")
            .stringTag(BUTTON_ACTION_KEY, "selectToolItemButton")
            .lore("&eDrag-and-drop onto the",
                    "&eitem in the grid &7you want to",
                    "&7mark as the tool required",
                    "&7to make the recipe.",
                    "&eDrag-and-drop it onto itself",
                    "&7to remove the previous selection.",
                    "&eIf none are selected, the tool",
                    "&e(if any) needs to be in the player's",
                    "&einventory").get();
    private static final ItemStack toggleShapelessButton = new ItemBuilder(getButtonData("editor_recipe_grid_toggleshapeless", Material.SLIME_BALL))
            .name("&eShapeless")
            .stringTag(BUTTON_ACTION_KEY, "toggleShapelessButton")
            .lore("&7If enabled, the ingredients will",
                    "&7not need to be in any specific",
                    "&7configuration to craft.",
                    "&7Otherwise the player will need",
                    "&7to match the shape of the ingredients",
                    "&7given here exactly").get();
    private static final ItemStack modifierButton = new ItemBuilder(getButtonData("editor_recipe_grid_modifiers", Material.WRITABLE_BOOK))
            .name("&dDynamic Item Modifiers")
            .stringTag(BUTTON_ACTION_KEY, "modifierButton")
            .lore("&7Modifiers are functions to edit",
                    "&7the output item based on player",
                    "&7stats or to apply crafting conditions.",
                    "&eClick to open the menu",
                    "&8&m                <>                ",
                    "%modifiers%").get();
    private static final ItemStack recipeOptionsButton = new ItemBuilder(getButtonData("editor_recipe_grid_recipeoptions", Material.WRITABLE_BOOK))
            .name("&bIngredient Options")
            .stringTag(BUTTON_ACTION_KEY, "recipeOptionsButton")
            .lore("&7Ingredient options are ingredient",
                    "&7flags you can put on an ingredient",
                    "&7to change its behavior during crafting.",
                    "&eClick to open the menu").get();
    private static final ItemStack toggleUnlockedForEveryoneButton = new ItemBuilder(getButtonData("editor_recipe_grid_unlockedforeveryone", Material.PAPER))
            .name("&eUnlocked for Everyone")
            .stringTag(BUTTON_ACTION_KEY, "toggleUnlockedForEveryoneButton")
            .lore("&7If enabled, the recipe will",
                    "&7available to everyone regardless",
                    "&7if unlocked or not. Otherwise it",
                    "&7will have to be unlocked through",
                    "&7perk rewards.",
                    "&eClick to toggle on/off").get();
    private static final ItemStack setToolRequirementButton = new ItemBuilder(getButtonData("editor_recipe_grid_settoolrequirement", Material.CRAFTING_TABLE))
            .name("&eTool Required")
            .stringTag(BUTTON_ACTION_KEY, "setToolRequirementButton")
            .lore("%description%",
                    "&8&m                <>                ",
                    "&7Determines whether this recipe requires",
                    "&7a tool to craft.",
                    "&7If no ingredient is marked as tool it",
                    "&7will need to be in the player's ",
                    "&7inventory.",
                    "&7A tool is determined by its \"Tool ID\"",
                    "&7which can be applied with a modifier.",
                    "&eRight/Left click to change the",
                    "&eID required, hold Shift during clicks",
                    "&eto change the requirement type").get();
    private static final ItemStack setDisplayNameButton = new ItemBuilder(getButtonData("editor_recipe_grid_setdisplayname", Material.NAME_TAG))
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
    private static final ItemStack setDescriptionButton = new ItemBuilder(getButtonData("editor_recipe_grid_setdescription", Material.WRITABLE_BOOK))
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

    public GridRecipeEditor(PlayerMenuUtility playerMenuUtility, DynamicGridRecipe recipe) {
        super(playerMenuUtility);
        this.recipe = recipe;

        this.items = recipe.getItems();
        this.result = recipe.getResult();
        this.requireValhallaTools = recipe.requireValhallaTools();
        this.tinker = recipe.tinker();
        this.tinkerGridIndex = recipe.getTinkerGridIndex();
        this.toolIndex = recipe.getToolIndex();
        this.shapeless = recipe.isShapeless();
        this.modifiers = recipe.getModifiers();
        this.unlockedForEveryone = recipe.isUnlockedForEveryone();
        this.toolRequirement = recipe.getToolRequirement();
        this.validations = recipe.getValidations();
        this.displayName = recipe.getDisplayName();
        this.description = recipe.getDescription();
        this.hidden = recipe.isHiddenFromBook();
    }

    public GridRecipeEditor(PlayerMenuUtility playerMenuUtility, DynamicGridRecipe recipe, String newName) {
        super(playerMenuUtility);
        this.recipe = new DynamicGridRecipe(newName);

        this.items = new HashMap<>(recipe.getItems());
        this.result = recipe.getResult().clone();
        this.requireValhallaTools = recipe.requireValhallaTools();
        this.tinker = recipe.tinker();
        this.tinkerGridIndex = recipe.getTinkerGridIndex();
        this.toolIndex = recipe.getToolIndex();
        this.shapeless = recipe.isShapeless();
        this.modifiers = new ArrayList<>(recipe.getModifiers().stream().map(DynamicItemModifier::copy).toList());
        this.unlockedForEveryone = recipe.isUnlockedForEveryone();
        this.toolRequirement = new ToolRequirement(recipe.getToolRequirement().getToolRequirementType(), recipe.getToolRequirement().getRequiredToolID());
        this.validations = new HashSet<>(recipe.getValidations());
        this.displayName = recipe.getDisplayName();
        this.description = recipe.getDescription();
        this.hidden = recipe.isHiddenFromBook();
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF201\uF80C\uF80A\uF808\uF802&8%recipe%" : TranslationManager.getTranslation("editormenu_gridrecipes")).replace("%recipe%", recipe.getName());
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(!(e.getClickedInventory() instanceof PlayerInventory));

        ItemStack cursor = e.getCursor();
        if (e.getClickedInventory() instanceof PlayerInventory) {
            if (!ItemUtils.isEmpty(cursor) && (cursor.equals(selectTinkerItemButton) || cursor.equals(selectToolItemButton))) {
                e.getWhoClicked().setItemOnCursor(null);
                e.setCancelled(true);
                setMenuItems();
                return;
            }
        }
        ItemStack clicked = e.getCurrentItem();

        String action = ItemUtils.getPDCString(BUTTON_ACTION_KEY, clicked, "");
        if (!StringUtils.isEmpty(action)){
            switch (action){
                case "backToMenuButton" -> {
                    new RecipeOverviewMenu(playerMenuUtility, RecipeOverviewMenu.CRAFTING_TABLE.getId()).open();
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
                        new RecipeOverviewMenu(playerMenuUtility, RecipeOverviewMenu.CRAFTING_TABLE.getId()).open();
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
                        Utils.sendMessage(e.getWhoClicked(), "          &2%ingredients% &afor the ingredients");
                        Utils.sendMessage(e.getWhoClicked(), "          &2%result% &afor a preformatted result");
                        Utils.sendMessage(e.getWhoClicked(), "          &2%tinker% &afor the raw name of the tinkered result");
                    } else description = null;
                }
                case "confirmButton" -> {
                    if (items.isEmpty()) {
                        Utils.sendMessage(e.getWhoClicked(), "&cPlease add some ingredients!");
                        setMenuItems();
                        return;
                    }
                    recipe.setItems(items);
                    recipe.setResult(result);
                    recipe.setModifiers(modifiers);
                    recipe.setShapeless(shapeless);
                    recipe.setTinker(tinker);
                    recipe.setRequireValhallaTools(requireValhallaTools);
                    recipe.setTinkerGridIndex(tinkerGridIndex);
                    recipe.setToolIndex(toolIndex);
                    recipe.setToolRequirement(toolRequirement);
                    recipe.setUnlockedForEveryone(unlockedForEveryone);
                    recipe.setValidations(validations);
                    recipe.setDescription(description);
                    recipe.setDisplayName(displayName);
                    recipe.setHiddenFromBook(hidden);

                    CustomRecipeRegistry.register(recipe, true);
                    CustomRecipeRegistry.setChangesMade();
                    new RecipeOverviewMenu(playerMenuUtility, RecipeOverviewMenu.CRAFTING_TABLE.getId()).open();
                    return;
                }
                case "selectValidationButton" -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new ValidationSelectionMenu(playerMenuUtility, this, Material.CRAFTING_TABLE).open();
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
                case "toggleHiddenButton" -> hidden = !hidden;
                case "toggleTinkerButton" -> tinker = !tinker;
                case "toggleShapelessButton" -> shapeless = !shapeless;
                case "toggleUnlockedForEveryoneButton" -> unlockedForEveryone = !unlockedForEveryone;
                case "selectTinkerItemButton", "selectToolItemButton" -> e.setCancelled(false);
            }
        }

        action = ItemUtils.getPDCString(BUTTON_ACTION_KEY, cursor, "");
        if (resultIndex == e.getRawSlot() || gridIndexes.contains(e.getRawSlot())){
            if (e.getRawSlot() == resultIndex && !tinker && !ItemUtils.isEmpty(cursor)) {
                result = cursor.clone();
            } else if (gridIndexes.contains(e.getRawSlot())){
                // clicked in grid
                int index = gridIndexes.indexOf(e.getRawSlot());
                if (!ItemUtils.isEmpty(cursor)){
                    SlotEntry entry = items.get(index);
                    if (entry != null && !ItemUtils.isEmpty(entry.getItem())){
                        if (!StringUtils.isEmpty(action) && action.equals("selectTinkerItemButton")) {
                            tinkerGridIndex = index;
                            e.getWhoClicked().setItemOnCursor(null);
                        } else if (!StringUtils.isEmpty(action) && action.equals("selectToolItemButton")){
                            toolIndex = index;
                            e.getWhoClicked().setItemOnCursor(null);
                        } else {
                            String option = ItemUtils.getPDCString(KEY_OPTION_ID, cursor, null);
                            if (option != null && selectedOption != null) {
                                if (!selectedOption.isCompatible(entry.getItem()) || !selectedOption.isCompatibleWithInputItem(false)) {
                                    Utils.sendMessage(e.getWhoClicked(), "&cNot compatible with this item");
                                } else {
                                    if (selectedOption.isUnique()) items.forEach((k, v) -> {
                                        IngredientChoice choice = v.getOption();
                                        if (choice != null && choice.equals(selectedOption)) items.get(k).setOption(null);
                                        Utils.sendMessage(e.getWhoClicked(), "Because this option is only allowed once, " + k + " had its option removed");
                                    });
                                    if (selectedOption instanceof IngredientChoice c) items.get(index).setOption(c);
                                    e.getWhoClicked().setItemOnCursor(null);
                                }
                            } else {
                                items.put(index, new SlotEntry(new ItemBuilder(cursor.clone()).amount(1).get(), new MaterialChoice()));
                            }
                        }
                    } else {
                        items.put(index, new SlotEntry(new ItemBuilder(cursor.clone()).amount(1).get(), new MaterialChoice()));
                    }
                } else {
                    items.remove(index);
                    if (toolIndex == index) toolIndex = -1;
                    if (tinkerGridIndex == index) tinkerGridIndex = -1;
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
        } else {
            if (ItemUtils.isEmpty(e.getCursor())) return;
            for (Integer slot : e.getRawSlots()){
                if (!gridIndexes.contains(slot) ||
                        e.getView().getInventory(slot) == null ||
                        e.getView().getInventory(slot) instanceof PlayerInventory) continue;
                e.setCancelled(true);
                int index = gridIndexes.indexOf(slot);
                SlotEntry entry = items.get(index);
                if (entry == null || ItemUtils.isEmpty(entry.getItem()))
                    items.put(index, new SlotEntry(new ItemBuilder(e.getCursor().clone()).amount(1).get(), new MaterialChoice()));
            }
        }
    }

    @SuppressWarnings("all")
    @Override
    public void setMenuItems() {
        inventory.clear();
        if (!ValhallaMMO.getPluginConfig().getBoolean("admin_gui_filler_removal")) for (int i = 0; i < 54; i++) inventory.setItem(i, filler);
        for (Integer i : gridIndexes) inventory.setItem(i, null);

        for (Integer slot : items.keySet()){
            SlotEntry entry = items.get(slot);
            if (ItemUtils.isEmpty(entry.getItem())) continue;
            List<String> lore = new ArrayList<>();
            if (tinkerGridIndex == slot.intValue()) {
                lore.add(Utils.chat("&8&m            <&7Tinkering&8&m>            "));
                lore.add(Utils.chat("&aThis item will be tinkered"));
            }
            if (toolIndex == slot.intValue()) {
                lore.add(Utils.chat("&8&m              <&7Tool&8&m>              "));
                lore.add(Utils.chat("&aThis item can be any material as long"));
                lore.add(Utils.chat("&aas it matches the tool requirements"));
            }
            lore.addAll(SlotEntry.getOptionLore(entry));

            ItemStack icon = new ItemBuilder(entry.getItem().clone()).appendLore(lore).get();
            inventory.setItem(gridIndexes.get(slot), icon);
        }
        if (!tinker) inventory.setItem(resultIndex, result.clone());
        else {
            ItemStack editItem = recipe.getGridTinkerEquipment().getItem();
            inventory.setItem(resultIndex, ItemUtils.isEmpty(editItem) ?
                    new ItemBuilder(Material.BARRIER).name("&cNo valid tinkerable item found").lore("&7Recipe might not be craftable").get() :
                    new ItemBuilder(editItem.getType()).name("&dTinkered Item").get());
        }

        List<String> modifierLore = new ArrayList<>();
        modifiers.forEach(m -> modifierLore.addAll(StringUtils.separateStringIntoLines("&d> " + m.getActiveDescription(), 40)));

        boolean toolRequired = toolRequirement.getRequiredToolID() >= 0 &&
                (toolRequirement.getToolRequirementType() != ToolRequirementType.NOT_REQUIRED &&
                        toolRequirement.getToolRequirementType() != ToolRequirementType.NONE_MANDATORY);
        String toolSettings = "&7ID: &6" + toolRequirement.getRequiredToolID() + "&7 | Type: &6" + StringUtils.toPascalCase(toolRequirement.getToolRequirementType().toString().replace("_", " "));
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

        List<String> gridDetails = new ArrayList<>();
        if (recipe.isShapeless()){
            String shapelessFormat = TranslationManager.getTranslation("ingredient_format_shapeless");
            Map<SlotEntry, Integer> contents = ItemUtils.getItemTotals(recipe.getItems().values());
            for (SlotEntry entry : contents.keySet()){
                int amount = contents.get(entry);
                gridDetails.add(shapelessFormat.replace("%amount%", String.valueOf(amount)).replace("%ingredient%", SlotEntry.toString(entry)));
            }
        } else {
            String shapeFormat = TranslationManager.getTranslation("ingredient_format_grid_shape");
            String charFormat = TranslationManager.getTranslation("ingredient_format_grid_ingredient");
            DynamicGridRecipe.ShapeDetails details = recipe.getRecipeShapeStrings();
            for (String shapeLine : details.getShape()){
                gridDetails.add(shapeFormat.replace("%characters%", shapeLine));
            }
            for (Character c : details.getItems().keySet()){
                if (details.getItems().get(c) == null) continue;
                gridDetails.add(charFormat.replace("%character%", String.valueOf(c)).replace("%ingredient%", SlotEntry.toString(details.getItems().get(c))));
            }
        }

        ItemMeta resultMeta = ItemUtils.getItemMeta(result);

        String tinkerFormat = TranslationManager.getTranslation("tinker_result_format");
        List<String> description = ItemUtils.setListPlaceholder(
                Arrays.asList(this.description == null ?
                        "&eDefault".split("/n") :
                        this.description
                                .replace("%result%", tinker ? tinkerFormat.replace("%item%", SlotEntry.toString(recipe.getGridTinkerEquipment())) : ItemUtils.getItemName(resultMeta))
                                .replace("%tinker%", tinker ? SlotEntry.toString(recipe.getGridTinkerEquipment()) : ItemUtils.getItemName(resultMeta))
                                .split("/n")
                ),
                "%ingredients%", gridDetails
        );

        inventory.setItem(0, new ItemBuilder(setDisplayNameButton).lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(setDisplayNameButton), "%display_name%", List.of(displayName == null ? "&eDefault" : displayName))).get());
        inventory.setItem(1, new ItemBuilder(setDescriptionButton).lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(setDescriptionButton), "%description%", description)).get());
        inventory.setItem(3, new ItemBuilder(selectValidationButton).lore(ItemUtils.setListPlaceholder(ItemUtils.getLore(selectValidationButton), "%description%", validationLore)).get());
        inventory.setItem(5, new ItemBuilder(toggleUnlockedForEveryoneButton).name("&eUnlocked for Everyone " + (unlockedForEveryone ? "&aYes" : "&fNo")).get());
        inventory.setItem(8, new ItemBuilder(toggleHiddenButton).name("&eHidden from Recipe Book: " + (hidden ? "&aYes" : "&fNo")).get());
        inventory.setItem(9, selectTinkerItemButton);
        inventory.setItem(17, new ItemBuilder(toggleValhallaToolRequirementButton).name("&eValhalla Tools: " + (requireValhallaTools ? "&aYes" : "&fNo")).get());
        inventory.setItem(19, recipeOptionsButton);
        inventory.setItem(24, new ItemBuilder(modifierButton).lore(modifierLore).get());
        inventory.setItem(25, new ItemBuilder(toolRequirementButton).name("&eTool Required: " + (toolRequired ? "&aYes" : "&fNo")).get());
        inventory.setItem(27, selectToolItemButton);
        inventory.setItem(32, new ItemBuilder(toggleShapelessButton).name("&eShapeless: " + (shapeless ? "&aYes" : "&fNo")).get());
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
        this.selectedOption = option;
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () ->
                playerMenuUtility.getOwner().setItemOnCursor(new ItemBuilder(option.getIcon()).stringTag(KEY_OPTION_ID, option.getName()).get()), 1L);
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
