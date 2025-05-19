package me.athlaeos.valhallammo.skills.skills;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.event.PlayerSkillLevelUpEvent;
import me.athlaeos.valhallammo.event.ValhallaUpdatedStatsEvent;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.placeholder.PlaceholderRegistry;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.skills.perk_rewards.MultiplicativeReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardRegistry;
import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpense;
import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpenseRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.skills.perkunlockconditions.UnlockCondition;
import me.athlaeos.valhallammo.skills.perkunlockconditions.UnlockConditionRegistry;
import me.athlaeos.valhallammo.skills.skills.implementations.PowerSkill;
import me.athlaeos.valhallammo.utility.BossBarUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@SuppressWarnings("unused")
public abstract class Skill {
    protected final String type;
    protected String requiredPermission;
    protected String displayName;
    protected String description;
    protected ItemStack icon;
    protected String expCurve;
    protected int maxLevel;
    protected List<PerkReward> startingPerks = new ArrayList<>();
    protected List<PerkReward> levelingPerks = new ArrayList<>();
    protected List<String> levelingMessages = new ArrayList<>();
    protected List<String> levelingCommands = new ArrayList<>();
    protected List<String> levelingUndoCommands = new ArrayList<>(); // perk rewards are capable of being inverted, but since commands are just strings they can't.
    // here the counteract commands are given to undo the changes made by leveling commands
    protected Map<Integer, List<PerkReward>> specialLevelingPerks = new HashMap<>();
    protected Map<Integer, List<String>> specialLevelingMessages = new HashMap<>();
    protected Map<Integer, List<String>> specialLevelingCommands = new HashMap<>();
    protected Map<Integer, List<String>> specialLevelingUndoCommands = new HashMap<>();
    protected List<Perk> perks = new ArrayList<>();
    protected int centerX;
    protected int centerY;
    protected BarColor expBarColor;
    protected BarStyle expBarStyle;
    protected String expBarTitle;
    protected boolean isNavigable;
    protected double experienceLimit = -1;
    protected String experienceLimitMessage = null;

    public abstract void loadConfiguration();

    /**
     * If true, the skill is intended to be interacted with and progressed through. If not, the skill is intended
     * to be hidden from sight and more or less just meant to be used to store extra data and add behavior to ValhallaMMO
     *
     * @return whether the skill is active or passive
     */
    public abstract boolean isLevelableSkill();

    public abstract Class<? extends Profile> getProfileType();

    /**
     * The order of priority in which this skill should display in the /skill menu
     *
     * @return the priority on which to sort the skills
     */
    public abstract int getSkillTreeMenuOrderPriority();

    /**
     * If true, the experience gained from this skill can be amplified with experience-scaling stats.
     * If false, the experience gained will always be the same regardless of stats.
     *
     * @return true if the experience should scale with stats, false otherwise.
     */
    public boolean isExperienceScaling() {
        return true;
    }

    /**
     * @return The starting X coordinate when the menu is first opened. The top-left corner is X0, Y0.
     * Going right from there is positive X, going down is positive Y.
     * 0123456X
     * 0_______
     * 1_______
     * 2_______
     * 3_O_____  <-- X1, Y3
     * 4_______
     * 5_______
     * Y
     */
    public int getCenterX() {
        return centerX;
    }

    /**
     * @return The starting Y coordinate when the menu is first opened. The top-left corner is X0, Y0.
     * Going right from there is positive X, going down is positive Y.
     * 0123456X
     * 0_______
     * 1_______
     * 2_______
     * 3_O_____  <-- X1, Y3
     * 4_______
     * 5_______
     * Y
     */
    public int getCenterY() {
        return centerY;
    }

    public BarColor getExpBarColor() {
        return expBarColor;
    }

    public BarStyle getExpBarStyle() {
        return expBarStyle;
    }

    public String getExpBarTitle() {
        return expBarTitle;
    }

    public boolean isNavigable() {
        return isNavigable;
    }

    public String getExpStatus() {
        return TranslationManager.getTranslation("status_experience_gained");
    }

    public Skill(String type) {
        this.type = type;
    }

    private int[] parseCoordinates(String coordinates) {
        if (coordinates == null) return null;
        int[] coords = new int[]{0, 0};
        try {
            String[] indivCoords = coordinates.split(",");
            if (indivCoords.length != 2) throw new IllegalArgumentException();
            coords[0] = Integer.parseInt(indivCoords[0]);
            coords[1] = Integer.parseInt(indivCoords[1]);
        } catch (IllegalArgumentException e) {
            ValhallaMMO.logWarning("invalid coordinates given for " + getClass().getSimpleName() + ", defaulting to 0,0. Coords are to be given in the format \"x,y\" where X and Y are whole numbers");
            coords[0] = 0;
        }
        return coords;
    }

