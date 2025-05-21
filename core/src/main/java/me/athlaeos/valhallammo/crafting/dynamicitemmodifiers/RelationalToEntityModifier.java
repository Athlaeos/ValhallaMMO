package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

public abstract class RelationalToEntityModifier extends DynamicItemModifier {
    public RelationalToEntityModifier(String name) {
        super(name);
    }

    @Override
    public boolean meetsRequirement(ModifierContext context) {
        return context.getRelativeEntity() != null;
    }
}
