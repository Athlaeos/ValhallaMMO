package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class SideBarUtils {
    private final static Map<UUID, Sidebar> activeSidebars = new HashMap<>();
    private static final List<Character> colors = List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'l', 'o', 'n', 'm', 'k', 'r');
    private static final ScoreboardManager manager = ValhallaMMO.getInstance().getServer().getScoreboardManager();

    /**
     * Shows a sidebar to the player
     * @param player the player to show the boss bar to
     * @param key the identifier for the scoreboard
     * @param title the title the scoreboard should display
     * @param contents the contents the scoreboard should display
     */
    public static void showSideBarToPlayer(final Player player, String key, String title, List<String> contents, boolean force){
        Sidebar sideBar = activeSidebars.get(player.getUniqueId());
        if (sideBar == null) sideBar = new Sidebar(player, key, title, contents);
        else sideBar.updateContents(contents);

        sideBar.showBoard(force);
        activeSidebars.put(player.getUniqueId(), sideBar);
    }

    public static void hideSideBarFromPlayer(final Player player, String key){
        if (player.getScoreboard().getObjective(key) != null && manager != null) player.setScoreboard(manager.getNewScoreboard());
    }

    private static class Sidebar {
        private final Player p;
        private final Scoreboard scoreboard = manager == null ? null : manager.getNewScoreboard();

        private Objective objective;

        public Sidebar(Player p, String key, String title, List<String> entries){
            this.p = p;
            if (scoreboard != null){
                this.objective = scoreboard.getObjective(key);
                if (this.objective == null) this.objective = scoreboard.registerNewObjective(key, Criteria.DUMMY, Utils.chat(title));
                this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                updateContents(entries);
            }
        }

        public void updateContents(List<String> newContents){
            if (scoreboard == null) return;

            newContents = newContents.subList(0, Math.min(newContents.size(), colors.size()));
            scoreboard.getTeams().forEach(Team::unregister);
            for (int i = 0; i < newContents.size(); i++){
                String lineContents = newContents.get(i);
                String color = Utils.chat("&" + colors.get(i) + "&r");
                Team team = scoreboard.getTeam("t" + i);
                if (team == null) team = scoreboard.registerNewTeam("t" + i);
                team.addEntry(color);

                team.setPrefix(lineContents);

                objective.getScore(color).setScore(i);
            }
        }

        public void showBoard(boolean force){
            if (force || p.getScoreboard().getObjective("valhalla") == null){
                p.setScoreboard(scoreboard);
            }
        }
    }
}
