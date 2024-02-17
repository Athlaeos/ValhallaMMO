package me.athlaeos.valhallammo.dom;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.event.PlayerEnterCombatEvent;
import me.athlaeos.valhallammo.event.PlayerLeaveCombatEvent;
import org.bukkit.entity.Player;

public class CombatLog {
    private static final int combatDuration = ValhallaMMO.getPluginConfig().getInt("combat_time_frame");
    private final Player who;
    private long timeLastStartedCombat = 0;
    private long timeLastCombatAction = 0;
    private boolean isInCombat = false;
    private long timeInCombat = 0;

    public CombatLog(Player who) {
        this.who = who;
    }

    public void combatAction() {
        if (timeLastCombatAction + 10000 < System.currentTimeMillis()) {
            PlayerEnterCombatEvent event = new PlayerEnterCombatEvent(who);
            ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                timeLastStartedCombat = event.getWhen();
                timeLastCombatAction = event.getWhen();
                timeInCombat = 0;
                isInCombat = true;
            }
        } else {
            timeInCombat += (System.currentTimeMillis() - timeLastCombatAction);
            timeLastCombatAction = System.currentTimeMillis();
        }
    }

    public void checkPlayerLeftCombat() {
        if (isInCombat && timeLastCombatAction + combatDuration < System.currentTimeMillis()) { // player was previously in combat
            // player should no longer be considered in combat
            PlayerLeaveCombatEvent event = new PlayerLeaveCombatEvent(who, timeLastStartedCombat, timeLastCombatAction + combatDuration);
            ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                isInCombat = false;
                timeInCombat = event.getTimeInCombat(timeLastStartedCombat);
            }
        }
    }

    /**
     * @return the current state of the log's isInCombat property
     */
    public boolean isInCombat() {
        return isInCombat;
    }

    public long getTimeInCombat() {
        return timeInCombat;
    }

    public long getTimeLastCombatAction() {
        return timeLastCombatAction;
    }

    public long getTimeLastStartedCombat() {
        return timeLastStartedCombat;
    }
}