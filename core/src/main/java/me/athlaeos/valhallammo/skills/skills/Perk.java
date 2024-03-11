package me.athlaeos.valhallammo.skills.skills;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpense;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.skills.perkunlockconditions.UnlockCondition;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Perk {
    private final String name;
    private final String displayName;
    private final String description;
    private final ItemStack icon;
    private int customModelDataUnlockable = -1;
    private int customModelDataUnlocked = -1;
    private int customModelDataVisible = -1;
    private final Skill skill;
    private final int x;
    private final int y;
    private final boolean hiddenUntilRequirementsMet;
    private final Collection<ResourceExpense> expenses;
    private final Collection<UnlockCondition> conditions;
    private final int levelRequirement;
    private final String permissionRequirement;
    private final List<PerkReward> rewards;
    private Collection<PerkConnectionIcon> connectionLine = new HashSet<>();
    private final List<String> messages;
    private final List<String> commands;
    private final List<String> undoCommands;

    public Perk(String name,
                String displayName,
                String description,
                ItemStack icon,
                Skill skill,
                int x,
                int y,
                boolean hidden,
                Collection<ResourceExpense> expenses,
                Collection<UnlockCondition> conditions,
                int levelRequirement,
                String permissionRequirement,
                List<PerkReward> rewards,
                List<String> messages,
                List<String> commands,
                List<String> undoCommands) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.skill = skill;
        this.x = x;
        this.y = y;
        this.hiddenUntilRequirementsMet = hidden;
        this.expenses = expenses;
        this.conditions = conditions;
        this.levelRequirement = levelRequirement;
        this.permissionRequirement = permissionRequirement;
        this.rewards = rewards;
        this.messages = messages;
        this.commands = commands;
        this.undoCommands = undoCommands;
    }

    public void setConnectionLine(Collection<PerkConnectionIcon> connectionLine) {
        this.connectionLine = connectionLine;
    }

    public Collection<PerkConnectionIcon> getConnectionLine() {
        return connectionLine;
    }

    public Collection<ResourceExpense> getExpenses() {
        return expenses;
    }

    public Collection<UnlockCondition> getConditions() {
        return conditions;
    }

    public String getPermissionRequirement() {
        return permissionRequirement;
    }

    public Skill getSkill() {
        return skill;
    }

    public List<String> getUndoCommands() {
        return undoCommands;
    }

    public boolean canUnlock(Player p){
        PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class); // ProfileRegistry.getPersistentProfile(p, PowerProfile.class);
        if (profile.getUnlockedPerks().contains(this.name) ||
                profile.getPermanentlyLockedPerks().contains(this.name) ||
                profile.getFakeUnlockedPerks().contains(this.name)) return false;

        return metResourceRequirements(p) && metLevelRequirement(p) &&
                metConditionRequirements(p, false);
    }

    public boolean shouldLock(Player p){
        PowerProfile profile = ProfileRegistry.getPersistentProfile(p, PowerProfile.class);
        if (!profile.getUnlockedPerks().contains(this.name)) return false;
        return !metLevelRequirement(p) || !metConditionRequirements(p, false);
    }

    public boolean metResourceRequirements(Player p){
        return expenses.isEmpty() || expenses.stream().allMatch(e -> e.canPurchase(p));
    }

    public boolean metConditionRequirements(Player p, boolean visibilityCheck){
        return conditions.isEmpty() || conditions.stream().allMatch(e -> e.canUnlock(p, visibilityCheck));
    }

    public boolean shouldBeVisible(Player p){
        if (!isHiddenUntilRequirementsMet() || canUnlock(p)) return true;
        PowerProfile profile = ProfileRegistry.getPersistentProfile(p, PowerProfile.class);
        if (profile.getUnlockedPerks().contains(this.name)) return true;
        if (profile.getPermanentlyLockedPerks().contains(this.name)) return false;
        if (profile.getFakeUnlockedPerks().contains(this.name)) return true;
        return metConditionRequirements(p, true);
    }

    /**
     * @return the custom model data on the icon when the perk is unlocked
     */
    public int getCustomModelDataUnlocked() {
        return customModelDataUnlocked;
    }

    /**
     * @return the custom model data on the icon when the perk is visible but not unlockable
     */
    public int getCustomModelDataVisible() {
        return customModelDataVisible;
    }

    public void setCustomModelDataUnlocked(int customModelDataUnlocked) {
        this.customModelDataUnlocked = customModelDataUnlocked;
    }

    public void setCustomModelDataVisible(int customModelDataVisible) {
        this.customModelDataVisible = customModelDataVisible;
    }

    /**
     * @return the custom model data on the icon when the perk can be unlocked
     */
    public int getCustomModelDataUnlockable() {
        return customModelDataUnlockable;
    }

    public void setCustomModelDataUnlockable(int customModelDataUnlockable) {
        this.customModelDataUnlockable = customModelDataUnlockable;
    }

    public boolean hasUnlocked(Player p){
        PowerProfile profile = ProfileRegistry.getPersistentProfile(p, PowerProfile.class);
        return profile.getUnlockedPerks().contains(this.name);
    }

    public boolean hasPermanentlyLocked(Player p){
        PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
        return profile.getPermanentlyLockedPerks().contains(this.name);
    }

    public boolean hasFakeUnlocked(Player p){
        PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
        return profile.getFakeUnlockedPerks().contains(this.name);
    }

    public boolean metLevelRequirement(Player p){
        Profile profile = ProfileRegistry.getPersistentProfile(p, skill.getProfileType());
        return profile.getLevel() >= levelRequirement;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isHiddenUntilRequirementsMet() {
        return hiddenUntilRequirementsMet;
    }

    public int getLevelRequirement() {
        return levelRequirement;
    }

    public List<PerkReward> getRewards() {
        return rewards;
    }

    public List<String> getMessages() {
        return messages;
    }

    public List<String> getCommands() {
        return commands;
    }

    public String getDescription() {
        return description;
    }

    public void execute(Player p){
        for (String message : messages){
            for (PerkReward reward : rewards) message = message.replace("{" + reward.getName() + "}", reward.rewardPlaceholder());
            p.sendMessage(Utils.chat(message));
        }
        for (String command : commands){
            ValhallaMMO.getInstance().getServer().dispatchCommand(ValhallaMMO.getInstance().getServer().getConsoleSender(), command.replace("%player%", p.getName()));
        }
        for (PerkReward reward : rewards){
            reward.apply(p);
        }
    }

    public void remove(Player p){
        for (String command : undoCommands){
            ValhallaMMO.getInstance().getServer().dispatchCommand(ValhallaMMO.getInstance().getServer().getConsoleSender(), command.replace("%player%", p.getName()));
        }
        for (PerkReward reward : rewards){
            if (reward.isPersistent()) continue;
            reward.remove(p);
        }
        for (ResourceExpense expense : expenses){
            if (expense.isRefundable()) expense.refund(p);
        }
    }
}
