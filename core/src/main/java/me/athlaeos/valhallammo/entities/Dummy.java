package me.athlaeos.valhallammo.entities;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class Dummy implements Listener {
    private static final NamespacedKey DUMMY_KEY = new NamespacedKey(ValhallaMMO.getInstance(), "dummy_head");

    public static boolean isDummyItem(ItemMeta meta){
        return meta.getPersistentDataContainer().has(DUMMY_KEY, PersistentDataType.BYTE);
    }

    public static void setDummyItem(ItemMeta meta, boolean dummy){
        if (dummy) meta.getPersistentDataContainer().set(DUMMY_KEY, PersistentDataType.BYTE, (byte) 1);
        else meta.getPersistentDataContainer().remove(DUMMY_KEY);
    }

    public static boolean isDummy(LivingEntity stand){
        if (stand.getType() != EntityType.ARMOR_STAND) return false;
        EntityProperties equipment = EntityCache.getAndCacheProperties(stand);
        if (equipment.getHelmet() == null) return false;
        return equipment.getHelmet().getMeta().getPersistentDataContainer().has(DUMMY_KEY, PersistentDataType.BYTE);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDummyCreation(PlayerInteractAtEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getRightClicked().getWorld().getName()) ||
                !(e.getRightClicked() instanceof ArmorStand a) || a.getEquipment() == null) return;
        ItemStack interactedWith = e.getPlayer().getInventory().getItemInMainHand();
        if (ItemUtils.isEmpty(interactedWith)) return;
        ItemMeta meta = ItemUtils.getItemMeta(interactedWith);
        if (meta == null || !isDummyItem(meta) || !ItemUtils.isEmpty(a.getEquipment().getHelmet())) return;
        e.setCancelled(true);
        ItemStack clone = interactedWith.clone();
        clone.setAmount(1);
        a.getEquipment().setHelmet(clone);
        if (interactedWith.getAmount() <= 1) e.getPlayer().getInventory().setItemInMainHand(null);
        else interactedWith.setAmount(interactedWith.getAmount() - 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDummyItemPlacement(BlockPlaceEvent e){
        if (ItemUtils.isEmpty(e.getItemInHand()) || !e.getItemInHand().getType().isBlock()) return;
        ItemMeta meta = ItemUtils.getItemMeta(e.getItemInHand());
        if (meta == null) return;
        if (isDummyItem(meta)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArrowHitDummy(ProjectileHitEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getEntity().getWorld().getName()) || e.getHitEntity() == null ||
                e.getHitBlock() != null || !(e.getEntity() instanceof AbstractArrow a) || !(e.getHitEntity() instanceof LivingEntity l) ||
                !isDummy(l)) return;
        ItemBuilder stored = ItemUtils.getStoredItem(a);
        if (stored == null) return;
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            if (a.isValid()) {
                if (a.getPickupStatus() == AbstractArrow.PickupStatus.ALLOWED) a.getWorld().dropItem(a.getLocation(), stored.get());
                a.remove();
            }
        }, 2L);
    }
}
