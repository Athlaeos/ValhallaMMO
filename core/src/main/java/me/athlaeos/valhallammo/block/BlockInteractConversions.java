package me.athlaeos.valhallammo.block;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Scheduling;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BlockInteractConversions {
    private static final Map<String, Conversion> conversions = new HashMap<>();
    private static final Map<Material, Map<Material, Collection<Conversion>>> conversionsByBlock = new HashMap<>();

    public static Map<Material, Map<Material, Collection<Conversion>>> getConversionsByBlock() {
        return conversionsByBlock;
    }

    public static Map<String, Conversion> getConversions() {
        return conversions;
    }

    public static void loadConversions(){
        YamlConfiguration config = ConfigManager.getConfig("block_conversions.yml").reload().get();

        ConfigurationSection conversionSection = config.getConfigurationSection("conversions");
        if (conversionSection != null){
            for (String name : conversionSection.getKeys(false)){
                Material from = Catch.catchOrElse(() -> Material.valueOf(config.getString("conversions." + name + ".from")), null);
                Material to = Catch.catchOrElse(() -> Material.valueOf(config.getString("conversions." + name + ".to")), null);
                if (from == null || to == null) continue;
                Sound sound = Catch.catchOrElse(() -> Sound.valueOf(config.getString("conversions." + name + ".sound")), null);
                Map<Material, Pair<Integer, Boolean>> usableItems = new HashMap<>();
                ConfigurationSection itemSection = config.getConfigurationSection("conversions." + name + ".with");
                if (itemSection != null){
                    for (String i : itemSection.getKeys(false)){
                        Material with = Catch.catchOrElse(() -> Material.valueOf(i.replaceFirst("c:", "")), null);
                        if (with == null) continue;
                        boolean consumed = i.startsWith("c:");
                        int modelData = config.getInt("conversions." + name + ".with." + i, -1);
                        usableItems.put(with, new Pair<>(modelData, consumed));
                    }
                }
                if (usableItems.isEmpty()) {
                    ValhallaMMO.logWarning("Block interact conversion " + name + " has no usable tools to execute it with");
                    continue;
                }
                conversions.put(name, new Conversion(name, from, to, usableItems, sound));
            }
        }

        for (String c : conversions.keySet()){
            Conversion conversion = conversions.get(c);
            for (Material tool : conversion.usableItems.keySet()){
                Map<Material, Collection<Conversion>> conversionsByTool = conversionsByBlock.getOrDefault(conversion.from, new HashMap<>());
                Collection<Conversion> existingConversions = conversionsByTool.getOrDefault(tool, new HashSet<>());
                existingConversions.add(conversion);
                conversionsByTool.put(tool, existingConversions);
                conversionsByBlock.put(conversion.from, conversionsByTool);
            }
        }
    }

    public static boolean trigger(Player user, Block clicked){
        if (!conversionsByBlock.containsKey(clicked.getType()) || !user.isSneaking()) return false;

        ItemBuilder used;
        boolean mainHand;
        if (!ItemUtils.isEmpty(user.getInventory().getItemInMainHand()) &&
                conversionsByBlock.get(clicked.getType()).containsKey(user.getInventory().getItemInMainHand().getType())) {
            mainHand = true;
            used = new ItemBuilder(user.getInventory().getItemInMainHand()); // if main hand isn't null and its type is contained in the conversions by block, try mainhand
        }
        else if (!ItemUtils.isEmpty(user.getInventory().getItemInOffHand()) &&
                conversionsByBlock.get(clicked.getType()).containsKey(user.getInventory().getItemInOffHand().getType())){
            mainHand = false;
            used = new ItemBuilder(user.getInventory().getItemInOffHand()); // if off hand isn't null and its type is contained in the conversions by block, try offhand
        } else return false; // both hands are empty or not contained, so do nothing

        Collection<Conversion> possibleConversion = conversionsByBlock.get(clicked.getType()).get(used.getItem().getType());
        PowerProfile profile = ProfileCache.getOrCache(user, PowerProfile.class);
        for (Conversion conversion : possibleConversion){
            if (!profile.getUnlockedBlockConversions().contains(conversion.name)) continue;

            Pair<Integer, Boolean> data = conversion.usableItems.get(used.getItem().getType());
            if (data.getOne() >= 0 && !used.getMeta().hasCustomModelData()) continue; // custom model data is required, but item has none
            if (data.getOne() >= 0 && used.getMeta().getCustomModelData() != data.getOne()) continue; // custom model data is required, but item has wrong data

            ItemStack item = mainHand ? user.getInventory().getItemInMainHand() : user.getInventory().getItemInOffHand();
            BlockPlaceEvent event = new BlockPlaceEvent(clicked,
                    clicked.getState(),
                    clicked.getLocation().add(0, -1, 0).getBlock(),
                    item,
                    user, true, mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
            ValhallaMMO.getInstance().getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) return false;
            if (clicked.getBlockData() instanceof Stairs s1) Scheduling.runLocationTask(ValhallaMMO.getInstance(), clicked.getLocation(), 1L, () -> { if (clicked.getBlockData() instanceof Stairs s2) s2.setShape(s1.getShape()); });
            if (clicked.getBlockData() instanceof Slab s1) Scheduling.runLocationTask(ValhallaMMO.getInstance(), clicked.getLocation(), 1L, () -> { if (clicked.getBlockData() instanceof Slab s2) s2.setType(s1.getType()); });

            clicked.setType(conversion.to);
            if (conversion.sound != null) user.playSound(user.getLocation(), conversion.sound, 1F, 1F);
            if (!data.getTwo()) return true;
            if (used.getMeta() instanceof Damageable && used.getItem().getType().getMaxDurability() > 0) {
                if (ItemUtils.damageItem(user, item, 1, mainHand ? EntityEffect.BREAK_EQUIPMENT_MAIN_HAND : EntityEffect.BREAK_EQUIPMENT_OFF_HAND, true)){
                    if (mainHand) user.getInventory().setItemInMainHand(null);
                    else user.getInventory().setItemInOffHand(null);
                }
            } else {
                if (item.getAmount() <= 1) {
                    if (mainHand) user.getInventory().setItemInMainHand(null);
                    else user.getInventory().setItemInOffHand(null);
                } else item.setAmount(item.getAmount() - 1);
            }
            return true;
        }
        return false;
    }

    public record Conversion(String name, Material from, Material to, Map<Material, Pair<Integer, Boolean>> usableItems, Sound sound) { }
}
