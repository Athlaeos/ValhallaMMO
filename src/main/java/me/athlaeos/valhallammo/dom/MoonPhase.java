package me.athlaeos.valhallammo.dom;

import org.bukkit.World;

public enum MoonPhase {
    FULL,
    WAXING_GIBBOUS,
    FIRST_QUARTER,
    WANING_CRESCENT,
    NEW,
    WAXING_CRESCENT,
    LAST_QUARTER,
    WANING_GIBBOUS,
    NO_MOON;

    public static MoonPhase getPhase(World world){
        if (world.getEnvironment() != World.Environment.NORMAL) return NO_MOON;
        int days = (int) Math.round(world.getFullTime() / 24000D);
        int phase = days % 8;
        return switch (phase){
            case 0 -> FULL;
            case 1 -> WAXING_GIBBOUS;
            case 2 -> FIRST_QUARTER;
            case 3 -> WANING_CRESCENT;
            case 4 -> NEW;
            case 5 -> WAXING_CRESCENT;
            case 6 -> LAST_QUARTER;
            case 7 -> WANING_GIBBOUS;
            default -> NO_MOON;
        };
    }
}
