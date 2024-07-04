package me.athlaeos.valhallammo.skills.perkresourcecost;

import me.athlaeos.valhallammo.skills.perkresourcecost.implementations.*;

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
        register("cost_prestige", new PrestigePointsExpense());
    }

    public static void register(String key, ResourceExpense expense){
        expenses.put(key, expense);
    }

    public static ResourceExpense createExpenseInstance(String key){
        ResourceExpense expense = expenses.get(key);
        if (expense == null) return null;
        return expense.createInstance();
    }

    public static Collection<String> getValuePlaceholders() {
        return expenses.values().stream().map(ResourceExpense::getCostPlaceholder).collect(Collectors.toSet());
    }

    public static Collection<String> getFailurePlaceholders() {
        return expenses.values().stream().map(ResourceExpense::getInsufficientCostPlaceholder).collect(Collectors.toSet());
    }

    public static Map<String, ResourceExpense> getExpenses() {
        return expenses;
    }
}
