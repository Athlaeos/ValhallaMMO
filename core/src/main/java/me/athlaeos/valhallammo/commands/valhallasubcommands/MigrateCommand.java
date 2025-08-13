package me.athlaeos.valhallammo.commands.valhallasubcommands;

import com.google.common.collect.ClassToInstanceMap;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.persistence.ProfilePersistence;
import me.athlaeos.valhallammo.persistence.implementations.SQL;
import me.athlaeos.valhallammo.persistence.implementations.SQLite;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MigrateCommand implements Command {
    private static final UUID CONSOLE = UUID.randomUUID();
    private final Map<UUID, Long> confirming = new HashMap<>();

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        UUID uuid = sender instanceof Player player ? player.getUniqueId() : CONSOLE;
        ProfilePersistence current = ProfileRegistry.getPersistence();
        String currentType = current.getType();
        String previousType = currentType.equals("mysql") ? "sqlite" : "mysql";
        Long confirmTime = confirming.remove(uuid);
        if (confirmTime == null || System.currentTimeMillis() - confirmTime > 30000) {
            confirming.put(uuid, System.currentTimeMillis());
            Utils.sendMessage(sender, "&cAre you sure you want to migrate data from %s to %s? Any data present in the %s database also present in the %s database will be overridden! If you are sure, run this command again within 30 seconds! &7(You may want to make backups before running this command)"
                    .formatted(previousType, currentType, previousType, currentType));
            return true;
        }

        ProfilePersistence previous = previousType.equals("mysql") ? new SQL() : new SQLite();
        if (previous.getConnection(true) == null) {
            Utils.sendMessage(sender, "&cFailed to connect to the previous %s database. Migration aborted. &7(Check logs for more info)".formatted(previousType));
            return true;
        }

        Utils.sendMessage(sender, "&aMigrating data from %s to %s...".formatted(previousType, currentType));
        Bukkit.getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
            try {
                current.uncacheAllProfiles();
                long start = System.currentTimeMillis();
                for (Profile profile : ProfileRegistry.getRegisteredProfiles().values()) {
                    if (!previous.hasProfileTable(profile)) {
                        Utils.sendMessage(sender, "&cNo existing profile table found for %s in the %s database. Skipping migration for this profile.".formatted(profile.getTableName(), previousType));
                        continue;
                    }
                    if (current.hasProfileTable(profile)) {
                        Utils.sendMessage(sender, "&eProfile table %s already exists in the %s database. Deleting and recreating...".formatted(profile.getTableName(), currentType));
                        long deleteStart = System.currentTimeMillis();
                        if (!current.deleteProfileTable(profile)) {
                            Utils.sendMessage(sender, "&cFailed to delete existing profile table for %s. Migration aborted.".formatted(profile.getTableName()));
                            return;
                        }
                        long deleteDuration = System.currentTimeMillis() - deleteStart;
                        Utils.sendMessage(sender, "&aDeletion took %s seconds.".formatted(deleteDuration / 1000.0));
                    }
                    long createStart = System.currentTimeMillis();
                    current.createProfileTable(profile);
                    long createDuration = System.currentTimeMillis() - createStart;
                    if (!current.hasProfileTable(profile)) {
                        Utils.sendMessage(sender, "&cFailed to create profile table for %s. Migration aborted.".formatted(profile.getTableName()));
                        return;
                    }
                    Utils.sendMessage(sender, "&aCreation took %s seconds.".formatted(createDuration / 1000.0));
                }
                long tableDuration = System.currentTimeMillis() - start;

                long playerStart = System.currentTimeMillis();
                int players = 0;
                for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                    UUID playerUUID = player.getUniqueId();
                    ClassToInstanceMap<Profile> profiles = previous.loadProfile(playerUUID);
                    for (Profile profile : profiles.values()) {
                        current.trySetPersistentProfile(playerUUID, profile, profile.getClass());
                    }

                    if (++players % 25 == 0) {
                        Utils.sendMessage(sender, "&aMigrated %s players so far...".formatted(players));
                    }
                }
                long playerDuration = System.currentTimeMillis() - playerStart;
                double averagePlayerDuration = playerDuration / (double) players;
                current.saveAllProfiles(false);

                long totalDuration = System.currentTimeMillis() - start;
                Utils.sendMessage(sender, "&aMigration completed for %s players in %s seconds. &7(%ss on database prep, %ss on player migration, avg %ss per player)"
                        .formatted(players, totalDuration / 1000.0, tableDuration / 1000.0, playerDuration / 1000.0, averagePlayerDuration / 1000.0));
            } catch (Exception e) {
                Utils.sendMessage(sender, "&cAn error occurred while migrating data: %s".formatted(e.getMessage()));
                e.printStackTrace();
            }
        });
        return true;
    }

    @Override
    public String getFailureMessage(String[] args) {
        return "/val migrate";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getCommand() {
        return "/val migrate";
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[]{"valhalla.migrate"};
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission("valhalla.migrate");
    }

    @Override
    public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
        return List.of();
    }
}
