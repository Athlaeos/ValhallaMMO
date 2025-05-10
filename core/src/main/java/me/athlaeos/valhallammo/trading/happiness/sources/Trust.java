package me.athlaeos.valhallammo.trading.happiness.sources;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.happiness.HappinessSource;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Trust implements HappinessSource, Listener {
    private static final Map<UUID, Map<UUID, Long>> lastHarmed = new HashMap<>();

    private final int trustingTime = CustomMerchantManager.getTradingConfig().getInt("trusting_requirement", 64);
    private final int distrustingTime = CustomMerchantManager.getTradingConfig().getInt("distrusting_max", 5);
    private final float trustingHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.trusting", 1);
    private final float distrustingHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.distrusting", -5);

    public Trust(){
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    @Override
    public String id() {
        return "TRUST";
    }

    @EventHandler
    public void onHarm(EntityDamageByEntityEvent e){
        if (e.isCancelled()) return;
        if (e.getEntity() instanceof AbstractVillager a && e.getEntity() instanceof Player p){
            Map<UUID, Long> map = lastHarmed.getOrDefault(a.getUniqueId(), new HashMap<>());
            map.put(p.getUniqueId(), CustomMerchantManager.time());
            lastHarmed.put(a.getUniqueId(), map);
        }
    }

    private static long getHarmed(Player player, AbstractVillager villager){
        return lastHarmed.getOrDefault(villager.getUniqueId(), new HashMap<>()).getOrDefault(player.getUniqueId(), -1L);
    }

    @Override
    public float get(Player contextPlayer, Entity entity) {
        if (contextPlayer == null || !(entity instanceof AbstractVillager villager)) return 0;
        long time = getHarmed(contextPlayer, villager);
        long difference = CustomMerchantManager.time() - time;
        if (difference <= distrustingTime) return distrustingHappiness;
        else if (difference >= trustingTime) return trustingHappiness;
        return 0;
    }

    @Override
    public boolean appliesTo(Entity entity) {
        return entity instanceof AbstractVillager;
    }
}
