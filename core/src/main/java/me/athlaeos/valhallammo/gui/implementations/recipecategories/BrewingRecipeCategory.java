package me.athlaeos.valhallammo.gui.implementations.recipecategories;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicBrewingRecipe;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.BrewingRecipeEditor;
import me.athlaeos.valhallammo.gui.implementations.RecipeOverviewMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import me.athlaeos.valhallammo.version.ConventionUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BrewingRecipeCategory extends RecipeCategory{
    public BrewingRecipeCategory(int position) {
        super("brewing", new ItemBuilder(Material.BREWING_STAND).name("&dBrewing Recipes &7(&5" +
                        CustomRecipeRegistry.getBrewingRecipes().size() + "&7)").lore("&fClick to access all &dBrewing Recipes").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF305\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_recipecategory_brewing")));
    }

    @Override
    public List<ItemStack> getRecipeButtons() {
        List<ItemStack> icons = new ArrayList<>();
        for (DynamicBrewingRecipe recipe : CustomRecipeRegistry.getBrewingRecipes().values()){
            List<String> lore = new ArrayList<>(List.of(
                    "&f" + SlotEntry.toString(recipe.getIngredient()) + " + " + SlotEntry.toString(recipe.getApplyOn()) + " = " + (recipe.tinker() ? "&eTinkered Input" : ItemUtils.getItemName(ItemUtils.getItemMeta(recipe.getResult()))),
                    recipe.isUnlockedForEveryone() ? "&aAccessible to anyone" : "&aNeeds to be unlocked to craft",
                    "&8&m                <>                "
            ));
            if (recipe.getModifiers().isEmpty()) lore.add("&aNo modifiers executed");
            recipe.getModifiers().forEach(m -> lore.addAll(StringUtils.separateStringIntoLines(m.getActiveDescription(), 40)));

            icons.add(new ItemBuilder(recipe.getIngredient().getItem())
                    .name("&f" + recipe.getName())
                    .lore(lore)
                    .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ConventionUtils.getHidePotionEffectsFlag(), ItemFlag.HIDE_DYE)
                    .color(Color.fromRGB(210, 60, 200)).stringTag(RecipeOverviewMenu.KEY_RECIPE, recipe.getName()).get());
        }
        icons.sort(Comparator.comparing(ItemStack::getType).thenComparing(item -> ChatColor.stripColor(ItemUtils.getItemName(ItemUtils.getItemMeta(item)))));
        return icons;
    }

    @Override
    public void onRecipeButtonClick(String recipeName, Player editor) {
        DynamicBrewingRecipe recipe = CustomRecipeRegistry.getBrewingRecipes().get(recipeName);
        if (recipe == null) throw new IllegalArgumentException("Brewing recipe of this name does not exist");
        new BrewingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(editor), recipe).open();
    }

    @Override
    public void createNew(String name, Player editor) {
        new BrewingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(editor), new DynamicBrewingRecipe(name)).open();
    }
}
