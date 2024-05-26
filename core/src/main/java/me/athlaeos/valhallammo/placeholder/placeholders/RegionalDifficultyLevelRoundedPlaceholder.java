package me.athlaeos.valhallammo.placeholder.placeholders;

import me.athlaeos.valhallammo.entities.MonsterScalingManager;
import me.athlaeos.valhallammo.placeholder.Placeholder;
import org.bukkit.entity.Player;

public class RegionalDifficultyLevelRoundedPlaceholder extends Placeholder {
    public RegionalDifficultyLevelRoundedPlaceholder(String placeholder) {
        super(placeholder);
    }

    @Override
    public String parse(String s, Player p) {
        return s.replace(this.placeholder, String.format("%,d", (int) Math.round(MonsterScalingManager.getAreaDifficultyLevel(p.getLocation()))));
    }
}
