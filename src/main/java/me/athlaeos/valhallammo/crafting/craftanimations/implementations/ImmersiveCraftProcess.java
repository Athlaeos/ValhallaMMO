package me.athlaeos.valhallammo.crafting.craftanimations.implementations;

import me.athlaeos.valhallammo.crafting.craftanimations.CraftAnimation;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.MathUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ImmersiveCraftProcess extends CraftAnimation {

    private static final Map<Material, Sound> blockSounds = new HashMap<>();
    private static Sound defaultSound = Sound.ITEM_AXE_STRIP;
    static {
        setBlockSound(Material.ANVIL, Sound.BLOCK_ANVIL_PLACE);
        setBlockSound(Material.CHIPPED_ANVIL, Sound.BLOCK_ANVIL_PLACE);
        setBlockSound(Material.DAMAGED_ANVIL, Sound.BLOCK_ANVIL_PLACE);
        setBlockSound(Material.CRAFTING_TABLE, Sound.ITEM_AXE_STRIP);
        setBlockSound(Material.GRINDSTONE, Sound.ENTITY_VILLAGER_WORK_WEAPONSMITH);
        setBlockSound(Material.SMITHING_TABLE, Sound.BLOCK_ANVIL_PLACE);
        setBlockSound(Material.LECTERN, Sound.BLOCK_ENCHANTMENT_TABLE_USE);
        setBlockSound(Material.FLETCHING_TABLE, Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER);
        setBlockSound(Material.CAULDRON, Sound.BLOCK_LAVA_EXTINGUISH);
        setBlockSound("WATER_CAULDRON", Sound.BLOCK_LAVA_EXTINGUISH);
        setBlockSound("LAVA_CAULDRON", Sound.ITEM_FIRECHARGE_USE);
        setBlockSound("POWDERED_SNOW_CAULDRON", Sound.BLOCK_LAVA_EXTINGUISH);
        setBlockSound(Material.CARTOGRAPHY_TABLE, Sound.ENTITY_VILLAGER_WORK_LIBRARIAN);
        setBlockSound(Material.LOOM, Sound.ENTITY_VILLAGER_WORK_SHEPHERD);
        setBlockSound(Material.STONECUTTER, Sound.UI_STONECUTTER_TAKE_RESULT);
    }

    public ImmersiveCraftProcess(String id) {
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
        block.getWorld().playSound(block.getLocation(), blockSounds.getOrDefault(block.getType(), defaultSound), 1F, 1F);
        Location origin = block.getLocation().add(0.5, 1.1, 0.5);
        if (block.getType() == Material.GRINDSTONE){
            for (Location l : MathUtils.getRandomPointsInPlane(origin, 0.2, 3)){
                block.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, l, 0,
                        (origin.getX() - crafter.getLocation().getX()) * 0.09,
                        0,
                        (origin.getZ() - crafter.getLocation().getZ()) * 0.09);
            }
        } else {
            for (Location l : MathUtils.getRandomPointsInPlane(origin, 0.35, 3)){
                block.getWorld().spawnParticle(Particle.BLOCK_DUST, l, 1, block.getBlockData());
            }
        }
    }

    public static void setDefaultSound(Sound defaultSound) {
        ImmersiveCraftProcess.defaultSound = defaultSound;
    }

    public static Sound getDefaultSound() {
        return defaultSound;
    }

    public static Map<Material, Sound> getBlockSounds() {
        return blockSounds;
    }
}
