package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

public enum ModifierPriority {
    SOONEST(1, "&fThis modifier will be executed before any other modifier", "&a■ □ &e□ □ □ &c□ □"),
    SOON(2, "&fThis modifier will be executed after SOONEST", "&a□ ■ &e□ □ □ &c□ □"),
    SOONISH(3, "&fThis modifier will be executed after SOON", "&a□ □ &e■ □ □ &c□ □"),
    NEUTRAL(4, "&fThe default execution priority", "&a□ □ &e□ ■ □ &c□ □"),
    LATERISH(5, "&fThis modifier will be executed before LATER", "&a□ □ &e□ □ ■ &c□ □"),
    LATER(6, "&fThis modifier will be executed before LAST", "&a□ □ &e□ □ □ &c■ □"),
    LAST(7, "&fThis modifier will be executed after all other modifiers", "&a□ □ &e□ □ □ &c□ ■");

    private final int priorityRating;
    private final String description;
    private final String visual;
    ModifierPriority(int priorityRating, String description, String visual){
        this.priorityRating = priorityRating;
        this.description = description;
        this.visual = visual;
    }

    public int getPriorityRating() {
        return priorityRating;
    }

    public String getDescription() {
        return description;
    }

    public String getVisual() {
        return visual;
    }
}