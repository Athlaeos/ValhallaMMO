package me.athlaeos.valhallammo.playerstats.profiles;

public enum ResetType {
    SKILLS_AND_STATS(true), // resets the player's stats and skill progression completely. Formerly "hard reset"
    SKILLS_ONLY(true), // resets skill progression only, preserves manually given stats
    STATS_ONLY(true), // resets manually given stats, does not reset any skill progression
    SKILLS_REFUND_EXP(false); // resets skill progression only, preserves manually given stats. Refunds exp. Formerly "soft reset"
    private final boolean askForConfirmation;
    ResetType(boolean askForConfirmation) {
        this.askForConfirmation = askForConfirmation;
    }

    public boolean shouldAskForConfirmation() {
        return askForConfirmation;
    }
}
