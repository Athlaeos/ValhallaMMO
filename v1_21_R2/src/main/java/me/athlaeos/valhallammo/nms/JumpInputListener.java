package me.athlaeos.valhallammo.nms;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.event.PlayerJumpEvent;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.listeners.JumpListener;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.EntityUtils;
import me.athlaeos.valhallammo.utility.MathUtils;
import me.athlaeos.valhallammo.version.PotionEffectMappings;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.*;

public class JumpInputListener implements Listener {
    private final Collection<UUID> holdingSpace = new HashSet<>();

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJumpKey(PlayerInputEvent e){
        if (!e.getInput().isJump()) {
            holdingSpace.remove(e.getPlayer().getUniqueId());
            return;
        } else if (holdingSpace.contains(e.getPlayer().getUniqueId())) return;
        holdingSpace.add(e.getPlayer().getUniqueId());
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || jumpsLeft.getOrDefault(e.getPlayer().getUniqueId(), 0) <= 0 ||
                e.getPlayer().getGameMode() == GameMode.CREATIVE || e.getPlayer().getGameMode() == GameMode.SPECTATOR || EntityUtils.isOnGround(e.getPlayer()) ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_DOUBLE_JUMPING)) return;
        double jumpHeightBonus = MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? 0 : AccumulativeStatManager.getCachedStats("JUMP_HEIGHT_MULTIPLIER", e.getPlayer(), 10000, true);

        PotionEffect jumpEffect = e.getPlayer().getPotionEffect(PotionEffectMappings.JUMP_BOOST.getPotionEffectType());
        int jumpLevel = 0;
        if (jumpEffect != null) jumpLevel = jumpEffect.getAmplifier() + 1;
        float f = e.getPlayer().getEyeLocation().getYaw() * 0.017453292F;
        double motionX = e.getPlayer().getVelocity().getX() - (e.getPlayer().isSprinting() ? MathUtils.sin(f) * 0.2 : 0);
        double motionY = 0.42 + (jumpLevel * 0.1F);
        double motionZ = e.getPlayer().getVelocity().getZ() + (e.getPlayer().isSprinting() ? MathUtils.cos(f) * 0.2 : 0);
        if (JumpListener.getMultiJumpAnimation() != null) JumpListener.getMultiJumpAnimation().animate(e.getPlayer(), e.getPlayer().getLocation(), e.getPlayer().getEyeLocation().getDirection(), 0);
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () ->
                        e.getPlayer().setVelocity(new org.bukkit.util.Vector(motionX, motionY, motionZ).add(new Vector(0, (jumpHeightBonus * 0.1), 0))),
                1L);

        e.getPlayer().setFallDistance(0);
        int remainingJumps = jumpsLeft.getOrDefault(e.getPlayer().getUniqueId(), 0) - 1;

        jumpsLeft.put(e.getPlayer().getUniqueId(), remainingJumps);
    }

    private final Map<UUID, Integer> jumpsLeft = new HashMap<>();

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName())) return;

        int extraJumps = (int) AccumulativeStatManager.getCachedStats("JUMPS_BONUS", e.getPlayer(), 10000, true);
        if (extraJumps <= 0) return;

        jumpsLeft.put(e.getPlayer().getUniqueId(), extraJumps);
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
}
