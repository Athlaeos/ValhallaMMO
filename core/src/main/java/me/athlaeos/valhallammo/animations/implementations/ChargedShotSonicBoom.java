package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.utility.MathUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.Collection;

import static me.athlaeos.valhallammo.utility.Utils.oldOrNew;

public class ChargedShotSonicBoom extends Animation {
    private final Particle sonicBoom;
    private final Particle.DustOptions sonicBoomOptions;

    public ChargedShotSonicBoom(String id) {
        super(id);
        YamlConfiguration config = ConfigManager.getConfig("skills/archery.yml").get();

        sonicBoom = Catch.catchOrElse(() -> Particle.valueOf(config.getString("charged_shot_sonic_boom_particle")), null, "Invalid charged shot sonic boom particle given in skills/archery.yml charged_shot_sonic_boom_particle");
        sonicBoomOptions = new Particle.DustOptions(Utils.hexToRgb(config.getString("charged_shot_sonic_boom_rgb", "#ffffff")), 0.5f);
    }

    @Override
    public void animate(LivingEntity entity, Location location, Vector direction, int tick) {
        if (sonicBoom == null) return;
        Location normalizedCenter = new Location(entity.getWorld(), 0, 0, 0);
        Location largeCircleCenter = new Location(entity.getWorld(), 0, 10, 0);
        Location middleCircleCenter = new Location(entity.getWorld(), 0, 15, 0);
        Location smallCircleCenter = new Location(entity.getWorld(), 0, 20, 0);

        Collection<Location> largeCircle = MathUtils.getRandomPointsInCircle(largeCircleCenter, 0.5, 30, true);
        Collection<Location> middleCircle = MathUtils.getRandomPointsInCircle(middleCircleCenter, 0.5, 30, true);
        Collection<Location> smallCircle = MathUtils.getRandomPointsInCircle(smallCircleCenter, 0.5, 30, true);

        MathUtils.transformExistingPoints(normalizedCenter, 0, entity.getEyeLocation().getPitch() + 90, 0, 1, largeCircle);
        MathUtils.transformExistingPoints(normalizedCenter, entity.getEyeLocation().getYaw() + 180, 0, 0, 1, largeCircle);
        MathUtils.transformExistingPoints(normalizedCenter, 0, entity.getEyeLocation().getPitch() + 90, 0, 1, middleCircle);
        MathUtils.transformExistingPoints(normalizedCenter, entity.getEyeLocation().getYaw() + 180, 0, 0, 1, middleCircle);
        MathUtils.transformExistingPoints(normalizedCenter, 0, entity.getEyeLocation().getPitch() + 90, 0, 1, smallCircle);
        MathUtils.transformExistingPoints(normalizedCenter, entity.getEyeLocation().getYaw() + 180, 0, 0, 1, smallCircle);

        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> largeCircle.forEach(l -> pulse(entity.getWorld(), entity.getEyeLocation(), l, largeCircleCenter, 0.25)), 2L);
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> middleCircle.forEach(l -> pulse(entity.getWorld(), entity.getEyeLocation(), l, middleCircleCenter, 0.15)), 4L);
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> smallCircle.forEach(l -> pulse(entity.getWorld(), entity.getEyeLocation(), l, smallCircleCenter, 0.1)), 6L);
    }

    private void pulse(World w, Location location, Location particleLocation, Location center, double intensity){
        if (particleLocation.equals(center) || location.getWorld() == null || particleLocation.getWorld() == null) return;
        if (!particleLocation.getWorld().equals(location.getWorld())) return;
        if (sonicBoom == Utils.DUST)
            w.spawnParticle(sonicBoom, location.add(particleLocation), 0, sonicBoomOptions);
        else w.spawnParticle(sonicBoom, location.add(particleLocation), 0,
                (particleLocation.getX() - center.getX()) * intensity,
                (particleLocation.getY() - center.getY()) * intensity,
                (particleLocation.getZ() - center.getZ()) * intensity
        );
    }
}
