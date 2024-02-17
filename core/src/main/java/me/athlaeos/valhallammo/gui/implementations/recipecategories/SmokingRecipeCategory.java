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

public class SmokingRecipeCategory extends CookingRecipeCategory{
    public SmokingRecipeCategory(int position) {
        super("smoking", new ItemBuilder(Material.SMOKER).name("&8Smoker Recipes &7(&7" +
                        CustomRecipeRegistry.getCookingRecipes().entrySet().stream().filter(
                                e -> e.getValue().getType() == DynamicCookingRecipe.CookingRecipeType.SMOKER
                        ).count() + "&7)").lore("&fClick to access all &7Smoker recipes").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF309\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_recipecategory_smoker")), DynamicCookingRecipe.CookingRecipeType.SMOKER);
    }

    @Override
    public void createNew(String name, Player editor) {
        new CookingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(editor), new DynamicCookingRecipe(name, DynamicCookingRecipe.CookingRecipeType.SMOKER)).open();
    }
}
