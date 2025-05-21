package me.athlaeos.valhallammo.entities;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.event.EntityUpdateLevelEvent;
import me.athlaeos.valhallammo.hooks.MythicMobsHook;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.regex.Pattern;

public class MonsterScalingManager {
    private static final NamespacedKey ENTITY_LEVEL = new NamespacedKey(ValhallaMMO.getInstance(), "entity_level");

    private static boolean enabled = false;
    private static boolean monstersOnly = true;
    private static String regionalPlayerLevel = null;
    private static final Map<String, String> globalStatScaling = new HashMap<>();
    private static final Map<String, String> defaultStatScaling = new HashMap<>();
    private static String defaultLevelScaling = null;
    private static String defaultExpOrbScaling = null;
    private static String defaultLootScaling = null;
    private static final Map<EntityType, Map<String, String>> entityStatScaling = new HashMap<>();
    private static final Map<EntityType, String> entityLevelScaling = new HashMap<>();
    private static final Map<EntityType, String> entityExpOrbScaling = new HashMap<>();
    private static final Map<EntityType, String> entityLootScaling = new HashMap<>();
    private static boolean wolfLeveling = false;
    private static String defaultWolfLevelScaling = null;

    private static final Map<UUID, Double> regionalMonsterLevelCache = new HashMap<>();

