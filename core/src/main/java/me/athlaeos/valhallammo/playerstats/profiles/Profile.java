package me.athlaeos.valhallammo.playerstats.profiles;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.playerstats.profiles.properties.BooleanProperties;
import me.athlaeos.valhallammo.playerstats.profiles.properties.StatProperties;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardRegistry;
import me.athlaeos.valhallammo.skills.perk_rewards.implementations.*;
import me.athlaeos.valhallammo.skills.skills.Skill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * A profile is used to store data on a player, each registered type of profile will be persisted
 */
@SuppressWarnings("unused")
public abstract class Profile {
    protected UUID owner;

    private final Map<String, StatProperties> numberStatProperties = new HashMap<>();

    protected Collection<String> tablesToUpdate = new HashSet<>();
    protected Collection<String> allStatNames = new HashSet<>();

    protected Map<String, NumberHolder<Integer>> ints = new HashMap<>();
    protected Map<String, NumberHolder<Float>> floats = new HashMap<>();
    protected Map<String, NumberHolder<Double>> doubles = new HashMap<>();
    protected Map<String, Collection<String>> stringSets = new HashMap<>();
    protected Map<String, BooleanHolder> booleans = new HashMap<>();

    public abstract String getTableName();

    public Profile(Player owner){
        if (owner == null) return;
        this.owner = owner.getUniqueId();
    }

    public void onCacheRefresh() {
        // do nothing by default
    }

    {
        intStat("level", new PropertyBuilder().format(StatFormat.INT).min(0).create());
        doubleStat("exp", new PropertyBuilder().format(StatFormat.FLOAT_P2).create());
        doubleStat("exp_total", new PropertyBuilder().format(StatFormat.FLOAT_P2).min(0).create());
        intStat("newGamePlus"); // amount of times the player has entered a new skill loop
        intStat("maxAllowedLevel", Short.MAX_VALUE, new PropertyBuilder().format(StatFormat.INT).min(0).perkReward().create());
    }

    public int getLevel(){
        return getInt("level");
    }
    public void setLevel(int level){
        setInt("level", level);
    }

    public double getEXP(){
        return getDouble("exp");
    }
    public void setEXP(double exp){
        setDouble("exp", exp);
    }

    public double getTotalEXP(){
        return getDouble("exp_total");
    }
    public void setTotalEXP(double exp){
        setDouble("exp_total", exp);
    }

    public int getNewGamePlus() {
        return getInt("newGamePlus");
    }
    public void setNewGamePlus(int newGamePlus){
        setInt("newGamePlus", newGamePlus);
    }

    public int getMaxAllowedLevel() {
        return getInt("maxAllowedLevel");
    }
    public void setMaxAllowedLevel(int maxAllowedLevel){
        setInt("maxAllowedLevel", maxAllowedLevel);
    }

    public abstract Class<? extends Skill> getSkillType();

    public Collection<String> getInts() {
        return ints.keySet();
    }

    public Collection<String> getDoubles() {
        return doubles.keySet();
    }

    public Collection<String> getFloats() {
        return floats.keySet();
    }

    public Collection<String> getStringSets() {
        return stringSets.keySet();
    }

    public Collection<String> getBooleans() {
        return booleans.keySet();
    }

    public int getInt(String stat) {
        NumberHolder<Integer> holder = ints.get(stat);
        if (holder == null) throw new IllegalArgumentException("No int stat with name " + stat + " is registered under " + getClass().getSimpleName());
        return holder.getValue();
    }
    public int getDefaultInt(String stat) {
        NumberHolder<Integer> holder = ints.get(stat);
        if (holder == null) throw new IllegalArgumentException("No int stat with name " + stat + " is registered under " + getClass().getSimpleName());
        return holder.getDefault();
    }
    public void setInt(String stat, int value){
        NumberHolder<Integer> holder = ints.get(stat);
        if (holder == null) throw new IllegalArgumentException("No int stat with name " + stat + " is registered under " + getClass().getSimpleName());
        StatProperties properties = numberStatProperties.get(stat);
        if (properties != null) {
            if (!Double.isNaN(properties.getMin())) value = (int) Math.max(properties.getMin(), value);
            if (!Double.isNaN(properties.getMax())) value = (int) Math.min(properties.getMax(), value);
        }
        holder.setValue(value);
    }

