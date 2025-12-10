package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.Locale;

public class OnBlockBreak implements EffectTrigger.ConfigurableTrigger, Listener {
    private static Listener singleListenerInstance = null;

    @Override
    public String id() {
        return "on_block_break_";
    }

    @Override
    public void onRegister() {
        if (singleListenerInstance != null) return;
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
        singleListenerInstance = this;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.isCancelled()) return;
        EntityProperties properties = EntityCache.getAndCacheProperties(e.getPlayer());

        for (String s : properties.getPermanentPotionEffects().keySet()){
            if (!s.startsWith(id())) continue;
            String arg = getArg(s);
            if (!arg.toUpperCase(Locale.US).contains(e.getBlock().getType().toString())) continue;
            trigger(e.getPlayer(), properties.getPermanentEffectCooldowns().get(s), properties.getPermanentPotionEffects().getOrDefault(s, new ArrayList<>()));
        }
    }

    @Override
    public String isValid(String arg) {
        String args = getArg(arg);
        String[] blocks = args.split("/");
        for (String block : blocks){
            Material material = Catch.catchOrElse(() -> Material.valueOf(block.toUpperCase(Locale.US)), null);
            if (material == null || !material.isBlock()) return "&cBlock list contained material " + block + ", which is not a valid block";
        }
        if (blocks.length == 0) return "&cBlock list is empty";
        return null;
    }

    @Override
    public String getUsage() {
        return "A block material list separated by slashes, like for example \"DIRT/GRASS_BLOCK/COBBLESTONE\"";
    }

    @Override
    public String asLore(String rawID) {
        String args = getArg(rawID);
        String[] blocks = args.split("/");
        return "&fBreak any of the following blocks to trigger: " + String.join(", ", blocks);
    }
}
