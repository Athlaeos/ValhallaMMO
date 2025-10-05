package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class EntityFlash extends Animation {
    public EntityFlash(String id) {
        super(id);
    }

    @Override
    public void animate(LivingEntity crafter, Location location, Vector direction, int tick) {
        if (location.getWorld() == null) return;
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_21_9)) {
            location.getWorld().spawnParticle(Particle.FLASH, location, 0, Color.WHITE);
        } else {
            location.getWorld().spawnParticle(Particle.FLASH, location, 0);
        }
    }
}
