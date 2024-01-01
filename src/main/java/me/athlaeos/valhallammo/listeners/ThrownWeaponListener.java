package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.throwable_weapon_animations.ThrowableItemStats;
import me.athlaeos.valhallammo.item.throwable_weapon_animations.ThrowableWeaponAnimation;
import me.athlaeos.valhallammo.item.throwable_weapon_animations.ThrowableWeaponAnimationRegistry;
import me.athlaeos.valhallammo.item.throwable_weapon_animations.ThrownItem;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ThrownWeaponListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.useItemInHand() == Event.Result.DENY ||
                e.getAction() != Action.RIGHT_CLICK_AIR || !e.hasItem() || ItemUtils.isEmpty(e.getItem()) ||
                !Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "delay_thrown_weapon_check") ||
                !Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "cooldown_thrown_weapon")) return;

        ItemBuilder item = new ItemBuilder(e.getItem());
        ThrowableItemStats stats = ThrowableWeaponAnimationRegistry.getItemStats(item.getMeta());
        if (stats == null) {
            Timer.setCooldown(e.getPlayer().getUniqueId(), 500, "delay_thrown_weapon_check");
            return;
        }

        ThrowableWeaponAnimation animation = ThrowableWeaponAnimationRegistry.getRegisteredAnimation(stats.getAnimationType());
        if (animation == null) return;

        Timer.setCooldownIgnoreIfPermission(e.getPlayer(), stats.getCooldown() * 50, "cooldown_thrown_weapon");

        animation.throwItem(e.getPlayer(), item, e.getHand());

        if (!stats.isInfinity()){
            if (item.getItem().getAmount() <= 1){
                if (e.getHand() == EquipmentSlot.HAND) e.getPlayer().getInventory().setItemInMainHand(null);
                else if (e.getHand() == EquipmentSlot.OFF_HAND) e.getPlayer().getInventory().setItemInOffHand(null);
            } else {
                ItemStack hand = e.getItem();
                hand.setAmount(hand.getAmount() - 1);
            }
        }
    }

}
