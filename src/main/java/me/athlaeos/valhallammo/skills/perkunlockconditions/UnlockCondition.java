package me.athlaeos.valhallammo.skills.perkunlockconditions;

import org.bukkit.entity.Player;

import java.util.List;

public interface UnlockCondition {
    void initCondition(Object value);
    String getValuePlaceholder();

    String getFailurePlaceholder();

    /**
     * Checks if the player meets the conditions to unlock the perk, if forceTrue is true it should return true
     * if this condition should not prevent perk visibility even if the condition is not met.
     * @param p the player to check this condition on
     * @param forceTrue should force the method to return true if this condition should not prevent perk visibility on failed condition
     * @return whether the player meets this perk requirement condition
     */
    boolean canUnlock(Player p, boolean forceTrue);

    UnlockCondition createInstance();

    String getFailedConditionMessage();

    /**
     * This method will be used in item lore, so the return value is a string list. If the name of the condition is present
     * in the icon's placeholders, the returned value will be inserted instead of it
     * @return the condition messages inserted into the item's lore if applicable
     */
    List<String> getConditionMessages();

    /**
     * Should be used to check if the plugin is capable of registering this condition, for example if it requires
     * another plugin to be hooked before it can function.
     * @return true if the condition is safe to register
     */
    default boolean canRegister() {
        return true;
    }
}
