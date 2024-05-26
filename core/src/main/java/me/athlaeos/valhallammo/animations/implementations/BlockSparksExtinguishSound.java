package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

import static me.athlaeos.valhallammo.utility.Utils.oldOrNew;

public class BlockSparksExtinguishSound extends Animation {
    private static final Map<Material, Effect> finishEffect = new HashMap<>();

    static {
        // no effect for powdered snow cauldrons
        finishEffect.put(ItemUtils.stringToMaterial("LAVA_CAULDRON", Material.CAULDRON), Effect.EXTINGUISH);
        finishEffect.put(ItemUtils.stringToMaterial("WATER_CAULDRON", Material.CAULDRON), Effect.BREWING_STAND_BREW);
        finishEffect.put(ItemUtils.stringToMaterial("CAULDRON", Material.CAULDRON), Effect.BREWING_STAND_BREW);
    }

    public BlockSparksExtinguishSound(String id) {
        super(id);
    }

    @Override
    public void animate(LivingEntity crafter, Location location, Vector direction, int tick) {
        Block block = location.getBlock();
        Effect finish = finishEffect.get(block.getType());

        block.getWorld().spawnParticle(Particle.valueOf(oldOrNew("FIREWORKS_SPARK", "FIREWORK")), block.getLocation().add(0.5, 0.5, 0.5), 20);
        if (finish != null) block.getWorld().playEffect(block.getLocation().add(0.5, 0.2, 0.5), finish, 0);
    }
}
