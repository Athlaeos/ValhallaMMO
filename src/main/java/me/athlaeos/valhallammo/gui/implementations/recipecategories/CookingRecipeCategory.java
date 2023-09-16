package me.athlaeos.valhallammo.gui.implementations.recipecategories;

import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCookingRecipe;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.CookingRecipeEditor;
import me.athlaeos.valhallammo.gui.implementations.RecipeOverviewMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class CookingRecipeCategory extends RecipeCategory{
    private final DynamicCookingRecipe.CookingRecipeType type;

    public CookingRecipeCategory(String id, ItemStack icon, int position, String title, DynamicCookingRecipe.CookingRecipeType type) {
        super(id, icon, position, title);
        this.type = type;
    }

    @Override
    public List<ItemStack> getRecipeButtons() {
        List<ItemStack> icons = new ArrayList<>();
        for (DynamicCookingRecipe recipe : CustomRecipeRegistry.getCookingRecipes().values()){
            if (recipe.getType() != type) continue;
            List<String> lore = new ArrayList<>(List.of(
                    "&f" + SlotEntry.toString(recipe.getInput()) + " >> &e" + StringUtils.toTimeStamp2(recipe.getCookTime(), 20) + "s &f>> " + (recipe.tinker() ? "&eTinkered Input" : ItemUtils.getItemName(ItemUtils.getItemMeta(recipe.getResult()))),
                    recipe.getExperience() > 0 ? "&aRewards " + recipe.getExperience() + " experience" : "&aRewards no experience",
                    recipe.requireValhallaTools() ? "&fRequires ValhallaMMO equipment" : "&fVanilla equipment may be used",
                    recipe.isUnlockedForEveryone() ? "&aAccessible to anyone" : "&aNeeds to be unlocked to craft",
                    "&8&m                <>                "
            ));

            if (!recipe.getValidations().isEmpty()){
                lore.add("&8&m                <>                ");
                for (String v : recipe.getValidations()){
                    Validation validation = ValidationRegistry.getValidation(v);
                    lore.add(validation.activeDescription());
                }
            } else lore.add("&fNo special conditions required");
            lore.add("&8&m                <>                ");

            if (recipe.getModifiers().isEmpty()) lore.add("&aNo modifiers executed");
            recipe.getModifiers().forEach(m -> lore.add(m.getActiveDescription()));

            icons.add(new ItemBuilder(recipe.tinker() ? recipe.getInput().getItem().getType() : recipe.getResult().getType())
                    .name("&f" + recipe.getName())
                    .lore(lore)
                    .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DYE)
                    .color(Color.fromRGB(210, 60, 200)).stringTag(RecipeOverviewMenu.KEY_RECIPE, recipe.getName()).get());
        }
        return icons;
    }

    @Override
    public void onRecipeButtonClick(String recipeName, Player editor) {
        DynamicCookingRecipe recipe = CustomRecipeRegistry.getCookingRecipes().get(recipeName);
        if (recipe == null) throw new IllegalArgumentException("Cooking recipe of this name does not exist");
        new CookingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(editor), recipe).open();
    }
}
