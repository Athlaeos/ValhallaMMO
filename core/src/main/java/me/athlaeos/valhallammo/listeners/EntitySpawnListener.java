package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.entities.EntityClassification;
import me.athlaeos.valhallammo.entities.MonsterScalingManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EntitySpawnListener implements Listener {
    private final int lightningDrawRange;
    public EntitySpawnListener(){
        lightningDrawRange = ValhallaMMO.getPluginConfig().getInt("lightning_draw_range", 0);
    }

    private static final Collection<String> viableSpawnReasons = Set.of(
            "DEFAULT", "NATURAL", "SPAWNER_EGG", "VILLAGE_INVASION", "VILLAGE_DEFENSE", "SPAWNER", "SILVERFISH_BLOCK",
            "RAID", "PATROL", "NETHER_PORTAL", "LIGHTNING", "ENDER_PEARL", "BUILD_WITHER", "BUILD_IRONGOLEM", "BUILD_SNOWMAN",
            "EGG", "CURED", "DISPENSE_EGG", "DROWNED", "INFECTION", "JOCKEY", "METAMORPHOSIS", "PIGLIN_ZOMBIFIED", "SLIME_SPLIT",
            "BREEDING", "TRIAL_SPAWNER", "TRAP"
    );

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLightningStrike(LightningStrikeEvent e){
        if (lightningDrawRange <= 0 || e.getCause() != LightningStrikeEvent.Cause.WEATHER) return;
        if (e.getLightning().getLocation().subtract(0, 1, 0).getBlock().getType() == Material.LIGHTNING_ROD) return; // lightning struck lightning rod, takes priority, disregard
        Entity closestEntity = null;
        double lowestLightningResistance = 0D;
        for (Entity nearby : e.getLightning().getWorld().getNearbyEntities(e.getLightning().getLocation(), lightningDrawRange, lightningDrawRange, lightningDrawRange)){
            if (!(nearby instanceof LivingEntity l) || l.getLocation().getBlock().getLightFromSky() <= 14) continue;
            double lightningResistance = AccumulativeStatManager.getCachedStats("LIGHTNING_RESISTANCE", l, 10000L, false);
            if (lightningResistance < lowestLightningResistance) {
                lowestLightningResistance = lightningResistance;
                closestEntity = l;
            }
        }
        if (closestEntity == null) return;
        double distanceX = closestEntity.getLocation().getX() - e.getLightning().getLocation().getX();
        double distanceZ = closestEntity.getLocation().getZ() - e.getLightning().getLocation().getZ();
        double closingFactor = Utils.getRandom().nextDouble() * (1 / (1 - lowestLightningResistance)); // produces a random number that will get get closer to 0 the more negative lightning resistance the entity has
        // at -100% lightning resistance, lightning strikes will spawn roughly twice as close at minimum. At -200%, thrice as close, etc.

        double xOffset = distanceX * (1 - closingFactor);
        double zOffset = distanceZ * (1 - closingFactor);
        Location newLocation = e.getLightning().getLocation().clone().add(xOffset, 0, zOffset);
        newLocation.setY(e.getLightning().getWorld().getHighestBlockYAt(newLocation.getBlockX(), newLocation.getBlockZ()));
        e.getLightning().teleport(newLocation);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent e){
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) return;
        e.getEntity().setMetadata("valhallammo_spawnreason", new FixedMetadataValue(ValhallaMMO.getInstance(), e.getSpawnReason().toString()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLevelledEntitySpawn(CreatureSpawnEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !viableSpawnReasons.contains(e.getSpawnReason().toString()) ||
                EntityClassification.matchesClassification(e.getEntityType(), EntityClassification.UNALIVE)) return;
        AttributeInstance maxHealth = e.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (e.getEntity().getCustomName() != null || (maxHealth != null && maxHealth.getBaseValue() != maxHealth.getValue())) return;

        if (e.getEntity() instanceof Wolf w){
            if (MonsterScalingManager.updateWolfLevel(w, w.getOwner())){
                if (maxHealth != null) w.setHealth(maxHealth.getValue());
            }
        } else {
            int predictedLevel = MonsterScalingManager.getNewLevel(e.getEntity());
            if (predictedLevel < 0) return;
            MonsterScalingManager.setLevel(e.getEntity(), predictedLevel);

            if (maxHealth != null) e.getEntity().setHealth(maxHealth.getValue());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWolfInteract(PlayerInteractAtEntityEvent e){
        if (!(e.getRightClicked() instanceof Wolf w) || w.getOwner() == null || ValhallaMMO.isWorldBlacklisted(e.getRightClicked().getWorld().getName())) return;
        AttributeInstance maxHealth = w.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth == null) return;
        double healthFraction = w.getHealth() / maxHealth.getValue();
        if (!MonsterScalingManager.updateWolfLevel(w, e.getPlayer())) return;

        AttributeInstance newMaxHealth = w.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (newMaxHealth != null) w.setHealth(Math.min(newMaxHealth.getValue(), healthFraction * newMaxHealth.getValue()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWolfTame(EntityTameEvent e){
        if (!(e.getEntity() instanceof Wolf w) || ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName())) return;
        AttributeInstance maxHealth = w.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth == null) return;
        double healthFraction = w.getHealth() / maxHealth.getValue();
        if (!MonsterScalingManager.updateWolfLevel(w, e.getOwner())) return;

        AttributeInstance newMaxHealth = w.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (newMaxHealth != null) w.setHealth(Math.min(newMaxHealth.getValue(), healthFraction * newMaxHealth.getValue()));
    }

    public static CreatureSpawnEvent.SpawnReason getSpawnReason(Entity e){
        if (!e.hasMetadata("valhallammo_spawnreason")) return null;
        List<MetadataValue> metadata = e.getMetadata("valhallammo_spawnreason");
        if (metadata.isEmpty()) return null;
        return Catch.catchOrElse(() -> CreatureSpawnEvent.SpawnReason.valueOf(metadata.get(0).asString()), null);
    }

    public static boolean isTrialSpawned(Entity e){
        CreatureSpawnEvent.SpawnReason reason = getSpawnReason(e);
        return reason != null && reason.toString().equalsIgnoreCase("TRIAL_SPAWNER");
    }
}