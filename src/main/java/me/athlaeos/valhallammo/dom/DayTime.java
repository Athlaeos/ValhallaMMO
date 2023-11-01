package me.athlaeos.valhallammo.dom;

import org.bukkit.World;

public enum DayTime {
    MORNING(true, 0, 5999),
    NOON(true, 6000, 8999),
    AFTERNOON(true, 9000, 11999),
    EVENING(true, 12000, 12999),
    EARLY_NIGHT(false, 13000, 17499),
    MIDNIGHT(false, 17500, 18499),
    LATE_NIGHT(false, 18500, 22499),
    DAWN(false, 22500, 24000),
    TIMELESS(false, -1, -1);

    private final boolean isDay;
    private final int fromTime;
    private final int toTime;

    DayTime(boolean isDay, int fromTime, int toTime){
        this.isDay = isDay;
        this.fromTime = fromTime;
        this.toTime = toTime;
    }

    public boolean isDay() {
        return isDay;
    }

    public int getFromTime() {
        return fromTime;
    }

    public int getToTime() {
        return toTime;
    }

    public static DayTime getTime(World world) {
        if (world.getEnvironment() != World.Environment.NORMAL) return TIMELESS;
        long time = world.getTime();
        for (DayTime dayTime : values()){
            if (dayTime.fromTime <= time && dayTime.toTime >= time) return dayTime;
        }
        return TIMELESS;
    }
}
