package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.WoodcuttingProfile;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.skills.skills.implementations.WoodcuttingSkill;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class WoodcuttingStatSource implements AccumulativeStatSource {
    private final String stat;
    private final Class<? extends Number> numberType;
    private final double def;

    /**
     * Grants stats only if the player is directly looking at a block the MiningSkill can grant exp for
     */
    public WoodcuttingStatSource(String stat){
        this.stat = stat;

        Profile baseProfile = ProfileRegistry.getBlankProfile((UUID) null, WoodcuttingProfile.class);
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
            throw new IllegalArgumentException("WoodcuttingStatSource:" + WoodcuttingProfile.class.getSimpleName() + " with stat " + stat +
                    " was initialized, but this profile type does not have such a stat");
        }
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        WoodcuttingSkill woodcuttingSkill = SkillRegistry.isRegistered(WoodcuttingSkill.class) ? (WoodcuttingSkill) SkillRegistry.getSkill(WoodcuttingSkill.class) : null;
        if (statPossessor instanceof Player p && woodcuttingSkill != null){
            Block b = p.getTargetBlockExact(8);
            if (b == null || !woodcuttingSkill.getDropsExpValues().containsKey(b.getType())) return def;

            WoodcuttingProfile profile = ProfileCache.getOrCache(p, WoodcuttingProfile.class);
            if (numberType.equals(Integer.class)) return profile.getInt(stat);
            if (numberType.equals(Float.class)) return profile.getFloat(stat);
            return profile.getDouble(stat);
        }
        return def;
    }
}