    public static void loadMonsterScalings(){
        YamlConfiguration config = ConfigManager.getConfig("mob_stats.yml").reload().get();

        enabled = config.getBoolean("enabled");
        if (!enabled) return;
        monstersOnly = config.getBoolean("monsters_only", true);
        regionalPlayerLevel = config.getString("regional_player_level");
        wolfLeveling = config.getBoolean("wolf_leveling");
        defaultWolfLevelScaling = config.getString("default_wolf_leveling");

        ConfigurationSection globalStats = config.getConfigurationSection("global");
        if (globalStats != null){
            for (String stat : globalStats.getKeys(false)){
                if (!AccumulativeStatManager.getSources().containsKey(stat)){
                    ValhallaMMO.logWarning("Invalid global stat " + stat + " referenced in mob_stats.yml");
                } else globalStatScaling.put(stat, config.getString("global." + stat));
            }
        }

        ConfigurationSection defaultStats = config.getConfigurationSection("default");
        if (defaultStats != null){
            for (String stat : defaultStats.getKeys(false)){
                if (stat.equals("level")) defaultLevelScaling = config.getString("default.level");
                else if (stat.equals("exp_orb_bonus")) defaultExpOrbScaling = config.getString("default.exp_orb_bonus");
                else if (stat.equals("loot_bonus")) defaultLootScaling = config.getString("default.loot_bonus");
                else {
                    if (!AccumulativeStatManager.getSources().containsKey(stat)){
                        ValhallaMMO.logWarning("Invalid default stat " + stat + " referenced in mob_stats.yml");
                    } else defaultStatScaling.put(stat, config.getString("default." + stat));
                }
            }
        }

        ConfigurationSection entityStats = config.getConfigurationSection("entity");
        if (entityStats != null){
            for (String entity : entityStats.getKeys(false)){
                EntityType e = Catch.catchOrElse(() -> EntityType.valueOf(entity), null);
                if (e == null) continue;

                ConfigurationSection stats = config.getConfigurationSection("entity." + entity);
                if (stats != null){
                    for (String stat : stats.getKeys(false)){
                        if (stat.equals("level")) entityLevelScaling.put(e, config.getString("entity." + entity + ".level"));
                        else if (stat.equals("exp_orb_bonus")) entityExpOrbScaling.put(e, config.getString("entity." + entity + ".exp_orb_bonus"));
                        else if (stat.equals("loot_bonus")) entityLootScaling.put(e, config.getString("entity." + entity + ".loot_bonus"));
                        else {
                            if (!AccumulativeStatManager.getSources().containsKey(stat)){
                                ValhallaMMO.logWarning("Invalid " + entity + " stat " + stat + " referenced in mob_stats.yml");
                            } else {
                                Map<String, String> existingStats = entityStatScaling.getOrDefault(e, new HashMap<>());
                                existingStats.put(stat, config.getString("entity." + entity + "." + stat));
                                entityStatScaling.put(e, existingStats);
                            }
                        }
                    }

                    // going through the default stats to add them to the entity stats, but only if the entity stats don't contain the default stat yet
                    for (String stat : defaultStatScaling.keySet()){
                        if (entityStatScaling.containsKey(e) && !entityStatScaling.get(e).containsKey(stat)) {
                            Map<String, String> existingStats = entityStatScaling.getOrDefault(e, new HashMap<>());
                            existingStats.put(stat, defaultStatScaling.get(stat));
                            entityStatScaling.put(e, existingStats);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the exp orb bonus based on mob level
     * @param entity the entity to get their bonus exp from
     * @return the exp orb bonus, or 0 if the entity is lv <0 or invalid or if no scaling has been defined
     */
    public static double getExpOrbMultiplier(LivingEntity entity){
        if (EntityClassification.matchesClassification(entity.getType(), EntityClassification.UNALIVE)) return 0;
        if (!enabled || (monstersOnly && EntityClassification.matchesClassification(entity.getType(), EntityClassification.PASSIVE) && !(entity instanceof Boss)) || entity instanceof Player) return 0;
        int level = getLevel(entity);
        if (level < 0) return 0;
        if (entityExpOrbScaling.containsKey(entity.getType())){
            String expScaling = entityExpOrbScaling.get(entity.getType());
            if (expScaling == null) return 0;
            return Utils.eval(parseRand(expScaling.replace("%level%", String.valueOf(level))));
        } else if (defaultExpOrbScaling != null) {
            return Utils.eval(parseRand(defaultExpOrbScaling.replace("%level%", String.valueOf(level))));
        }
        return 0;
    }

    /**
     * Returns the exp orb bonus based on mob level
     * @param entity the entity to get their bonus exp from
     * @return the exp orb bonus, or 0 if the entity is lv <0 or invalid or if no scaling has been defined
     */
    public static double getLootMultiplier(LivingEntity entity){
        if (EntityClassification.matchesClassification(entity.getType(), EntityClassification.UNALIVE)) return 0;
        if (!enabled || (monstersOnly && EntityClassification.matchesClassification(entity.getType(), EntityClassification.PASSIVE) && !(entity instanceof Boss)) || entity instanceof Player) return 0;
        int level = getLevel(entity);
        if (level < 0) return 0;
        if (entityLootScaling.containsKey(entity.getType())){
            String expScaling = entityLootScaling.get(entity.getType());
            if (expScaling == null) return 0;
            return Utils.eval(parseRand(expScaling.replace("%level%", String.valueOf(level))));
        } else if (defaultLootScaling != null) {
            return Utils.eval(parseRand(defaultLootScaling.replace("%level%", String.valueOf(level))));
        }
        return 0;
    }

    /**
     * Returns the entity's stats for the given stat value. If the entity has no level, or is a player, they are treated as an unaffected entity and get no stat adjustments.
     * @param entity the entity to get their stats from
     * @param stat the stat to get from the entity
     * @return the entity's stat, or 0 if the entity is a player, or is not a monster when only monsters are allowed.
     */
    public static double getStatValue(LivingEntity entity, String stat){
        if (!enabled || EntityClassification.matchesClassification(entity.getType(), EntityClassification.UNALIVE)) return 0;
        int level = Math.max(0, getLevel(entity));
        if (ValhallaMMO.isHookFunctional(MythicMobsHook.class) && MythicMobsHook.isMythicMob(entity))
            return MythicMobsHook.getMythicMobStat(stat, entity);
        if (entityStatScaling.containsKey(entity.getType())){
            Map<String, String> entityStats = entityStatScaling.getOrDefault(entity.getType(), new HashMap<>());
            if (!entityStats.containsKey(stat)) return 0;
            return Utils.eval(parseRand(entityStatScaling.get(entity.getType()).get(stat).replace("%level%", String.valueOf(level))));
        } else if ((!EntityClassification.matchesClassification(entity.getType(), EntityClassification.PASSIVE) || entity instanceof Boss || (wolfLeveling && entity instanceof Wolf)) && defaultStatScaling.containsKey(stat)) {
            return Utils.eval(parseRand(defaultStatScaling.get(stat).replace("%level%", String.valueOf(level))));
        } else if (globalStatScaling.containsKey(stat)) {
            return Utils.eval(parseRand(globalStatScaling.get(stat).replace("%level%", String.valueOf(level))));
        }
        return 0;
    }

    /**
     * Retrieves the entity level if present, returns -1 if the entity has no level.
     * @param entity the entity to get their level from
     * @return their level, -1 if none found.
     */
    public static int getLevel(LivingEntity entity){
        return enabled ? entity.getPersistentDataContainer().getOrDefault(ENTITY_LEVEL, PersistentDataType.INTEGER, -1) : 0;
    }

    /**
     * Sets the entity level to the entity. Removed if given level is negative. Players cannot be given a level
     * @param entity the entity to set their level
     * @param level the level to set
     */
    public static void setLevel(LivingEntity entity, int level){
        if (entity instanceof Player) return;
        EntityUpdateLevelEvent event = new EntityUpdateLevelEvent(entity, level);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        level = event.getLevel();
        if (level >= 0) entity.getPersistentDataContainer().set(ENTITY_LEVEL, PersistentDataType.INTEGER, level);
        else entity.getPersistentDataContainer().remove(ENTITY_LEVEL);
        EntityAttributeStats.updateStats(entity);
    }

    /**
     * Given an entity, returns what level they should be given the surrounding players and scalings they have.
     * @param entity the entity
     * @return the level the plugin calculated they should be. Will return -1 if the entity is not a monster or no level scaling is available for them.
     */
    public static int getNewLevel(LivingEntity entity){
        if (EntityClassification.matchesClassification(entity.getType(), EntityClassification.UNALIVE)) return -1;
        if (!enabled || (monstersOnly && EntityClassification.matchesClassification(entity.getType(), EntityClassification.PASSIVE) && !(entity instanceof Boss)) || entity instanceof Player) return -1;
        int powerLevel = (int) Math.round(getAreaDifficultyLevel(entity.getLocation(), null));
        if (entityLevelScaling.containsKey(entity.getType())){
            return Math.max(0, (int) Utils.eval(parseRand(entityLevelScaling.get(entity.getType()).replace("%level%", String.valueOf(powerLevel)))));
        } else if (defaultLevelScaling != null) return Math.max(0, (int) Utils.eval(parseRand(defaultLevelScaling.replace("%level%", String.valueOf(powerLevel)))));
        else return -1;
    }

    /**
     * Updates a wolf's level and stats according to its owner's level. Nothing happens if the given player is not the wolf's owner
     * @param wolf the wolf
     * @param tamer the owner
     */
    public static boolean updateWolfLevel(Wolf wolf, AnimalTamer tamer){
        if (!(tamer instanceof Player player) || !tamer.getUniqueId().equals(player.getUniqueId()) || !wolfLeveling) return false;
        int levelBefore = getLevel(wolf);
        PowerProfile profile = ProfileCache.getOrCache(player, PowerProfile.class);
        int level;
        if (entityLevelScaling.containsKey(EntityType.WOLF))
            level = Math.max(0, (int) Utils.eval(entityLevelScaling.get(EntityType.WOLF).replace("%level%", String.valueOf(profile.getLevel()))));
        else if (defaultWolfLevelScaling != null) level = Math.max(0, (int) Utils.eval(defaultWolfLevelScaling.replace("%level%", String.valueOf(profile.getLevel()))));
        else return false;
        EntityUpdateLevelEvent event = new EntityUpdateLevelEvent(wolf, level);
        ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;
        level = event.getLevel();
        if (level == levelBefore) return false;
        if (level >= 0) wolf.getPersistentDataContainer().set(ENTITY_LEVEL, PersistentDataType.INTEGER, level);
        else wolf.getPersistentDataContainer().remove(ENTITY_LEVEL);
        EntityAttributeStats.updateStats(wolf);
        return true;
    }

    /**
     * Returns the average power level of surrounding players given a location.
     * @param l the location to check the average power level of. Takes the average of all players in a 128 block radius.
     * @return The average power level of surrounding players.
     */
    public static double getAreaDifficultyLevel(Location l, Player cacheFor){
        if (!enabled || l.getWorld() == null) return 0;
        List<Player> players = EntityUtils.getNearbyPlayers(l, 128, false);
        int combinedLevel = 0;
        int lowest = -1;
        int highest = -1;
        for (Player p : players){
            if (p.getGameMode() != GameMode.SURVIVAL) continue;
            PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
            combinedLevel += profile.getLevel();
            if (lowest < 0 || lowest > profile.getLevel()) lowest = profile.getLevel();
            if (highest < 0 || highest < profile.getLevel()) highest = profile.getLevel();
        }
        double level = regionalPlayerLevel == null ?
                (double) combinedLevel / players.size() :
                Utils.eval(regionalPlayerLevel.replace("%combined_player_level%", String.valueOf(combinedLevel))
                        .replace("%nearby_player_count%", String.valueOf(players.size()))
                        .replace("%min_player_level%", String.valueOf(lowest))
                        .replace("%max_player_level%", String.valueOf(highest)));
        if (cacheFor != null) regionalMonsterLevelCache.put(cacheFor.getUniqueId(), level);
        return level;
    }

    public static double getCachedDifficultyLevel(Player from){
        if (Timer.isCooldownPassed(from.getUniqueId(), "delay_regional_difficulty_cache_update")){
            ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> getAreaDifficultyLevel(from.getLocation(), from));
            Timer.setCooldown(from.getUniqueId(), 10000, "delay_regional_difficulty_cache_update");
        }
        return regionalMonsterLevelCache.getOrDefault(from.getUniqueId(), 0D);
    }

    private static String parseRand(String expression){
        String[] rands = StringUtils.substringsBetween(expression, "rand(", ")");
        if (rands == null) return expression;
        for (String rand : rands){
            String[] args = rand.split(",");
            if (args.length == 1) expression = expression.replaceFirst(Pattern.quote("rand(" + rand + ")"), String.valueOf(Utils.getRandom().nextInt(Integer.parseInt(args[0]))));
            else if (args.length == 2){
                int lower = Integer.parseInt(args[0].trim());
                int upper = Integer.parseInt(args[1].trim());
                expression = expression.replaceFirst(Pattern.quote("rand(" + rand + ")"), String.valueOf(Utils.getRandom().nextInt((upper - lower) + 1) + lower));
            }
        }
        return expression;
    }
}
