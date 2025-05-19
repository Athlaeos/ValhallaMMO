package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.skills.skills.implementations.ArcherySkill;
import me.athlaeos.valhallammo.utility.MathUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;

import static me.athlaeos.valhallammo.utility.Utils.oldOrNew;

public class ChargedShotAmmo extends Animation {
    private static final Particle ammo;
    private static final Particle.DustOptions ammoOptions;
    static {
        YamlConfiguration config = ConfigManager.getConfig("skills/archery.yml").get();

        ammo = Catch.catchOrElse(() -> Particle.valueOf(config.getString("charged_shot_ammo_particle")), null, "Invalid charged shot ammo particle given in skills/archery.yml charged_shot_ammo_particle");
        ammoOptions = new Particle.DustOptions(Utils.hexToRgb(config.getString("charged_shot_ammo_rgb", "#ffffff")), 0.5f);
    }

    public ChargedShotAmmo(String id) {
        super(id);
    }

    @Override
    public void animate(LivingEntity entity, Location location, Vector direction, int tick) {
        if (entity instanceof Player p) new AmmoAnimation(p, ArcherySkill.getChargedShotCharges(p.getUniqueId())).runTaskTimer(ValhallaMMO.getInstance(), 1L, 1L);
    }

    private static final double cP = MathUtils.cos(MathUtils.toRadians(0));
    private static final double sP = MathUtils.sin(MathUtils.toRadians(0));
    private static final double cY = MathUtils.cos(MathUtils.toRadians(2));
    private static final double sY = MathUtils.sin(MathUtils.toRadians(2));
    private static final double cR = MathUtils.cos(MathUtils.toRadians(0));
    private static final double sR = MathUtils.sin(MathUtils.toRadians(0));

    private static class AmmoAnimation extends BukkitRunnable{
        private final Player p;
        private Collection<Location> circle;
        private int charges;
        private final Location normalizedLocation;

        public AmmoAnimation(Player p, int charges){
            this.p = p;
            this.charges = charges;
            normalizedLocation = new Location(p.getWorld(), 0, 0, 0);
            this.circle = MathUtils.getEvenCircle(normalizedLocation, 0.75, charges, 0);
        }

        @Override
        public void run() {
            int remainingCharges = ArcherySkill.getChargedShotCharges(p.getUniqueId());
            if (!p.isValid() || !p.isOnline() || remainingCharges <= 0) {
                cancel();
                return;
            }
            for (Location l : circle) {
                if (l.getWorld() == null || !l.getWorld().equals(p.getWorld())) l.setWorld(p.getWorld());
                if (ammo == Utils.DUST)
                    p.getWorld().spawnParticle(ammo, l.clone().add(p.getLocation()).add(0, 0.8, 0), 0, ammoOptions);
                else p.getWorld().spawnParticle(ammo, l.clone().add(p.getLocation()).add(0, 0.8, 0), 0);
            }
            // if the charges on the animation doesn't match the remaining charges, reset the circle
            if (remainingCharges != charges) {
                charges = remainingCharges;
                circle = MathUtils.getEvenCircle(normalizedLocation, 0.75, charges, 0);
            }
            MathUtils.transformExistingPointsPredefined(normalizedLocation, cP, sP, cY, sY, cR, sR, 1, circle);
        }
    }
}
