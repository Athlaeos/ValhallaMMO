package me.athlaeos.valhallammo.animations.implementations;

import me.athlaeos.valhallammo.animations.Animation;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ElementalBladeExpiration extends Animation {

    private static Sound sound = null;

    public ElementalBladeExpiration(String id) {
        super(id);
        sound = Utils.getSound(ConfigManager.getConfig("skills/enchanting.yml").get().getString("elemental_blade_expiration_sound"), null, null);
    }

    @Override
    public void animate(LivingEntity entity, Location location, Vector direction, int tick) {
        if (!(entity instanceof Player p)) return;
        p.getWorld().playSound(p.getLocation(), sound, 1F, 1F);
    }
}
