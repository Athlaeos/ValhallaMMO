package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class ChargedShotActivation extends Animation {
    private final Sound prefireSound;
    private final float prefireVolume;
    private final float prefirePitch;

    public ChargedShotActivation(String id) {
        super(id);
        YamlConfiguration config = ConfigManager.getConfig("skills/archery.yml").get();
        prefireSound = Catch.catchOrElse(() -> Sound.valueOf(config.getString("charged_shot_prefire_sound")), null, "Invalid charged shot prefire sound given in skills/archery.yml charged_shot_prefire_sound");
        prefireVolume = (float) config.getDouble("charged_shot_prefire_sound_volume");
        prefirePitch = (float) config.getDouble("charged_shot_prefire_sound_pitch");
    }

    @Override
    public void animate(LivingEntity entity, Location location, Vector direction, int tick) {
        if (prefireSound != null) entity.getWorld().playSound(location, prefireSound, prefireVolume, prefirePitch);


    }
}
