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

public class BlastingRecipeCategory extends CookingRecipeCategory{
    public BlastingRecipeCategory(int position) {
        super("blasting",
                new ItemBuilder(Material.BLAST_FURNACE)
                        .name("&6Blast Furnace Recipes &7(&e" +
                                CustomRecipeRegistry.getCookingRecipes().entrySet().stream().filter(
                                        e -> e.getValue().getType() == DynamicCookingRecipe.CookingRecipeType.BLAST_FURNACE
                                ).count() + "&7)").lore("&fClick to access all &6Blast furnace recipes").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF308\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_recipecategory_blastfurnace")), DynamicCookingRecipe.CookingRecipeType.BLAST_FURNACE);
    }

    @Override
    public void createNew(String name, Player editor) {
        new CookingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(editor), new DynamicCookingRecipe(name, DynamicCookingRecipe.CookingRecipeType.BLAST_FURNACE)).open();
    }
}
