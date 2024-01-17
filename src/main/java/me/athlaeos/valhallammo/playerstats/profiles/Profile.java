package me.athlaeos.valhallammo.playerstats.profiles;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.persistence.Database;
import me.athlaeos.valhallammo.persistence.ProfilePersistence;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.properties.PropertyBuilder;
import me.athlaeos.valhallammo.playerstats.profiles.properties.BooleanProperties;
import me.athlaeos.valhallammo.playerstats.profiles.properties.StatProperties;
import me.athlaeos.valhallammo.skills.skills.Skill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public abstract Class<? extends Skill> getSkillType();

    public int getInt(String stat) {
        if (!ints.containsKey(stat)) throw new IllegalArgumentException("No int stat with this name is registered under " + getClass().getSimpleName());
        return ints.get(stat).getValue();
    }
    public int getDefaultInt(String stat) {
        if (!ints.containsKey(stat)) throw new IllegalArgumentException("No int stat with this name is registered under " + getClass().getSimpleName());
        return ints.get(stat).getDefault();
    }
    public void setInt(String stat, int value){
        if (!ints.containsKey(stat)) throw new IllegalArgumentException("No int stat with this name is registered under " + getClass().getSimpleName());
        if (numberStatProperties.containsKey(stat) && !Double.isNaN(numberStatProperties.get(stat).getMin())) value = (int) Math.max(numberStatProperties.get(stat).getMin(), value);
        if (numberStatProperties.containsKey(stat) && !Double.isNaN(numberStatProperties.get(stat).getMax())) value = (int) Math.min(numberStatProperties.get(stat).getMax(), value);
        ints.get(stat).setValue(value);
    }

    public float getFloat(String stat) {
        if (!floats.containsKey(stat)) throw new IllegalArgumentException("No float stat with this name is registered under " + getClass().getSimpleName());
        return floats.get(stat).getValue();
    }
    public float getDefaultFloat(String stat) {
        if (!floats.containsKey(stat)) throw new IllegalArgumentException("No float stat with this name is registered under " + getClass().getSimpleName());
        return floats.get(stat).getDefault();
    }
    public void setFloat(String stat, float value){
        if (!floats.containsKey(stat)) throw new IllegalArgumentException("No float stat with this name is registered under " + getClass().getSimpleName());
        if (numberStatProperties.containsKey(stat) && !Double.isNaN(numberStatProperties.get(stat).getMin())) value = (float) Math.max(numberStatProperties.get(stat).getMin(), value);
        if (numberStatProperties.containsKey(stat) && !Double.isNaN(numberStatProperties.get(stat).getMax())) value = (float) Math.min(numberStatProperties.get(stat).getMax(), value);
        floats.get(stat).setValue(value);
    }

    public double getDouble(String stat) {
        if (!doubles.containsKey(stat)) throw new IllegalArgumentException("No double stat with this name " + stat + " is registered under " + getClass().getSimpleName());
        return doubles.get(stat).getValue();
    }
    public double getDefaultDouble(String stat) {
        if (!doubles.containsKey(stat)) throw new IllegalArgumentException("No double stat with this name " + stat + " is registered under " + getClass().getSimpleName());
        return doubles.get(stat).getDefault();
    }
    public void setDouble(String stat, double value){
        if (!doubles.containsKey(stat)) throw new IllegalArgumentException("No double stat with this name " + stat + " is registered under " + getClass().getSimpleName());
        if (numberStatProperties.containsKey(stat) && !Double.isNaN(numberStatProperties.get(stat).getMin())) value = Math.max(numberStatProperties.get(stat).getMin(), value);
        if (numberStatProperties.containsKey(stat) && !Double.isNaN(numberStatProperties.get(stat).getMax())) value = Math.min(numberStatProperties.get(stat).getMax(), value);
        doubles.get(stat).setValue(value);
    }

    public Collection<String> getStringSet(String stat) {
        if (!stringSets.containsKey(stat)) throw new IllegalArgumentException("No stringSet stat with this name " + stat + " is registered under " + getClass().getSimpleName());
        return stringSets.get(stat);
    }
    public void setStringSet(String stat, Collection<String> value){
        if (!stringSets.containsKey(stat)) throw new IllegalArgumentException("No stringSet stat with this name " + stat + " is registered under " + getClass().getSimpleName());
        stringSets.put(stat, value);
    }

    public boolean getBoolean(String stat) {
        if (!booleans.containsKey(stat)) throw new IllegalArgumentException("No boolean stat with this name " + stat + " is registered under " + getClass().getSimpleName());
        return booleans.get(stat).getValue();
    }
    public boolean getDefaultBoolean(String stat) {
        if (!booleans.containsKey(stat)) throw new IllegalArgumentException("No boolean stat with this name " + stat + " is registered under " + getClass().getSimpleName());
        return booleans.get(stat).getDefault();
    }
    public boolean shouldBooleanStatHavePerkReward(String stat){
        if (!booleans.containsKey(stat) || booleans.get(stat).getProperties() == null) return false;
        return booleans.get(stat).getProperties().generatePerkReward();
    }
    public void setBoolean(String stat, boolean value){
        if (!booleans.containsKey(stat)) throw new IllegalArgumentException("No boolean stat with this name " + stat + " is registered under " + getClass().getSimpleName());
        booleans.get(stat).setValue(value);
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

    private final NamespacedKey key = new NamespacedKey(ValhallaMMO.getInstance(), "PDC_persistence_" + getClass().getSimpleName().toLowerCase());
    public NamespacedKey getKey(){
        return key;
    }

    public UUID getOwner() {
        return owner;
    }

    public Map<String, StatProperties> getNumberStatProperties() {
        return numberStatProperties;
    }

    public void createTable(Database conn) throws SQLException {
        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        query.append(getTableName()).append(" (");
        query.append("owner VARCHAR(40) PRIMARY KEY");

        // prepare table with all non-update stat names
        for (String s : allStatNames){
            if (tablesToUpdate.contains(s)) continue;
            String lower = s.toLowerCase();
            if (ints.containsKey(s)) query.append(", ").append(lower).append(" INTEGER default ").append(ints.get(s).def);
            if (doubles.containsKey(s)) query.append(", ").append(lower).append(" DOUBLE default ").append(doubles.get(s).def);
            if (floats.containsKey(s)) query.append(", ").append(lower).append(" FLOAT default ").append(floats.get(s).def);
            if (stringSets.containsKey(s)) query.append(", ").append(lower).append(" TEXT");
            if (booleans.containsKey(s)) query.append(", ").append(lower).append(" BOOLEAN default ").append(booleans.get(s));
        }
        query.append(");");
        conn.getConnection().prepareStatement(query.toString()).execute();

        // edit table with new columns
        for (String s : allStatNames){
            String lower = s.toLowerCase();
            if (ints.containsKey(s)) conn.addColumnIfNotExists(getTableName(), lower, "INTEGER default " + ints.get(s).def);
            if (doubles.containsKey(s)) conn.addColumnIfNotExists(getTableName(), lower, "DOUBLE default " + doubles.get(s).def);
            if (floats.containsKey(s)) conn.addColumnIfNotExists(getTableName(), lower, "FLOAT default " + floats.get(s).def);
            if (stringSets.containsKey(s)) conn.addColumnIfNotExists(getTableName(), lower, "TEXT");
            if (booleans.containsKey(s)) conn.addColumnIfNotExists(getTableName(), lower, "BOOLEAN default " + booleans.get(s).def);
        }
    }

    public void insertOrUpdateProfile(Database conn) throws SQLException {
        StringBuilder query = new StringBuilder("REPLACE INTO ").append(getTableName()).append(" (owner");
        // stat names
        Map<Integer, String> indexMap = new HashMap<>();
        int index = 2;
        for (String s : allStatNames){
            query.append(", ").append(s);
            indexMap.put(index, s);
            index++;
        }
        query.append(") VALUES (?");
        // param placeholders
        query.append(", ?".repeat(allStatNames.size()));
        query.append(");");
        // populating param placeholders
        PreparedStatement stmt = conn.getConnection().prepareStatement(query.toString());
        stmt.setString(1, owner.toString());
        for (int i : indexMap.keySet()){
            String s = indexMap.get(i);
            if (ints.containsKey(s)) stmt.setInt(i, ints.get(s).value);
            else if (doubles.containsKey(s)) stmt.setDouble(i, doubles.get(s).value);
            else if (floats.containsKey(s)) stmt.setFloat(i, floats.get(s).value);
            else if (stringSets.containsKey(s)) stmt.setString(i, ProfilePersistence.serializeStringSet(stringSets.get(s)));
            else if (booleans.containsKey(s)) stmt.setBoolean(i, booleans.get(s).getValue());
            else ValhallaMMO.logWarning("Stat " + s + " from " + this.getClass().getSimpleName() + " did not belong to a valid data type");
        }
        stmt.execute();
    }
    public Profile fetchProfile(Player p, Database conn) throws SQLException{
        PreparedStatement stmt = conn.getConnection().prepareStatement("SELECT * FROM " + getTableName() + " WHERE owner = ?;");
        stmt.setString(1, p.getUniqueId().toString());
        ResultSet result = stmt.executeQuery();
        if (result.next()){
            Profile profile = getBlankProfile(p);
            for (String s : allStatNames){
                String lower = s.toLowerCase();
                if (ints.containsKey(s)) {
                    profile.ints.get(s).value = result.getInt(lower);
                    if (result.wasNull()) profile.ints.get(s).value = profile.ints.get(s).def;
                }
                else if (doubles.containsKey(s)) {
                    profile.doubles.get(s).value = result.getDouble(lower);
                    if (result.wasNull()) profile.doubles.get(s).value = profile.doubles.get(s).def;
                }
                else if (floats.containsKey(s)) {
                    profile.floats.get(s).value = result.getFloat(lower);
                    if (result.wasNull()) profile.floats.get(s).value = profile.floats.get(s).def;
                }
                else if (stringSets.containsKey(s)) profile.stringSets.put(s, ProfilePersistence.deserializeStringSet(Objects.requireNonNullElse(result.getString(lower), "")));
                else if (booleans.containsKey(s)) {
                    profile.booleans.get(s).value = result.getBoolean(lower);
                    if (result.wasNull()) profile.booleans.get(s).value = profile.booleans.get(s).def;
                }
                else ValhallaMMO.logWarning("Stat " + s + " in " + this.getClass().getSimpleName() + " was not found in database");
            }
            return profile;
        }
        return null;
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
            if (ints.containsKey(s)) {
                StatProperties mode = this.ints.get(s).getProperties();
                merged.ints.get(s).value = (int) mergeNumbers(mode, this.ints.get(s).value, profile.ints.get(s).value, profile.ints.get(s).def);
            }
            else if (doubles.containsKey(s)) {
                StatProperties mode = this.doubles.get(s).getProperties();
                merged.doubles.get(s).value = mergeNumbers(mode, this.doubles.get(s).value, profile.doubles.get(s).value, profile.doubles.get(s).def);
            }
            else if (floats.containsKey(s)) {
                StatProperties mode = this.floats.get(s).getProperties();
                merged.floats.get(s).value = (float) mergeNumbers(mode, this.floats.get(s).value, profile.floats.get(s).value, profile.floats.get(s).def);
            }
            else if (stringSets.containsKey(s)) {
                Collection<String> sets = new HashSet<>(profile.stringSets.get(s));
                sets.addAll(this.stringSets.get(s));
                merged.stringSets.put(s, sets);
            }
            else if (booleans.containsKey(s)) {
                merged.booleans.get(s).setValue(booleans.get(s).getProperties().shouldPrioritizeTrue() ?
                        (profile.booleans.get(s).getValue() || this.booleans.get(s).getValue()) : // if either are true, put true
                        (!(!profile.booleans.get(s).getValue() || !this.booleans.get(s).getValue()))); // if either are false, put false
            }
            else ValhallaMMO.logWarning("Stat " + s + " in " + this.getClass().getSimpleName() + " was not associated to datatype");
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
}
