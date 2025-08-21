package me.athlaeos.valhallammo.listeners;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.block.BlockInteractConversions;
import me.athlaeos.valhallammo.hooks.WorldGuardHook;
import me.athlaeos.valhallammo.item.WeightClass;
import me.athlaeos.valhallammo.playerstats.EntityCache;
import me.athlaeos.valhallammo.playerstats.EntityProperties;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantData;
import me.athlaeos.valhallammo.trading.dom.MerchantType;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Parryer;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Villager;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InteractListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRightClickWeaponAction(PlayerInteractEvent e){
        if (e.useItemInHand() == Event.Result.DENY ||
                ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName())) return;
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            EntityProperties properties = EntityCache.getAndCacheProperties(e.getPlayer());
            boolean dualWielding = properties.getMainHand() != null && properties.getOffHand() != null &&
                    WeightClass.getWeightClass(properties.getMainHand().getMeta()) != WeightClass.WEIGHTLESS &&
                    WeightClass.getWeightClass(properties.getOffHand().getMeta()) != WeightClass.WEIGHTLESS; // only items with a weight class can dual wield attack
            if (!dualWielding) {
                if (e.getHand() == EquipmentSlot.OFF_HAND || e.getPlayer().getAttackCooldown() < 0.9) return;
                Parryer.attemptParry(e.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockConvert(PlayerInteractEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.useItemInHand() == Event.Result.DENY ||
                e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null ||
                !Timer.isCooldownPassed(e.getPlayer().getUniqueId(), "cooldown_block_conversions") ||
                WorldGuardHook.inDisabledRegion(e.getPlayer().getLocation(), e.getPlayer(), WorldGuardHook.VMMO_ABILITIES_BLOCKCONVERSIONS)) return;
        if (BlockInteractConversions.trigger(e.getPlayer(), e.getClickedBlock())) e.setCancelled(true);
        Timer.setCooldown(e.getPlayer().getUniqueId(), 250, "cooldown_block_conversions");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMerchantSummon(PlayerInteractEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || e.useItemInHand() == Event.Result.DENY ||
                e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null || e.getHand() == null) return;
        ItemStack hand = e.getItem();
        if (ItemUtils.isEmpty(hand) || hand.getType() != Material.VILLAGER_SPAWN_EGG) return;
        ItemMeta meta = hand.getItemMeta();
        MerchantType merchantType = meta == null ? null : CustomMerchantManager.getSummonType(meta);
        if (merchantType == null) return;
        for (Villager.Profession profession : CustomMerchantManager.getMerchantConfigurations().keySet()){
            if (CustomMerchantManager.getMerchantConfiguration(profession).getMerchantTypes().contains(merchantType.getType())){
                Villager villager = e.getPlayer().getWorld().spawn(e.getClickedBlock().getLocation().add(0.5, 1, 0.5), Villager.class);
                villager.setProfession(profession);
                villager.setVillagerExperience(1); // to stop it from losing its profession
                MerchantData data = CustomMerchantManager.createMerchant(villager.getUniqueId(), merchantType, e.getPlayer());
                data.setExp(1);
                e.setCancelled(true);

                e.getPlayer().playSound(e.getPlayer(), Sound.ENTITY_VILLAGER_YES, 1F, 1F);
                if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
                if (hand.getAmount() == 1) e.getPlayer().getInventory().setItem(e.getHand(), null);
                else hand.setAmount(hand.getAmount() - 1);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMerchantChangeType(PlayerInteractEntityEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getPlayer().getWorld().getName()) || !(e.getRightClicked() instanceof Villager v)) return;

        ItemStack hand = e.getPlayer().getInventory().getItem(e.getHand());
        if (ItemUtils.isEmpty(hand)) return;
        ItemMeta meta = hand.getItemMeta();
        MerchantType merchantType = meta == null ? null : CustomMerchantManager.getSummonType(meta);
        if (merchantType == null) return;
        for (Villager.Profession profession : CustomMerchantManager.getMerchantConfigurations().keySet()){
            if (CustomMerchantManager.getMerchantConfiguration(profession).getMerchantTypes().contains(merchantType.getType())){
                v.setProfession(profession);
                v.setVillagerExperience(1); // to stop it from losing its profession
                MerchantData data = CustomMerchantManager.createMerchant(v.getUniqueId(), merchantType, e.getPlayer());
                data.setExp(1);
                e.setCancelled(true);

                e.getPlayer().playSound(e.getPlayer(), Sound.ENTITY_VILLAGER_YES, 1F, 1F);
                if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
                if (hand.getAmount() == 1) e.getPlayer().getInventory().setItem(e.getHand(), null);
                else hand.setAmount(hand.getAmount() - 1);
                return;
            }
        }
    }
}
