package me.athlaeos.valhallammo.skills.perk_rewards;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class PerkReward implements Cloneable{
    protected String name;
    protected boolean persist = true;

    public PerkReward(String name){
        this.name = name.toLowerCase();
    }

    /**
     * Executes a perk reward onto the player
     * @param player the player to execute the perk reward on
     */
    public abstract void apply(Player player);

    public abstract void remove(Player player);
    public boolean isPersistent() {
        return persist;
    }

    public void setPersistent(boolean persist) {
        this.persist = persist;
    }

    /**
     * Perk rewards take a variety of arguments to do their shenanigans, but from a config only a generic object is
     * fetched. In this method you can check if the object is of the right type, and then cast and use it in execution.
     * @param argument the generic object to parse
     */
    public abstract void parseArgument(Object argument);

    public String getName() {
        return name;
    }

    /**
     * The return value of tab autocomplete in the /val reward command
     * @param currentArg what the current value of the command arg is
     * @return a list of tab suggestions
     */
    public List<String> getTabAutoComplete(String currentArg){
        return new ArrayList<>();
    }

    @Override
    public PerkReward clone() throws CloneNotSupportedException {
        return (PerkReward) super.clone();
    }

    public abstract String rewardPlaceholder();

    protected int parseInt(Object o){
        if (o instanceof Number number) return number.intValue();
        throw new IllegalArgumentException("Invalid argument type for perk reward " + getClass().getSimpleName() + ", expected a number(integer), but was " + o.getClass().getSimpleName());
    }

    protected float parseFloat(Object o){
        if (o instanceof Number number) return number.floatValue();
        throw new IllegalArgumentException("Invalid argument type for perk reward " + getClass().getSimpleName() + ", expected a number(float), but was " + o.getClass().getSimpleName());
    }

    protected double parseDouble(Object o){
        if (o instanceof Number number) return number.doubleValue();
        throw new IllegalArgumentException("Invalid argument type for perk reward " + getClass().getSimpleName() + ", expected a number(double), but was " + o.getClass().getSimpleName());
    }

    protected String parseString(Object o){
        if (o instanceof String) return (String) o;
        throw new IllegalArgumentException("Invalid argument type for perk reward " + getClass().getSimpleName() + ", expected a string, but was " + o.getClass().getSimpleName());
    }

    protected boolean parseBoolean(Object o){
        if (o instanceof Boolean) return (Boolean) o;
        throw new IllegalArgumentException("Invalid argument type for perk reward " + getClass().getSimpleName() + ", expected a boolean, but was " + o.getClass().getSimpleName());
    }

    @SuppressWarnings("unchecked")
    protected List<String> parseStringList(Object o){
        if (o instanceof List<?>) return (List<String>) o;
        throw new IllegalArgumentException("Invalid argument type for perk reward " + getClass().getSimpleName() + ", expected a string list, but was " + o.getClass().getSimpleName());
    }

    public abstract PerkRewardArgumentType getRequiredType();
}
