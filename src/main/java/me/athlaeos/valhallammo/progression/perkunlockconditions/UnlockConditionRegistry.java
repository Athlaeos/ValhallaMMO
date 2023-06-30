package me.athlaeos.valhallammo.progression.perkunlockconditions;

import me.athlaeos.valhallammo.progression.perkunlockconditions.implementations.AllPerksUnlockedRequirement;
import me.athlaeos.valhallammo.progression.perkunlockconditions.implementations.OtherSkillLevelRequirement;
import me.athlaeos.valhallammo.progression.perkunlockconditions.implementations.SinglePerkUnlockedRequirement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UnlockConditionRegistry {
    private static final Map<String, UnlockCondition> conditions = new HashMap<>();

    public static void registerDefaultConditions(){
        register(new AllPerksUnlockedRequirement());
        register(new SinglePerkUnlockedRequirement());
        register(new OtherSkillLevelRequirement());

    }

    public static void register(UnlockCondition expense){
        if (expense.canRegister()) conditions.put(expense.getValuePlaceholder(), expense);
    }

    public static UnlockCondition createConditionInstance(String key){
        UnlockCondition expense = conditions.get(key);
        if (expense == null) return null;
        return expense.createInstance();
    }

    /**
     * Retrieves a collection of all the value placeholders of all registered unlock conditions. These placeholders
     * also happen to be the keys under which they are registered.
     * These placeholders are used in perk icon lore to display what exactly is required to unlock the perk.
     * @return a collection of value placeholders
     */
    public static Collection<String> getValuePlaceholders() {
        return conditions.keySet();
    }

    /**
     * Retrieves a collection of all failure placeholders of all registered unlock conditions.
     * These placeholders are used in perk icon lore to display exactly what the player is lacking in case they do
     * not meet the required conditions to unlock the perk.
     * @return a collection of failure placeholders
     */
    public static Collection<String> getFailurePlaceholders() {
        return conditions.values().stream().map(UnlockCondition::getFailurePlaceholder).collect(Collectors.toSet());
    }
}
