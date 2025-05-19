package me.athlaeos.valhallammo.item.item_attributes;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DefaultAttributeAdd;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DefaultAttributeRemove;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DefaultAttributeScale;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public abstract class AttributeWrapper {
    private static final Map<String, Attribute> VANILLA_ATTRIBUTES = new HashMap<>();

    protected final String attribute;
    protected String convertTo = null;
    protected Double min = Double.NEGATIVE_INFINITY;
    protected Double max = Double.MAX_VALUE;
    protected double value;
    protected boolean isHidden = false;
    protected final StatFormat format;
    protected AttributeModifier.Operation operation;
    protected boolean isVanilla;
    protected Attribute vanillaAttribute;

    public AttributeWrapper(String attribute, StatFormat format){
        this.attribute = attribute;
        this.format = format;
        this.value = 0;
        this.operation = AttributeModifier.Operation.ADD_NUMBER;
        this.vanillaAttribute = getAttribute(attribute);
        this.isVanilla = this.vanillaAttribute != null;
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
        ModifierRegistry.register(new DefaultAttributeAdd("attribute_add_" + attribute.toLowerCase(java.util.Locale.US), attribute, smallIncrement, bigIncrement, icon));
        ModifierRegistry.register(new DefaultAttributeRemove("attribute_remove_" + attribute.toLowerCase(java.util.Locale.US), attribute, icon));
        ModifierRegistry.register(new DefaultAttributeScale("attribute_scale_" + attribute.toLowerCase(java.util.Locale.US), attribute, icon));
        return this;
    }
    /**
     * Registers the modifiers for addition, removal, and scaling to {@link ModifierRegistry} with the predefined values
     * of 0.01 for smallIncrement, and 0.1 for bigIncrement
     * @param icon the icon the modifier should have
     * @return this, for ease of registration
     */
    public AttributeWrapper addModifier(Material icon){
        return addModifier(icon, 0.01, 0.1);
    }

    /**
     * Used when Minecraft has an attribute added that already existed within ValhallaMMO to convert an old custom attribute to the new vanilla one.
     * Whenever such an attributewrapper is used, it uses the new name instead of the old one.
     * @param newName the new attribute which should be used
     * @return this, for ease of registration
     */
    public AttributeWrapper convertTo(String newName){
        this.convertTo = newName;
        if (newName == null) return this;
        try {
            this.vanillaAttribute = Attribute.valueOf(newName);
            isVanilla = true;
        } catch (IllegalArgumentException ignored){
            this.vanillaAttribute = null;
            isVanilla = false;
        }
        return this;
    }

    public Material getIcon() { return icon; }
    public double getSmallIncrement() { return smallIncrement; }
    public double getBigIncrement() { return bigIncrement; }
    public boolean isHidden() { return isHidden; }
    public String getConvertTo() { return convertTo; }

    public boolean isCompatible(ItemStack i){
        return true;
    }
    public void onApply(ItemMeta i){}
    public void onRemove(ItemMeta i) {}
    public void onApply(ItemBuilder i){ onApply(i.getMeta()); }
    public void onRemove(ItemBuilder i){ onRemove(i.getMeta()); }

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
        return TranslationManager.getRawTranslation("attribute_" + attribute.toLowerCase(java.util.Locale.US));
    }

    public AttributeWrapper setValue(double value) { this.value = value; return this; }
    public AttributeWrapper setOperation(AttributeModifier.Operation operation) { this.operation = operation; return this; }
    public AttributeWrapper setHidden(boolean hidden) { isHidden = hidden; return this; }

    public String getLoreDisplay(){
        return null;
    }

    public String getAttributeIcon(){
        return TranslationManager.getTranslation("stat_icon_" + attribute.toLowerCase(java.util.Locale.US));
    }

    public abstract AttributeWrapper copy();

    private static Attribute getAttribute(String attribute) {
        // TODO: Replace this with a registry call instead of enum lookup, rn registry isn't viable with how the stat system is setup
        if (VANILLA_ATTRIBUTES.containsKey(attribute)) return VANILLA_ATTRIBUTES.get(attribute);
        Attribute vanilla = null;
        try {
            vanilla = Attribute.valueOf(attribute);
        } catch (IllegalArgumentException ignored) {}
        VANILLA_ATTRIBUTES.put(attribute, vanilla);
        return vanilla;
    }
}
