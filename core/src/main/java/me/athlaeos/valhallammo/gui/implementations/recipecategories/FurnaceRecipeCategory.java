package me.athlaeos.valhallammo.gui.implementations.recipecategories;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCookingRecipe;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.CookingRecipeEditor;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FurnaceRecipeCategory extends CookingRecipeCategory{
    public FurnaceRecipeCategory(int position) {
        super("furnace", new ItemBuilder(Material.FURNACE).name("&7Furnace Recipes &7(&8" +
                        CustomRecipeRegistry.getCookingRecipes().entrySet().stream().filter(
                                e -> e.getValue().getType() == DynamicCookingRecipe.CookingRecipeType.FURNACE
                        ).count() + "&7)").lore("&fClick to access all &7Furnace recipes").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF307\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_recipecategory_furnace")), DynamicCookingRecipe.CookingRecipeType.FURNACE);
    }

    @Override
    public void createNew(String name, Player editor) {
        new CookingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(editor), new DynamicCookingRecipe(name, DynamicCookingRecipe.CookingRecipeType.FURNACE)).open();
    }
}
