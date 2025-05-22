package me.athlaeos.valhallammo.skills.perk_rewards.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

public class FlightReward extends PerkReward implements Listener {
    private static final NamespacedKey KEY_GRANTED_FLIGHT = new NamespacedKey(ValhallaMMO.getInstance(), "granted_flight");

    private final boolean flightPvPPrevention = ValhallaMMO.getPluginConfig().getBoolean("flight_pvp_prevention", true);
    private final boolean flightPvEPrevention = ValhallaMMO.getPluginConfig().getBoolean("flight_pve_prevention", true);
    private final int flightPvPPreventionDuration = ValhallaMMO.getPluginConfig().getInt("flight_pvp_prevention_duration", 30);
    private final int flightPvEPreventionDuration = ValhallaMMO.getPluginConfig().getInt("flight_pve_prevention_duration", 5);
    private final boolean flightPreventionSlowFalling = ValhallaMMO.getPluginConfig().getBoolean("flight_prevention_slow_falling", false);

    public FlightReward() {
        super("enable_flight");
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    @Override
    public void apply(Player player) {
        Profile profile = isPersistent() ? ProfileRegistry.getPersistentProfile(player, PowerProfile.class) : ProfileRegistry.getSkillProfile(player, PowerProfile.class);

        profile.setBoolean("flight", true);

        if (isPersistent()) ProfileRegistry.setPersistentProfile(player, profile, PowerProfile.class);
        else ProfileRegistry.setSkillProfile(player, profile, PowerProfile.class);
        ProfileCache.resetCache(player);

        setFlight(player, true);
    }

    @Override
    public void remove(Player player) {
        Profile profile = isPersistent() ? ProfileRegistry.getPersistentProfile(player, PowerProfile.class) : ProfileRegistry.getSkillProfile(player, PowerProfile.class);

        profile.setBoolean("flight", false);

        if (isPersistent()) ProfileRegistry.setPersistentProfile(player, profile, PowerProfile.class);
        else ProfileRegistry.setSkillProfile(player, profile, PowerProfile.class);
        ProfileCache.resetCache(player);

        setFlight(player, false);
    }

    public static void setFlight(Player p, boolean flight){
        if (flight){
            PowerProfile profile = ProfileCache.getOrCache(p, PowerProfile.class);
            if (!p.getAllowFlight() && profile.getBoolean("flight")) {
                // player may not fly
                p.setAllowFlight(true);
                p.getPersistentDataContainer().set(KEY_GRANTED_FLIGHT, PersistentDataType.BYTE, (byte) 0);
            }
        } else {
            if (p.getAllowFlight() && p.getPersistentDataContainer().has(KEY_GRANTED_FLIGHT, PersistentDataType.BYTE)){
                // player may fly, and it's because they've been granted it through this reward
                p.setAllowFlight(false);
                p.getPersistentDataContainer().remove(KEY_GRANTED_FLIGHT);
            }
        }
    }

    @Override
    public void parseArgument(Object o) {

    }

    @Override
    public String rewardPlaceholder() {
        return TranslationManager.getTranslation("translation_toggles");
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.NONE;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onToggleFlight(PlayerToggleFlightEvent e){
        if (!e.isFlying() || !e.getPlayer().getPersistentDataContainer().has(KEY_GRANTED_FLIGHT, PersistentDataType.BYTE)) return;
        if (Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "flight_interrupt")) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPvP(EntityDamageByEntityEvent e){
        if (flightPvPPrevention && e.getEntity() instanceof Player v && EntityUtils.getTrueDamager(e) instanceof Player a){
            Timer.setCooldown(v.getUniqueId(), 1000 * flightPvPPreventionDuration, "flight_interrupt");
            Timer.setCooldown(a.getUniqueId(), 1000 * flightPvPPreventionDuration, "flight_interrupt");
            if (v.isFlying() && v.getPersistentDataContainer().has(KEY_GRANTED_FLIGHT, PersistentDataType.BYTE)) {
                v.setFlying(false);
                if (flightPreventionSlowFalling) v.addPotionEffect(new PotionEffect(PotionEffectMappings.SLOW_FALLING.getPotionEffectType(), 200, 0, true, false, false));
            }
            if (a.isFlying() && a.getPersistentDataContainer().has(KEY_GRANTED_FLIGHT, PersistentDataType.BYTE)) {
                a.setFlying(false);
                if (flightPreventionSlowFalling) a.addPotionEffect(new PotionEffect(PotionEffectMappings.SLOW_FALLING.getPotionEffectType(), 200, 0, true, false, false));
            }
        } else if (flightPvEPrevention && e.getEntity() instanceof Player p){
            Timer.setCooldown(p.getUniqueId(), 1000 * flightPvEPreventionDuration, "flight_interrupt");
            if (p.isFlying() && p.getPersistentDataContainer().has(KEY_GRANTED_FLIGHT, PersistentDataType.BYTE)) {
                p.setFlying(false);
                if (flightPreventionSlowFalling) p.addPotionEffect(new PotionEffect(PotionEffectMappings.SLOW_FALLING.getPotionEffectType(), 200, 0, true, false, false));
            }
        } else if (flightPvEPrevention && EntityUtils.getTrueDamager(e) instanceof Player p){
            Timer.setCooldown(p.getUniqueId(), 1000 * flightPvEPreventionDuration, "flight_interrupt");
            if (p.isFlying() && p.getPersistentDataContainer().has(KEY_GRANTED_FLIGHT, PersistentDataType.BYTE)) {
                p.setFlying(false);
                if (flightPreventionSlowFalling) p.addPotionEffect(new PotionEffect(PotionEffectMappings.SLOW_FALLING.getPotionEffectType(), 200, 0, true, false, false));
            }
        }
    }
}
