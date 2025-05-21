package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarUtils {
    private final static Map<UUID, Map<String, TemporaryBossBar>> activeBossBars = new HashMap<>();

    public static Map<UUID, Map<String, TemporaryBossBar>> getActiveBossBars() {
        return activeBossBars;
    }

    /**
     * Shows a boss bar to the player for a given amount of time
     * @param player the player to show the boss bar to
     * @param progress the progress of the boss bar
     * @param time the time (in TENTH SECONDS) to show the boss bar
     */
    public static void showBossBarToPlayer(final Player player, String title, double progress, final int time, String skillType, BarColor color, BarStyle style){
        TemporaryBossBar bossBar = null;
        Map<String, TemporaryBossBar> bars = activeBossBars.get(player.getUniqueId());
        if (bars != null) bossBar = bars.get(skillType);

        if (progress < 0) progress = 0D;
        if (progress > 1) progress = 1D;

        if (bossBar == null){
            bossBar = new TemporaryBossBar(time, progress, title, player, skillType, color, style);
            Map<String, TemporaryBossBar> existingBossBars = activeBossBars.get(player.getUniqueId());
            if (existingBossBars == null) existingBossBars = new HashMap<>();
            existingBossBars.put(skillType, bossBar);
            bossBar.runTaskTimer(ValhallaMMO.getInstance(), 0L, 2L);
            activeBossBars.put(player.getUniqueId(), existingBossBars);
        }

        bossBar.setTimer(time);
        bossBar.setFraction(progress);
        bossBar.setText(title);
    }

    public static class TemporaryBossBar extends BukkitRunnable {
        private int timer;
        private double fraction;
        private String text;
        private final BossBar bossBar;
        private final Player p;
        private final String skillType;

        private String lastText;
        private double lastFraction;

        public TemporaryBossBar(int timer, double fraction, String text, Player p, String skillType, BarColor color, BarStyle style){
            this.timer = timer;
            this.fraction = fraction;
            this.text = text;
            this.p = p;
            this.skillType = skillType;
            this.bossBar = ValhallaMMO.getInstance().getServer().createBossBar(text, color, style);
        }

        @Override
        public void run() {
            if (timer <= 0){
                bossBar.removeAll();
                Map<String, TemporaryBossBar> existingBossBars = activeBossBars.get(p.getUniqueId());
                if (existingBossBars == null) existingBossBars = new HashMap<>();
                existingBossBars.remove(skillType);
                activeBossBars.put(p.getUniqueId(), existingBossBars);
                cancel();
            } else {
                if (!this.text.equals(lastText)) {
                    bossBar.setTitle(text);
                    lastText = text;
                }
                if (this.fraction != lastFraction) {
                    bossBar.setProgress(fraction);
                    lastFraction = fraction;
                }
                bossBar.addPlayer(p);
                timer--;
            }
        }

        public void setTimer(int timer) {
            this.timer = timer;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setFraction(double fraction) {
            this.fraction = fraction;
        }
    }
}
