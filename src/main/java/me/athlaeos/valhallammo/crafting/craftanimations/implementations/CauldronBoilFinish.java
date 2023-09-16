package me.athlaeos.valhallammo.crafting.craftanimations.implementations;

import me.athlaeos.valhallammo.crafting.craftanimations.CraftAnimation;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CauldronBoilFinish extends CraftAnimation {
    private static final Map<Material, Effect> finishEffect = new HashMap<>();

    static {
        // no effect for powdered snow cauldrons
        finishEffect.put(ItemUtils.stringToMaterial("LAVA_CAULDRON", Material.CAULDRON), Effect.EXTINGUISH);
        finishEffect.put(ItemUtils.stringToMaterial("WATER_CAULDRON", Material.CAULDRON), Effect.BREWING_STAND_BREW);
        finishEffect.put(ItemUtils.stringToMaterial("CAULDRON", Material.CAULDRON), Effect.BREWING_STAND_BREW);
    }

    public CauldronBoilFinish(String id) {
        super(id);
    }

    @Override
    public void animate(Player crafter, Block block, int tick) {
        Effect finish = finishEffect.get(block.getType());

        block.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, block.getLocation().add(0.5, 0.5, 0.5), 20);
        if (finish != null) block.getWorld().playEffect(block.getLocation().add(0.5, 0.2, 0.5), finish, 0);
    }
}
