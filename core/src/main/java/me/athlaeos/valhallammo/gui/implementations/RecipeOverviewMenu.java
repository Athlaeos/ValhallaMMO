package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.implementations.recipecategories.*;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class RecipeOverviewMenu extends Menu {
    private static final NamespacedKey KEY_ACTION = new NamespacedKey(ValhallaMMO.getInstance(), "key_action");
    public static final NamespacedKey KEY_RECIPE_CATEGORY = new NamespacedKey(ValhallaMMO.getInstance(), "key_recipe_category");
    public static final NamespacedKey KEY_RECIPE = new NamespacedKey(ValhallaMMO.getInstance(), "key_recipe");

    public static final RecipeCategory DISABLED = new DisabledRecipesCategory(4);
    public static final RecipeCategory CRAFTING_TABLE = new GridRecipeCategory(10);
    public static final RecipeCategory FURNACE = new FurnaceRecipeCategory(13);
    public static final RecipeCategory IMMERSIVE= new ImmersiveRecipeCategory(16);
    public static final RecipeCategory BREWING = new BrewingRecipeCategory(19);
    public static final RecipeCategory BLASTING = new BlastingRecipeCategory(22);
    public static final RecipeCategory CAULDRON = new CauldronRecipeCategory(25);
    public static final RecipeCategory SMITHING = new SmithingRecipeCategory(28);
    public static final RecipeCategory SMOKING = new SmokingRecipeCategory(31);
    public static final RecipeCategory CAMPFIRE = new CampfireRecipeCategory(40);
    private static final Map<String, RecipeCategory> categories = new HashMap<>();
    static {
        registerCategory(DISABLED);
        registerCategory(CRAFTING_TABLE);
        registerCategory(FURNACE);
        registerCategory(IMMERSIVE);
        registerCategory(BREWING);
        registerCategory(BLASTING);
        registerCategory(CAULDRON);
        registerCategory(SMITHING);
        registerCategory(SMOKING);
        registerCategory(CAMPFIRE);
    }
    public static void registerCategory(RecipeCategory category){ categories.put(category.getId(), category); }
    public static Map<String, RecipeCategory> getCategories() { return new HashMap<>(categories); }

    private static final ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&r").get();
    private static final ItemStack createNewButton = new ItemBuilder(getButtonData("editor_newrecipe", Material.LIME_DYE))
            .name("&b&lNew")
            .stringTag(KEY_ACTION, "createNewButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack nextPageButton = new ItemBuilder(getButtonData("editor_nextpage", Material.ARROW))
            .name("&7&lNext page")
            .stringTag(KEY_ACTION, "nextPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack previousPageButton = new ItemBuilder(getButtonData("editor_prevpage", Material.ARROW))
            .name("&7&lPrevious page")
            .stringTag(KEY_ACTION, "previousPageButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();
    private static final ItemStack backToMenuButton = new ItemBuilder(getButtonData("editor_backtomenu", Material.BOOK))
            .name("&fBack to Menu")
            .stringTag(KEY_ACTION, "backToMenuButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).get();

    private int currentPage = 0;
    private RecipeCategory currentCategory = null;

    public RecipeOverviewMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    public RecipeOverviewMenu(PlayerMenuUtility playerMenuUtility, String category) {
        super(playerMenuUtility);
        this.currentCategory = categories.get(category);
    }

    @Override
    public String getMenuName() {
        return currentCategory == null ?
                Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF303\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_recipeoverview")) :
                Utils.chat(currentCategory.getTitle());
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (ItemUtils.isEmpty(clickedItem)) return;
        e.setCancelled(!(e.getClickedInventory() instanceof PlayerInventory));
        String action = ItemUtils.getPDCString(KEY_ACTION, clickedItem, null);
        if (!StringUtils.isEmpty(action)){
            switch (action){
                case "backToMenuButton" -> {
                    new RecipeOverviewMenu(playerMenuUtility).open();
                    return;
                }
                case "createNewButton" -> {
                    playerMenuUtility.setPreviousMenu(this); // fallback in case player doesn't wanna
                    e.getWhoClicked().closeInventory();
                    Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                            new Question("&fWhat should the recipe's name be? (type in chat, or 'cancel' to cancel)", s -> !CustomRecipeRegistry.getAllRecipes().contains(s), "&cRecipe with this name already exists! Try again")
                    ) {
                        @Override
                        public Action<Player> getOnFinish() {
                            if (getQuestions().isEmpty()) return super.getOnFinish();
                            Question question = getQuestions().get(0);
                            if (question.getAnswer() == null) return super.getOnFinish();
                            return (p) -> {
                                String answer = question.getAnswer().replaceAll(" ", "_").toLowerCase();
                                if (answer.contains("cancel")) playerMenuUtility.getPreviousMenu().open();
                                else if (CustomRecipeRegistry.getAllRecipes().contains(answer))
                                    Utils.sendMessage(getWho(), TranslationManager.getTranslation("error_command_recipe_exists"));
                                else currentCategory.createNew(answer, p);
                            };
                        }
                    };
                    Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionnaire);
                }
                case "nextPageButton" -> currentPage++;
                case "previousPageButton" -> currentPage--;
            }
        }
        String clickedRecipe = ItemUtils.getPDCString(KEY_RECIPE, clickedItem, null);
        if (!StringUtils.isEmpty(clickedRecipe)){
            if (currentCategory != null) {
                currentCategory.onRecipeButtonClick(clickedRecipe, (Player) e.getWhoClicked());
            }
        }

        String clickedCategory = ItemUtils.getPDCString(KEY_RECIPE_CATEGORY, clickedItem, "");
        if (!StringUtils.isEmpty(clickedCategory)) {
            currentCategory = categories.get(clickedCategory);
            if (currentCategory != null){
                new RecipeOverviewMenu(playerMenuUtility, currentCategory.getId()).open();
                return;
            }
        }

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

    @Override
    public void setMenuItems() {
        inventory.clear();
        if (currentCategory != null) setPickRecipeView();
        else setViewCategoriesView();
    }

    private void setPickRecipeView(){
        if (!ValhallaMMO.getPluginConfig().getBoolean("admin_gui_filler_removal")) for (int i = 45; i < 54; i++) inventory.setItem(i, filler);
        List<ItemStack> recipes = currentCategory.getRecipeButtons();
        if (!this.currentCategory.equals(DISABLED)) recipes.add(createNewButton);
        Map<Integer, List<ItemStack>> pages = Utils.paginate(45, recipes);

        currentPage = Math.max(1, Math.min(currentPage, pages.size()));

        if (!pages.isEmpty()){
            pages.get(currentPage - 1).forEach(inventory::addItem);
        }

        inventory.setItem(45, previousPageButton);
        inventory.setItem(49, backToMenuButton);
        inventory.setItem(53, nextPageButton);
    }

    private void setViewCategoriesView(){
        if (!ValhallaMMO.getPluginConfig().getBoolean("admin_gui_filler_removal")) for (int i = 0; i < 54; i++) inventory.setItem(i, filler);

        for (RecipeCategory category : categories.values()){
            inventory.setItem(category.getPosition(), new ItemBuilder(category.getIcon()).stringTag(KEY_RECIPE_CATEGORY, category.getId()).get());
        }
    }
}
