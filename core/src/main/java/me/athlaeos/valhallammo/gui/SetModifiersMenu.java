package me.athlaeos.valhallammo.gui;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;

import java.util.List;

public interface SetModifiersMenu {
    void setResultModifiers(List<DynamicItemModifier> resultModifiers);

    List<DynamicItemModifier> getResultModifiers();
}
