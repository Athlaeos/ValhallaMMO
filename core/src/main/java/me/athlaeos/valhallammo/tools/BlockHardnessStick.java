package me.athlaeos.valhallammo.tools;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.AnimationUtils;
import me.athlaeos.valhallammo.utility.BlockUtils;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.TreeMap;

public class BlockHardnessStick implements Listener {
    private static final NamespacedKey HARDNESS_STICK = new NamespacedKey(ValhallaMMO.getInstance(), "hardness_stick");
    private static final Map<Float, String> hardnessEquivalenceMap = new TreeMap<>();
    private static final ItemStack stick = new ItemBuilder(Material.STICK).name("&dHardness Stick: 1.5")
            .lore("&fShift while scrolling to",
                    "&fchange hardness setting.",
                    "&eThe intensity depends on the",
                    "&eslot this item is held in:",
                    "&e1: 0.05, &62: 0.1, &e3: 0.5",
                    "&e4: 1, &65: 5, &e6: 25",
                    "&c7: 100, &48: 500, &c9: 2500",
                    "",
                    "&fLeft-Click block to change",
                    "&fits hardness to your setting.",
                    "",
                    "&fRight-Click block to remove",
                    "&fits custom hardness").floatTag(HARDNESS_STICK, 1F).get();

    static {
        hardnessEquivalenceMap.put(55F, " &7(&8Reinforced Deepslate&7)");
        hardnessEquivalenceMap.put(50F, " &7(&dObsidian&7)");
        hardnessEquivalenceMap.put(30F, " &7(&8Ancient Debris&7)");
        hardnessEquivalenceMap.put(22.5F, " &7(&dEnder Chest&7)");
        hardnessEquivalenceMap.put(10F, " &7(&7Hardened Glass&7)");
        hardnessEquivalenceMap.put(5F, " &7(&fIron Block&7)");
        hardnessEquivalenceMap.put(4.5F, " &7(&8Deepslate Ores&7)");
        hardnessEquivalenceMap.put(4F, " &7(&fCobweb&7)");
        hardnessEquivalenceMap.put(3.5F, " &7(&7Furnace&7)");
        hardnessEquivalenceMap.put(3F, " &7(&7Regular Ores&7)");
        hardnessEquivalenceMap.put(2.8F, " &7(&9Blue Ice&7)");
        hardnessEquivalenceMap.put(2.5F, " &7(&6Chest&7)");
        hardnessEquivalenceMap.put(2F, " &7(&7Cobblestone&7)");
        hardnessEquivalenceMap.put(1.8F, " &7(&fConcrete&7)");
        hardnessEquivalenceMap.put(1.5F, " &7(&7Stone&7)");
        hardnessEquivalenceMap.put(1.25F, " &7(&cTerracotta&7)");
        hardnessEquivalenceMap.put(1F, " &7(&aMelon&7)");
        hardnessEquivalenceMap.put(0.8F, " &7(&fQuartz Block&7)");
        hardnessEquivalenceMap.put(0.75F, " &7(&fCalcite&7)");
        hardnessEquivalenceMap.put(0.7F, " &7(&7Rails&7)");
        hardnessEquivalenceMap.put(0.6F, " &7(&aGrass Block&7)");
        hardnessEquivalenceMap.put(0.5F, " &7(&6Dirt&7)");
        hardnessEquivalenceMap.put(0.4F, " &7(&cNetherrack&7)");
        hardnessEquivalenceMap.put(0.3F, " &7(&fGlass&7)");
        hardnessEquivalenceMap.put(0.2F, " &7(&aLeaves&7)");
        hardnessEquivalenceMap.put(0.1F, " &7(&fCarpet&7)");
        hardnessEquivalenceMap.put(0F, " &7(&fInstant&7)");
        hardnessEquivalenceMap.put(-1F, " &7(&cUnbreakable&7)");
    }

    private String getMaterialEquivalence(float hardness){
        for (Float f : hardnessEquivalenceMap.keySet()){
            if (hardness + 0.001 > f && hardness - 0.001 < f) return hardnessEquivalenceMap.get(f);
        }
        return "";
    }

    public static ItemStack getStick() {
        return stick;
    }

    private float getIntensity(int slot){
        return switch (slot){
            case 0 -> 0.05F;
            case 1 -> 0.1F;
            case 2 -> 0.5F;
            case 3 -> 1F;
            case 4 -> 5F;
            case 5 -> 25F;
            case 6 -> 100F;
            case 7 -> 500F;
            case 8 -> 2500F;
            default -> 0;
        };
    }
    private boolean increasedSlot(int fromSlot, int toSlot){
        return switch (fromSlot){
            case 0 -> toSlot < 4;
            case 1 -> toSlot > 1;
            case 2 -> toSlot > 2;
            case 3 -> toSlot > 3;
            case 4 -> toSlot > 4;
            case 5 -> toSlot > 5;
            case 6 -> toSlot > 6;
            case 7 -> toSlot > 7;
            case 8 -> toSlot < 5;
            default -> true;
        };
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onScroll(PlayerItemHeldEvent e){
        if (!e.getPlayer().isSneaking()) return;
        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        if (ItemUtils.isEmpty(hand) || hand.getType() != Material.STICK) return;
        ItemBuilder item = new ItemBuilder(hand);
        if (!item.getMeta().getPersistentDataContainer().has(HARDNESS_STICK, PersistentDataType.FLOAT)) return;
        e.setCancelled(true);
        float currentValue = item.getMeta().getPersistentDataContainer().getOrDefault(HARDNESS_STICK, PersistentDataType.FLOAT, -999F);
        if (currentValue <= -999) return;
        float newValue = Math.max(-1, currentValue + (increasedSlot(e.getPreviousSlot(), e.getNewSlot()) ? getIntensity(e.getPreviousSlot()) : -getIntensity(e.getPreviousSlot())));
        item.floatTag(HARDNESS_STICK, newValue);
        item.name(String.format("&dHardness Stick: %.2f%s", newValue, getMaterialEquivalence(newValue)));
        e.getPlayer().getInventory().setItemInMainHand(item.get());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockHardnessTouch(PlayerInteractEvent e){
        if (e.useItemInHand() == Event.Result.DENY || e.getClickedBlock() == null) return;
        Block clicked = e.getClickedBlock();
        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        if (ItemUtils.isEmpty(hand) || hand.getType() != Material.STICK) return;
        ItemBuilder builder = new ItemBuilder(hand);
        if (!builder.getMeta().getPersistentDataContainer().has(HARDNESS_STICK, PersistentDataType.FLOAT)) return;
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            float hardnessSetting = builder.getMeta().getPersistentDataContainer().getOrDefault(HARDNESS_STICK, PersistentDataType.FLOAT, -999F);
            if (hardnessSetting <= -999F) return;
            BlockUtils.setCustomHardness(clicked, hardnessSetting);
            AnimationUtils.outlineBlock(clicked, 10, 0.5F, 0, 255, 0);
            e.setCancelled(true);
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            BlockUtils.removeCustomHardness(clicked);
            AnimationUtils.outlineBlock(clicked, 10, 0.5F, 255, 0, 0);
            e.setCancelled(true);
        }
    }
}
