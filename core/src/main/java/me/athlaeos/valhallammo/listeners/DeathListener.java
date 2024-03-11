package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.entities.MonsterScalingManager;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import me.athlaeos.valhallammo.skills.ChunkEXPNerf;
import me.athlaeos.valhallammo.utility.Bleeder;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.*;

public class DeathListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent e){
        PotionEffectRegistry.removePotionEffects(e.getEntity(), EntityPotionEffectEvent.Cause.DEATH, eff -> eff.getWrapper().isRemovable());

        String cause = EntityDamagedListener.getLastDamageCause(e.getEntity());
        if (cause == null) return;
        Entity lastDamager = EntityDamagedListener.getLastDamager(e.getEntity());

        List<String> deathMessages = lastDamager != null ?
                TranslationManager.getListTranslation("death_message_" + cause.toLowerCase() + "_enemy") :
                TranslationManager.getListTranslation("death_message_" + cause.toLowerCase());
        if (deathMessages == null || deathMessages.isEmpty()) return;
        String pickedEntry = deathMessages.get(Utils.getRandom().nextInt(deathMessages.size()));
        if (!StringUtils.isEmpty(pickedEntry)) e.setDeathMessage(Utils.chat(pickedEntry
                .replace("%player%", e.getEntity().getName())
                .replace("%killer%", lastDamager != null ? lastDamager.getName() : ""))
        );
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExpDrops(EntityDeathEvent e){
        double multiplier = (1 + MonsterScalingManager.getExpOrbMultiplier(e.getEntity()));
        if (e.getEntity().getKiller() != null) multiplier *= ChunkEXPNerf.getChunkEXPOrbsNerf(e.getEntity().getLocation().getChunk(), e.getEntity().getKiller(), "exp_orbs");
        e.setDroppedExp(Utils.randomAverage(e.getDroppedExp() * multiplier));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent e){
        EntityCache.removeProperties(e.getEntity());
        Bleeder.removeBleed(e.getEntity());
        AccumulativeStatManager.updateStats(e.getEntity());
        String cause = EntityDamagedListener.getLastDamageCause(e.getEntity());
    }
}