    public float getFloat(String stat) {
        NumberHolder<Float> holder = floats.get(stat);
        if (holder == null) throw new IllegalArgumentException("No float stat with name " + stat + " is registered under " + getClass().getSimpleName());
        return holder.getValue();
    }
    public float getDefaultFloat(String stat) {
        NumberHolder<Float> holder = floats.get(stat);
        if (holder == null) throw new IllegalArgumentException("No float stat with name " + stat + " is registered under " + getClass().getSimpleName());
        return holder.getDefault();
    }
    public void setFloat(String stat, float value){
        NumberHolder<Float> holder = floats.get(stat);
        if (holder == null) throw new IllegalArgumentException("No float stat with name " + stat + " is registered under " + getClass().getSimpleName());
        StatProperties properties = numberStatProperties.get(stat);
        if (properties != null) {
            if (!Double.isNaN(properties.getMin())) value = (float) Math.max(properties.getMin(), value);
            if (!Double.isNaN(properties.getMax())) value = (float) Math.min(properties.getMax(), value);
        }
        holder.setValue(value);
    }

    public double getDouble(String stat) {
        NumberHolder<Double> holder = doubles.get(stat);
        if (holder == null) throw new IllegalArgumentException("No double stat with name " + stat + " is registered under " + getClass().getSimpleName());
        return holder.getValue();
    }
    public double getDefaultDouble(String stat) {
        NumberHolder<Double> holder = doubles.get(stat);
        if (holder == null) throw new IllegalArgumentException("No double stat with name " + stat + " is registered under " + getClass().getSimpleName());
        return holder.getDefault();
    }
    public void setDouble(String stat, double value){
        NumberHolder<Double> holder = doubles.get(stat);
        if (holder == null) throw new IllegalArgumentException("No double stat with name " + stat + " is registered under " + getClass().getSimpleName());
        StatProperties properties = numberStatProperties.get(stat);
        if (properties != null) {
            if (!Double.isNaN(properties.getMin())) value = Math.max(properties.getMin(), value);
            if (!Double.isNaN(properties.getMax())) value = Math.min(properties.getMax(), value);
        }
        holder.setValue(value);
    }

    public Collection<String> getStringSet(String stat) {
        Collection<String> strings = stringSets.get(stat);
        if (strings == null) throw new IllegalArgumentException("No stringSet stat with name " + stat + " is registered under " + getClass().getSimpleName());
        return strings;
    }
    public void setStringSet(String stat, Collection<String> value){
        if (!stringSets.containsKey(stat)) throw new IllegalArgumentException("No stringSet stat with name " + stat + " is registered under " + getClass().getSimpleName());
        stringSets.put(stat, value);
    }

    public boolean getBoolean(String stat) {
        BooleanHolder holder = booleans.get(stat);
        if (holder == null) throw new IllegalArgumentException("No boolean stat with name " + stat + " is registered under " + getClass().getSimpleName());
        return holder.getValue();
    }
    public boolean getDefaultBoolean(String stat) {
        BooleanHolder holder = booleans.get(stat);
        if (holder == null) throw new IllegalArgumentException("No boolean stat with name " + stat + " is registered under " + getClass().getSimpleName());
        return holder.getDefault();
    }
    public boolean shouldBooleanStatHavePerkReward(String stat){
        BooleanHolder holder = booleans.get(stat);
        if (holder == null || holder.getProperties() == null) return false;
        return holder.getProperties().generatePerkReward();
    }
    public void setBoolean(String stat, boolean value){
        BooleanHolder holder = booleans.get(stat);
        if (holder == null) throw new IllegalArgumentException("No boolean stat with this name " + stat + " is registered under " + getClass().getSimpleName());
        holder.setValue(value);
    }

    public Collection<String> intStatNames() {
        return ints.keySet();
    }

    public Collection<String> floatStatNames() {
        return floats.keySet();
    }

    public Collection<String> doubleStatNames() {
        return doubles.keySet();
    }

    public Collection<String> stringSetStatNames() {
        return stringSets.keySet();
    }

    public Collection<String> booleanStatNames() {
        return booleans.keySet();
    }

    public Collection<String> getAllStatNames() {
        return allStatNames;
    }

    /**
     * Registers an integer stat with the default format {@link StatFormat#INT} and generates a perk reward.
     * @param name the name of the stat
     */
    protected void intStat(String name){ intStat(name, 0, new PropertyBuilder().format(StatFormat.INT).perkReward().create()); }
    protected void intStat(String name, StatProperties properties){ intStat(name, 0, properties); }
    protected void intStat(String name, int def, StatProperties properties){
        if (allStatNames.contains(name)) throw new IllegalArgumentException("Duplicate stat name " + name);
        allStatNames.add(name);
        ints.put(name, new NumberHolder<>(def, def, properties));
        if (properties != null) this.numberStatProperties.put(name, properties);
        tablesToUpdate.add(name);
    }

