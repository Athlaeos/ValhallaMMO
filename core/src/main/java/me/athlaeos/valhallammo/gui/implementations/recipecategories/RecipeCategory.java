package me.athlaeos.valhallammo.gui.implementations.recipecategories;

import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static me.athlaeos.valhallammo.gui.implementations.RecipeOverviewMenu.KEY_RECIPE_CATEGORY;

public abstract class RecipeCategory{
    private final String id;
    private final ItemStack icon;
    private final String title;
    private final int position;

    public RecipeCategory(String id, ItemStack icon, int position, String title){
        this.id = id;
        this.icon = new ItemBuilder(icon).stringTag(KEY_RECIPE_CATEGORY, id).get();
        this.position = position;
        this.title = title;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public int getPosition() {
        return position;
    }

    public String getId() {
        return id;
    }

    public abstract List<ItemStack> getRecipeButtons();

    public abstract void onRecipeButtonClick(String recipe, Player editor);

    public abstract void createNew(String name, Player editor);
}