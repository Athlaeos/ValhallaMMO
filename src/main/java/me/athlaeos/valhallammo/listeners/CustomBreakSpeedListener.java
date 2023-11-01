package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.ActiveBlockDig;
import me.athlaeos.valhallammo.block.DigPacketInfo;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class CustomBreakSpeedListener implements Listener {
    private static boolean disabled = true;
    private static final Map<UUID, PotionEffect> previousFatigueEffects = new HashMap<>();
    private static final Map<UUID, Long> previousFatigueEffectRemoved = new HashMap<>();
    private static final PotionEffect fatigueEffect = new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, -1, true, false, false);

    private static final Map<UUID, ActiveBlockDig> activeBreakingProcesses = new HashMap<>();
    private static final Collection<Location> instantBlockBreaks = new HashSet<>();

    public CustomBreakSpeedListener(){
        disabled = false;
    }

    public static void onStart(DigPacketInfo info){
        if (info == null || disabled) return;
        if (info.getType() == DigPacketInfo.Type.START){
            Block b = info.block();

            int breakTime = info.breakTime();
            if (breakTime > 0){
                fatiguePlayer(info.getDigger());
                ItemStack hand = info.getDigger().getInventory().getItemInMainHand();
                ItemBuilder minedWith = ItemUtils.isEmpty(hand) ? null : new ItemBuilder(hand);
                activeBreakingProcesses.put(info.getDigger().getUniqueId(), new ActiveBlockDig(info, minedWith, breakTime));
            } else {
                instantBlockBreaks.add(b.getLocation());
                ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
                    ValhallaMMO.getNms().breakBlock(info.getDigger(), b);
                    BlockUtils.removeCustomHardness(b);
                });
            }
        }
    }

    public static void onStop(DigPacketInfo info){
        Player p = info.getDigger();
        if (!info.finished() || p == null || disabled) return;
        if (info.getType() == DigPacketInfo.Type.ABORT){
            if (activeBreakingProcesses.containsKey(p.getUniqueId())){
                activeBreakingProcesses.get(p.getUniqueId()).abort();
                activeBreakingProcesses.remove(p.getUniqueId());
            }
            removeFatiguedPlayer(p);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent e){
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        ActiveBlockDig activeBlock = activeBreakingProcesses.get(e.getPlayer().getUniqueId());
        if (activeBlock != null){
            activeBreakingProcesses.remove(e.getPlayer().getUniqueId());
            return;
        }
        if (instantBlockBreaks.remove(e.getBlock().getLocation())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent e){
        removeFatiguedPlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e){
        removeFatiguedPlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent e){
        removeFatiguedPlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent e){
        removeFatiguedPlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onResurrect(EntityResurrectEvent e){
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof Player p && activeBreakingProcesses.containsKey(p.getUniqueId())){
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> fatiguePlayer(p), 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMilk(PlayerItemConsumeEvent e){
        if (e.isCancelled()) return;
        if (activeBreakingProcesses.containsKey(e.getPlayer().getUniqueId())){
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> fatiguePlayer(e.getPlayer()), 1L);
        }
    }

    private static void fatiguePlayer(Player p){
        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
            PotionEffect previousEffect = p.getPotionEffect(PotionEffectType.SLOW_DIGGING);
            if (previousEffect != null && previousEffect.getAmplifier() < 5) {
                previousFatigueEffects.put(p.getUniqueId(), previousEffect);
                previousFatigueEffectRemoved.put(p.getUniqueId(), System.currentTimeMillis());
            }
            p.addPotionEffect(fatigueEffect);
        });
    }

    private static void removeFatiguedPlayer(Player p){
        ValhallaMMO.getInstance().getServer().getScheduler().runTask(ValhallaMMO.getInstance(), () -> {
            p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
            PotionEffect previousEffect = previousFatigueEffects.get(p.getUniqueId());
            if (previousEffect != null){
                int newDuration = previousEffect.getDuration() - (int) ((System.currentTimeMillis() - previousFatigueEffectRemoved.get(p.getUniqueId())) / 50D);
                if (newDuration > 0) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, newDuration, previousEffect.getAmplifier()));
                }
                previousFatigueEffectRemoved.remove(p.getUniqueId());
                previousFatigueEffects.remove(p.getUniqueId());
            }
        });
    }
}
