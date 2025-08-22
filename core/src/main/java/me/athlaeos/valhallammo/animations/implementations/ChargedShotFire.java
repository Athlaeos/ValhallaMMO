package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class ChargedShotFire extends Animation {
    private final Sound fireSound;
    private final float fireVolume;
    private final float firePitch;

    public ChargedShotFire(String id) {
        super(id);
        YamlConfiguration config = ConfigManager.getConfig("skills/archery.yml").get();
        fireSound = Utils.getSound(config.getString("charged_shot_fire_sound"), null, "Invalid charged shot fire sound given in skills/archery.yml charged_shot_fire_sound");
        fireVolume = (float) config.getDouble("charged_shot_fire_sound_volume");
        firePitch = (float) config.getDouble("charged_shot_fire_sound_pitch");
    }

    @Override
    public void animate(LivingEntity entity, Location location, Vector direction, int tick) {
        if (fireSound != null) entity.getWorld().playSound(location, fireSound, fireVolume, firePitch);
    }
}