    /**
     * @param baseSkillConfig   the intended base skill config, containing simple properties
     * @param progressionConfig the intended skill progression config, containing details about the skill's progression
     */
    public void loadCommonConfig(YamlConfiguration baseSkillConfig, YamlConfiguration progressionConfig) {
        this.displayName = Utils.chat(TranslationManager.translatePlaceholders(baseSkillConfig.getString("display_name")));
        this.description = Utils.chat(TranslationManager.translatePlaceholders(baseSkillConfig.getString("description")));
        this.icon = ItemUtils.getIconFromConfig(baseSkillConfig, "icon", getClass().getSimpleName() + " skill config", new ItemStack(Material.BARRIER));
        this.requiredPermission = baseSkillConfig.getString("permission");
        this.isNavigable = progressionConfig.getBoolean("navigable", true);

        int modelData = baseSkillConfig.getInt("icon_data", -1);
        if (modelData >= 0) {
            ItemMeta iconMeta = ItemUtils.getItemMeta(icon);
            if (iconMeta != null) {
                iconMeta.setCustomModelData(modelData);
                ItemUtils.setMetaNoClone(this.icon, iconMeta);
            }
        }

        this.expCurve = progressionConfig.getString("experience.exp_level_curve");
        this.maxLevel = progressionConfig.getInt("experience.max_level");
        this.levelingMessages = TranslationManager.translateListPlaceholders(progressionConfig.getStringList("messages"));
        this.levelingCommands = progressionConfig.getStringList("commands");
        this.levelingUndoCommands = progressionConfig.getStringList("undo_commands");
        this.experienceLimit = progressionConfig.getDouble("experience.daily_limit", -1);
        this.experienceLimitMessage = progressionConfig.getString("experience.daily_limit_warning", null);

        int[] coords = parseCoordinates(progressionConfig.getString("starting_coordinates", "0,0"));
        this.centerX = coords[0];
        this.centerY = coords[1];

        ConfigurationSection startingPerksSection = progressionConfig.getConfigurationSection("starting_perks");
        if (startingPerksSection != null) {
            for (String startPerk : startingPerksSection.getKeys(false)) {
                Object arg = progressionConfig.get("starting_perks." + startPerk);
                if (arg != null) {
                    PerkReward reward = PerkRewardRegistry.createReward(startPerk, arg);
                    if (reward == null) continue;
                    startingPerks.add(reward);
                }
            }
        }

        ConfigurationSection levelingPerksSection = progressionConfig.getConfigurationSection("leveling_perks");
        if (levelingPerksSection != null) {
            for (String levelPerk : levelingPerksSection.getKeys(false)) {
                Object arg = progressionConfig.get("leveling_perks." + levelPerk);
                if (arg != null) {
                    PerkReward reward = PerkRewardRegistry.createReward(levelPerk, arg);
                    if (reward == null) continue;
                    levelingPerks.add(reward);
                }
            }
        }

        ConfigurationSection perksSection = progressionConfig.getConfigurationSection("perks");
        if (perksSection != null) {
            for (String perkName : perksSection.getKeys(false)) {
                String displayName = TranslationManager.translatePlaceholders(progressionConfig.getString("perks." + perkName + ".name"));
                String description = TranslationManager.translatePlaceholders(progressionConfig.getString("perks." + perkName + ".description"));
                ItemStack icon = ItemUtils.getIconFromConfig(progressionConfig, "perks." + perkName + ".icon", getClass().getSimpleName() + " progression config", new ItemStack(Material.PAPER));

                int[] c = parseCoordinates(progressionConfig.getString("perks." + perkName + ".coords", "0,0"));
                int perkX = c[0];
                int perkY = c[1];

                Collection<ResourceExpense> expenses = new HashSet<>();
                for (String expenseKey : ResourceExpenseRegistry.getExpenses().keySet()) {
                    ResourceExpense expense = ResourceExpenseRegistry.createExpenseInstance(expenseKey);
                    Object cost = progressionConfig.get("perks." + perkName + "." + expenseKey);
                    if (expense == null || cost == null) continue;
                    expense.initExpense(cost);
                    expenses.add(expense);
                }

                Collection<UnlockCondition> conditions = new HashSet<>();
                for (String conditionKey : UnlockConditionRegistry.getValuePlaceholders()) {
                    UnlockCondition condition = UnlockConditionRegistry.createConditionInstance(conditionKey);
                    Object cost = progressionConfig.get("perks." + perkName + "." + conditionKey);
                    if (condition == null || cost == null) continue;
                    condition.initCondition(cost);
                    conditions.add(condition);
                }

                boolean hidden = progressionConfig.getBoolean("perks." + perkName + ".hidden");
                int required_level = progressionConfig.getInt("perks." + perkName + ".required_lv");

                List<PerkReward> perkRewards = new ArrayList<>();
                ConfigurationSection perkRewardSection = progressionConfig.getConfigurationSection("perks." + perkName + ".perk_rewards");
                if (perkRewardSection != null) {
                    for (String rewardString : perkRewardSection.getKeys(false)) {
                        Object arg = progressionConfig.get("perks." + perkName + ".perk_rewards." + rewardString);
                        if (arg != null) {
                            PerkReward reward = PerkRewardRegistry.createReward(rewardString, arg);
                            if (reward == null) continue;
                            perkRewards.add(reward);
                        }
                    }
                }

                List<String> perkMessages = TranslationManager.translateListPlaceholders(progressionConfig.getStringList("perks." + perkName + ".messages"));
                List<String> commands = progressionConfig.getStringList("perks." + perkName + ".commands");
                List<String> undoCommands = progressionConfig.getStringList("perks." + perkName + ".undo_commands");

                int customModelDataUnlockable = progressionConfig.getInt("perks." + perkName + ".custom_model_data_unlockable", -1);
                int customModelDataUnlocked = progressionConfig.getInt("perks." + perkName + ".custom_model_data_unlocked", -1);
                int customModelDataVisible = progressionConfig.getInt("perks." + perkName + ".custom_model_data_visible", -1);

                Perk newPerk = new Perk(perkName, displayName, description, icon,
                        this, perkX, perkY, hidden, expenses, conditions, required_level, requiredPermission, perkRewards,
                        perkMessages, commands, undoCommands);
                if (customModelDataUnlockable > 0) newPerk.setCustomModelDataUnlockable(customModelDataUnlockable);
                if (customModelDataUnlocked > 0) newPerk.setCustomModelDataUnlocked(customModelDataUnlocked);
                if (customModelDataVisible > 0) newPerk.setCustomModelDataVisible(customModelDataVisible);

                Collection<PerkConnectionIcon> connectionLine = new HashSet<>();
                ConfigurationSection connectionSection = progressionConfig.getConfigurationSection("perks." + perkName + ".connection_line");
                if (connectionSection != null) {
                    // Formatted like so:
                    // skill:
                    //   connection_line:
                    //     '1':
                    //       position: '1,1'
                    //       locked: BLACK_STAINED_GLASS_PANE:9999999
                    //       unlockable: YELLOW_STAINED_GLASS_PANE:9999999
                    //       unlocked: GREEN_STAINED_GLASS_PANE:9999999
                    for (String i : connectionSection.getKeys(false)) {
                        int[] position = parseCoordinates(progressionConfig.getString("perks." + perkName + ".connection_line." + i + ".position"));
                        if (position == null) {
                            ValhallaMMO.logWarning("Invalid perk connection piece at " + i + ", no position found.");
                            continue;
                        }
                        String[] lockedParts = progressionConfig.getString("perks." + perkName + ".connection_line." + i + ".locked", "").split(":");
                        Material lockedMaterial = Material.valueOf(lockedParts[0]);
                        int lockedData = lockedParts.length > 1 ? Integer.parseInt(lockedParts[1]) : -1;
                        String[] unlockedParts = progressionConfig.getString("perks." + perkName + ".connection_line." + i + ".unlocked", "").split(":");
                        Material unlockedMaterial = Material.valueOf(unlockedParts[0]);
                        int unlockedData = unlockedParts.length > 1 ? Integer.parseInt(unlockedParts[1]) : -1;
                        String[] unlockableParts = progressionConfig.getString("perks." + perkName + ".connection_line." + i + ".unlockable", "").split(":");
                        Material unlockableMaterial = Material.valueOf(unlockableParts[0]);
                        int unlockableData = unlockableParts.length > 1 ? Integer.parseInt(unlockableParts[1]) : -1;
                        connectionLine.add(new PerkConnectionIcon(this, newPerk, position[0], position[1], lockedMaterial, unlockableMaterial, unlockedMaterial, lockedData, unlockableData, unlockedData));
                    }
                    newPerk.setConnectionLine(connectionLine);
                }
                perks.add(newPerk);
            }
        }
        perks.sort(Comparator.comparingInt(Perk::getLevelRequirement));

        ConfigurationSection specialSection = progressionConfig.getConfigurationSection("special_perks");
        if (specialSection != null) {
            for (String stringLevel : specialSection.getKeys(false)) {
                int level;
                try {
                    level = Integer.parseInt(stringLevel);
                } catch (IllegalArgumentException ignored) {
                    ValhallaMMO.logWarning("Invalid special level given at special_perks." + stringLevel + ". Cancelled this special level, it should be a whole number!");
                    continue;
                }

                specialLevelingCommands.put(level, progressionConfig.getStringList("special_perks." + stringLevel + ".commands"));
                specialLevelingUndoCommands.put(level, progressionConfig.getStringList("special_perks." + stringLevel + ".undo_commands"));
                specialLevelingMessages.put(level, TranslationManager.translateListPlaceholders(progressionConfig.getStringList("special_perks." + stringLevel + ".messages")));
                List<PerkReward> specialPerkRewards = new ArrayList<>();

                ConfigurationSection perkSection = progressionConfig.getConfigurationSection("special_perks." + stringLevel + ".perk_rewards");
                if (perkSection != null) {
                    for (String perkName : perkSection.getKeys(false)) {
                        Object arg = progressionConfig.get("special_perks." + stringLevel + ".perk_rewards." + perkName);
                        if (arg != null) {
                            PerkReward reward = PerkRewardRegistry.createReward(perkName, arg);
                            if (reward == null) continue;
                            specialPerkRewards.add(reward);
                        }
                    }
                }

                specialLevelingPerks.put(level, specialPerkRewards);
            }
        }

        this.expBarTitle = TranslationManager.translatePlaceholders(baseSkillConfig.getString("levelbar_title", ""));
        try {
            this.expBarColor = BarColor.valueOf(baseSkillConfig.getString("levelbar_color", "YELLOW"));
        } catch (IllegalArgumentException ignored) {
            this.expBarColor = BarColor.WHITE;
        }

        try {
            this.expBarStyle = BarStyle.valueOf(baseSkillConfig.getString("levelbar_style", "SEGMENTED_6"));
        } catch (IllegalArgumentException ignored) {
            this.expBarStyle = BarStyle.SEGMENTED_6;
        }
    }

