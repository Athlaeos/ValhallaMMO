package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Timer;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class DrillingActive extends Animation {
    private static final Particle particle;
    private static final Particle.DustOptions options;
    private static final String drillingOff;
    private static final Sound drillingActiveSound;
    static {
        YamlConfiguration config = ConfigManager.getConfig("skills/mining.yml").get();

        particle = Catch.catchOrElse(() -> Particle.valueOf(config.getString("drilling_effect_particle")), null, "Invalid drilling particle given in skills/mining.yml drilling_effect_particle");
        options = new Particle.DustOptions(Utils.hexToRgb(config.getString("drilling_effect_color", "#ffffff")), 0.5f);
        drillingOff = TranslationManager.translatePlaceholders(config.getString("drilling_toggle_off"));
        drillingActiveSound = Catch.catchOrElse(() -> Sound.valueOf(config.getString("drilling_active_sound")), null, "Invalid drilling active sound given in skills/mining.yml drilling_active_sound");
    }

    public DrillingActive(String id) {
        super(id);
    }

    @Override
    public void animate(LivingEntity entity, Location location, Vector direction, int tick) {
        AccumulativeStatManager.updateStats(entity);
        if (entity instanceof Player p) new DrillingAnimation(p).runTaskTimer(ValhallaMMO.getInstance(), 1L, 1L);
    }

    private static class DrillingAnimation extends BukkitRunnable{
        private final Player p;
        private int tick = -1;
        public DrillingAnimation(Player p){
            this.p = p;
        }

        @Override
        public void run() {
            tick++;
            if (!p.isValid() || !p.isOnline() || Timer.isCooldownPassed(p.getUniqueId(), "mining_drilling_duration")
            || ItemUtils.isEmpty(p.getInventory().getItemInMainHand()) || !p.getInventory().getItemInMainHand().getType().toString().endsWith("_PICKAXE")) {
                cancel();
                Utils.sendActionBar(p, drillingOff);
                AccumulativeStatManager.updateStats(p);
                Timer.setCooldown(p.getUniqueId(), 0, "mining_drilling_duration");
            }
            RayTraceResult result = p.rayTraceBlocks(5, FluidCollisionMode.NEVER);
            if (result == null) return;
            Block hit = result.getHitBlock();
            if (hit == null) return;
            if (particle == Particle.REDSTONE)
                p.spawnParticle(particle, result.getHitPosition().toLocation(p.getWorld()), 0, options);
            else if (particle == Particle.BLOCK_DUST)
                p.spawnParticle(particle, result.getHitPosition().toLocation(p.getWorld()), 0, hit.getBlockData());
            else p.spawnParticle(particle, result.getHitPosition().toLocation(p.getWorld()), 0);
            p.playSound(p, drillingActiveSound, .1F, 1F);
        }
    }
}
