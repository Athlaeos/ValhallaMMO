package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MiningProfile;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.skills.skills.implementations.MiningSkill;
import me.athlaeos.valhallammo.utility.Timer;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MiningDrillingActiveSource implements AccumulativeStatSource {
    @Override
    public double fetch(Entity statPossessor, boolean use) {
        MiningSkill miningSkill = SkillRegistry.isRegistered(MiningSkill.class) ? (MiningSkill) SkillRegistry.getSkill(MiningSkill.class) : null;
        if (statPossessor instanceof Player p && miningSkill != null && !Timer.isCooldownPassed(statPossessor.getUniqueId(), "mining_drilling_duration")){
            Block b = p.getTargetBlockExact(8);
            if (b == null || !miningSkill.getDropsExpValues().containsKey(b.getType())) return 0;
            MiningProfile profile = ProfileCache.getOrCache(p, MiningProfile.class);
            return profile.getDrillingSpeedBonus();
        }
        return 0;
    }
}
