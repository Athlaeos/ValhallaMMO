package me.athlaeos.valhallammo.crafting.craftanimations.implementations;

import me.athlaeos.valhallammo.crafting.craftanimations.CraftAnimation;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ImmersiveCraftFinish extends CraftAnimation {

    private static final Map<Material, Sound> blockSounds = new HashMap<>();
    private static Sound defaultSound = Sound.ITEM_AXE_STRIP;
    static {
        setBlockSound(Material.ANVIL, Sound.ENTITY_VILLAGER_WORK_TOOLSMITH);
        setBlockSound(Material.CHIPPED_ANVIL, Sound.ENTITY_VILLAGER_WORK_TOOLSMITH);
        setBlockSound(Material.DAMAGED_ANVIL, Sound.ENTITY_VILLAGER_WORK_TOOLSMITH);
        setBlockSound(Material.CRAFTING_TABLE, Sound.ENTITY_VILLAGER_WORK_FISHERMAN);
        setBlockSound(Material.GRINDSTONE, Sound.ENTITY_VILLAGER_WORK_WEAPONSMITH);
        setBlockSound(Material.SMITHING_TABLE, Sound.ENTITY_VILLAGER_WORK_TOOLSMITH);
        setBlockSound(Material.LECTERN, Sound.BLOCK_ENCHANTMENT_TABLE_USE);
        setBlockSound(Material.FLETCHING_TABLE, Sound.ITEM_CROSSBOW_LOADING_MIDDLE);
        setBlockSound(Material.CAULDRON, Sound.BLOCK_LAVA_EXTINGUISH);
        setBlockSound("WATER_CAULDRON", Sound.BLOCK_LAVA_EXTINGUISH);
        setBlockSound("LAVA_CAULDRON", Sound.BLOCK_LAVA_AMBIENT);
        setBlockSound("POWDERED_SNOW_CAULDRON", Sound.BLOCK_LAVA_EXTINGUISH);
        setBlockSound(Material.CARTOGRAPHY_TABLE, Sound.ENTITY_VILLAGER_WORK_LIBRARIAN);
        setBlockSound(Material.LOOM, Sound.BLOCK_WOOL_PLACE);
        setBlockSound(Material.STONECUTTER, Sound.UI_STONECUTTER_TAKE_RESULT);
    }

    public ImmersiveCraftFinish(String id) {
        super(id);
    }

    public static void setBlockSound(Material block, Sound sound){
        blockSounds.put(block, sound);
    }

    public static void setBlockSound(String block, Sound sound){
        Material b = ItemUtils.stringToMaterial(block, null);
        if (b != null) blockSounds.put(b, sound);
    }

    @Override
    public void animate(Player crafter, Block block, int tick) {
        block.getWorld().playSound(block.getLocation(), blockSounds.getOrDefault(block.getType(), defaultSound), .3F, 1F);
        block.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, block.getLocation().add(0.5, 1, 0.5), 15);
    }

    public static void setDefaultSound(Sound defaultSound) {
        ImmersiveCraftFinish.defaultSound = defaultSound;
    }

    public static Sound getDefaultSound() {
        return defaultSound;
    }

    public static Map<Material, Sound> getBlockSounds() {
        return blockSounds;
    }
}
