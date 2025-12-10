package me.athlaeos.valhallammo.potioneffects.effect_triggers.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.FoodClass;
import me.athlaeos.valhallammo.item.FoodPropertyManager;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTrigger;
import me.athlaeos.valhallammo.potioneffects.effect_triggers.EffectTriggerRegistry;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Locale;

public class OnConsumption implements EffectTrigger, Listener {
    private static Listener singleListenerInstance = null;

    private final FoodClass foodClass;
    public OnConsumption(FoodClass foodClass){
        this.foodClass = foodClass;
    }

    @Override
    public String id() {
        return "on_eat_" + foodClass.toString().toLowerCase(Locale.US);
    }

    @Override
    public void onRegister() {
        if (singleListenerInstance != null) return;
        ValhallaMMO.getInstance().getServer().getPluginManager().registerEvents(this, ValhallaMMO.getInstance());
        singleListenerInstance = this;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onConsume(PlayerItemConsumeEvent e){
        ItemStack item = e.getItem();
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || ItemUtils.isEmpty(item)) return;
        Player p = e.getPlayer();
        ItemMeta meta = ItemUtils.getItemMeta(item);
        if (meta == null) return;
        FoodClass type = FoodPropertyManager.getFoodClass(item, meta);
        if (type == null) return;
        EntityProperties properties = EntityCache.getAndCacheProperties(p);

        String id = "on_eat_" + type.toString().toLowerCase(Locale.US);
        EffectTrigger trigger = EffectTriggerRegistry.getTrigger(id);
        if (trigger == null) return;
        if (!properties.getPermanentPotionEffects().getOrDefault(id, new ArrayList<>()).isEmpty()) {
            trigger.trigger(p, id, properties.getPermanentEffectCooldowns().get(id), properties.getPermanentPotionEffects().getOrDefault(id, new ArrayList<>()));
        }
    }
}
