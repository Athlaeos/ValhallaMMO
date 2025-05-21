package me.athlaeos.valhallammo.hooks;

import io.lumine.mythic.api.mobs.entities.SpawnReason;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.item.ItemComponentBukkitItemStack;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import io.lumine.mythic.bukkit.utils.numbers.RandomDouble;
import io.lumine.mythic.core.drops.droppables.VanillaItemDrop;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.entities.MonsterScalingManager;
import me.athlaeos.valhallammo.event.EntityUpdateLevelEvent;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.listeners.EntityDamagedListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
            @EventHandler
            public void mythicDropLoadEvent(MythicDropLoadEvent e)
            {
                if (!e.getDropName().equalsIgnoreCase("valhallammo") && !e.getDropName().equalsIgnoreCase("val"))
                    return;

                var split = e.getContainer().getLine().split(" ");
                String itemName = split[1];
                String amountRange = split.length > 2 ? split[2] : "1";
                var item = CustomItemRegistry.getProcessedItem(itemName);
                if (item == null)
                    return;

                e.register(new VanillaItemDrop(e.getContainer().getLine(), e.getConfig(), new ItemComponentBukkitItemStack(item), new RandomDouble(amountRange)));
            }

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

            @EventHandler
            public void onLevelUp(EntityUpdateLevelEvent e)
            {
                var entity = e.getEntity();
                var mob = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId());
                if (mob.isEmpty() || mob.get().getType().getFaction().equalsIgnoreCase("Animals"))
                    return;

                mob.get().setLevel(e.getLevel());
            }
            @EventHandler
            public void onDamage(MythicDamageEvent e)
            {
                if (e.getTarget().getBukkitEntity() instanceof LivingEntity le)
                {
                    if (e.getDamageMetadata().getElement() != null)
                        EntityDamagedListener.setCustomDamageCause(e.getTarget().getUniqueId(), e.getDamageMetadata().getElement());
                }
            }
        };

        Bukkit.getPluginManager().registerEvents(listener, ValhallaMMO.getInstance());
    }

    public static boolean isMythicMob(Entity entity)
    {
        if (!ValhallaMMO.isHookFunctional(MythicMobsHook.class))
            return false;
        return MythicBukkit.inst().getMobManager().isMythicMob(entity);
    }

    public static double getMythicMobStat(String stat, Entity entity)
    {
        stat = "VAL-" + stat;
        if (!ValhallaMMO.isHookFunctional(MythicMobsHook.class))
            return 0;
        var inst = MythicBukkit.inst();
        var mob = inst.getMobManager().getActiveMob(entity.getUniqueId());
        if (mob.isEmpty())
            return 0;
        var pd = mob.get().getType().getStats().getOrDefault(stat, null);
        if (pd == null)
            return 0;
        return pd.get(mob.get());
    }
}
