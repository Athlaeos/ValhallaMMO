package me.athlaeos.valhallammo.version;

import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

public class SmithingTransformRecipeWrapper {
    public static SmithingRecipe get(NamespacedKey key, ItemStack result, RecipeChoice template, RecipeChoice base, RecipeChoice addition){
        return new SmithingTransformRecipe(key, result, template, base, addition);
    }

    public static boolean templatesMatch(SmithingRecipe recipe, ItemStack template){
        if (recipe instanceof SmithingTransformRecipe s){
            if (ItemUtils.isEmpty(template)) return false;
            return s.getTemplate().test(template);
        }
        return true;
    }

    public static ItemStack getTemplate(SmithingRecipe recipe){
        if (recipe instanceof SmithingTransformRecipe r){
            return convertChoice(r.getTemplate());
        } else if (recipe instanceof SmithingTrimRecipe r){
            return convertChoice(r.getTemplate());
        } else return null;
    }

    private static ItemStack convertChoice(RecipeChoice choice){
        if (choice instanceof RecipeChoice.MaterialChoice m){
            return m.getItemStack();
        } else if (choice instanceof RecipeChoice.ExactChoice m){
            return m.getItemStack();
        } else return null;
    }
}
