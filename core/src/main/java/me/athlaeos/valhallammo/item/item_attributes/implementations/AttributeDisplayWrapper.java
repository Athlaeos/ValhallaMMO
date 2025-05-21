package me.athlaeos.valhallammo.item.item_attributes.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.CustomFlag;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.item_attributes.AttributeWrapper;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Predicate;

public class AttributeDisplayWrapper extends AttributeWrapper {
    private final Collection<Material> compatibleWith = new HashSet<>();
    private final Predicate<Double> isPositive;
    private final String defaultIcon;
    private final StatFormat add_number;
    private final StatFormat add_scalar;
    private final StatFormat multiply_scalar_1;
    private double displayStatOffset = 0;

    /**
     * Generic display wrappers display their attributes in item lore, given a stat format and with the info when the attribute
     * is considered beneficiary or not. The translations for the attributes are automatically gathered from the translation file
     * based on the attribute's name. The translation key should match "attribute_" + attributeName.toLowerCase(java.util.Locale.US), or it will
     * not be able to find a translation. If new attributes are registered by other plugins they should add these key/translation pairs to
     * the translation manager.<br>
     * If a registered attribute matches a vanilla attribute, the format will be ignored and instead a
     * @param attribute the attribute name
     * @param format the format the attribute should be displayed with
     * @param isPositive when the attribute value is good or not. Good attributes will receive a beneficial prefix, bad attributes a negative prefix
     *                   which usually just means colors.
     */
    public AttributeDisplayWrapper(String attribute, StatFormat format, String defaultIcon, Predicate<Double> isPositive) {
        super(attribute, format);
        this.isPositive = isPositive;
        this.defaultIcon = defaultIcon;
        this.add_number = null;
        this.add_scalar = null;
        this.multiply_scalar_1 = null;
    }

    /**
     * Generic display wrappers display their attributes in item lore, given a stat format and with the info when the attribute
     * is considered beneficiary or not. The translations for the attributes are automatically gathered from the translation file
     * based on the attribute's name. The translation key should match "attribute_" + attributeName.toLowerCase(java.util.Locale.US), or it will
     * not be able to find a translation. If new attributes are registered by other plugins they should add these key/translation pairs to
     * the translation manager.
     * @param attribute the attribute name
     * @param format the format the attribute should be displayed with
     * @param isPositive when the attribute value is good or not. Good attributes will receive a beneficial prefix, bad attributes a negative prefix
     *                   which usually just means colors.
     * @param compatibleWith which materials the attribute is compatible with
     */
    public AttributeDisplayWrapper(String attribute, StatFormat format, String defaultIcon, Predicate<Double> isPositive, Material... compatibleWith) {
        super(attribute, format);
        this.compatibleWith.addAll(Set.of(compatibleWith));
        this.isPositive = isPositive;
        this.defaultIcon = defaultIcon;
        this.add_number = null;
        this.add_scalar = null;
        this.multiply_scalar_1 = null;
    }

    /**
     * Sets a stat display offset to the attribute wrapper. The actual value of the attribute is unchanged, but the given value will be
     * added to the amount displayed in the item lore. <br>
     * A player sometimes has some stats by default, like 4 attack speed and 1 attack damage. This is used to offset the displayed stats to make
     * them feel more accurate. (in the case of attack speed, -4 is applied to make 1.6 attack speed not display as -2.4)
     * @param displayStatOffset how much the amount in the item's lore should be offset
     * @return the attribute, for ease of use
     */
    public AttributeDisplayWrapper offset(double displayStatOffset){
        this.displayStatOffset = displayStatOffset;
        return this;
    }

    /**
     * Generic display wrappers display their attributes in item lore, given the info when the attribute
     * is considered beneficiary or not. The translations for the attributes are automatically gathered from the translation file
     * based on the attribute's name. The translation key should match "attribute_" + attributeName.toLowerCase(java.util.Locale.US), or it will
     * not be able to find a translation. If new attributes are registered by other plugins they should add these key/translation pairs to
     * the translation manager.<br>
     * Unlike the other two constructors, the main format is left empty and replaced with 3 operation-specific formats. This constructor should be used for vanilla
     * attributes, as their operations are configurable when custom ones (at least the default ones) aren't.
     * @param attribute the attribute name
     * @param isPositive when the attribute value is good or not. Good attributes will receive a beneficial prefix, bad attributes a negative prefix
     *                   which usually just means colors.
     * @param add_number the format to use in case of an ADD_NUMBER operation
     * @param add_scalar the format to use in case of an ADD_SCALAR operation
     * @param multiply_scalar_1 the format to use in case of an MULTIPLY_SCALAR_1 operation
     */
    public AttributeDisplayWrapper(String attribute, String defaultIcon, Predicate<Double> isPositive, StatFormat add_number, StatFormat add_scalar, StatFormat multiply_scalar_1) {
        super(attribute, null);
        this.isPositive = isPositive;
        this.defaultIcon = defaultIcon;
        this.add_number = add_number;
        this.add_scalar = add_scalar;
        this.multiply_scalar_1 = multiply_scalar_1;
    }

    @Override
    public boolean isCompatible(ItemStack i) {
        return compatibleWith.isEmpty() || compatibleWith.contains(i.getType());
    }

