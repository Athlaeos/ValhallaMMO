package me.athlaeos.valhallammo.gui.implementations.recipecategories;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicBrewingRecipe;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCauldronRecipe;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.BrewingRecipeEditor;
import me.athlaeos.valhallammo.gui.implementations.CauldronRecipeEditor;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CauldronRecipeCategory extends RecipeCategory{


    public CauldronRecipeCategory(int position) {
        super("cauldron", new ItemBuilder(Material.CAULDRON).name("&bCauldron Recipes &7(&9" +
                        CustomRecipeRegistry.getCauldronRecipes().size() + "&7)").lore("&fClick to access all &bCauldron recipes").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF30C\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_recipecategory_cauldron")));
    }

    @Override
    public List<ItemStack> getRecipeButtons() {
        List<ItemStack> icons = new ArrayList<>();
        for (DynamicCauldronRecipe recipe : CustomRecipeRegistry.getCauldronRecipes().values()){
            List<String> lore = new ArrayList<>();
            if (!recipe.getIngredients().isEmpty()){
                for (ItemStack entry : recipe.getIngredients().keySet()){
                    int amount = recipe.getIngredients().get(entry);
                    lore.add("&e" + amount + "&7x &e" + recipe.getMetaRequirement().getChoice().ingredientDescription(entry));
                }
            } else lore.add("&eReaction occurs for free");
            if (recipe.isTimedRecipe())
                lore.add("&fLeaving ingredients in for " + StringUtils.toTimeStamp2(recipe.getCookTime(), 20) + "s");
            else
                lore.add("&fThrowing " + recipe.getCatalyst().getItem().getAmount() + "x" + SlotEntry.toString(recipe.getCatalyst()) + " &finto cauldron");
            lore.add("&fproduces " + (recipe.tinkerCatalyst() && !recipe.isTimedRecipe() ? "&eTinkered Catalyst" : ItemUtils.getItemName(ItemUtils.getItemMeta(recipe.getResult()))));
            lore.add("&8&m                <>                ");
            lore.add(recipe.requiresValhallaTools() ? "&fRequires ValhallaMMO equipment" : "&fVanilla equipment may be used");
            lore.add(recipe.isUnlockedForEveryone() ? "&aAccessible to anyone" : "&aNeeds to be unlocked to craft");

            if (!recipe.getValidations().isEmpty()){
                lore.add("&8&m                <>                ");
                for (String v : recipe.getValidations()){
                    Validation validation = ValidationRegistry.getValidation(v);
                    lore.add(validation.activeDescription());
                }
            } else lore.add("&fNo special conditions required");
            lore.add("&8&m                <>                ");

            recipe.getModifiers().forEach(m -> lore.add(m.getActiveDescription()));

            icons.add(new ItemBuilder(recipe.tinkerCatalyst() ? recipe.getCatalyst().getItem().getType() : recipe.getResult().getType())
                    .name("&f" + recipe.getName())
                    .lore(lore)
                    .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DYE)
                    .stringTag(RecipeOverviewMenu.KEY_RECIPE, recipe.getName()).get());
        }
        icons.sort(Comparator.comparing(ItemStack::getType).thenComparing(item -> ChatColor.stripColor(ItemUtils.getItemName(ItemUtils.getItemMeta(item)))));
        return icons;
    }

    @Override
    public void onRecipeButtonClick(String recipeName, Player editor) {
        DynamicCauldronRecipe recipe = CustomRecipeRegistry.getCauldronRecipes().get(recipeName);
        if (recipe == null) throw new IllegalArgumentException("Cauldron recipe of this name does not exist");
        new CauldronRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(editor), recipe).open();
    }

    @Override
    public void createNew(String name, Player editor) {
        new CauldronRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(editor), new DynamicCauldronRecipe(name)).open();
    }
}
