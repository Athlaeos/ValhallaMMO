package me.athlaeos.valhallammo.progression.perkresourcecost;

import me.athlaeos.valhallammo.progression.perkresourcecost.implementations.EconomyExpense;
import me.athlaeos.valhallammo.progression.perkresourcecost.implementations.ExperienceExpense;
import me.athlaeos.valhallammo.progression.perkresourcecost.implementations.ExperienceLevelsExpense;
import me.athlaeos.valhallammo.progression.perkresourcecost.implementations.SkillPointsExpense;
import me.athlaeos.valhallammo.progression.perkunlockconditions.UnlockCondition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ResourceExpenseRegistry {
    private static final Map<String, ResourceExpense> expenses = new HashMap<>();

    public static void registerDefaultExpenses(){
        register("cost_money", new EconomyExpense());
        register("cost_experience", new ExperienceExpense());
        register("cost_levels", new ExperienceLevelsExpense());
        register("cost", new SkillPointsExpense());
    }

    public static void register(String key, ResourceExpense expense){
        if (expense.canRegister()) expenses.put(key, expense);
    }

    public static ResourceExpense createExpenseInstance(String key){
        ResourceExpense expense = expenses.get(key);
        if (expense == null) return null;
        return expense.createInstance();
    }

    public static Collection<String> getValuePlaceholders() {
        return expenses.keySet();
    }

    public static Collection<String> getFailurePlaceholders() {
        return expenses.values().stream().map(ResourceExpense::getInsufficientFundsMessage).collect(Collectors.toSet());
    }
}
