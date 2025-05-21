package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.*;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.crafting.recipetypes.*;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.SetModifiersMenu;
import me.athlaeos.valhallammo.item.CustomItem;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.ConventionUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;
import java.util.stream.Collectors;

public class DynamicModifierMenu extends Menu {
    private static final NamespacedKey KEY_ACTION = new NamespacedKey(ValhallaMMO.getInstance(), "key_action");
    private static final NamespacedKey KEY_MODIFIER_ID = new NamespacedKey(ValhallaMMO.getInstance(), "key_modifier_id");
    private static final NamespacedKey KEY_MODIFIER_CATEGORY_ID = new NamespacedKey(ValhallaMMO.getInstance(), "key_modifier_category_id");
    private static final NamespacedKey KEY_MODIFIER_BUTTON = new NamespacedKey(ValhallaMMO.getInstance(), "key_modifier_button");

    private final List<ItemStack> scrollItems = new ArrayList<>(ModifierCategoryRegistry.getCategories().values().stream().sorted(Comparator.comparingInt(ModifierCategory::order)).map(c ->
            new ItemBuilder(c.icon()).stringTag(KEY_MODIFIER_CATEGORY_ID, c.id()).get()
    ).toList());

    private static final List<Integer> modifierButtonIndexes = Arrays.asList(3, 4, 5, 6, 7, 12, 13, 14, 15, 16, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34, 39, 40, 41, 42, 43);
    private static final List<Integer> scrollBarIndexes = Arrays.asList(45, 46, 47, 48, 49, 50, 51, 52, 53);
    private static final ItemStack modifierPriorityButton = new ItemBuilder(Material.CLOCK).stringTag(KEY_ACTION, "modifierPriorityButton").name("&6&lPriority").get();
    private static final ItemStack confirmButton = new ItemBuilder(Material.STRUCTURE_VOID).stringTag(KEY_ACTION, "confirmButton").name("&b&lSave").get();
    private static final ItemStack createNewButton = new ItemBuilder(getButtonData("editor_newrecipe", Material.LIME_DYE))
            .name("&b&lNew")
            .stringTag(KEY_ACTION, "createNewButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack copyFromRecipeButton = new ItemBuilder(getButtonData("editor_modifiers_copyfromrecipe", Material.CRAFTING_TABLE))
            .name("&b&lCopy from Recipe")
            .lore("&fCopies the modifiers from a selected", "&frecipe. This overwrites existing", "&fmodifiers with the same ID!")
            .stringTag(KEY_ACTION, "copyFromRecipeButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack copyFromIndexButton = new ItemBuilder(getButtonData("editor_modifiers_copyfromindexeditem", Material.ENDER_CHEST))
            .name("&d&lCopy from Indexed Item")
            .lore("&fCopies the modifiers from a selected", "&findexed item. This overwrites existing", "&fmodifiers with the same ID!")
            .stringTag(KEY_ACTION, "copyFromIndexButton")
            .flag(ItemFlag.HIDE_ATTRIBUTES).wipeAttributes().get();
    private static final ItemStack cancelButton = new ItemBuilder(Material.BARRIER).stringTag(KEY_ACTION, "cancelButton").name("&cDelete").get();
    private static final ItemStack nextPageButton = new ItemBuilder(Material.ARROW).stringTag(KEY_ACTION, "nextPageButton").name("&7&lNext page").get();
    private static final ItemStack previousPageButton = new ItemBuilder(Material.ARROW).stringTag(KEY_ACTION, "previousPageButton").name("&7&lPrevious page").get();

    private final Menu menu;
    private View view = View.VIEW_MODIFIERS;
    private int currentPage = 0;
    private ModifierCategory currentCategory = ModifierCategoryRegistry.ALL;

    private final List<DynamicItemModifier> currentModifiers;
    private DynamicItemModifier currentModifier = null;
    private ModifierPriority priority = ModifierPriority.NEUTRAL;
    private final boolean showRelational;

    public DynamicModifierMenu(PlayerMenuUtility playerMenuUtility, Menu menu) {
        super(playerMenuUtility);
        this.menu = menu;
        this.showRelational = false;

        if (menu instanceof SetModifiersMenu m) this.currentModifiers = m.getResultModifiers();
        else this.currentModifiers = new ArrayList<>();
    }
    public DynamicModifierMenu(PlayerMenuUtility playerMenuUtility, Menu menu, boolean showAdvanced) {
        super(playerMenuUtility);
        this.menu = menu;
        this.showRelational = showAdvanced;

        if (menu instanceof SetModifiersMenu m) this.currentModifiers = m.getResultModifiers();
        else this.currentModifiers = new ArrayList<>();
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF301\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_ingredientselection"));
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
                case "confirmButton" -> {
                    if (view == View.NEW_MODIFIER){
                        if (currentModifier != null){
                            currentModifiers.removeIf(m -> m.getName().equalsIgnoreCase(currentModifier.getName()));
                            currentModifier.setPriority(priority);
                            currentModifiers.add(currentModifier);
                            DynamicItemModifier.sortModifiers(currentModifiers);

                            priority = ModifierPriority.NEUTRAL;
                            currentModifier = null;
                            view = View.VIEW_MODIFIERS;
                        }
                    } else if (view == View.PICK_MODIFIERS) view = View.VIEW_MODIFIERS;
                    else if (view == View.VIEW_MODIFIERS){
                        if (menu instanceof SetModifiersMenu m){
                            m.setResultModifiers(currentModifiers);
                            menu.open();
                        }
                    }
                }
                case "copyFromRecipeButton" -> view = View.RECIPE_MODIFIERS;
                case "copyFromIndexButton" -> view = View.INDEX_MODIFIERS;
                case "createNewButton" -> view = View.PICK_MODIFIERS;
                case "nextPageButton" -> currentPage++;
                case "previousPageButton" -> currentPage--;
                case "cancelButton" -> {
                    if (view == View.RECIPE_MODIFIERS || view == View.INDEX_MODIFIERS) view = View.VIEW_MODIFIERS;
                    else {
                        if (currentModifier != null) {
                            if (currentModifiers.removeIf(m -> m.getName().equalsIgnoreCase(currentModifier.getName()))) view = View.VIEW_MODIFIERS;
                            else view = View.PICK_MODIFIERS;
                        }
                    }
                }
                case "modifierPriorityButton" -> {
                    int currentIndex = Arrays.asList(ModifierPriority.values()).indexOf(priority);
                    // increase priority on left click, decrease priority on right click
                    if (e.getClick().isRightClick()) currentIndex = Math.min(ModifierPriority.values().length - 1, currentIndex + 1);
                    else if (e.getClick().isLeftClick()) currentIndex = (currentIndex - 1 < 0) ? ModifierPriority.values().length - 1 : currentIndex - 1;
                    priority = ModifierPriority.values()[currentIndex];
                }
            }
        }
        String clickedModifier = ItemUtils.getPDCString(KEY_MODIFIER_ID, clickedItem, null);
        if (!StringUtils.isEmpty(clickedModifier)){
            if (view == View.PICK_MODIFIERS){
                currentModifier = ModifierRegistry.createModifier(clickedModifier);

                view = View.NEW_MODIFIER;
            } else if (view == View.VIEW_MODIFIERS){
                for (DynamicItemModifier modifier : currentModifiers){
                    if (modifier.getName().equals(clickedModifier)){
                        currentModifier = modifier;
                        priority = modifier.getPriority();
                        view = View.NEW_MODIFIER;
                        break;
                    }
                }
            }
        }
        String clickedCategory = ItemUtils.getPDCString(KEY_MODIFIER_CATEGORY_ID, clickedItem, "");
        if (!StringUtils.isEmpty(clickedCategory)) currentCategory = ModifierCategoryRegistry.getCategory(clickedCategory);
        String clickedRecipe = ItemUtils.getPDCString(RECIPES_KEY, clickedItem, null);
        List<DynamicItemModifier> newModifiers = new ArrayList<>();
        if (clickedRecipe != null) {
            if (CustomRecipeRegistry.getGridRecipes().containsKey(clickedRecipe)) newModifiers.addAll(CustomRecipeRegistry.getGridRecipes().get(clickedRecipe).getModifiers().stream().map(DynamicItemModifier::copy).toList());
            else if (CustomRecipeRegistry.getCookingRecipes().containsKey(clickedRecipe)) newModifiers.addAll(CustomRecipeRegistry.getCookingRecipes().get(clickedRecipe).getModifiers().stream().map(DynamicItemModifier::copy).toList());
            else if (CustomRecipeRegistry.getBrewingRecipes().containsKey(clickedRecipe)) newModifiers.addAll(CustomRecipeRegistry.getBrewingRecipes().get(clickedRecipe).getModifiers().stream().map(DynamicItemModifier::copy).toList());
            else if (CustomRecipeRegistry.getSmithingRecipes().containsKey(clickedRecipe)) newModifiers.addAll(CustomRecipeRegistry.getSmithingRecipes().get(clickedRecipe).getResultModifiers().stream().map(DynamicItemModifier::copy).toList());
            else if (CustomRecipeRegistry.getCauldronRecipes().containsKey(clickedRecipe)) newModifiers.addAll(CustomRecipeRegistry.getCauldronRecipes().get(clickedRecipe).getModifiers().stream().map(DynamicItemModifier::copy).toList());
            else if (CustomRecipeRegistry.getImmersiveRecipes().containsKey(clickedRecipe)) newModifiers.addAll(CustomRecipeRegistry.getImmersiveRecipes().get(clickedRecipe).getModifiers().stream().map(DynamicItemModifier::copy).toList());
            else Utils.sendMessage(e.getWhoClicked(), "&cThis recipe has already been deleted");

            if (!newModifiers.isEmpty()){
                currentModifiers.removeIf(cm -> newModifiers.stream().anyMatch(nm -> cm.getName().equalsIgnoreCase(nm.getName())));
                currentModifiers.addAll(newModifiers);
                view = View.VIEW_MODIFIERS;
            }
        }
        String clickedIndexedItem = ItemUtils.getPDCString(INDEX_KEY, clickedItem, null);
        if (clickedIndexedItem != null){
            if (CustomItemRegistry.getItems().containsKey(clickedIndexedItem)) newModifiers.addAll(CustomItemRegistry.getItem(clickedIndexedItem).getModifiers().stream().map(DynamicItemModifier::copy).toList());
            else Utils.sendMessage(e.getWhoClicked(), "&cThis item has already been deleted");

            if (!newModifiers.isEmpty()){
                currentModifiers.removeIf(cm -> newModifiers.stream().anyMatch(nm -> cm.getName().equalsIgnoreCase(nm.getName())));
                currentModifiers.addAll(newModifiers);
                view = View.VIEW_MODIFIERS;
            }
        }

        if (ItemUtils.getPDCInt(KEY_MODIFIER_BUTTON, clickedItem, 0) > 0 && currentModifier != null){
            int buttonPressed = modifierButtonIndexes.indexOf(e.getRawSlot());
            currentModifier.onButtonPress(e, buttonPressed);
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
        switch (view) {
            case VIEW_MODIFIERS -> setViewModifiersView();
            case PICK_MODIFIERS -> setPickModifiersView();
            case NEW_MODIFIER -> setNewModifierView();
            case INDEX_MODIFIERS -> setItemIndexModifiersView();
            case RECIPE_MODIFIERS -> setRecipeModifiersView();
        }
    }

    private final List<ItemStack> recipeButtons = new ArrayList<>();
    private final NamespacedKey RECIPES_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "key_recipes");
    private final NamespacedKey INDEX_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "key_index");

    private void setRecipeModifiersView(){
        List<ItemStack> buttons = new ArrayList<>();
        if (recipeButtons.isEmpty()){
            for (DynamicGridRecipe recipe : CustomRecipeRegistry.getGridRecipes().values()) buttons.add(icon(recipe.getName(), "&6Grid Recipes", recipe.tinker() ? Objects.requireNonNullElse(recipe.getGridTinkerEquipment(), new SlotEntry(recipe.getResult(), new MaterialChoice())).getItem() : recipe.getResult(), recipe.getModifiers(), true));
            for (DynamicCookingRecipe recipe : CustomRecipeRegistry.getCookingRecipes().values()) buttons.add(icon(recipe.getName(), "&c" + StringUtils.toPascalCase(recipe.getType().toString().replace("_", " ")) + " Recipes", recipe.tinker() ? recipe.getInput().getItem() : recipe.getResult(), recipe.getModifiers(), true));
            for (DynamicSmithingRecipe recipe : CustomRecipeRegistry.getSmithingRecipes().values()) buttons.add(icon(recipe.getName(), "&9Smithing Recipes", recipe.tinkerBase() ? recipe.getBase().getItem() : recipe.getResult(), recipe.getResultModifiers(), true));
            for (DynamicBrewingRecipe recipe : CustomRecipeRegistry.getBrewingRecipes().values()) buttons.add(icon(recipe.getName(), "&dBrewing Recipes", recipe.tinker() ? recipe.getIngredient().getItem() : recipe.getResult(), recipe.getModifiers(), true));
            for (DynamicCauldronRecipe recipe : CustomRecipeRegistry.getCauldronRecipes().values()) buttons.add(icon(recipe.getName(), "&bCauldron Recipes", recipe.tinkerCatalyst() ? recipe.getCatalyst().getItem() : recipe.getResult(), recipe.getModifiers(), true));
            for (ImmersiveCraftingRecipe recipe : CustomRecipeRegistry.getImmersiveRecipes().values()) buttons.add(icon(recipe.getName(), "&aImmersive Recipes", recipe.tinker() ? recipe.getTinkerInput().getItem() : recipe.getResult(), recipe.getModifiers(), true));
            recipeButtons.addAll(buttons);
        } else buttons.addAll(recipeButtons);
        buttons.sort(Comparator.comparing(ItemStack::getType).thenComparing(item -> ChatColor.stripColor(ItemUtils.getItemName(ItemUtils.getItemMeta(item)))));

        Map<Integer, List<ItemStack>> pages = Utils.paginate(45, buttons);

        currentPage = Math.max(1, Math.min(currentPage, pages.size()));

        if (!pages.isEmpty()){
            for (ItemStack i : pages.get(currentPage - 1)){
                inventory.addItem(i);
            }
        }

        inventory.setItem(45, previousPageButton);
        inventory.setItem(53, nextPageButton);
        inventory.setItem(49, cancelButton);
    }

    private void setItemIndexModifiersView(){
        List<ItemStack> buttons = new ArrayList<>();
        for (CustomItem item : CustomItemRegistry.getItems().values()) buttons.add(icon(item.getId(), "&6Grid Recipes", item.getItem(), item.getModifiers(), false));            recipeButtons.addAll(buttons);

        buttons.sort(Comparator.comparing(ItemStack::getType).thenComparing(item -> ChatColor.stripColor(ItemUtils.getItemName(ItemUtils.getItemMeta(item)))));
        Map<Integer, List<ItemStack>> pages = Utils.paginate(45, buttons);

        currentPage = Math.max(1, Math.min(currentPage, pages.size()));

        if (!pages.isEmpty()){
            for (ItemStack i : pages.get(currentPage - 1)){
                inventory.addItem(i);
            }
        }

        inventory.setItem(45, previousPageButton);
        inventory.setItem(53, nextPageButton);
        inventory.setItem(49, cancelButton);
    }

    private ItemStack icon(String name, String source, ItemStack base, List<DynamicItemModifier> modifiers, boolean recipes){
        return new ItemBuilder(base).name("&e" + name + "&f from " + source).lore("&fModifiers:", "").stringTag(recipes ? RECIPES_KEY : INDEX_KEY, name).appendLore(modifiers.stream().map(m -> "&f> " + m.getDisplayName()).toList()).get();
    }

    private void setNewModifierView(){
        if (currentModifier != null){
            if (currentModifier.getButtons() != null){
                for (Integer b : currentModifier.getButtons().keySet()){
                    if (b >= modifierButtonIndexes.size()) {
                        throw new IllegalArgumentException("Modifier " + currentModifier.getName() + " has button in invalid position " + b + ", must be between 0 and 25(non-inclusive)!");
                    }
                    ItemStack base = currentModifier.getButtons().get(b);
                    if (ItemUtils.isEmpty(base)) throw new IllegalStateException("Modifier " + currentModifier.getName() + " has empty button in slot " + b);
                    ItemStack button = new ItemBuilder(base).intTag(KEY_MODIFIER_BUTTON, 1).get();

                    inventory.setItem(modifierButtonIndexes.get(b), button);
                }
            }

            List<String> modifierPriorityLore = new ArrayList<>(List.of("&e" + priority, "&8&m                <>                ", priority.getVisual(), "&8&m                <>                "));
            modifierPriorityLore.addAll(StringUtils.separateStringIntoLines(priority.getDescription(), 40));
            ItemStack priorityButton = new ItemBuilder(modifierPriorityButton).lore(modifierPriorityLore).name("&fExecution Priority: &e" + priority).get();

            inventory.setItem(19, priorityButton);
        }
        inventory.setItem(53, confirmButton);
        inventory.setItem(45, cancelButton);
    }

    private void setPickModifiersView(){
        for (int i = 0; i < 36; i++){
            inventory.setItem(i, null);
        }
        Map<String, DynamicItemModifier> modifiers = ModifierRegistry.getModifiers();
        Collection<String> currentStringModifiers = currentModifiers.stream().map(DynamicItemModifier::getName).collect(Collectors.toSet());
        List<DynamicItemModifier> sortedModifiers = new ArrayList<>(modifiers.values());
        sortedModifiers.sort(Comparator.comparing(DynamicItemModifier::getDisplayName));

        List<ItemStack> totalModifierButtons = new ArrayList<>();
        for (DynamicItemModifier modifier : sortedModifiers){
            if (currentStringModifiers.contains(modifier.getName())) continue;
            if (modifier instanceof RelationalToItemModifier && !showRelational) continue;

            // category isn't ALL, and the modifier's categories don't contain selected category
            if (!currentCategory.equals(ModifierCategoryRegistry.ALL) && !modifier.getCategories().contains(currentCategory.id())) continue;

            ItemStack icon = new ItemBuilder(modifier.getModifierIcon())
                    .lore(StringUtils.separateStringIntoLines(modifier.getDescription(), 40))
                    .name(modifier.getDisplayName())
                    .flag(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES, ConventionUtils.getHidePotionEffectsFlag(), ItemFlag.HIDE_ENCHANTS).wipeAttributes()
                    .stringTag(KEY_MODIFIER_ID, modifier.getName()).get();

            totalModifierButtons.add(icon);
        }
        Map<Integer, List<ItemStack>> pages = Utils.paginate(36, totalModifierButtons);

        currentPage = Math.max(1, Math.min(currentPage, pages.size()));

        if (!pages.isEmpty()){
            for (ItemStack i : pages.get(currentPage - 1)){
                inventory.addItem(i);
            }
        }

        inventory.setItem(36, previousPageButton);
        inventory.setItem(44, nextPageButton);
        inventory.setItem(40, confirmButton);

        setScrollBar();
    }

    private void setViewModifiersView(){
        for (int i = 0; i < 45; i++){
            inventory.setItem(i, null);
        }
        List<DynamicItemModifier> modifiers = currentModifiers.stream().filter(m -> showRelational || !(m instanceof RelationalToItemModifier)).limit(45).sorted(Comparator.comparingInt((DynamicItemModifier a) -> a.getPriority().getPriorityRating())).toList();
        for (DynamicItemModifier modifier : modifiers){
            if (ItemUtils.isEmpty(modifier.getModifierIcon())) ValhallaMMO.logWarning("Modifier " + modifier.getName() + " has no icon, please notify plugin author Athlaeos!");
            ItemStack icon = new ItemBuilder(ItemUtils.isEmpty(modifier.getModifierIcon()) ? new ItemStack(Material.BARRIER) : modifier.getModifierIcon())
                    .lore(StringUtils.separateStringIntoLines(modifier.getActiveDescription(), 40))
                    .name(modifier.getDisplayName())
                    .stringTag(KEY_MODIFIER_ID, modifier.getName())
                    .flag(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES, ConventionUtils.getHidePotionEffectsFlag(), ItemFlag.HIDE_ENCHANTS).wipeAttributes()
                    .get();
            inventory.addItem(icon);
        }
        if (currentModifiers.size() <= 44){
            inventory.addItem(createNewButton);
        }
        inventory.setItem(46, copyFromIndexButton);
        inventory.setItem(49, confirmButton);
        inventory.setItem(52, copyFromRecipeButton);
    }

    @SuppressWarnings("unused")
    private void setScrollBar(){
        for (ModifierCategory category : ModifierCategoryRegistry.getCategories().values()){
            for (int o = 0; o < 9; o++){
                if (o >= scrollItems.size()) break;
                ItemStack iconToPut = scrollItems.get(o);
                inventory.setItem(scrollBarIndexes.get(o), iconToPut);
            }
            ItemStack centerItem = inventory.getItem(49);
            if (ItemUtils.isEmpty(centerItem)) continue;
            String centerValue = ItemUtils.getPDCString(KEY_MODIFIER_CATEGORY_ID, centerItem, "");
            if (!ItemUtils.isEmpty(centerItem) && this.currentCategory.id().equalsIgnoreCase(centerValue)) break;
            Collections.rotate(scrollItems, 1);
        }
    }

    private enum View{
        PICK_MODIFIERS,
        VIEW_MODIFIERS,
        NEW_MODIFIER,
        RECIPE_MODIFIERS,
        INDEX_MODIFIERS
    }
}
