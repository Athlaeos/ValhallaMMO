package me.athlaeos.valhallammo.trading.happiness.sources;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.happiness.HappinessSource;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;

import java.util.*;

public class Comfort implements HappinessSource, Listener {
    private final int comfortRequirement = CustomMerchantManager.getTradingConfig().getInt("comfort_required_things", 64);
    private final int depressingMax = CustomMerchantManager.getTradingConfig().getInt("depressing_luxuries_max", 5);
    private final float comfortHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.comfortable", 1);
    private final float depressingHappiness = (float) CustomMerchantManager.getTradingConfig().getDouble("happiness_sources.depressing", -5);
    private final Collection<String> luxuryThings = new HashSet<>(CustomMerchantManager.getTradingConfig().getStringList("nice_things"));

    private final Map<UUID, Float> happinessCache = new HashMap<>();

    public Comfort(){
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
    }

    @Override
    public String id() {
        return "COMFORT";
    }

    @EventHandler
    public void onRestock(VillagerReplenishTradeEvent e){
        happinessCache.remove(e.getEntity().getUniqueId());
    }

    @Override
    public float get(Player contextPlayer, Entity entity) {
        if (happinessCache.containsKey(entity.getUniqueId())) return happinessCache.get(entity.getUniqueId());
        Collection<String> found = new HashSet<>();

        Block b = entity.getLocation().getBlock();
        for (int x = b.getX() - 8; x <= b.getX() + 8; x++){
            for (int y = b.getY() - 4; y <= b.getY() + 4; y++){
                for (int z = b.getZ() - 8; z <= b.getZ() + 8; z++){
                    Block at = entity.getWorld().getBlockAt(x, y, z);
                    if (!found.contains(at.getType().toString()) && luxuryThings.contains(at.getType().toString()))
                        found.add(at.getType().toString());
                }
            }
        }

        for (Entity nearby : entity.getNearbyEntities(8, 4, 8))
            if (!found.contains(nearby.getType().toString()) && luxuryThings.contains(nearby.getType().toString()))
                found.add(nearby.getType().toString());

        float happiness = 0;
        if (found.size() <= depressingMax) happiness = depressingHappiness;
        else if (found.size() >= comfortRequirement) happiness = comfortHappiness;

        happinessCache.put(entity.getUniqueId(), happiness);
        return happiness;
    }

    @Override
    public boolean appliesTo(Entity entity) {
        return entity instanceof AbstractVillager;
    }
}
