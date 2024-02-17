package me.athlaeos.valhallammo.gui.implementations.recipecategories;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.ToolRequirementType;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCauldronRecipe;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicGridRecipe;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.CauldronRecipeEditor;
import me.athlaeos.valhallammo.gui.implementations.GridRecipeEditor;
import me.athlaeos.valhallammo.gui.implementations.RecipeOverviewMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GridRecipeCategory extends RecipeCategory{
    public GridRecipeCategory(int position) {
        super("crafting_table", new ItemBuilder(Material.CRAFTING_TABLE).name("&aCrafting Grid Recipes &7(&2" +
                        CustomRecipeRegistry.getGridRecipes().size() + "&7)").lore("&fClick to access all &aCrafting grid recipes").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF304\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_recipecategory_craftingtable")));
    }

    @Override
    public List<ItemStack> getRecipeButtons() {
        List<ItemStack> icons = new ArrayList<>();
        for (DynamicGridRecipe recipe : CustomRecipeRegistry.getGridRecipes().values()){
            List<String> lore = new ArrayList<>();
            if (recipe.isShapeless()){
                Map<SlotEntry, Integer> contents = ItemUtils.getItemTotals(recipe.getItems().values());
                for (SlotEntry entry : contents.keySet()){
                    int amount = contents.get(entry);
                    lore.add("&e" + amount + "&7x &e" + SlotEntry.toString(entry));
                }
            } else {
                DynamicGridRecipe.ShapeDetails details = recipe.getRecipeShapeStrings();
                for (String shapeLine : details.getShape()){
                    lore.add("&7[&e" + shapeLine + "&7]&7");
                }
                for (Character c : details.getItems().keySet()){
                    if (details.getItems().get(c) == null) continue;
                    lore.add("&e" + c + "&7: &e" + SlotEntry.toString(details.getItems().get(c)));
                }
            }

            boolean toolRequired = recipe.getToolRequirement().getRequiredToolID() >= 0 &&
                    (recipe.getToolRequirement().getToolRequirementType() != ToolRequirementType.NOT_REQUIRED ||
                            recipe.getToolRequirement().getToolRequirementType() != ToolRequirementType.NONE_MANDATORY);
            lore.add(toolRequired ? switch (recipe.getToolRequirement().getToolRequirementType()){
                case EQUAL -> "&aTool with ID " + recipe.getToolRequirement().getRequiredToolID() + " required";
                case NOT_REQUIRED -> "&aNo tool required";
                case NONE_MANDATORY -> "&aNo tool is allowed to be used";
                case EQUAL_OR_LESSER -> "&aTool with ID of or lesser than " + recipe.getToolRequirement().getRequiredToolID() + " required";
                case EQUAL_OR_GREATER -> "&aTool with ID of or greater than " + recipe.getToolRequirement().getRequiredToolID() + " required";
            } : "&aNo tool required");
            lore.add(recipe.requireValhallaTools() ? "&fRequires ValhallaMMO equipment" : "&fVanilla equipment may be used");
            lore.add(recipe.isUnlockedForEveryone() ? "&aAccessible to anyone" : "&aNeeds to be unlocked to craft");

            if (!recipe.getValidations().isEmpty()){
                lore.add("&8&m                <>                ");
                for (String v : recipe.getValidations()){
                    Validation validation = ValidationRegistry.getValidation(v);
                    lore.add(validation.activeDescription());
                }
            } else lore.add("&fNo special conditions required");
            lore.add("&8&m                <>                ");

            if (recipe.getModifiers().isEmpty()) lore.add("&aNo modifiers executed");
            recipe.getModifiers().forEach(m -> lore.addAll(StringUtils.separateStringIntoLines(m.getActiveDescription(), 40)));

            icons.add(new ItemBuilder(recipe.tinker() ? Objects.requireNonNullElse(recipe.getGridTinkerEquipment().getItem(), new ItemStack(Material.BARRIER)) : recipe.getResult())
                    .name("&f" + recipe.getName())
                    .lore(lore)
                    .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DYE)
                    .stringTag(RecipeOverviewMenu.KEY_RECIPE, recipe.getName()).get());
        }
        icons.sort(Comparator.comparing(ItemStack::getType).thenComparing(item -> ChatColor.stripColor(ItemUtils.getItemName(ItemUtils.getItemMeta(item)))));
        return icons;
    }

    @Override
    public void onRecipeButtonClick(String recipe, Player editor) {
        DynamicGridRecipe r = CustomRecipeRegistry.getGridRecipes().get(recipe);
        if (r == null) throw new IllegalArgumentException("Crafting grid recipe of this name does not exist");
        new GridRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(editor), r).open();
    }

    @Override
    public void createNew(String name, Player editor) {
        new GridRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(editor), new DynamicGridRecipe(name)).open();
    }
}