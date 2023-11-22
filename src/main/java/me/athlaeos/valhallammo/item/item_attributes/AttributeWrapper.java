package me.athlaeos.valhallammo.item.item_attributes;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DefaultAttributeAdd;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DefaultAttributeRemove;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DefaultAttributeScale;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class AttributeWrapper {
    protected final String attribute;
    protected Double min = Double.MIN_VALUE;
    protected Double max = Double.MAX_VALUE;
    protected double value;
    protected final StatFormat format;
    protected AttributeModifier.Operation operation;
    protected final boolean isVanilla;
    protected final Attribute vanillaAttribute;

    public AttributeWrapper(String attribute, StatFormat format){
        this.attribute = attribute;
        this.format = format;
        this.value = 0;
        this.operation = AttributeModifier.Operation.ADD_NUMBER;
        boolean isVanilla;
        Attribute vanillaAttribute;
        try {
            vanillaAttribute = Attribute.valueOf(attribute);
            isVanilla = true;
        } catch (IllegalArgumentException ignored){
            vanillaAttribute = null;
            isVanilla = false;
        }
        this.vanillaAttribute = vanillaAttribute;
        this.isVanilla = isVanilla;
    }

    private Material icon = null;
    private double smallIncrement = 0.01;
    private double bigIncrement = 0.1;
    /**
     * Registers the modifiers for addition, removal, and scaling to {@link ModifierRegistry}
     * @param icon the icon the modifier should have
     * @return this, for ease of registration
     */
    public AttributeWrapper addModifier(Material icon, double smallIncrement, double bigIncrement){
        this.icon = icon;
        this.smallIncrement = smallIncrement;
        this.bigIncrement = bigIncrement;
        ModifierRegistry.register(new DefaultAttributeAdd("attribute_add_" + attribute.toLowerCase(), attribute, smallIncrement, bigIncrement, icon));
        ModifierRegistry.register(new DefaultAttributeRemove("attribute_remove_" + attribute.toLowerCase(), attribute, icon));
        ModifierRegistry.register(new DefaultAttributeScale("attribute_scale_" + attribute.toLowerCase(), attribute, icon));
        return this;
    }

    public Material getIcon() { return icon; }
    public double getSmallIncrement() { return smallIncrement; }
    public double getBigIncrement() { return bigIncrement; }

    /**
     * Registers the modifiers for addition, removal, and scaling to {@link ModifierRegistry} with the predefined values
     * of 0.01 for smallIncrement, and 0.1 for bigIncrement
     * @param icon the icon the modifier should have
     * @return this, for ease of registration
     */
    public AttributeWrapper addModifier(Material icon){
        return addModifier(icon, 0.01, 0.1);
    }

    public boolean isCompatible(ItemStack i){
        return true;
    }
    public void onApply(ItemMeta i){}
    public void onRemove(ItemMeta i) {}

    public Double getMin() { return min; }
    public Double getMax() { return max; }
    public AttributeWrapper min(Double min){
        this.min = min;
        return this;
    }
    public AttributeWrapper max(Double max){
        this.max = max;
        return this;
    }
    public double getValue() {
        double val = value;
        if (min != null) val = Math.max(value, min);
        if (max != null) val = Math.min(value, max);
        return val;
    }
    public AttributeModifier.Operation getOperation() { return operation; }
    public String getAttribute() { return attribute; }
    public boolean isVanilla() { return isVanilla; }
    public Attribute getVanillaAttribute() { return vanillaAttribute; }
    public StatFormat getFormat() { return format; }
    public String getAttributeName(){
        return TranslationManager.getRawTranslation("attribute_" + attribute.toLowerCase());
    }

    public AttributeWrapper setValue(double value) { this.value = value; return this; }
    public AttributeWrapper setOperation(AttributeModifier.Operation operation) { this.operation = operation; return this; }



    public String getLoreDisplay(){
        return null;
    }

    public String getAttributeIcon(){
        return TranslationManager.getTranslation("stat_icon_" + attribute.toLowerCase());
    }

    public abstract AttributeWrapper copy();
}
