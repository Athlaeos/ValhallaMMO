package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class EntitySpawnListener implements Listener {

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent e){
        if (e.isCancelled() || e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) return;
        e.getEntity().setMetadata("valhallammo_spawnreason", new FixedMetadataValue(ValhallaMMO.getInstance(), e.getSpawnReason().toString()));
    }

    public static CreatureSpawnEvent.SpawnReason getSpawnReason(Entity e){
        if (!e.hasMetadata("valhallammo_spawnreason")) return null;
        List<MetadataValue> metadata = e.getMetadata("valhallammo_spawnreason");
        if (metadata.isEmpty()) return null;
        return Catch.catchOrElse(() -> CreatureSpawnEvent.SpawnReason.valueOf(metadata.get(0).asString()), null);
    }
}