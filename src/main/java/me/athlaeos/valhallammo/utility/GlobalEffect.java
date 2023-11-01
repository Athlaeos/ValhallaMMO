package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalEffect extends BukkitRunnable {

    private static final Map<String, EffectProperties> activeGlobalEffects = new HashMap<>();
    private static final Collection<String> validEffects = new HashSet<>();
    private static final int globalbuff_bossbar_duration = ConfigManager.getConfig("config.yml").get().getInt("globalbuff_bossbar_duration", 10);
    private static final int globalbuff_cycle_pause = ConfigManager.getConfig("config.yml").get().getInt("globalbuff_cycle_pause", 300);

    public GlobalEffect(){
        this.runTaskTimer(ValhallaMMO.getInstance(), 1L, 20L);
    }


    private static List<EffectProperties> currentLoopEffects = new ArrayList<>();
    private static int currentLoopIndex = 0;
    private static final BossBar bossBar = ValhallaMMO.getInstance().getServer().createBossBar("", BarColor.BLUE, BarStyle.SOLID);
    private static int timer = 0;

    @Override
    public void run() {
        if (timer == 0){
            // at the start of a cycle, the list is refreshed
            currentLoopEffects = activeGlobalEffects.values().stream().filter(effectProperties -> effectProperties.bossBar != null && isActive(effectProperties.effect)).collect(Collectors.toList());
        }
        if (!currentLoopEffects.isEmpty()){
            if (currentLoopIndex < currentLoopEffects.size()){
                EffectProperties currentEffect = currentLoopEffects.get(currentLoopIndex);
                long l = getDuration(currentEffect.effect);
                long lastsFor = l == -1 ? -1 : Math.max(0, l);
                long originalDuration = getOriginalDuration(currentEffect.effect);

                ValhallaMMO.getInstance().getServer().getOnlinePlayers().forEach(player -> {
                    if (!temporarilyExcludePlayers.contains(player)){
                        bossBar.addPlayer(player);
                    }
                });
                bossBar.setTitle(Utils.chat(currentEffect.bossBar
                        .replace("%time%", StringUtils.toTimeStamp2(lastsFor, 1000))
                        .replace("%time2%", StringUtils.toTimeStamp(lastsFor, 1000))));
                bossBar.setColor(currentEffect.color);
                bossBar.setStyle(currentEffect.style);

                if (originalDuration > 0){
                    bossBar.setProgress(Math.max(0, Math.min(1, (double) (lastsFor) / (double) (originalDuration))));
                }
                if (timer >= (currentLoopIndex + 1) * globalbuff_bossbar_duration){
                    currentLoopIndex++;
                }
            } else {
                new ArrayList<>(bossBar.getPlayers()).forEach(player -> {
                    if (!temporarilyExcludePlayers.contains(player)){
                        bossBar.removePlayer(player);
                    }
                });
                // pause phase
            }
            timer++;
            if (timer >= globalbuff_cycle_pause + (currentLoopEffects.size() * globalbuff_bossbar_duration)) {
                timer = 0;
                currentLoopIndex = 0;
            }
        }
    }

    private static final Collection<Player> temporarilyExcludePlayers = new HashSet<>();

    public static void temporarilyRevealBossBar(Player p){
        temporarilyExcludePlayers.add(p);
        new BukkitRunnable(){
            private List<EffectProperties> currentLoopEffects = new ArrayList<>();
            private int currentLoopIndex = 0;
            private int timer = 0;
            @Override
            public void run() {
                if (!p.isOnline()) {
                    temporarilyExcludePlayers.remove(p);
                    cancel();
                    return;
                }
                if (timer == 0){
                    // at the start of a cycle, the list is refreshed
                    currentLoopEffects = activeGlobalEffects.values().stream().filter(effectProperties -> effectProperties.bossBar != null && isActive(effectProperties.effect)).collect(Collectors.toList());
                    if (currentLoopEffects.isEmpty()){
                        temporarilyExcludePlayers.remove(p);
                        cancel();
                        return;
                    }
                }
                if (currentLoopIndex < currentLoopEffects.size()){
                    EffectProperties currentEffect = currentLoopEffects.get(currentLoopIndex);
                    long l = getDuration(currentEffect.effect);
                    long lastsFor = l == -1 ? -1 : Math.max(0, l);
                    long originalDuration = getDuration(currentEffect.effect);

                    bossBar.addPlayer(p);
                    bossBar.setTitle(Utils.chat(currentEffect.bossBar
                            .replace("%time%", StringUtils.toTimeStamp2(lastsFor, 1000))
                            .replace("%time2%", StringUtils.toTimeStamp(lastsFor, 1000))));
                    bossBar.setColor(currentEffect.color);
                    bossBar.setStyle(currentEffect.style);

                    if (originalDuration > 0){
                        bossBar.setProgress(Math.max(0, Math.min(1, (double) (lastsFor) / (double) (originalDuration))));
                    }
                    if (timer >= (currentLoopIndex + 1) * globalbuff_bossbar_duration){
                        currentLoopIndex++;
                    }
                } else {
                    bossBar.removeAll();
                    // pause phase
                }
                timer++;
                if (timer >= (currentLoopEffects.size() * globalbuff_bossbar_duration)) {
                    temporarilyExcludePlayers.remove(p);
                    cancel();
                }
            }
        }.runTaskTimer(ValhallaMMO.getInstance(), 1L, 20L);
    }

    public static void saveActiveGlobalEffects(){
        YamlConfiguration config = ConfigManager.getConfig("global_effects.yml").get();
        ConfigurationSection section = config.getConfigurationSection("active_effects");
        if (section != null){
            section.getKeys(false).forEach(s -> config.set("active_effects." + s, null));
        }
        for (String s : activeGlobalEffects.keySet()){
            EffectProperties details = activeGlobalEffects.get(s);
            if (details.effectiveUntil != -1 && details.effectiveUntil < System.currentTimeMillis()) continue;
            config.set("active_effects." + s + ".lasts_until", details.effectiveUntil);
            config.set("active_effects." + s + ".original_duration", details.getOriginalLength());
            config.set("active_effects." + s + ".amplifier", details.amplifier);
            if (details.bossBar != null){
                config.set("active_effects." + s + ".boss_bar", details.bossBar);
                config.set("active_effects." + s + ".bar_color", details.color.toString());
                config.set("active_effects." + s + ".bar_style", details.style.toString());
            }
        }
        ConfigManager.saveConfig("global_effects.yml");
    }

    public static void loadActiveGlobalEffects(){
        YamlConfiguration config = ConfigManager.getConfig("global_effects.yml").get();
        ConfigurationSection section = config.getConfigurationSection("active_effects");
        if (section != null){
            for (String effect : section.getKeys(false)){
                long lasts_until = config.getLong("active_effects." + effect + ".lasts_until");
                if (lasts_until != -1 && lasts_until < System.currentTimeMillis()) continue;
                long original_duration = config.getLong("active_effects." + effect + ".original_duration");
                double amplifier = config.getDouble("active_effects." + effect + ".amplifier");
                String bossBar = config.getString("active_effects." + effect + ".boss_bar");
                if (bossBar != null){
                    try {
                        BarColor color = BarColor.valueOf(config.getString("active_effects." + effect + ".bar_color"));
                        BarStyle style = BarStyle.valueOf(config.getString("active_effects." + effect + ".bar_style"));
                        activeGlobalEffects.put(effect, new EffectProperties(effect, original_duration, lasts_until, amplifier, bossBar, color, style));
                    } catch (IllegalArgumentException ignored){
                        ValhallaMMO.logWarning("Invalid BarColor or BarStyle for " + effect + " in global_effects.yml");
                    }
                } else {
                    activeGlobalEffects.put(effect, new EffectProperties(effect, original_duration, lasts_until, amplifier, null, null, null));
                }
            }
        }
    }

    /**
     * Adds a global effect with a given duration and amplifier
     * The chosen mode determines how the effect is added, if it already exists
     * ADDITIVE_DURATION stacks the given duration on top of the current duration. If an effect is already active with
     * 1 hour remaining, adding a new effect with this mode with a duration of 3 hours results in a final duration of 4 hours.
     * The amplifier is overwritten by the new
     * ADDITIVE_AMPLIFIER does the same thing, except with the amplifier. An existing amplifier of 50 would be amplified to 150
     * if a new effect with an amplifier of 100 was added with this mode. The duration is overwritten by the new
     * ADDITIVE_BOTH does both of the two modes. Neither of the two are overwritten
     * OVERWRITE does not add the effects, instead it completely overwrites them.
     * @param effect the effect to add
     * @param duration the duration of the effect in milliseconds
     * @param amplifier the amplifier of the effect
     * @param mode the mode the effect will be added in
     */
    public static void addEffect(String effect, long duration, double amplifier, EffectAdditionMode mode, String bossBar, BarColor barColor, BarStyle barStyle){
        double existingAmplifier = getAmplifier(effect);
        long existingDuration = getDuration(effect);
        long originalDuration = getOriginalDuration(effect);
        if (originalDuration == 0) originalDuration = duration;
        switch (mode) {
            case ADDITIVE_BOTH -> {
                existingAmplifier += amplifier;
                if (existingDuration != -1)
                    existingDuration += duration;

            }
            case ADDITIVE_DURATION -> {
                existingAmplifier = amplifier;
                if (existingDuration != -1)
                    existingDuration += duration;
            }
            case ADDITIVE_AMPLIFIER -> {
                existingAmplifier += amplifier;
                existingDuration = duration;
            }
            case OVERWRITE -> {
                existingAmplifier = amplifier;
                existingDuration = duration;
            }
        }
        if (existingDuration != -1){
            originalDuration = Math.max(existingDuration, originalDuration);
            existingDuration += System.currentTimeMillis();
        } else {
            originalDuration = -1;
        }
        timer = 0;
        currentLoopIndex = 0;

        activeGlobalEffects.put(effect, new EffectProperties(effect, originalDuration, existingDuration, existingAmplifier, bossBar, barColor, barStyle));
    }

    /**
     * Returns true if the given effect is active (duration left > 0)
     * @param buff the name of the effect
     * @return true if active, false if not
     */
    public static boolean isActive(String buff){
        if (activeGlobalEffects.containsKey(buff)){
            if (activeGlobalEffects.get(buff).effectiveUntil == -1) return true;
            return activeGlobalEffects.get(buff).effectiveUntil > System.currentTimeMillis();
        }
        return false;
    }

    /**
     * Returns the remaining duration of the given effect
     * If not active, duration is 0
     * @param buff the name of the effect
     * @return the remaining duration (in milliseconds) of the effect
     */
    public static long getDuration(String buff){
        if (isActive(buff)){
            if (activeGlobalEffects.get(buff).effectiveUntil == -1) return -1;
            return Math.max(0, activeGlobalEffects.get(buff).effectiveUntil - System.currentTimeMillis());
        }
        return 0;
    }

    public static long getOriginalDuration(String buff){
        if (isActive(buff)){
            if (activeGlobalEffects.get(buff).getOriginalLength() == -1) return -1;
            return Math.max(0, activeGlobalEffects.get(buff).getOriginalLength());
        }
        return 0;
    }

    public static String getBossBarTitle(String buff){
        if (isActive(buff)){
            return activeGlobalEffects.get(buff).bossBar;
        }
        return null;
    }

    /**
     * Returns the amplifier of the given effect
     * If not active, amplifier is 0
     * @param buff the name of the effect
     * @return the amplifier of the effect
     */
    public static double getAmplifier(String buff){
        if (isActive(buff)){
            return Math.max(0, activeGlobalEffects.get(buff).amplifier);
        }
        return 0;
    }

    public static Collection<String> getValidEffects() {
        return validEffects;
    }

    public static void addValidEffect(String effect){
        validEffects.add(effect);
    }

    public static void removeValidEffect(String effect){
        validEffects.remove(effect);
    }

    public static Map<String, EffectProperties> getActiveGlobalEffects() {
        return activeGlobalEffects;
    }

    private static class EffectProperties{
        private final String effect;
        private final long effectiveUntil;
        private long originalLength;
        private final double amplifier;
        private final String bossBar;
        private final BarColor color;
        private final BarStyle style;
        public EffectProperties(String effect, long originalLength, long effectiveUntil, double amplifier, String bossBar, BarColor color, BarStyle style){
            this.effect = effect;
            this.originalLength = originalLength;
            this.effectiveUntil = effectiveUntil;
            this.amplifier = amplifier;
            this.bossBar = bossBar;
            this.color = color;
            this.style = style;
        }

        public long getOriginalLength() {
            return originalLength;
        }

        public void setOriginalLength(long originalLength) {
            this.originalLength = originalLength;
        }
    }

    public enum EffectAdditionMode {
        OVERWRITE,
        ADDITIVE_DURATION,
        ADDITIVE_AMPLIFIER,
        ADDITIVE_BOTH
    }
}
