package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.MathUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

import static me.athlaeos.valhallammo.utility.Utils.oldOrNew;

public class BlockBubbles extends Animation {
    private static final Map<Material, Particle> bubbleParticles = new HashMap<>();
    private static final Map<Material, Particle> smokeParticles = new HashMap<>();
    private static final Map<Material, Sound> boilSound = new HashMap<>();

    static {
        // if lava, powdered snow, and water cauldrons don't exist in this version of minecraft, default to CAULDRON:WATER_BUBBLE
        bubbleParticles.put(ItemUtils.stringToMaterial("LAVA_CAULDRON", Material.CAULDRON), Particle.FLAME);
        bubbleParticles.put(ItemUtils.stringToMaterial("POWDERED_SNOW_CAULDRON", Material.CAULDRON), Particle.END_ROD);
        bubbleParticles.put(ItemUtils.stringToMaterial("WATER_CAULDRON", Material.CAULDRON), Particle.valueOf(oldOrNew("WATER_BUBBLE", "BUBBLE")));

        // powdered snow is the only cauldron with a different particle effect
        Material powderedSnowCauldron = ItemUtils.stringToMaterial("WATER_CAULDRON", null);
        if (powderedSnowCauldron != null) smokeParticles.put(powderedSnowCauldron, Particle.valueOf(oldOrNew("FIREWORKS_SPARK", "FIREWORK")));

        // snow cauldron makes no sound during boiling
        boilSound.put(ItemUtils.stringToMaterial("LAVA_CAULDRON", Material.CAULDRON), Sound.BLOCK_LAVA_AMBIENT);
        boilSound.put(ItemUtils.stringToMaterial("WATER_CAULDRON", Material.CAULDRON), Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT);
        boilSound.put(ItemUtils.stringToMaterial("CAULDRON", Material.CAULDRON), Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT);
    }

    public BlockBubbles(String id) {
        super(id);
    }

    @Override
    public void animate(LivingEntity crafter, Location location, Vector direction, int tick) {
        Block block = location.getBlock();
        Particle bubble = bubbleParticles.getOrDefault(block.getType(), Particle.valueOf(oldOrNew("WATER_BUBBLE", "BUBBLE")));
        Particle smoke = smokeParticles.getOrDefault(block.getType(), Particle.CAMPFIRE_COSY_SMOKE);
        Sound sound = boilSound.get(block.getType());

        if (tick % 20 == 0) {
            for (Location l : MathUtils.getRandomPointsInArea(block.getLocation().add(0.5, 0.8, 0.5), 0.4, 1))
                block.getWorld().spawnParticle(smoke, l, 0, 0, 0.15, 0);
        }
        if (sound != null && tick % 10 == 0) {
            block.getWorld().playSound(block.getLocation().add(0.5, 1, 0.5), Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 4F, 1F);
        }
        if (tick % 4 == 0){
            for (Location l : MathUtils.getRandomPointsInPlane(block.getLocation().add(0.5, 0.95, 0.5), 0.35, 1))
                block.getWorld().spawnParticle(bubble, l, 0, 0, 0.15, 0);
        }
    }
}