    /**
     * Returns the double amount of experience needed to progress to this level, rounded to 4 decimals
     *
     * @param level the integer level to get how much EXP it takes to reach it
     * @return the double amount of experience needed to progress to reach this level. If the level exceeds the
     * max level, it returns -1.
     */
    public double expForLevel(int level) {
        if (isLevelableSkill() && level <= maxLevel) {
            return Utils.round6Decimals(Utils.eval(expCurve.replace("%level%", String.valueOf(level))));
        } else {
            return -1;
        }
    }

    /**
     * Levels a player up if the levelup conditions are met, if the player has more or equal exp than is required to level
     * up to the next level.
     *
     * @param p the player to level up depending on if the conditions are met.
     */
    public void updateLevelUpConditions(Player p, boolean silent) {
        Profile profile = ProfileRegistry.getPersistentProfile(p, getProfileType());
        if (profile == null) return;
        int currentLevel = profile.getLevel();
        double EXP = profile.getEXP();
        int nextLevel = currentLevel;
        if (EXP < 0) {
            // level down
            nextLevel -= 1; // since the player has negative experience, they should level down at least once.
            // loop from current level to 0 because that's the max amount of iterations to check for level downs
            for (int i = nextLevel; i >= 0; i--) {
                double expForLevel = expForLevel(i);
                if (-EXP >= expForLevel) {
                    // player has enough negative experience to level down once
                    nextLevel--;
                    EXP += expForLevel;
                } else {
                    EXP += expForLevel;
                    break; // player does not have enough negative experience to level down
                }
            }
        } else {
            if (profile.getLevel() >= maxLevel || profile.getLevel() >= profile.getMaxAllowedLevel()) {
                if (!ProfileCache.getOrCache(p, PowerProfile.class).hideExperienceGain()) {
                    showBossBar(p, profile, 0);
                }
                return;
            }
            // level up
            // loop from current level +1 to max level because that's the max amount of iterations to check for level ups
            for (int i = nextLevel + 1; i <= maxLevel; i++) {
                double expForLevel = expForLevel(i);
                if (EXP >= expForLevel) {
                    // player has enough experience to level up once
                    nextLevel++;
                    EXP -= expForLevel;
                    if (nextLevel >= profile.getMaxAllowedLevel()) EXP = 0;
                } else break; // player does not have enough experience to level up further
            }
        }
        // if the level doesn't change, nothing further needs to happen
        if (nextLevel == currentLevel) return;

        profile.setEXP(EXP);
        profile.setLevel(Math.max(0, nextLevel));

        // if a skill is at level 0, exp and total exp should not go below 0
        if (profile.getLevel() <= 0 && profile.getEXP() < 0) profile.setEXP(0);
        if (profile.getLevel() <= 0 && profile.getTotalEXP() < 0) profile.setTotalEXP(0);

        ProfileRegistry.setPersistentProfile(p, profile, getProfileType());
        changePlayerLevel(p, currentLevel, Math.max(0, nextLevel), silent);

        if (!ProfileCache.getOrCache(p, PowerProfile.class).hideExperienceGain()) {
            showBossBar(p, profile, 0);
        }
    }

