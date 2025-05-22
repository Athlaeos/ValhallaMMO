package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.version.AttributeMappings;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

import java.util.UUID;

public class BlockDamageListener implements Listener {
    private static final UUID UUID_DIG_SPEED = UUID.fromString("ba599501-275e-4cd3-b369-97f7e1f48798");
    private static final Attribute BLOCK_BREAK_SPEED = AttributeMappings.BLOCK_BREAK_SPEED.getAttribute();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent e){
        if (BLOCK_BREAK_SPEED == null) return;
        double extraDigSpeed = AccumulativeStatManager.getStats("BLOCK_SPECIFIC_DIG_SPEED", e.getPlayer(), true);
        extraDigSpeed += (float) AccumulativeStatManager.getCachedStats("DIG_SPEED", e.getPlayer(), 10000, true);
        ValhallaMMO.getNms().addUniqueAttribute(e.getPlayer(), UUID_DIG_SPEED, "block_specific_dig_speed", BLOCK_BREAK_SPEED, extraDigSpeed, AttributeModifier.Operation.ADD_SCALAR);
    }
}
