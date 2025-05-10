package me.athlaeos.valhallammo.version;

import java.util.Collection;
import java.util.Set;

public enum ActivityMappings {
    CORE("core"),
    IDLE("idle"),
    WORK("work"),
    PLAY("play"),
    REST("rest"),
    MEET("meet"),
    PANIC("panic"),
    RAID("raid"),
    PRE_RAID("pre_raid"),
    HIDE("hide"),
    FIGHT("fight"),
    CELEBRATE("celebrate"),
    ADMIRE_ITEM("admire_item"),
    AVOID("avoid"),
    RIDE("ride"),
    PLAY_DEAD("play_dead"),
    LONG_JUMP("long_jump"),
    RAM("ram"),
    TONGUE("tongue"),
    SWIM("swim"),
    LAY_SPAWN("lay_spawn"),
    SNIFF("sniff"),
    INVESTIGATE("investigate"),
    ROAR("roar"),
    EMERGE("emerge"),
    DIG("dig");
    private final Collection<String> names;
    ActivityMappings(String... names){
        this.names = Set.of(names);
    }

    public static ActivityMappings fromName(String name){
        for (ActivityMappings mapping : ActivityMappings.values())
            if (mapping.names.contains(name)) return mapping;
        return null;
    }
}
