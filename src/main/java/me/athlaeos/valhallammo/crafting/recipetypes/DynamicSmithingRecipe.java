package me.athlaeos.valhallammo.crafting.recipetypes;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration.MaterialChoice;
import me.athlaeos.valhallammo.crafting.recipetypes.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.Collection;
import java.util.List;

public class DynamicSmithingRecipe {
    private final String name;
    private final NamespacedKey key;

    private ItemStack result;
    private SlotEntry template = null;
    private SlotEntry base = new SlotEntry(
            new ItemBuilder(Material.DIAMOND_SWORD).name("&r&fReplace me!").lore("&7I'm just a placeholder base!").get(),
            new MaterialChoice(),
            null);
    private SlotEntry addition = new SlotEntry(
            new ItemBuilder(Material.NETHERITE_INGOT).name("&r&fReplace me!").lore("&7I'm just a placeholder addition!").get(),
            new MaterialChoice(),
            null);
    private boolean requireValhallaTools = false;
    private boolean tinkerBase = false;
    private List<DynamicItemModifier> resultModifiers;
    private List<DynamicItemModifier> additionModifiers;
    private boolean unlockedForEveryone;

    public DynamicSmithingRecipe(String name){
        this.name = name;
        this.key = new NamespacedKey(ValhallaMMO.getInstance(), "smithingrecipe_" + name);
    }

    public String getName() {return name;}
    public NamespacedKey getKey() {return key;}
    public SlotEntry getTemplate() {return template;}
    public SlotEntry getBase() {return base;}
    public SlotEntry getAddition() {return addition;}
    public boolean isRequireValhallaTools() {return requireValhallaTools;}
    public boolean isTinkerBase() {return tinkerBase;}
    public List<DynamicItemModifier> getResultModifiers() {return resultModifiers;}
    public List<DynamicItemModifier> getAdditionModifiers() {return additionModifiers;}
    public boolean isUnlockedForEveryone() {return unlockedForEveryone;}

    public void setTemplate(SlotEntry template) {this.template = template;}
    public void setBase(SlotEntry base) {this.base = base;}
    public void setAddition(SlotEntry addition) {this.addition = addition;}
    public void setRequireValhallaTools(boolean requireValhallaTools) {this.requireValhallaTools = requireValhallaTools;}
    public void setTinkerBase(boolean tinkerBase) {this.tinkerBase = tinkerBase;}
    public void setResultModifiers(List<DynamicItemModifier> resultModifiers) {this.resultModifiers = resultModifiers;}
    public void setAdditionModifiers(List<DynamicItemModifier> additionModifiers) {this.additionModifiers = additionModifiers;}
    public void setUnlockedForEveryone(boolean unlockedForEveryone) {this.unlockedForEveryone = unlockedForEveryone;}

    public SmithingRecipe generateRecipe() {
        RecipeChoice t = null;
        if (template != null) t = template.getOption() == null ?
                new RecipeChoice.MaterialChoice(template.getItem().getType()) :
                template.getOption().getChoice(template.getItem(), false);
        if (base == null || addition == null) return null;
        RecipeChoice b = base.getOption() == null ?
                new RecipeChoice.MaterialChoice(base.getItem().getType()) :
                base.getOption().getChoice(base.getItem(), false);
        RecipeChoice a = addition.getOption() == null ?
                new RecipeChoice.MaterialChoice(addition.getItem().getType()) :
                addition.getOption().getChoice(addition.getItem(), false);

        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20) && t != null)
            return new SmithingTransformRecipe(key, result, t, b, a);
        else return new SmithingRecipe(key, result, b, a);
    }
}
