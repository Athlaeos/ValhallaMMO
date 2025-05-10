package me.athlaeos.valhallammo.hooks;

import io.lumine.mythic.api.mobs.entities.SpawnReason;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.entities.MonsterScalingManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MythicMobsHook extends PluginHook
{
    private Listener listener;

    public MythicMobsHook()
    {
        super("MythicMobs");
    }

    public Listener getListener()
    {
        return listener;
    }

    @Override
    public void whenPresent()
    {
        this.listener = new Listener()
        {
            @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
            public void onMythicMobSpawn(MythicMobSpawnEvent event)
            {
                if (!event.getSpawnReason().equals(SpawnReason.NATURAL))
                {
                    return;
                }

                String faction = event.getMobType().getFaction();
                if (faction == null || !faction.equalsIgnoreCase("Animals"))
                {
                    event.setMobLevel(MonsterScalingManager.getAreaDifficultyLevel(event.getLocation(), null));
                }
            }
        };

        Bukkit.getPluginManager().registerEvents(listener, ValhallaMMO.getInstance());
    }
}
