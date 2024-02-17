package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.MiningProfile;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.skills.skills.implementations.MiningSkill;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MiningStatSource implements AccumulativeStatSource {
    private final String stat;
    private final Class<? extends Number> numberType;
    private final double def;

    /**
     * Grants stats only if the player is directly looking at a block the MiningSkill can grant exp for
     */
    public MiningStatSource(String stat){
        this.stat = stat;

        Profile baseProfile = ProfileRegistry.getBlankProfile(null, MiningProfile.class);
        if (baseProfile.intStatNames().contains(stat)) {
            def = baseProfile.getDefaultInt(stat);
            numberType = Integer.class;
        } else if (baseProfile.floatStatNames().contains(stat)) {
            def = baseProfile.getDefaultFloat(stat);
            numberType = Float.class;
        } else if (baseProfile.doubleStatNames().contains(stat)) {
            def = baseProfile.getDefaultDouble(stat);
            numberType = Double.class;
        } else {
            def = 0;
            numberType = null;
        }

        if (numberType == null) {
            throw new IllegalArgumentException("MiningStatSource:" + MiningProfile.class.getSimpleName() + " with stat " + stat +
                    " was initialized, but this profile type does not have such a stat");
        }
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        MiningSkill miningSkill = SkillRegistry.isRegistered(MiningSkill.class) ? (MiningSkill) SkillRegistry.getSkill(MiningSkill.class) : null;
        if (statPossessor instanceof Player p && miningSkill != null){
            Block b = p.getTargetBlockExact(8);
            if (b == null || !miningSkill.getDropsExpValues().containsKey(b.getType())) return def;
            MiningProfile profile = ProfileCache.getOrCache(p, MiningProfile.class);
            if (numberType.equals(Integer.class)) return profile.getInt(stat);
            if (numberType.equals(Float.class)) return profile.getFloat(stat);
            return profile.getDouble(stat);
        }
        return def;
    }
}