    @Override
    public void onApply(ItemMeta i) {
        // if the item has HIDE_ATTRIBUTES, do not display in lore unless item also has DISPLAY_ATTRIBUTES
        // HIDE_ATTRIBUTES will hide vanilla attributes regardless, but if DISPLAY_ATTRIBUTES is also present these vanilla attributes will be displayed as lore instead
        boolean customFlag = CustomFlag.hasFlag(i, CustomFlag.DISPLAY_ATTRIBUTES);
        boolean vanillaFlag = i.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES);
        if (isHidden || ((isVanilla && (!customFlag || !vanillaFlag)) || (!isVanilla && (vanillaFlag && !customFlag)))) onRemove(i);
        else {
            String translation = getAttributeName();
            if (StringUtils.isEmpty(translation)) return;
            ItemUtils.replaceOrAddLore(i,
                    translation
                            .replace("%icon%", "")
                            .replace("%value%", "").trim(),
                    getLoreDisplay().trim()
            );
        }
    }
    @Override
    public void onApply(ItemBuilder i) {
        // if the item has HIDE_ATTRIBUTES, do not display in lore unless item also has DISPLAY_ATTRIBUTES
        // HIDE_ATTRIBUTES will hide vanilla attributes regardless, but if DISPLAY_ATTRIBUTES is also present these vanilla attributes will be displayed as lore instead
        boolean customFlag = CustomFlag.hasFlag(i.getMeta(), CustomFlag.DISPLAY_ATTRIBUTES);
        boolean vanillaFlag = i.getMeta().hasItemFlag(ItemFlag.HIDE_ATTRIBUTES);
        if (isHidden || ((isVanilla && (!customFlag || !vanillaFlag)) || (!isVanilla && (vanillaFlag && !customFlag)))) onRemove(i);
        else {
            String translation = getAttributeName();
            if (StringUtils.isEmpty(translation)) return;
            ItemUtils.replaceOrAddLore(i,
                    translation
                            .replace("%icon%", "")
                            .replace("%value%", "").trim(),
                    getLoreDisplay().trim()
            );
        }
    }

    @Override
    public String getLoreDisplay(){
        String format = this.format == null ? switch (operation){
            case ADD_SCALAR -> add_scalar.format(value + displayStatOffset);
            case ADD_NUMBER -> add_number.format(value + displayStatOffset);
            case MULTIPLY_SCALAR_1 -> multiply_scalar_1.format(value + displayStatOffset);
        } : this.format.format(value + displayStatOffset);
        String translation = getAttributeName();
        String prefix = prefix(isPositive.test(value + displayStatOffset));
        return Utils.chat(prefix + translation
                .replace("%value%", format)
                .replace("%icon%", getAttributeIcon() + prefix));
    }

    @Override
    public void onRemove(ItemMeta i) {
        String translation = TranslationManager.getTranslation("attribute_" + attribute.toLowerCase(java.util.Locale.US));
        if (StringUtils.isEmpty(translation)) return;
        ItemUtils.removeIfLoreContains(i, translation
                .replace("%icon%", "")
                .replace("%value%", "").trim());
    }

    @Override
    public void onRemove(ItemBuilder i) {
        String translation = TranslationManager.getTranslation("attribute_" + attribute.toLowerCase(java.util.Locale.US));
        if (StringUtils.isEmpty(translation)) return;
        ItemUtils.removeIfLoreContains(i, translation
                .replace("%icon%", "")
                .replace("%value%", "").trim());
    }

    @Override
    public String getAttributeIcon() {
        return StringUtils.isEmpty(super.getAttributeIcon()) ? ValhallaMMO.isResourcePackConfigForced() ? "&f" + defaultIcon + " " : super.getAttributeIcon() : super.getAttributeIcon();
    }

    @Override
    public AttributeWrapper copy() {
        if (format == null) return new AttributeDisplayWrapper(attribute, defaultIcon, isPositive, add_number, add_scalar, multiply_scalar_1).offset(displayStatOffset).setOperation(operation).setValue(value).convertTo(convertTo);
        else return new AttributeDisplayWrapper(attribute, format, defaultIcon, isPositive, compatibleWith.toArray(new Material[0])).offset(displayStatOffset).setOperation(operation).setValue(value).convertTo(convertTo);
    }

    public String prefix(boolean positive){
        return TranslationManager.getTranslation("stat_attribute_" + (positive ? "positive" : "negative") + "_prefix");
    }

    @Override
    public StatFormat getFormat() {
        return this.format == null ? switch (operation){
            case ADD_SCALAR -> add_scalar;
            case ADD_NUMBER -> add_number;
            case MULTIPLY_SCALAR_1 -> multiply_scalar_1;
        } : this.format;
    }

    @Override
    public String toString(){
        return this.format == null ? switch (operation){
            case ADD_SCALAR -> add_scalar.format(value + displayStatOffset);
            case ADD_NUMBER -> add_number.format(value + displayStatOffset);
            case MULTIPLY_SCALAR_1 -> multiply_scalar_1.format(value + displayStatOffset);
        } : this.format.format(value + displayStatOffset);
    }

    public Predicate<Double> getIsPositive() {
        return isPositive;
    }

    public double getDisplayStatOffset() {
        return displayStatOffset;
    }
}
