package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

public abstract class RelationalToItemModifier extends DynamicItemModifier {
    public RelationalToItemModifier(String name) {
        super(name);
    }

    @Override
    public boolean meetsRequirement(ModifierContext context) {
        return !context.getOtherInvolvedItems().isEmpty();
    }
}
