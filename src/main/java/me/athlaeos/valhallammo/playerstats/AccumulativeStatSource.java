package me.athlaeos.valhallammo.playerstats;

import org.bukkit.entity.Entity;

public interface AccumulativeStatSource {
    /**
     * Fetches some stat given an Entity stat possessor.
     * If the stat source only reads a stat, use can be ignored.
     * But if the stat source executes some additional change after reading said stat, use can be set to true
     * to check if the stat is only being read, and you don't want the additional change to execute.
     * @param statPossessor the Entity to fetch the stat from
     * @param use whether the stat is only meant for display purposes or actual physical execution
     * @return the stat to return
     */
    double fetch(Entity statPossessor, boolean use);
}
