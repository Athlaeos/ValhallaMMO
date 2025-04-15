package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.particle.ParticleWrapper;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Projectile;

import static me.athlaeos.valhallammo.utility.Utils.oldOrNew;

public class AnimationUtils {
    public static void trailProjectile(Projectile projectile, ParticleWrapper particle, int duration){
        new ValhallaRunnable(){
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

    public static void outlineBlock(Block b, int lineDensity, float particleSize, int red, int green, int blue){
        Particle.DustOptions data = new Particle.DustOptions(Color.fromRGB(red, green, blue), particleSize);
        for (Location point : MathUtils.getCubeWithLines(b.getLocation().clone().add(0.5, 0.5, 0.5), lineDensity, 0.5)){
            b.getWorld().spawnParticle(Particle.valueOf(oldOrNew("REDSTONE", "DUST")), point, 0, data);
        }
    }
}
