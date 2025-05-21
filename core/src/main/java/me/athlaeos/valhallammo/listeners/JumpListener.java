package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.animations.AnimationRegistry;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.event.PlayerJumpEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.MathUtils;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.*;

public class JumpListener implements Listener {
    private static Animation multiJumpAnimation = AnimationRegistry.MULTI_JUMP;
    private static final Collection<UUID> playersGivenFlight = new HashSet<>();
    private final Map<UUID, Integer> jumpsLeft = new HashMap<>();

    public static void setMultiJumpAnimation(Animation multiJumpAnimation) {
        JumpListener.multiJumpAnimation = multiJumpAnimation;
    }

    public static Animation getMultiJumpAnimation() {
        return multiJumpAnimation;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJump(PlayerJumpEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName())) return;
        if (!MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5)){
            double jumpHeightBonus = AccumulativeStatManager.getCachedStats("JUMP_HEIGHT_MULTIPLIER", e.getPlayer(), 10000, true);
            if (jumpHeightBonus > 0) e.getPlayer().setVelocity(e.getPlayer().getVelocity().add(new Vector(0, (jumpHeightBonus * 0.15), 0)));
        }

        if (e.getPlayer().getAllowFlight()) return; // players who already have the permission to fly are not able to multi-jump
        int extraJumps = (int) AccumulativeStatManager.getCachedStats("JUMPS_BONUS", e.getPlayer(), 10000, true);
        if (extraJumps <= 0) return;

        jumpsLeft.put(e.getPlayer().getUniqueId(), extraJumps);
        playersGivenFlight.add(e.getPlayer().getUniqueId());
        e.getPlayer().setAllowFlight(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.getPlayer().getGameMode() == GameMode.SPECTATOR ||
                e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (!playersGivenFlight.contains(e.getPlayer().getUniqueId()) && !jumpsLeft.containsKey(e.getPlayer().getUniqueId())){
            int extraJumps = (int) AccumulativeStatManager.getCachedStats("JUMPS_BONUS", e.getPlayer(), 10000, true);
            if (extraJumps > 0) { // still gives the player access to multi jumping if the player falls off of something
                jumpsLeft.put(e.getPlayer().getUniqueId(), extraJumps);
                playersGivenFlight.add(e.getPlayer().getUniqueId());
                e.getPlayer().setAllowFlight(true);
            }
        }
        if (!playersGivenFlight.contains(e.getPlayer().getUniqueId())) return;
        if (!e.getPlayer().getLocation().add(0, -0.1, 0).getBlock().getType().isSolid()) return; // player not standing on solid ground
        playersGivenFlight.remove(e.getPlayer().getUniqueId());
        e.getPlayer().setAllowFlight(false);
        jumpsLeft.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        double jumpHeightBonus = AccumulativeStatManager.getCachedStats("JUMP_HEIGHT_MULTIPLIER", e.getEntity(), 10000, true);
        if (jumpHeightBonus == 0) return;
        double fallDamage = Math.max(0, e.getEntity().getFallDistance() - 3 - jumpHeightBonus);
        if (e.getEntity().getLocation().subtract(0, 0.2, 0).getBlock().getType() == Material.POINTED_DRIPSTONE) fallDamage *= 2;
        e.setDamage(fallDamage);
    }

    @EventHandler(ignoreCancelled = true)
    public void onToggleFlight(PlayerToggleFlightEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || !playersGivenFlight.contains(e.getPlayer().getUniqueId()) ||
        e.getPlayer().getGameMode() == GameMode.CREATIVE || e.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
        e.setCancelled(true);
        double jumpHeightBonus = MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? 0 : AccumulativeStatManager.getCachedStats("JUMP_HEIGHT_MULTIPLIER", e.getPlayer(), 10000, true);

        if (!WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_DOUBLE_JUMPING)){
            PotionEffect jumpEffect = e.getPlayer().getPotionEffect(PotionEffectMappings.JUMP_BOOST.getPotionEffectType());
            int jumpLevel = 0;
            if (jumpEffect != null) jumpLevel = jumpEffect.getAmplifier() + 1;
            float f = e.getPlayer().getEyeLocation().getYaw() * 0.017453292F;
            double motionX = e.getPlayer().getVelocity().getX() - (e.getPlayer().isSprinting() ? MathUtils.sin(f) * 0.2 : 0);
            double motionY = 0.42 + (jumpLevel * 0.1F);
            double motionZ = e.getPlayer().getVelocity().getZ() + (e.getPlayer().isSprinting() ? MathUtils.cos(f) * 0.2 : 0);
            if (multiJumpAnimation != null) multiJumpAnimation.animate(e.getPlayer(), e.getPlayer().getLocation(), e.getPlayer().getEyeLocation().getDirection(), 0);
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () ->
                            e.getPlayer().setVelocity(new Vector(motionX, motionY, motionZ).add(new Vector(0, (jumpHeightBonus * 0.1), 0))),
                    1L);
            e.getPlayer().setFallDistance(0);
        }

        int remainingJumps = jumpsLeft.getOrDefault(e.getPlayer().getUniqueId(), 0) - 1;
        if (remainingJumps <= 0){
            e.getPlayer().setAllowFlight(false);
            playersGivenFlight.remove(e.getPlayer().getUniqueId());
        } else {
            jumpsLeft.put(e.getPlayer().getUniqueId(), remainingJumps);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        if (playersGivenFlight.contains(e.getPlayer().getUniqueId())) e.getPlayer().setAllowFlight(false);
    }

    public static void onServerStop(){
        for (Player p : ValhallaMMO.getInstance().getServer().getOnlinePlayers()) if (playersGivenFlight.contains(p.getUniqueId())) p.setAllowFlight(false);
    }
}
