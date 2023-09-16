package me.athlaeos.valhallammo.gui.implementations.recipecategories;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCauldronRecipe;
import me.athlaeos.valhallammo.crafting.recipetypes.ImmersiveCraftingRecipe;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.CauldronRecipeEditor;
import me.athlaeos.valhallammo.gui.implementations.ImmersiveRecipeEditor;
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

public class ImmersiveRecipeCategory extends RecipeCategory{
    public ImmersiveRecipeCategory(int position) {
        super("immersive", new ItemBuilder(Material.ANVIL).name("&eImmersive Crafting Recipes &7(&6" +
                        CustomRecipeRegistry.getImmersiveRecipes().size() + "&7)").lore("&fClick to access all &eImmersive crafting recipes").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF30B\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_recipecategory_immersive")));
    }

    @Override
    public List<ItemStack> getRecipeButtons() {
        List<ItemStack> icons = new ArrayList<>();
        for (ImmersiveCraftingRecipe recipe : CustomRecipeRegistry.getImmersiveRecipes().values()){
            List<String> lore = new ArrayList<>();
            if (!recipe.getIngredients().isEmpty()){
                for (ItemStack entry : recipe.getIngredients().keySet()){
                    int amount = recipe.getIngredients().get(entry);
                    lore.add("&e" + amount + "&7x &e" + recipe.getMetaRequirement().getChoice().ingredientDescription(entry));
                }
            } else lore.add("&eRecipe is crafted for free");
            lore.add("&eCrafted on &f" + recipe.getBlock() + (recipe.destroysStation() ? "&e which is destroyed afterwards" : ""));
            if (recipe.tinker()) lore.add("&fHolding " + SlotEntry.toString(recipe.getTinkerInput()) + " for " + StringUtils.toTimeStamp2(recipe.getTimeToCraft(), 20) + "s upgrades it");
            else lore.add("&fProduces " + ItemUtils.getItemName(ItemUtils.getItemMeta(recipe.getResult())) + " after " + StringUtils.toTimeStamp2(recipe.getTimeToCraft(), 20) + "s");
            if (recipe.destroysStation()) lore.add("&eDestroys the crafting station afterwards");
            if (recipe.destroysStation()) lore.add("&fCan be crafted " + recipe.getConsecutiveCrafts() + " times before resetting");
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

            icons.add(new ItemBuilder(recipe.getResult().getType())
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
        ImmersiveCraftingRecipe recipe = CustomRecipeRegistry.getImmersiveRecipes().get(recipeName);
        if (recipe == null) throw new IllegalArgumentException("Immersive recipe of this name does not exist");
        new ImmersiveRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(editor), recipe).open();
    }

    @Override
    public void createNew(String name, Player editor) {
        new ImmersiveRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(editor), new ImmersiveCraftingRecipe(name)).open();
    }
}
