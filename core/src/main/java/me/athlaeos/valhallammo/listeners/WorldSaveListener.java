package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.item.ArmorSetRegistry;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.parties.PartyManager;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.utility.GlobalEffect;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

public class WorldSaveListener implements Listener {
    @EventHandler
    public void onWorldSave(WorldSaveEvent e){
        if (e.getWorld().equals(ValhallaMMO.getInstance().getServer().getWorlds().get(0))) {
            ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
                ProfileRegistry.saveAll();
                CustomRecipeRegistry.saveRecipes(false);
                LootTableRegistry.saveAll();
                ArmorSetRegistry.saveArmorSets();
                CustomItemRegistry.saveItems();
                GlobalEffect.saveActiveGlobalEffects();
                PartyManager.saveParties();
            });
        }
    }
}
