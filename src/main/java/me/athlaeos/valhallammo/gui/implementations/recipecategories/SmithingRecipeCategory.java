package me.athlaeos.valhallammo.gui.implementations.recipecategories;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.blockvalidations.Validation;
import me.athlaeos.valhallammo.crafting.blockvalidations.ValidationRegistry;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicCauldronRecipe;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicSmithingRecipe;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.gui.implementations.CauldronRecipeEditor;
import me.athlaeos.valhallammo.gui.implementations.RecipeOverviewMenu;
import me.athlaeos.valhallammo.gui.implementations.SmithingRecipeEditor;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SmithingRecipeCategory extends RecipeCategory{
    public SmithingRecipeCategory(int position) {
        super("smithing", new ItemBuilder(Material.SMITHING_TABLE).name("&8Smithing Recipes &7(&7" +
                        CustomRecipeRegistry.getSmithingRecipes().size() + "&7)").lore("&fClick to access all &7Smithing Recipes").get(),
                position, Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF306\uF80C\uF80A\uF808\uF802" : TranslationManager.getTranslation("editormenu_recipecategory_smithing")));
    }

    @Override
    public List<ItemStack> getRecipeButtons() {
        List<ItemStack> icons = new ArrayList<>();
        for (DynamicSmithingRecipe recipe : CustomRecipeRegistry.getSmithingRecipes().values()){
            List<String> lore = new ArrayList<>();
            if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20)) lore.add(recipe.getTemplate() == null ? "&fNo template" : "&fTemplate: " + SlotEntry.toString(recipe.getTemplate()));
            lore.add("&f" + SlotEntry.toString(recipe.getBase()) + " + " + SlotEntry.toString(recipe.getAddition()) + " = " + (recipe.tinkerBase() ? "&eTinkered Base" : ItemUtils.getItemName(ItemUtils.getItemMeta(recipe.getResult()))));
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

            if (!recipe.consumeAddition()){
                if (recipe.getAdditionModifiers().isEmpty()) lore.add("&aNo modifiers executed on addition");
                else lore.add("&fAddition Modifiers");
                recipe.getAdditionModifiers().forEach(m -> lore.addAll(StringUtils.separateStringIntoLines(m.getActiveDescription(), 40)));
                lore.add("&8&m                <>                ");
            }

            if (recipe.getResultModifiers().isEmpty()) lore.add("&aNo modifiers executed on result");
            else lore.add("&fResult Modifiers");
            recipe.getResultModifiers().forEach(m -> lore.addAll(StringUtils.separateStringIntoLines(m.getActiveDescription(), 40)));

            icons.add(new ItemBuilder(recipe.tinkerBase() ? recipe.getBase().getItem().getType() : recipe.getResult().getType())
                    .name("&f" + recipe.getName())
                    .lore(lore)
                    .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DYE)
                    .color(Color.fromRGB(210, 60, 200)).stringTag(RecipeOverviewMenu.KEY_RECIPE, recipe.getName()).get());
        }
        icons.sort(Comparator.comparing(ItemStack::getType).thenComparing(item -> ChatColor.stripColor(ItemUtils.getItemName(ItemUtils.getItemMeta(item)))));
        return icons;
    }

    @Override
    public void onRecipeButtonClick(String recipeName, Player editor) {
        DynamicSmithingRecipe recipe = CustomRecipeRegistry.getSmithingRecipes().get(recipeName);
        if (recipe == null) throw new IllegalArgumentException("Smithing recipe of this name does not exist");
        new SmithingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(editor), recipe).open();
    }

    @Override
    public void createNew(String name, Player editor) {
        new SmithingRecipeEditor(PlayerMenuUtilManager.getPlayerMenuUtility(editor), new DynamicSmithingRecipe(name)).open();
    }
}