    /**
     * Registers a float stat with the default format {@link StatFormat#FLOAT_P2} and generates a perk reward.
     * @param name the name of the stat
     */
    protected void floatStat(String name){ floatStat(name, 0, new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create()); }
    protected void floatStat(String name, StatProperties properties){ floatStat(name, 0, properties); }
    protected void floatStat(String name, float def, StatProperties properties){
        if (allStatNames.contains(name)) throw new IllegalArgumentException("Duplicate stat name " + name);
        allStatNames.add(name);
        floats.put(name, new NumberHolder<>(def, def, properties));
        if (properties != null) this.numberStatProperties.put(name, properties);
        tablesToUpdate.add(name);
    }

    /**
     * Registers a double stat with the default format {@link StatFormat#FLOAT_P2} and generates a perk reward.
     * @param name the name of the stat
     */
    protected void doubleStat(String name){ doubleStat(name, 0, new PropertyBuilder().format(StatFormat.FLOAT_P2).perkReward().create()); }
    protected void doubleStat(String name, StatProperties properties){ doubleStat(name, 0, properties); }
    protected void doubleStat(String name, double def, StatProperties properties){
        if (allStatNames.contains(name)) throw new IllegalArgumentException("Duplicate stat name " + name);
        allStatNames.add(name);
        doubles.put(name, new NumberHolder<>(def, def, properties));
        if (properties != null) this.numberStatProperties.put(name, properties);
        tablesToUpdate.add(name);
    }

    protected void stringSetStat(String name){
        if (allStatNames.contains(name)) throw new IllegalArgumentException("Duplicate stat name " + name);
        allStatNames.add(name);
        stringSets.put(name, new HashSet<>());
        tablesToUpdate.add(name);
    }

    protected void booleanStat(String name){ booleanStat(name, false, new BooleanProperties(true, true)); }
    protected void booleanStat(String name, BooleanProperties properties){ booleanStat(name, false, properties); }
    protected void booleanStat(String name, boolean def, BooleanProperties properties){
        if (allStatNames.contains(name)) throw new IllegalArgumentException("Duplicate stat name " + name);
        allStatNames.add(name);
        booleans.put(name, new BooleanHolder(def, def, properties));
        tablesToUpdate.add(name);
    }

