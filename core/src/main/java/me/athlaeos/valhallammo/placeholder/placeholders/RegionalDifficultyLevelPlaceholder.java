package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.entities.MonsterScalingManager;
import me.athlaeos.valhallammo.placeholder.Placeholder;
import org.bukkit.entity.Player;

public class RegionalDifficultyLevelPlaceholder extends Placeholder {
    public RegionalDifficultyLevelPlaceholder(String placeholder) {
        super(placeholder);
    }

    @Override
    public String parse(String s, Player p) {
        return s.replace(this.placeholder, String.format("%,.1f", MonsterScalingManager.getAreaDifficultyLevel(p.getLocation())));
    }
}
