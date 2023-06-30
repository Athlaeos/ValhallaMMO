package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

public enum ModifierPriority {
    SOONEST(1),
    SOON(2),
    SOONISH(3),
    NEUTRAL(4),
    LATERISH(5),
    LATER(6),
    LAST(7);

    private final int priorityRating;
    ModifierPriority(int priorityRating){
        this.priorityRating = priorityRating;
    }

    public int getPriorityRating() {
        return priorityRating;
    }
}