    private final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "PDC_persistence_" + getClass().getSimpleName().toLowerCase(java.util.Locale.US));
    public NamespacedKey getKey(){
        return key;
    }

    public UUID getOwner() {
        return owner;
    }

    public Map<String, StatProperties> getNumberStatProperties() {
        return numberStatProperties;
    }

    public abstract Profile getBlankProfile(Player owner);

    /**
     * Merges this profile with the given profile, and assigns the new owner.<br>
     * Integers, doubles, and floats will be added together and have one of its default values subtracted.<br>
     * So if both profiles have 100 for their experience gain stat, with the default being 100, 100 + 100 - 100 = 100<br>
     * If one profile has 0 and the other -1, with the default being 0, 0 + -1 - 0 = -1<br>
     * Collections will be added together, and for booleans the value stored in the Profile in the profile parameter will be used.
     * @param profile the profile to merge into this profile
     * @param owner the owner to assign to this new merged profile (probably original owner)
     * @return the new merged profile
     */
    public Profile merge(Profile profile, Player owner){
        Profile merged = getBlankProfile(owner);
        for (String s : allStatNames){
            NumberHolder<Integer> intStat = ints.get(s);
            if (intStat != null) {
                NumberHolder<Integer> other = profile.ints.get(s);
                merged.ints.get(s).value = (int) mergeNumbers(intStat.properties, intStat.value, other.value, other.def);
                continue;
            }
            NumberHolder<Double> doubleStat = doubles.get(s);
            if (doubleStat != null) {
                NumberHolder<Double> other = profile.doubles.get(s);
                merged.doubles.get(s).value = mergeNumbers(doubleStat.properties, doubleStat.value, other.value, other.def);
                continue;
            }
            NumberHolder<Float> floatStat = floats.get(s);
            if (floatStat != null) {
                NumberHolder<Float> other = profile.floats.get(s);
                merged.floats.get(s).value = (float) mergeNumbers(floatStat.properties, floatStat.value, other.value, other.def);
                continue;
            }
            Collection<String> strings = stringSets.get(s);
            if (strings != null) {
                Collection<String> allStrings = new HashSet<>(profile.stringSets.get(s));
                allStrings.addAll(strings);
                merged.stringSets.put(s, allStrings);
                continue;
            }
            BooleanHolder booleanStat = booleans.get(s);
            if (booleanStat != null) {
                BooleanHolder other = profile.booleans.get(s);
                merged.booleans.get(s).setValue(booleanStat.getProperties().shouldPrioritizeTrue() ?
                        (other.getValue() || booleanStat.getValue()) : // if either are true, put true
                        (!(!other.getValue() || !booleanStat.getValue()))); // if either are false, put false
            } else {
                ValhallaMMO.logWarning("Stat " + s + " in " + this.getClass().getSimpleName() + " was not associated to datatype");
            }
        }
        return merged;
    }

    private double mergeNumbers(StatProperties mode, double n1, double n2, double def){
        if (mode.addWhenMerged()){
            // values of both profiles should be added together in a merge
            return (n1 + n2) - def;
        } else {
            if (mode.shouldPrioritizePositive()){
                // the greater value between both profiles should be used
                return Math.max(n1, n2);
            } else {
                // the lesser value between both profiles should be used
                return Math.min(n1, n2);
            }
        }
    }

    protected static class NumberHolder<T extends Number> {
        private T value;
        private final T def;
        private final StatProperties properties;
        public NumberHolder(T value, T def, StatProperties additive){
            this.value = value;
            this.def = def;
            this.properties = additive;
        }

        public StatProperties getProperties() {
            return properties;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public T getDefault() {
            return def;
        }
    }

    protected static class BooleanHolder {
        private boolean value;
        private final boolean def;
        private final BooleanProperties properties;
        public BooleanHolder(boolean value, boolean def, BooleanProperties additive){
            this.value = value;
            this.def = def;
            this.properties = additive;
        }

        public BooleanProperties getProperties() {
            return properties;
        }

        public boolean getValue() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }

        public boolean getDefault() {
            return def;
        }
    }

    public static StatFormat getFormat(Class<? extends Profile> type, String stat){
        Profile profile = ProfileRegistry.getRegisteredProfiles().get(type);
        for (String i : profile.intStatNames()){
            if (!i.equals(stat)) continue;
            return profile.getNumberStatProperties().get(i).getFormat();
        }
        for (String i : profile.floatStatNames()){
            if (!i.equals(stat)) continue;
            return profile.getNumberStatProperties().get(i).getFormat();
        }
        for (String i : profile.doubleStatNames()){
            if (!i.equals(stat)) continue;
            return profile.getNumberStatProperties().get(i).getFormat();
        }
        return null;
    }

    public void registerPerkRewards(){
        String skill = getSkillType().getSimpleName().toLowerCase(java.util.Locale.US).replace("skill", "");
        if (getSkillType() == null) return;
        for (String s : getAllStatNames()) {
            StatProperties properties = getNumberStatProperties().get(s);
            if (properties != null && properties.generatePerkRewards()) {
                if (intStatNames().contains(s)) {
                    PerkRewardRegistry.register(new ProfileIntAdd(skill + "_" + s + "_add", s, getClass()));
                    PerkRewardRegistry.register(new ProfileIntSet(skill + "_" + s + "_set", s, getClass()));
                } else if (floatStatNames().contains(s)) {
                    PerkRewardRegistry.register(new ProfileFloatAdd(skill + "_" + s + "_add", s, getClass()));
                    PerkRewardRegistry.register(new ProfileFloatSet(skill + "_" + s + "_set", s, getClass()));
                } else if (doubleStatNames().contains(s)) {
                    PerkRewardRegistry.register(new ProfileDoubleAdd(skill + "_" + s + "_add", s, getClass()));
                    PerkRewardRegistry.register(new ProfileDoubleSet(skill + "_" + s + "_set", s, getClass()));
                }
            }
            if (shouldBooleanStatHavePerkReward(s)){
                PerkRewardRegistry.register(new ProfileBooleanSet(skill + "_" + s + "_set", s, getClass()));
                PerkRewardRegistry.register(new ProfileBooleanToggle(skill + "_" + s + "_toggle", s, getClass()));
            }
        }
    }

    public Collection<String> getTablesToUpdate() {
        return tablesToUpdate;
    }
}
