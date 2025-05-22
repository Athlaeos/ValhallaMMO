package me.athlaeos.valhallammo.parties;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.utility.EntityUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PartyPvPListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !(e.getEntity() instanceof Player v)) return;
        Entity trueDamager = EntityUtils.getTrueDamager(e);
        if (!(trueDamager instanceof Player a)) return;
        Party victimParty = PartyManager.getParty(v);
        Party attackerParty = PartyManager.getParty(a);
        if (victimParty == null || attackerParty == null) return;
        if (!victimParty.getId().equalsIgnoreCase(attackerParty.getId())) return;
        if (!victimParty.isFriendlyFireEnabled()) e.setCancelled(true);
    }
}
