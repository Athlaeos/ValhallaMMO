package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.entities.EntityClassification;
import me.athlaeos.valhallammo.entities.MonsterScalingManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EntitySpawnListener implements Listener {

    private static final Collection<String> viableSpawnReasons = Set.of(
            "DEFAULT", "NATURAL", "SPAWNER_EGG", "VILLAGE_INVASION", "VILLAGE_DEFENSE", "SPAWNER", "SILVERFISH_BLOCK",
            "RAID", "PATROL", "NETHER_PORTAL", "LIGHTNING", "ENDER_PEARL", "BUILD_WITHER", "BUILD_IRONGOLEM", "BUILD_SNOWMAN",
            "EGG", "CURED", "DISPENSE_EGG", "DROWNED", "INFECTION", "JOCKEY", "METAMORPHOSIS", "PIGLIN_ZOMBIFIED", "SLIME_SPLIT"
    );

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntitySpawn(CreatureSpawnEvent e){
        if (e.isCancelled() || e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) return;
        e.getEntity().setMetadata("valhallammo_spawnreason", new FixedMetadataValue(ValhallaMMO.getInstance(), e.getSpawnReason().toString()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLevelledEntitySpawn(CreatureSpawnEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || !viableSpawnReasons.contains(e.getSpawnReason().toString()) ||
                e.isCancelled() || EntityClassification.matchesClassification(e.getEntityType(), EntityClassification.UNALIVE)) return;
        int predictedLevel = MonsterScalingManager.getNewLevel(e.getEntity());
        if (predictedLevel < 0) return;
        MonsterScalingManager.setLevel(e.getEntity(), predictedLevel);

        AttributeInstance maxHealth = e.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) e.getEntity().setHealth(maxHealth.getValue());
    }

    public static CreatureSpawnEvent.SpawnReason getSpawnReason(Entity e){
        if (!e.hasMetadata("valhallammo_spawnreason")) return null;
        List<MetadataValue> metadata = e.getMetadata("valhallammo_spawnreason");
        if (metadata.isEmpty()) return null;
        return Catch.catchOrElse(() -> CreatureSpawnEvent.SpawnReason.valueOf(metadata.get(0).asString()), null);
    }
}