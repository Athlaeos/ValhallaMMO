package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.skills.skills.implementations.ArcherySkill;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ArcheryChargedShotVelocitySource implements AccumulativeStatSource {
    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (statPossessor instanceof Player p){
            ArcherySkill.ChargedShotUser user = ArcherySkill.getChargedShotUsers().get(p.getUniqueId());
            if (user == null || user.getCharges() <= 0) return 0;
            return user.getVelocityBuff();
        }
        return 0;
    }
}
