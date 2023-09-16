package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.particle.ParticleWrapper;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;

public class AnimationUtils {
    public static void trailProjectile(Projectile projectile, ParticleWrapper particle, int duration){
        new BukkitRunnable(){
            int count = 0;
            @Override
            public void run() {
                if (count >= duration) cancel();
                else if (projectile != null && projectile.isValid()) particle.spawnParticle(projectile.getLocation(), 0);
                else cancel();

                count++;
            }
        }.runTaskTimer(ValhallaMMO.getInstance(), 1L, 1L);
    }
}
