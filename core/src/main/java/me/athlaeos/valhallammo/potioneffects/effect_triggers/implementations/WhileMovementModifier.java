package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.potioneffects.CustomPotionEffect;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.potioneffects.PotionEffectWrapper;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import java.util.ArrayList;
import java.util.List;

public class WhileMovementModifier implements EffectTrigger.ConstantTrigger, Listener {
    private static Listener singleListenerInstance = null;
    private final Boolean movementMode;
    public WhileMovementModifier(Boolean movementMode){
        this.movementMode = movementMode;
    }

    @Override
    public boolean shouldTrigger(LivingEntity entity) {
        if (!(entity instanceof Player p)) return false;
        return movementMode == null ? (!p.isSneaking() && !p.isSprinting()) : (movementMode ? p.isSprinting() : p.isSneaking());
    }

    @Override
    public int tickDelay() {
        return 10;
    }

    @Override
    public String id() {
        return "while_" + (movementMode == null ? "walking" : (movementMode ? "sprinting" : "sneaking"));
    }

    @Override
    public void onRegister() {
        if (singleListenerInstance != null) return;
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
        singleListenerInstance = this;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSprintToggle(PlayerToggleSprintEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName())) return;
        EntityProperties properties = EntityCache.getAndCacheProperties(e.getPlayer());
        if (e.isSprinting()){
            // player starts sprinting and thus also stops walking
            // add sprint effects, remove walk effects
            if (!properties.getPermanentPotionEffects().isEmpty()) {
                if (!properties.getPermanentPotionEffects().getOrDefault("while_sprinting", new ArrayList<>()).isEmpty())
                    trigger(e.getPlayer(), properties.getPermanentPotionEffects().getOrDefault("while_sprinting", new ArrayList<>()));
                if (!properties.getPermanentPotionEffects().getOrDefault("while_walking", new ArrayList<>()).isEmpty())
                    unTrigger(e.getPlayer(), properties.getPermanentPotionEffects().getOrDefault("while_walking", new ArrayList<>()));
            }
        } else {
            // player stops sprinting
            // add walk effects and remove sprint effects
            if (!properties.getPermanentPotionEffects().getOrDefault("while_sprinting", new ArrayList<>()).isEmpty())
                unTrigger(e.getPlayer(), properties.getPermanentPotionEffects().getOrDefault("while_sprinting", new ArrayList<>()));
            if (!properties.getPermanentPotionEffects().getOrDefault("while_walking", new ArrayList<>()).isEmpty())
                trigger(e.getPlayer(), properties.getPermanentPotionEffects().getOrDefault("while_walking", new ArrayList<>()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSneakToggle(PlayerToggleSneakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName())) return;
        EntityProperties properties = EntityCache.getAndCacheProperties(e.getPlayer());
        if (e.isSneaking()){
            // player starts sneaking and thus also stops walking
            // add sneak effects, remove walk effects
            if (!properties.getPermanentPotionEffects().isEmpty()) {
                if (!properties.getPermanentPotionEffects().getOrDefault("while_sneaking", new ArrayList<>()).isEmpty())
                    trigger(e.getPlayer(), properties.getPermanentPotionEffects().getOrDefault("while_sneaking", new ArrayList<>()));
                if (!properties.getPermanentPotionEffects().getOrDefault("while_walking", new ArrayList<>()).isEmpty())
                    unTrigger(e.getPlayer(), properties.getPermanentPotionEffects().getOrDefault("while_walking", new ArrayList<>()));
            }
        } else {
            // player stops sneaking
            // add walk effects and remove sneak effects
            if (!properties.getPermanentPotionEffects().getOrDefault("while_sneaking", new ArrayList<>()).isEmpty())
                unTrigger(e.getPlayer(), properties.getPermanentPotionEffects().getOrDefault("while_sneaking", new ArrayList<>()));
            if (!properties.getPermanentPotionEffects().getOrDefault("while_walking", new ArrayList<>()).isEmpty())
                trigger(e.getPlayer(), properties.getPermanentPotionEffects().getOrDefault("while_walking", new ArrayList<>()));
        }
    }

    private void unTrigger(Player p, List<PotionEffectWrapper> effects){
        for (PotionEffectWrapper effectWrapper : effects){
            if (effectWrapper.isVanilla()) p.removePotionEffect(effectWrapper.getVanillaEffect());
            else {
                CustomPotionEffect effect = new CustomPotionEffect(effectWrapper, 0, effectWrapper.getAmplifier());
                PotionEffectRegistry.addEffect(p, null, effect, true, 1, EntityPotionEffectEvent.Cause.EXPIRATION);
            }
        }
    }
}
