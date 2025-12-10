package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class HitRadiantFlash extends Animation {

    public HitRadiantFlash(String id) {
        super(id);
    }

    @Override
    public void animate(LivingEntity entity, Location location, Vector direction, int tick) {
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_21_9)) {
            entity.getWorld().spawnParticle(Particle.FLASH, location, 0, Color.WHITE);
        } else {
            entity.getWorld().spawnParticle(Particle.FLASH, location, 0);
        }
        entity.getWorld().playSound(location, Sound.ITEM_TRIDENT_RETURN, .5F, 1F);
    }
}
