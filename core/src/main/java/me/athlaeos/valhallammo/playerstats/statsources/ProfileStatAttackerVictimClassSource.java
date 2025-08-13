package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.entities.EntityClassification;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.EvEAccumulativeStatSource;
import me.athlaeos.valhallammo.playerstats.format.StatFormat;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ProfileStatAttackerVictimClassSource implements AccumulativeStatSource, EvEAccumulativeStatSource {
    private final Class<? extends Profile> type;
    private final String stat;
    private final Class<? extends Number> numberType;
    private final Profile baseProfile;
    private final double def;
    private final EntityClassification entityClassification;
    private boolean negative = false;

    public ProfileStatAttackerVictimClassSource(Class<? extends Profile> type, String stat, EntityClassification entityClassification){
        this.type = type;
        this.stat = stat;
        this.entityClassification = entityClassification;
        baseProfile = ProfileRegistry.getBlankProfile((UUID) null, type);

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
            throw new IllegalArgumentException("ProfileStatAttackerVictimClassSource:" + type.getSimpleName() + " with stat " + stat +
                    " was initialized, but this profile type does not have such a stat");
        }
    }

    public ProfileStatAttackerVictimClassSource n(){
        this.negative = true;
        return this;
    }

    @Override
    public double fetch(Entity p, boolean use) {
        return 0;
    }

    public StatFormat getFormat(){
        return baseProfile.getNumberStatProperties().get(stat).getFormat();
    }

    @Override
    public double fetch(Entity victim, Entity attackedBy, boolean use) {
        if (attackedBy instanceof Player pl && EntityClassification.matchesClassification(victim, entityClassification)){
            Profile profile = ProfileCache.getOrCache(pl, type);
            String requiredPermission = SkillRegistry.isRegistered(profile.getSkillType()) ? SkillRegistry.getSkill(profile.getSkillType()).getRequiredPermission() : null;
            if (requiredPermission != null && !pl.hasPermission(requiredPermission)) return def;
            if (numberType.equals(Integer.class)) return (negative ? -1 : 1) * profile.getInt(stat);
            if (numberType.equals(Float.class)) return (negative ? -1 : 1) * profile.getFloat(stat);
            return (negative ? -1 : 1) * profile.getDouble(stat);
        }
        return def;
    }
}