    public int getLevelFromEXP(double exp) {
        int level = 0;
        for (int i = 1; i <= getMaxLevel(); i++) {
            double neededExp = expForLevel(i);
            if (exp >= neededExp) {
                exp -= neededExp;
                level++;
            } else break;
        }
        return level;
    }

    /**
     * Adds an amount of EXP to the player's profile, and checks if the player should level up
     * If this method is being called on an inactive skill, no exp is added
     *
     * @param p      the player to add EXP to
     * @param amount the amount of EXP to add
     */
    public void addEXP(Player p, double amount, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        // non-levelable skills should not gain exp
        if (!isLevelableSkill() || (requiredPermission != null && !p.hasPermission(requiredPermission)) || hasReachedLimit(p)) return;
        // creative mode players should not gain skill-acquired exp
        if (!(this instanceof PowerSkill) && p.getGameMode() == GameMode.CREATIVE && reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION) return;
        // only experience-scaling skills should scale with exp multipliers. By default, this only excludes PowerSkill
        if (isExperienceScaling() && (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION ||
                reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.EXP_SHARE)) amount *= (1 + AccumulativeStatManager.getCachedStats("GLOBAL_EXP_GAIN", p, 10000, true));

        PlayerSkillExperienceGainEvent event = new PlayerSkillExperienceGainEvent(p, amount, this, reason);
        if (Bukkit.isPrimaryThread()) ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            if (event.getAmount() == 0) return;

            Profile profile = ProfileRegistry.getPersistentProfile(p, event.getLeveledSkill().getProfileType());
            if (profile.getLevel() >= profile.getMaxAllowedLevel() &&
                    (reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION ||
                            reason == PlayerSkillExperienceGainEvent.ExperienceGainReason.EXP_SHARE)) return; // player has already reached max allowed level, do not proceed
            profile.setEXP(profile.getEXP() + event.getAmount());
            profile.setTotalEXP(profile.getTotalEXP() + event.getAmount());
            incrementExperienceLimit(p, event.getAmount());

            if (!silent) {
                double statusAmount = accumulateEXP(p, event.getAmount(), event.getLeveledSkill());
                if (!(this instanceof PowerSkill) && !StringUtils.isEmpty(getExpStatus())) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Utils.chat(getExpStatus()
                            .replace("%skill%", event.getLeveledSkill().displayName)
                            .replace("%exp%", ((statusAmount >= 0) ? "+" : "") + String.format("%,.2f", statusAmount)))));
                }
                if (!ProfileCache.getOrCache(p, PowerProfile.class).hideExperienceGain()) {
                    showBossBar(p, profile, statusAmount);
                }
            }

            ProfileRegistry.setPersistentProfile(p, profile, profile.getClass());
            // level conditions don't need to be checked if the player's current exp isn't enough to level up, or low enough to level down
            double nextLevelEXP = expForLevel(profile.getLevel() + 1);
            if ((nextLevelEXP > 0 && profile.getEXP() >= nextLevelEXP) || profile.getEXP() < 0) {
                updateLevelUpConditions(p, silent);
                AccumulativeStatManager.updateStats(p);
            }
        }
    }

    private final Map<UUID, Double> experienceLimitMap = new HashMap<>();
    private final Map<UUID, Long> experienceLimitTimeMap = new HashMap<>();
    private final Collection<UUID> sentExperienceLimitMessage = new HashSet<>();
    public boolean hasReachedLimit(Player p){
        if (experienceLimit < 0 || this instanceof PowerSkill) return false;
        return experienceLimitMap.getOrDefault(p.getUniqueId(), 0D) >= experienceLimit;
    }

    public void incrementExperienceLimit(Player p, double exp){
        if (experienceLimit < 0 || this instanceof PowerSkill) return;
        // If the player time was not yet recorded, or it's been a day, reset the time and counter
        if (!experienceLimitTimeMap.containsKey(p.getUniqueId()) ||
                experienceLimitTimeMap.get(p.getUniqueId()) + (24 * 60 * 60 * 1000) < System.currentTimeMillis()) {
            experienceLimitTimeMap.put(p.getUniqueId(), System.currentTimeMillis());
            experienceLimitMap.remove(p.getUniqueId());
            sentExperienceLimitMessage.remove(p.getUniqueId());
        }
        double existingValue = experienceLimitMap.getOrDefault(p.getUniqueId(), 0D);
        existingValue += exp;
        experienceLimitMap.put(p.getUniqueId(), existingValue);
        if (hasReachedLimit(p) && !sentExperienceLimitMessage.contains(p.getUniqueId()))
            Utils.sendMessage(p, TranslationManager.translatePlaceholders(experienceLimitMessage));
    }

    public void addLevels(Player player, int levels, boolean silent, PlayerSkillExperienceGainEvent.ExperienceGainReason reason) {
        if (levels == 0) return;
        Profile p = ProfileRegistry.getPersistentProfile(player, getProfileType());
        double expToGive = 0;
        if (levels < 0) {
            for (int level = p.getLevel() + levels; level < p.getLevel(); level++) {
                expToGive -= expForLevel(level);
            }
        } else {
            for (int level = p.getLevel() + 1; level <= p.getLevel() + levels; level++) {
                expToGive += expForLevel(level);
            }
        }
        addEXP(player, Math.ceil(expToGive), silent, reason);
//        // make sure the player ends up at 0 exp for the level
//        if (p.getLevel() < levels) addEXP(player, expForLevel(p.getLevel() + 1) - p.getEXP(), silent, reason);
//        else if (p.getLevel() == levels) addEXP(player, -p.getEXP(), silent, reason);
//        else addEXP(player, expForLevel(p.getLevel()) + p.getEXP(), silent, reason);
    }

    private final Map<UUID, EXPStatusStruct> expTracker = new HashMap<>();

    /*
    This method is solely responsible for the prevention of possibly misleading action bar messages when obtaining EXP.
    If you were to get an amount of exp several times quickly in succession, as typically happens with brewing for example,
    this method would make it, so you would see the combined total of exp rather than the last instance of exp you were given.
    This exp accumulates on the actionbar as long as:
    - it's been less than 3 seconds since the last EXP instance
    - the types of EXP match to what's given
    - the amount is less than 100,000,000

    This method has no influence on the experience obtained, it is purely a visual effect.
     */
    private double accumulateEXP(Player p, double amount, Skill type) {
        EXPStatusStruct struct = expTracker.getOrDefault(p.getUniqueId(), new EXPStatusStruct(type, 0, System.currentTimeMillis()));
        if (!struct.getType().equals(type) || struct.getTime_since_last() + 3000L <= System.currentTimeMillis() || struct.getExp() > 100000000) {
            struct = new EXPStatusStruct(type, 0, System.currentTimeMillis());
        }
        struct.setExp(struct.getExp() + amount);
        struct.setTime_since_last(System.currentTimeMillis());
        struct.setType(type);

        expTracker.put(p.getUniqueId(), struct);
        return struct.getExp();
    }

    private static class EXPStatusStruct {
        private Skill type;
        private Long time_since_last;
        private double exp;

        public EXPStatusStruct(Skill type, double exp, Long time_since_last) {
            this.type = type;
            this.exp = exp;
            this.time_since_last = time_since_last;
        }

        public double getExp() {
            return exp;
        }

        public Long getTime_since_last() {
            return time_since_last;
        }

        public Skill getType() {
            return type;
        }

        public void setExp(double exp) {
            this.exp = exp;
        }

        public void setTime_since_last(Long time_since_last) {
            this.time_since_last = time_since_last;
        }

        public void setType(Skill type) {
            this.type = type;
        }
    }

    private void showBossBar(Player p, Profile profile, double accumulatedEXP) {
        if (!StringUtils.isEmpty(expBarTitle)) {
            double expForNextLevel = expForLevel(profile.getLevel() + 1);

            String bossBarTitle = Utils.chat(expBarTitle
                    .replace("%skill%", this.displayName)
                    .replace("%exp%", ((accumulatedEXP >= 0) ? "+" : "") + String.format("%,.2f", accumulatedEXP))
                    .replace("%exp_current%", String.format("%.2f", profile.getEXP()))
                    .replace("%lv_current%", String.valueOf(profile.getLevel()))
                    .replace("%lv_next%", (expForNextLevel == -1) ? TranslationManager.getTranslation("max_level") : String.valueOf(profile.getLevel() + 1))
                    .replace("%exp_next%", (expForNextLevel == -1) ? TranslationManager.getTranslation("max_level") : String.format("%.2f", expForNextLevel)));
            float fraction;
            if (expForNextLevel <= 0) {
                fraction = 1;
            } else {
                fraction = (float) Utils.round6Decimals(profile.getEXP() / expForNextLevel);
            }
            BossBarUtils.showBossBarToPlayer(p, bossBarTitle, Math.min(fraction, 1F), 50, this.type, expBarColor, expBarStyle);
        }
    }

    /**
     * Levels a player up once, and applies any rewards within the skill to the player
     *
     * @param p the player to level up
     */
    private void changePlayerLevel(Player p, int from, int to, boolean silent) {
        if (!isLevelableSkill()) return;
        if (to < from) {
            // level down
            for (int i = from - 1; i >= to; i--) {
                if (specialLevelingUndoCommands.containsKey(i)) {
                    for (String command : specialLevelingUndoCommands.get(i)) {
                        ValhallaMMO.getInstance().getServer().dispatchCommand(
                                ValhallaMMO.getInstance().getServer().getConsoleSender(),
                                command.replace("%player%", p.getName())
                        );
                    }
                }
            }

            for (String command : levelingUndoCommands) {
                ValhallaMMO.getInstance().getServer().dispatchCommand(
                        ValhallaMMO.getInstance().getServer().getConsoleSender(),
                        command.replace("%player%", p.getName())
                );
            }

            ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
                for (int i = from; i > to; i--) {
                    for (PerkReward reward : levelingPerks) {
                        if (reward instanceof MultiplicativeReward || reward.isPersistent()) continue;
                        reward.remove(p);
                    }

                    if (specialLevelingPerks.containsKey(i)) {
                        for (PerkReward reward : specialLevelingPerks.get(i)) {
                            if (reward.isPersistent()) continue;
                            reward.remove(p);
                        }
                    }
                }

                for (PerkReward reward : levelingPerks) {
                    if (reward instanceof MultiplicativeReward r) {
                        if (reward.isPersistent()) continue;
                        r.remove(p, from - to);
                    } else reward.remove(p);
                }

                if (arePerksForgettable()) {
                    // forgetting perks if the player no longer meets perk requirements
                    List<Perk> sortedPerks = new ArrayList<>(perks);
                    sortedPerks.sort(Comparator.comparingInt(Perk::getLevelRequirement));
                    Collections.reverse(sortedPerks);
                    for (Perk perk : sortedPerks) {
                        if (perk.shouldLock(p)) {
                            perk.remove(p);
                            p.sendMessage(Utils.chat(TranslationManager.getTranslation("perk_forget").replace("%perk%", perk.getDisplayName())));
                        }
                    }
                }
            });
        } else {
            // level up
            for (int i = from + 1; i <= to; i++) {
                // special messages, commands, and perk rewards
                if (!silent) {
                    if (specialLevelingMessages.containsKey(i)) {
                        for (String message : specialLevelingMessages.get(i)) {
                            p.sendMessage(PlaceholderRegistry.parsePapi(PlaceholderRegistry.parse(Utils.chat(message.replace("%player%", p.getName())), p), p));
                        }
                    }
                }
                if (specialLevelingCommands.containsKey(i)) {
                    for (String command : specialLevelingCommands.get(i)) {
                        ValhallaMMO.getInstance().getServer().dispatchCommand(
                                ValhallaMMO.getInstance().getServer().getConsoleSender(),
                                command.replace("%player%", p.getName())
                        );
                    }
                }

                for (String command : levelingCommands) {
                    ValhallaMMO.getInstance().getServer().dispatchCommand(
                            ValhallaMMO.getInstance().getServer().getConsoleSender(),
                            command.replace("%player%", p.getName())
                    );
                }
            }

            if (!silent) {
                Map<Integer, List<String>> messages = new HashMap<>();
                int lastDelay = 0;
                for (String message : levelingMessages){
                    String[] matches = message.startsWith("DELAY(") ? org.apache.commons.lang.StringUtils.substringsBetween(message, "DELAY(", ")") : new String[0];
                    int delay = Math.max(0, matches == null || matches.length == 0 ? 0 : Catch.catchOrElse(() -> Integer.parseInt(matches[0]), 0));

                    lastDelay += delay;
                    List<String> thisDelayMessages = messages.getOrDefault(lastDelay, new ArrayList<>());
                    thisDelayMessages.add(message);
                    messages.put(lastDelay, thisDelayMessages);
                }
                if (lastDelay == 0){
                    levelingMessages.forEach(message ->
                            Utils.sendMessage(p, TranslationManager.translatePlaceholders(message).replace("%player%", p.getName()).replace("%level%", String.valueOf(to)))
                    );
                } else {
                    int finalDuration = lastDelay;
                    new BukkitRunnable(){
                        int timer = 0;
                        @Override
                        public void run() {
                            if (timer >= finalDuration || !p.isOnline()) {
                                cancel();
                                return;
                            }

                            messages.getOrDefault(timer, new ArrayList<>())
                                    .forEach(message ->
                                            Utils.sendMessage(p, TranslationManager.translatePlaceholders(message).replace("%player%", p.getName()).replace("%level%", String.valueOf(to)))
                                    );
                            timer++;
                        }
                    }.runTaskTimer(ValhallaMMO.getInstance(), 0L, 1L);
                }
            }

            ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
                for (int i = from + 1; i <= to; i++) {
                    for (PerkReward reward : levelingPerks) {
                        if (reward instanceof MultiplicativeReward) continue;
                        reward.apply(p);
                    }

                    if (specialLevelingPerks.containsKey(i)) {
                        for (PerkReward reward : specialLevelingPerks.get(i)) {
                            reward.apply(p);
                        }
                    }
                }

                for (PerkReward reward : levelingPerks) {
                    if (reward instanceof MultiplicativeReward r) {
                        r.apply(p, to - from);
                    } else reward.apply(p);
                }
            });
        }
        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () ->
                ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(new PlayerSkillLevelUpEvent(p, this, from, to))
        );
    }

    public void updateSkillStats(Player p, boolean runPersistentStartingPerks) {
        Profile persistentProfile = ProfileRegistry.getPersistentProfile(p, getProfileType());
        int level = persistentProfile.getLevel();
        PowerProfile powerProfile = ProfileRegistry.getPersistentProfile(p, PowerProfile.class);

        for (PerkReward r : startingPerks) {
            if (!runPersistentStartingPerks && r.isPersistent()) continue;
            r.apply(p);
        }
        if (level > 0) {
            for (int i = 1; i <= level; i++) {
                for (PerkReward reward : levelingPerks) {
                    if (reward instanceof MultiplicativeReward || reward.isPersistent())
                        continue; // persistent rewards should not be executed repeatedly
                    reward.apply(p);
                }

                // special perk rewards
                if (specialLevelingPerks.containsKey(i)) {
                    for (PerkReward reward : specialLevelingPerks.get(i)) {
                        if (reward.isPersistent()) continue;
                        reward.apply(p);
                    }
                }
            }

            for (PerkReward reward : levelingPerks) {
                if (reward instanceof MultiplicativeReward r && !reward.isPersistent()) {
                    r.apply(p, level);
                }
            }
        }

        Collection<String> unlockedPerks = powerProfile.getUnlockedPerks();
        unlockedPerks.addAll(powerProfile.getPermanentlyUnlockedPerks());
        Collection<String> fakeUnlockedPerks = powerProfile.getFakeUnlockedPerks();
        Collection<String> permanentlyLockedPerks = powerProfile.getPermanentlyLockedPerks();
        for (Perk perk : perks) {
            if (fakeUnlockedPerks.contains(perk.getName()) || permanentlyLockedPerks.contains(perk.getName())) continue;
            if (unlockedPerks.contains(perk.getName())) { // do not trigger perks again if they've not been unlocked, or if they're fake unlocked or permanently unlocked
                for (PerkReward reward : perk.getRewards()) {
                    if (reward.isPersistent()) continue;
                    reward.apply(p);
                }
                if (!perk.getExpenses().isEmpty()){
                    ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
                        for (ResourceExpense expense : perk.getExpenses()) {
                            expense.purchase(p, false);
                        }
                    });
                }
            }
        }

        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () ->
                ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(new ValhallaUpdatedStatsEvent(p, getProfileType())));
    }

    private final boolean perksForgettable = ConfigManager.getConfig("config.yml").reload().get().getBoolean("forgettable_perks");

    /**
     * if true, players that level downwards will forget perks if they no longer meet their perk requirements (level & perk dependencies)
     *
     * @return if perks should be forgettable if leveling below its requirements
     */
    public boolean arePerksForgettable() {
        return perksForgettable;
    }

    public String getType() {
        return type;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public String getExpCurve() {
        return expCurve;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public List<PerkReward> getStartingPerks() {
        return startingPerks;
    }

    public List<PerkReward> getLevelingPerks() {
        return levelingPerks;
    }

    public List<String> getLevelingMessages() {
        return levelingMessages;
    }

    public List<String> getLevelingCommands() {
        return levelingCommands;
    }

    public List<String> getLevelingUndoCommands() {
        return levelingUndoCommands;
    }

    public Map<Integer, List<PerkReward>> getSpecialLevelingPerks() {
        return specialLevelingPerks;
    }

    public Map<Integer, List<String>> getSpecialLevelingMessages() {
        return specialLevelingMessages;
    }

    public Map<Integer, List<String>> getSpecialLevelingCommands() {
        return specialLevelingCommands;
    }

    public Map<Integer, List<String>> getSpecialLevelingUndoCommands() {
        return specialLevelingUndoCommands;
    }

    public List<Perk> getPerks() {
        return perks;
    }

    public boolean isPerksForgettable() {
        return perksForgettable;
    }

    public boolean hasPermissionAccess(Player p){
        return requiredPermission == null || p.hasPermission(requiredPermission);
    }
}
